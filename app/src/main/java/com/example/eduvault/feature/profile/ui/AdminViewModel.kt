package com.example.eduvault.feature.profile.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.eduvault.domain.model.DocumentCategory
import com.example.eduvault.domain.model.User
import com.example.eduvault.domain.repository.AdminRepository
import com.example.eduvault.domain.repository.CategoryRepository
import com.example.eduvault.feature.library.ui.LibraryDoc
import com.example.eduvault.feature.library.ui.toLibraryDoc
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AdminUiState(
    val pendingDocs: List<LibraryDoc> = emptyList(),
    val reportedDocs: List<LibraryDoc> = emptyList(),
    val users: List<User> = emptyList(),
    val categories: List<DocumentCategory> = emptyList(),
    val isLoadingPending: Boolean = false,
    val isLoadingReported: Boolean = false,
    val isLoadingUsers: Boolean = false,
    val isLoadingCats: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val accessDenied: Boolean = false
)

@HiltViewModel
class AdminViewModel @Inject constructor(
    private val categoryRepository: CategoryRepository,
    private val adminRepository: AdminRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminUiState())
    val uiState: StateFlow<AdminUiState> = _uiState.asStateFlow()

    init {
        loadAllData()
    }

    fun loadAllData() {
        _uiState.update { it.copy(errorMessage = null, accessDenied = false) }
        loadPendingDocs()
        loadReportedDocs()
        loadUsers()
        loadCategories()
    }

    fun loadPendingDocs() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingPending = true) }
            adminRepository.getPendingDocuments().onSuccess { docs ->
                _uiState.update { it.copy(pendingDocs = docs.map { doc -> doc.toLibraryDoc() }, isLoadingPending = false) }
            }.onFailure { error ->
                _uiState.update { it.copy(isLoadingPending = false, errorMessage = adminErrorMessage(error), accessDenied = it.accessDenied || error.isAdminPermissionDenied()) }
            }
        }
    }

    fun loadReportedDocs() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingReported = true) }
            adminRepository.getReportedDocuments().onSuccess { docs ->
                _uiState.update { it.copy(reportedDocs = docs.map { doc -> doc.toLibraryDoc() }, isLoadingReported = false) }
            }.onFailure { error ->
                _uiState.update { it.copy(isLoadingReported = false, errorMessage = adminErrorMessage(error), accessDenied = it.accessDenied || error.isAdminPermissionDenied()) }
            }
        }
    }

    fun loadUsers() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingUsers = true) }
            adminRepository.getAllUsers().onSuccess { members ->
                _uiState.update { it.copy(users = members, isLoadingUsers = false) }
            }.onFailure { error ->
                _uiState.update { it.copy(isLoadingUsers = false, errorMessage = adminErrorMessage(error), accessDenied = it.accessDenied || error.isAdminPermissionDenied()) }
            }
        }
    }

    fun loadCategories() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingCats = true) }
            categoryRepository.getCategories().onSuccess { cats ->
                _uiState.update { it.copy(categories = cats, isLoadingCats = false) }
            }.onFailure { error ->
                _uiState.update { it.copy(isLoadingCats = false, errorMessage = adminErrorMessage(error), accessDenied = it.accessDenied || error.isAdminPermissionDenied()) }
            }
        }
    }

    fun approveDocument(docId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingPending = true, successMessage = null, errorMessage = null) }
            adminRepository.approveDocument(docId).onSuccess {
                _uiState.update { it.copy(successMessage = "Phê duyệt tài liệu thành công!") }
                loadPendingDocs()
            }.onFailure { error ->
                _uiState.update { it.copy(isLoadingPending = false, errorMessage = adminErrorMessage(error), accessDenied = it.accessDenied || error.isAdminPermissionDenied()) }
            }
        }
    }

    fun rejectDocument(docId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingPending = true, successMessage = null, errorMessage = null) }
            adminRepository.deleteDocument(docId).onSuccess {
                _uiState.update { it.copy(successMessage = "Từ chối và xóa tài liệu vi phạm thành công!") }
                loadPendingDocs()
            }.onFailure { error ->
                _uiState.update { it.copy(isLoadingPending = false, errorMessage = adminErrorMessage(error), accessDenied = it.accessDenied || error.isAdminPermissionDenied()) }
            }
        }
    }

    fun dismissReport(docId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingReported = true, successMessage = null, errorMessage = null) }
            adminRepository.dismissReport(docId).onSuccess {
                _uiState.update { it.copy(successMessage = "Bác tố cáo tài liệu thành công!") }
                loadReportedDocs()
            }.onFailure { error ->
                _uiState.update { it.copy(isLoadingReported = false, errorMessage = adminErrorMessage(error), accessDenied = it.accessDenied || error.isAdminPermissionDenied()) }
            }
        }
    }

    fun deleteReportedDoc(docId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingReported = true, successMessage = null, errorMessage = null) }
            adminRepository.deleteDocument(docId).onSuccess {
                _uiState.update { it.copy(successMessage = "Xóa tài liệu bị tố cáo thành công!") }
                loadReportedDocs()
            }.onFailure { error ->
                _uiState.update { it.copy(isLoadingReported = false, errorMessage = adminErrorMessage(error), accessDenied = it.accessDenied || error.isAdminPermissionDenied()) }
            }
        }
    }

    fun toggleUserBlock(uid: String, currentBlockStatus: Boolean) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingUsers = true, successMessage = null, errorMessage = null) }
            adminRepository.updateUserBlockStatus(uid, !currentBlockStatus).onSuccess {
                val verb = if (currentBlockStatus) "Mở khóa" else "Khóa"
                _uiState.update { it.copy(successMessage = "$verb tài khoản thành công!") }
                loadUsers()
            }.onFailure { error ->
                _uiState.update { it.copy(isLoadingUsers = false, errorMessage = adminErrorMessage(error), accessDenied = it.accessDenied || error.isAdminPermissionDenied()) }
            }
        }
    }

    fun addCategory(name: String, emoji: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingCats = true, successMessage = null, errorMessage = null) }
            categoryRepository.addCategory(name, emoji).onSuccess {
                _uiState.update { it.copy(successMessage = "Thêm danh mục mới thành công!") }
                loadCategories()
            }.onFailure { error ->
                _uiState.update { it.copy(isLoadingCats = false, errorMessage = adminErrorMessage(error), accessDenied = it.accessDenied || error.isAdminPermissionDenied()) }
            }
        }
    }

    fun updateCategory(id: String, name: String, emoji: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingCats = true, successMessage = null, errorMessage = null) }
            categoryRepository.updateCategory(id, name, emoji).onSuccess {
                _uiState.update { it.copy(successMessage = "Cập nhật danh mục thành công!") }
                loadCategories()
            }.onFailure { error ->
                _uiState.update { it.copy(isLoadingCats = false, errorMessage = adminErrorMessage(error), accessDenied = it.accessDenied || error.isAdminPermissionDenied()) }
            }
        }
    }

    fun deleteCategory(id: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingCats = true, successMessage = null, errorMessage = null) }
            categoryRepository.deleteCategory(id).onSuccess {
                _uiState.update { it.copy(successMessage = "Xóa danh mục thành công!") }
                loadCategories()
            }.onFailure { error ->
                _uiState.update { it.copy(isLoadingCats = false, errorMessage = adminErrorMessage(error), accessDenied = it.accessDenied || error.isAdminPermissionDenied()) }
            }
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(successMessage = null, errorMessage = null) }
    }
}

internal fun Throwable.isAdminPermissionDenied(): Boolean = generateSequence(this) { it.cause }
    .any { it.message.orEmpty().contains("PERMISSION_DENIED", ignoreCase = true) ||
        it.message.orEmpty().contains("insufficient permissions", ignoreCase = true) }

internal fun adminErrorMessage(error: Throwable): String = when {
    error.isAdminPermissionDenied() -> "Tài khoản chưa được cấp quyền quản trị. Vui lòng liên hệ người quản lý chương trình."
    generateSequence(error) { it.cause }.any { it.message.orEmpty().contains("UNAVAILABLE", true) } ->
        "Chưa kết nối được dịch vụ. Kiểm tra mạng và thử lại."
    else -> "Chưa thực hiện được thao tác quản trị. Vui lòng thử lại."
}
