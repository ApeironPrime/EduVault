package com.example.eduvault.feature.home.ui

import android.annotation.SuppressLint
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.outlined.Bookmarks
import androidx.compose.material.icons.outlined.Cancel
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.automirrored.outlined.LibraryBooks
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Quiz
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import kotlinx.coroutines.tasks.await
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.eduvault.feature.library.ui.LibraryContent
import com.example.eduvault.feature.library.ui.UploadDocSheet
import com.example.eduvault.feature.library.ui.DocViewerDialog
import com.example.eduvault.feature.library.ui.LibraryDoc
import com.example.eduvault.feature.library.ui.LibraryDocType
import com.example.eduvault.feature.notes.ui.NotesDialog
import com.example.eduvault.feature.notes.ui.NoteEditorDialog
import com.example.eduvault.feature.notes.ui.NotesViewModel
import com.example.eduvault.feature.quiz.ui.QuizContent
import com.example.eduvault.feature.quiz.ui.QuizViewModel
import com.example.eduvault.feature.quiz.ui.QuizSet
import com.example.eduvault.feature.quiz.ui.Difficulty
import com.example.eduvault.feature.quiz.ui.QuizScreenMode
import com.example.eduvault.feature.profile.ui.ProfileContent
import com.example.eduvault.core.theme.ColorAmber
import com.example.eduvault.core.theme.ColorAmberDark
import com.example.eduvault.core.theme.ColorBorder
import com.example.eduvault.core.theme.ColorCream
import com.example.eduvault.core.theme.ColorForest
import com.example.eduvault.core.theme.ColorForestLight
import com.example.eduvault.core.theme.ColorError
import com.example.eduvault.core.theme.ColorInk
import com.example.eduvault.core.theme.ColorInkLight
import com.example.eduvault.core.theme.ColorInkLighter
import com.example.eduvault.core.theme.ColorPaper
import com.example.eduvault.core.theme.ColorTextOnDark
import com.example.eduvault.core.theme.ColorTextOnDarkSecondary
import com.example.eduvault.core.theme.ColorTextOnLight
import com.example.eduvault.core.theme.ColorTextOnLightSecondary
import com.example.eduvault.core.theme.DmSansFamily
import com.example.eduvault.core.theme.EduVaultTheme
import com.example.eduvault.core.theme.JetBrainsMonoFamily
import com.example.eduvault.core.theme.PlayfairDisplayFamily

// ─── Entry Point ──────────────────────────────────────────────────────────────

import com.example.eduvault.domain.model.AuthState
import com.example.eduvault.domain.model.DocReaderState
import com.example.eduvault.domain.model.AiChatMessage
import com.example.eduvault.core.ui.parseMarkdownToAnnotatedString
import androidx.compose.runtime.collectAsState


@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    quizViewModel: QuizViewModel = hiltViewModel(),
    libraryViewModel: com.example.eduvault.feature.library.ui.LibraryViewModel = hiltViewModel(),
    onLogout: () -> Unit = {},
    onNavigateToAdmin: () -> Unit = {},
    onNavigateToLogin: () -> Unit = {},
    onNavigateToRegister: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val dynamicTopicTags by viewModel.dynamicTopicTags.collectAsStateWithLifecycle()
    val dynamicRecentDocs by viewModel.recentDocs.collectAsStateWithLifecycle()
    val dynamicActivityItems by viewModel.activityItems.collectAsStateWithLifecycle()
    val libraryUiState by libraryViewModel.uiState.collectAsStateWithLifecycle()
    var showUploadSheet by remember { mutableStateOf(false) }
    var showGuestPrompt by remember { mutableStateOf(false) }
    var showFlashcards by remember { mutableStateOf(false) }
    var showStudyAi by remember { mutableStateOf(false) }
    var showNotifications by remember { mutableStateOf(false) }
    var showCreateQuizSetup by remember { mutableStateOf(false) }
    var presetDocTitle by remember { mutableStateOf("") }
    var selectedRecentDoc by remember { mutableStateOf<RecentDoc?>(null) }
    val context = androidx.compose.ui.platform.LocalContext.current

    var showNotes by remember { mutableStateOf(false) }
    val notesViewModel: NotesViewModel = hiltViewModel()
    val notesUiState by notesViewModel.uiState.collectAsStateWithLifecycle()

    HomeScaffold(
        uiState = uiState,
        recentDocs = dynamicRecentDocs,
        activityItems = dynamicActivityItems,
        topicTags = dynamicTopicTags,
        quizViewModel = quizViewModel,
        onTabSelected = viewModel::onTabSelected,
        onUploadClick = {
            // Guest không được upload
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

    if (notesUiState.editingNote != null) {
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
                    "Môn học: ${selectedDocObj.courseCode} - Tiêu đề: ${selectedDocObj.title} - Phân loại: ${selectedDocObj.type.label}."
                } else {
                    "Tài liệu học tập môn $docTitle tổng hợp lý thuyết trọng tâm."
                }

                quizViewModel.generateAndStartQuiz(
                    docId = selectedDocObj?.id,
                    docTitle = docTitle,
                    count = count,
                    difficulty = difficulty,
                    format = format,
                    docContent = docContent,
                    onError = { errorMsg ->
                        android.widget.Toast.makeText(context, errorMsg, android.widget.Toast.LENGTH_LONG).show()
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
        // Xác định trạng thái đọc dựa trên AuthState + Credit
        val user = (uiState.authState as? AuthState.Authenticated)?.user
        val cost = calculatePrice(recentDoc.fileSizeBytes, recentDoc.quantityLabel)
        val isUnlocked = user?.role == "admin" ||
                         recentDoc.authorId == user?.uid ||
                         user?.unlockedDocuments?.contains(recentDoc.id) == true
        val readerState = when {
            isUnlocked -> DocReaderState.Unlocked
            uiState.authState is AuthState.Guest -> DocReaderState.GuestLocked
            (user?.documentCredits ?: 0) < cost -> DocReaderState.NoCredits
            else -> DocReaderState.HasCredits(user?.documentCredits ?: 1)
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
                        android.widget.Toast.makeText(context, "Đã gửi báo cáo vi phạm tài liệu này!", android.widget.Toast.LENGTH_SHORT).show()
                    },
                    onFailure = { err ->
                        android.widget.Toast.makeText(context, "Tố cáo thất bại: $err", android.widget.Toast.LENGTH_LONG).show()
                    }
                )
            },
            onNavigateToLogin = onNavigateToLogin,
            onNavigateToRegister = onNavigateToRegister,
            onUnlockWithCredit = {
                libraryViewModel.unlockDocument(recentDoc.id) { result ->
                    result.onSuccess {
                        // Cập nhật lại HomeViewModel để làm mới thông tin UI
                        viewModel.loadCurrentUser()
                        android.widget.Toast.makeText(context, "Mở khóa tài liệu thành công!", android.widget.Toast.LENGTH_SHORT).show()
                    }.onFailure { err ->
                        android.widget.Toast.makeText(context, err.localizedMessage ?: "Mở khóa thất bại", android.widget.Toast.LENGTH_LONG).show()
                    }
                }
            },
            onGenerateSummary = {
                kotlinx.coroutines.suspendCancellableCoroutine { cont ->
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
                            ?: "Tóm tắt tài liệu học thuật ${recentDoc.title} thuộc chủ đề ${recentDoc.course}."
                    ) { result ->
                        if (cont.isActive) cont.resume(result) { /* cancelled */ }
                    }
                }
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
                        android.widget.Toast.makeText(context, "Đã xóa tài liệu thành công!", android.widget.Toast.LENGTH_SHORT).show()
                    },
                    onFailure = { err ->
                        android.widget.Toast.makeText(context, "Xóa tài liệu thất bại: $err", android.widget.Toast.LENGTH_LONG).show()
                    }
                )
            }
        )
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// SCAFFOLD: Bottom Navigation + FAB
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
private fun HomeScaffold(
    uiState: HomeUiState,
    recentDocs: List<RecentDoc>,
    activityItems: List<ActivityItem>,
    topicTags: List<TopicTag>,
    quizViewModel: QuizViewModel,
    onTabSelected: (HomeTab) -> Unit,
    onUploadClick: () -> Unit,
    onOpenDoc: (RecentDoc) -> Unit,
    onOpenFlashcards: () -> Unit,
    onOpenStudyAi: () -> Unit,
    onOpenNotes: () -> Unit,
    onNotificationClick: () -> Unit,
    onStartQuizSetup: (String) -> Unit,
    onLogout: () -> Unit = {},
    onNavigateToAdmin: () -> Unit = {},
    onNavigateToLogin: () -> Unit = {},
    onNavigateToRegister: () -> Unit = {}
) {
    val quizUiState by quizViewModel.uiState.collectAsState()
    val showBottomBar = uiState.selectedTab != HomeTab.QUIZ || quizUiState.screenMode == QuizScreenMode.DASHBOARD

    Scaffold(
        containerColor = ColorCream,
        bottomBar = {
            if (showBottomBar) {
                HomeBottomNav(
                    selectedTab = uiState.selectedTab,
                    onTabSelected = onTabSelected,
                    onUploadClick = onUploadClick
                )
            }
        },
    ) { paddingValues ->
        when (uiState.selectedTab) {
            HomeTab.HOME -> HomeContent(
                uiState = uiState,
                recentDocs = recentDocs,
                activityItems = activityItems,
                topicTags = topicTags,
                onStartQuizSetup = onStartQuizSetup,
                paddingValues = paddingValues,
                onTabSelected = onTabSelected,
                onUploadClick = onUploadClick,
                onOpenDoc = onOpenDoc,
                onOpenFlashcards = onOpenFlashcards,
                onOpenStudyAi = onOpenStudyAi,
                onOpenNotes = onOpenNotes,
                onNotificationClick = onNotificationClick
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
                onStartQuizSetup = { onStartQuizSetup("") }
            )
            HomeTab.PROFILE -> ProfileContent(
                paddingValues = paddingValues,
                onLogout = onLogout,
                onNavigateToAdmin = onNavigateToAdmin,
                onNavigateToLogin = onNavigateToLogin,
                onNavigateToRegister = onNavigateToRegister
            )
        }
    }
}

