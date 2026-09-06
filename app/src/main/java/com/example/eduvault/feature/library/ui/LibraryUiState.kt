package com.example.eduvault.feature.library.ui

/**
 * Trạng thái UI cho màn hình Thư viện tài liệu (Documents).
 */
data class LibraryUiState(
    val searchQuery: String = "",
    val selectedSubject: String = "Tất cả",
    val selectedDocType: DocTypeFilter = DocTypeFilter.ALL,
    val selectedSort: SortType = SortType.NEWEST,
    val selectedSchool: String = "Tất cả trường",
    val currentPage: Int = 1,
    val totalPages: Int = 0,
    val totalCount: Int = 0,
    val isGridView: Boolean = true,
    val isLoading: Boolean = false,
    val documentCredits: Int = 1,
    val uploadCount: Int = 0,
    val unlockedDocuments: List<String> = emptyList(),
    val documents: List<LibraryDoc> = emptyList(),
    val savedDocuments: List<String> = emptyList(),
    val docTypeCounts: Map<DocTypeFilter, Int> = emptyMap(),
    
    // Dynamic Banner Stats
    val bannerTotalDocs: Int = 0,
    val bannerTotalSubjects: Int = 0,
    val bannerTotalSaved: Int = 0,
    
    // DocViewer Dynamic Content State
    val activeDocContent: com.example.eduvault.domain.model.DocViewerTabsContent? = null,
    val isLoadingDocContent: Boolean = false,
    val docContentError: String? = null,
    val currentUserId: String = "",
    val currentUserRole: String = "user"
)

enum class SortType(val label: String) {
    NEWEST("Mới nhất"),
    POPULAR("Phổ biến"),
    RATING("Đánh giá")
}

enum class DocTypeFilter(val label: String) {
    ALL("Tất cả"),
    NOTE("Ghi chú"),
    QUIZ("Quiz"),
    SLIDE("Slide"),
    SUMMARY("Tóm tắt"),
    FLASHCARD("Flashcard")
}

enum class LibraryDocType(val label: String) {
    NOTE("Ghi chú"),
    QUIZ("Quiz"),
    SLIDE("Slide"),
    SUMMARY("Tóm tắt")
}

data class LibraryDoc(
    val id: String,
    val courseCode: String,
    val title: String,
    val type: LibraryDocType,
    val quantityLabel: String, // e.g. "42 tr", "30 câu", "18 slides"
    val rating: Float,
    val views: String, // e.g. "2.1k", "700"
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
