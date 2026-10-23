package com.example.eduvault.feature.quiz.ui

import com.example.eduvault.domain.model.Difficulty
import com.example.eduvault.domain.model.QuizAnswer
import com.example.eduvault.domain.model.QuizQuestion
import com.example.eduvault.domain.model.QuizSet

// Expose typealiases for backwards compatibility in UI layer
typealias Difficulty = com.example.eduvault.domain.model.Difficulty
typealias QuizSet = com.example.eduvault.domain.model.QuizSet
typealias QuizAnswer = com.example.eduvault.domain.model.QuizAnswer
typealias QuizQuestion = com.example.eduvault.domain.model.QuizQuestion

/**
 * Thống kê tiến độ luyện tập
 */
data class QuizProgress(
    val avgScore: Float, // e.g. 8.5
    val completedCount: Int, // e.g. 18
    val completionRate: Int // e.g. 92%
)


/**
 * Các chế độ màn hình trắc nghiệm
 */
enum class QuizScreenMode {
    DASHBOARD,
    PLAYER,
    RESULTS
}

/**
 * Trạng thái UI đầy đủ cho màn hình Quiz AI
 */
data class QuizUiState(
    val screenMode: QuizScreenMode = QuizScreenMode.DASHBOARD,
    val selectedSubject: String = "Tất cả",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val attemptSaveMessage: String? = null,

    // Dashboard state
    val quizSets: List<QuizSet> = emptyList(),
    val progress: QuizProgress = QuizProgress(0.0f, 0, 0),

    // Player state
    val activeQuizSet: QuizSet? = null,
    val questions: List<QuizQuestion> = emptyList(),
    val currentQuestionIndex: Int = 0,
    val selectedOption: String? = null, // Đáp án đã chọn hiện tại ("A", "B", "C", "D")
    val isAnswerLocked: Boolean = false, // Khóa đáp án sau khi người dùng đã chọn
    val remainingSeconds: Int = 60, // Đếm ngược thời gian cho câu hỏi hiện tại
    val answersMap: Map<Int, String?> = emptyMap(), // Ánh xạ từ chỉ mục câu hỏi -> đáp án đã chọn
    val correctAnswersCount: Int = 0,
    val timeSpentSeconds: Int = 0, // Tổng thời gian đã làm bài
    val xpEarned: Int = 0,
    val quizFormat: String = "Trắc nghiệm" // Hình thức câu hỏi: "Trắc nghiệm" | "Đúng / Sai" | "Điền khuyết"
)
