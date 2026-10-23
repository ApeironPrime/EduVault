package com.example.eduvault.feature.auth.ui

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
 * JVM Local Unit Tests for ForgotPasswordViewModel.
 * Verifies validation logic, countdown triggers, state transitions, and repo callbacks.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ForgotPasswordViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var fakeAuthRepository: FakeAuthRepository
    private lateinit var viewModel: ForgotPasswordViewModel

    @Before
    fun setUp() {
        fakeAuthRepository = FakeAuthRepository()
        viewModel = ForgotPasswordViewModel(fakeAuthRepository)
    }

    @Test
    fun testEmailChange_updatesStateAndClearsErrors() {
        viewModel.onEmailChange("test@example.com")
        val state = viewModel.uiState.value
        assertEquals("test@example.com", state.email)
        assertNull(state.emailError)
        assertNull(state.errorMessage)
    }

    @Test
    fun testOtpChange_updatesStateAndClearsErrors() {
        viewModel.onOtpChange("123456")
        val state = viewModel.uiState.value
        assertEquals("123456", state.otp)
        assertNull(state.otpError)
        assertNull(state.errorMessage)
    }

    @Test
    fun testOtpChange_ignoresInputLongerThan6Digits() {
        viewModel.onOtpChange("123456")
        viewModel.onOtpChange("1234567")
        val state = viewModel.uiState.value
        assertEquals("123456", state.otp) // The 7th digit should be ignored
    }

    @Test
    fun testSendOtp_withBlankEmail_failsValidation() = runTest(mainDispatcherRule.testDispatcher) {
        viewModel.onEmailChange("")
        viewModel.onSendOtpClick()
        
        val state = viewModel.uiState.value
        assertTrue(state.hasAttemptedSend)
        assertEquals("Vui lòng nhập email", state.emailError)
        assertFalse(state.isOtpSent)
    }

    @Test
    fun testSendOtp_withInvalidEmailFormat_failsValidation() = runTest(mainDispatcherRule.testDispatcher) {
        viewModel.onEmailChange("invalid-email")
        viewModel.onSendOtpClick()
        
        val state = viewModel.uiState.value
        assertTrue(state.hasAttemptedSend)
        assertEquals("Định dạng email không hợp lệ", state.emailError)
        assertFalse(state.isOtpSent)
    }

    @Test
    fun testSendOtp_withValidEmail_success() = runTest(mainDispatcherRule.testDispatcher) {
        viewModel.onEmailChange("valid@example.com")
        
        assertFalse(viewModel.uiState.value.isOtpSent)
        
        viewModel.onSendOtpClick()
        runCurrent()
        
        val state = viewModel.uiState.value
        assertTrue(state.isOtpSent)
        assertNull(state.emailError)
        assertNull(state.errorMessage)
        assertEquals(60, state.resendCountdown)
    }

    @Test
    fun testSendOtp_loadingStateTransition() = runTest(mainDispatcherRule.testDispatcher) {
        viewModel.onEmailChange("valid@example.com")
        
        // Setup repository delay to simulate async network latency
        fakeAuthRepository.delayMs = 1000
        
        viewModel.onSendOtpClick()
        
        // Executes up to the repository delay suspension point
        runCurrent()
        assertTrue(viewModel.uiState.value.isLoading)
        
        // Advance time to complete the delay
        advanceTimeBy(1000)
        runCurrent()
        
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertTrue(state.isOtpSent)
    }

    @Test
    fun testSendOtp_failure() = runTest(mainDispatcherRule.testDispatcher) {
        fakeAuthRepository.sendPasswordResetEmailResult = Result.failure(Exception("Email không tồn tại trong hệ thống"))
        viewModel.onEmailChange("notfound@example.com")
        viewModel.onSendOtpClick()
        
        runCurrent()
        
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertFalse(state.isOtpSent)
        assertEquals("Email không tồn tại trong hệ thống", state.errorMessage)
    }

    @Test
    fun testOtpCountdown_decrementsEachSecond() = runTest(mainDispatcherRule.testDispatcher) {
        viewModel.onEmailChange("valid@example.com")
        viewModel.onSendOtpClick()
        
        runCurrent()
        
        assertEquals(60, viewModel.uiState.value.resendCountdown)
        
        advanceTimeBy(1000)
        runCurrent()
        assertEquals(59, viewModel.uiState.value.resendCountdown)
        
        advanceTimeBy(9000)
        runCurrent()
        assertEquals(50, viewModel.uiState.value.resendCountdown)
        
        advanceTimeBy(50000)
        runCurrent()
        assertEquals(0, viewModel.uiState.value.resendCountdown)
    }

    @Test
    fun testResendOtp_failsIfCountdownNotFinished() = runTest(mainDispatcherRule.testDispatcher) {
        viewModel.onEmailChange("valid@example.com")
        viewModel.onSendOtpClick()
        runCurrent()
        
        assertEquals(60, viewModel.uiState.value.resendCountdown)
        
        viewModel.onResendOtpClick()
        runCurrent()
        assertEquals(60, viewModel.uiState.value.resendCountdown)
    }

    @Test
    fun testResendOtp_succeedsIfCountdownIsZero() = runTest(mainDispatcherRule.testDispatcher) {
        viewModel.onEmailChange("valid@example.com")
        viewModel.onSendOtpClick()
        runCurrent()
        
        advanceTimeBy(60000) // Finish countdown
        runCurrent()
        assertEquals(0, viewModel.uiState.value.resendCountdown)
        
        viewModel.onResendOtpClick()
        runCurrent()
        assertEquals(60, viewModel.uiState.value.resendCountdown)
    }

    @Test
    fun testVerifyLink_withValidFirebaseLink_success() = runTest(mainDispatcherRule.testDispatcher) {
        val oobCode = "fake_oob_code_123"
        val realLink = "https://eduvault.firebaseapp.com/__/auth/action?mode=resetPassword&oobCode=$oobCode"
        
        fakeAuthRepository.verifyPasswordResetCodeResult = Result.success("test@example.com")
        
        viewModel.onLinkInputChange(realLink)
        
        runCurrent()
        
        val state = viewModel.uiState.value
        assertTrue(state.isRealLinkValid)
        assertEquals("test@example.com", state.verifiedEmail)
        assertEquals(oobCode, state.oobCode)
        assertEquals("", state.otp)
        assertFalse(state.isLoading)
    }

    @Test
    fun testVerifyLink_withInvalidLink_fails() = runTest(mainDispatcherRule.testDispatcher) {
        val badLink = "https://invalid-link-without-oob-code.com"
        viewModel.onLinkInputChange(badLink)
        
        val state = viewModel.uiState.value
        assertFalse(state.isRealLinkValid)
        assertNull(state.oobCode)
        assertNull(state.verifiedEmail)
    }

    @Test
    fun testVerifyLink_expiredOrInvalidOobCode_failure() = runTest(mainDispatcherRule.testDispatcher) {
        val oobCode = "expired_code"
        val realLink = "https://eduvault.firebaseapp.com/__/auth/action?mode=resetPassword&oobCode=$oobCode"
        
        fakeAuthRepository.verifyPasswordResetCodeResult = Result.failure(Exception("Mã khôi phục đã hết hạn hoặc không hợp lệ"))
        
        viewModel.onLinkInputChange(realLink)
        
        runCurrent()
        
        val state = viewModel.uiState.value
        assertFalse(state.isRealLinkValid)
        assertNull(state.oobCode)
        assertEquals("Mã khôi phục đã hết hạn hoặc không hợp lệ", state.errorMessage)
    }

    @Test
    fun testVerifyOtp_withValidRealLink_succeedsImmediately() = runTest(mainDispatcherRule.testDispatcher) {
        val oobCode = "fake_oob_code_123"
        val realLink = "https://eduvault.firebaseapp.com/__/auth/action?mode=resetPassword&oobCode=$oobCode"
        fakeAuthRepository.verifyPasswordResetCodeResult = Result.success("test@example.com")
        viewModel.onLinkInputChange(realLink)
        runCurrent()
        
        viewModel.onVerifyOtpClick()
        runCurrent()
        
        val state = viewModel.uiState.value
        assertTrue(state.isVerifySuccess)
        assertNull(state.errorMessage)
    }

    @Test
    fun testVerifyOtp_withDemoOtp_failsValidation_ifBlank() = runTest(mainDispatcherRule.testDispatcher) {
        viewModel.onOtpChange("")
        viewModel.onVerifyOtpClick()
        
        val state = viewModel.uiState.value
        assertTrue(state.hasAttemptedVerify)
        assertEquals("Vui lòng mở hoặc dán liên kết đặt lại mật khẩu trong email để tiếp tục.", state.errorMessage)
        assertFalse(state.isVerifySuccess)
    }

    @Test
    fun testVerifyOtp_withDemoOtp_failsValidation_ifShort() = runTest(mainDispatcherRule.testDispatcher) {
        viewModel.onOtpChange("123")
        viewModel.onVerifyOtpClick()
        
        val state = viewModel.uiState.value
        assertEquals("Vui lòng mở hoặc dán liên kết đặt lại mật khẩu trong email để tiếp tục.", state.errorMessage)
        assertFalse(state.isVerifySuccess)
    }

    @Test
    fun testVerifyOtp_withDemoOtp_failsValidation_ifNonDigits() = runTest(mainDispatcherRule.testDispatcher) {
        viewModel.onOtpChange("12a456")
        viewModel.onVerifyOtpClick()
        
        val state = viewModel.uiState.value
        assertEquals("Vui lòng mở hoặc dán liên kết đặt lại mật khẩu trong email để tiếp tục.", state.errorMessage)
        assertFalse(state.isVerifySuccess)
    }

    @Test
    fun testVerifyOtp_withSixDigitValue_doesNotAuthorizeReset() = runTest(mainDispatcherRule.testDispatcher) {
        viewModel.onOtpChange("123456")
        viewModel.onVerifyOtpClick()
        
        runCurrent()
        assertFalse(viewModel.uiState.value.isLoading)
        
        advanceTimeBy(1200)
        runCurrent()
        
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertFalse(state.isVerifySuccess)
        assertTrue(state.errorMessage != null)
    }

    @Test
    fun testBackToEmailStep_resetsOtpStateAndCountdown() = runTest(mainDispatcherRule.testDispatcher) {
        viewModel.onEmailChange("valid@example.com")
        viewModel.onSendOtpClick()
        runCurrent()
        
        viewModel.onOtpChange("123456")
        
        viewModel.onBackToEmailStep()
        
        val state = viewModel.uiState.value
        assertFalse(state.isOtpSent)
        assertEquals("", state.otp)
        assertNull(state.otpError)
        assertNull(state.errorMessage)
        assertFalse(state.hasAttemptedVerify)
        assertEquals(0, state.resendCountdown)
    }

    @Test
    fun testDismissError_clearsErrorMessage() = runTest(mainDispatcherRule.testDispatcher) {
        viewModel.onEmailChange("valid@example.com")
        fakeAuthRepository.sendPasswordResetEmailResult = Result.failure(Exception("An error occurred"))
        viewModel.onSendOtpClick()
        runCurrent()
        
        assertEquals("An error occurred", viewModel.uiState.value.errorMessage)
        
        viewModel.onDismissError()
        assertNull(viewModel.uiState.value.errorMessage)
    }

    @Test fun pastedOpaqueLinkDoesNotCrashOrKeepVerifiedAccess() = runTest(mainDispatcherRule.testDispatcher) {
        viewModel.onLinkInputChange("https://example.com/?oobCode=valid")
        runCurrent()
        assertTrue(viewModel.uiState.value.isRealLinkValid)
        viewModel.onLinkInputChange("mailto:test@example.com")
        runCurrent()
        assertFalse(viewModel.uiState.value.isRealLinkValid)
        assertNull(viewModel.uiState.value.oobCode)
        viewModel.onVerifyOtpClick()
        assertFalse(viewModel.uiState.value.isVerifySuccess)
    }
}
