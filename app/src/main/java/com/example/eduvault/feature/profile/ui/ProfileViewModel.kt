package com.example.eduvault.feature.profile.ui

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.eduvault.domain.model.AuthState
import com.example.eduvault.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.CancellationException
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadUserProfile()
    }

    /**
     * Tải thông tin tài khoản người dùng hiện tại
     */
    fun loadUserProfile() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            authRepository.getCurrentUser()
                .onSuccess { user ->
                    if (user != null) {
                        _uiState.update {
                            it.copy(
                                authState = AuthState.Authenticated(user),
                                user = user,
                                isLoading = false,
                                editFullName = user.fullName,
                                editUniversity = user.university,
                                editAvatarUrl = user.avatarUrl
                            )
                        }
                    } else {
                        // Chưa đăng nhập — chế độ khách
                        _uiState.update {
                            it.copy(
                                authState = AuthState.Guest,
                                user = null,
                                isLoading = false
                            )
                        }
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            authState = AuthState.Guest,
                            isLoading = false,
                            errorMessage = error.message
                        )
                    }
                }
        }
    }

    fun onStartEditing() {
        val user = _uiState.value.user
        _uiState.update {
            it.copy(
                isEditing = true,
                editFullName = user?.fullName ?: "",
                editUniversity = user?.university ?: "",
                editAvatarUrl = user?.avatarUrl ?: "",
                errorMessage = null,
                successMessage = null
            )
        }
    }

    fun onCancelEditing() {
        _uiState.update { it.copy(isEditing = false, errorMessage = null, successMessage = null) }
    }

    fun onFullNameChange(name: String) {
        _uiState.update { it.copy(editFullName = name) }
    }

    fun onUniversityChange(uni: String) {
        _uiState.update { it.copy(editUniversity = uni) }
    }

    /**
     * Xử lý sao chép ảnh được chọn từ thiết bị của người dùng vào bộ nhớ trong cục bộ
     */
    fun handleCustomAvatarPicked(context: Context, uri: Uri) {
        viewModelScope.launch {
            var draft: File? = null
            try {
                val file = withContext(Dispatchers.IO) {
                    val target = File.createTempFile("profile_avatar_", ".jpg", context.filesDir)
                    draft = target
                    val input = context.contentResolver.openInputStream(uri) ?: throw IOException("Không đọc được ảnh")
                    input.use { source -> target.outputStream().use { output -> source.copyTo(output) } }
                    target
                }
                if (_uiState.value.isEditing) _uiState.update { it.copy(editAvatarUrl = file.absolutePath) }
                else file.delete()
            } catch (cancelled: CancellationException) {
                draft?.delete()
                throw cancelled
            } catch (_: Exception) {
                draft?.delete()
                _uiState.update { it.copy(errorMessage = "Không đọc được ảnh đại diện. Vui lòng chọn ảnh khác.") }
            }
        }
    }

    /**
     * Lưu thông tin hồ sơ thay đổi vào hệ thống Firebase + Firestore
     */
    fun saveProfile() {
        val state = _uiState.value
        if (state.editFullName.trim().isEmpty()) {
            _uiState.update { it.copy(errorMessage = "Họ tên không được để trống.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null, successMessage = null) }
            authRepository.updateProfile(
                fullName = state.editFullName.trim(),
                university = state.editUniversity.trim(),
                avatarUrl = state.editAvatarUrl
            ).onSuccess { updatedUser ->
                _uiState.update {
                    it.copy(
                        user = updatedUser,
                        isSaving = false,
                        isEditing = false,
                        successMessage = "Cập nhật hồ sơ thành công!"
                    )
                }
            }.onFailure { error ->
                _uiState.update { it.copy(isSaving = false, errorMessage = error.message) }
            }
        }
    }

    /**
     * Thay đổi mật khẩu tài khoản
     */
    fun onChangePassword(current: String, new: String, onFinished: () -> Unit) {
        if (current.isBlank() || new.isBlank()) {
            _uiState.update { it.copy(changePasswordError = "Vui lòng nhập đầy đủ thông tin.") }
            return
        }
        if (new.length < 6) {
            _uiState.update { it.copy(changePasswordError = "Mật khẩu mới phải có ít nhất 6 ký tự.") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isChangingPassword = true, changePasswordError = null, changePasswordSuccess = null) }
            authRepository.changePassword(current, new)
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            isChangingPassword = false,
                            changePasswordSuccess = "Đổi mật khẩu thành công!"
                        )
                    }
                    onFinished()
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isChangingPassword = false,
                            changePasswordError = error.message ?: "Đổi mật khẩu thất bại."
                        )
                    }
                }
        }
    }

    /**
     * Xóa thông báo lỗi/thành công của chức năng đổi mật khẩu
     */
    fun clearChangePasswordMessages() {
        _uiState.update { it.copy(changePasswordError = null, changePasswordSuccess = null) }
    }

    /**
     * Đăng xuất tài khoản
     */
    fun logout(onSuccess: () -> Unit) {
        authRepository.logout()
        onSuccess()
    }
}
