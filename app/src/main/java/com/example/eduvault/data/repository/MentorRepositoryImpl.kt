package com.example.eduvault.data.repository

import com.example.eduvault.domain.model.*
import com.example.eduvault.domain.repository.MentorRepository
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Source
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MentorRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
) : MentorRepository {
    private suspend fun mentorUid(): String {
        val user = auth.currentUser ?: error("Vui lòng đăng nhập lại.")
        check(user.getIdToken(false).await().claims["mentor"] == true) {
            "Tài khoản chưa được cấp quyền mentor."
        }
        return user.uid
    }

    override suspend fun getAssignedLearners(): Result<List<MentorLearner>> = safely {
        val mentorId = mentorUid()
        val assignments = firestore.collection("mentorAssignments").document(mentorId)
            .collection("learners").get(Source.SERVER).await()
        val programs = mutableMapOf<String, TrainingProgram>()
        assignments.documents.map { binding ->
            val learnerId = binding.id
            val programId = binding.getString("programId") ?: error("Phân công chưa có chương trình.")
            val profile = firestore.collection("users").document(learnerId).get(Source.SERVER).await()
            check(profile.exists()) { "Không tìm thấy hồ sơ học viên được giao." }
            val program = programs[programId] ?: firestore.collection("trainingPrograms")
                .document(programId).get(Source.SERVER).await().let {
                    check(it.exists()) { "Chương trình được giao chưa sẵn sàng." }
                    it.data.orEmpty().toTrainingProgram(it.id)
                }.also { programs[programId] = it }
            val assignment = profile.reference.collection("trainingAssignments")
                .document(programId).get(Source.SERVER).await()
            check(assignment.exists()) { "Học viên chưa nhận lộ trình. Hãy yêu cầu học viên đăng nhập trước." }
            val data = assignment.data.orEmpty()
            MentorLearner(learnerId, profile.getString("name") ?: profile.getString("fullName") ?: "Học viên",
                profile.getString("email") ?: "",
                data.toTrainingDashboard(program),
                evidenceVersion(data))
        }
    }

    override suspend fun reviewEvidence(learner: MentorLearner, approved: Boolean, feedback: String): Result<Unit> = safely {
        val mentorId = mentorUid()
        mentorReviewError(approved, feedback)?.let { error(it) }
        val programId = learner.dashboard.program.id
        val ref = firestore.collection("users").document(learner.uid)
            .collection("trainingAssignments").document(programId)
        val programRef = firestore.collection("trainingPrograms").document(programId)
        firestore.runTransaction { tx ->
            val program = tx.get(programRef).data.orEmpty().toTrainingProgram(programId)
            val data = tx.get(ref).data ?: error("Lộ trình không còn tồn tại.")
            val evidence = data.toTrainingEvidence() ?: error("Không có bằng chứng chờ duyệt.")
            val assignment = data.toTrainingAssignment(programId)
            val dashboard = TrainingDashboard(program, assignment, evidence)
            check(evidence.status == "PENDING" && dashboard.canSubmit(evidence.moduleId)) {
                "Bằng chứng đã được xử lý. Hãy tải lại danh sách."
            }
            check(evidenceVersion(data) == learner.evidenceVersion) {
                "Học viên đã gửi bằng chứng mới. Hãy tải lại trước khi nghiệm thu."
            }
            val reviewedEvidence = (data["evidence"] as Map<*, *>).entries.associate { it.key.toString() to it.value }.toMutableMap()
            reviewedEvidence.putAll(mapOf("status" to if (approved) "APPROVED" else "REJECTED",
                "feedback" to feedback.trim(), "reviewedBy" to mentorId,
                "reviewedAt" to FieldValue.serverTimestamp()))
            val completed = if (approved) assignment.completedModuleIds.toList() + evidence.moduleId
                else assignment.completedModuleIds.toList()
            check(program.modules.isNotEmpty()) { "Chương trình chưa có chặng học." }
            tx.update(ref, mapOf("evidence" to reviewedEvidence, "completedModuleIds" to completed,
                "completionPercent" to if (approved) completed.size * 100 / program.modules.size else assignment.completionPercent,
                "status" to if (completed.size == program.modules.size) "COMPLETED" else "IN_PROGRESS",
                "reviewedEvidence" to ((data["reviewedEvidence"] as? Map<*, *>).orEmpty().entries.associate { it.key.toString() to it.value } + (evidence.moduleId to reviewedEvidence)),
                "updatedAt" to FieldValue.serverTimestamp()))
        }.await()
        // The completed transaction is server-acknowledged. Reloading the list
        // is separate, so a read failure cannot turn this successful write into a failure.
        Unit
    }

    override suspend fun saveTask(learner: MentorLearner, moduleId: String, task: TrainingTaskDefinition?): Result<Unit> = safely {
        val mentorId = mentorUid()
        task?.let { trainingTaskError(it)?.let { message -> error(message) } }
        val programId = learner.dashboard.program.id
        val ref = firestore.collection("users").document(learner.uid)
            .collection("trainingAssignments").document(programId)
        val programRef = firestore.collection("trainingPrograms").document(programId)
        firestore.runTransaction { tx ->
            val program = tx.get(programRef).data.orEmpty().toTrainingProgram(programId)
            val data = tx.get(ref).data ?: error("Không tìm thấy lộ trình học viên.")
            check(program.modules.any { it.id == moduleId }) { "Chặng học không tồn tại." }
            val dashboard = data.toTrainingDashboard(program)
            check(moduleId !in dashboard.assignment.completedModuleIds) { "Không thể đổi nhiệm vụ đã nghiệm thu." }
            check(!(dashboard.evidence?.moduleId == moduleId && dashboard.evidence.status == "PENDING")) {
                "Học viên đã nộp bài. Hãy nghiệm thu hoặc yêu cầu bổ sung trước khi đổi nhiệm vụ."
            }
            check(dashboard.assignment.taskDefinitions[moduleId] == learner.dashboard.assignment.taskDefinitions[moduleId]) {
                "Nhiệm vụ đã được thay đổi. Hãy tải lại trước khi lưu."
            }
            val definitions = (data["taskDefinitions"] as? Map<*, *>).orEmpty().entries
                .associate { it.key.toString() to it.value }.toMutableMap()
            if (task == null) definitions.remove(moduleId) else definitions[moduleId] = mapOf(
                "title" to task.title.trim(), "instructions" to task.instructions.trim(),
                "dueDate" to task.dueDate.trim(), "resourceLink" to task.resourceLink.trim(),
                "updatedBy" to mentorId, "updatedAt" to FieldValue.serverTimestamp())
            tx.update(ref, mapOf("taskDefinitions" to definitions, "taskEditModuleId" to moduleId,
                "updatedAt" to FieldValue.serverTimestamp()))
        }.await()
        Unit
    }

    private fun evidenceVersion(data: Map<String, Any>): String {
        val evidence = data["evidence"] as? Map<*, *> ?: return ""
        val timestamp = evidence["submittedAt"] as? Timestamp ?: return ""
        return "${timestamp.seconds}:${timestamp.nanoseconds}"
    }

    private suspend fun <T> safely(action: suspend () -> T): Result<T> = try {
        Result.success(action())
    } catch (cancelled: CancellationException) {
        throw cancelled
    } catch (error: Exception) {
        val message = when ((error as? FirebaseFirestoreException)?.code) {
            FirebaseFirestoreException.Code.PERMISSION_DENIED -> "Bạn chưa được phân công hoặc không có quyền nghiệm thu học viên này."
            FirebaseFirestoreException.Code.UNAVAILABLE -> "Chưa kết nối được. Vui lòng kiểm tra mạng rồi thử lại."
            null -> error.message ?: "Không thể xử lý bằng chứng. Vui lòng thử lại."
            else -> "Không thể tải hoặc lưu nghiệm thu. Vui lòng thử lại."
        }
        Result.failure(IllegalStateException(message, error))
    }
}
