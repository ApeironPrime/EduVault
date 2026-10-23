package com.example.eduvault.data.repository

import com.example.eduvault.domain.model.*
import com.example.eduvault.domain.repository.TrainingRepository
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Source
import com.google.firebase.firestore.MetadataChanges
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TrainingRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
) : TrainingRepository {
    companion object { private const val PROGRAM_ID = "software-fresher-foundation" }

    private fun uid() = auth.currentUser?.uid ?: error("Vui lòng đăng nhập để mở lộ trình.")
    private fun assignmentRef(uid: String, programId: String) = firestore.collection("users")
        .document(uid).collection("trainingAssignments").document(programId)

    override suspend fun getMyTrainingProgram(): Result<TrainingDashboard> = runCatching {
        val uid = uid()
        val programSnapshot = firestore.collection("trainingPrograms").document(PROGRAM_ID).get(Source.SERVER).await()
        check(programSnapshot.exists()) { "Chương trình đào tạo đang được chuẩn bị. Vui lòng thử lại sau." }
        val program = programSnapshot.data.orEmpty().toTrainingProgram(programSnapshot.id)
        check(program.modules.isNotEmpty()) { "Chương trình đào tạo chưa có chặng học." }
        val ref = assignmentRef(uid, program.id)
        firestore.runTransaction { transaction ->
            if (!transaction.get(ref).exists()) {
                transaction.set(ref, mapOf(
                    "userId" to uid, "programId" to program.id,
                    "completedModuleIds" to emptyList<String>(), "completionPercent" to 0,
                    "status" to "IN_PROGRESS", "assignedAt" to FieldValue.serverTimestamp(),
                    "dueAt" to Timestamp(java.util.Date(System.currentTimeMillis() + 30L * 86_400_000)),
                    "createdAt" to FieldValue.serverTimestamp(), "updatedAt" to FieldValue.serverTimestamp(),
                ))
            }
        }.await()
        val data = ref.get(Source.SERVER).await().data ?: error("Chưa đọc được lộ trình đã lưu.")
        data.toTrainingDashboard(program)
    }.friendlyFailure()

    override fun observeAssignment(program: TrainingProgram) = callbackFlow {
        val listener = assignmentRef(uid(), program.id).addSnapshotListener(MetadataChanges.INCLUDE) { snapshot, error ->
            if (error != null) {
                trySend(Result.failure<TrainingDashboard>(error).friendlyFailure())
            } else if (snapshot != null && !snapshot.metadata.hasPendingWrites() && !snapshot.metadata.isFromCache) {
                val data = snapshot.data
                trySend(runCatching {
                    check(data != null) { "Không tìm thấy lộ trình. Vui lòng tải lại." }
                    data.toTrainingDashboard(program)
                }.friendlyFailure())
            }
        }
        awaitClose { listener.remove() }
    }

    override suspend fun setModuleCompletion(programId: String, moduleId: String, completed: Boolean): Result<TrainingAssignment> =
        Result.failure(IllegalStateException("Tiến độ nghiệm thu do quản trị viên cập nhật sau khi kiểm tra bằng chứng."))

    override suspend fun submitEvidence(programId: String, moduleId: String, link: String, note: String): Result<TrainingEvidence> = runCatching {
        val uid = uid()
        trainingEvidenceError(link, note)?.let { error(it) }
        require(programId == PROGRAM_ID) { "Chương trình đào tạo không hợp lệ." }
        val program = firestore.collection("trainingPrograms").document(programId).get(Source.SERVER).await()
        val moduleIds = program.get("moduleIds") as? List<*> ?: error("Chương trình chưa được thiết lập đầy đủ.")
        require(moduleId in moduleIds) { "Chặng học không tồn tại." }
        val ref = assignmentRef(uid, programId)
        firestore.runTransaction { transaction ->
            val assignment = transaction.get(ref)
            check(assignment.exists()) { "Hãy tải lộ trình trước khi nộp bằng chứng." }
            val completed = (assignment.get("completedModuleIds") as? List<*>).orEmpty()
            require(moduleIds.firstOrNull { it !in completed } == moduleId) {
                "Hãy hoàn thành chặng trước và chờ mentor duyệt trước khi tiếp tục."
            }
            transaction.update(ref, mapOf(
                "evidence" to mapOf(
                    "moduleId" to moduleId, "link" to link.trim(), "note" to note.trim(),
                    "status" to "PENDING", "submittedAt" to FieldValue.serverTimestamp(),
                ),
                "updatedAt" to FieldValue.serverTimestamp(),
            ))
        }.await()
        // Transaction.await() confirms the write. A subsequent read failure must not
        // report a failed submission or invite duplicate submissions. Zero denotes
        // an as-yet unknown server time; the assignment listener fills it in.
        confirmedEvidence(TrainingEvidence(moduleId, link.trim(), note.trim(), 0L)) {
            ref.get(Source.SERVER).await().data?.toTrainingEvidence()
        }
    }.friendlyFailure()

    private fun <T> Result<T>.friendlyFailure(): Result<T> = fold({ Result.success(it) }, { error ->
        if (error is CancellationException) throw error
        val message = when ((error as? FirebaseFirestoreException)?.code) {
            FirebaseFirestoreException.Code.PERMISSION_DENIED -> "Tài khoản chưa được cấp quyền tham gia chương trình. Vui lòng liên hệ người phụ trách đào tạo."
            FirebaseFirestoreException.Code.UNAVAILABLE -> "Chưa kết nối được. Vui lòng kiểm tra mạng rồi tải lại."
            else -> if (error is FirebaseFirestoreException) "Không thể tải hoặc lưu dữ liệu đào tạo. Vui lòng thử lại." else error.message ?: "Không thể tải hoặc lưu dữ liệu đào tạo."
        }
        Result.failure(IllegalStateException(message, error))
    })
}
