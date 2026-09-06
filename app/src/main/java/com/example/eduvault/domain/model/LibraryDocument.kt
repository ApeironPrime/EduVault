package com.example.eduvault.domain.model

/**
 * Business representation of a document. UI-specific screens may render this
 * model, but repositories must never depend on a feature package for it.
 */
data class LibraryDocument(
    val id: String,
    val courseCode: String,
    val title: String,
    val type: LibraryDocumentType,
    val quantityLabel: String,
    val rating: Float,
    val views: String,
    val isSaved: Boolean = false,
    val bgIndex: Int = 0,
    val aiVerified: Boolean = false,
    val aiCheckResult: String = "",
    val status: String = "APPROVED",
    val reportCount: Int = 0,
    val fileName: String = "",
    val downloadUrl: String = "",
    val authorId: String = "",
    val fileSizeBytes: Long = 0L
)

enum class LibraryDocumentType(val label: String) {
    NOTE("Ghi chú"),
    QUIZ("Quiz"),
    SLIDE("Slide"),
    SUMMARY("Tóm tắt")
}
