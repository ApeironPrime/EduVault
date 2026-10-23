package com.example.eduvault.feature.auth.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import java.net.URI
import java.net.URLDecoder

import com.example.eduvault.domain.repository.AuthRepository

/**
 * Khôi phục mật khẩu bằng email và xác minh liên kết Firebase Auth.
 *
 * Quy tắc tuân thủ:
 * - Không có Context, Activity, Fragment, hoặc View nào trong class này.
 * - Trạng thái UI được quản lý 100% qua StateFlow.
 * - Mọi tác vụ bất đồng bộ chạy trong viewModelScope.
 */
@HiltViewModel
class ForgotPasswordViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ForgotPasswordUiState())
    val uiState: StateFlow<ForgotPasswordUiState> = _uiState.asStateFlow()

    private var countdownJob: Job? = null
    private var linkVerificationJob: Job? = null

    // ─── Event handlers ───────────────────────────────────────────────────────

    fun onEmailChange(email: String) {
        _uiState.update { it.copy(email = email, emailError = null, errorMessage = null) }
    }

    fun onOtpChange(otp: String) {
        if (otp.length <= 6) {
            _uiState.update { it.copy(otp = otp, otpError = null, errorMessage = null) }
        }
    }

    fun onLinkInputChange(link: String) {
        linkVerificationJob?.cancel()
        _uiState.update { it.copy(linkInput = link, errorMessage = null, isLoading = false,
            isRealLinkValid = false, oobCode = null, verifiedEmail = null, isVerifySuccess = false) }
        val oobCode = try {
            val uri = URI(link.trim())
            if (!uri.isOpaque && uri.scheme in listOf("https", "http") && !uri.host.isNullOrBlank()) {
                uri.rawQuery?.split("&")?.firstOrNull { it.substringBefore("=") == "oobCode" }
                    ?.substringAfter("=", "")?.let { URLDecoder.decode(it, "UTF-8") }?.takeIf { it.isNotBlank() }
            } else null
        } catch (_: Exception) { null }

        if (oobCode != null) {
            linkVerificationJob = viewModelScope.launch {
                _uiState.update { it.copy(isLoading = true) }
                authRepository.verifyPasswordResetCode(oobCode)
                    .onSuccess { email ->
                        if (_uiState.value.linkInput != link) return@onSuccess
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                isRealLinkValid = true,
                                verifiedEmail = email,
                                oobCode = oobCode,
                                otpError = null
                            )
                        }
                    }
                    .onFailure { exception ->
                        if (_uiState.value.linkInput != link) return@onFailure
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                isRealLinkValid = false,
                                oobCode = null,
                                errorMessage = exception.message ?: "Mã khôi phục đã hết hạn hoặc không hợp lệ. Vui lòng gửi lại yêu cầu."
                            )
                        }
                    }
            }
        } else {
            _uiState.update {
                it.copy(
                    isRealLinkValid = false,
                    oobCode = null,
                    verifiedEmail = null
                )
            }
        }
    }

    /**
     * Bước 1: Gửi email đặt lại mật khẩu của Firebase Auth.
     */
    fun onSendOtpClick() {
        _uiState.update { it.copy(hasAttemptedSend = true) }
        if (!validateEmail()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val email = _uiState.value.email.trim()
            authRepository.sendPasswordResetEmail(email)
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isOtpSent = true,
                            otp = "",
                            linkInput = "",
                            isRealLinkValid = false,
                            verifiedEmail = null,
                            oobCode = null,
                            hasAttemptedVerify = false,
                        )
                    }
                    startResendCountdown()
                }
                .onFailure { exception ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = exception.message ?: "Gửi email khôi phục thất bại. Vui lòng thử lại."
                        )
                    }
                }
        }
    }

    /** Tiếp tục chỉ khi liên kết khôi phục đã được xác minh. */
    fun onVerifyOtpClick() {
        _uiState.update { it.copy(hasAttemptedVerify = true) }
        
        // Nếu dán link khôi phục thật hợp lệ -> Cho đi tiếp ngay
        if (_uiState.value.isRealLinkValid) {
            _uiState.update { it.copy(isVerifySuccess = true) }
            return
        }

        _uiState.update { it.copy(errorMessage = "Vui lòng mở hoặc dán liên kết đặt lại mật khẩu trong email để tiếp tục.") }

    }

    /**
     * Gửi lại OTP (chỉ available khi countdown = 0).
     */
    fun onResendOtpClick() {
        if (_uiState.value.resendCountdown > 0) return
        onSendOtpClick()
    }

    /**
     * Quay lại bước nhập email từ bước OTP.
     */
    fun onBackToEmailStep() {
        countdownJob?.cancel()
        linkVerificationJob?.cancel()
        _uiState.update {
            it.copy(
                isOtpSent = false,
                isLoading = false,
                isRealLinkValid = false,
                oobCode = null,
                verifiedEmail = null,
                otp = "",
                otpError = null,
                errorMessage = null,
                hasAttemptedVerify = false,
                resendCountdown = 0,
            )
        }
    }

    fun onDismissError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    // ─── Private helpers ──────────────────────────────────────────────────────

    private fun validateEmail(): Boolean {
        val email = _uiState.value.email.trim()
        val error = when {
            email.isBlank() -> "Vui lòng nhập email"
            !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() ->
                "Định dạng email không hợp lệ"
            else -> null
        }
        if (error != null) {
            _uiState.update { it.copy(emailError = error) }
            return false
        }
        return true
    }

    private fun startResendCountdown(seconds: Int = 60) {
        countdownJob?.cancel()
        countdownJob = viewModelScope.launch {
            for (i in seconds downTo 1) {
                _uiState.update { it.copy(resendCountdown = i) }
                delay(1000)
            }
            _uiState.update { it.copy(resendCountdown = 0) }
        }
    }

    override fun onCleared() {
        super.onCleared()
        countdownJob?.cancel()
    }
}
