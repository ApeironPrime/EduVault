package com.example.eduvault.feature.profile.ui

import com.example.eduvault.domain.model.AuthState
import com.example.eduvault.domain.model.User

/**
 * Trạng thái UI cho màn hình Hồ sơ cá nhân (Profile).
 */
data class ProfileUiState(
    /** Giữ cả User lẫn trạng thái Guest/Authenticated trong một sealed class */
    val authState: AuthState = AuthState.Guest,
    /** Backward-compat: nullable user cho các thành phần cũ cần User? */
    val user: User? = null,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val isEditing: Boolean = false,

    // Form inputs during edit mode
    val editFullName: String = "",
    val editUniversity: String = "",
    val editAvatarUrl: String = "",

    // Password change status
    val isChangingPassword: Boolean = false,
    val changePasswordError: String? = null,
    val changePasswordSuccess: String? = null
)