// ─── Bottom Navigation Bar ────────────────────────────────────────────────────

@Composable
private fun HomeBottomNav(
    selectedTab: HomeTab,
    onTabSelected: (HomeTab) -> Unit,
    onUploadClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding(),
        contentAlignment = Alignment.BottomCenter
    ) {
        // The actual bar background and items
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .background(ColorInk)
                .border(
                    width = 1.dp,
                    color = ColorBorder.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
                )
                .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Column 1: Trang chủ
            BottomNavItem(
                selected = selectedTab == HomeTab.HOME,
                onClick = { onTabSelected(HomeTab.HOME) },
                icon = Icons.Outlined.Home,
                label = "Trang chủ",
                modifier = Modifier.weight(1f)
            )

            // Column 2: Thư viện
            BottomNavItem(
                selected = selectedTab == HomeTab.DOCUMENTS,
                onClick = { onTabSelected(HomeTab.DOCUMENTS) },
                icon = Icons.AutoMirrored.Outlined.LibraryBooks,
                label = "Thư viện",
                modifier = Modifier.weight(1f)
            )

            // Column 3: Spacer for elevated round plus button
            Spacer(modifier = Modifier.width(64.dp))

            // Column 4: Quiz
            BottomNavItem(
                selected = selectedTab == HomeTab.QUIZ,
                onClick = { onTabSelected(HomeTab.QUIZ) },
                icon = Icons.Outlined.Quiz,
                label = "Quiz AI",
                modifier = Modifier.weight(1f)
            )

            // Column 5: Tôi
            BottomNavItem(
                selected = selectedTab == HomeTab.PROFILE,
                onClick = { onTabSelected(HomeTab.PROFILE) },
                icon = Icons.Outlined.AccountCircle,
                label = "Tôi",
                modifier = Modifier.weight(1f)
            )
        }

        // Elevated Amber floating button in the center
        Box(
            modifier = Modifier
                .offset(y = (-16).dp) // Raised elevated position
                .size(54.dp)
                .clip(CircleShape)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(ColorAmber, ColorAmberDark)
                    )
                )
                .border(3.dp, ColorInk, CircleShape)
                .clickable { onUploadClick() },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "+",
                color = ColorInk,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = DmSansFamily
            )
        }
    }
}

