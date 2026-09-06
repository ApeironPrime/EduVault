package com.example.eduvault.feature.auth.ui

import androidx.lifecycle.SavedStateHandle
import com.example.eduvault.MainDispatcherRule
import com.example.eduvault.data.repository.FakeAuthRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * JVM Local Unit Tests for ResetPasswordViewModel.
 * Verifies SavedStateHandle extraction, form validation, visibility toggling,
 * and repo callbacks for both demo and production modes.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ResetPasswordViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var fakeAuthRepository: FakeAuthRepository

    @Before
    fun setUp() {
        fakeAuthRepository = FakeAuthRepository()
    }

    private fun createViewModel(oobCode: String?): ResetPasswordViewModel {
        val savedStateHandle = if (oobCode != null) {
            SavedStateHandle(mapOf("oobCode" to oobCode))
        } else {
            SavedStateHandle()
        }
        return ResetPasswordViewModel(fakeAuthRepository, savedStateHandle)
    }

    @Test
    fun testInitialization_withOobCode_setsRealMode() {
        val viewModel = createViewModel("code_123")
        val state = viewModel.uiState.value
        assertFalse(state.isDemoMode)
        assertEquals("code_123", state.oobCode)
    }

    @Test
    fun testInitialization_withoutOobCode_setsDemoMode() {
        val viewModel = createViewModel(null)
        val state = viewModel.uiState.value
        assertTrue(state.isDemoMode)
        assertNull(state.oobCode)
    }

    @Test
    fun testNewPasswordChange_updatesStateAndClearsErrors() {
        val viewModel = createViewModel(null)
        viewModel.onNewPasswordChange("newPass123")
        val state = viewModel.uiState.value
        assertEquals("newPass123", state.newPassword)
        assertNull(state.newPasswordError)
        assertNull(state.errorMessage)
    }

    @Test
    fun testConfirmPasswordChange_updatesStateAndClearsErrors() {
        val viewModel = createViewModel(null)
        viewModel.onConfirmPasswordChange("confirmPass")
        val state = viewModel.uiState.value
        assertEquals("confirmPass", state.confirmPassword)
        assertNull(state.confirmPasswordError)
        assertNull(state.errorMessage)
    }

    @Test
    fun testTogglePasswordVisibility() {
        val viewModel = createViewModel(null)
        assertFalse(viewModel.uiState.value.isPasswordVisible)
        
        viewModel.onTogglePasswordVisibility()
        assertTrue(viewModel.uiState.value.isPasswordVisible)
        
        viewModel.onTogglePasswordVisibility()
        assertFalse(viewModel.uiState.value.isPasswordVisible)
    }

    @Test
    fun testToggleConfirmPasswordVisibility() {
        val viewModel = createViewModel(null)
        assertFalse(viewModel.uiState.value.isConfirmPasswordVisible)
        
        viewModel.onToggleConfirmPasswordVisibility()
        assertTrue(viewModel.uiState.value.isConfirmPasswordVisible)
        
        viewModel.onToggleConfirmPasswordVisibility()
        assertFalse(viewModel.uiState.value.isConfirmPasswordVisible)
    }

    @Test
    fun testReset_withBlankPassword_failsValidation() {
        val viewModel = createViewModel(null)
        viewModel.onNewPasswordChange("")
        viewModel.onConfirmPasswordChange("")
        viewModel.onResetClick()
        
        val state = viewModel.uiState.value
        assertTrue(state.hasAttemptedReset)
        assertEquals("Vui lòng nhập mật khẩu mới", state.newPasswordError)
        assertFalse(state.isResetSuccess)
    }

    @Test
    fun testReset_withShortPassword_failsValidation() {
        val viewModel = createViewModel(null)
        viewModel.onNewPasswordChange("short")
        viewModel.onConfirmPasswordChange("short")
        viewModel.onResetClick()
        
        val state = viewModel.uiState.value
        assertEquals("Mật khẩu phải có ít nhất 8 ký tự", state.newPasswordError)
        assertFalse(state.isResetSuccess)
    }

    @Test
    fun testReset_withMismatchingConfirmPassword_failsValidation() {
        val viewModel = createViewModel(null)
        viewModel.onNewPasswordChange("validPassword123")
        viewModel.onConfirmPasswordChange("differentPassword")
        viewModel.onResetClick()
        
        val state = viewModel.uiState.value
        assertEquals("Mật khẩu xác nhận không khớp", state.confirmPasswordError)
        assertFalse(state.isResetSuccess)
    }

    @Test
    fun testReset_withoutFirebaseCode_isRejected() = runTest(mainDispatcherRule.testDispatcher) {
        val viewModel = createViewModel(null)
        viewModel.onNewPasswordChange("securePassword123")
        viewModel.onConfirmPasswordChange("securePassword123")
        
        viewModel.onResetClick()
        
        runCurrent()
        assertTrue(viewModel.uiState.value.isLoading)
        assertFalse(viewModel.uiState.value.isResetSuccess)
        
        advanceTimeBy(1500)
        runCurrent()
        
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertFalse(state.isResetSuccess)
        assertTrue(state.errorMessage != null)
    }

    @Test
    fun testReset_inRealMode_success() = runTest(mainDispatcherRule.testDispatcher) {
        val oobCode = "real_oob_code"
        val viewModel = createViewModel(oobCode)
        viewModel.onNewPasswordChange("securePassword123")
        viewModel.onConfirmPasswordChange("securePassword123")
        
        fakeAuthRepository.confirmPasswordResetResult = Result.success(Unit)
        
        viewModel.onResetClick()
        
        runCurrent()
        
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertTrue(state.isResetSuccess)
        assertNull(state.errorMessage)
    }

    @Test
    fun testReset_inRealMode_failure() = runTest(mainDispatcherRule.testDispatcher) {
        val oobCode = "expired_oob_code"
        val viewModel = createViewModel(oobCode)
        viewModel.onNewPasswordChange("securePassword123")
        viewModel.onConfirmPasswordChange("securePassword123")
        
        fakeAuthRepository.confirmPasswordResetResult = Result.failure(Exception("Mã khôi phục đã hết hạn"))
        
        viewModel.onResetClick()
        
        runCurrent()
        
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertFalse(state.isResetSuccess)
        assertEquals("Mã khôi phục đã hết hạn", state.errorMessage)
    }

    @Test
    fun testDismissError_clearsErrorMessage() = runTest(mainDispatcherRule.testDispatcher) {
        val viewModel = createViewModel("some_code")
        viewModel.onNewPasswordChange("securePassword123")
        viewModel.onConfirmPasswordChange("securePassword123")
        fakeAuthRepository.confirmPasswordResetResult = Result.failure(Exception("Some Error"))
        viewModel.onResetClick()
        runCurrent()
        
        assertEquals("Some Error", viewModel.uiState.value.errorMessage)
        
        viewModel.onDismissError()
        assertNull(viewModel.uiState.value.errorMessage)
    }
}
