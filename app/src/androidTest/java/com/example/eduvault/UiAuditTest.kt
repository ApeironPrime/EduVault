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
import com.example.eduvault.feature.home.ui.dialogs.NotificationsPanel
import com.example.eduvault.feature.profile.ui.*
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class UiAuditTest {
    @get:Rule val compose = createComposeRule()
    private class Categories : CategoryRepository {
        override suspend fun getCategories() = Result.success(emptyList<DocumentCategory>())
        override suspend fun addCategory(name: String, emoji: String) = Result.success(Unit)
        override suspend fun updateCategory(id: String, name: String, emoji: String) = Result.success(Unit)
        override suspend fun deleteCategory(id: String) = Result.success(Unit)
    }
    private class Admin(val denied: Boolean = false) : AdminRepository {
        override suspend fun getPendingDocuments() = Result.success(emptyList<LibraryDocument>())
        override suspend fun getReportedDocuments() = Result.success(emptyList<LibraryDocument>())
        override suspend fun getAllUsers(): Result<List<User>> = if (denied)
            Result.failure(Exception("PERMISSION_DENIED: Missing or insufficient permissions.")) else
            Result.success(listOf(User("u", "Học viên kiểm tra bố cục", "test@example.com")))
        override suspend fun deleteDocument(docId: String) = Result.success(Unit)
        override suspend fun approveDocument(docId: String) = Result.success(Unit)
        override suspend fun dismissReport(docId: String) = Result.success(Unit)
        override suspend fun updateUserBlockStatus(uid: String, isBlocked: Boolean) = Result.success(Unit)
    }
    @Test fun longNotificationKeepsTimestampReadableAndCloseReachable() {
        var closes = 0
        compose.setContent {
            val density = LocalDensity.current.density
            CompositionLocalProvider(LocalDensity provides Density(density, 1.3f)) {
                EduVaultTheme {
                    Box(Modifier.size(280.dp, 360.dp)) {
                        NotificationsPanel(listOf(EduNotification("n", "Bắt đầu chương trình Onboarding và quy chuẩn bảo mật",
                            "Mentor đã gửi hướng dẫn thực hành.", System.currentTimeMillis() - 120000)), { closes++ })
                    }
                }
            }
        }
        compose.onNodeWithText("2 phút trước").performScrollTo().assertIsDisplayed()
        val timestamp = compose.onNodeWithText("2 phút trước").fetchSemanticsNode().boundsInRoot
        assertTrue("Timestamp must fit on a line, not one character per line", timestamp.width > timestamp.height * 2)
        compose.onNodeWithText("Mentor đã gửi hướng dẫn thực hành.").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Đóng thông báo").assertIsDisplayed().performClick()
        compose.runOnIdle { assertEquals(1, closes) }
    }
    @Test fun deniedAdminShowsAccessStateInsteadOfEmptyMetrics() {
        val vm = AdminViewModel(Categories(), Admin(denied = true))
        var backs = 0
        compose.setContent { EduVaultTheme { AdminDashboardScreen({ backs++ }, vm) } }
        compose.waitUntil(10000) { vm.uiState.value.accessDenied }
        compose.onNodeWithText("Cần quyền quản trị").assertIsDisplayed()
        compose.onNodeWithText("PERMISSION_DENIED", substring = true).assertDoesNotExist()
        compose.onNodeWithText("Tổng quan đào tạo").assertDoesNotExist()
        compose.onNodeWithText("Quay lại hồ sơ").performClick()
        compose.runOnIdle { assertEquals(1, backs) }
    }
    @Test fun compactAdminCanScrollPastOverviewToUsers() {
        val vm = AdminViewModel(Categories(), Admin())
        compose.setContent {
            val density = LocalDensity.current.density
            CompositionLocalProvider(LocalDensity provides Density(density, 1.3f)) {
                EduVaultTheme { Box(Modifier.size(320.dp, 600.dp)) { AdminDashboardScreen({}, vm) } }
            }
        }
        compose.waitUntil(10000) { vm.uiState.value.users.isNotEmpty() }
        compose.onNodeWithText("Thành viên 👥").performScrollTo().performClick()
        compose.onNodeWithTag("admin_content").performScrollToNode(hasText("Học viên kiểm tra bố cục"))
        compose.onNodeWithText("Học viên kiểm tra bố cục").assertIsDisplayed()
        compose.onNodeWithText("test@example.com").performScrollTo().assertIsDisplayed()
    }
}
