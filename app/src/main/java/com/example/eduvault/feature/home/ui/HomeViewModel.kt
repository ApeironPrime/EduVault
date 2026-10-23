package com.example.eduvault.feature.home.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.eduvault.domain.model.AuthState
import com.example.eduvault.domain.model.AiChatMessage
import com.example.eduvault.domain.model.EduNotification
import com.example.eduvault.domain.model.NotificationType
import com.example.eduvault.domain.repository.AuthRepository
import com.example.eduvault.domain.repository.AiRepository
import com.example.eduvault.domain.repository.NotificationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

import com.example.eduvault.domain.usecase.GetPublishedDocumentsUseCase

/**
 * ViewModel cho màn hình Home.
 *
 * Quy tắc tuân thủ:
 * - Không có Context, Activity, Fragment, hoặc View nào trong class này.
 * - Trạng thái UI được quản lý 100% qua StateFlow.
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val aiRepository: AiRepository,
    private val notificationRepository: NotificationRepository,
    private val getPublishedDocuments: GetPublishedDocumentsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _recentDocsFlow = MutableStateFlow<List<RecentDoc>>(emptyList())
    val recentDocs: StateFlow<List<RecentDoc>> = _recentDocsFlow.asStateFlow()

    private val _activityItemsFlow = MutableStateFlow<List<ActivityItem>>(emptyList())
    val activityItems: StateFlow<List<ActivityItem>> = _activityItemsFlow.asStateFlow()

    // ─── Dynamic Notifications state management ───────────────────────────────
    private val _notifications = MutableStateFlow<List<EduNotification>>(emptyList())
    val notifications: StateFlow<List<EduNotification>> = _notifications.asStateFlow()

    fun loadNotifications(userId: String) {
        viewModelScope.launch {
            if (userId.isBlank() || userId == "guest_user") {
                // Đối với Guest: Trả về thông tin mẫu động chỉ trong RAM (tuyệt đối không ghi Firestore)
                val currentTime = System.currentTimeMillis()
                val guestNotifications = listOf(
                    EduNotification(
                        id = "guest_notif_01",
                        title = "Chào mừng đến với EduVault",
                        content = "Chào mừng bạn đến EduVault! Đăng nhập để truy cập học liệu đào tạo và lưu tiến độ của bạn.",
                        timestamp = currentTime - 5 * 60_000, // 5 phút trước
                        userId = "guest_user",
                        type = NotificationType.SYSTEM
                    ),
                    EduNotification(
                        id = "guest_notif_02",
                        title = "Học liệu đào tạo nội bộ",
                        content = "Đăng nhập để đọc học liệu và tham gia lộ trình đào tạo.",
                        timestamp = currentTime - 2 * 3600_000, // 2 giờ trước
                        userId = "guest_user",
                        type = NotificationType.AI
                    )
                )
                updateNotifications(guestNotifications, showActivity = false)
            } else {
                notificationRepository.ensureDefaultNotifications(userId)
                updateNotifications(
                    notificationRepository.getNotifications(userId).getOrDefault(emptyList()),
                    showActivity = true,
                )
            }
        }
    }

    // ─── StudyAI Chatbot state management ─────────────────────────────────────
    private val _chatMessages = MutableStateFlow<List<AiChatMessage>>(listOf(
        AiChatMessage(false, "Chào bạn! Mình là Trợ lý đào tạo StudyAI. Bạn muốn làm rõ kiến thức kỹ thuật, tóm tắt tài liệu hay tạo câu hỏi luyện tập hôm nay? 🧠")
    ))
    val chatMessages: StateFlow<List<AiChatMessage>> = _chatMessages.asStateFlow()

    private val _isAiTyping = MutableStateFlow(false)
    val isAiTyping: StateFlow<Boolean> = _isAiTyping.asStateFlow()

    private var lastRequestTime = 0L

    fun sendChatMessage(question: String) {
        val trimmed = question.trim()
        if (trimmed.isEmpty()) return

        val currentTime = System.currentTimeMillis()
        if (currentTime - lastRequestTime < 3000) { // Rate limit 3 giây chống spam
            _chatMessages.update {
                it + AiChatMessage(true, trimmed) + AiChatMessage(false, "⚠️ Vui lòng đợi một chút trước khi gửi câu hỏi tiếp theo! (Giới hạn 3 giây giữa các lần hỏi)")
            }
            return
        }
        lastRequestTime = currentTime

        // Thêm câu hỏi của User vào list chat
        _chatMessages.update { it + AiChatMessage(true, trimmed) }

        viewModelScope.launch {
            _isAiTyping.value = true
            try {
                // Lấy tối đa 6 tin nhắn gần nhất làm context cho Gemini (takeLast(6))
                val history = _chatMessages.value.takeLast(6)
                val result = aiRepository.askGeneralStudyAi(trimmed, history)
                result.onSuccess { answer ->
                    _chatMessages.update { it + AiChatMessage(false, answer) }
                }.onFailure { err ->
                    _chatMessages.update { it + AiChatMessage(false, "Có lỗi xảy ra: ${err.localizedMessage}") }
                }
            } catch (e: Exception) {
                _chatMessages.update { it + AiChatMessage(false, "Không thể kết nối AI: ${e.localizedMessage}") }
            } finally {
                // Đảm bảo loading state luôn được tắt kể cả khi lỗi
                _isAiTyping.value = false
            }
        }
    }

    init {
        loadCurrentUser()
    }

    fun onProfileUpdated(user: com.example.eduvault.domain.model.User) {
        _uiState.update { it.copy(userName = user.fullName, university = user.university,
            authState = AuthState.Authenticated(user)) }
    }

    fun loadCurrentUser() {
        viewModelScope.launch {
            val user = authRepository.getCurrentUser().getOrNull()
            _uiState.update {
                it.copy(
                    authState = user?.let(AuthState::Authenticated) ?: AuthState.Guest,
                    userName = user?.fullName?.takeIf(String::isNotBlank) ?: "Bạn",
                    university = user?.university?.takeIf(String::isNotBlank)
                        ?: "Chương trình đào tạo nhân sự mới",
                )
            }
            loadNotifications(user?.uid ?: "guest_user")
            loadRecentDocuments()
        }
    }

    fun loadRecentDocuments() {
        viewModelScope.launch {
            getPublishedDocuments().onSuccess { firestoreDocs ->
                val converted = firestoreDocs.map { doc ->
                    val type = when (doc.type) {
                        com.example.eduvault.domain.model.LibraryDocumentType.NOTE -> DocType.NOTE
                        com.example.eduvault.domain.model.LibraryDocumentType.QUIZ -> DocType.QUIZ
                        com.example.eduvault.domain.model.LibraryDocumentType.SLIDE -> DocType.SLIDE
                        com.example.eduvault.domain.model.LibraryDocumentType.SUMMARY -> DocType.SUMMARY
                    }
                    val emoji = when (type) {
                        DocType.NOTE -> "📝"
                        DocType.QUIZ -> "🧩"
                        DocType.SLIDE -> "📊"
                        DocType.SUMMARY -> "⚖️"
                    }
                    val viewsInt = if (doc.views.contains("k")) {
                        ((doc.views.replace("k", "").replace(" ", "").toFloatOrNull() ?: 0f) * 1000).toInt()
                    } else {
                        doc.views.toIntOrNull() ?: 0
                    }
                    RecentDoc(
                        id = doc.id,
                        title = doc.title,
                        course = doc.courseCode,
                        type = type,
                        emoji = emoji,
                        views = viewsInt,
                        bgIndex = doc.bgIndex,
                        fileName = doc.fileName,
                        downloadUrl = doc.downloadUrl,
                        authorId = doc.authorId,
                        fileSizeBytes = doc.fileSizeBytes,
                        quantityLabel = doc.quantityLabel,
                        rating = doc.rating
                    )
                }
                
                _recentDocsFlow.value = converted.take(6)
            }.onFailure {
                _recentDocsFlow.value = emptyList()
            }
        }
    }

    private fun updateNotifications(notifications: List<EduNotification>, showActivity: Boolean) {
        _notifications.value = notifications
        _uiState.update { it.copy(unreadNotificationCount = notifications.count { notice -> !notice.isRead }) }
        _activityItemsFlow.value = if (showActivity) notifications.take(5).map { notification ->
            val type = when (notification.type) {
                NotificationType.AI -> if (notification.title.contains("Tóm tắt", ignoreCase = true)
                    || notification.title.contains("Summary", ignoreCase = true)) ActivityType.SLIDE else ActivityType.QUIZ
                NotificationType.ACHIEVEMENT -> ActivityType.QUIZ
                NotificationType.SYSTEM, NotificationType.COMMUNITY -> ActivityType.NOTE
            }
            ActivityItem(
                id = notification.id,
                title = notification.title,
                subtitle = notification.content,
                time = notification.getFormattedTime().ifBlank { "Vừa xong" },
                type = type,
            )
        } else emptyList()
    }

    // ─── Event handlers ───────────────────────────────────────────────────────

    fun onTabSelected(tab: HomeTab) {
        _uiState.update { it.copy(selectedTab = tab) }
    }

}
