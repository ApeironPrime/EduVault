package com.example.eduvault.feature.library.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject
import com.example.eduvault.domain.repository.AuthRepository
import com.example.eduvault.domain.repository.DocumentRepository
import com.example.eduvault.domain.repository.CategoryRepository
import com.example.eduvault.domain.repository.AiRepository
import com.example.eduvault.domain.repository.NotificationRepository
import com.example.eduvault.domain.repository.FlashcardRepository
import com.example.eduvault.domain.model.NotificationType
import com.example.eduvault.feature.library.ui.toLibraryDoc

private const val PAGE_SIZE = 6

@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val documentRepository: DocumentRepository,
    private val categoryRepository: CategoryRepository,
    private val aiRepository: AiRepository,
    private val notificationRepository: NotificationRepository,
    private val flashcardRepository: FlashcardRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LibraryUiState())
    val uiState: StateFlow<LibraryUiState> = _uiState.asStateFlow()

    private val _subjects = MutableStateFlow<List<String>>(listOf("Tất cả", "Git", "Lập trình", "Kiểm thử", "Agile/Scrum", "DevOps", "Bảo mật"))
    val subjects: StateFlow<List<String>> = _subjects.asStateFlow()

    private val allDocsList = mutableListOf<LibraryDoc>()

    init {
        loadDocuments()
        loadCategories()
        loadUserCredits()
    }

    fun loadDocuments() {
        viewModelScope.launch {
            documentRepository.getDocuments().onSuccess { firestoreDocs ->
                allDocsList.clear()
                allDocsList.addAll(firestoreDocs.map { it.toLibraryDoc() })
                syncSavedStatusAndFilter()
            }.onFailure {
                allDocsList.clear()
                syncSavedStatusAndFilter()
            }
        }
    }

    fun loadCategories() {
        viewModelScope.launch {
            categoryRepository.getCategories().onSuccess { cats ->
                val list = listOf("Tất cả") + cats.map { it.name }
                _subjects.update { list }
                filterAndSortDocs()
            }
        }
    }

    fun loadUserCredits() {
        viewModelScope.launch {
            authRepository.getCurrentUser().onSuccess { user ->
                _uiState.update { 
                    it.copy(
                        documentCredits = user?.documentCredits ?: -1,
                        uploadCount = user?.uploadCount ?: 0,
                        unlockedDocuments = user?.unlockedDocuments ?: emptyList(),
                        savedDocuments = user?.savedDocuments ?: emptyList(),
                        currentUserId = user?.uid ?: "",
                        currentUserRole = user?.role ?: "user"
                    )
                }
                syncSavedStatusAndFilter()
            }
        }
    }

    /** Mở tài liệu đào tạo. Quyền truy cập được quyết định bởi trạng thái đăng nhập. */
    fun onOpenDocument(docId: String, onAllowed: () -> Unit, onBlocked: () -> Unit) {
        onAllowed()
    }

    /** Legacy community-mode API, retained for backward compatibility. */
    fun unlockDocument(docId: String, onResult: (Result<com.example.eduvault.domain.model.User>) -> Unit) {
        viewModelScope.launch {
            authRepository.unlockDocument(docId).onSuccess { user ->
                _uiState.update {
                    it.copy(
                        documentCredits = user.documentCredits,
                        uploadCount = user.uploadCount,
                        unlockedDocuments = user.unlockedDocuments
                    )
                }
                onResult(Result.success(user))
            }.onFailure { err ->
                onResult(Result.failure(err))
            }
        }
    }

    /**
     * Báo cáo tài liệu khiếm nhã hoặc vi phạm.
     */
    fun reportDocument(docId: String, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        viewModelScope.launch {
            documentRepository.reportDocument(docId).onSuccess {
                onSuccess()
            }.onFailure { error ->
                onFailure(error.localizedMessage ?: "Lỗi chưa rõ")
            }
        }
    }

    // ─── Event Handlers ───────────────────────────────────────────────────────

    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query, currentPage = 1) }
        filterAndSortDocs()
    }

    fun onSubjectSelected(subject: String) {
        _uiState.update { it.copy(selectedSubject = subject, currentPage = 1) }
        filterAndSortDocs()
    }

    fun onDocTypeSelected(filter: DocTypeFilter) {
        _uiState.update { it.copy(selectedDocType = filter, currentPage = 1) }
        filterAndSortDocs()
    }

    fun onSortSelected(sort: SortType) {
        _uiState.update { it.copy(selectedSort = sort, currentPage = 1) }
        filterAndSortDocs()
    }

    fun onSchoolSelected(school: String) {
        _uiState.update { it.copy(selectedSchool = school, currentPage = 1) }
        filterAndSortDocs()
    }

    fun onPageSelected(page: Int) {
        _uiState.update { it.copy(currentPage = page) }
        filterAndSortDocs()
    }

    fun onToggleViewMode() {
        _uiState.update { it.copy(isGridView = !it.isGridView) }
    }

    fun onToggleSaved(docId: String) {
        viewModelScope.launch {
            // Sửa bug code gốc: tìm và toggle isSaved trong allDocsList RAM ngay lập tức để phản hồi nhanh
            val index = allDocsList.indexOfFirst { it.id == docId }
            if (index != -1) {
                val doc = allDocsList[index]
                allDocsList[index] = doc.copy(isSaved = !doc.isSaved)
            }
            filterAndSortDocs()

            // Thực hiện ghi nhận bookmark thật lên database Firestore
            authRepository.toggleSaveDocument(docId).onSuccess { user ->
                _uiState.update { 
                    it.copy(
                        savedDocuments = user.savedDocuments
                    )
                }
                syncSavedStatusAndFilter()
            }.onFailure {
                // Nếu lưu database thất bại (ví dụ: Guest hoặc mất mạng), ta rollback lại RAM
                val idx = allDocsList.indexOfFirst { it.id == docId }
                if (idx != -1) {
                    val doc = allDocsList[idx]
                    allDocsList[idx] = doc.copy(isSaved = !doc.isSaved)
                }
                filterAndSortDocs()
            }
        }
    }

    private fun syncSavedStatusAndFilter() {
        val savedIds = _uiState.value.savedDocuments.toSet()
        val isLoggedIn = _uiState.value.currentUserId.isNotEmpty()
        for (i in allDocsList.indices) {
            val doc = allDocsList[i]
            if (isLoggedIn) {
                allDocsList[i] = doc.copy(isSaved = doc.id in savedIds)
            }
        }
        filterAndSortDocs()
    }

    // ─── Helper filter & sort logic ───────────────────────────────────────────

    private fun filterAndSortDocs() {
        viewModelScope.launch {
            val query = _uiState.value.searchQuery.trim().lowercase()
            val subject = _uiState.value.selectedSubject
            val typeFilter = _uiState.value.selectedDocType
            val sortType = _uiState.value.selectedSort

            // Filter
            var filtered = allDocsList.filter { doc ->
                val matchesSearch = query.isEmpty() ||
                        doc.title.lowercase().contains(query) ||
                        doc.courseCode.lowercase().contains(query)
                
                val matchesSubject = subject == "Tất cả" || 
                        doc.title.contains(subject, ignoreCase = true) ||
                        doc.courseCode.contains(subject, ignoreCase = true) ||
                        when (subject) {
                            "Git" -> doc.courseCode.contains("GIT", ignoreCase = true)
                            "Lập trình" -> listOf("JAVA", "KOTLIN", "DOTNET", "WEB", "MOBILE").any { doc.courseCode.contains(it, ignoreCase = true) }
                            "Kiểm thử" -> listOf("TEST", "QA", "QC").any { doc.courseCode.contains(it, ignoreCase = true) }
                            "Agile/Scrum" -> listOf("AGILE", "SCRUM").any { doc.courseCode.contains(it, ignoreCase = true) }
                            "DevOps" -> listOf("DEVOPS", "CI/CD", "CLOUD").any { doc.courseCode.contains(it, ignoreCase = true) }
                            "Bảo mật" -> listOf("SEC", "SECURITY").any { doc.courseCode.contains(it, ignoreCase = true) }
                            else -> false
                        }

                val matchesType = typeFilter == DocTypeFilter.ALL || when (typeFilter) {
                    DocTypeFilter.NOTE -> doc.type == LibraryDocType.NOTE
                    DocTypeFilter.QUIZ -> doc.type == LibraryDocType.QUIZ
                    DocTypeFilter.SLIDE -> doc.type == LibraryDocType.SLIDE
                    DocTypeFilter.SUMMARY -> doc.type == LibraryDocType.SUMMARY
                    else -> false
                }

                matchesSearch && matchesSubject && matchesType
            }

            // Sort
            filtered = when (sortType) {
                SortType.NEWEST -> filtered.sortedByDescending { it.id.toIntOrNull() ?: 0 }
                SortType.POPULAR -> filtered.sortedByDescending { it.views.replace("k", "000").replace(".", "").toIntOrNull() ?: 0 }
                SortType.RATING -> filtered.sortedByDescending { it.rating }
            }

            val notesCount = allDocsList.count { it.type == LibraryDocType.NOTE }
            val quizzesCount = allDocsList.count { it.type == LibraryDocType.QUIZ }
            val slidesCount = allDocsList.count { it.type == LibraryDocType.SLIDE }
            val summariesCount = allDocsList.count { it.type == LibraryDocType.SUMMARY }

            var flashcardCount = 0
            val currentUserId = _uiState.value.currentUserId
            if (currentUserId.isNotEmpty()) {
                flashcardRepository.getUserFlashcardCount(currentUserId).onSuccess { count ->
                    flashcardCount = count
                }
            }

            val actualDocsCount = allDocsList.size
            val actualSubjectsCount = allDocsList.map { it.courseCode.trim().uppercase() }.distinct().count { it.isNotEmpty() }
            val savedCount = allDocsList.count { it.isSaved }

            val currentPageVal = _uiState.value.currentPage
            val totalCountVal = filtered.size
            val totalPagesVal = if (totalCountVal == 0) 1 else (totalCountVal + PAGE_SIZE - 1) / PAGE_SIZE
            val safePage = if (currentPageVal < 1) 1 else if (currentPageVal > totalPagesVal) totalPagesVal else currentPageVal

            val startIndex = (safePage - 1) * PAGE_SIZE
            val slicedDocs = filtered.drop(startIndex).take(PAGE_SIZE)

            _uiState.update {
                it.copy(
                    documents = slicedDocs,
                    totalCount = totalCountVal,
                    totalPages = totalPagesVal,
                    currentPage = safePage,
                    bannerTotalDocs = actualDocsCount,
                    bannerTotalSubjects = actualSubjectsCount,
                    bannerTotalSaved = savedCount,
                    docTypeCounts = mapOf(
                        DocTypeFilter.ALL to actualDocsCount,
                        DocTypeFilter.NOTE to notesCount,
                        DocTypeFilter.QUIZ to quizzesCount,
                        DocTypeFilter.SLIDE to slidesCount,
                        DocTypeFilter.SUMMARY to summariesCount,
                        DocTypeFilter.FLASHCARD to flashcardCount
                    )
                )
            }
        }
    }

    /**
     * Tự động sinh tóm tắt thông minh (Smart Summary) cho tài liệu bằng Gemini AI.
     * - Áp dụng cơ chế Firestore Caching & Versioning để tiết kiệm 100% token ở các lần đọc sau.
     */
    fun generateDocumentSummary(
        docId: String,
        docTitle: String,
        docType: String,
        content: String,
        onResult: (Result<String>) -> Unit
    ) {
        viewModelScope.launch {
            try {
                // 1. Kiểm tra Cache & Version trong DocumentRepository
                documentRepository.getCachedSummary(docId).onSuccess { cachedSummary ->
                    if (!cachedSummary.isNullOrBlank()) {
                        onResult(Result.success(cachedSummary))
                        return@launch
                    }
                }

                // 2. Gọi Gemini sinh tóm tắt mới nếu chưa có hoặc sai phiên bản
                val contentToUse = if (content.trim().isNotEmpty()) content else {
                    "Tài liệu đào tạo $docTitle dành cho chương trình intern và fresher."
                }

                aiRepository.generateSummaryFromDocument(
                    title = docTitle,
                    type = docType,
                    content = contentToUse
                ).onSuccess { generatedSummary ->
                    // Sinh thông báo sự kiện tạo tóm tắt thông minh thành công thực tế lưu vào Firestore
                    viewModelScope.launch {
                        authRepository.getCurrentUser().onSuccess { user ->
                            if (user != null) {
                                notificationRepository.addNotification(
                                    userId = user.uid,
                                    title = "✨ Tóm tắt thông minh",
                                    content = "Bản tóm tắt kiến thức bằng AI cho tài liệu '${docTitle}' đã được sinh thành công!",
                                    type = NotificationType.AI
                                )
                            }
                        }
                    }

                    // 3. Ghi ngược lại Firestore cache kèm version = 1 qua DocumentRepository
                    documentRepository.saveCachedSummary(docId, generatedSummary).onFailure { cacheErr ->
                        android.util.Log.e("LibraryViewModel", "Không thể lưu cache AI summary: ${cacheErr.localizedMessage}")
                    }
                    onResult(Result.success(generatedSummary))
                }.onFailure { err ->
                    onResult(Result.failure(err))
                }
            } catch (e: Exception) {
                onResult(Result.failure(e))
            }
        }
    }

    /**
     * Hỏi đáp trực tiếp về tài liệu cụ thể bằng Gemini AI.
     */
    fun askAiAboutDocument(
        docTitle: String,
        docType: String,
        content: String,
        question: String,
        onResult: (Result<String>) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val contentToUse = if (content.trim().isNotEmpty()) content else {
                    "Tài liệu đào tạo $docTitle dành cho chương trình intern và fresher."
                }
                aiRepository.askAiAboutDocument(
                    title = docTitle,
                    type = docType,
                    content = contentToUse,
                    question = question
                ).onSuccess { answer ->
                    onResult(Result.success(answer))
                }.onFailure { err ->
                    onResult(Result.failure(err))
                }
            } catch (e: Exception) {
                onResult(Result.failure(e))
            }
        }
    }

    /**
     * Tải nội dung chi tiết động cho 3 tab của DocViewer.
     * Quy trình: Kiểm tra cache ở Repository -> Nếu có thì nạp luôn -> Nếu không có thì gọi Gemini sinh -> Lưu cache ngầm.
     */
    fun loadDocViewerContent(
        docId: String,
        docTitle: String,
        docCourseCode: String,
        docTypeLabel: String
    ) {
        viewModelScope.launch {
            _uiState.update { 
                it.copy(
                    isLoadingDocContent = true, 
                    docContentError = null,
                    activeDocContent = null
                ) 
            }

            // Bước 1: Gọi Repository lấy Cache
            documentRepository.getCachedDocViewerContent(docId).onSuccess { cachedContent ->
                if (cachedContent != null) {
                    // Trả về cache nhanh chóng
                    _uiState.update {
                        it.copy(
                            isLoadingDocContent = false,
                            activeDocContent = cachedContent
                        )
                    }
                } else {
                    // Không có cache -> Chuyển sang Bước 2: Gọi Gemini sinh
                    generateDocContentFromAi(docId, docTitle, docCourseCode, docTypeLabel)
                }
            }.onFailure { err ->
                // Firestore/Network cache lỗi -> Ghi log cảnh báo nhưng vẫn gọi Gemini để đảm bảo trải nghiệm tốt nhất
                android.util.Log.w("LibraryViewModel", "Lỗi đọc cache Firestore: ${err.localizedMessage}")
                generateDocContentFromAi(docId, docTitle, docCourseCode, docTypeLabel)
            }
        }
    }

    private suspend fun generateDocContentFromAi(
        docId: String,
        docTitle: String,
        docCourseCode: String,
        docTypeLabel: String
    ) {
        aiRepository.generateDocViewerContent(
            title = docTitle,
            courseCode = docCourseCode,
            type = docTypeLabel
        ).onSuccess { generatedContent ->
            // Cập nhật State UI lập tức
            _uiState.update {
                it.copy(
                    isLoadingDocContent = false,
                    activeDocContent = generatedContent
                )
            }

            // Ghi đè cache ngầm vào Firestore (không chặn luồng UI của người dùng, có bắt lỗi bằng Log.w)
            viewModelScope.launch {
                documentRepository.saveDocViewerContent(docId, generatedContent).onFailure { cacheErr ->
                    android.util.Log.w("LibraryViewModel", "Không thể cập nhật cache Firestore cho tài liệu $docId: ${cacheErr.localizedMessage}")
                }
            }
        }.onFailure { err ->
            _uiState.update {
                it.copy(
                    isLoadingDocContent = false,
                    docContentError = err.localizedMessage ?: "Lỗi nạp nội dung từ AI"
                )
            }
        }
    }

    fun clearDocViewerContent() {
        _uiState.update {
            it.copy(
                activeDocContent = null,
                isLoadingDocContent = false,
                docContentError = null
            )
        }
    }

    /**
     * Tải tệp tài liệu PDF thông qua DocumentRepository.
     */
    suspend fun downloadDocumentPdf(downloadUrl: String, destinationFile: File): Result<File> {
        return documentRepository.downloadDocumentPdf(downloadUrl, destinationFile)
    }

    /**
     * Cho phép Admin hoặc Tác giả xóa tài liệu khỏi hệ thống.
     */
    fun deleteDocument(docId: String, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        viewModelScope.launch {
            documentRepository.deleteDocument(docId).onSuccess {
                // Xóa khỏi danh sách RAM cục bộ ngay lập tức để cập nhật UI nhanh
                allDocsList.removeAll { it.id == docId }
                filterAndSortDocs()
                onSuccess()
            }.onFailure { error ->
                onFailure(error.localizedMessage ?: "Lỗi xóa tài liệu")
            }
        }
    }
}
