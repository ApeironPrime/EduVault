package com.example.eduvault.feature.notes.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.eduvault.domain.model.UserNote
import com.example.eduvault.domain.repository.NoteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Trạng thái UI cho màn hình Ghi chú cá nhân.
 */
data class NotesUiState(
    val notes: List<UserNote> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    /** Ghi chú đang được chỉnh sửa trong bottom sheet (null = đóng sheet) */
    val editingNote: UserNote? = null,
    /** Nội dung đang gõ trong editor */
    val editorContent: String = "",
    val isSaving: Boolean = false,
    val saveSuccess: Boolean = false
)

/**
 * ViewModel cho màn hình Ghi chú cá nhân.
 */
@HiltViewModel
class NotesViewModel @Inject constructor(
    private val noteRepository: NoteRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(NotesUiState())
    val uiState: StateFlow<NotesUiState> = _uiState.asStateFlow()

    init {
        loadAllNotes()
    }

    /** Tải tất cả ghi chú của người dùng từ Firestore */
    fun loadAllNotes() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            noteRepository.getAllNotes()
                .onSuccess { notes ->
                    _uiState.update { it.copy(notes = notes, isLoading = false) }
                }
                .onFailure { err ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = err.localizedMessage ?: "Không thể tải ghi chú"
                        )
                    }
                }
        }
    }

    /**
     * Mở bottom sheet ghi chú cho một tài liệu cụ thể.
     * Nếu đã có ghi chú → nạp sẵn nội dung; nếu chưa → mở trống.
     */
    fun openNoteEditor(docId: String, docTitle: String, docCourseCode: String) {
        _uiState.update { it.copy(error = null) }
        viewModelScope.launch {
            // Tìm trong cache trước
            val existing = _uiState.value.notes.find { it.docId == docId }
            if (existing != null) {
                _uiState.update {
                    it.copy(editingNote = existing, editorContent = existing.noteContent)
                }
            } else {
                // Thử fetch từ Firestore (phòng trường hợp chưa load)
                noteRepository.getNote(docId).onSuccess { note ->
                    val noteToEdit = note ?: UserNote(
                        docId = docId,
                        docTitle = docTitle,
                        docCourseCode = docCourseCode
                    )
                    _uiState.update {
                        it.copy(
                            editingNote = noteToEdit,
                            editorContent = noteToEdit.noteContent
                        )
                    }
                }.onFailure {
                    // A failed read is not proof that the note does not exist.
                    _uiState.update {
                        it.copy(error = "Không thể mở ghi chú. Vui lòng kiểm tra kết nối rồi thử lại.")
                    }
                }
            }
        }
    }

    /** Cập nhật nội dung đang gõ */
    fun onEditorContentChanged(content: String) {
        _uiState.update { it.copy(editorContent = content) }
    }

    /** Lưu ghi chú hiện tại */
    fun saveCurrentNote() {
        val editingNote = _uiState.value.editingNote ?: return
        val content = _uiState.value.editorContent

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            val noteToSave = editingNote.copy(noteContent = content)
            noteRepository.saveNote(noteToSave)
                .onSuccess {
                    // Cập nhật list local
                    _uiState.update { state ->
                        val updatedList = state.notes.toMutableList()
                        val idx = updatedList.indexOfFirst { it.docId == noteToSave.docId }
                        val saved = noteToSave.copy(updatedAt = System.currentTimeMillis())
                        if (idx >= 0) updatedList[idx] = saved else updatedList.add(0, saved)
                        state.copy(
                            notes = updatedList.sortedByDescending { it.updatedAt },
                            isSaving = false,
                            saveSuccess = true,
                            editingNote = null
                        )
                    }
                }
                .onFailure { err ->
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            error = err.localizedMessage ?: "Lưu thất bại"
                        )
                    }
                }
        }
    }

    /** Xóa ghi chú đang mở */
    fun deleteCurrentNote() {
        val editingNote = _uiState.value.editingNote ?: return
        viewModelScope.launch {
            noteRepository.deleteNote(editingNote.docId)
                .onSuccess {
                    _uiState.update { state ->
                        state.copy(
                            notes = state.notes.filter { it.docId != editingNote.docId },
                            editingNote = null,
                            editorContent = ""
                        )
                    }
                }
                .onFailure { err ->
                    _uiState.update {
                        it.copy(error = err.localizedMessage ?: "Xóa thất bại")
                    }
                }
        }
    }

    /** Đóng bottom sheet editor mà không lưu */
    fun closeNoteEditor() {
        _uiState.update { it.copy(editingNote = null, editorContent = "", saveSuccess = false) }
    }

    /** Xóa thông báo lỗi */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    /** Reset saveSuccess sau khi UI đã xử lý */
    fun clearSaveSuccess() {
        _uiState.update { it.copy(saveSuccess = false) }
    }
}
