package com.example.eduvault.data.repository

import com.example.eduvault.BuildConfig
import com.example.eduvault.domain.model.AiChatMessage
import com.example.eduvault.domain.model.DocViewerTabsContent
import com.example.eduvault.domain.model.QuizAnswer
import com.example.eduvault.domain.model.QuizQuestion
import com.example.eduvault.domain.repository.AiRepository
import com.google.firebase.functions.FirebaseFunctions
import kotlinx.coroutines.tasks.await
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * AI client with no model SDK or credentials in the APK. Every request goes
 * through the authenticated, App Check-protected Cloud Functions gateway.
 */
@Singleton
class AiRepositoryImpl @Inject constructor(
    private val functions: FirebaseFunctions
) : AiRepository {
    override suspend fun generateQuizFromDocument(
        title: String,
        type: String,
        content: String,
        count: Int,
        format: String
    ): Result<List<QuizQuestion>> = runCatching {
        val response = call("QUIZ", mapOf(
            "title" to title,
            "type" to type,
            "content" to content,
            "count" to count,
            "format" to format
        ))
        parseQuiz(response, format)
    }

    override suspend fun generateSummaryFromDocument(
        title: String,
        type: String,
        content: String
    ): Result<String> = runCatching {
        call("SUMMARY", mapOf("title" to title, "type" to type, "content" to content))
    }

    override suspend fun generateDocViewerContent(
        title: String,
        courseCode: String,
        type: String
    ): Result<DocViewerTabsContent> = runCatching {
        val json = JSONObject(call("DOC_VIEWER", mapOf(
            "title" to title,
            "courseCode" to courseCode,
            "type" to type
        )))
        DocViewerTabsContent(
            content = json.requiredString("content", 8_000),
            keyPoints = json.requiredString("keyPoints", 6_000),
            practice = json.requiredString("practice", 6_000)
        )
    }

    override suspend fun askAiAboutDocument(
        title: String,
        type: String,
        content: String,
        question: String
    ): Result<String> = runCatching {
        call("DOCUMENT_QA", mapOf(
            "title" to title,
            "type" to type,
            "content" to content,
            "question" to question
        ))
    }

    override suspend fun askGeneralStudyAi(
        question: String,
        history: List<AiChatMessage>
    ): Result<String> = runCatching {
        call("STUDY_CHAT", mapOf(
            "question" to question,
            "history" to history.takeLast(6).map {
                mapOf("role" to if (it.isUser) "user" else "assistant", "text" to it.text)
            }
        ))
    }

    private suspend fun call(operation: String, payload: Map<String, Any>): String {
        check(!BuildConfig.SPARK_ONLY) { "AI trực tuyến chưa bật trong bản Spark 0 phí." }
        val result = functions.availableCallable("generateAiContent")
            .call(payload + ("operation" to operation))
            .await()
        val data = result.data as? Map<*, *> ?: error("Invalid AI gateway response.")
        val text = data["text"] as? String ?: error("AI gateway returned no text.")
        require(text.isNotBlank() && text.length <= 12_000) { "AI gateway returned invalid text." }
        return text
    }

    private fun parseQuiz(rawJson: String, format: String): List<QuizQuestion> {
        val array = JSONArray(rawJson)
        require(array.length() in 1..20) { "AI quiz response has an invalid question count." }
        val expectedOptionCount = if (format == "Đúng / Sai") 2 else 4
        return List(array.length()) { index ->
            val item = array.getJSONObject(index)
            val options = item.getJSONArray("options")
            require(options.length() == expectedOptionCount) { "AI question has an invalid option count." }
            val correctIndex = item.getInt("correctAnswer")
            require(correctIndex in 0 until expectedOptionCount) { "AI quiz response has an invalid answer." }
            QuizQuestion(
                id = UUID.randomUUID().toString(),
                text = item.requiredString("question", 1_500),
                options = List(expectedOptionCount) { optionIndex ->
                    QuizAnswer(('A'.code + optionIndex).toChar().toString(), options.getString(optionIndex).trim())
                },
                correctOption = ('A'.code + correctIndex).toChar().toString(),
                explanation = item.optString("explanation").trim()
            )
        }
    }

    private fun JSONObject.requiredString(name: String, maxLength: Int): String {
        val value = getString(name).trim()
        require(value.isNotEmpty() && value.length <= maxLength) { "AI response field $name is invalid." }
        return value
    }
}
