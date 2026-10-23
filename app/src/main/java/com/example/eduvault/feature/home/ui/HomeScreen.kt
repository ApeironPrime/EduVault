package com.example.eduvault.feature.home.ui

import com.example.eduvault.domain.model.evidenceTaskTitle
import com.example.eduvault.domain.model.evidenceInstructions
import androidx.activity.compose.BackHandler

import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.rememberGraphicsLayer
import com.example.eduvault.feature.home.ui.components.DockBackdrop
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.eduvault.core.theme.ColorSlateBackground
import com.example.eduvault.domain.model.AuthState
import com.example.eduvault.domain.model.DocReaderState
import com.example.eduvault.feature.home.ui.components.ActivitySection
import com.example.eduvault.feature.home.ui.components.FocusActionCard
import com.example.eduvault.feature.home.ui.components.HeroProgressCard
import com.example.eduvault.feature.home.ui.components.TrainingMilestonesCard
import com.example.eduvault.feature.home.ui.components.HomeBottomNav
import com.example.eduvault.feature.home.ui.components.HomeTopBar
import com.example.eduvault.feature.home.ui.components.RecentDocsRow
import com.example.eduvault.feature.home.ui.components.SectionHeader
import com.example.eduvault.feature.home.ui.components.SubmitEvidenceDialog
import com.example.eduvault.feature.home.ui.components.TrainingAvailabilityCard
import com.example.eduvault.feature.home.ui.dialogs.CreateQuizSetupDialog
import com.example.eduvault.feature.home.ui.dialogs.FlashcardDialog
import com.example.eduvault.feature.home.ui.dialogs.GuestLoginPromptDialog
import com.example.eduvault.feature.home.ui.dialogs.NotificationsDialog
import com.example.eduvault.feature.home.ui.dialogs.StudyAiDialog
import com.example.eduvault.feature.library.ui.viewer.DocViewerDialog
import com.example.eduvault.feature.library.ui.LibraryContent
import com.example.eduvault.feature.library.ui.LibraryDoc
import com.example.eduvault.feature.library.ui.LibraryDocType
import com.example.eduvault.feature.library.ui.LibraryViewModel
import com.example.eduvault.feature.library.ui.UploadDocSheet
import com.example.eduvault.feature.notes.ui.NoteEditorDialog
import com.example.eduvault.feature.notes.ui.NotesDialog
import com.example.eduvault.feature.notes.ui.NotesViewModel
import com.example.eduvault.feature.profile.ui.ProfileContent
import com.example.eduvault.feature.quiz.ui.QuizContent
import com.example.eduvault.feature.quiz.ui.QuizScreenMode
import com.example.eduvault.feature.quiz.ui.QuizViewModel
import kotlinx.coroutines.CompletableDeferred

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    trainingViewModel: TrainingViewModel = hiltViewModel(),
    quizViewModel: QuizViewModel = hiltViewModel(),
    libraryViewModel: LibraryViewModel = hiltViewModel(),
    onLogout: () -> Unit = {},
    onNavigateToAdmin: () -> Unit = {},
    onNavigateToLogin: () -> Unit = {},
    onNavigateToRegister: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val signedInUser = (uiState.authState as? AuthState.Authenticated)?.user
    if (signedInUser?.role == "mentor") {
        com.example.eduvault.feature.mentor.ui.MentorScreen(user = signedInUser, onLogout = onLogout)
        return
    }
    BackHandler(enabled = uiState.selectedTab != HomeTab.HOME) { viewModel.onTabSelected(HomeTab.HOME) }
    val dynamicRecentDocs by viewModel.recentDocs.collectAsStateWithLifecycle()
    val dynamicActivityItems by viewModel.activityItems.collectAsStateWithLifecycle()
    val libraryUiState by libraryViewModel.uiState.collectAsStateWithLifecycle()
    val trainingUiState by trainingViewModel.uiState.collectAsStateWithLifecycle()
    var showUploadSheet by remember { mutableStateOf(false) }
    var showGuestPrompt by remember { mutableStateOf(false) }
    var showFlashcards by remember { mutableStateOf(false) }
    var showStudyAi by remember { mutableStateOf(false) }
    var showNotifications by remember { mutableStateOf(false) }
    var showCreateQuizSetup by remember { mutableStateOf(false) }
    var presetDocTitle by remember { mutableStateOf("") }
    var selectedRecentDoc by remember { mutableStateOf<RecentDoc?>(null) }
    val context = LocalContext.current

    var showNotes by remember { mutableStateOf(false) }
    val notesViewModel: NotesViewModel = hiltViewModel()
    val notesUiState by notesViewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(notesUiState.error, showNotes) {
        if (!showNotes && notesUiState.editingNote == null &&
            notesUiState.error == "Không thể mở ghi chú. Vui lòng kiểm tra kết nối rồi thử lại.") {
            Toast.makeText(context, notesUiState.error, Toast.LENGTH_LONG).show()
            notesViewModel.clearError()
        }
    }

    var showSubmitEvidenceDialog by remember { mutableStateOf(false) }
    val hasSubmittedTask = trainingUiState.dashboard?.currentEvidence?.status == "PENDING"

    LaunchedEffect(trainingUiState.evidenceSubmissionVersion) {
        if (trainingUiState.evidenceSubmissionVersion > 0 && showSubmitEvidenceDialog) {
            showSubmitEvidenceDialog = false
            Toast.makeText(context, "Đã gửi bằng chứng cho mentor nghiệm thu.", Toast.LENGTH_LONG).show()
        }
    }

    LaunchedEffect(uiState.authState) {
        if (uiState.authState is AuthState.Authenticated) trainingViewModel.loadProgram()
        else trainingViewModel.clear()
    }

    HomeScaffold(
        uiState = uiState,
        trainingUiState = trainingUiState,
        recentDocs = dynamicRecentDocs,
        activityItems = dynamicActivityItems,
        quizViewModel = quizViewModel,
        hasSubmittedTask = hasSubmittedTask,
        onSubmitEvidenceClick = {
            if (uiState.authState is AuthState.Guest) showGuestPrompt = true
            else {
                trainingViewModel.clearEvidenceError()
                showSubmitEvidenceDialog = true
            }
        },
        onRetryTraining = { trainingViewModel.loadProgram(force = true) },
        onTabSelected = viewModel::onTabSelected,
        onUploadClick = {
            if (uiState.authState is AuthState.Guest) showGuestPrompt = true
            else showUploadSheet = true
        },
        onOpenDoc = { recentDoc ->
            if (recentDoc.type == DocType.QUIZ) {
                presetDocTitle = recentDoc.title
                showCreateQuizSetup = true
            } else {
                selectedRecentDoc = recentDoc
            }
        },
        onOpenFlashcards = { showFlashcards = true },
        onOpenStudyAi = { showStudyAi = true },
        onOpenNotes = {
            if (uiState.authState is AuthState.Guest) {
                showGuestPrompt = true
            } else {
                showNotes = true
            }
        },
        onNotificationClick = { showNotifications = true },
        onStartQuizSetup = { title ->
            presetDocTitle = title
            showCreateQuizSetup = true
        },
        onProfileUpdated = viewModel::onProfileUpdated,
        onLogout = onLogout,
        onNavigateToAdmin = onNavigateToAdmin,
        onNavigateToLogin = onNavigateToLogin,
        onNavigateToRegister = onNavigateToRegister
    )

    if (showNotes) {
        NotesDialog(
            viewModel = notesViewModel,
            onDismiss = { showNotes = false }
        )
    }

    if (notesUiState.editingNote != null && !showNotes) {
        NoteEditorDialog(
            viewModel = notesViewModel,
            onDismiss = { notesViewModel.closeNoteEditor() }
        )
    }

    if (showUploadSheet) {
        UploadDocSheet(
            onDismiss = { showUploadSheet = false }
        )
    }

    if (showFlashcards) {
        FlashcardDialog(
            onDismiss = { showFlashcards = false }
        )
    }

    if (showStudyAi) {
        StudyAiDialog(
            onDismiss = { showStudyAi = false }
        )
    }

    if (showNotifications) {
        NotificationsDialog(
            onDismiss = { showNotifications = false }
        )
    }

    if (showCreateQuizSetup) {
        CreateQuizSetupDialog(
            quizViewModel = quizViewModel,
            presetTitle = presetDocTitle,
            onDismiss = { showCreateQuizSetup = false; presetDocTitle = "" },
            onConfirm = { docTitle, count, difficulty, format ->
                showCreateQuizSetup = false
                val selectedDocObj = quizViewModel.availableDocs.value.find { it.title == docTitle }
                val docContent = if (selectedDocObj != null) {
                    "Kỹ năng: ${selectedDocObj.courseCode} - Tiêu đề: ${selectedDocObj.title} - Phân loại: ${selectedDocObj.type.label}."
                } else {
                    "Tài liệu đào tạo $docTitle tổng hợp kiến thức trọng tâm cho intern và fresher."
                }

                quizViewModel.generateAndStartQuiz(
                    docId = selectedDocObj?.id,
                    docTitle = docTitle,
                    count = count,
                    difficulty = difficulty,
                    format = format,
                    docContent = docContent,
                    onError = { errorMsg ->
                        Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show()
                    }
                )
                viewModel.onTabSelected(HomeTab.QUIZ)
            }
        )
    }

    if (showGuestPrompt) {
        GuestLoginPromptDialog(
            onDismiss = { showGuestPrompt = false },
            onNavigateToLogin = onNavigateToLogin,
            onNavigateToRegister = onNavigateToRegister
        )
    }

    if (showSubmitEvidenceDialog) {
        SubmitEvidenceDialog(
            onDismiss = { if (!trainingUiState.isSubmittingEvidence) showSubmitEvidenceDialog = false },
            onSubmit = trainingViewModel::submitEvidence,
            isSubmitting = trainingUiState.isSubmittingEvidence,
            errorMessage = trainingUiState.evidenceError,
            initialLink = trainingUiState.dashboard?.currentEvidence?.link.orEmpty(),
            initialNote = trainingUiState.dashboard?.currentEvidence?.note.orEmpty(),
            stageNumber = trainingUiState.dashboard?.activeStageNumber ?: 1,
            moduleTitle = trainingUiState.dashboard?.activeModule?.evidenceTaskTitle.orEmpty(),
            instructions = trainingUiState.dashboard?.activeModule?.evidenceInstructions.orEmpty(),
            mentorFeedback = trainingUiState.dashboard?.currentEvidence?.takeIf { it.status == "REJECTED" }?.feedback.orEmpty(),
            submissionStatus = trainingUiState.dashboard?.currentEvidence?.status,
        )
    }

    selectedRecentDoc?.let { recentDoc ->
        val mappedDoc = LibraryDoc(
            id = recentDoc.id,
            courseCode = recentDoc.course,
            title = recentDoc.title,
            type = when (recentDoc.type) {
                DocType.NOTE -> LibraryDocType.NOTE
                DocType.QUIZ -> LibraryDocType.QUIZ
                DocType.SLIDE -> LibraryDocType.SLIDE
                DocType.SUMMARY -> LibraryDocType.SUMMARY
            },
            quantityLabel = recentDoc.quantityLabel,
            rating = recentDoc.rating,
            views = "${recentDoc.views}",
            bgIndex = recentDoc.bgIndex,
            fileName = recentDoc.fileName,
            downloadUrl = recentDoc.downloadUrl,
            authorId = recentDoc.authorId,
            fileSizeBytes = recentDoc.fileSizeBytes
        )
        val user = (uiState.authState as? AuthState.Authenticated)?.user
        val readerState = if (uiState.authState is AuthState.Guest) {
            DocReaderState.GuestLocked
        } else {
            DocReaderState.Unlocked
        }
        LaunchedEffect(recentDoc.id) {
            libraryViewModel.loadDocViewerContent(
                recentDoc.id,
                recentDoc.title,
                recentDoc.course,
                when (recentDoc.type) {
                    DocType.NOTE -> "Ghi chú"
                    DocType.QUIZ -> "Quiz"
                    DocType.SLIDE -> "Slide"
                    DocType.SUMMARY -> "Tóm tắt"
                }
            )
        }

        DocViewerDialog(
            doc = mappedDoc,
            readerState = readerState,
            activeContent = libraryUiState.activeDocContent,
            isLoadingContent = libraryUiState.isLoadingDocContent,
            contentError = libraryUiState.docContentError,
            onRetryLoadContent = {
                libraryViewModel.loadDocViewerContent(
                    recentDoc.id,
                    recentDoc.title,
                    recentDoc.course,
                    when (recentDoc.type) {
                        DocType.NOTE -> "Ghi chú"
                        DocType.QUIZ -> "Quiz"
                        DocType.SLIDE -> "Slide"
                        DocType.SUMMARY -> "Tóm tắt"
                    }
                )
            },
            onDismiss = {
                libraryViewModel.clearDocViewerContent()
                selectedRecentDoc = null
            },
            onReportClick = {
                libraryViewModel.reportDocument(
                    docId = recentDoc.id,
                    onSuccess = {
                        libraryViewModel.clearDocViewerContent()
                        selectedRecentDoc = null
                        Toast.makeText(context, "Đã gửi báo cáo vi phạm tài liệu này!", Toast.LENGTH_SHORT).show()
                    },
                    onFailure = { err ->
                        Toast.makeText(context, "Tố cáo thất bại: $err", Toast.LENGTH_LONG).show()
                    }
                )
            },
            onNavigateToLogin = onNavigateToLogin,
            onNavigateToRegister = onNavigateToRegister,
            onUnlockWithCredit = {},
            onGenerateSummary = {
                val deferred = CompletableDeferred<Result<String>>()
                libraryViewModel.generateDocumentSummary(
                    docId = recentDoc.id,
                    docTitle = recentDoc.title,
                    docType = when (recentDoc.type) {
                        DocType.NOTE -> "Ghi chú"
                        DocType.QUIZ -> "Quiz"
                        DocType.SLIDE -> "Slide"
                        DocType.SUMMARY -> "Tóm tắt"
                    },
                    content = libraryUiState.activeDocContent?.content
                        ?: "Tóm tắt tài liệu đào tạo ${recentDoc.title} thuộc kỹ năng ${recentDoc.course}."
                ) { result ->
                    deferred.complete(result)
                }
                deferred.await()
            },
            onNoteClick = { doc ->
                if (uiState.authState is AuthState.Guest) {
                    showGuestPrompt = true
                } else {
                    notesViewModel.openNoteEditor(doc.id, doc.title, doc.courseCode)
                }
            },
            currentUserId = user?.uid ?: "",
            currentUserRole = user?.role ?: "user",
            onDeleteDoc = { docToDelete ->
                libraryViewModel.deleteDocument(
                    docId = docToDelete.id,
                    onSuccess = {
                        libraryViewModel.clearDocViewerContent()
                        selectedRecentDoc = null
                        viewModel.loadRecentDocuments()
                        Toast.makeText(context, "Đã xóa tài liệu thành công!", Toast.LENGTH_SHORT).show()
                    },
                    onFailure = { err ->
                        Toast.makeText(context, "Xóa tài liệu thất bại: $err", Toast.LENGTH_LONG).show()
                    }
                )
            }
        )
    }
}