@Composable
private fun BottomNavItem(
    selected: Boolean,
    onClick: () -> Unit,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clickable(
                onClick = onClick,
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (selected) ColorAmber else ColorTextOnDarkSecondary,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            text = label,
            color = if (selected) ColorAmber else ColorTextOnDarkSecondary,
            fontFamily = DmSansFamily,
            fontSize = 10.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// HOME CONTENT (LazyColumn)
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
private fun HomeContent(
    uiState: HomeUiState,
    recentDocs: List<RecentDoc>,
    activityItems: List<ActivityItem>,
    topicTags: List<TopicTag>,
    onStartQuizSetup: (String) -> Unit,
    paddingValues: PaddingValues,
    onTabSelected: (HomeTab) -> Unit,
    onUploadClick: () -> Unit,
    onOpenDoc: (RecentDoc) -> Unit,
    onOpenFlashcards: () -> Unit,
    onOpenStudyAi: () -> Unit,
    onOpenNotes: () -> Unit,
    onNotificationClick: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = paddingValues.calculateBottomPadding()),
        contentPadding = PaddingValues(bottom = 16.dp),
    ) {
        // ── Top App Bar ────────────────────────────────────────────────────
        item {
            HomeTopBar(
                uiState = uiState,
                onNotificationClick = onNotificationClick
            )
        }

        // ── Hero Section ───────────────────────────────────────────────────
        item {
            val user = (uiState.authState as? AuthState.Authenticated)?.user
            HeroSection(
                onUploadClick = onUploadClick,
                onOpenStudyAi = onOpenStudyAi,
                user = user,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)
            )
        }

        // ── Quick Tools ────────────────────────────────────────────────────
        item {
            SectionHeader(
                title = "Công cụ nhanh",
                modifier = Modifier.padding(horizontal = 14.dp)
            )
            Spacer(modifier = Modifier.height(10.dp))
        }
        item {
            QuickToolsGrid(
                onTabSelected = onTabSelected,
                onOpenFlashcards = onOpenFlashcards,
                onOpenStudyAi = onOpenStudyAi,
                onOpenNotes = onOpenNotes,
                onStartQuizSetup = { onStartQuizSetup("") },
                modifier = Modifier.padding(horizontal = 14.dp)
            )
            Spacer(modifier = Modifier.height(20.dp))
        }

        // ── Recent Documents ───────────────────────────────────────────────
        item {
            SectionHeader(
                title = "Tài liệu gần đây",
                actionText = "Xem tất cả",
                onActionClick = { onTabSelected(HomeTab.DOCUMENTS) },
                modifier = Modifier.padding(horizontal = 14.dp)
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


        // ── Activity ───────────────────────────────────────────────────────
        item {
            SectionHeader(
                title = "Hoạt động gần đây",
                modifier = Modifier.padding(horizontal = 14.dp)
            )
            Spacer(modifier = Modifier.height(10.dp))
        }
        item {
            ActivitySection(
                items = activityItems,
                onTabSelected = onTabSelected,
                onStartQuizSetup = onStartQuizSetup,
                modifier = Modifier.padding(horizontal = 14.dp)
            )
            Spacer(modifier = Modifier.height(20.dp))
        }

    }
}

// ─── Top App Bar ──────────────────────────────────────────────────────────────

@Composable
private fun HomeTopBar(
    uiState: HomeUiState,
    onNotificationClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(ColorInk)
            .drawBehind {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(ColorAmber.copy(alpha = 0.15f), Color.Transparent),
                        center = Offset(size.width * 0.85f, size.height * 0.5f),
                        radius = size.width * 0.55f,
                    ),
                    radius = size.width * 0.55f,
                    center = Offset(size.width * 0.85f, size.height * 0.5f),
                )
            }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            // Logo mark
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Brush.linearGradient(colors = listOf(ColorAmber, ColorAmberDark))),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "E",
                    fontFamily = PlayfairDisplayFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    color = ColorInk,
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = buildAnnotatedString {
                        append("Xin chào, ")
                        withStyle(SpanStyle(color = ColorAmber, fontStyle = FontStyle.Italic)) {
                            append(uiState.userName.split(" ").first())
                        }
                        append(" 👋")
                    },
                    fontFamily = PlayfairDisplayFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = ColorTextOnDark,
                )
                Text(
                    text = "🏛️ ${uiState.university}",
                    fontFamily = DmSansFamily,
                    fontSize = 11.sp,
                    color = ColorTextOnDarkSecondary,
                )
            }



            // Notification button
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(ColorInkLight)
                    .clickable(indication = null, interactionSource = remember { MutableInteractionSource() }) { onNotificationClick() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Notifications,
                    contentDescription = "Thông báo",
                    tint = ColorTextOnDark,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

// ─── Hero Section ─────────────────────────────────────────────────────────────

@Composable
private fun HeroSection(
    onUploadClick: () -> Unit,
    onOpenStudyAi: () -> Unit,
    user: com.example.eduvault.domain.model.User?,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "hero_float")
    val float1 by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = -6f,
        animationSpec = infiniteRepeatable(tween(3000, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "float1"
    )
    val float2 by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = -6f,
        animationSpec = infiniteRepeatable(tween(3200, 800, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "float2"
    )
    val float3 by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = -6f,
        animationSpec = infiniteRepeatable(tween(2800, 1600, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "float3"
    )
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.12f, targetValue = 0.22f,
        animationSpec = infiniteRepeatable(tween(3500, easing = LinearEasing), RepeatMode.Reverse),
        label = "hero_glow"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(ColorInk)
            .drawBehind {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(ColorAmber.copy(alpha = glowAlpha), Color.Transparent),
                        center = Offset(size.width * 0.75f, size.height * 0.25f),
                        radius = size.width * 0.65f
                    ),
                    radius = size.width * 0.65f,
                    center = Offset(size.width * 0.75f, size.height * 0.25f),
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(ColorForest.copy(alpha = glowAlpha * 0.8f), Color.Transparent),
                        center = Offset(size.width * 0.25f, size.height * 0.85f),
                        radius = size.width * 0.5f
                    ),
                    radius = size.width * 0.5f,
                    center = Offset(size.width * 0.25f, size.height * 0.85f),
                )
            }
            .padding(20.dp)
    ) {
        Column {
            // Label
            Text(
                text = "✦  NỀN TẢNG HỌC TẬP THÔNG MINH",
                fontFamily = JetBrainsMonoFamily,
                fontSize = 9.sp,
                color = ColorAmber,
                letterSpacing = 1.5.sp,
                modifier = Modifier.padding(bottom = 10.dp)
            )

            // Title
            Text(
                text = buildAnnotatedString {
                    append("Học ")
                    withStyle(SpanStyle(color = ColorAmber, fontStyle = FontStyle.Italic)) {
                        append("hiệu quả hơn")
                    }
                    append("\nvới AI của bạn")
                },
                fontFamily = PlayfairDisplayFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 26.sp,
                lineHeight = 32.sp,
                color = ColorTextOnDark,
                letterSpacing = (-0.5).sp,
                modifier = Modifier.padding(bottom = 10.dp)
            )

            // Description
            Text(
                text = "Tóm tắt tài liệu, tạo quiz, ghi chú thông minh — tất cả được xây dựng từ chính nguồn học của bạn.",
                fontFamily = DmSansFamily,
                fontSize = 13.sp,
                lineHeight = 20.sp,
                color = ColorTextOnDarkSecondary,
                modifier = Modifier.padding(bottom = 18.dp)
            )

            // Floating mini-cards row
            Row(
                horizontalArrangement = Arrangement.spacedBy(9.dp),
                modifier = Modifier.padding(bottom = 20.dp)
            ) {
                HeroFloatCard(
                    emoji = "📝", label = "Ghi chú", subtitle = if (user != null) "${user.uploadCount} tài liệu" else "Tự động tạo",
                    modifier = Modifier.graphicsLayer { translationY = float1 }
                )
                HeroFloatCard(
                    emoji = "🧩", label = "Quiz AI", subtitle = if (user != null) "${user.quizCount} lượt làm" else "Luyện tập ngay",
                    modifier = Modifier.graphicsLayer { translationY = float2 }
                )
                HeroFloatCard(
                    emoji = "📊", label = "Tóm tắt", subtitle = if (user != null) "${user.documentCredits} credit" else "Trích xuất cốt lõi",
                    modifier = Modifier.graphicsLayer { translationY = float3 }
                )
            }

            // CTA Buttons
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(ColorAmber)
                        .clickable { onOpenStudyAi() }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "🤖  Thử StudyAI",
                        fontFamily = DmSansFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                        color = ColorInk,
                    )
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .border(1.dp, ColorTextOnDark.copy(alpha = 0.2f), RoundedCornerShape(10.dp))
                        .background(ColorTextOnDark.copy(alpha = 0.08f))
                        .clickable { onUploadClick() }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Tải tài liệu",
                        fontFamily = DmSansFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 13.sp,
                        color = ColorTextOnDark,
                    )
                }
            }
        }
    }
}

@Composable
private fun HeroFloatCard(
    emoji: String,
    label: String,
    subtitle: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(ColorTextOnDark.copy(alpha = 0.08f))
            .border(1.dp, ColorTextOnDark.copy(alpha = 0.12f), RoundedCornerShape(10.dp))
            .padding(horizontal = 10.dp, vertical = 8.dp)
    ) {
        Column {
            Text(
                text = emoji,
                fontSize = 14.sp,
                modifier = Modifier.padding(bottom = 3.dp)
            )
            Text(
                text = label,
                fontFamily = JetBrainsMonoFamily,
                fontSize = 8.sp,
                color = ColorAmber,
                letterSpacing = 0.8.sp,
            )
            Text(
                text = subtitle,
                fontFamily = DmSansFamily,
                fontSize = 10.sp,
                color = ColorTextOnDark,
                lineHeight = 14.sp,
            )
        }
    }
}

// ─── Section Header ───────────────────────────────────────────────────────────

