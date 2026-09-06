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
import androidx.core.net.toUri

import com.example.eduvault.domain.repository.AuthRepository

/**
 * ViewModel cho màn hình Quên mật khẩu (2 bước: Nhập email → Xác nhận OTP).
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
        _uiState.update { it.copy(linkInput = link, errorMessage = null) }
        val parsedUri = try {
            link.toUri()
        } catch (e: Exception) {
            null
        }
        val oobCode = parsedUri?.getQueryParameter("oobCode")

        if (oobCode != null) {
            viewModelScope.launch {
                _uiState.update { it.copy(isLoading = true) }
                authRepository.verifyPasswordResetCode(oobCode)
                    .onSuccess { email ->
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                isRealLinkValid = true,
                                verifiedEmail = email,
                                oobCode = oobCode,
                                otp = "888888", // Mã OTP tượng trưng cho thật
                                otpError = null
                            )
                        }
                    }
                    .onFailure { exception ->
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

    /**
     * Bước 2: Xác nhận OTP người dùng nhập.
     * Lưu ý: Vì Firebase Auth gửi link đặt lại mật khẩu trực tiếp qua Email nên không dùng OTP số.
     * Hệ thống hỗ trợ chế độ Demo OTP 6 số và dán Link khôi phục thật để khôi phục mật khẩu 100%.
     */
    fun onVerifyOtpClick() {
        _uiState.update { it.copy(hasAttemptedVerify = true) }
        
        // Nếu dán link khôi phục thật hợp lệ -> Cho đi tiếp ngay
        if (_uiState.value.isRealLinkValid) {
            _uiState.update { it.copy(isVerifySuccess = true) }
            return
        }

        if (!validateOtp()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            delay(1200) // Tạo độ trễ mượt mà
            _uiState.update {
                it.copy(
                    isLoading = false,
                    errorMessage = "Vui lòng mở liên kết đặt lại mật khẩu trong email để tiếp tục."
                )
            }
        }
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
        _uiState.update {
            it.copy(
                isOtpSent = false,
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

    private fun validateOtp(): Boolean {
        val otp = _uiState.value.otp.trim()
        val error = when {
            otp.isBlank() -> "Vui lòng nhập mã OTP"
            otp.length < 6 -> "Mã OTP phải có đủ 6 chữ số"
            !otp.all { it.isDigit() } -> "Mã OTP chỉ được chứa chữ số"
            else -> null
        }
        if (error != null) {
            _uiState.update { it.copy(otpError = error) }
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
