package com.example.eduvault.feature.quiz.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.isActive
import com.example.eduvault.domain.repository.AuthRepository
import com.example.eduvault.domain.usecase.GetPublishedDocumentsUseCase
import com.example.eduvault.domain.repository.DocumentRepository
import com.example.eduvault.domain.repository.AiRepository
import com.example.eduvault.domain.repository.NotificationRepository
import com.example.eduvault.domain.model.NotificationType
import javax.inject.Inject
import com.example.eduvault.feature.library.ui.toLibraryDoc

@HiltViewModel
class QuizViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val getPublishedDocuments: GetPublishedDocumentsUseCase,
    private val documentRepository: DocumentRepository,
    private val aiRepository: AiRepository,
    private val notificationRepository: NotificationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(QuizUiState())
    val uiState: StateFlow<QuizUiState> = _uiState.asStateFlow()

    private val _availableDocs = MutableStateFlow<List<com.example.eduvault.feature.library.ui.LibraryDoc>>(emptyList())
    val availableDocs: StateFlow<List<com.example.eduvault.feature.library.ui.LibraryDoc>> = _availableDocs.asStateFlow()

    private val allQuizSetsList = mutableListOf<QuizSet>()
    private val localQuestionsCache = mutableMapOf<String, List<com.example.eduvault.feature.quiz.ui.QuizQuestion>>()


    private var timerJob: Job? = null
    private var attemptSaveJob: Job? = null



    init {
        loadAvailableDocuments()
        loadQuizSets()
        loadUserStats()
    }

    fun loadAvailableDocuments() {
        viewModelScope.launch {
            getPublishedDocuments().onSuccess { firestoreDocs ->
                _availableDocs.value = firestoreDocs.map { it.toLibraryDoc() }
            }.onFailure {
                _availableDocs.value = emptyList()
            }
        }
    }

    fun loadQuizSets() {
        viewModelScope.launch {
            _uiState.update { it.copy(errorMessage = null) }
            authRepository.getQuizSets().onSuccess { firestoreSets ->
                val localAiSets = allQuizSetsList.filter { it.id.startsWith("ai_") }
                allQuizSetsList.clear()
                allQuizSetsList.addAll(localAiSets)
                allQuizSetsList.addAll(firestoreSets)
                
                _uiState.update { state ->
                    val filtered = if (state.selectedSubject == "Tất cả") {
                        allQuizSetsList
                    } else {
                        allQuizSetsList.filter { it.subject.contains(state.selectedSubject, ignoreCase = true) }
                    }
                    state.copy(quizSets = filtered.toList())
                }
            }.onFailure {
                val localAiSets = allQuizSetsList.filter { it.id.startsWith("ai_") }
                allQuizSetsList.clear()
                allQuizSetsList.addAll(localAiSets)
                
                _uiState.update { state ->
                    val filtered = if (state.selectedSubject == "Tất cả") {
                        allQuizSetsList
                    } else {
                        allQuizSetsList.filter { it.subject.contains(state.selectedSubject, ignoreCase = true) }
                    }
                    state.copy(quizSets = filtered.toList(), errorMessage = "Chưa tải được bài tự kiểm tra. Vui lòng thử lại.")
                }
            }
        }
    }

    fun loadUserStats() {
        viewModelScope.launch {
            authRepository.getCurrentUser().onSuccess { user ->
                if (user != null) {
                    val compRate = if (user.quizCount > 0) {
                        (user.quizAverageScore * 10).toInt().coerceIn(0, 100)
                    } else 0
                    
                    _uiState.update { state ->
                        state.copy(
                            progress = QuizProgress(
                                avgScore = user.quizAverageScore,
                                completedCount = user.quizCount,
                                completionRate = compRate
                            )
                        )
                    }
                } else {
                    _uiState.update { state ->
                        state.copy(progress = QuizProgress(0.0f, 0, 0))
                    }
                }
            }.onFailure {
                _uiState.update { state ->
                    state.copy(progress = QuizProgress(0.0f, 0, 0))
                }
            }
        }
    }


    // ─── Event Handlers ───────────────────────────────────────────────────────

    /**
     * Lọc danh sách bộ đề theo nhóm kỹ năng
     */
    fun onSubjectSelected(subject: String) {
        _uiState.update { state ->
            val filtered = if (subject == "Tất cả") {
                allQuizSetsList
            } else {
                allQuizSetsList.filter { it.subject.contains(subject, ignoreCase = true) }
            }
            state.copy(
                selectedSubject = subject,
                quizSets = filtered
            )
        }
    }

    private var lastAiCallTime: Long = 0

    /**
     * Tự động sinh bài đánh giá kiến thức từ tài liệu đào tạo và bắt đầu làm bài.
     */
    fun generateAndStartQuiz(
        docId: String? = null,
        docTitle: String,
        count: Int,
        difficulty: Difficulty,
        format: String = "Trắc nghiệm",
        docContent: String,
        onError: (String) -> Unit
    ) {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastAiCallTime < 60000) {
            val remainSeconds = 60 - (currentTime - lastAiCallTime) / 1000
            onError("Bạn thao tác quá nhanh. Vui lòng đợi trong $remainSeconds giây để tránh spam API.")
            return
        }

        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            var contentToUse = docContent

            // Nếu có docId, thử truy xuất nội dung lý thuyết thực tế từ cache trước
            if (!docId.isNullOrEmpty()) {
                documentRepository.getCachedDocViewerContent(docId).onSuccess { cachedContent ->
                    if (cachedContent != null && cachedContent.content.trim().isNotEmpty()) {
                        contentToUse = cachedContent.content
                    }
                }
            }

            if (contentToUse.trim().isEmpty()) {
                contentToUse = "Tài liệu đào tạo $docTitle dành cho chương trình intern và fresher."
            }

            aiRepository.generateQuizFromDocument(
                title = docTitle,
                type = "Tài liệu ôn tập",
                content = contentToUse,
                count = count,
                format = format
            ).onSuccess { generatedQuestions ->
                lastAiCallTime = System.currentTimeMillis()
                
                // Sinh thông báo sự kiện tạo Quiz AI thành công thực tế lưu vào Firestore
                viewModelScope.launch {
                    authRepository.getCurrentUser().onSuccess { user ->
                        if (user != null) {
                            notificationRepository.addNotification(
                                userId = user.uid,
                                title = "🧩 Đề ôn tập AI",
                                content = "EduVault vừa tạo bài luyện tập $format gồm $count câu hỏi từ tài liệu '${docTitle}'!",
                                type = NotificationType.AI
                            )
                        }
                    }
                }

                val customSet = QuizSet(
                    id = "ai_${System.currentTimeMillis()}",
                    subject = docTitle,
                    title = "Quiz AI ($format): $docTitle",
                    difficulty = difficulty,
                    questionCount = count,
                    playCount = "1",
                    isNew = true,
                    bgIndex = (Math.abs(docTitle.hashCode()) % 6)
                )

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        screenMode = QuizScreenMode.PLAYER,
                    attemptSaveMessage = null,
                        activeQuizSet = customSet,
                        questions = generatedQuestions,
                        currentQuestionIndex = 0,
                        selectedOption = null,
                        isAnswerLocked = false,
                        remainingSeconds = 60,
                        answersMap = emptyMap(),
                        correctAnswersCount = 0,
                        timeSpentSeconds = 0,
                        xpEarned = 0,
                        quizFormat = format
                    )
                }

                // Lưu cache câu hỏi tạm thời của phiên để hỗ trợ chơi lại
                localQuestionsCache[customSet.id] = generatedQuestions

                // Tự động lưu bộ đề lên Firestore để phục vụ chấm điểm và ghi nhận qua Cloud Functions
                viewModelScope.launch {
                    authRepository.saveQuizSet(customSet, generatedQuestions)
                }

                startTimer()
            }.onFailure { e ->
                _uiState.update { it.copy(isLoading = false) }
                onError(e.localizedMessage ?: "Quá trình tạo đề thi AI gặp sự cố định dạng. Vui lòng bấm thử lại.")
            }
        }
    }

    /**
     * Bắt đầu chơi bộ đề trắc nghiệm
     */
    fun startQuiz(quizSet: QuizSet) {
        attemptSaveJob?.cancel()
        _uiState.update { it.copy(isLoading = true, errorMessage = null, attemptSaveMessage = null) }
        
        viewModelScope.launch {
            val fetchedQuestions = localQuestionsCache[quizSet.id]
                ?: authRepository.getQuizQuestions(quizSet.id).getOrElse { emptyList() }

            if (fetchedQuestions.isEmpty() || quizSet.questionCount <= 0) {
                _uiState.update { it.copy(isLoading = false,
                    errorMessage = "Chưa tải được câu hỏi. Vui lòng thử lại.") }
                return@launch
            }

            val finalQuestions = mutableListOf<QuizQuestion>()
            val count = quizSet.questionCount
            // Cycle through fetchedQuestions to fill up to 'count' questions
            var cycleIndex = 0
            while (finalQuestions.size < count) {
                val q = fetchedQuestions[cycleIndex % fetchedQuestions.size]
                finalQuestions.add(q.copy(id = "${q.id}_${finalQuestions.size}"))
                cycleIndex++
            }

            _uiState.update {
                it.copy(
                    isLoading = false,
                    screenMode = QuizScreenMode.PLAYER,
                    attemptSaveMessage = null,
                    activeQuizSet = quizSet,
                    questions = finalQuestions,
                    currentQuestionIndex = 0,
                    selectedOption = null,
                    isAnswerLocked = false,
                    remainingSeconds = 60,
                    answersMap = emptyMap(),
                    correctAnswersCount = 0,
                    timeSpentSeconds = 0,
                    xpEarned = 0
                )
            }

            startTimer()
        }
    }

    /**
     * Chọn phương án trả lời
     */
    fun selectOption(optionKey: String) {
        val state = _uiState.value
        if (state.isAnswerLocked) return // Không cho chọn lại sau khi đã khóa

        val currentQuestion = state.questions.getOrNull(state.currentQuestionIndex) ?: return
        val isCorrect = currentQuestion.correctOption == optionKey

        val updatedAnswersMap = state.answersMap.toMutableMap()
        updatedAnswersMap[state.currentQuestionIndex] = optionKey

        val updatedCorrectCount = if (isCorrect) state.correctAnswersCount + 1 else state.correctAnswersCount
        val xpReward = if (isCorrect) state.xpEarned + 20 else state.xpEarned // 20 XP cho mỗi câu đúng

        stopTimer() // Dừng bộ đếm ngược khi đã chọn đáp án

        _uiState.update {
            it.copy(
                selectedOption = optionKey,
                isAnswerLocked = true,
                answersMap = updatedAnswersMap,
                correctAnswersCount = updatedCorrectCount,
                xpEarned = xpReward
            )
        }
    }

    /**
     * Chuyển sang câu hỏi tiếp theo hoặc hiển thị màn hình kết quả
     */
    fun nextQuestion() {
        val state = _uiState.value
        val nextIndex = state.currentQuestionIndex + 1

        if (nextIndex < state.questions.size) {
            // Chuyển sang câu hỏi kế tiếp
            _uiState.update {
                it.copy(
                    currentQuestionIndex = nextIndex,
                    selectedOption = null,
                    isAnswerLocked = false,
                    remainingSeconds = 60
                )
            }
            startTimer() // Khởi động lại đếm ngược cho câu hỏi mới
        } else {
            // Hoàn thành bộ đề -> Chuyển sang màn hình Kết Quả
            stopTimer()
            _uiState.update {
                it.copy(
                    screenMode = QuizScreenMode.RESULTS,
                    // Cập nhật điểm thưởng thêm 50 XP nếu đạt điểm tối đa
                    xpEarned = if (it.correctAnswersCount == it.questions.size) it.xpEarned + 50 else it.xpEarned
                )
            }

            // Capture the completed attempt before a retry or navigation changes player state.
            val completedState = _uiState.value
            val currentSet = completedState.activeQuizSet
            if (currentSet != null && completedState.questions.isNotEmpty()) {
                val alphabet = listOf("A", "B", "C", "D")
                val answers = completedState.questions.indices.map { index ->
                    alphabet.indexOf(completedState.answersMap[index] ?: "")
                }
                _uiState.update { it.copy(attemptSaveMessage = "Đang lưu kết quả tự kiểm tra…") }
                attemptSaveJob?.cancel()
                attemptSaveJob = viewModelScope.launch {
                    val result = authRepository.recordQuizAttempt(
                        currentSet.id, java.util.UUID.randomUUID().toString(), answers
                    )
                    if (!isActive) return@launch
                    result.onSuccess { saved ->
                        _uiState.update {
                            it.copy(
                                xpEarned = if (saved.xpEarned > 0) saved.xpEarned else it.xpEarned,
                                attemptSaveMessage = "Đã lưu kết quả tự kiểm tra."
                            )
                        }
                        loadUserStats()
                    }.onFailure {
                        _uiState.update {
                            it.copy(attemptSaveMessage = "Kết quả phiên này chưa được lưu vào hồ sơ. Bạn vẫn có thể xem lại đáp án.")
                        }
                    }
                }
            }
        }
    }

    /**
     * Bỏ qua câu hỏi hiện tại (Đánh dấu sai)
     */
    fun skipQuestion() {
        val state = _uiState.value
        if (state.isAnswerLocked) {
            // Nếu đã trả lời xong và bấm nút, hoạt động giống như phím Tiếp theo
            nextQuestion()
            return
        }

        // Đánh dấu bỏ qua là trả lời sai
        val updatedAnswersMap = state.answersMap.toMutableMap()
        updatedAnswersMap[state.currentQuestionIndex] = "" // Rỗng nghĩa là bỏ qua/không trả lời

        stopTimer()

        _uiState.update {
            it.copy(
                selectedOption = "",
                isAnswerLocked = true,
                answersMap = updatedAnswersMap
            )
        }
    }

    /**
     * Chơi lại bài trắc nghiệm hiện tại
     */
    fun retryQuiz() {
        val activeSet = _uiState.value.activeQuizSet
        if (activeSet != null) {
            startQuiz(activeSet)
        }
    }

    /**
     * Thoát bài trắc nghiệm, quay lại Dashboard chính
     */
    fun exitQuiz() {
        attemptSaveJob?.cancel()
        stopTimer()
        _uiState.update {
            it.copy(
                screenMode = QuizScreenMode.DASHBOARD,
                attemptSaveMessage = null,
                activeQuizSet = null,
                questions = emptyList(),
                currentQuestionIndex = 0,
                selectedOption = null,
                isAnswerLocked = false,
                answersMap = emptyMap(),
                correctAnswersCount = 0,
                timeSpentSeconds = 0,
                xpEarned = 0
            )
        }
    }

    // ─── Timer Logic ──────────────────────────────────────────────────────────

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                _uiState.update { state ->
                    val nextRemaining = state.remainingSeconds - 1
                    val nextTimeSpent = state.timeSpentSeconds + 1

                    if (nextRemaining <= 0) {
                        // Hết thời gian -> Tự động khóa đáp án là sai để hiển thị giải thích
                        stopTimer()
                        val updatedAnswersMap = state.answersMap.toMutableMap()
                        updatedAnswersMap[state.currentQuestionIndex] = "" // Đáp án rỗng

                        state.copy(
                            remainingSeconds = 0,
                            timeSpentSeconds = nextTimeSpent,
                            selectedOption = "",
                            isAnswerLocked = true,
                            answersMap = updatedAnswersMap
                        )
                    } else {
                        state.copy(
                            remainingSeconds = nextRemaining,
                            timeSpentSeconds = nextTimeSpent
                        )
                    }
                }
            }
        }
    }

    private fun stopTimer() {
        timerJob?.cancel()
        timerJob = null
    }

    override fun onCleared() {
        super.onCleared()
        stopTimer()
    }
}