@Composable
private fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    actionText: String? = null,
    onActionClick: () -> Unit = {},
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            fontFamily = PlayfairDisplayFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = ColorTextOnLight,
            letterSpacing = (-0.3).sp,
        )
        if (actionText != null) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) { onActionClick() }
            ) {
                Text(
                    text = actionText,
                    fontFamily = DmSansFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp,
                    color = ColorForest,
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.ArrowForward,
                    contentDescription = null,
                    tint = ColorForest,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}

// ─── Quick Tools Grid ─────────────────────────────────────────────────────────

@Composable
private fun QuickToolsGrid(
    onTabSelected: (HomeTab) -> Unit,
    onOpenFlashcards: () -> Unit,
    onOpenStudyAi: () -> Unit,
    onOpenNotes: () -> Unit,
    onStartQuizSetup: () -> Unit,
    modifier: Modifier = Modifier
) {
    val tools = listOf(
        QuickTool("📝", "Ghi chú", "Xem và chỉnh sửa tất cả ghi chú cá nhân của bạn", ColorAmber),
        QuickTool("🧩", "Tạo Quiz", "Sinh câu hỏi trắc nghiệm từ bất kỳ tài liệu nào", ColorForest),
        QuickTool("🃏", "Flashcard", "Ôn tập nhanh với phương pháp spaced repetition", Color(0xFFC0392B)),
        QuickTool("🔍", "Hỏi AI", "Đặt câu hỏi về bất kỳ chủ đề nào", ColorInk),
    )
    val bgColors = listOf(
        ColorAmber.copy(alpha = 0.12f),
        ColorForest.copy(alpha = 0.10f),
        Color(0xFFC0392B).copy(alpha = 0.09f),
        ColorInk.copy(alpha = 0.07f),
    )

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        // Row 1
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            QuickToolCard(
                tool = tools[0],
                bgColor = bgColors[0],
                onClick = onOpenNotes,
                modifier = Modifier.weight(1f)
            )
            QuickToolCard(
                tool = tools[1],
                bgColor = bgColors[1],
                onClick = onStartQuizSetup,
                modifier = Modifier.weight(1f)
            )
        }
        // Row 2
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            QuickToolCard(
                tool = tools[2],
                bgColor = bgColors[2],
                onClick = onOpenFlashcards,
                modifier = Modifier.weight(1f)
            )
            QuickToolCard(
                tool = tools[3],
                bgColor = bgColors[3],
                onClick = onOpenStudyAi,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

private data class QuickTool(val emoji: String, val name: String, val desc: String, val accentColor: Color)

@Composable
private fun QuickToolCard(
    tool: QuickTool,
    bgColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, ColorBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp, pressedElevation = 4.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(bgColor),
                contentAlignment = Alignment.Center,
            ) {
                Text(text = tool.emoji, fontSize = 18.sp)
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = tool.name,
                fontFamily = DmSansFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp,
                color = ColorTextOnLight,
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = tool.desc,
                fontFamily = DmSansFamily,
                fontSize = 11.sp,
                color = ColorTextOnLightSecondary,
                lineHeight = 15.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

// ─── Recent Documents Row (horizontal scroll) ─────────────────────────────────

@Composable
private fun RecentDocsRow(
    docs: List<RecentDoc>,
    onOpenDoc: (RecentDoc) -> Unit
) {
    val thumbBgs = listOf(
        listOf(Color(0xFFFEF9F0), Color(0xFFFDF0D5)),
        listOf(Color(0xFFF0F9F4), Color(0xFFD4EDDA)),
        listOf(Color(0xFFF0F4FF), Color(0xFFDDE8FF)),
        listOf(Color(0xFFFFF0F0), Color(0xFFFFDDDD)),
        listOf(Color(0xFFF5F0FF), Color(0xFFEAD5FF)),
        listOf(Color(0xFFF0FCFF), Color(0xFFD5F0FF)),
    )
    val badgeColors = mapOf(
        DocType.NOTE to Pair(ColorAmber.copy(alpha = 0.18f), ColorAmberDark),
        DocType.QUIZ to Pair(ColorForest.copy(alpha = 0.14f), ColorForest),
        DocType.SLIDE to Pair(Color(0xFF4A5568).copy(alpha = 0.12f), Color(0xFF2D3748)),
        DocType.SUMMARY to Pair(Color(0xFFC0392B).copy(alpha = 0.10f), Color(0xFF922B21)),
    )

    if (docs.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(Color(0xFFF9F6F0))
                .border(1.dp, ColorBorder, RoundedCornerShape(14.dp))
                .padding(vertical = 28.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("📂", fontSize = 28.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Chưa có tài liệu nào",
                    fontFamily = DmSansFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    color = ColorInk
                )
                Text(
                    text = "Hãy upload tài liệu đầu tiên của bạn!",
                    fontFamily = DmSansFamily,
                    fontSize = 11.sp,
                    color = ColorTextOnLightSecondary
                )
            }
        }
        return
    }

    LazyRow(
        contentPadding = PaddingValues(horizontal = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(11.dp),
    ) {
        items(docs) { doc ->
            val bg = thumbBgs.getOrElse(doc.bgIndex) { thumbBgs[0] }
            val badge = badgeColors[doc.type] ?: Pair(ColorAmber.copy(alpha = 0.18f), ColorAmberDark)

            DocCard(
                doc = doc,
                thumbGradient = Brush.linearGradient(bg.map { it }),
                badgeBg = badge.first,
                badgeText = badge.second,
                onClick = { onOpenDoc(doc) }
            )
        }
    }
}

@Composable
private fun DocCard(
    doc: RecentDoc,
    thumbGradient: Brush,
    badgeBg: Color,
    badgeText: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(170.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(13.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, ColorBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column {
            // Thumb
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(88.dp)
                    .background(thumbGradient),
                contentAlignment = Alignment.Center
            ) {
                Text(text = doc.emoji, fontSize = 28.sp)
                // Badge
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp)
                        .clip(RoundedCornerShape(5.dp))
                        .background(badgeBg)
                        .padding(horizontal = 7.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = doc.type.label.uppercase(),
                        fontFamily = JetBrainsMonoFamily,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Medium,
                        color = badgeText,
                        letterSpacing = 0.5.sp,
                    )
                }
                // Pages
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(7.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color.White.copy(alpha = 0.85f))
                        .padding(horizontal = 5.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = doc.quantityLabel.replace("tr", "trang"),
                        fontFamily = JetBrainsMonoFamily,
                        fontSize = 9.sp,
                        color = ColorTextOnLightSecondary,
                    )
                }
            }

            // Info
            Column(modifier = Modifier.padding(horizontal = 11.dp, vertical = 10.dp)) {
                Text(
                    text = doc.course,
                    fontFamily = JetBrainsMonoFamily,
                    fontSize = 9.sp,
                    color = ColorTextOnLightSecondary,
                    letterSpacing = 0.8.sp,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Text(
                    text = doc.title,
                    fontFamily = DmSansFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp,
                    color = ColorTextOnLight,
                    lineHeight = 16.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(7.dp),
                    ) {
                        Text(
                            text = "👁 ${doc.views}",
                            fontFamily = DmSansFamily,
                            fontSize = 10.sp,
                            color = ColorTextOnLightSecondary,
                        )
                        Text(
                            text = if (doc.rating <= 0f) "⭐ Chưa đánh giá" else "⭐ ${doc.rating}",
                            fontFamily = DmSansFamily,
                            fontSize = 10.sp,
                            color = ColorTextOnLightSecondary,
                        )
                    }
                    val context = androidx.compose.ui.platform.LocalContext.current
                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .clip(RoundedCornerShape(7.dp))
                            .background(ColorCream)
                            .clickable {
                                android.widget.Toast.makeText(
                                    context,
                                    "Đã lưu \"${doc.title}\" vào mục yêu thích! 🔖",
                                    android.widget.Toast.LENGTH_SHORT
                                ).show()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Bookmarks,
                            contentDescription = "Lưu",
                            tint = ColorTextOnLightSecondary,
                            modifier = Modifier.size(13.dp)
                        )
                    }
                }
            }
        }
    }
}

// ─── AI Tools Section ─────────────────────────────────────────────────────────

@Composable
private fun AiToolsSection(
    onTabSelected: (HomeTab) -> Unit,
    onOpenStudyAi: () -> Unit,
    onStartQuizSetup: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // Card 1: StudyAI
        AiToolCard(
            tag = "✦  AI POWERED",
            title = "StudyAI",
            description = "Hỏi bất kỳ câu hỏi nào về tài liệu của bạn. AI sẽ giải thích theo cách dễ hiểu nhất.",
            emoji = "🤖",
            isAmber = true,
            onClick = onOpenStudyAi
        )
        // Card 2: Quiz Generator
        AiToolCard(
            tag = "✦  QUIZ ENGINE",
            title = "Tạo Quiz tự động",
            description = "Upload tài liệu — Quiz được tạo trong vài giây. Hỗ trợ trắc nghiệm, điền khuyết, đúng/sai.",
            emoji = "🧩",
            isAmber = false,
            onClick = onStartQuizSetup
        )
    }
}

@Composable
private fun AiToolCard(
    tag: String,
    title: String,
    description: String,
    emoji: String,
    isAmber: Boolean,
    onClick: () -> Unit
) {
    val accentGlow = if (isAmber) ColorAmber else ColorForest

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(ColorInk)
            .drawBehind {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(accentGlow.copy(alpha = 0.18f), Color.Transparent),
                        center = Offset(size.width * 0.9f, size.height * 0.2f),
                        radius = size.width * 0.6f,
                    ),
                    radius = size.width * 0.6f,
                    center = Offset(size.width * 0.9f, size.height * 0.2f),
                )
            }
            .clickable { onClick() }
            .padding(20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = tag,
                    fontFamily = JetBrainsMonoFamily,
                    fontSize = 9.sp,
                    color = if (isAmber) ColorAmber else ColorForestLight,
                    letterSpacing = 1.2.sp,
                    modifier = Modifier.padding(bottom = 7.dp)
                )
                Text(
                    text = title,
                    fontFamily = PlayfairDisplayFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = ColorTextOnDark,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
                Text(
                    text = description,
                    fontFamily = DmSansFamily,
                    fontSize = 12.sp,
                    color = ColorTextOnDarkSecondary,
                    lineHeight = 17.sp,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(ColorTextOnDark.copy(alpha = 0.10f))
                        .border(1.dp, ColorTextOnDark.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                        .clickable { onClick() }
                        .padding(horizontal = 13.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "Thử ngay →",
                        fontFamily = DmSansFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 12.sp,
                        color = ColorTextOnDark,
                    )
                }
            }
            Text(
                text = emoji,
                fontSize = 48.sp,
                modifier = Modifier
                    .padding(start = 8.dp)
                    .graphicsLayer { alpha = 0.15f }
                    .offset(x = 6.dp, y = 8.dp)
            )
        }
    }
}

