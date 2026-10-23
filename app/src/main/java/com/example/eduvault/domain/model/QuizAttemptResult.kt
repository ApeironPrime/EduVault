package com.example.eduvault.domain.model

/**
 * Kết quả nộp bài thi trắc nghiệm được tính toán và trả về từ backend Cloud Functions.
 */
data class QuizAttemptResult(
    val quizId: String,
    val correctCount: Int,
    val questionCount: Int,
    val score: Double,
    val xpEarned: Int
)
