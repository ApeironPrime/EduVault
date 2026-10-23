package com.example.eduvault.feature.auth.ui

import com.example.eduvault.MainDispatcherRule
import com.example.eduvault.data.repository.FakeAuthRepository
import com.example.eduvault.domain.model.User
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RegisterViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var fakeAuthRepository: FakeAuthRepository
    private lateinit var viewModel: RegisterViewModel

    @Before
    fun setUp() {
        fakeAuthRepository = FakeAuthRepository()
        viewModel = RegisterViewModel(fakeAuthRepository)
    }

    @Test
    fun `field change events update state and clear respective errors`() {
        viewModel.onFullNameChange("Nguyen Van A")
        assertEquals("Nguyen Van A", viewModel.uiState.value.fullName)
        assertNull(viewModel.uiState.value.fullNameError)

        viewModel.onEmailChange("a@test.com")
        assertEquals("a@test.com", viewModel.uiState.value.email)
        assertNull(viewModel.uiState.value.emailError)

        viewModel.onPasswordChange("password123")
        assertEquals("password123", viewModel.uiState.value.password)
        assertNull(viewModel.uiState.value.passwordError)

        viewModel.onConfirmPasswordChange("password123")
        assertEquals("password123", viewModel.uiState.value.confirmPassword)
        assertNull(viewModel.uiState.value.confirmPasswordError)
    }

    @Test
    fun `toggle visibility changes password visibility flags`() {
        assertFalse(viewModel.uiState.value.isPasswordVisible)
        viewModel.onTogglePasswordVisibility()
        assertTrue(viewModel.uiState.value.isPasswordVisible)

        assertFalse(viewModel.uiState.value.isConfirmPasswordVisible)
        viewModel.onToggleConfirmPasswordVisibility()
        assertTrue(viewModel.uiState.value.isConfirmPasswordVisible)
    }

    @Test
    fun `onRegisterClick with blank fullName fails validation`() = runTest {
        viewModel.onFullNameChange("   ")
        viewModel.onEmailChange("a@test.com")
        viewModel.onPasswordChange("password123")
        viewModel.onConfirmPasswordChange("password123")
        viewModel.onRegisterClick()

        val state = viewModel.uiState.value
        assertTrue(state.hasAttemptedRegister)
        assertEquals("Vui lòng nhập họ và tên", state.fullNameError)
        assertFalse(state.isRegisterSuccess)
    }

    @Test
    fun `onRegisterClick with short fullName fails validation`() = runTest {
        viewModel.onFullNameChange("A")
        viewModel.onEmailChange("a@test.com")
        viewModel.onPasswordChange("password123")
        viewModel.onConfirmPasswordChange("password123")
        viewModel.onRegisterClick()

        val state = viewModel.uiState.value
        assertEquals("Họ và tên phải có ít nhất 2 ký tự", state.fullNameError)
        assertFalse(state.isRegisterSuccess)
    }

    @Test
    fun `onRegisterClick with invalid email fails validation`() = runTest {
        viewModel.onFullNameChange("Nguyen Van A")
        viewModel.onEmailChange("invalid-email")
        viewModel.onPasswordChange("password123")
        viewModel.onConfirmPasswordChange("password123")
        viewModel.onRegisterClick()

        val state = viewModel.uiState.value
        assertEquals("Định dạng email không hợp lệ", state.emailError)
        assertFalse(state.isRegisterSuccess)
    }

    @Test
    fun `onRegisterClick with short password fails validation`() = runTest {
        viewModel.onFullNameChange("Nguyen Van A")
        viewModel.onEmailChange("a@test.com")
        viewModel.onPasswordChange("12345")
        viewModel.onConfirmPasswordChange("12345")
        viewModel.onRegisterClick()

        val state = viewModel.uiState.value
        assertEquals("Mật khẩu phải có ít nhất 8 ký tự", state.passwordError)
        assertFalse(state.isRegisterSuccess)
    }

    @Test
    fun `onRegisterClick with mismatched confirmPassword fails validation`() = runTest {
        viewModel.onFullNameChange("Nguyen Van A")
        viewModel.onEmailChange("a@test.com")
        viewModel.onPasswordChange("password123")
        viewModel.onConfirmPasswordChange("different123")
        viewModel.onRegisterClick()

        val state = viewModel.uiState.value
        assertEquals("Mật khẩu xác nhận không khớp", state.confirmPasswordError)
        assertFalse(state.isRegisterSuccess)
    }

    @Test
    fun `onRegisterClick with valid inputs registers successfully`() = runTest {
        fakeAuthRepository.registerResult = Result.success(
            User(uid = "reg_1", fullName = "Nguyen Van A", email = "a@test.com")
        )

        viewModel.onFullNameChange("Nguyen Van A")
        viewModel.onEmailChange("a@test.com")
        viewModel.onPasswordChange("password123")
        viewModel.onConfirmPasswordChange("password123")
        viewModel.onRegisterClick()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertTrue(state.isRegisterSuccess)
        assertNull(state.registerError)
    }

    @Test
    fun `onRegisterClick with repository error displays registerError`() = runTest {
        fakeAuthRepository.registerResult = Result.failure(Exception("Email này đã được đăng ký"))

        viewModel.onFullNameChange("Nguyen Van A")
        viewModel.onEmailChange("existing@test.com")
        viewModel.onPasswordChange("password123")
        viewModel.onConfirmPasswordChange("password123")
        viewModel.onRegisterClick()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertFalse(state.isRegisterSuccess)
        assertEquals("Email này đã được đăng ký", state.registerError)
    }

    @Test
    fun `loginWithGoogle registers or logs in with Google token`() = runTest {
        fakeAuthRepository.loginWithGoogleResult = Result.success(
            User(uid = "g_reg_1", fullName = "Google Reg", email = "greg@test.com")
        )

        viewModel.loginWithGoogle("valid_google_token")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertTrue(state.isRegisterSuccess)
        assertNull(state.registerError)
    }

    @Test
    fun `onDismissError clears registerError`() = runTest {
        fakeAuthRepository.registerResult = Result.failure(Exception("Some network error"))
        viewModel.onFullNameChange("Nguyen Van A")
        viewModel.onEmailChange("a@test.com")
        viewModel.onPasswordChange("password123")
        viewModel.onConfirmPasswordChange("password123")
        viewModel.onRegisterClick()
        advanceUntilIdle()

        assertEquals("Some network error", viewModel.uiState.value.registerError)

        viewModel.onDismissError()
        assertNull(viewModel.uiState.value.registerError)
    }
}