// ─── Activity Section ─────────────────────────────────────────────────────────

@Composable
private fun ActivitySection(
    items: List<ActivityItem>,
    onTabSelected: (HomeTab) -> Unit,
    onStartQuizSetup: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, ColorBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 13.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Gần đây",
                    fontFamily = PlayfairDisplayFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = ColorTextOnLight,
                )
                Text(
                    text = "Xem tất cả →",
                    fontFamily = DmSansFamily,
                    fontSize = 12.sp,
                    color = ColorForest,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) { onTabSelected(HomeTab.DOCUMENTS) }
                )
            }

            // Activity rows
            items.forEachIndexed { index, item ->
                ActivityRow(
                    item = item,
                    onClick = {
                        if (item.type == ActivityType.QUIZ) {
                            onStartQuizSetup(item.title)
                        } else {
                            onTabSelected(HomeTab.DOCUMENTS)
                        }
                    }
                )
                if (index < items.size - 1) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(ColorBorder)
                    )
                }
            }
        }
    }
}

@Composable
private fun ActivityRow(item: ActivityItem, onClick: () -> Unit) {
    val (iconBg, iconEmoji) = when (item.type) {
        ActivityType.NOTE -> Pair(ColorAmber.copy(alpha = 0.12f), "📝")
        ActivityType.QUIZ -> Pair(ColorForest.copy(alpha = 0.12f), "🧩")
        ActivityType.SLIDE -> Pair(Color(0xFF4A5568).copy(alpha = 0.10f), "📊")
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // Icon
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(9.dp))
                .background(iconBg),
            contentAlignment = Alignment.Center,
        ) {
            Text(text = iconEmoji, fontSize = 16.sp)
        }

        // Info
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.title,
                fontFamily = DmSansFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 13.sp,
                color = ColorTextOnLight,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = item.subtitle,
                fontFamily = DmSansFamily,
                fontSize = 11.sp,
                color = ColorTextOnLightSecondary,
            )
            if (item.progress != null) {
                Spacer(modifier = Modifier.height(5.dp))
                LinearProgressIndicator(
                    progress = { item.progress },
                    modifier = Modifier
                        .width(70.dp)
                        .height(3.5.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = ColorForestLight,
                    trackColor = ColorBorder,
                    strokeCap = StrokeCap.Round,
                )
            }
        }

        // Time
        Text(
            text = item.time,
            fontFamily = JetBrainsMonoFamily,
            fontSize = 10.sp,
            color = ColorTextOnLightSecondary,
        )
    }
}

// ─── Topic Tags Section ───────────────────────────────────────────────────────

@Composable
private fun TopicTagsSection(
    tags: List<TopicTag>,
    onTabSelected: (HomeTab) -> Unit,
    modifier: Modifier = Modifier
) {
    androidx.compose.foundation.layout.FlowRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        tags.forEach { tag ->
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(100.dp))
                    .background(Color.White)
                    .border(1.5.dp, ColorBorder, RoundedCornerShape(100.dp))
                    .clickable { onTabSelected(HomeTab.DOCUMENTS) }
                    .padding(horizontal = 13.dp, vertical = 7.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(
                        text = tag.label,
                        fontFamily = DmSansFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 12.sp,
                        color = ColorTextOnLightSecondary,
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(100.dp))
                            .background(ColorCream)
                            .padding(horizontal = 6.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = "${tag.count}",
                            fontFamily = JetBrainsMonoFamily,
                            fontSize = 10.sp,
                            color = ColorTextOnLightSecondary,
                        )
                    }
                }
            }
        }
    }
}

// ─── Preview ─────────────────────────────────────────────────────────────────

private class PreviewAuthRepository : com.example.eduvault.domain.repository.AuthRepository {
    override suspend fun login(email: String, password: String): Result<com.example.eduvault.domain.model.User> = Result.failure(Exception())
    override suspend fun register(fullName: String, email: String, password: String): Result<com.example.eduvault.domain.model.User> = Result.failure(Exception())
    override suspend fun sendPasswordResetEmail(email: String): Result<Unit> = Result.failure(Exception())
    override suspend fun verifyPasswordResetCode(code: String): Result<String> = Result.success("user@example.com")
    override suspend fun confirmPasswordReset(oobCode: String, newPassword: String): Result<Unit> = Result.success(Unit)
    override suspend fun getCurrentUser(): Result<com.example.eduvault.domain.model.User?> = Result.success(null)
    override suspend fun updateProfile(fullName: String, university: String, avatarUrl: String): Result<com.example.eduvault.domain.model.User> = Result.success(com.example.eduvault.domain.model.User("preview_uid", fullName, "preview@example.com"))
    override suspend fun addCredits(amount: Int): Result<com.example.eduvault.domain.model.User> = Result.success(com.example.eduvault.domain.model.User("preview_uid", "Preview User", "preview@example.com"))
    override suspend fun consumeCredit(): Result<com.example.eduvault.domain.model.User> = Result.success(com.example.eduvault.domain.model.User("preview_uid", "Preview User", "preview@example.com"))
    override suspend fun unlockDocument(docId: String): Result<com.example.eduvault.domain.model.User> = Result.success(com.example.eduvault.domain.model.User("preview_uid", "Preview User", "preview@example.com"))
    override suspend fun getQuizSets(): Result<List<com.example.eduvault.feature.quiz.ui.QuizSet>> = Result.success(emptyList())
    override suspend fun getQuizQuestions(quizId: String): Result<List<com.example.eduvault.feature.quiz.ui.QuizQuestion>> = Result.success(emptyList())
    override suspend fun getAllUsers(): Result<List<com.example.eduvault.domain.model.User>> = Result.success(emptyList())
    override suspend fun updateUserQuizStats(score: Float, xpEarned: Int): Result<com.example.eduvault.domain.model.User> = Result.success(com.example.eduvault.domain.model.User("preview_uid", "Preview User", "preview@example.com"))
    override suspend fun toggleSaveDocument(docId: String): Result<com.example.eduvault.domain.model.User> = Result.success(com.example.eduvault.domain.model.User("preview_uid", "Preview User", "preview@example.com"))
    override suspend fun loginWithGoogle(idToken: String): Result<com.example.eduvault.domain.model.User> = Result.success(com.example.eduvault.domain.model.User("preview_uid", "Preview User", "preview@example.com"))
    override suspend fun changePassword(currentPassword: String, newPassword: String): Result<Unit> = Result.success(Unit)
    override suspend fun saveQuizSet(quizSet: com.example.eduvault.feature.quiz.ui.QuizSet, questions: List<com.example.eduvault.feature.quiz.ui.QuizQuestion>): Result<Unit> = Result.success(Unit)
    override fun logout() {}
}

private class PreviewDocumentRepository : com.example.eduvault.domain.repository.DocumentRepository {
    override suspend fun uploadUserDocument(title: String, subjectCode: String, docType: String, fileUri: String): Result<Unit> = Result.success(Unit)
    override suspend fun getDocuments(): Result<List<com.example.eduvault.domain.model.LibraryDocument>> = Result.success(emptyList())
    override suspend fun reportDocument(docId: String): Result<Unit> = Result.success(Unit)
    override suspend fun getCachedDocViewerContent(docId: String): Result<com.example.eduvault.domain.model.DocViewerTabsContent?> = Result.success(null)
    override suspend fun saveDocViewerContent(docId: String, content: com.example.eduvault.domain.model.DocViewerTabsContent): Result<Unit> = Result.success(Unit)
    override suspend fun deleteDocument(docId: String): Result<Unit> = Result.success(Unit)
}

private class PreviewAiRepository : com.example.eduvault.domain.repository.AiRepository {
    override suspend fun generateQuizFromDocument(title: String, type: String, content: String, count: Int, format: String): Result<List<com.example.eduvault.feature.quiz.ui.QuizQuestion>> = Result.success(emptyList())
    override suspend fun generateSummaryFromDocument(title: String, type: String, content: String): Result<String> = Result.success("")
    override suspend fun askAiAboutDocument(title: String, type: String, content: String, question: String): Result<String> = Result.success("")
    override suspend fun askGeneralStudyAi(question: String, history: List<com.example.eduvault.domain.model.AiChatMessage>): Result<String> = Result.success("")
    override suspend fun generateDocViewerContent(title: String, courseCode: String, type: String): Result<com.example.eduvault.domain.model.DocViewerTabsContent> =
        Result.success(com.example.eduvault.domain.model.DocViewerTabsContent("", "", ""))
}

