package com.example.eduvault

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.example.eduvault.core.theme.EduVaultTheme
import com.example.eduvault.domain.model.*
import com.example.eduvault.feature.home.ui.TrainingUiState
import com.example.eduvault.feature.profile.ui.ProfileUiState
import com.example.eduvault.feature.profile.ui.components.ProfileHeaderSection
import com.example.eduvault.feature.profile.ui.components.ProfileViewDetails
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class ProfileTest {
    @get:Rule val compose = createComposeRule()
    private val user = User("u", "Test User", "test@example.com", avatarUrl = "emoji:👨‍🎓", totalXp = 999, uploadCount = 42)
    private val dashboard = TrainingDashboard(
        TrainingProgram("p", "Onboarding Fresher", "", 1, listOf(
            TrainingModule("s1", "Bảo mật", "", "SEC101", 1, 35, true),
            TrainingModule("s2", "Git", "", "GIT101", 2, 50, true),
            TrainingModule("s3", "Mock project", "", "DEV101", 3, 180, true))),
        TrainingAssignment("p", setOf("s1"), 0, 1, 33, TrainingStatus.IN_PROGRESS))

    @Test fun profileReflectsSharedProgressAndKeepsAccountActions() {
        val training = mutableStateOf(TrainingUiState(dashboard = dashboard))
        var edits = 0
        var logouts = 0
        compose.setContent {
            EduVaultTheme {
                Column(Modifier.verticalScroll(rememberScrollState())) {
                    ProfileHeaderSection(ProfileUiState(user = user), PaddingValues(), { edits++ })
                    ProfileViewDetails(ProfileUiState(user = user), { logouts++ }, onEditClick = { edits++ },
                        trainingState = training.value)
                }
            }
        }
        compose.onNodeWithText("TU").assertExists()
        compose.onNodeWithText("👨‍🎓").assertDoesNotExist()
        compose.onNodeWithText("999 XP", substring = true).assertDoesNotExist()
        compose.onNodeWithText("Điểm thi đua").assertDoesNotExist()
        compose.onNodeWithText("Tài liệu đã tải lên").assertDoesNotExist()
        compose.onNodeWithText("33%").assertExists()
        compose.onNodeWithText("1/3 chặng đã đạt").assertExists()
        compose.onNodeWithText("Đang học · Chặng 2: Git").assertExists()
        compose.onNodeWithText("Chỉnh sửa hồ sơ").performClick()
        compose.onNodeWithText("Thông tin và bảo mật").performScrollTo().performClick()
        compose.onNodeWithText("Hướng dẫn onboarding").performScrollTo().performClick()
        compose.onNodeWithText("mentor nghiệm thu", substring = true).assertExists()
        compose.onNodeWithText("Đã hiểu").performClick()
        compose.onNodeWithText("Đăng xuất tài khoản").performScrollTo().performClick()
        compose.runOnIdle {
            assertEquals(2, edits)
            assertEquals(1, logouts)
            training.value = TrainingUiState(dashboard = dashboard.copy(assignment = dashboard.assignment.copy(
                completedModuleIds = setOf("s1", "s2", "s3"), completionPercent = 100, status = TrainingStatus.COMPLETED)))
        }
        compose.onNodeWithText("100%").assertExists()
        compose.onNodeWithText("3/3 chặng đã đạt").assertExists()
        compose.onNodeWithText("Đã hoàn thành chương trình").assertExists()
        compose.onNodeWithText("Đang học · Chặng 2: Git").assertDoesNotExist()
    }

    @Test fun unavailableProgramCanRetryWithoutInventingProgress() {
        var retries = 0
        compose.setContent {
            EduVaultTheme {
                Column(Modifier.verticalScroll(rememberScrollState())) {
                    ProfileViewDetails(ProfileUiState(user = user), {},
                        trainingState = TrainingUiState(error = "Không thể tải chương trình."),
                        onRetryTraining = { retries++ })
                }
            }
        }
        compose.onNodeWithText("33%").assertDoesNotExist()
        compose.onNodeWithText("Tải lại lộ trình").performScrollTo().performClick()
        compose.runOnIdle { assertEquals(1, retries) }
    }
}
