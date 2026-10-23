package com.example.eduvault.feature.library.ui

import com.example.eduvault.MainDispatcherRule
import com.example.eduvault.data.repository.FakeAuthRepository
import com.example.eduvault.domain.model.*
import com.example.eduvault.domain.repository.*
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
import java.io.File

@OptIn(ExperimentalCoroutinesApi::class)
class LibraryViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private class FakeDocumentRepository : DocumentRepository {
        var documentsList: List<LibraryDocument> = emptyList()
        var cachedSummaryMap = mutableMapOf<String, String>()
        var savedSummaryMap = mutableMapOf<String, String>()
        var cachedDocViewerMap = mutableMapOf<String, DocViewerTabsContent>()
        var savedDocViewerMap = mutableMapOf<String, DocViewerTabsContent>()
        var downloadedUrl: String? = null
        var deletedDocId: String? = null
        var deleteDocumentResult: Result<Unit> = Result.success(Unit)

        override suspend fun uploadUserDocument(title: String, subjectCode: String, docType: String, fileUri: String): Result<Unit> = Result.success(Unit)
        override suspend fun getDocuments(): Result<List<LibraryDocument>> = Result.success(documentsList)
        override suspend fun getDocumentsPage(pageSize: Int, cursor: String?): Result<DocumentPage> = Result.success(DocumentPage(documentsList, null))
        override suspend fun reportDocument(docId: String): Result<Unit> = Result.success(Unit)

        override suspend fun getCachedDocViewerContent(docId: String): Result<DocViewerTabsContent?> =
            Result.success(cachedDocViewerMap[docId])

        override suspend fun saveDocViewerContent(docId: String, content: DocViewerTabsContent): Result<Unit> {
            savedDocViewerMap[docId] = content
            return Result.success(Unit)
        }

        override suspend fun getCachedSummary(docId: String): Result<String?> = Result.success(cachedSummaryMap[docId])

        override suspend fun saveCachedSummary(docId: String, summary: String): Result<Unit> {
            savedSummaryMap[docId] = summary
            return Result.success(Unit)
        }

        override suspend fun downloadDocumentPdf(downloadUrl: String, destinationFile: File): Result<File> {
            downloadedUrl = downloadUrl
            return Result.success(destinationFile)
        }

