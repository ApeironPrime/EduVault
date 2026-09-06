package com.example.eduvault.data.repository

import com.example.eduvault.domain.model.User
import com.example.eduvault.domain.repository.AuthRepository
import com.example.eduvault.feature.quiz.ui.QuizQuestion
import com.example.eduvault.feature.quiz.ui.QuizSet

/**
 * Fake implementation of AuthRepository to be used in local JVM unit tests.
 * Allows easy configuration of success/failure results for all operations.
 */
class FakeAuthRepository : AuthRepository {
    // Variable to control simulated delay
    var delayMs: Long = 0

    // Variables to control results in tests
    var loginResult: Result<User> = Result.success(User(uid = "test-uid", fullName = "Test User", email = "test@example.com"))
    var registerResult: Result<User> = Result.success(User(uid = "test-uid", fullName = "Test User", email = "test@example.com"))
    var sendPasswordResetEmailResult: Result<Unit> = Result.success(Unit)
    var verifyPasswordResetCodeResult: Result<String> = Result.success("test@example.com")
    var confirmPasswordResetResult: Result<Unit> = Result.success(Unit)
    var getCurrentUserResult: Result<User?> = Result.success(null)
    var updateProfileResult: Result<User> = Result.success(User(uid = "test-uid", fullName = "Test User", email = "test@example.com"))
    var addCreditsResult: Result<User> = Result.success(User(uid = "test-uid", fullName = "Test User", email = "test@example.com"))
    var consumeCreditResult: Result<User> = Result.success(User(uid = "test-uid", fullName = "Test User", email = "test@example.com"))
    var unlockDocumentResult: Result<User> = Result.success(User(uid = "test-uid", fullName = "Test User", email = "test@example.com"))
    var getQuizSetsResult: Result<List<QuizSet>> = Result.success(emptyList())
    var getQuizQuestionsResult: Result<List<QuizQuestion>> = Result.success(emptyList())
    var getAllUsersResult: Result<List<User>> = Result.success(emptyList())
    var updateUserQuizStatsResult: Result<User> = Result.success(User(uid = "test-uid", fullName = "Test User", email = "test@example.com"))
    var toggleSaveDocumentResult: Result<User> = Result.success(User(uid = "test-uid", fullName = "Test User", email = "test@example.com"))
    var loginWithGoogleResult: Result<User> = Result.success(User(uid = "test-uid", fullName = "Test User", email = "test@example.com"))
    var changePasswordResult: Result<Unit> = Result.success(Unit)
    var saveQuizSetResult: Result<Unit> = Result.success(Unit)

    override suspend fun login(email: String, password: String): Result<User> = loginResult

    override suspend fun register(fullName: String, email: String, password: String): Result<User> = registerResult

    override suspend fun sendPasswordResetEmail(email: String): Result<Unit> {
        if (delayMs > 0) kotlinx.coroutines.delay(delayMs)
        return sendPasswordResetEmailResult
    }

    override suspend fun verifyPasswordResetCode(code: String): Result<String> {
        if (delayMs > 0) kotlinx.coroutines.delay(delayMs)
        return verifyPasswordResetCodeResult
    }

    override suspend fun confirmPasswordReset(oobCode: String, newPassword: String): Result<Unit> {
        if (delayMs > 0) kotlinx.coroutines.delay(delayMs)
        return confirmPasswordResetResult
    }

    override suspend fun getCurrentUser(): Result<User?> = getCurrentUserResult

    override suspend fun updateProfile(fullName: String, university: String, avatarUrl: String): Result<User> = updateProfileResult

    override suspend fun addCredits(amount: Int): Result<User> = addCreditsResult

    override suspend fun consumeCredit(): Result<User> = consumeCreditResult

    override suspend fun unlockDocument(docId: String): Result<User> = unlockDocumentResult

    override suspend fun getQuizSets(): Result<List<QuizSet>> = getQuizSetsResult

    override suspend fun getQuizQuestions(quizId: String): Result<List<QuizQuestion>> = getQuizQuestionsResult

    override suspend fun getAllUsers(): Result<List<User>> = getAllUsersResult

    override suspend fun updateUserQuizStats(score: Float, xpEarned: Int): Result<User> = updateUserQuizStatsResult

    override suspend fun toggleSaveDocument(docId: String): Result<User> = toggleSaveDocumentResult

    override suspend fun loginWithGoogle(idToken: String): Result<User> = loginWithGoogleResult

    override suspend fun changePassword(currentPassword: String, newPassword: String): Result<Unit> = changePasswordResult

    override suspend fun saveQuizSet(quizSet: QuizSet, questions: List<QuizQuestion>): Result<Unit> = saveQuizSetResult

    override fun logout() {
        // No-op
    }
}
