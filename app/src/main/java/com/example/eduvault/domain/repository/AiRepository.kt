package com.example.eduvault.domain.repository

import com.example.eduvault.domain.model.AiChatMessage
import com.example.eduvault.domain.model.DocViewerTabsContent
import com.example.eduvault.domain.model.QuizQuestion

/**
 * Interface cho AiRepository - Quản lý tương tác với Google Gemini AI.
 */
interface AiRepository {

    /**
     * Tự động sinh đề thi trắc nghiệm học thuật dựa trên nội dung tài liệu.
     */
    suspend fun generateQuizFromDocument(
        title: String,
        type: String,
        content: String,
        count: Int,
        format: String = "Trắc nghiệm"
    ): Result<List<QuizQuestion>>

    /**
     * Tự động sinh bản tóm tắt nhanh Markdown dựa trên nội dung tài liệu.
     */
    suspend fun generateSummaryFromDocument(
        title: String,
        type: String,
        content: String
    ): Result<String>

    /**
     * Tự động sinh nội dung chi tiết, kiến thức trọng tâm và bài tập ôn luyện bám sát tài liệu.
     */
    suspend fun generateDocViewerContent(
        title: String,
        courseCode: String,
        type: String
    ): Result<DocViewerTabsContent>

    /**
     * Hỏi đáp trực quan với Trợ lý AI (Tutor) về tài liệu cụ thể.
     */
    suspend fun askAiAboutDocument(
        title: String,
        type: String,
        content: String,
        question: String
    ): Result<String>

    /**
     * Hỏi đáp chung với Trợ lý Học tập StudyAI về bất kỳ chủ đề học thuật nào.
     */
    suspend fun askGeneralStudyAi(
        question: String,
        history: List<AiChatMessage>
    ): Result<String>
}