        override suspend fun deleteDocument(docId: String): Result<Unit> {
            deletedDocId = docId
            return deleteDocumentResult
        }
    }

    private class FakeCategoryRepository : CategoryRepository {
        override suspend fun getCategories(): Result<List<DocumentCategory>> = Result.success(emptyList())
        override suspend fun addCategory(name: String, emoji: String): Result<Unit> = Result.success(Unit)
        override suspend fun updateCategory(id: String, name: String, emoji: String): Result<Unit> = Result.success(Unit)
        override suspend fun deleteCategory(id: String): Result<Unit> = Result.success(Unit)
    }

    private class FakeAiRepository : AiRepository {
        var generatedSummaryResult: Result<String> = Result.success("Bản tóm tắt AI từ mock")
        var generatedDocViewerResult: Result<DocViewerTabsContent> = Result.success(DocViewerTabsContent("Chi tiết", "Kiến thức", "Bài tập"))
        var aiCallCount = 0
        var docViewerAiCallCount = 0

        override suspend fun generateQuizFromDocument(title: String, type: String, content: String, count: Int, format: String): Result<List<QuizQuestion>> = Result.success(emptyList())

        override suspend fun generateSummaryFromDocument(title: String, type: String, content: String): Result<String> {
            aiCallCount++
            return generatedSummaryResult
        }

        override suspend fun generateDocViewerContent(title: String, courseCode: String, type: String): Result<DocViewerTabsContent> {
            docViewerAiCallCount++
            return generatedDocViewerResult
        }

        override suspend fun askAiAboutDocument(title: String, type: String, content: String, question: String): Result<String> = Result.success("")
        override suspend fun askGeneralStudyAi(question: String, history: List<AiChatMessage>): Result<String> = Result.success("")
    }

    private class FakeNotificationRepository : NotificationRepository {
        override suspend fun getNotifications(userId: String): Result<List<EduNotification>> = Result.success(emptyList())
        override suspend fun ensureDefaultNotifications(userId: String): Result<Unit> = Result.success(Unit)
        override suspend fun addNotification(userId: String, title: String, content: String, type: NotificationType): Result<Unit> = Result.success(Unit)
        override suspend fun markAsRead(id: String): Result<Unit> = Result.success(Unit)
    }

    private class FakeFlashcardRepository : FlashcardRepository {
        var userFlashcardCountResult: Result<Int> = Result.success(5)
        override suspend fun getFlashcardSets(): Result<List<FlashcardSet>> = Result.success(emptyList())
        override suspend fun getUserFlashcardCount(userId: String): Result<Int> = userFlashcardCountResult
    }

    private fun sampleDocument(id: String, title: String, code: String, type: LibraryDocumentType = LibraryDocumentType.NOTE) = LibraryDocument(
        id = id,
        courseCode = code,
        title = title,
        type = type,
        quantityLabel = "10 trang",
        rating = 4.9f,
        views = "3.2k",
        fileName = "test.pdf",
        downloadUrl = "https://example.com/test.pdf",
        authorId = "user_1",
        fileSizeBytes = 2048L,
        bgIndex = 0
    )

    private fun createViewModel(
        fakeAuth: FakeAuthRepository = FakeAuthRepository(),
        fakeDocRepo: FakeDocumentRepository = FakeDocumentRepository(),
        fakeCatRepo: FakeCategoryRepository = FakeCategoryRepository(),
        fakeAiRepo: FakeAiRepository = FakeAiRepository(),
        fakeNotifRepo: FakeNotificationRepository = FakeNotificationRepository(),
        fakeFlashcardRepo: FakeFlashcardRepository = FakeFlashcardRepository()
    ): LibraryViewModel {
        return LibraryViewModel(
            authRepository = fakeAuth,
            documentRepository = fakeDocRepo,
            categoryRepository = fakeCatRepo,
            aiRepository = fakeAiRepo,
            notificationRepository = fakeNotifRepo,
            flashcardRepository = fakeFlashcardRepo
        )
    }

    @Test
    fun `initial load fetches documents and calculates flashcard count via repository`() = runTest {
        val fakeAuth = FakeAuthRepository().apply {
            getCurrentUserResult = Result.success(User(uid = "user_1", fullName = "Test User", email = "test@edu.vn", savedDocuments = listOf("doc_1")))
        }

        val fakeDocRepo = FakeDocumentRepository().apply {
            documentsList = listOf(sampleDocument("doc_1", "Kinh tế vi mô", "EC101"))
        }

        val fakeFlashcardRepo = FakeFlashcardRepository().apply {
            userFlashcardCountResult = Result.success(8)
        }

        val viewModel = createViewModel(fakeAuth = fakeAuth, fakeDocRepo = fakeDocRepo, fakeFlashcardRepo = fakeFlashcardRepo)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(1, state.documents.size)
        assertEquals("doc_1", state.documents[0].id)
        assertTrue(state.documents[0].isSaved)
        assertEquals(8, state.docTypeCounts[DocTypeFilter.FLASHCARD])
    }

    @Test
    fun `generateDocumentSummary uses cached summary when available without invoking AI`() = runTest {
        val fakeDocRepo = FakeDocumentRepository().apply {
            cachedSummaryMap["doc_123"] = "Bản tóm tắt đã lưu trong cache"
        }
        val fakeAiRepo = FakeAiRepository()

        val viewModel = createViewModel(fakeDocRepo = fakeDocRepo, fakeAiRepo = fakeAiRepo)
        advanceUntilIdle()

        var resultSummary: String? = null
        viewModel.generateDocumentSummary("doc_123", "Tiêu đề tài liệu", "Ghi chú", "") { res ->
            resultSummary = res.getOrNull()
        }
        advanceUntilIdle()

        assertEquals("Bản tóm tắt đã lưu trong cache", resultSummary)
        assertEquals(0, fakeAiRepo.aiCallCount)
    }

    @Test
    fun `generateDocumentSummary invokes AI and caches result when cache is empty`() = runTest {
        val fakeDocRepo = FakeDocumentRepository()
        val fakeAiRepo = FakeAiRepository().apply {
            generatedSummaryResult = Result.success("Tóm tắt mới từ AI")
        }

        val viewModel = createViewModel(fakeDocRepo = fakeDocRepo, fakeAiRepo = fakeAiRepo)
        advanceUntilIdle()

        var resultSummary: String? = null
        viewModel.generateDocumentSummary("doc_999", "Đề cương Toán cao cấp", "Tóm tắt", "") { res ->
            resultSummary = res.getOrNull()
        }
        advanceUntilIdle()

        assertEquals("Tóm tắt mới từ AI", resultSummary)
        assertEquals(1, fakeAiRepo.aiCallCount)
        assertEquals("Tóm tắt mới từ AI", fakeDocRepo.savedSummaryMap["doc_999"])
    }

    @Test
    fun `downloadDocumentPdf delegates download cleanly to DocumentRepository`() = runTest {
        val fakeDocRepo = FakeDocumentRepository()
        val viewModel = createViewModel(fakeDocRepo = fakeDocRepo)

        val targetFile = File("test.pdf")
        val result = viewModel.downloadDocumentPdf("https://firebase.storage/sample.pdf", targetFile)

        assertTrue(result.isSuccess)
        assertEquals("https://firebase.storage/sample.pdf", fakeDocRepo.downloadedUrl)
    }

    @Test
    fun `deleteDocument removes document from state and notifies onSuccess`() = runTest {
        val fakeDocRepo = FakeDocumentRepository().apply {
            documentsList = listOf(
                sampleDocument("doc_to_delete", "Tài liệu mẫu", "CS101"),
                sampleDocument("doc_stay", "Tài liệu giữ lại", "CS102")
            )
        }
        val viewModel = createViewModel(fakeDocRepo = fakeDocRepo)
        advanceUntilIdle()

        assertEquals(2, viewModel.uiState.value.documents.size)

        var successCalled = false
        var failureMessage: String? = null

        viewModel.deleteDocument(
            docId = "doc_to_delete",
            onSuccess = { successCalled = true },
            onFailure = { failureMessage = it }
        )
        advanceUntilIdle()

        assertTrue(successCalled)
        assertNull(failureMessage)
        assertEquals("doc_to_delete", fakeDocRepo.deletedDocId)
        assertEquals(1, viewModel.uiState.value.documents.size)
        assertEquals("doc_stay", viewModel.uiState.value.documents[0].id)
    }

    @Test
    fun `deleteDocument failure calls onFailure callback`() = runTest {
        val fakeDocRepo = FakeDocumentRepository().apply {
            documentsList = listOf(sampleDocument("doc_fail", "Tài liệu", "CS101"))
            deleteDocumentResult = Result.failure(Exception("Bạn không có quyền xóa tài liệu này"))
        }
        val viewModel = createViewModel(fakeDocRepo = fakeDocRepo)
        advanceUntilIdle()

        var successCalled = false
        var failureMessage: String? = null

        viewModel.deleteDocument(
            docId = "doc_fail",
            onSuccess = { successCalled = true },
            onFailure = { failureMessage = it }
        )
        advanceUntilIdle()

        assertFalse(successCalled)
        assertEquals("Bạn không có quyền xóa tài liệu này", failureMessage)
        assertEquals(1, viewModel.uiState.value.documents.size)
    }

    @Test
    fun `onToggleSaved updates saved status optimistically and syncs with repository`() = runTest {
        val fakeAuth = FakeAuthRepository().apply {
            getCurrentUserResult = Result.success(User(uid = "user_1", fullName = "Student", email = "student@test.com", savedDocuments = emptyList()))
            toggleSaveDocumentResult = Result.success(User(uid = "user_1", fullName = "Student", email = "student@test.com", savedDocuments = listOf("doc_1")))
        }
        val fakeDocRepo = FakeDocumentRepository().apply {
            documentsList = listOf(sampleDocument("doc_1", "Kinh tế vi mô", "EC101"))
        }

        val viewModel = createViewModel(fakeAuth = fakeAuth, fakeDocRepo = fakeDocRepo)
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.documents[0].isSaved)

        viewModel.onToggleSaved("doc_1")
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.documents[0].isSaved)
        assertTrue(viewModel.uiState.value.savedDocuments.contains("doc_1"))
    }

    @Test
    fun `onToggleSaved rolls back saved status when repository fails`() = runTest {
        val fakeAuth = FakeAuthRepository().apply {
            getCurrentUserResult = Result.success(User(uid = "user_1", fullName = "Student", email = "student@test.com", savedDocuments = emptyList()))
            toggleSaveDocumentResult = Result.failure(Exception("Lỗi kết nối"))
        }
        val fakeDocRepo = FakeDocumentRepository().apply {
            documentsList = listOf(sampleDocument("doc_1", "Kinh tế vi mô", "EC101"))
        }

        val viewModel = createViewModel(fakeAuth = fakeAuth, fakeDocRepo = fakeDocRepo)
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.documents[0].isSaved)

        viewModel.onToggleSaved("doc_1")
        advanceUntilIdle()

        // Rolled back
        assertFalse(viewModel.uiState.value.documents[0].isSaved)
    }

    @Test
    fun `loadDocViewerContent returns cached content without calling AI`() = runTest {
        val cachedContent = DocViewerTabsContent("Chi tiết từ cache", "Trọng tâm", "Bài tập")
        val fakeDocRepo = FakeDocumentRepository().apply {
            cachedDocViewerMap["doc_viewer_1"] = cachedContent
        }
        val fakeAiRepo = FakeAiRepository()

        val viewModel = createViewModel(fakeDocRepo = fakeDocRepo, fakeAiRepo = fakeAiRepo)
        advanceUntilIdle()

        viewModel.loadDocViewerContent("doc_viewer_1", "Tiêu đề", "CS101", "Ghi chú")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoadingDocContent)
        assertEquals("Chi tiết từ cache", state.activeDocContent?.content)
        assertEquals(0, fakeAiRepo.docViewerAiCallCount)
    }

    @Test
    fun `loadDocViewerContent calls AI and saves to cache when no cache exists`() = runTest {
        val fakeDocRepo = FakeDocumentRepository()
        val fakeAiRepo = FakeAiRepository()

        val viewModel = createViewModel(fakeDocRepo = fakeDocRepo, fakeAiRepo = fakeAiRepo)
        advanceUntilIdle()

        viewModel.loadDocViewerContent("doc_viewer_2", "Đề thi thử", "MATH101", "Quiz")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoadingDocContent)
        assertEquals("Chi tiết", state.activeDocContent?.content)
        assertEquals(1, fakeAiRepo.docViewerAiCallCount)
        assertEquals("Chi tiết", fakeDocRepo.savedDocViewerMap["doc_viewer_2"]?.content)

        // Clear doc content
        viewModel.clearDocViewerContent()
        assertNull(viewModel.uiState.value.activeDocContent)
    }

    @Test
    fun `onToggleViewMode toggles grid and list views`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.isGridView)

        viewModel.onToggleViewMode()
        assertFalse(viewModel.uiState.value.isGridView)

        viewModel.onToggleViewMode()
        assertTrue(viewModel.uiState.value.isGridView)
    }
}
