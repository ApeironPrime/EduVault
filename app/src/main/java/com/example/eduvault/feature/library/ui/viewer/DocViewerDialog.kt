package com.example.eduvault.feature.library.ui.viewer

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.eduvault.core.theme.ColorAmber
import com.example.eduvault.core.theme.ColorForest
import com.example.eduvault.core.theme.ColorInk
import com.example.eduvault.core.theme.ColorPaper
import com.example.eduvault.core.theme.ColorTextOnLightSecondary
import com.example.eduvault.core.theme.DmSansFamily
import com.example.eduvault.core.theme.JetBrainsMonoFamily
import com.example.eduvault.core.theme.PlayfairDisplayFamily
import com.example.eduvault.domain.model.DocReaderState
import com.example.eduvault.domain.model.DocViewerTabsContent
import com.example.eduvault.feature.library.ui.LibraryDoc
import com.example.eduvault.feature.library.ui.LibraryViewModel
import kotlinx.coroutines.launch
import java.io.File

@Composable
fun DocViewerDialog(
    doc: LibraryDoc,
    readerState: DocReaderState = DocReaderState.Unlocked,
    activeContent: DocViewerTabsContent? = null,
    isLoadingContent: Boolean = false,
    contentError: String? = null,
    onRetryLoadContent: () -> Unit = {},
    onDismiss: () -> Unit,
    onReportClick: () -> Unit = {},
    onNavigateToLogin: () -> Unit = {},
    onNavigateToRegister: () -> Unit = {},
    onUnlockWithCredit: () -> Unit = {},
    onGenerateSummary: (suspend () -> Result<String>)? = null,
    onNoteClick: ((LibraryDoc) -> Unit)? = null,
    onDeleteDoc: ((LibraryDoc) -> Unit)? = null,
    onDownloadPdf: (suspend (String, File) -> Result<File>)? = null,
    currentUserId: String = "",
    currentUserRole: String = "user"
) {
    val libraryViewModel: LibraryViewModel = hiltViewModel()
    val downloadPdfAction: suspend (String, File) -> Result<File> = onDownloadPdf ?: { url, file ->
        libraryViewModel.downloadDocumentPdf(url, file)
    }
    val state = readerState
    val isFullyUnlocked = state is DocReaderState.Unlocked
    val coroutineScope = rememberCoroutineScope()

    var fontSize by remember { mutableIntStateOf(14) }

    var aiSummaryState by remember { mutableStateOf<String?>(null) }
    var isSummarizing by remember { mutableStateOf(false) }

    val chapters: List<Pair<String, String>> = remember(activeContent, doc) {
        val content = activeContent
        if (content != null) {
            listOf(
                "📖 Nội dung" to content.content,
                "💡 Trọng tâm" to content.keyPoints,
                "✏️ Luyện tập" to content.practice,
                "✨ Smart Summary" to "",
                "💬 Hỏi đáp AI" to ""
            )
        } else {
            listOf(
                "📖 Nội dung" to """
                    Tài liệu: ${doc.title}
                    Mã kỹ năng: ${doc.courseCode}
                    Phân loại: ${doc.type.label}
                    Định dạng: ${doc.type.label}
                """.trimIndent(),
                "💡 Trọng tâm" to "Đang chờ tải nội dung trọng tâm...",
                "✏️ Luyện tập" to "Đang chờ tải bài luyện tập...",
                "✨ Smart Summary" to "",
                "💬 Hỏi đáp AI" to ""
            )
        }
    }

    val chipLabels = remember(chapters) {
        chapters.map { it.first }
    }
    var selectedChip by remember { mutableIntStateOf(0) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.6f))
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) { onDismiss() }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) { /* block click outside */ }
            ) {
                // Header
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                listOf(Color(0xFF151922), Color(0xFF1E2432))
                            )
                        )
                        .padding(horizontal = 16.dp, vertical = 14.dp)
                ) {
                    // Row 1: Course code chip + Font controls + Close
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFF2E2416))
                                .border(1.dp, Color(0xFFC48B36), RoundedCornerShape(6.dp))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = doc.courseCode,
                                fontFamily = JetBrainsMonoFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = Color(0xFFE5A642)
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Font size controls
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "A-",
                                    fontFamily = DmSansFamily,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 13.sp,
                                    color = Color.White.copy(alpha = 0.8f),
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .clickable { if (fontSize > 10) fontSize-- }
                                        .padding(horizontal = 4.dp, vertical = 2.dp)
                                )
                                Text(
                                    text = "${fontSize}sp",
                                    fontFamily = JetBrainsMonoFamily,
                                    fontSize = 12.sp,
                                    color = Color.White.copy(alpha = 0.6f)
                                )
                                Text(
                                    text = "A+",
                                    fontFamily = DmSansFamily,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 13.sp,
                                    color = Color.White.copy(alpha = 0.8f),
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .clickable { if (fontSize < 22) fontSize++ }
                                        .padding(horizontal = 4.dp, vertical = 2.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(4.dp))

                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.12f))
                                    .clickable { onDismiss() },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Đóng",
                                    tint = Color.White.copy(alpha = 0.85f),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Row 2: Title
                    Text(
                        text = doc.title,
                        fontFamily = PlayfairDisplayFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color.White,
                        lineHeight = 24.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "${doc.type.label} • ${doc.quantityLabel}",
                        fontFamily = DmSansFamily,
                        fontSize = 11.5.sp,
                        color = Color.White.copy(alpha = 0.75f)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Row 4: Tab chips
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(chipLabels.size) { index ->
                            val isSelected = selectedChip == index
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(
                                        if (isSelected) Color(0xFFE5A642) else Color.White.copy(alpha = 0.05f)
                                    )
                                    .border(
                                        width = 1.dp,
                                        color = if (isSelected) Color(0xFFE5A642) else Color.White.copy(alpha = 0.15f),
                                        shape = RoundedCornerShape(20.dp)
                                    )
                                    .clickable { selectedChip = index }
                                    .padding(horizontal = 14.dp, vertical = 7.dp)
                            ) {
                                Text(
                                    text = chipLabels[index],
                                    fontFamily = DmSansFamily,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 12.sp,
                                    color = if (isSelected) Color(0xFF1E1E1E) else Color.White.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }
                }

                // Content Area
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .background(ColorPaper)
                ) {
                    val index = selectedChip
                    val context = LocalContext.current
                    if (index in chapters.indices) {
                        val (sectionTitle, sectionBody) = chapters[index]
                        val localFolder = context.getExternalFilesDir("uploaded_docs") ?: File(context.filesDir, "uploaded_docs")
                        val localFile = File(localFolder, doc.fileName)
                        val isPdf = sectionTitle == "📖 Nội dung" &&
                                doc.fileName.endsWith(".pdf", ignoreCase = true) &&
                                !isLoadingContent &&
                                contentError == null

                        if (isPdf) {
                            PdfViewer(
                                file = localFile,
                                downloadUrl = doc.downloadUrl,
                                modifier = Modifier.fillMaxSize(),
                                onDownloadFile = downloadPdfAction
                            )
                        } else {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .verticalScroll(rememberScrollState())
                                    .padding(horizontal = 16.dp, vertical = 14.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .width(4.dp)
                                            .height(20.dp)
                                            .clip(RoundedCornerShape(1.dp))
                                            .background(Color(0xFFC48B36))
                                    )
                                    Text(
                                        text = sectionTitle,
                                        fontFamily = PlayfairDisplayFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = Color(0xFFC48B36)
                                    )
                                }

                                Spacer(Modifier.height(12.dp))

                                when {
                                    index > 0 && sectionTitle !in listOf("✨ Smart Summary", "💬 Hỏi đáp AI") && !isFullyUnlocked -> {
                                        LockedChapterOverlay(
                                            readerState = state,
                                            onNavigateToLogin = onNavigateToLogin,
                                            onNavigateToRegister = onNavigateToRegister,
                                            onUnlockWithCredit = onUnlockWithCredit
                                        )
                                    }
                                    sectionTitle == "✨ Smart Summary" -> {
                                        AISummaryContent(
                                            summary = aiSummaryState,
                                            isSummarizing = isSummarizing,
                                            onGenerateClick = {
                                                val generator = onGenerateSummary
                                                if (generator != null && !isSummarizing) {
                                                    isSummarizing = true
                                                    coroutineScope.launch {
                                                        generator().onSuccess { res ->
                                                            aiSummaryState = res
                                                            isSummarizing = false
                                                        }.onFailure { err ->
                                                            aiSummaryState = "Không thể tạo tóm tắt AI: ${err.localizedMessage}"
                                                            isSummarizing = false
                                                        }
                                                    }
                                                }
                                            }
                                        )
                                    }
                                    sectionTitle == "💬 Hỏi đáp AI" -> {
                                        AIChatContent(
                                            docTitle = doc.title,
                                            docType = doc.type.label,
                                            docContent = chapters.find { it.first == "📖 Nội dung" }?.second ?: doc.title,
                                            onAskAi = { question, onResult ->
                                                libraryViewModel.askAiAboutDocument(
                                                    docTitle = doc.title,
                                                    docType = doc.type.label,
                                                    content = chapters.find { it.first == "📖 Nội dung" }?.second ?: doc.title,
                                                    question = question,
                                                    onResult = onResult
                                                )
                                            }
                                        )
                                    }
                                    sectionTitle in listOf("📖 Nội dung", "💡 Trọng tâm", "✏️ Luyện tập") && isLoadingContent -> {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 40.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.spacedBy(16.dp)
                                        ) {
                                            CircularProgressIndicator(
                                                color = ColorAmber,
                                                modifier = Modifier.size(36.dp)
                                            )
                                            Text(
                                                text = when (sectionTitle) {
                                                    "📖 Nội dung" -> "Đang phân tích & soạn thảo bài giảng lý thuyết..."
                                                    "💡 Trọng tâm" -> "Đang trích xuất & cấu trúc kiến thức cốt lõi..."
                                                    else -> "Đang thiết lập bộ bài tập luyện tập củng cố..."
                                                },
                                                fontFamily = DmSansFamily,
                                                fontSize = 13.sp,
                                                color = ColorTextOnLightSecondary,
                                                textAlign = TextAlign.Center
                                            )
                                        }
                                    }
                                    sectionTitle in listOf("📖 Nội dung", "💡 Trọng tâm", "✏️ Luyện tập") && contentError != null -> {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 32.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                            Text(
                                                text = "⚠️ $contentError",
                                                fontFamily = DmSansFamily,
                                                fontSize = 12.5.sp,
                                                color = Color.Red.copy(alpha = 0.8f),
                                                textAlign = TextAlign.Center
                                            )
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(ColorAmber)
                                                    .clickable { onRetryLoadContent() }
                                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = "Thử tải lại 🔄",
                                                    fontFamily = DmSansFamily,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 12.sp,
                                                    color = ColorInk
                                                )
                                            }
                                        }
                                    }
                                    else -> {
                                        Text(
                                            text = parseMarkdownToAnnotatedString(sectionBody, fontSize.toFloat()),
                                            fontFamily = DmSansFamily,
                                            fontSize = fontSize.toFloat().sp,
                                            color = Color(0xFF2C2C2C),
                                            lineHeight = (fontSize * 1.8f).sp,
                                            letterSpacing = 0.1.sp,
                                            modifier = Modifier.padding(top = 12.dp, bottom = 24.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(80.dp))
                            }
                        }
                    }
                }

                // Bottom Action Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                        .drawBehind {
                            drawLine(
                                color = Color(0xFFE5E5E5),
                                start = Offset(0f, 0f),
                                end = Offset(size.width, 0f),
                                strokeWidth = 1f
                            )
                        }
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val canDelete = onDeleteDoc != null && (doc.authorId == currentUserId || currentUserRole == "admin")
                    if (canDelete) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFFFEBEB))
                                .clickable { onDeleteDoc?.invoke(doc) }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "🗑 Xóa",
                                fontFamily = DmSansFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = Color(0xFFE53935),
                                textAlign = TextAlign.Center
                            )
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFFFEBEB))
                                .clickable { onReportClick() }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "🚨 Tố cáo",
                                fontFamily = DmSansFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = Color(0xFFE53935),
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    if (onNoteClick != null) Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFF5F2EA))
                            .clickable { onNoteClick?.invoke(doc) }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "📝 Ghi chú",
                            fontFamily = DmSansFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = Color(0xFF4E3D30),
                            textAlign = TextAlign.Center
                        )
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFF3E8FF))
                            .clickable {
                                val aiTabIndex = chipLabels.indexOfFirst { it == "💬 Hỏi đáp AI" }
                                if (aiTabIndex >= 0) selectedChip = aiTabIndex
                            }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "🤖 Hỏi AI",
                            fontFamily = DmSansFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = Color(0xFF8E24AA),
                            textAlign = TextAlign.Center
                        )
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(ColorForest)
                            .clickable { onDismiss() }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Đóng",
                            fontFamily = DmSansFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}
