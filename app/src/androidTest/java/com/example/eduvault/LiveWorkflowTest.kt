package com.example.eduvault

import androidx.test.platform.app.InstrumentationRegistry
import com.example.eduvault.data.repository.MentorRepositoryImpl
import com.example.eduvault.data.repository.TrainingRepositoryImpl
import com.example.eduvault.domain.model.*
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout
import org.junit.Assert.*
import org.junit.Assume.assumeTrue
import org.junit.Test
import java.util.UUID

/** Opt-in: exercises the actual Android repositories against an isolated live Spark fixture. */
class LiveWorkflowTest {
    @Test fun learnerAndMentorCompleteAllStagesUsingRealAndroidRepositories() = runBlocking { withTimeout(240_000) {
        val args = InstrumentationRegistry.getArguments()
        assumeTrue(args.getString("liveWorkflow") == "true")
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val options = FirebaseApp.getInstance().options
        assertEquals("appeduvault", options.projectId)
        val studentApp = FirebaseApp.initializeApp(context, options, "student-qa-${UUID.randomUUID()}")!!
        val mentorApp = FirebaseApp.initializeApp(context, options, "mentor-qa-${UUID.randomUUID()}")!!
        val studentAuth = FirebaseAuth.getInstance(studentApp)
        val mentorAuth = FirebaseAuth.getInstance(mentorApp)
        val studentDb = FirebaseFirestore.getInstance(studentApp)
        val mentorDb = FirebaseFirestore.getInstance(mentorApp)
        try {
            studentAuth.signInWithEmailAndPassword(requireNotNull(args.getString("qaEmail")), requireNotNull(args.getString("qaPassword"))).await()
            mentorAuth.signInWithEmailAndPassword(requireNotNull(args.getString("mentorEmail")), requireNotNull(args.getString("mentorPassword"))).await()
            assertEquals(true, mentorAuth.currentUser!!.getIdToken(true).await().claims["mentor"])
            val learnerId = studentAuth.currentUser!!.uid
            val student = TrainingRepositoryImpl(studentAuth, studentDb)
            val mentor = MentorRepositoryImpl(mentorAuth, mentorDb)
            fun link(stage: Int) = "https://github.com/eduvault-verification/pull/$stage"
            suspend fun assigned() = mentor.getAssignedLearners().getOrThrow().single { it.uid == learnerId }
            var dashboard = student.getMyTrainingProgram().getOrThrow()
            assertEquals(0, dashboard.assignment.completionPercent)
            val modules = dashboard.orderedModules
            assertTrue(student.submitEvidence(dashboard.program.id, modules[1].id, link(2), "Skip").isFailure)
            val definition = TrainingTaskDefinition("Checklist thực hành bảo mật", "Hoàn thành checklist và nộp bằng chứng theo quy chuẩn.", "15/11/2026", "https://developer.android.com")
            mentor.saveTask(assigned(), modules[0].id, definition).getOrThrow()
            dashboard = student.getMyTrainingProgram().getOrThrow()
            assertEquals(definition.title, dashboard.activeModule!!.evidenceTaskTitle)
            student.submitEvidence(dashboard.program.id, modules[0].id, link(1), "Checklist").getOrThrow()
            dashboard = student.getMyTrainingProgram().getOrThrow()
            assertEquals("PENDING", dashboard.currentEvidence!!.status)
            assertTrue(dashboard.currentEvidence!!.submittedAtMillis > 0)
            assertEquals(0, dashboard.assignment.completionPercent)
            assertTrue(mentor.saveTask(assigned(), modules[0].id, definition.copy(title = "Cannot change pending task")).isFailure)
            mentor.reviewEvidence(assigned(), false, "Bổ sung ảnh cấu hình bảo mật").getOrThrow()
            dashboard = student.getMyTrainingProgram().getOrThrow()
            assertEquals("REJECTED", dashboard.currentEvidence!!.status)
            assertEquals("Bổ sung ảnh cấu hình bảo mật", dashboard.currentEvidence!!.feedback)
            assertEquals("Cần bổ sung", dashboard.stageStatusLabel(modules[0].id))
            assertEquals(TrainingTaskStatus.TODO, dashboard.tasks[0].status)
            assertEquals(TrainingTaskStatus.LOCKED, dashboard.tasks[1].status)
            student.submitEvidence(dashboard.program.id, modules[0].id, link(1), "Added security screenshot").getOrThrow()
            for ((index, module) in modules.withIndex()) {
                if (index > 0) student.submitEvidence(dashboard.program.id, module.id, link(index + 1), "Stage evidence").getOrThrow()
                val pending = assigned()
                assertEquals("PENDING", pending.dashboard.currentEvidence!!.status)
                mentor.reviewEvidence(pending, true, "Đã kiểm tra bằng chứng thực hành").getOrThrow()
                dashboard = student.getMyTrainingProgram().getOrThrow()
                assertEquals((index + 1) * 100 / modules.size, dashboard.assignment.completionPercent)
                assertEquals(index + 1, dashboard.reviewedEvidence.size)
                assertEquals("APPROVED", dashboard.reviewedEvidence[module.id]!!.status)
            }
            assertEquals(TrainingStatus.COMPLETED, dashboard.assignment.status)
            assertNull(dashboard.activeModule)
            assertTrue(mentor.saveTask(assigned(), modules[0].id, definition).isFailure)
            assertTrue(student.setModuleCompletion(dashboard.program.id, modules[0].id, true).isFailure)
        } finally {
            studentAuth.signOut(); mentorAuth.signOut()
            studentDb.terminate().await(); mentorDb.terminate().await()
            studentApp.delete(); mentorApp.delete()
        }
    } }
}
