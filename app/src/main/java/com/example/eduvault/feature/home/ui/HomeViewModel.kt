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

import com.example.eduvault.domain.repository.CategoryRepository
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
    private val categoryRepository: CategoryRepository,
    private val aiRepository: AiRepository,
    private val notificationRepository: NotificationRepository,
    private val getPublishedDocuments: GetPublishedDocumentsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _topicTags = MutableStateFlow<List<TopicTag>>(emptyList())
    val dynamicTopicTags: StateFlow<List<TopicTag>> = _topicTags.asStateFlow()

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
                        title = "🏆 Thành tựu mới! (Guest Mode)",
                        content = "Chào mừng bạn ghé thăm EduVault! Đăng nhập để chia sẻ tài liệu và lưu trữ tiến trình học tập không giới hạn.",
                        timestamp = currentTime - 5 * 60_000, // 5 phút trước
                        userId = "guest_user",
                        type = NotificationType.SYSTEM
                    ),
                    EduNotification(
                        id = "guest_notif_02",
                        title = "🧠 Trải nghiệm AI miễn phí",
                        content = "EduVault hỗ trợ Guest dùng thử tab Tóm tắt AI và Chatbot học tập chung hoàn toàn miễn phí!",
                        timestamp = currentTime - 2 * 3600_000, // 2 giờ trước
                        userId = "guest_user",
                        type = NotificationType.AI
                    )
                )
                _notifications.value = guestNotifications
            } else {
                // Đối với người dùng đã đăng nhập:
                // 1. Đảm bảo đã chèn dữ liệu mẫu nếu Firestore trống
                notificationRepository.ensureDefaultNotifications(userId)
                // 2. Fetch thông báo động thực tế từ Firestore
                notificationRepository.getNotifications(userId)
                    .onSuccess { list ->
                        _notifications.value = list
                    }
                    .onFailure {
                        _notifications.value = emptyList()
                    }
            }
        }
    }

    // ─── StudyAI Chatbot state management ─────────────────────────────────────
    private val _chatMessages = MutableStateFlow<List<AiChatMessage>>(listOf(
        AiChatMessage(false, "Chào bạn! Mình là Trợ lý Học tập StudyAI. Bạn cần mình giải đáp thắc mắc, tóm tắt tài liệu hay tạo câu hỏi ôn tập nào hôm nay? 🧠")
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
        loadTopicTags()
    }

    fun loadCurrentUser() {
        viewModelScope.launch {
            authRepository.getCurrentUser()
                .onSuccess { user ->
                    if (user != null) {
                        _uiState.update {
                            it.copy(
                                authState = AuthState.Authenticated(user),
                                userName = user.fullName.ifBlank { "Sinh viên" },
                                university = user.university.ifBlank { "ĐH Kinh tế TP.HCM" }
                            )
                        }
                        loadNotifications(user.uid)
                        loadRecentDocuments()
                        loadDynamicActivity(user.uid)
                    } else {
                        // Chưa đăng nhập — chế độ khách
                        _uiState.update {
                            it.copy(
                                authState = AuthState.Guest,
                                userName = "Bạn",
                                university = "Nền tảng chia sẻ tài liệu học thuật"
                            )
                        }
                        loadNotifications("guest_user")
                        loadRecentDocuments()
                        loadDynamicActivity("guest_user")
                    }
                }
                .onFailure {
                    // Lỗi mạng/Firebase — fallback về Guest
                    _uiState.update {
                        it.copy(
                            authState = AuthState.Guest,
                            userName = "Bạn",
                            university = "Nền tảng chia sẻ tài liệu học thuật"
                        )
                    }
                    loadNotifications("guest_user")
                    loadRecentDocuments()
                    loadDynamicActivity("guest_user")
                }
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
                    val savesInt = (doc.rating * 10).toInt()
                    val pagesInt = doc.quantityLabel.replace(" tr", "").replace(" câu", "").replace(" slides", "").toIntOrNull() ?: 15
                    
                    RecentDoc(
                        id = doc.id,
                        title = doc.title,
                        course = doc.courseCode,
                        type = type,
                        emoji = emoji,
                        views = viewsInt,
                        saves = savesInt,
                        pages = pagesInt,
                        bgIndex = doc.bgIndex,
                        fileName = doc.fileName,
                        downloadUrl = doc.downloadUrl,
                        authorId = doc.authorId,
                        fileSizeBytes = doc.fileSizeBytes,
                        quantityLabel = doc.quantityLabel,
                        rating = doc.rating
                    )
                }
                
                // Calculate actual dynamic categories counts from Firestore documents
                val tags = listOf(
                    TopicTag("Kinh tế học", firestoreDocs.count { it.courseCode.contains("EC", ignoreCase = true) || it.title.contains("kinh tế", ignoreCase = true) }),
                    TopicTag("Marketing", firestoreDocs.count { it.courseCode.contains("MKT", ignoreCase = true) }),
                    TopicTag("Kế toán", firestoreDocs.count { it.courseCode.contains("ACC", ignoreCase = true) }),
                    TopicTag("Pháp luật", firestoreDocs.count { it.courseCode.contains("LAW", ignoreCase = true) }),
                    TopicTag("Thống kê", firestoreDocs.count { it.courseCode.contains("STAT", ignoreCase = true) }),
                    TopicTag("Tài chính", firestoreDocs.count { it.courseCode.contains("FIN", ignoreCase = true) || it.title.contains("tài chính", ignoreCase = true) })
                )
                _topicTags.update { tags }
                
                if (converted.isNotEmpty()) {
                    _recentDocsFlow.value = converted.take(6)
                } else {
                    _recentDocsFlow.value = emptyList()
                }
            }.onFailure {
                _recentDocsFlow.value = emptyList()
            }
        }
    }

    fun loadDynamicActivity(userId: String) {
        viewModelScope.launch {
            if (userId.isBlank() || userId == "guest_user") {
                _activityItemsFlow.value = emptyList()
                return@launch
            }
            
            notificationRepository.getNotifications(userId).onSuccess { notifications ->
                val converted = notifications.map { notif ->
                    val (type, progress) = when (notif.type) {
                        NotificationType.AI -> {
                            if (notif.title.contains("Tóm tắt", ignoreCase = true) || notif.title.contains("Summary", ignoreCase = true)) {
                                Pair(ActivityType.SLIDE, 0.50f)
                            } else {
                                Pair(ActivityType.QUIZ, 0.85f)
                            }
                        }
                        NotificationType.SYSTEM -> Pair(ActivityType.NOTE, 1.0f)
                        NotificationType.ACHIEVEMENT -> Pair(ActivityType.QUIZ, 1.0f)
                        NotificationType.COMMUNITY -> Pair(ActivityType.NOTE, 0.70f)
                    }
                    
                    val timeStr = notif.getFormattedTime()
                    val cleanTime = if (timeStr.isBlank()) "Vừa xong" else timeStr
                    
                    ActivityItem(
                        id = notif.id,
                        title = notif.title,
                        subtitle = notif.content,
                        time = cleanTime,
                        type = type,
                        progress = progress
                    )
                }
                
                if (converted.isNotEmpty()) {
                    _activityItemsFlow.value = converted.take(5)
                } else {
                    _activityItemsFlow.value = emptyList()
                }
            }.onFailure {
                _activityItemsFlow.value = emptyList()
            }
        }
    }

    private fun loadTopicTags() {
        viewModelScope.launch {
            categoryRepository.getCategories().onSuccess { cats ->
                val tagsList = cats.map { cat ->
                    TopicTag(cat.name, cat.count)
                }
                _topicTags.update { tagsList }
            }.onFailure {
                _topicTags.update { emptyList() }
            }
        }
    }

    // ─── Event handlers ───────────────────────────────────────────────────────

    fun onTabSelected(tab: HomeTab) {
        _uiState.update { it.copy(selectedTab = tab) }
    }

}
