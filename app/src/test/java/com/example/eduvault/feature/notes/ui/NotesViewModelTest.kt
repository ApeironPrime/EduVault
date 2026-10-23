package com.example.eduvault.feature.notes.ui

import com.example.eduvault.MainDispatcherRule
import com.example.eduvault.domain.model.UserNote
import com.example.eduvault.domain.repository.NoteRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class NotesViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private class FakeNoteRepository : NoteRepository {
        val notesMap = mutableMapOf<String, UserNote>()
        var getAllNotesResult: Result<List<UserNote>>? = null
        var getNoteResult: Result<UserNote?>? = null
        var saveNoteResult: Result<Unit> = Result.success(Unit)
        var deleteNoteResult: Result<Unit> = Result.success(Unit)

        override suspend fun getNote(docId: String): Result<UserNote?> {
            getNoteResult?.let { return it }
            return Result.success(notesMap[docId])
        }

        override suspend fun saveNote(note: UserNote): Result<Unit> {
            saveNoteResult.onSuccess {
                notesMap[note.docId] = note
            }
            return saveNoteResult
        }

        override suspend fun deleteNote(docId: String): Result<Unit> {
            deleteNoteResult.onSuccess {
                notesMap.remove(docId)
            }
            return deleteNoteResult
        }

        override suspend fun getAllNotes(): Result<List<UserNote>> {
            getAllNotesResult?.let { return it }
            return Result.success(notesMap.values.sortedByDescending { it.updatedAt })
        }
    }

    private fun sampleNote(docId: String, title: String, content: String, updated: Long = 1000L) = UserNote(
        docId = docId,
        docTitle = title,
        docCourseCode = "CS101",
        noteContent = content,
        updatedAt = updated,
        createdAt = 100L
    )

    @Test
    fun `init loads all notes and updates uiState on success`() = runTest {
        val fakeRepo = FakeNoteRepository().apply {
            notesMap["doc_1"] = sampleNote("doc_1", "Lập trình mạng", "Nội dung ghi chú socket")
            notesMap["doc_2"] = sampleNote("doc_2", "Hệ điều hành", "Quản lý tiến trình")
        }

        val viewModel = NotesViewModel(fakeRepo)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(2, state.notes.size)
        assertNull(state.error)
    }

    @Test
    fun `loadAllNotes sets error state when repository fails`() = runTest {
        val fakeRepo = FakeNoteRepository().apply {
            getAllNotesResult = Result.failure(Exception("Lỗi kết nối Firestore"))
        }

        val viewModel = NotesViewModel(fakeRepo)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertTrue(state.notes.isEmpty())
        assertEquals("Lỗi kết nối Firestore", state.error)
    }

    @Test
    fun `openNoteEditor with existing note in cache populates editor immediately`() = runTest {
        val note = sampleNote("doc_1", "Tiêu đề tài liệu", "Ghi chú đã lưu từ trước")
        val fakeRepo = FakeNoteRepository().apply {
            notesMap["doc_1"] = note
        }

        val viewModel = NotesViewModel(fakeRepo)
        advanceUntilIdle()

        viewModel.openNoteEditor("doc_1", "Tiêu đề tài liệu", "CS101")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertNotNull(state.editingNote)
        assertEquals("doc_1", state.editingNote?.docId)
        assertEquals("Ghi chú đã lưu từ trước", state.editorContent)
    }

    @Test
    fun `openNoteEditor with note in repo populates editor from repository`() = runTest {
        val fakeRepo = FakeNoteRepository()
        val viewModel = NotesViewModel(fakeRepo)
        advanceUntilIdle()

        // Simulate note existing in repo but not loaded initially
        val repoNote = sampleNote("doc_remote", "Kinh tế vi mô", "Nội dung remote")
        fakeRepo.getNoteResult = Result.success(repoNote)

        viewModel.openNoteEditor("doc_remote", "Kinh tế vi mô", "EC101")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertNotNull(state.editingNote)
        assertEquals("doc_remote", state.editingNote?.docId)
        assertEquals("Nội dung remote", state.editorContent)
    }

    @Test
    fun `openNoteEditor when note does not exist creates blank note template`() = runTest {
        val fakeRepo = FakeNoteRepository()
        val viewModel = NotesViewModel(fakeRepo)
        advanceUntilIdle()

        viewModel.openNoteEditor("doc_new", "Hóa đại cương", "CH101")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertNotNull(state.editingNote)
        assertEquals("doc_new", state.editingNote?.docId)
        assertEquals("Hóa đại cương", state.editingNote?.docTitle)
        assertEquals("", state.editorContent)
    }

    @Test fun `failed note read does not open blank editor that could overwrite stored content`() = runTest {
        val repo = FakeNoteRepository().apply {
            getNoteResult = Result.failure(Exception("Network unavailable"))
        }
        val vm = NotesViewModel(repo)
        advanceUntilIdle()
        vm.openNoteEditor("doc", "Git Flow", "GIT101")
        advanceUntilIdle()
        assertNull(vm.uiState.value.editingNote)
        assertTrue(vm.uiState.value.error!!.contains("Không thể mở ghi chú"))
        vm.saveCurrentNote()
        advanceUntilIdle()
        assertTrue(repo.notesMap.isEmpty())
    }

    @Test
    fun `onEditorContentChanged updates editorContent in state`() = runTest {
        val fakeRepo = FakeNoteRepository()
        val viewModel = NotesViewModel(fakeRepo)
        advanceUntilIdle()

        viewModel.openNoteEditor("doc_1", "Bài giảng", "CS101")
        advanceUntilIdle()

        viewModel.onEditorContentChanged("Nội dung mới đang gõ...")
        assertEquals("Nội dung mới đang gõ...", viewModel.uiState.value.editorContent)
    }

    @Test
    fun `saveCurrentNote persists note and updates local sorted list`() = runTest {
        val fakeRepo = FakeNoteRepository()
        val viewModel = NotesViewModel(fakeRepo)
        advanceUntilIdle()

        viewModel.openNoteEditor("doc_save", "Toán rời rạc", "MATH101")
        advanceUntilIdle()

        viewModel.onEditorContentChanged("Đồ thị Euler và Hamilton")
        viewModel.saveCurrentNote()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isSaving)
        assertTrue(state.saveSuccess)
        assertNull(state.editingNote) // sheet is closed

        // Check local list updated
        assertEquals(1, state.notes.size)
        assertEquals("Đồ thị Euler và Hamilton", state.notes[0].noteContent)
        assertEquals("doc_save", state.notes[0].docId)
    }

    @Test
    fun `saveCurrentNote failure sets error state`() = runTest {
        val fakeRepo = FakeNoteRepository().apply {
            saveNoteResult = Result.failure(Exception("Không có quyền ghi dữ liệu"))
        }
        val viewModel = NotesViewModel(fakeRepo)
        advanceUntilIdle()

        viewModel.openNoteEditor("doc_err", "Toán", "MATH")
        advanceUntilIdle()

        viewModel.saveCurrentNote()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isSaving)
        assertFalse(state.saveSuccess)
        assertEquals("Không có quyền ghi dữ liệu", state.error)
    }

    @Test
    fun `deleteCurrentNote removes note from state and resets editor`() = runTest {
        val note = sampleNote("doc_del", "Vật lý", "Cơ học lượng tử")
        val fakeRepo = FakeNoteRepository().apply {
            notesMap["doc_del"] = note
        }
        val viewModel = NotesViewModel(fakeRepo)
        advanceUntilIdle()

        assertEquals(1, viewModel.uiState.value.notes.size)

        viewModel.openNoteEditor("doc_del", "Vật lý", "PHYS")
        advanceUntilIdle()

        viewModel.deleteCurrentNote()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.notes.isEmpty())
        assertNull(state.editingNote)
        assertEquals("", state.editorContent)
    }

    @Test
    fun `closeNoteEditor, clearError, and clearSaveSuccess reset state properly`() = runTest {
        val fakeRepo = FakeNoteRepository()
        val viewModel = NotesViewModel(fakeRepo)
        advanceUntilIdle()

        viewModel.openNoteEditor("doc_1", "Tiêu đề", "CS")
        advanceUntilIdle()
        viewModel.onEditorContentChanged("Tạm bợ")

        viewModel.closeNoteEditor()
        val state = viewModel.uiState.value
        assertNull(state.editingNote)
        assertEquals("", state.editorContent)
        assertFalse(state.saveSuccess)

        // Test clearError
        fakeRepo.getAllNotesResult = Result.failure(Exception("Lỗi tạm thời"))
        viewModel.loadAllNotes()
        advanceUntilIdle()
        assertEquals("Lỗi tạm thời", viewModel.uiState.value.error)
        viewModel.clearError()
        assertNull(viewModel.uiState.value.error)

        // Test clearSaveSuccess
        viewModel.openNoteEditor("doc_2", "Tiêu đề", "CS")
        advanceUntilIdle()
        viewModel.saveCurrentNote()
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.saveSuccess)
        viewModel.clearSaveSuccess()
        assertFalse(viewModel.uiState.value.saveSuccess)
    }
}
