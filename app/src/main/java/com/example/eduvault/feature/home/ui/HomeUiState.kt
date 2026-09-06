package com.example.eduvault.feature.home.ui

import com.example.eduvault.domain.model.AuthState

/**
 * Trạng thái UI của màn hình Home.
 */
data class HomeUiState(
    val userName: String = "Bạn",
    val university: String = "Nền tảng chia sẻ tài liệu học thuật",
    val followCount: Int = 0,
    val uploadCount: Int = 0,
    val savedCount: Int = 0,
    val isLoading: Boolean = false,
    val selectedTab: HomeTab = HomeTab.HOME,
    /** Trạng thái xác thực — Guest hoặc Authenticated */
    val authState: AuthState = AuthState.Guest,
)

enum class HomeTab {
    HOME, DOCUMENTS, QUIZ, PROFILE
}

/** Data class cho tài liệu gần đây */
data class RecentDoc(
    val id: String,
    val title: String,
    val course: String,
    val type: DocType,
    val emoji: String,
    val views: Int,
    val saves: Int,
    val pages: Int,
    val bgIndex: Int = 0,
    val fileName: String = "",
    val downloadUrl: String = "",
    val authorId: String = "",
    val fileSizeBytes: Long = 0L,
    val quantityLabel: String = "",
    val rating: Float = 0f
)

enum class DocType(val label: String) {
    NOTE("Ghi chú"),
    QUIZ("Quiz"),
    SLIDE("Slide"),
    SUMMARY("Tóm tắt"),
}

/** Data class cho activity gần đây */
data class ActivityItem(
    val id: String,
    val title: String,
    val subtitle: String,
    val time: String,
    val type: ActivityType,
    val progress: Float? = null,
)

enum class ActivityType {
    NOTE, QUIZ, SLIDE
}

/** Data class cho tag */
data class TopicTag(val label: String, val count: Int)