@Composable
fun HomeScaffold(
    uiState: HomeUiState,
    trainingUiState: TrainingUiState = TrainingUiState(),
    recentDocs: List<RecentDoc>,
    activityItems: List<ActivityItem>,
    quizViewModel: QuizViewModel,
    hasSubmittedTask: Boolean = false,
    onSubmitEvidenceClick: () -> Unit = {},
    onRetryTraining: () -> Unit = {},
    onTabSelected: (HomeTab) -> Unit,
    onUploadClick: () -> Unit,
    onOpenDoc: (RecentDoc) -> Unit,
    onOpenFlashcards: () -> Unit,
    onOpenStudyAi: () -> Unit,
    onOpenNotes: () -> Unit,
    onNotificationClick: () -> Unit,
    onStartQuizSetup: (String) -> Unit,
    onProfileUpdated: (com.example.eduvault.domain.model.User) -> Unit = {},
    onLogout: () -> Unit = {},
    onNavigateToAdmin: () -> Unit = {},
    onNavigateToLogin: () -> Unit = {},
    onNavigateToRegister: () -> Unit = {}
) {
    val quizUiState by quizViewModel.uiState.collectAsState()
    val showBottomBar = uiState.selectedTab != HomeTab.QUIZ || quizUiState.screenMode == QuizScreenMode.DASHBOARD

    val backdropLayer = rememberGraphicsLayer()
    val backdrop = remember(backdropLayer) { DockBackdrop(backdropLayer) }
    Scaffold(
        containerColor = ColorSlateBackground,
        bottomBar = {
            if (showBottomBar) {
                HomeBottomNav(
                    selectedTab = uiState.selectedTab,
                    backdrop = backdrop,
                    onTabSelected = onTabSelected
                )
            }
        },
    ) { paddingValues ->
        Box(with(backdrop) { Modifier.fillMaxSize().capture() }) {
            when (uiState.selectedTab) {
                HomeTab.HOME -> HomeContent(
                    uiState = uiState,
                    trainingUiState = trainingUiState,
                    recentDocs = recentDocs,
                    activityItems = activityItems,
                    onStartQuizSetup = onStartQuizSetup,
                    paddingValues = paddingValues,
                    onTabSelected = onTabSelected,
                    onUploadClick = onUploadClick,
                    onOpenDoc = onOpenDoc,
                    onOpenFlashcards = onOpenFlashcards,
                    onOpenStudyAi = onOpenStudyAi,
                    onOpenNotes = onOpenNotes,
                    onNotificationClick = onNotificationClick,
                    onRetryTraining = onRetryTraining,
                    onSubmitEvidenceClick = onSubmitEvidenceClick,
                    hasSubmittedTask = hasSubmittedTask,
                )
                HomeTab.DOCUMENTS -> LibraryContent(
                    paddingValues = paddingValues,
                    onUploadClick = onUploadClick,
                    onNavigateToLogin = onNavigateToLogin,
                    onNavigateToRegister = onNavigateToRegister
                )
                HomeTab.QUIZ -> QuizContent(
                    paddingValues = paddingValues,
                    viewModel = quizViewModel,
                    trainingState = trainingUiState,
                    onSubmitEvidence = onSubmitEvidenceClick,
                    onRetryTraining = onRetryTraining
                )
                HomeTab.PROFILE -> ProfileContent(
                    paddingValues = paddingValues,
                    onLogout = onLogout,
                    onNavigateToAdmin = onNavigateToAdmin,
                    onNavigateToLogin = onNavigateToLogin,
                    onNavigateToRegister = onNavigateToRegister,
                    trainingState = trainingUiState,
                    onRetryTraining = onRetryTraining,
                    onProfileUpdated = onProfileUpdated
                )
            }
        }
    }
}

