package com.example.eduvault.feature.quiz.ui

import com.example.eduvault.MainDispatcherRule
import com.example.eduvault.data.repository.FakeAuthRepository
import com.example.eduvault.domain.model.AiChatMessage
import com.example.eduvault.domain.model.DocViewerTabsContent
import com.example.eduvault.domain.model.DocumentPage
import com.example.eduvault.domain.model.EduNotification
import com.example.eduvault.domain.model.LibraryDocument
import com.example.eduvault.domain.model.NotificationType
import com.example.eduvault.domain.model.QuizAttemptResult
import com.example.eduvault.domain.model.User
import com.example.eduvault.domain.repository.AiRepository
import com.example.eduvault.domain.repository.DocumentRepository
import com.example.eduvault.domain.repository.NotificationRepository
import com.example.eduvault.domain.usecase.GetPublishedDocumentsUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
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
class QuizViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private class FakeDocumentRepository : DocumentRepository {
        override suspend fun uploadUserDocument(title: String, subjectCode: String, docType: String, fileUri: String): Result<Unit> = Result.success(Unit)
        override suspend fun getDocuments(): Result<List<LibraryDocument>> = Result.success(emptyList())
        override suspend fun getDocumentsPage(pageSize: Int, cursor: String?): Result<DocumentPage> = Result.success(DocumentPage(emptyList(), null))
        override suspend fun reportDocument(docId: String): Result<Unit> = Result.success(Unit)
        override suspend fun getCachedDocViewerContent(docId: String): Result<DocViewerTabsContent?> = Result.success(null)
        override suspend fun saveDocViewerContent(docId: String, content: DocViewerTabsContent): Result<Unit> = Result.success(Unit)
        override suspend fun getCachedSummary(docId: String): Result<String?> = Result.success(null)
        override suspend fun saveCachedSummary(docId: String, summary: String): Result<Unit> = Result.success(Unit)
        override suspend fun downloadDocumentPdf(downloadUrl: String, destinationFile: File): Result<File> = Result.success(destinationFile)
        override suspend fun deleteDocument(docId: String): Result<Unit> = Result.success(Unit)
    }

    private class FakeAiRepository : AiRepository {
        var generatedQuestionsResult: Result<List<QuizQuestion>> = Result.success(emptyList())
        var generateQuizCallCount = 0

        override suspend fun generateQuizFromDocument(title: String, type: String, content: String, count: Int, format: String): Result<List<QuizQuestion>> {
            generateQuizCallCount++
            return generatedQuestionsResult
        }

        override suspend fun generateSummaryFromDocument(title: String, type: String, content: String): Result<String> = Result.success("")
        override suspend fun generateDocViewerContent(title: String, courseCode: String, type: String): Result<DocViewerTabsContent> = Result.success(DocViewerTabsContent("", "", ""))
        override suspend fun askAiAboutDocument(title: String, type: String, content: String, question: String): Result<String> = Result.success("")
        override suspend fun askGeneralStudyAi(question: String, history: List<AiChatMessage>): Result<String> = Result.success("")
    }

    private class FakeNotificationRepository : NotificationRepository {
        val notificationsList = mutableListOf<EduNotification>()

        override suspend fun getNotifications(userId: String): Result<List<EduNotification>> = Result.success(notificationsList)
        override suspend fun ensureDefaultNotifications(userId: String): Result<Unit> = Result.success(Unit)
        override suspend fun addNotification(userId: String, title: String, content: String, type: NotificationType): Result<Unit> {
            notificationsList.add(EduNotification(id = "notif_${notificationsList.size}", title = title, content = content, type = type))
            return Result.success(Unit)
        }
        override suspend fun markAsRead(id: String): Result<Unit> = Result.success(Unit)
    }

    private fun createViewModel(
        fakeAuth: FakeAuthRepository = FakeAuthRepository(),
        fakeDoc: FakeDocumentRepository = FakeDocumentRepository(),
        fakeAi: FakeAiRepository = FakeAiRepository(),
        fakeNotif: FakeNotificationRepository = FakeNotificationRepository()
    ): QuizViewModel {
        return QuizViewModel(
            authRepository = fakeAuth,
            getPublishedDocuments = GetPublishedDocumentsUseCase(fakeDoc),
            documentRepository = fakeDoc,
            aiRepository = fakeAi,
            notificationRepository = fakeNotif
        )
    }

    private fun sampleQuestion(id: String = "q_1", correct: String = "A") = QuizQuestion(
        id = id,
        text = "Câu hỏi $id?",
        options = listOf(
            QuizAnswer("A", "Đáp án A"),
            QuizAnswer("B", "Đáp án B"),
            QuizAnswer("C", "Đáp án C"),
            QuizAnswer("D", "Đáp án D")
        ),
        correctOption = correct,
        explanation = "Giải thích cho câu hỏi $id"
    )

    private fun sampleQuizSet(id: String = "set_1", subject: String = "Kinh tế học", count: Int = 2) = QuizSet(
        id = id,
        subject = subject,
        title = "Bộ đề $subject",
        difficulty = Difficulty.MEDIUM,
        questionCount = count,
        playCount = "10"
    )

    @Test fun `empty question response stays in task center with retry message`() = runTest {
        val viewModel = createViewModel(fakeAuth = FakeAuthRepository().apply {
            getQuizQuestionsResult = Result.success(emptyList())
        })
        viewModel.startQuiz(sampleQuizSet("unavailable", "Bảo mật", 5))
        advanceUntilIdle()
        assertEquals(QuizScreenMode.DASHBOARD, viewModel.uiState.value.screenMode)
        assertFalse(viewModel.uiState.value.isLoading)
        assertEquals("Chưa tải được câu hỏi. Vui lòng thử lại.", viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `init loads quiz sets and user stats`() = runTest {
        val fakeAuth = FakeAuthRepository().apply {
            getCurrentUserResult = Result.success(
                User(uid = "me", fullName = "Minh", email = "minh@test.com", quizCount = 5, quizAverageScore = 8.5f, totalXp = 150)
            )
            getQuizSetsResult = Result.success(
                listOf(sampleQuizSet("s1", "Marketing"), sampleQuizSet("s2", "Kinh tế"))
            )
            getAllUsersResult = Result.success(
                listOf(
                    User(uid = "u1", fullName = "Lan", email = "lan@test.com", totalXp = 200),
                    User(uid = "me", fullName = "Minh", email = "minh@test.com", totalXp = 150),
                    User(uid = "u2", fullName = "Nam", email = "nam@test.com", totalXp = 50)
                )
            )
        }

        val viewModel = createViewModel(fakeAuth = fakeAuth)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(2, state.quizSets.size)

        // User stats
        assertEquals(5, state.progress.completedCount)
        assertEquals(8.5f, state.progress.avgScore)
        assertEquals(85, state.progress.completionRate)
    }

    @Test
    fun `onSubjectSelected filters quiz sets or shows all`() = runTest {
        val fakeAuth = FakeAuthRepository().apply {
            getQuizSetsResult = Result.success(
                listOf(
                    sampleQuizSet("s1", "Kinh tế"),
                    sampleQuizSet("s2", "Marketing"),
                    sampleQuizSet("s3", "Kinh tế quốc tế")
                )
            )
        }
        val viewModel = createViewModel(fakeAuth = fakeAuth)
        advanceUntilIdle()

        assertEquals(3, viewModel.uiState.value.quizSets.size)

        // Filter by "Kinh tế"
        viewModel.onSubjectSelected("Kinh tế")
        val filtered = viewModel.uiState.value.quizSets
        assertEquals(2, filtered.size)
        assertTrue(filtered.all { it.subject.contains("Kinh tế") })

        // Filter by "Tất cả"
        viewModel.onSubjectSelected("Tất cả")
        assertEquals(3, viewModel.uiState.value.quizSets.size)
    }

    @Test
    fun `completing a quiz records attempt through recordQuizAttempt`() = runTest {
        val fakeAuth = FakeAuthRepository().apply {
            getCurrentUserResult = Result.success(User("user_123", "Student", "student@test.com"))
            recordQuizAttemptResult = Result.success(
                QuizAttemptResult(quizId = "quiz_abc", correctCount = 1, questionCount = 1, score = 100.0, xpEarned = 20)
            )
            getQuizQuestionsResult = Result.success(listOf(sampleQuestion("q_1", "A")))
        }

        var recordedQuizId: String? = null
        var recordedAnswers: List<Int>? = null
        fakeAuth.onRecordQuizAttempt = { quizId, _, answers ->
            recordedQuizId = quizId
            recordedAnswers = answers
        }

        val viewModel = createViewModel(fakeAuth = fakeAuth)
        val testSet = sampleQuizSet("quiz_abc", "Kinh tế", 1)

        viewModel.startQuiz(testSet)
        runCurrent()

        assertEquals(QuizScreenMode.PLAYER, viewModel.uiState.value.screenMode)
        assertEquals(1, viewModel.uiState.value.questions.size)

        viewModel.selectOption("A")
        runCurrent()

        viewModel.nextQuestion()
        runCurrent()
        advanceUntilIdle()

        assertEquals(QuizScreenMode.RESULTS, viewModel.uiState.value.screenMode)
        assertEquals("quiz_abc", recordedQuizId)
        assertEquals(listOf(0), recordedAnswers)
        assertEquals(20, viewModel.uiState.value.xpEarned)
        assertEquals("Đã lưu kết quả tự kiểm tra.", viewModel.uiState.value.attemptSaveMessage)
    }

    @Test fun `failed attempt save remains visible without reporting persistence`() = runTest {
        val fakeAuth = FakeAuthRepository().apply {
            getQuizQuestionsResult = Result.success(listOf(sampleQuestion()))
            recordQuizAttemptResult = Result.failure(Exception("Functions unavailable"))
        }
        val vm = createViewModel(fakeAuth = fakeAuth)
        vm.startQuiz(sampleQuizSet(count = 1))
        runCurrent()
        vm.selectOption("A")
        vm.nextQuestion()
        advanceUntilIdle()
        assertEquals(QuizScreenMode.RESULTS, vm.uiState.value.screenMode)
        assertEquals(1, vm.uiState.value.correctAnswersCount)
        assertTrue(vm.uiState.value.attemptSaveMessage!!.contains("chưa được lưu"))
        assertFalse(vm.uiState.value.attemptSaveMessage!!.contains("Functions"))
        vm.exitQuiz()
        assertNull(vm.uiState.value.attemptSaveMessage)
    }

    @Test
    fun `selectOption with incorrect answer does not award XP`() = runTest {
        val fakeAuth = FakeAuthRepository().apply {
            getQuizQuestionsResult = Result.success(listOf(sampleQuestion("q_1", "A")))
        }
        val viewModel = createViewModel(fakeAuth = fakeAuth)
        val testSet = sampleQuizSet("set_1", "Test", 1)

        viewModel.startQuiz(testSet)
        runCurrent()

        // Select "B" while correct is "A"
        viewModel.selectOption("B")
        runCurrent()

        val state = viewModel.uiState.value
        assertEquals("B", state.selectedOption)
        assertTrue(state.isAnswerLocked)
        assertEquals(0, state.correctAnswersCount)
        assertEquals(0, state.xpEarned)
    }

    @Test
    fun `skipQuestion marks current question as incorrect and locks answer`() = runTest {
        val fakeAuth = FakeAuthRepository().apply {
            getQuizQuestionsResult = Result.success(listOf(sampleQuestion("q_1", "A"), sampleQuestion("q_2", "B")))
        }
        val viewModel = createViewModel(fakeAuth = fakeAuth)
        viewModel.startQuiz(sampleQuizSet("set_1", "Test", 2))
        runCurrent()

        viewModel.skipQuestion()
        runCurrent()

        val state = viewModel.uiState.value
        assertEquals("", state.selectedOption)
        assertTrue(state.isAnswerLocked)
        assertEquals("", state.answersMap[0])
    }

    @Test
    fun `startQuiz cycles questions when count exceeds fetched question pool`() = runTest {
        val fakeAuth = FakeAuthRepository().apply {
            // Pool only has 1 question
            getQuizQuestionsResult = Result.success(listOf(sampleQuestion("q_single", "C")))
        }
        val viewModel = createViewModel(fakeAuth = fakeAuth)
        // Request 3 questions
        viewModel.startQuiz(sampleQuizSet("set_cycle", "Test", 3))
        runCurrent()

        val state = viewModel.uiState.value
        assertEquals(3, state.questions.size)
        assertEquals("q_single_0", state.questions[0].id)
        assertEquals("q_single_1", state.questions[1].id)
        assertEquals("q_single_2", state.questions[2].id)
    }

    @Test
    fun `exitQuiz resets player state and returns to DASHBOARD`() = runTest {
        val fakeAuth = FakeAuthRepository().apply {
            getQuizQuestionsResult = Result.success(listOf(sampleQuestion("q_1", "A")))
        }
        val viewModel = createViewModel(fakeAuth = fakeAuth)
        viewModel.startQuiz(sampleQuizSet("set_1", "Test", 1))
        runCurrent()

        assertEquals(QuizScreenMode.PLAYER, viewModel.uiState.value.screenMode)

        viewModel.exitQuiz()
        runCurrent()

        val state = viewModel.uiState.value
        assertEquals(QuizScreenMode.DASHBOARD, state.screenMode)
        assertNull(state.activeQuizSet)
        assertTrue(state.questions.isEmpty())
        assertEquals(0, state.currentQuestionIndex)
    }

    @Test
    fun `retryQuiz restarts active quiz from beginning`() = runTest {
        val fakeAuth = FakeAuthRepository().apply {
            getQuizQuestionsResult = Result.success(listOf(sampleQuestion("q_1", "A"), sampleQuestion("q_2", "B")))
        }
        val viewModel = createViewModel(fakeAuth = fakeAuth)
        val set = sampleQuizSet("set_retry", "Test", 2)
        viewModel.startQuiz(set)
        runCurrent()

        // Answer first question
        viewModel.selectOption("A")
        runCurrent()
        viewModel.nextQuestion()
        runCurrent()
        assertEquals(1, viewModel.uiState.value.currentQuestionIndex)

        // Retry quiz
        viewModel.retryQuiz()
        runCurrent()

        val state = viewModel.uiState.value
        assertEquals(0, state.currentQuestionIndex)
        assertNull(state.selectedOption)
        assertFalse(state.isAnswerLocked)
        assertEquals(0, state.correctAnswersCount)
    }

    @Test
    fun `generateAndStartQuiz generates questions via AI and launches quiz`() = runTest {
        val generated = listOf(sampleQuestion("ai_q1", "B"), sampleQuestion("ai_q2", "D"))
        val fakeAi = FakeAiRepository().apply {
            generatedQuestionsResult = Result.success(generated)
        }
        val fakeNotif = FakeNotificationRepository()
        val fakeAuth = FakeAuthRepository().apply {
            getCurrentUserResult = Result.success(User("u1", "Student", "s@test.com"))
        }

        val viewModel = createViewModel(fakeAuth = fakeAuth, fakeAi = fakeAi, fakeNotif = fakeNotif)
        advanceUntilIdle()

        var errorMessage: String? = null
        viewModel.generateAndStartQuiz(
            docId = "doc_test",
            docTitle = "Kinh tế lượng",
            count = 2,
            difficulty = Difficulty.HARD,
            format = "Trắc nghiệm",
            docContent = "Nội dung bài học mô hình hồi quy đa biến",
            onError = { errorMessage = it }
        )
        advanceUntilIdle()

        assertNull(errorMessage)
        assertEquals(1, fakeAi.generateQuizCallCount)

        val state = viewModel.uiState.value
        assertEquals(QuizScreenMode.PLAYER, state.screenMode)
        assertNotNull(state.activeQuizSet)
        assertTrue(state.activeQuizSet?.id?.startsWith("ai_") == true)
        assertEquals(2, state.questions.size)

        // Check AI notification was dispatched
        assertTrue(fakeNotif.notificationsList.any { it.type == NotificationType.AI })
    }

    @Test
    fun `generateAndStartQuiz rate limit blocks immediate second call within 60s`() = runTest {
        val fakeAi = FakeAiRepository().apply {
            generatedQuestionsResult = Result.success(listOf(sampleQuestion("q1", "A")))
        }
        val viewModel = createViewModel(fakeAi = fakeAi)
        advanceUntilIdle()

        // First call
        viewModel.generateAndStartQuiz(
            docTitle = "Toán",
            count = 1,
            difficulty = Difficulty.EASY,
            docContent = "Nội dung",
            onError = {}
        )
        advanceUntilIdle()
        assertEquals(1, fakeAi.generateQuizCallCount)

        // Immediate second call
        var rateLimitError: String? = null
        viewModel.generateAndStartQuiz(
            docTitle = "Lý",
            count = 1,
            difficulty = Difficulty.EASY,
            docContent = "Nội dung",
            onError = { rateLimitError = it }
        )
        advanceUntilIdle()

        // AI should NOT be called again
        assertEquals(1, fakeAi.generateQuizCallCount)
        assertNotNull(rateLimitError)
        assertTrue(rateLimitError?.contains("Bạn thao tác quá nhanh") == true)
    }

    @Test fun `failed quiz load reports retryable error and retry restores quizzes`() = runTest {
        val auth = FakeAuthRepository().apply { getQuizSetsResult = Result.failure(Exception("offline")) }
        val viewModel = createViewModel(fakeAuth = auth)
        advanceUntilIdle()
        assertNotNull(viewModel.uiState.value.errorMessage)
        auth.getQuizSetsResult = Result.success(listOf(sampleQuizSet("security", "SEC101")))
        viewModel.loadQuizSets()
        advanceUntilIdle()
        assertNull(viewModel.uiState.value.errorMessage)
        assertEquals("security", viewModel.uiState.value.quizSets.single().id)
    }
}
