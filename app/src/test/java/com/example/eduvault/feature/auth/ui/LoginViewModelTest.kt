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
class LoginViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var fakeAuthRepository: FakeAuthRepository
    private lateinit var viewModel: LoginViewModel

    @Before
    fun setUp() {
        fakeAuthRepository = FakeAuthRepository()
        viewModel = LoginViewModel(fakeAuthRepository)
    }

    @Test
    fun `onEmailChange updates state and clears errors`() {
        viewModel.onEmailChange("user@test.com")
        val state = viewModel.uiState.value
        assertEquals("user@test.com", state.email)
        assertNull(state.emailError)
        assertNull(state.loginError)
    }

    @Test
    fun `onPasswordChange updates state and clears errors`() {
        viewModel.onPasswordChange("password123")
        val state = viewModel.uiState.value
        assertEquals("password123", state.password)
        assertNull(state.passwordError)
        assertNull(state.loginError)
    }

    @Test
    fun `onTogglePasswordVisibility toggles isPasswordVisible`() {
        assertFalse(viewModel.uiState.value.isPasswordVisible)
        viewModel.onTogglePasswordVisibility()
        assertTrue(viewModel.uiState.value.isPasswordVisible)
        viewModel.onTogglePasswordVisibility()
        assertFalse(viewModel.uiState.value.isPasswordVisible)
    }

    @Test
    fun `onLoginClick with blank email shows email error`() = runTest {
        viewModel.onEmailChange("   ")
        viewModel.onPasswordChange("password123")
        viewModel.onLoginClick()

        val state = viewModel.uiState.value
        assertTrue(state.hasAttemptedLogin)
        assertEquals("Vui lòng nhập email", state.emailError)
        assertFalse(state.isLoginSuccess)
    }

    @Test
    fun `onLoginClick with invalid email format shows format error`() = runTest {
        viewModel.onEmailChange("invalid-email")
        viewModel.onPasswordChange("password123")
        viewModel.onLoginClick()

        val state = viewModel.uiState.value
        assertTrue(state.hasAttemptedLogin)
        assertEquals("Định dạng email không hợp lệ", state.emailError)
        assertFalse(state.isLoginSuccess)
    }

    @Test
    fun `onLoginClick with blank password shows password error`() = runTest {
        viewModel.onEmailChange("user@test.com")
        viewModel.onPasswordChange("")
        viewModel.onLoginClick()

        val state = viewModel.uiState.value
        assertEquals("Vui lòng nhập mật khẩu", state.passwordError)
        assertFalse(state.isLoginSuccess)
    }

    @Test
    fun `onLoginClick with short password shows min length error`() = runTest {
        viewModel.onEmailChange("user@test.com")
        viewModel.onPasswordChange("short")
        viewModel.onLoginClick()

        val state = viewModel.uiState.value
        assertEquals("Mật khẩu phải có ít nhất 8 ký tự", state.passwordError)
        assertFalse(state.isLoginSuccess)
    }

    @Test
    fun `onLoginClick with valid credentials logs in successfully`() = runTest {
        fakeAuthRepository.loginResult = Result.success(
            User(uid = "u1", fullName = "Valid User", email = "user@test.com")
        )

        viewModel.onEmailChange("user@test.com")
        viewModel.onPasswordChange("password123")
        viewModel.onLoginClick()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertTrue(state.isLoginSuccess)
        assertNull(state.loginError)
    }

    @Test
    fun `onLoginClick with repository error displays loginError`() = runTest {
        fakeAuthRepository.loginResult = Result.failure(Exception("Sai mật khẩu hoặc tài khoản không tồn tại"))

        viewModel.onEmailChange("user@test.com")
        viewModel.onPasswordChange("wrongpass123")
        viewModel.onLoginClick()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertFalse(state.isLoginSuccess)
        assertEquals("Sai mật khẩu hoặc tài khoản không tồn tại", state.loginError)
    }

    @Test
    fun `loginWithGoogle with valid token succeeds`() = runTest {
        fakeAuthRepository.loginWithGoogleResult = Result.success(
            User(uid = "google_u1", fullName = "Google User", email = "google@test.com")
        )

        viewModel.loginWithGoogle("valid_id_token")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertTrue(state.isLoginSuccess)
        assertNull(state.loginError)
    }

    @Test
    fun `loginWithGoogle with failure sets loginError`() = runTest {
        fakeAuthRepository.loginWithGoogleResult = Result.failure(Exception("Google Sign-In failed"))

        viewModel.loginWithGoogle("invalid_token")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertFalse(state.isLoginSuccess)
        assertEquals("Google Sign-In failed", state.loginError)
    }

    @Test
    fun `onDismissError clears loginError`() = runTest {
        fakeAuthRepository.loginResult = Result.failure(Exception("Some error"))
        viewModel.onEmailChange("user@test.com")
        viewModel.onPasswordChange("password123")
        viewModel.onLoginClick()
        advanceUntilIdle()

        assertEquals("Some error", viewModel.uiState.value.loginError)

        viewModel.onDismissError()
        assertNull(viewModel.uiState.value.loginError)
    }
}