private class PreviewNotificationRepository : com.example.eduvault.domain.repository.NotificationRepository {
    override suspend fun getNotifications(userId: String): Result<List<com.example.eduvault.domain.model.EduNotification>> = Result.success(emptyList())
    override suspend fun ensureDefaultNotifications(userId: String): Result<Unit> = Result.success(Unit)
    override suspend fun addNotification(userId: String, title: String, content: String, type: com.example.eduvault.domain.model.NotificationType): Result<Unit> = Result.success(Unit)
    override suspend fun markAsRead(id: String): Result<Unit> = Result.success(Unit)
}

@Preview(name = "HomeScreen — Phone", showBackground = true, widthDp = 390, heightDp = 844)
@SuppressLint("ViewModelConstructorInComposable")
@Composable
private fun HomeScreenPreview() {
    EduVaultTheme(darkTheme = false) {
        HomeScaffold(
            uiState = HomeUiState(),
            recentDocs = emptyList(),
            activityItems = emptyList(),
            topicTags = emptyList(),
            quizViewModel = QuizViewModel(
                PreviewAuthRepository(),
                com.example.eduvault.domain.usecase.GetPublishedDocumentsUseCase(PreviewDocumentRepository()),
                PreviewDocumentRepository(),
                PreviewAiRepository(),
                PreviewNotificationRepository()
            ),
            onTabSelected = {},
            onUploadClick = {},
            onOpenDoc = {},
            onOpenFlashcards = {},
            onOpenStudyAi = {},
            onOpenNotes = {},
            onNotificationClick = {},
            onStartQuizSetup = {},
        )
    }
}



// ─── Flashcard Dialog (Simulated Study Tool) ────────────────────────────────────
data class FlashcardItem(val term: String, val definition: String)
data class FlashcardSet(
    val id: String,
    val title: String,
    val cardCount: Int,
    val cards: List<FlashcardItem>,
    val authorId: String,
    val flashcardSetID: String
)