@Composable
fun HomeContent(
    uiState: HomeUiState,
    trainingUiState: TrainingUiState,
    recentDocs: List<RecentDoc>,
    activityItems: List<ActivityItem>,
    onStartQuizSetup: (String) -> Unit,
    paddingValues: PaddingValues,
    onTabSelected: (HomeTab) -> Unit,
    onUploadClick: () -> Unit,
    onOpenDoc: (RecentDoc) -> Unit,
    onOpenFlashcards: () -> Unit,
    onOpenStudyAi: () -> Unit,
    onOpenNotes: () -> Unit,
    onNotificationClick: () -> Unit,
    onRetryTraining: () -> Unit,
    onSubmitEvidenceClick: () -> Unit,
    hasSubmittedTask: Boolean = false,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize()
            .padding(bottom = paddingValues.calculateBottomPadding())
            .testTag("roadmap_content"),
        contentPadding = PaddingValues(bottom = 24.dp),
    ) {
        // ── 1. Top Bar tinh gọn & Định danh chuyên nghiệp ──
        item {
            HomeTopBar(
                uiState = uiState,
                onNotificationClick = onNotificationClick
            )
            Spacer(modifier = Modifier.height(10.dp))
        }

        // ── 2. Hero Progress Card (Màu xanh Navy đậm #0F172A) ──
        val dashboard = trainingUiState.dashboard
        if (dashboard != null) {
            item {
                HeroProgressCard(
                    completionPercent = dashboard.assignment.completionPercent,
                    targetText = "Hoàn thành trước " + java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.forLanguageTag("vi-VN")).format(java.util.Date(dashboard.assignment.dueAtMillis)),
                    statusNote = "Đã nghiệm thu ${dashboard.assignment.completedModuleIds.size}/${dashboard.program.modules.size} chặng" +
                        (if (dashboard.currentEvidence?.status == "PENDING") " • Bằng chứng chờ duyệt" else ""),
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Spacer(modifier = Modifier.height(18.dp))
            }
        }

        if (dashboard == null || trainingUiState.error != null) {
            item {
                TrainingAvailabilityCard(
                    isLoading = trainingUiState.isLoading,
                    message = trainingUiState.error,
                    onRetry = onRetryTraining,
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
                Spacer(Modifier.height(18.dp))
            }
        }
        if (dashboard != null) {
            item {
                TrainingMilestonesCard(
                    dashboard = dashboard,
                    onSubmitEvidence = onSubmitEvidenceClick,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Spacer(modifier = Modifier.height(18.dp))
            }
            if (dashboard.currentEvidence?.status == "REJECTED") item {
                androidx.compose.foundation.layout.Column(Modifier.padding(horizontal = 16.dp)) {
                    androidx.compose.material3.Text("Mentor yêu cầu bổ sung", color = androidx.compose.ui.graphics.Color(0xFFB45309))
                    androidx.compose.material3.Text(dashboard.currentEvidence?.feedback?.takeIf { it.isNotBlank() }
                        ?: "Hãy bổ sung bằng chứng theo yêu cầu của mentor.", color = androidx.compose.ui.graphics.Color(0xFF475569))
                }
            }
            dashboard.activeModule?.let { module -> item {
                FocusActionCard(
                    onSubmitClick = onSubmitEvidenceClick,
                    hasSubmitted = hasSubmittedTask,
                    stageNumber = dashboard.activeStageNumber,
                    taskTitle = module.evidenceTaskTitle,
                    taskDescription = module.evidenceInstructions +
                        (module.taskDefinition?.dueDate?.takeIf { it.isNotBlank() }?.let { "\nHạn nộp: $it" } ?: ""),
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Spacer(modifier = Modifier.height(20.dp))
            } }
        }

        // ── 5. Học liệu & Hoạt động đào tạo gần đây ──
        if (recentDocs.isNotEmpty()) {
            item {
                SectionHeader(
                    title = "Học liệu đính kèm",
                    actionText = "Xem tất cả",
                    onActionClick = { onTabSelected(HomeTab.DOCUMENTS) },
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Spacer(modifier = Modifier.height(10.dp))
            }
            item {
                RecentDocsRow(
                    docs = recentDocs,
                    onOpenDoc = onOpenDoc
                )
                Spacer(modifier = Modifier.height(20.dp))
            }
        }

        if (activityItems.isNotEmpty()) {
            item {
                SectionHeader(
                    title = "Nhật ký đào tạo gần đây",
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Spacer(modifier = Modifier.height(10.dp))
            }
            item {
                ActivitySection(
                    items = activityItems,
                    onTabSelected = onTabSelected,
                    onStartQuizSetup = onStartQuizSetup,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        }
    }
}
