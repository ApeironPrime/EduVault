package com.example.eduvault.feature.profile.ui

import com.example.eduvault.MainDispatcherRule
import com.example.eduvault.data.repository.FakeAuthRepository
import com.example.eduvault.domain.model.AuthState
import com.example.eduvault.domain.model.User
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `loadUserProfile authenticated user populates state and edit fields`() = runTest {
        val fakeAuth = FakeAuthRepository()
        val testUser = User(
            uid = "usr-1",
            fullName = "Nguyễn Văn A",
            email = "vana@edu.vn",
            university = "ĐH Kinh tế",
            avatarUrl = "emoji:👨‍🎓"
        )
        fakeAuth.getCurrentUserResult = Result.success(testUser)

        val viewModel = ProfileViewModel(fakeAuth)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertTrue(state.authState is AuthState.Authenticated)
        assertEquals("Nguyễn Văn A", state.user?.fullName)
        assertEquals("ĐH Kinh tế", state.editUniversity)
        assertEquals("emoji:👨‍🎓", state.editAvatarUrl)
    }

    @Test
    fun `loadUserProfile guest user sets Guest authState`() = runTest {
        val fakeAuth = FakeAuthRepository()
        fakeAuth.getCurrentUserResult = Result.success(null)

        val viewModel = ProfileViewModel(fakeAuth)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertTrue(state.authState is AuthState.Guest)
        assertNull(state.user)
    }

    @Test
    fun `start and cancel editing transitions state properly`() = runTest {
        val fakeAuth = FakeAuthRepository()
        val testUser = User(uid = "usr-1", fullName = "Lê Thị B", email = "b@edu.vn")
        fakeAuth.getCurrentUserResult = Result.success(testUser)

        val viewModel = ProfileViewModel(fakeAuth)
        advanceUntilIdle()

        viewModel.onStartEditing()
        assertTrue(viewModel.uiState.value.isEditing)

        viewModel.onFullNameChange("Tên Mới")
        assertEquals("Tên Mới", viewModel.uiState.value.editFullName)

        viewModel.onCancelEditing()
        assertFalse(viewModel.uiState.value.isEditing)
    }

    @Test
    fun `saveProfile with empty full name shows error`() = runTest {
        val fakeAuth = FakeAuthRepository()
        val testUser = User(uid = "usr-1", fullName = "Trần C", email = "c@edu.vn")
        fakeAuth.getCurrentUserResult = Result.success(testUser)

        val viewModel = ProfileViewModel(fakeAuth)
        advanceUntilIdle()

        viewModel.onStartEditing()
        viewModel.onFullNameChange("   ")
        viewModel.saveProfile()

        assertEquals("Họ tên không được để trống.", viewModel.uiState.value.errorMessage)
        assertFalse(viewModel.uiState.value.isSaving)
    }

    @Test
    fun `saveProfile with valid input updates profile and exits edit mode`() = runTest {
        val fakeAuth = FakeAuthRepository()
        val initialUser = User(uid = "usr-1", fullName = "Trần C", email = "c@edu.vn", university = "UEH")
        val updatedUser = initialUser.copy(fullName = "Trần Văn C Cập Nhật", university = "Bách Khoa")
        fakeAuth.getCurrentUserResult = Result.success(initialUser)
        fakeAuth.updateProfileResult = Result.success(updatedUser)

        val viewModel = ProfileViewModel(fakeAuth)
        advanceUntilIdle()

        viewModel.onStartEditing()
        viewModel.onFullNameChange("Trần Văn C Cập Nhật")
        viewModel.onUniversityChange("Bách Khoa")
        viewModel.saveProfile()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isSaving)
        assertFalse(state.isEditing)
        assertEquals("Trần Văn C Cập Nhật", state.user?.fullName)
        assertEquals("Bách Khoa", state.user?.university)
        assertEquals("Cập nhật hồ sơ thành công!", state.successMessage)
    }

    @Test
    fun `onChangePassword with short password sets error`() = runTest {
        val fakeAuth = FakeAuthRepository()
        val viewModel = ProfileViewModel(fakeAuth)
        advanceUntilIdle()

        var finished = false
        viewModel.onChangePassword("oldPass123", "123") { finished = true }

        assertFalse(finished)
        assertEquals("Mật khẩu mới phải có ít nhất 6 ký tự.", viewModel.uiState.value.changePasswordError)
    }

    @Test
    fun `onChangePassword with valid password calls repository successfully`() = runTest {
        val fakeAuth = FakeAuthRepository()
        fakeAuth.changePasswordResult = Result.success(Unit)

        val viewModel = ProfileViewModel(fakeAuth)
        advanceUntilIdle()

        var finished = false
        viewModel.onChangePassword("oldPass123", "newPass456") { finished = true }
        advanceUntilIdle()

        assertTrue(finished)
        assertEquals("Đổi mật khẩu thành công!", viewModel.uiState.value.changePasswordSuccess)
        assertNull(viewModel.uiState.value.changePasswordError)
    }

    @Test
    fun `logout triggers callback`() = runTest {
        val fakeAuth = FakeAuthRepository()
        val viewModel = ProfileViewModel(fakeAuth)
        advanceUntilIdle()

        var loggedOut = false
        viewModel.logout { loggedOut = true }

        assertTrue(loggedOut)
    }

    @Test fun `change password preserves intentional whitespace`() = runTest {
        var received: Pair<String, String>? = null
        val auth = object : FakeAuthRepository() {
            override suspend fun changePassword(currentPassword: String, newPassword: String): Result<Unit> {
                received = currentPassword to newPassword
                return Result.success(Unit)
            }
        }
        val vm = ProfileViewModel(auth)
        advanceUntilIdle()
        vm.onChangePassword(" oldPassword ", " newPassword ") {}
        advanceUntilIdle()
        assertEquals(" oldPassword " to " newPassword ", received)
    }
}
