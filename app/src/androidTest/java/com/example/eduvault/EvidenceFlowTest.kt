package com.example.eduvault

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Scaffold
import androidx.compose.ui.platform.testTag
import com.example.eduvault.feature.home.ui.HomeContent
import com.example.eduvault.feature.home.ui.HomeUiState
import com.example.eduvault.feature.home.ui.HomeTab
import com.example.eduvault.feature.home.ui.TrainingUiState
import com.example.eduvault.feature.home.ui.preview.PreviewFresherDashboard
import com.example.eduvault.feature.home.ui.components.HomeBottomNav
import org.junit.Assert.assertTrue
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performScrollTo
import com.example.eduvault.core.theme.EduVaultTheme
import com.example.eduvault.domain.model.trainingEvidenceError
import com.example.eduvault.domain.model.*
import com.example.eduvault.feature.home.ui.components.FocusActionCard
import com.example.eduvault.feature.home.ui.components.SubmitEvidenceDialog
import com.example.eduvault.feature.home.ui.components.TrainingMilestonesCard
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class EvidenceFlowTest {
    @get:Rule val compose = createComposeRule()

    @Test fun timelineLocksFutureStagesAndMovesActiveStageAfterReview() {
        val modules = listOf(
            TrainingModule("security", "Bảo mật", "", "SEC101", 1, 35, true),
            TrainingModule("git", "Git", "", "GIT101", 2, 50, true),
            TrainingModule("mock", "Mock project", "", "DEV101", 3, 180, true),
        )
        var dashboard by mutableStateOf(TrainingDashboard(
            TrainingProgram("foundation", "Foundation", "", 1, modules),
            TrainingAssignment("foundation", emptySet(), 1, 2, 0, TrainingStatus.IN_PROGRESS),
        ))
        var submits = 0
        compose.setContent {
            EduVaultTheme {
                Column(Modifier.verticalScroll(rememberScrollState())) {
                    TrainingMilestonesCard(dashboard, onSubmitEvidence = { submits++ })
                }
            }
        }
        compose.onAllNodesWithText("Đang khóa", substring = true).assertCountEquals(2)
        compose.onNodeWithText("Chặng 2: Git").performClick()
        compose.runOnIdle { assertEquals(0, submits) }
        compose.onNodeWithText("Chặng 1: Bảo mật").performClick()
        compose.runOnIdle {
            assertEquals(1, submits)
            dashboard = dashboard.copy(assignment = dashboard.assignment.copy(
                completedModuleIds = setOf("security"), completionPercent = 33))
        }
        compose.onAllNodesWithText("Đang khóa", substring = true).assertCountEquals(1)
        compose.onNodeWithText("Đã đạt", substring = true).assertIsDisplayed()
        compose.onNodeWithText("Chặng 2: Git").performClick()
        compose.runOnIdle { assertEquals(2, submits) }
    }

    @Test fun rejectedEvidenceShowsSupplementStateInsteadOfPendingAndKeepsFutureStageLocked() {
        val evidence = TrainingEvidence(requireNotNull(PreviewFresherDashboard.activeModule).id, "https://github.com/team/repo", "", 1000,
            status = "REJECTED", feedback = "Bổ sung ảnh java -version")
        val dashboard = PreviewFresherDashboard.copy(evidence = evidence)
        compose.setContent { EduVaultTheme {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                TrainingMilestonesCard(dashboard, onSubmitEvidence = {})
            }
        } }
        compose.onNodeWithText("Cần bổ sung", substring = true).performScrollTo().assertIsDisplayed()
        compose.onAllNodesWithText("Chờ duyệt", substring = true).assertCountEquals(0)
        compose.onAllNodesWithText("Đang khóa", substring = true).assertCountEquals(1)
    }

    @Test fun roadmapViewportAndSubmitButtonStayAboveFloatingDock() {
        compose.setContent {
            EduVaultTheme {
                Scaffold(bottomBar = {
                    Box(Modifier.testTag("dock")) {
                        HomeBottomNav(HomeTab.HOME, onTabSelected = {})
                    }
                }) { padding ->
                    HomeContent(
                        uiState = HomeUiState(),
                        trainingUiState = TrainingUiState(dashboard = PreviewFresherDashboard),
                        recentDocs = emptyList(), activityItems = emptyList(),
                        paddingValues = padding, onStartQuizSetup = {}, onTabSelected = {},
                        onUploadClick = {}, onOpenDoc = {}, onOpenFlashcards = {},
                        onOpenStudyAi = {}, onOpenNotes = {}, onNotificationClick = {},
                        onRetryTraining = {}, onSubmitEvidenceClick = {},
                    )
                }
            }
        }
        val dockTop = compose.onNodeWithTag("dock").fetchSemanticsNode().boundsInRoot.top
        val viewportBottom = compose.onNodeWithTag("roadmap_content").fetchSemanticsNode().boundsInRoot.bottom
        assertTrue("Scroll viewport must end above the dock", viewportBottom <= dockTop + 1f)
        compose.onNodeWithText("Nộp bằng chứng").performScrollTo().assertIsDisplayed()
        val buttonBottom = compose.onNodeWithText("Nộp bằng chứng").fetchSemanticsNode().boundsInRoot.bottom
        assertTrue("Submission action must be reachable above the dock", buttonBottom < dockTop)
    }

    @Test fun evidenceButtonOpensDialogAndInvalidInputStaysVisible() {
        compose.setContent {
            var open by remember { mutableStateOf(false) }
            var error by remember { mutableStateOf<String?>(null) }
            EduVaultTheme {
                FocusActionCard(onSubmitClick = { open = true }, stageNumber = 2)
                if (open) SubmitEvidenceDialog(
                    stageNumber = 2,
                    onDismiss = { open = false },
                    onSubmit = { link, note -> error = trainingEvidenceError(link, note) },
                    errorMessage = error,
                )
            }
        }
        compose.onNodeWithText("Nộp bằng chứng").performClick()
        compose.onNodeWithText("Nộp bài thẩm định Chặng 2").assertIsDisplayed()
        compose.onNodeWithText("Gửi thẩm định").performScrollTo().performClick()
        compose.onNodeWithText("Vui lòng nhập link bằng chứng.").assertIsDisplayed()
        compose.onNodeWithText("Hủy").performClick()
        compose.onNodeWithText("Nộp bằng chứng").assertIsDisplayed()
    }

    @Test fun submittedValuesReachHandlerAndButtonsDisableDuringRequest() {
        var received: Pair<String, String>? = null
        compose.setContent {
            var sending by remember { mutableStateOf(false) }
            EduVaultTheme {
                SubmitEvidenceDialog(
                    onDismiss = {},
                    onSubmit = { link, note -> received = link to note; sending = true },
                    isSubmitting = sending,
                )
            }
        }
        compose.onNodeWithTag("evidence_link").performTextInput("https://github.com/team/repo/pull/1")
        compose.onNodeWithTag("evidence_note").performTextInput("Setup complete")
        compose.onNodeWithText("Gửi thẩm định").performScrollTo().performClick()
        compose.onNodeWithText("Đang gửi…").assertIsNotEnabled()
        compose.onNodeWithText("Hủy").assertIsNotEnabled()
        compose.runOnIdle {
            assertEquals("https://github.com/team/repo/pull/1" to "Setup complete", received)
        }
    }
}