@Composable
fun FlashcardDialog(onDismiss: () -> Unit) {
    var currentIndex by remember { mutableIntStateOf(0) }
    var isFlipped by remember { mutableStateOf(false) }

    var selectedSet by remember { mutableStateOf<FlashcardSet?>(null) }
    var flashcardSets by remember { mutableStateOf<List<FlashcardSet>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }



    LaunchedEffect(Unit) {
        try {
            isLoading = true
            val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
            val snapshot = db.collection("flashcards").get().await()
            val sets = snapshot.documents.mapNotNull { doc ->
                val id = doc.id
                val title = doc.getString("title") ?: "Bộ thẻ chưa đặt tên"
                val authorId = doc.getString("authorId") ?: ""
                val flashcardSetID = doc.getString("flashcardSetID") ?: doc.getString("documentID") ?: id
                
                val rawCards = doc.get("cards") as? List<Map<String, Any>> ?: emptyList()
                val cardsList = rawCards.map { cardMap ->
                    FlashcardItem(
                        term = cardMap["term"] as? String ?: "",
                        definition = cardMap["definition"] as? String ?: ""
                    )
                }
                
                val cardCount = doc.getLong("cardCount")?.toInt() ?: cardsList.size
                
                FlashcardSet(
                    id = id,
                    title = title,
                    cardCount = cardCount,
                    cards = cardsList,
                    authorId = authorId,
                    flashcardSetID = flashcardSetID
                )
            }
            flashcardSets = sets
            isLoading = false
        } catch (e: Exception) {
            errorMessage = e.localizedMessage
            flashcardSets = emptyList()
            isLoading = false
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f))
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) { onDismiss() },
            contentAlignment = Alignment.Center
        ) {
            androidx.compose.material3.Card(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) { /* chặn click trượt */ }
                    .padding(vertical = 24.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (selectedSet == null) "Bộ thẻ ghi nhớ 🃏" else selectedSet!!.title,
                            fontFamily = PlayfairDisplayFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = ColorInk,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        Icon(
                            imageVector = Icons.Outlined.Cancel,
                            contentDescription = "Đóng",
                            tint = ColorTextOnLightSecondary,
                            modifier = Modifier
                                .size(20.dp)
                                .clickable { onDismiss() }
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    if (selectedSet == null) {
                        Text(
                            text = "Chọn một bộ thẻ để ôn luyện kiến thức",
                            fontFamily = DmSansFamily,
                            fontSize = 11.sp,
                            color = ColorTextOnLightSecondary
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        if (isLoading) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                androidx.compose.material3.CircularProgressIndicator(
                                    color = ColorAmber
                                )
                            }
                        } else if (flashcardSets.isEmpty()) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(ColorAmber.copy(alpha = 0.1f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = "🃏", fontSize = 32.sp)
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "Chưa có bộ thẻ ghi nhớ nào",
                                    fontFamily = DmSansFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = ColorInk
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Bạn chưa tạo bộ flashcard nào. Hãy tải lên tài liệu học tập hoặc ôn luyện Quiz để hệ thống tự động tạo các bộ thẻ ôn tập cho bạn nhé!",
                                    fontFamily = DmSansFamily,
                                    fontSize = 11.sp,
                                    color = ColorTextOnLightSecondary,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                    modifier = Modifier.padding(horizontal = 16.dp)
                                )
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 280.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                items(flashcardSets) { set ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(ColorCream)
                                            .border(1.dp, ColorBorder, RoundedCornerShape(12.dp))
                                            .clickable {
                                                selectedSet = set
                                                currentIndex = 0
                                                isFlipped = false
                                            }
                                            .padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(ColorAmber.copy(alpha = 0.15f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(text = "🃏", fontSize = 16.sp)
                                        }

                                        Spacer(modifier = Modifier.width(12.dp))

                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = set.title,
                                                fontFamily = DmSansFamily,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp,
                                                color = ColorInk,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = "${set.cardCount} thẻ ghi nhớ · ID: ${set.flashcardSetID}",
                                                fontFamily = DmSansFamily,
                                                fontSize = 11.sp,
                                                color = ColorTextOnLightSecondary
                                            )
                                        }

                                        Icon(
                                            imageVector = Icons.AutoMirrored.Outlined.ArrowForward,
                                            contentDescription = "Học",
                                            tint = ColorAmberDark,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        val currentSet = selectedSet!!
                        val cards = currentSet.cards

                        if (cards.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "Bộ thẻ này không có thẻ nào!",
                                        fontFamily = DmSansFamily,
                                        fontSize = 13.sp,
                                        color = ColorTextOnLightSecondary
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(ColorInk)
                                            .clickable { selectedSet = null }
                                            .padding(horizontal = 14.dp, vertical = 8.dp)
                                    ) {
                                        Text(
                                            text = "Quay lại",
                                            fontFamily = DmSansFamily,
                                            fontSize = 12.sp,
                                            color = Color.White
                                        )
                                    }
                                }
                            }
                        } else {
                            Text(
                                text = "Thẻ số ${currentIndex + 1}/${cards.size} · Nhấp vào thẻ để lật mặt",
                                fontFamily = DmSansFamily,
                                fontSize = 11.sp,
                                color = ColorTextOnLightSecondary
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(
                                        if (!isFlipped) {
                                            Brush.verticalGradient(listOf(ColorAmber.copy(alpha = 0.12f), ColorAmber.copy(alpha = 0.04f)))
                                        } else {
                                            Brush.verticalGradient(listOf(ColorForest.copy(alpha = 0.12f), ColorForest.copy(alpha = 0.04f)))
                                        }
                                    )
                                    .border(
                                        2.dp,
                                        if (!isFlipped) ColorAmber else ColorForest,
                                        RoundedCornerShape(16.dp)
                                    )
                                    .clickable { isFlipped = !isFlipped }
                                    .padding(20.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = if (!isFlipped) "MẶT TRƯỚC (THUẬT NGỮ)" else "MẶT SAU (ĐỊNH NGHĨA)",
                                        fontFamily = JetBrainsMonoFamily,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (!isFlipped) ColorAmberDark else ColorForest,
                                        letterSpacing = 1.sp
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .verticalScroll(rememberScrollState()),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = if (!isFlipped) cards[currentIndex].term else cards[currentIndex].definition,
                                            fontFamily = PlayfairDisplayFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = if (!isFlipped) 20.sp else 13.sp,
                                            color = ColorInk,
                                            textAlign = TextAlign.Center,
                                            lineHeight = 20.sp
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .border(1.dp, ColorBorder, RoundedCornerShape(8.dp))
                                        .background(ColorCream)
                                        .clickable {
                                            isFlipped = false
                                            currentIndex = (currentIndex - 1 + cards.size) % cards.size
                                        }
                                        .padding(horizontal = 14.dp, vertical = 8.dp)
                                ) {
                                    Text(
                                        text = "← Trước",
                                        fontFamily = DmSansFamily,
                                        fontSize = 12.sp,
                                        color = ColorTextOnLight
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .border(1.dp, ColorBorder, RoundedCornerShape(8.dp))
                                        .background(Color.White)
                                        .clickable {
                                            selectedSet = null
                                        }
                                        .padding(horizontal = 14.dp, vertical = 8.dp)
                                ) {
                                    Text(
                                        text = "Danh sách",
                                        fontFamily = DmSansFamily,
                                        fontSize = 12.sp,
                                        color = ColorTextOnLightSecondary
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(ColorInk)
                                        .clickable {
                                            isFlipped = false
                                            currentIndex = (currentIndex + 1) % cards.size
                                        }
                                        .padding(horizontal = 14.dp, vertical = 8.dp)
                                ) {
                                    Text(
                                        text = "Tiếp theo →",
                                        fontFamily = DmSansFamily,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ─── StudyAI Dialog (Simulated Companion Bot) ───────────────────────────────────
@Composable
fun StudyAiDialog(
    onDismiss: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    var messageQuery by remember { mutableStateOf("") }
    val chatMessages by viewModel.chatMessages.collectAsState()
    val isAiTyping by viewModel.isAiTyping.collectAsState()

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f))
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) { onDismiss() },
            contentAlignment = Alignment.Center
        ) {
            androidx.compose.material3.Card(
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) { /* chặn click trượt */ },
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "🤖", fontSize = 22.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text(
                                    text = "StudyAI Companion",
                                    fontFamily = PlayfairDisplayFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = ColorInk
                                )
                                Text(
                                    text = if (isAiTyping) "StudyAI đang soạn câu trả lời..." else "Trợ lý AI đang hoạt động",
                                    fontFamily = DmSansFamily,
                                    fontSize = 10.sp,
                                    color = if (isAiTyping) ColorAmberDark else ColorForest
                                )
                            }
                        }
                        Icon(
                            imageVector = Icons.Outlined.Cancel,
                            contentDescription = "Đóng",
                            tint = ColorTextOnLightSecondary,
                            modifier = Modifier
                                .size(20.dp)
                                .clickable { onDismiss() }
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Chat messages
                    val scrollState = rememberScrollState()
                    LaunchedEffect(chatMessages.size, isAiTyping) {
                        scrollState.animateScrollTo(scrollState.maxValue)
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(280.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(ColorCream)
                            .border(1.dp, ColorBorder, RoundedCornerShape(12.dp))
                            .padding(12.dp)
                            .verticalScroll(scrollState),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        chatMessages.forEach { msg ->
                            val isUser = msg.isUser
                            val text = msg.text
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(0.82f)
                                        .clip(
                                            RoundedCornerShape(
                                                topStart = 12.dp,
                                                topEnd = 12.dp,
                                                bottomStart = if (isUser) 12.dp else 0.dp,
                                                bottomEnd = if (isUser) 0.dp else 12.dp
                                            )
                                        )
                                        .background(if (isUser) ColorAmber else ColorInk)
                                        .padding(10.dp)
                                ) {
                                    if (isUser) {
                                        Text(
                                            text = text,
                                            fontFamily = DmSansFamily,
                                            fontSize = 12.sp,
                                            color = ColorInk,
                                            lineHeight = 16.sp
                                        )
                                    } else {
                                        Text(
                                            text = parseMarkdownToAnnotatedString(text),
                                            fontFamily = DmSansFamily,
                                            fontSize = 12.sp,
                                            color = Color.White,
                                            lineHeight = 16.sp
                                        )
                                    }
                                }
                            }
                        }

                        // Bong bóng AI đang suy nghĩ (typing indicator)
                        if (isAiTyping) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Start
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(0.82f)
                                        .clip(
                                            RoundedCornerShape(
                                                topStart = 12.dp,
                                                topEnd = 12.dp,
                                                bottomStart = 0.dp,
                                                bottomEnd = 12.dp
                                            )
                                        )
                                        .background(ColorInk)
                                        .padding(10.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        androidx.compose.material3.CircularProgressIndicator(
                                            modifier = Modifier.size(12.dp),
                                            color = ColorAmber,
                                            strokeWidth = 1.5.dp
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "StudyAI đang suy nghĩ...",
                                            fontFamily = DmSansFamily,
                                            fontSize = 11.sp,
                                            color = Color.White.copy(alpha = 0.8f),
                                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Suggestions
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val suggestions = listOf("Tóm tắt môn học", "Giải thích SWOT")
                        suggestions.forEach { suggestion ->
                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(ColorCream)
                                    .border(1.dp, ColorBorder, CircleShape)
                                    .clickable(enabled = !isAiTyping) {
                                        viewModel.sendChatMessage(suggestion)
                                    }
                                    .padding(horizontal = 10.dp, vertical = 5.dp)
                            ) {
                                Text(
                                    text = suggestion,
                                    fontFamily = DmSansFamily,
                                    fontSize = 10.5.sp,
                                    color = if (isAiTyping) ColorTextOnLightSecondary.copy(alpha = 0.5f) else ColorTextOnLightSecondary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Query box
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = messageQuery,
                            onValueChange = { messageQuery = it },
                            placeholder = { Text("Hỏi AI bất kỳ điều gì...", fontSize = 12.sp) },
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = ColorAmber,
                                unfocusedBorderColor = ColorBorder
                            ),
                            enabled = !isAiTyping,
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isAiTyping || messageQuery.trim().isEmpty()) ColorAmber.copy(alpha = 0.5f) else ColorAmber)
                                .clickable(enabled = !isAiTyping && messageQuery.trim().isNotEmpty()) {
                                    viewModel.sendChatMessage(messageQuery)
                                    messageQuery = ""
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "✈️", fontSize = 16.sp)
                        }
                    }
                }
            }
        }
    }
}

