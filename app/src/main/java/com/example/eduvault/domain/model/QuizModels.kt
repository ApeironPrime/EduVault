package com.example.eduvault.domain.model

/**
 * Độ khó của bộ đề trắc nghiệm (Domain Entity).
 */
enum class Difficulty(val label: String) {
    EASY("Dễ"),
    MEDIUM("T.Bình"),
    HARD("Khó")
}

/**
 * Bộ đề trắc nghiệm (Domain Entity).
 */
data class QuizSet(
    val id: String,
    val subject: String,
    val title: String,
    val difficulty: Difficulty,
    val questionCount: Int,
    val playCount: String,
    val isNew: Boolean = false,
    val bgIndex: Int = 0,
    val trainingModuleId: String? = null
)

/**
 * Lựa chọn đáp án trắc nghiệm (Domain Entity).
 */
data class QuizAnswer(
    val key: String, // "A", "B", "C", "D"
    val text: String
)

/**
 * Câu hỏi trắc nghiệm (Domain Entity).
 */
data class QuizQuestion(
    val id: String,
    val text: String,
    val hint: String = "",
    val options: List<QuizAnswer>,
    val correctOption: String, // "A", "B", "C", "D"
    val explanation: String
)
