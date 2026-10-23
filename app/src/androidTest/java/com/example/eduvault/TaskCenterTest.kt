package com.example.eduvault

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.example.eduvault.core.theme.EduVaultTheme
import com.example.eduvault.domain.model.*
import com.example.eduvault.feature.home.ui.TrainingUiState
import com.example.eduvault.feature.tasks.ui.TaskCenterScreen
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class TaskCenterTest {
    @get:Rule val compose = createComposeRule()
    private fun dashboard(evidence: TrainingEvidence? = null) = TrainingDashboard(
        TrainingProgram("p", "Onboarding", "", 1, listOf(
            TrainingModule("onboarding-security", "Bảo mật", "", "SEC101", 1, 35, true),
            TrainingModule("git-workflow", "Git", "", "GIT101", 2, 50, true),
            TrainingModule("mock-project", "Mock project", "", "DEV101", 3, 180, true))),
        TrainingAssignment("p", setOf("onboarding-security"), 0, 1, 33, TrainingStatus.IN_PROGRESS), evidence)

    @Test fun filtersOnlyShowTheirActualTasksAndSubmissionUsesSharedHandler() {
        var submissions = 0
        compose.setContent {
            EduVaultTheme { TaskCenterScreen(TrainingUiState(dashboard = dashboard()), emptyList(),
                PaddingValues(), { submissions++ }, {}, {}) }
        }
        compose.onNodeWithText("Nộp bằng chứng").performScrollTo().performClick()
        compose.runOnIdle { assertEquals(1, submissions) }
        compose.onNodeWithTag("task_center").performScrollToIndex(0)
        compose.onAllNodesWithText("Cần làm").onLast().performClick()
        compose.onNodeWithText("Setup Android Studio & JDK 17").assertIsDisplayed()
        compose.onNodeWithText("Nộp checklist quy chuẩn & bảo mật").assertDoesNotExist()
        compose.onNodeWithText("Nộp repository và báo cáo mock project").assertDoesNotExist()
        compose.onAllNodesWithText("Chờ duyệt").onLast().performClick()
        compose.onNodeWithText("Không có đầu việc trong mục này.").assertIsDisplayed()
        compose.onNodeWithText("Đã đạt").performClick()
        compose.onNodeWithText("Nộp checklist quy chuẩn & bảo mật").assertIsDisplayed()
        compose.onNodeWithText("Nộp bằng chứng").assertDoesNotExist()
        compose.onNodeWithText("Tất cả").performClick()
        compose.onNodeWithText("Nộp checklist quy chuẩn & bảo mật").assertIsDisplayed()
        compose.onNodeWithText("Setup Android Studio & JDK 17").assertIsDisplayed()
        compose.onNodeWithText("Nộp repository và báo cáo mock project").performScrollTo().assertIsDisplayed()
    }
    @Test fun pendingTaskHasReceiptActionAndUnrelatedQuizzesAreHidden() {
        var quizStarts = 0
        val security = QuizSet("q", "SEC101", "Quy chuẩn & bảo mật", Difficulty.EASY, 5, "", trainingModuleId = "onboarding-security")
        val unrelated = security.copy(id = "legacy", title = "Xác suất thống kê", trainingModuleId = null)
        compose.setContent {
            EduVaultTheme { TaskCenterScreen(TrainingUiState(dashboard = dashboard(
                TrainingEvidence("git-workflow", "https://example.com", "", 1))), listOf(security, unrelated),
                PaddingValues(), {}, { quizStarts++ }, {}) }
        }
        compose.onNodeWithText("Xem / cập nhật bằng chứng").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Làm bài tự kiểm tra").performScrollTo().assertHasClickAction().performClick()
        compose.runOnIdle { assertEquals(1, quizStarts) }
        compose.onNodeWithText("Xác suất thống kê").assertDoesNotExist()
        compose.onNodeWithText("Nộp bằng chứng").assertDoesNotExist()
    }
}