// ─── Notifications Dialog (Warm Editorial Tech) ──────────────────────────────────
@Composable
fun NotificationsDialog(
    onDismiss: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val notifications by viewModel.notifications.collectAsState()

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f))
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) { onDismiss() },
            contentAlignment = Alignment.Center
        ) {
            androidx.compose.material3.Card(
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) { /* chặn click trượt */ }
                    .padding(vertical = 24.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    // Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "🔔", fontSize = 20.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Thông báo mới nhất",
                                fontFamily = PlayfairDisplayFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = ColorInk
                            )
                        }
                        Icon(
                            imageVector = Icons.Outlined.Cancel,
                            contentDescription = "Đóng",
                            tint = ColorTextOnLightSecondary,
                            modifier = Modifier
                                .size(20.dp)
                                .clickable { onDismiss() }
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Notifications list
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(280.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(notifications) { msg ->
                            val title = msg.title
                            val content = msg.content
                            val time = msg.getFormattedTime()
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(ColorCream)
                                    .border(1.dp, ColorBorder, RoundedCornerShape(12.dp))
                                    .padding(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        if (!msg.isRead) {
                                            Box(
                                                modifier = Modifier
                                                    .size(6.dp)
                                                    .clip(CircleShape)
                                                    .background(ColorAmberDark)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                        }
                                        Text(
                                            text = title,
                                            fontFamily = DmSansFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = ColorInk
                                        )
                                    }
                                    Text(
                                        text = time,
                                        fontFamily = JetBrainsMonoFamily,
                                        fontSize = 9.sp,
                                        color = ColorTextOnLightSecondary
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = content,
                                    fontFamily = DmSansFamily,
                                    fontSize = 11.sp,
                                    color = ColorTextOnLightSecondary,
                                    lineHeight = 15.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Dismiss button
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(ColorInk)
                            .clickable { onDismiss() }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Đóng thông báo",
                            fontFamily = DmSansFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

// ─── Create Quiz Setup Dialog (Warm Editorial Tech) ──────────────────────────────────
@Composable
fun CreateQuizSetupDialog(
    quizViewModel: QuizViewModel,
    presetTitle: String,
    onDismiss: () -> Unit,
    onConfirm: (docTitle: String, questionCount: Int, difficulty: Difficulty, format: String) -> Unit
) {
    val availableDocs by quizViewModel.availableDocs.collectAsStateWithLifecycle()

    val docTitles = remember(availableDocs) {
        availableDocs.map { it.title }
    }

    // Pre-select document based on preset title or default to first
    var selectedDoc by remember(docTitles, presetTitle) {
        mutableStateOf(
            docTitles.find { it.contains(presetTitle, ignoreCase = true) || presetTitle.contains(it, ignoreCase = true) }
                ?: docTitles.firstOrNull() ?: ""
        )
    }
    var selectedCount by remember { mutableIntStateOf(5) }
    var selectedDifficulty by remember { mutableStateOf(Difficulty.EASY) }
    val formatOptions = listOf("Trắc nghiệm", "Đúng / Sai", "Điền khuyết")
    var selectedFormat by remember { mutableStateOf("Trắc nghiệm") }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f))
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) { onDismiss() },
            contentAlignment = Alignment.Center
        ) {
            androidx.compose.material3.Card(
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) { /* chặn click trượt */ }
                    .padding(vertical = 24.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(20.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    // Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "✦", fontSize = 20.sp, color = ColorAmber)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Tùy chỉnh bộ đề Quiz",
                                fontFamily = PlayfairDisplayFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = ColorInk
                            )
                        }
                        Icon(
                            imageVector = Icons.Outlined.Cancel,
                            contentDescription = "Đóng",
                            tint = ColorTextOnLightSecondary,
                            modifier = Modifier
                                .size(20.dp)
                                .clickable { onDismiss() }
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // 1. Select Document
                    Text(
                        text = "1. CHỌN TÀI LIỆU ÔN TẬP",
                        fontFamily = JetBrainsMonoFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 9.sp,
                        color = ColorAmberDark,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        docTitles.forEach { doc ->
                            val isSelected = doc == selectedDoc
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isSelected) ColorAmber.copy(alpha = 0.08f) else ColorCream)
                                    .border(
                                        width = if (isSelected) 1.5.dp else 1.dp,
                                        color = if (isSelected) ColorAmber else ColorBorder,
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                    .clickable { selectedDoc = doc }
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(16.dp)
                                        .clip(CircleShape)
                                        .background(if (isSelected) ColorAmber else Color.White)
                                        .border(1.dp, if (isSelected) ColorAmber else ColorBorder, CircleShape)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = doc,
                                    fontFamily = DmSansFamily,
                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                                    fontSize = 11.5.sp,
                                    color = ColorInk,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // 2. Select Question Count
                    Text(
                        text = "2. SỐ LƯỢNG CÂU HỎI",
                        fontFamily = JetBrainsMonoFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 9.sp,
                        color = ColorAmberDark,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val countOptions = listOf(5, 10, 15, 20)
                        countOptions.forEach { count ->
                            val isSelected = count == selectedCount
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) ColorInk else ColorCream)
                                    .border(1.dp, if (isSelected) ColorInk else ColorBorder, RoundedCornerShape(8.dp))
                                    .clickable { selectedCount = count }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "$count câu",
                                    fontFamily = DmSansFamily,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) ColorAmber else ColorTextOnLightSecondary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // 3. Select Difficulty
                    Text(
                        text = "3. ĐỘ KHÓ BỘ ĐỀ",
                        fontFamily = JetBrainsMonoFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 9.sp,
                        color = ColorAmberDark,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val difficulties = listOf(Difficulty.EASY, Difficulty.MEDIUM, Difficulty.HARD)
                        difficulties.forEach { diff ->
                            val isSelected = diff == selectedDifficulty
                            val activeBg = when (diff) {
                                Difficulty.EASY -> ColorForest
                                Difficulty.MEDIUM -> ColorAmber
                                Difficulty.HARD -> ColorError
                            }
                            val activeText = if (diff == Difficulty.MEDIUM) ColorInk else Color.White

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) activeBg else ColorCream)
                                    .border(1.dp, if (isSelected) activeBg else ColorBorder, RoundedCornerShape(8.dp))
                                    .clickable { selectedDifficulty = diff }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = diff.label,
                                    fontFamily = DmSansFamily,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) activeText else ColorTextOnLightSecondary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // 4. Select Format
                    Text(
                        text = "4. HÌNH THỨC CÂU HỎI",
                        fontFamily = JetBrainsMonoFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 9.sp,
                        color = ColorAmberDark,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        formatOptions.forEach { format ->
                            val isSelected = format == selectedFormat
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) ColorInk else ColorCream)
                                    .border(1.dp, if (isSelected) ColorInk else ColorBorder, RoundedCornerShape(8.dp))
                                    .clickable { selectedFormat = format }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = format,
                                    fontFamily = DmSansFamily,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) ColorAmber else ColorTextOnLightSecondary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .border(1.5.dp, ColorBorder, RoundedCornerShape(10.dp))
                                .background(Color.White)
                                .clickable { onDismiss() }
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Hủy bỏ",
                                fontFamily = DmSansFamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp,
                                color = ColorTextOnLightSecondary
                            )
                        }

                        Box(
                            modifier = Modifier
                                .weight(1.3f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(ColorInk)
                                .clickable { onConfirm(selectedDoc, selectedCount, selectedDifficulty, selectedFormat) }
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Bắt đầu tạo bộ đề ✦",
                                fontFamily = DmSansFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = ColorAmber
                            )
                        }
                    }
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// GUEST LOGIN PROMPT DIALOG — Hiển thị khi Guest cố dùng tính năng thành viên
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
fun GuestLoginPromptDialog(
    onDismiss: () -> Unit,
    onNavigateToLogin: () -> Unit,
    onNavigateToRegister: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.88f)
                .clip(RoundedCornerShape(20.dp))
                .background(ColorPaper)
                .border(1.dp, ColorBorder, RoundedCornerShape(20.dp))
                .padding(24.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(text = "🔐", fontSize = 36.sp)

                Text(
                    text = "Tính năng dành cho thành viên",
                    fontFamily = PlayfairDisplayFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = ColorInk,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "Đăng nhập để tải tài liệu lên, nhận Credit\nvà lưu tiến trình học tập của bạn.",
                    fontFamily = DmSansFamily,
                    fontSize = 13.sp,
                    color = ColorTextOnLightSecondary,
                    textAlign = TextAlign.Center,
                    lineHeight = 19.sp
                )

                Spacer(Modifier.height(4.dp))

                // Login button
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            Brush.horizontalGradient(listOf(ColorAmber, ColorAmberDark))
                        )
                        .clickable {
                            onDismiss()
                            onNavigateToLogin()
                        }
                        .padding(vertical = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Đăng nhập",
                        fontFamily = DmSansFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = ColorInk
                    )
                }

                // Register button
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.5.dp, ColorInk.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                        .clickable {
                            onDismiss()
                            onNavigateToRegister()
                        }
                        .padding(vertical = 13.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Tạo tài khoản miễn phí",
                        fontFamily = DmSansFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp,
                        color = ColorInk
                    )
                }

                androidx.compose.material3.TextButton(onClick = onDismiss) {
                    Text(
                        text = "Để sau",
                        fontFamily = DmSansFamily,
                        fontSize = 13.sp,
                        color = ColorTextOnLightSecondary
                    )
                }
            }
        }
    }
}

private fun calculatePrice(fileSizeBytes: Long, quantityLabel: String): Int {
    if (fileSizeBytes > 0L) {
        val sizeInMb = fileSizeBytes / (1024.0 * 1024.0)
        return when {
            sizeInMb <= 2.0 -> 1
            sizeInMb <= 5.0 -> 2
            else -> 3
        }
    }
    val cleanStr = quantityLabel.replace(",", ".").trim()
    if (cleanStr.endsWith("MB", ignoreCase = true)) {
        val sizeInMb = cleanStr.replace("MB", "", ignoreCase = true).trim().toDoubleOrNull() ?: 0.0
        return when {
            sizeInMb <= 2.0 -> 1
            sizeInMb <= 5.0 -> 2
            else -> 3
        }
    }
    return 1
}
