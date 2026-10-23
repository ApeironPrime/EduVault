package com.example.eduvault

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import com.example.eduvault.core.theme.EduVaultTheme
import com.example.eduvault.domain.model.*
import com.example.eduvault.domain.repository.*
import com.example.eduvault.feature.mentor.ui.*
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class MentorTest {
    @get:Rule val compose = createComposeRule()
    private fun authStub(): AuthRepository = java.lang.reflect.Proxy.newProxyInstance(
        AuthRepository::class.java.classLoader, arrayOf(AuthRepository::class.java)
    ) { _, method, _ -> if (method.name == "logout") Unit else error("Unexpected auth call") } as AuthRepository
    private fun learner() = MentorLearner("l", "Học viên được phân công", "learner@example.test",
        TrainingDashboard(TrainingProgram("p", "Onboarding", "", 1,
            listOf(TrainingModule("m", "Quy chuẩn & bảo mật", "", "SEC", 1, 15, true))),
            TrainingAssignment("p", emptySet(), 1, 2, 0, TrainingStatus.IN_PROGRESS),
            TrainingEvidence("m", "https://github.com/example/pull/1", "Checklist", 1000)), "1:0")
    private class Repo(val learner: MentorLearner) : MentorRepository {
        var reviews = 0
        var taskSaves = 0
        var savedTask: TrainingTaskDefinition? = null
        var items = listOf(learner)
        override suspend fun saveTask(learner: MentorLearner, moduleId: String, task: TrainingTaskDefinition?): Result<Unit> {
            taskSaves++
            savedTask = task
            items = listOf(learner.copy(dashboard = learner.dashboard.copy(assignment = learner.dashboard.assignment.copy(
                taskDefinitions = if (task == null) emptyMap() else mapOf(moduleId to task)))))
            return Result.success(Unit)
        }
        override suspend fun getAssignedLearners() = Result.success(items)
        override suspend fun reviewEvidence(learner: MentorLearner, approved: Boolean, feedback: String): Result<Unit> {
            reviews++
            items = listOf(learner.copy(dashboard = learner.dashboard.copy(evidence = learner.dashboard.evidence?.copy(
                status = if (approved) "APPROVED" else "REJECTED", feedback = feedback))))
            return Result.success(Unit)
        }
    }
    @Test fun narrowMentorCanReviewAndRequiresFeedbackForRejection() {
        val repo = Repo(learner())
        val vm = MentorViewModel(repo, authStub())
        compose.setContent {
            val density = LocalDensity.current.density
            CompositionLocalProvider(LocalDensity provides Density(density, 1.3f)) {
                EduVaultTheme { Box(Modifier.size(320.dp, 600.dp)) {
                    MentorScreen(User("mentor", "Mentor", "mentor@example.test", role = "mentor"), {}, vm)
                } }
            }
        }
        compose.waitUntil(10000) { vm.uiState.value.learners.isNotEmpty() }
        compose.onNodeWithTag("mentor_learners").performScrollToNode(hasText("Yêu cầu bổ sung"))
        compose.onNodeWithText("Yêu cầu bổ sung").assertIsDisplayed().performClick()
        compose.onNodeWithText("Xác nhận").performClick()
        compose.onNodeWithText("Hãy ghi rõ nội dung học viên cần bổ sung.").assertIsDisplayed()
        compose.runOnIdle { assertEquals(0, repo.reviews) }
        compose.onNodeWithText("Nội dung cần bổ sung").performTextInput("Bổ sung ảnh cấu hình")
        compose.onNodeWithText("Xác nhận").performClick()
        compose.waitUntil(10000) { vm.uiState.value.reviewVersion == 1 }
        compose.runOnIdle { assertEquals(1, repo.reviews) }
        compose.onNodeWithText("Xác nhận").assertDoesNotExist()
    }
    @Test fun mentorCanCreateStageTaskWithValidationAndSeeSavedAssignment() {
        val repo = Repo(learner().copy(dashboard = learner().dashboard.copy(evidence = null)))
        val vm = MentorViewModel(repo, authStub())
        compose.setContent { EduVaultTheme { Box(Modifier.size(320.dp, 600.dp)) {
            MentorScreen(User("mentor", "Mentor", "mentor@example.test", role = "mentor"), {}, vm)
        } } }
        compose.waitUntil(10000) { vm.uiState.value.learners.isNotEmpty() }
        compose.onNodeWithText("Nhiệm vụ").performClick()
        compose.onNodeWithTag("mentor_learners").performScrollToNode(hasText("Soạn nhiệm vụ chặng 1"))
        compose.onNodeWithText("Soạn nhiệm vụ chặng 1").performClick()
        compose.onNodeWithText("Lưu & giao nhiệm vụ").performClick()
        compose.onNodeWithText("Yêu cầu thực hành cần từ 10 đến 4.000 ký tự.").assertExists()
        compose.runOnIdle { assertEquals(0,repo.taskSaves) }
        compose.onNodeWithText("Tên nhiệm vụ").performTextReplacement("Checklist bảo mật mới")
        compose.onNodeWithText("Yêu cầu & tiêu chí nghiệm thu").performTextReplacement("Hoàn thành checklist và nộp bằng chứng")
        compose.onNodeWithText("Lưu & giao nhiệm vụ").performClick()
        compose.waitUntil(10000) { vm.uiState.value.reviewVersion == 1 }
        compose.runOnIdle { assertEquals(1,repo.taskSaves); assertEquals("Checklist bảo mật mới",repo.savedTask!!.title) }
        compose.onNodeWithText("Lưu & giao nhiệm vụ").assertDoesNotExist()
        compose.onNodeWithTag("mentor_learners").performScrollToNode(hasText("Checklist bảo mật mới"))
        compose.onNodeWithText("Checklist bảo mật mới").assertIsDisplayed()
    }

}
