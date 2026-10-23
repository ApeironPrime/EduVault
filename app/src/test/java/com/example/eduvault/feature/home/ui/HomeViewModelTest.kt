package com.example.eduvault.feature.home.ui

import com.example.eduvault.MainDispatcherRule
import com.example.eduvault.data.repository.FakeAuthRepository
import com.example.eduvault.domain.model.AiChatMessage
import com.example.eduvault.domain.model.AuthState
import com.example.eduvault.domain.model.DocViewerTabsContent
import com.example.eduvault.domain.model.DocumentPage
import com.example.eduvault.domain.model.EduNotification
import com.example.eduvault.domain.model.LibraryDocument
import com.example.eduvault.domain.model.LibraryDocumentType
import com.example.eduvault.domain.model.NotificationType
import com.example.eduvault.domain.model.QuizQuestion
import com.example.eduvault.domain.model.User
import com.example.eduvault.domain.repository.AiRepository
import com.example.eduvault.domain.repository.DocumentRepository
import com.example.eduvault.domain.repository.NotificationRepository
import com.example.eduvault.domain.usecase.GetPublishedDocumentsUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.io.File

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    // ─── Fake Repositories ───────────────────────────────────────────────────

    private class FakeNotificationRepository : NotificationRepository {
        var notificationsResult: Result<List<EduNotification>> = Result.success(emptyList())
        var readCount = 0
        var ensureDefaultCalled = false

        override suspend fun getNotifications(userId: String): Result<List<EduNotification>> {
            readCount++
            return notificationsResult
        }

        override suspend fun ensureDefaultNotifications(userId: String): Result<Unit> {
            ensureDefaultCalled = true
            return Result.success(Unit)
        }

        override suspend fun addNotification(userId: String, title: String, content: String, type: NotificationType): Result<Unit> = Result.success(Unit)
        override suspend fun markAsRead(id: String): Result<Unit> = Result.success(Unit)
    }

    private class FakeDocumentRepository : DocumentRepository {
        var documentsResult: Result<List<LibraryDocument>> = Result.success(emptyList())
        var delayMs: Long = 0L

        override suspend fun getDocuments(): Result<List<LibraryDocument>> {
            if (delayMs > 0) delay(delayMs)
            return documentsResult
        }

        override suspend fun getDocumentsPage(pageSize: Int, cursor: String?): Result<DocumentPage> =
            Result.success(DocumentPage(documentsResult.getOrDefault(emptyList()), null))

        override suspend fun uploadUserDocument(title: String, subjectCode: String, docType: String, fileUri: String): Result<Unit> = Result.success(Unit)
        override suspend fun reportDocument(docId: String): Result<Unit> = Result.success(Unit)
        override suspend fun getCachedDocViewerContent(docId: String): Result<DocViewerTabsContent?> = Result.success(null)
        override suspend fun saveDocViewerContent(docId: String, content: DocViewerTabsContent): Result<Unit> = Result.success(Unit)
        override suspend fun getCachedSummary(docId: String): Result<String?> = Result.success(null)
        override suspend fun saveCachedSummary(docId: String, summary: String): Result<Unit> = Result.success(Unit)
        override suspend fun downloadDocumentPdf(downloadUrl: String, destinationFile: File): Result<File> = Result.success(destinationFile)
        override suspend fun deleteDocument(docId: String): Result<Unit> = Result.success(Unit)
    }

    private class FakeAiRepository : AiRepository {
        var askAiResult: Result<String> = Result.success("StudyAI response")
        var askAiCallCount = 0

        override suspend fun generateQuizFromDocument(title: String, type: String, content: String, count: Int, format: String): Result<List<QuizQuestion>> = Result.success(emptyList())
        override suspend fun generateSummaryFromDocument(title: String, type: String, content: String): Result<String> = Result.success("")
        override suspend fun generateDocViewerContent(title: String, courseCode: String, type: String): Result<DocViewerTabsContent> = Result.success(DocViewerTabsContent("", "", ""))
        override suspend fun askAiAboutDocument(title: String, type: String, content: String, question: String): Result<String> = Result.success("")

        override suspend fun askGeneralStudyAi(question: String, history: List<AiChatMessage>): Result<String> {
            askAiCallCount++
            return askAiResult
        }
    }

    // ─── Test Helpers ────────────────────────────────────────────────────────

    private fun createViewModel(
        fakeAuth: FakeAuthRepository = FakeAuthRepository(),
        fakeAi: FakeAiRepository = FakeAiRepository(),
        fakeNotif: FakeNotificationRepository = FakeNotificationRepository(),
        fakeDoc: FakeDocumentRepository = FakeDocumentRepository()
    ): HomeViewModel {
        val getPublishedDocsUseCase = GetPublishedDocumentsUseCase(fakeDoc)
        return HomeViewModel(
            authRepository = fakeAuth,
            aiRepository = fakeAi,
            notificationRepository = fakeNotif,
            getPublishedDocuments = getPublishedDocsUseCase
        )
    }

    private fun sampleDocument(id: String, code: String, title: String) = LibraryDocument(
        id = id,
        courseCode = code,
        title = title,
        type = LibraryDocumentType.NOTE,
        quantityLabel = "20 tr",
        rating = 4.8f,
        views = "1.5k",
        fileName = "test.pdf",
        downloadUrl = "https://example.com/test.pdf",
        authorId = "author_1",
        fileSizeBytes = 1024L,
        bgIndex = 1
    )

    // ─── Tests ───────────────────────────────────────────────────────────────

    @Test
    fun `init with authenticated user sets authenticated state and loads user data`() = runTest {
        val fakeAuth = FakeAuthRepository().apply {
            getCurrentUserResult = Result.success(
                User(uid = "u101", fullName = "Nguyen Van A", university = "UEH", email = "a@ueh.edu.vn")
            )
        }
        val fakeDoc = FakeDocumentRepository().apply {
            documentsResult = Result.success(
                listOf(sampleDocument("d1", "IT01", "Lập trình Android"))
            )
        }
        val fakeNotif = FakeNotificationRepository().apply {
            notificationsResult = Result.success(
                listOf(
                    EduNotification(
                        id = "n1",
                        title = "Thông báo mới",
                        content = "Nội dung",
                        timestamp = System.currentTimeMillis(),
                        userId = "u101",
                        type = NotificationType.SYSTEM
                    )
                )
            )
        }

        val viewModel = createViewModel(fakeAuth, FakeAiRepository(), fakeNotif, fakeDoc)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.authState is AuthState.Authenticated)
        assertEquals("Nguyen Van A", state.userName)
        assertEquals("UEH", state.university)
        assertTrue(fakeNotif.ensureDefaultCalled)
        assertEquals(1, viewModel.notifications.value.size)
        assertEquals(1, viewModel.recentDocs.value.size)
        assertEquals("Lập trình Android", viewModel.recentDocs.value[0].title)


    }

    @Test
    fun `init with unauthenticated user sets guest state and loads guest notifications`() = runTest {
        val fakeAuth = FakeAuthRepository().apply {
            getCurrentUserResult = Result.success(null)
        }
        val viewModel = createViewModel(fakeAuth = fakeAuth)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.authState is AuthState.Guest)
        assertEquals("Bạn", state.userName)
        assertEquals(2, viewModel.notifications.value.size)
        assertTrue(viewModel.notifications.value.all { it.userId == "guest_user" })
    }

    @Test
    fun `init with auth failure gracefully falls back to guest state`() = runTest {
        val fakeAuth = FakeAuthRepository().apply {
            getCurrentUserResult = Result.failure(RuntimeException("Network disconnected"))
        }
        val viewModel = createViewModel(fakeAuth = fakeAuth)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.authState is AuthState.Guest)
        assertEquals("Bạn", state.userName)
    }

    @Test
    fun `document refresh replaces earlier documents and failure clears stale data`() = runTest {
        val fakeDoc = FakeDocumentRepository().apply {
            documentsResult = Result.success(listOf(sampleDocument("git", "GIT101", "Git Flow")))
        }
        val viewModel = createViewModel(fakeDoc = fakeDoc)
        advanceUntilIdle()
        assertEquals("git", viewModel.recentDocs.value.single().id)
        fakeDoc.documentsResult = Result.failure(Exception("Unavailable"))
        viewModel.loadRecentDocuments()
        advanceUntilIdle()
        assertTrue(viewModel.recentDocs.value.isEmpty())
    }

    @Test
    fun `notifications and activity use one repository read without invented progress`() = runTest {
        val fakeAuth = FakeAuthRepository().apply {
            getCurrentUserResult = Result.success(User("learner", "Learner", "learner@example.com"))
        }
        val fakeNotif = FakeNotificationRepository().apply {
            notificationsResult = Result.success(listOf(EduNotification(
                id = "welcome", userId = "learner", title = "Chào mừng", content = "Bắt đầu lộ trình",
                timestamp = System.currentTimeMillis(), type = NotificationType.SYSTEM)))
        }
        val viewModel = createViewModel(fakeAuth = fakeAuth, fakeNotif = fakeNotif)
        advanceUntilIdle()
        assertEquals(1, fakeNotif.readCount)
        assertEquals(viewModel.notifications.value.single().id, viewModel.activityItems.value.single().id)
        assertEquals(null, viewModel.activityItems.value.single().progress)
    }

    @Test
    fun `sendChatMessage sends prompt to AI and appends response`() = runTest {
        val fakeAi = FakeAiRepository().apply {
            askAiResult = Result.success("Lạm phát là sự tăng mức giá chung liên tục.")
        }
        val viewModel = createViewModel(fakeAi = fakeAi)
        advanceUntilIdle()

        assertEquals(1, viewModel.chatMessages.value.size) // Default greeting

        viewModel.sendChatMessage("Giải thích lạm phát là gì?")
        advanceUntilIdle()

        assertEquals(1, fakeAi.askAiCallCount)
        assertFalse(viewModel.isAiTyping.value)

        val messages = viewModel.chatMessages.value
        assertEquals(3, messages.size) // Greeting + User question + AI answer
        assertEquals("Giải thích lạm phát là gì?", messages[1].text)
        assertTrue(messages[1].isUser)
        assertEquals("Lạm phát là sự tăng mức giá chung liên tục.", messages[2].text)
        assertFalse(messages[2].isUser)
    }

    @Test
    fun `sendChatMessage enforces 3-second rate limit between requests`() = runTest {
        val fakeAi = FakeAiRepository()
        val viewModel = createViewModel(fakeAi = fakeAi)
        advanceUntilIdle()

        // First message
        viewModel.sendChatMessage("Câu hỏi 1")
        advanceUntilIdle()
        assertEquals(1, fakeAi.askAiCallCount)

        // Immediate second message (within 3 seconds)
        viewModel.sendChatMessage("Câu hỏi 2 quá nhanh")
        advanceUntilIdle()

        // Should NOT invoke AI repository second time
        assertEquals(1, fakeAi.askAiCallCount)

        val messages = viewModel.chatMessages.value
        val rateLimitWarning = messages.last()
        assertTrue(rateLimitWarning.text.contains("Vui lòng đợi một chút"))
    }

    @Test
    fun `sendChatMessage ignores blank message`() = runTest {
        val fakeAi = FakeAiRepository()
        val viewModel = createViewModel(fakeAi = fakeAi)
        advanceUntilIdle()

        val initialCount = viewModel.chatMessages.value.size
        viewModel.sendChatMessage("   ")
        advanceUntilIdle()

        assertEquals(initialCount, viewModel.chatMessages.value.size)
        assertEquals(0, fakeAi.askAiCallCount)
    }

    @Test
    fun `onTabSelected updates selectedTab in uiState`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        assertEquals(HomeTab.HOME, viewModel.uiState.value.selectedTab)

        viewModel.onTabSelected(HomeTab.DOCUMENTS)
        assertEquals(HomeTab.DOCUMENTS, viewModel.uiState.value.selectedTab)

        viewModel.onTabSelected(HomeTab.QUIZ)
        assertEquals(HomeTab.QUIZ, viewModel.uiState.value.selectedTab)
    }

    @Test
    fun `empty live data does not invent documents or training achievements`() = runTest {
        val fakeAuth = FakeAuthRepository().apply {
            getCurrentUserResult = Result.success(User(uid = "learner", fullName = "Learner", email = "learner@example.com"))
        }
        val viewModel = createViewModel(fakeAuth = fakeAuth)
        advanceUntilIdle()
        assertTrue(viewModel.recentDocs.value.isEmpty())
        assertTrue(viewModel.activityItems.value.isEmpty())
    }
}
