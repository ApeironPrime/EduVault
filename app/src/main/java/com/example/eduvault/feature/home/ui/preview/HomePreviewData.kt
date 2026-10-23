package com.example.eduvault.feature.home.ui.preview

import android.annotation.SuppressLint
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.example.eduvault.core.theme.EduVaultTheme
import com.example.eduvault.domain.model.AiChatMessage
import com.example.eduvault.domain.model.DocViewerTabsContent
import com.example.eduvault.domain.model.DocumentPage
import com.example.eduvault.domain.model.EduNotification
import com.example.eduvault.domain.model.NotificationType
import com.example.eduvault.domain.model.LibraryDocument
import com.example.eduvault.domain.model.QuizQuestion
import com.example.eduvault.domain.model.QuizSet
import com.example.eduvault.domain.model.QuizAttemptResult
import com.example.eduvault.domain.model.User
import com.example.eduvault.domain.repository.AiRepository
import com.example.eduvault.domain.repository.AuthRepository
import com.example.eduvault.domain.repository.DocumentRepository
import com.example.eduvault.domain.repository.NotificationRepository
import com.example.eduvault.domain.usecase.GetPublishedDocumentsUseCase
import com.example.eduvault.feature.home.ui.HomeScaffold
import com.example.eduvault.feature.home.ui.HomeUiState
import com.example.eduvault.feature.quiz.ui.QuizViewModel
import java.io.File

class PreviewAuthRepository : AuthRepository {
    override suspend fun login(email: String, password: String): Result<User> = Result.failure(Exception())
    override suspend fun register(fullName: String, email: String, password: String): Result<User> = Result.failure(Exception())
    override suspend fun sendPasswordResetEmail(email: String): Result<Unit> = Result.failure(Exception())
    override suspend fun verifyPasswordResetCode(code: String): Result<String> = Result.success("user@example.com")
    override suspend fun confirmPasswordReset(oobCode: String, newPassword: String): Result<Unit> = Result.success(Unit)
    override suspend fun getCurrentUser(): Result<User?> = Result.success(null)
    override suspend fun updateProfile(fullName: String, university: String, avatarUrl: String): Result<User> = Result.success(User("preview_uid", fullName, "preview@example.com"))
    override suspend fun addCredits(amount: Int): Result<User> = Result.success(User("preview_uid", "Preview User", "preview@example.com"))
    override suspend fun consumeCredit(): Result<User> = Result.success(User("preview_uid", "Preview User", "preview@example.com"))
    override suspend fun unlockDocument(docId: String): Result<User> = Result.success(User("preview_uid", "Preview User", "preview@example.com"))
    override suspend fun getQuizSets(): Result<List<QuizSet>> = Result.success(emptyList())
    override suspend fun getQuizQuestions(quizId: String): Result<List<QuizQuestion>> = Result.success(emptyList())
    override suspend fun getAllUsers(): Result<List<User>> = Result.success(emptyList())
    override suspend fun updateUserQuizStats(score: Float, xpEarned: Int): Result<User> = Result.success(User("preview_uid", "Preview User", "preview@example.com"))
    override suspend fun recordQuizAttempt(quizId: String, attemptId: String, answers: List<Int>): Result<QuizAttemptResult> = Result.success(QuizAttemptResult(quizId, 5, 5, 100.0, 50))
    override suspend fun toggleSaveDocument(docId: String): Result<User> = Result.success(User("preview_uid", "Preview User", "preview@example.com"))
    override suspend fun loginWithGoogle(idToken: String): Result<User> = Result.success(User("preview_uid", "Preview User", "preview@example.com"))
    override suspend fun changePassword(currentPassword: String, newPassword: String): Result<Unit> = Result.success(Unit)
    override suspend fun saveQuizSet(quizSet: QuizSet, questions: List<QuizQuestion>): Result<Unit> = Result.success(Unit)
    override fun logout() {}
}

class PreviewDocumentRepository : DocumentRepository {
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

class PreviewAiRepository : AiRepository {
    override suspend fun generateQuizFromDocument(title: String, type: String, content: String, count: Int, format: String): Result<List<QuizQuestion>> = Result.success(emptyList())
    override suspend fun generateSummaryFromDocument(title: String, type: String, content: String): Result<String> = Result.success("")
    override suspend fun askAiAboutDocument(title: String, type: String, content: String, question: String): Result<String> = Result.success("")
    override suspend fun askGeneralStudyAi(question: String, history: List<AiChatMessage>): Result<String> = Result.success("")
    override suspend fun generateDocViewerContent(title: String, courseCode: String, type: String): Result<DocViewerTabsContent> =
        Result.success(DocViewerTabsContent("", "", ""))
}

class PreviewNotificationRepository : NotificationRepository {
    override suspend fun getNotifications(userId: String): Result<List<EduNotification>> = Result.success(emptyList())
    override suspend fun ensureDefaultNotifications(userId: String): Result<Unit> = Result.success(Unit)
    override suspend fun addNotification(userId: String, title: String, content: String, type: NotificationType): Result<Unit> = Result.success(Unit)
    override suspend fun markAsRead(id: String): Result<Unit> = Result.success(Unit)
}

@Preview(name = "HomeScreen — Phone", showBackground = true, widthDp = 390, heightDp = 844)
@SuppressLint("ViewModelConstructorInComposable")
@Composable
fun HomeScreenPreview() {
    EduVaultTheme(darkTheme = false) {
        HomeScaffold(
            uiState = HomeUiState(),
            recentDocs = emptyList(),
            activityItems = emptyList(),
            quizViewModel = QuizViewModel(
                PreviewAuthRepository(),
                GetPublishedDocumentsUseCase(PreviewDocumentRepository()),
                PreviewDocumentRepository(),
                PreviewAiRepository(),
                PreviewNotificationRepository()
            ),
            onTabSelected = {},
            onUploadClick = {},
            onOpenDoc = {},
            onOpenFlashcards = {},
            onOpenStudyAi = {},
            onOpenNotes = {},
            onNotificationClick = {},
            onStartQuizSetup = {},
        )
    }
}
