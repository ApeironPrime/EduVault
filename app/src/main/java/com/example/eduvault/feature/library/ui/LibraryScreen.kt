package com.example.eduvault.feature.library.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.outlined.Bookmark
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Cancel
import androidx.compose.material.icons.outlined.CloudUpload
import androidx.compose.material.icons.outlined.RemoveRedEye
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.eduvault.core.theme.ColorAmber
import com.example.eduvault.core.theme.ColorAmberDark
import com.example.eduvault.core.theme.ColorAmberLight
import com.example.eduvault.core.theme.ColorBorder
import com.example.eduvault.core.theme.ColorCream
import com.example.eduvault.core.theme.ColorForest
import com.example.eduvault.core.theme.ColorInk
import com.example.eduvault.core.theme.ColorPaper
import com.example.eduvault.core.theme.ColorTextOnDark
import com.example.eduvault.core.theme.ColorTextOnDarkSecondary
import com.example.eduvault.core.theme.ColorTextOnLight
import com.example.eduvault.core.theme.ColorTextOnLightSecondary
import com.example.eduvault.core.theme.DmSansFamily
import com.example.eduvault.core.theme.EduVaultTheme
import com.example.eduvault.core.ui.appendMathText
import com.example.eduvault.core.theme.JetBrainsMonoFamily
import com.example.eduvault.core.theme.PlayfairDisplayFamily
import com.example.eduvault.domain.model.DocReaderState
import com.example.eduvault.domain.model.DocViewerTabsContent
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import androidx.compose.foundation.Image
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.ui.platform.LocalContext
import java.io.File
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.sync.withLock

@Composable
fun LibraryContent(
    paddingValues: PaddingValues,
    onUploadClick: () -> Unit,
    onNavigateToLogin: () -> Unit = {},
    onNavigateToRegister: () -> Unit = {},
    viewModel: LibraryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    var showBlockDialog by remember { mutableStateOf(false) }
    var selectedViewDoc by remember { mutableStateOf<LibraryDoc?>(null) }

    // Reload credits when entering the tab
    androidx.compose.runtime.LaunchedEffect(Unit) {
        viewModel.loadUserCredits()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ColorPaper)
            .padding(paddingValues)
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        // 1. Search Bar & Pill selectors & Actions
        SearchBarAndActions(
            searchQuery = uiState.searchQuery,
            onSearchQueryChange = viewModel::onSearchQueryChange,
            selectedSort = uiState.selectedSort,
            onSortSelected = viewModel::onSortSelected,
            selectedSchool = uiState.selectedSchool,
            onSchoolSelected = viewModel::onSchoolSelected,
            isGridView = uiState.isGridView,
            onToggleViewMode = viewModel::onToggleViewMode,
            documentCredits = uiState.documentCredits,
            onUploadClick = onUploadClick
        )

        Spacer(modifier = Modifier.height(14.dp))

        // 2. Banner: "Tài liệu của bạn"
        DocsBanner(
            totalDocs = uiState.bannerTotalDocs,
            totalSubjects = uiState.bannerTotalSubjects,
            totalSaved = uiState.bannerTotalSaved
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 4. Document Type Tabs
        DocTypeTabsRow(
            selectedDocType = uiState.selectedDocType,
            onDocTypeSelected = viewModel::onDocTypeSelected,
            counts = uiState.docTypeCounts
        )

        Spacer(modifier = Modifier.height(14.dp))

        // 5. Document List/Grid showing count and quick pills
        DocSortHeaderRow(
            totalCount = uiState.totalCount,
            selectedSort = uiState.selectedSort,
            onSortSelected = viewModel::onSortSelected
        )

        Spacer(modifier = Modifier.height(12.dp))

        // 6. Documents list grid or list column
        if (uiState.documents.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Không tìm thấy tài liệu phù hợp",
                    fontFamily = DmSansFamily,
                    color = ColorTextOnLightSecondary,
                    fontSize = 14.sp
                )
            }
        } else {
            if (uiState.isGridView) {
                // Since verticalScroll is active on outer Column, we construct a responsive flow list
                // rather than standard LazyVerticalGrid to avoid nested scroll issues
                val chunkedList = uiState.documents.chunked(2)
                chunkedList.forEach { rowItems ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        rowItems.forEach { doc ->
                            LibraryDocCard(
                                doc = doc,
                                onToggleSaved = viewModel::onToggleSaved,
                                onDocClick = {
                                    viewModel.onOpenDocument(
                                        docId = doc.id,
                                        onAllowed = { selectedViewDoc = doc },
                                        onBlocked = { showBlockDialog = true }
                                    )
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        if (rowItems.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }
            } else {
                uiState.documents.forEach { doc ->
                    LibraryDocListItem(
                        doc = doc,
                        onToggleSaved = viewModel::onToggleSaved,
                        onDocClick = {
                            viewModel.onOpenDocument(
                                docId = doc.id,
                                onAllowed = { selectedViewDoc = doc },
                                onBlocked = { showBlockDialog = true }
                            )
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 7. Pagination
        PaginationRow(
            currentPage = uiState.currentPage,
            totalPages = uiState.totalPages,
            onPageSelected = viewModel::onPageSelected
        )

        Spacer(modifier = Modifier.height(16.dp))
    }

    // Dialogs overlay
    if (showBlockDialog) {
        CreditBlockDialog(
            onDismiss = { showBlockDialog = false },
            onUploadClick = onUploadClick
        )
    }

    selectedViewDoc?.let { doc ->
        val context = LocalContext.current

        LaunchedEffect(doc.id) {
            viewModel.loadDocViewerContent(
                doc.id,
                doc.title,
                doc.courseCode,
                doc.type.label
            )
        }

        val cost = calculatePrice(doc.fileSizeBytes, doc.quantityLabel)
        val isUnlocked = uiState.currentUserRole == "admin" ||
                         doc.authorId == uiState.currentUserId ||
                         uiState.unlockedDocuments.contains(doc.id)

        val readerState = when {
            isUnlocked -> DocReaderState.Unlocked
            uiState.documentCredits == -1 -> DocReaderState.GuestLocked
            uiState.documentCredits < cost -> DocReaderState.NoCredits
            else -> DocReaderState.HasCredits(uiState.documentCredits)
        }

        DocViewerDialog(
            doc = doc,
            readerState = readerState,
            activeContent = uiState.activeDocContent,
            isLoadingContent = uiState.isLoadingDocContent,
            contentError = uiState.docContentError,
            onRetryLoadContent = {
                viewModel.loadDocViewerContent(
                    doc.id,
                    doc.title,
                    doc.courseCode,
                    doc.type.label
                )
            },
            onDismiss = {
                viewModel.clearDocViewerContent()
                selectedViewDoc = null
            },
            onReportClick = {
                viewModel.reportDocument(
                    docId = doc.id,
                    onSuccess = {
                        viewModel.clearDocViewerContent()
                        selectedViewDoc = null
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
                viewModel.unlockDocument(doc.id) { result ->
                    result.onSuccess {
                        android.widget.Toast.makeText(context, "Mở khóa tài liệu thành công!", android.widget.Toast.LENGTH_SHORT).show()
                    }.onFailure { err ->
                        android.widget.Toast.makeText(context, err.localizedMessage ?: "Mở khóa thất bại", android.widget.Toast.LENGTH_LONG).show()
                    }
                }
            },
            onGenerateSummary = {
                val deferred = kotlinx.coroutines.CompletableDeferred<Result<String>>()
                viewModel.generateDocumentSummary(
                    docId = doc.id,
                    docTitle = doc.title,
                    docType = doc.type.label,
                    content = uiState.activeDocContent?.content ?: "Tóm tắt tài liệu học thuật ${doc.title} thuộc chủ đề ${doc.courseCode}."
                ) { result ->
                    deferred.complete(result)
                }
                deferred.await()
            },
            currentUserId = uiState.currentUserId,
            currentUserRole = uiState.currentUserRole,
            onDeleteDoc = { docToDelete ->
                viewModel.deleteDocument(
                    docId = docToDelete.id,
                    onSuccess = {
                        viewModel.clearDocViewerContent()
                        selectedViewDoc = null
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

// ─── Component 1: Search Bar & Actions ─────────────────────────────────────────
@Composable
private fun SearchBarAndActions(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    selectedSort: SortType,
    onSortSelected: (SortType) -> Unit,
    selectedSchool: String,
    onSchoolSelected: (String) -> Unit,
    isGridView: Boolean,
    onToggleViewMode: () -> Unit,
    documentCredits: Int,
    onUploadClick: () -> Unit
) {
    val focusManager = LocalFocusManager.current
    var schoolMenuExpanded by remember { mutableStateOf(false) }
    var sortMenuExpanded by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Main Search Text Field
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                placeholder = {
                    Text(
                        text = "Tìm tài liệu, môn học...",
                        fontFamily = DmSansFamily,
                        fontSize = 13.sp,
                        color = ColorTextOnLightSecondary
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.Search,
                        contentDescription = "Search",
                        tint = ColorTextOnLightSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedBorderColor = ColorAmber,
                    unfocusedBorderColor = ColorBorder
                )
            )

            Spacer(modifier = Modifier.width(8.dp))

            // Credit Badge displaying actual credits
            Row(
                modifier = Modifier
                    .height(48.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(ColorAmber.copy(alpha = 0.12f))
                    .border(1.5.dp, ColorAmber, RoundedCornerShape(10.dp))
                    .clickable { onUploadClick() }
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "🧩 $documentCredits lượt",
                    fontFamily = JetBrainsMonoFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.5.sp,
                    color = ColorAmberDark
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Pill Dropdowns
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                // School Select
                Box {
                    Row(
                        modifier = Modifier
                            .background(Color.White, RoundedCornerShape(8.dp))
                            .border(1.dp, ColorBorder, RoundedCornerShape(8.dp))
                            .clickable { schoolMenuExpanded = true }
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = selectedSchool,
                            fontFamily = DmSansFamily,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = ColorTextOnLight
                        )
                    }

                    DropdownMenu(
                        expanded = schoolMenuExpanded,
                        onDismissRequest = { schoolMenuExpanded = false }
                    ) {
                        listOf("Tất cả trường", "ĐH Kinh tế TP.HCM", "ĐH Bách Khoa", "ĐH Quốc Gia").forEach { school ->
                            DropdownMenuItem(
                                text = { Text(school, fontFamily = DmSansFamily, fontSize = 12.sp) },
                                onClick = {
                                    onSchoolSelected(school)
                                    schoolMenuExpanded = false
                                }
                            )
                        }
                    }
                }

                // Sort Dropdown
                Box {
                    Row(
                        modifier = Modifier
                            .background(Color.White, RoundedCornerShape(8.dp))
                            .border(1.dp, ColorBorder, RoundedCornerShape(8.dp))
                            .clickable { sortMenuExpanded = true }
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = selectedSort.label,
                            fontFamily = DmSansFamily,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = ColorTextOnLight
                        )
                    }

                    DropdownMenu(
                        expanded = sortMenuExpanded,
                        onDismissRequest = { sortMenuExpanded = false }
                    ) {
                        SortType.values().forEach { sort ->
                            DropdownMenuItem(
                                text = { Text(sort.label, fontFamily = DmSansFamily, fontSize = 12.sp) },
                                onClick = {
                                    onSortSelected(sort)
                                    sortMenuExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            // Grid / List Layout Toggle
            IconButton(
                onClick = onToggleViewMode,
                modifier = Modifier
                    .size(34.dp)
                    .background(ColorCream, RoundedCornerShape(8.dp))
            ) {
                Icon(
                    imageVector = if (isGridView) Icons.Filled.List else Icons.Filled.GridOn,
                    contentDescription = "Toggle view",
                    tint = ColorTextOnLight,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

// ─── Component 2: Banner "Tài liệu của bạn" ────────────────────────────────────
@Composable
private fun DocsBanner(
    totalDocs: Int,
    totalSubjects: Int,
    totalSaved: Int
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(ColorInk)
            .drawBehind {
                // Background radial gradients matching warm editorial tech look
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(ColorAmber.copy(alpha = 0.18f), Color.Transparent),
                        center = Offset(size.width * 0.9f, size.height * 0.1f),
                        radius = size.width * 0.5f
                    )
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(ColorForest.copy(alpha = 0.14f), Color.Transparent),
                        center = Offset(size.width * 0.4f, size.height * 0.9f),
                        radius = size.width * 0.4f
                    )
                )
            }
            .padding(20.dp)
    ) {
        Column {
            Text(
                text = "✦ KHO HỌC LIỆU",
                fontFamily = JetBrainsMonoFamily,
                fontSize = 9.sp,
                color = ColorAmber,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.5.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = buildAnnotatedString {
                    append("Tài liệu ")
                    withStyle(style = SpanStyle(color = ColorAmber, fontStyle = FontStyle.Italic)) {
                        append("của bạn")
                    }
                },
                fontFamily = PlayfairDisplayFamily,
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Tìm kiếm, lọc và quản lý toàn bộ ghi chú, quiz, slide từ cộng đồng và thư viện cá nhân.",
                fontFamily = DmSansFamily,
                fontSize = 11.sp,
                color = ColorTextOnDarkSecondary,
                lineHeight = 16.sp,
                modifier = Modifier.fillMaxWidth(0.85f)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Stat 1
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = if (totalDocs >= 1000) "${String.format(Locale.ROOT, "%.1f", totalDocs / 1000f)}k" else "$totalDocs",
                        fontFamily = JetBrainsMonoFamily,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = ColorAmber
                    )
                    Text(
                        text = "TÀI LIỆU",
                        fontFamily = DmSansFamily,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = ColorTextOnDarkSecondary,
                        letterSpacing = 0.5.sp
                    )
                }

                // Stat 2
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "$totalSubjects",
                        fontFamily = JetBrainsMonoFamily,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = ColorAmber
                    )
                    Text(
                        text = "MÔN HỌC",
                        fontFamily = DmSansFamily,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = ColorTextOnDarkSecondary,
                        letterSpacing = 0.5.sp
                    )
                }

                // Stat 3
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "$totalSaved",
                        fontFamily = JetBrainsMonoFamily,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = ColorAmber
                    )
                    Text(
                        text = "ĐÃ LƯU",
                        fontFamily = DmSansFamily,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = ColorTextOnDarkSecondary,
                        letterSpacing = 0.5.sp
                    )
                }
            }
        }
    }
}

// ─── Component 3: Course Chips Row ─────────────────────────────────────────────
@Composable
private fun CourseChipsRow(
    selectedSubject: String,
    onSubjectSelected: (String) -> Unit
) {
    val subjects = listOf("Tất cả", "Kinh tế vi mô", "Marketing", "Thống kê", "Quản trị", "Kế toán", "Luật", "Lập trình")
    val colors = listOf(ColorForest, ColorAmber, Color(0xFFC0392B), ColorInk, Color(0xFF8E44AD), Color(0xFF2980B9))

    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        items(subjects) { subject ->
            val isSelected = selectedSubject == subject
            val color = if (subject == "Tất cả") ColorForest else {
                val index = (subject.hashCode() % colors.size).let { if (it < 0) it + colors.size else it }
                colors[index]
            }

            Row(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(if (isSelected) color else Color.White)
                    .border(1.dp, if (isSelected) color else ColorBorder, CircleShape)
                    .clickable { onSubjectSelected(subject) }
                    .padding(horizontal = 14.dp, vertical = 7.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .background(if (isSelected) Color.White else color, CircleShape)
                )

                Spacer(modifier = Modifier.width(6.dp))

                Text(
                    text = subject,
                    fontFamily = DmSansFamily,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (isSelected) Color.White else ColorTextOnLight
                )
            }
        }
    }
}

// ─── Component 4: Document Type Tabs ───────────────────────────────────────────
@Composable
private fun DocTypeTabsRow(
    selectedDocType: DocTypeFilter,
    onDocTypeSelected: (DocTypeFilter) -> Unit,
    counts: Map<DocTypeFilter, Int>
) {
    val tabs = DocTypeFilter.values()

    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        items(tabs) { tab ->
            val isSelected = selectedDocType == tab
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isSelected) ColorInk else Color.White)
                    .border(1.dp, if (isSelected) ColorInk else ColorBorder, RoundedCornerShape(8.dp))
                    .clickable { onDocTypeSelected(tab) }
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Icon representation
                val icon = when (tab) {
                    DocTypeFilter.ALL -> "📁"
                    DocTypeFilter.NOTE -> "📝"
                    DocTypeFilter.QUIZ -> "🧩"
                    DocTypeFilter.SLIDE -> "📊"
                    DocTypeFilter.SUMMARY -> "📌"
                    DocTypeFilter.FLASHCARD -> "🃏"
                }

                Text(text = icon, fontSize = 12.sp)
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = tab.label,
                    fontFamily = DmSansFamily,
                    fontSize = 12.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isSelected) Color.White else ColorTextOnLight
                )

                Spacer(modifier = Modifier.width(5.dp))

                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(if (isSelected) Color.White.copy(alpha = 0.15f) else ColorCream)
                        .padding(horizontal = 6.dp, vertical = 1.dp)
                ) {
                    Text(
                        text = "${counts[tab] ?: 0}",
                        fontFamily = JetBrainsMonoFamily,
                        fontSize = 9.sp,
                        color = if (isSelected) ColorTextOnDarkSecondary else ColorTextOnLightSecondary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

// ─── Component 5: Doc Sort Header Row ──────────────────────────────────────────
@Composable
private fun DocSortHeaderRow(
    totalCount: Int,
    selectedSort: SortType,
    onSortSelected: (SortType) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = buildAnnotatedString {
                append("Hiển thị ")
                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, color = ColorInk)) {
                    append("$totalCount")
                }
                append(" tài liệu")
            },
            fontFamily = DmSansFamily,
            fontSize = 12.sp,
            color = ColorTextOnLightSecondary
        )

        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            SortType.values().forEach { sort ->
                val isSelected = selectedSort == sort
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(if (isSelected) ColorInk else Color.Transparent)
                        .border(1.dp, if (isSelected) ColorInk else ColorBorder, CircleShape)
                        .clickable { onSortSelected(sort) }
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = sort.label,
                        fontFamily = DmSansFamily,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (isSelected) Color.White else ColorTextOnLightSecondary
                    )
                }
            }
        }
    }
}

// ─── Component 6: Document Grid Card ───────────────────────────────────────────
@Composable
private fun LibraryDocCard(
    doc: LibraryDoc,
    onToggleSaved: (String) -> Unit,
    onDocClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Curated soft background list
    val bgGradients = listOf(
        Brush.linearGradient(listOf(Color(0xFFFEF9F0), Color(0xFFFDF0D5))), // Note
        Brush.linearGradient(listOf(Color(0xFFF0F9F4), Color(0xFFD4EDDA))), // Quiz
        Brush.linearGradient(listOf(Color(0xFFF0F4FF), Color(0xFFDDE8FF))), // Slide
        Brush.linearGradient(listOf(Color(0xFFFFF0F0), Color(0xFFFDD8D8))), // Summary
        Brush.linearGradient(listOf(Color(0xFFF5F0FF), Color(0xFFEAD5FF))),
        Brush.linearGradient(listOf(Color(0xFFF0FCFF), Color(0xFFD5F0FF)))
    )
    val gradient = bgGradients[doc.bgIndex % bgGradients.size]

    val emoji = when (doc.type) {
        LibraryDocType.NOTE -> "📝"
        LibraryDocType.QUIZ -> "🧩"
        LibraryDocType.SLIDE -> "📊"
        LibraryDocType.SUMMARY -> "📌"
    }

    val badgeBg = when (doc.type) {
        LibraryDocType.NOTE -> ColorAmber.copy(alpha = 0.15f)
        LibraryDocType.QUIZ -> ColorForest.copy(alpha = 0.12f)
        LibraryDocType.SLIDE -> ColorTextOnLightSecondary.copy(alpha = 0.12f)
        LibraryDocType.SUMMARY -> Color(0xFFC0392B).copy(alpha = 0.1f)
    }

    val badgeTextColor = when (doc.type) {
        LibraryDocType.NOTE -> ColorAmberDark
        LibraryDocType.QUIZ -> ColorForest
        LibraryDocType.SLIDE -> ColorTextOnLight
        LibraryDocType.SUMMARY -> Color(0xFFC0392B)
    }

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, ColorBorder, RoundedCornerShape(12.dp))
            .background(Color.White)
            .clickable { onDocClick() }
    ) {
        // Thumbnail section
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(86.dp)
                .background(gradient),
            contentAlignment = Alignment.Center
        ) {
            // Type badge
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(8.dp)
                    .background(badgeBg, RoundedCornerShape(4.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = doc.type.label.uppercase(),
                    fontFamily = JetBrainsMonoFamily,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold,
                    color = badgeTextColor
                )
            }

            // Quantity Pages tag
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(8.dp)
                    .background(Color.White.copy(alpha = 0.85f), RoundedCornerShape(4.dp))
                    .padding(horizontal = 5.dp, vertical = 2.dp)
            ) {
                Text(
                    text = doc.quantityLabel,
                    fontFamily = JetBrainsMonoFamily,
                    fontSize = 8.sp,
                    color = ColorTextOnLightSecondary,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // Large emoji center
            Text(text = emoji, fontSize = 28.sp)
        }

        // Info Section
        Column(modifier = Modifier.padding(10.dp)) {
            Text(
                text = doc.courseCode,
                fontFamily = JetBrainsMonoFamily,
                fontSize = 9.sp,
                color = ColorTextOnLightSecondary,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )

            Spacer(modifier = Modifier.height(3.dp))

            Text(
                text = doc.title,
                fontFamily = DmSansFamily,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = ColorInk,
                maxLines = 2,
                minLines = 2,
                lineHeight = 15.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.Star,
                        contentDescription = "Rating",
                        tint = ColorAmber,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = if (doc.rating <= 0f) "Chưa đánh giá" else "${doc.rating}",
                        fontFamily = DmSansFamily,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = ColorTextOnLight
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.Outlined.RemoveRedEye,
                        contentDescription = "Views",
                        tint = ColorTextOnLightSecondary,
                        modifier = Modifier.size(11.dp)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = doc.views,
                        fontFamily = DmSansFamily,
                        fontSize = 10.sp,
                        color = ColorTextOnLightSecondary
                    )
                }

                IconButton(
                    onClick = { onToggleSaved(doc.id) },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = if (doc.isSaved) Icons.Outlined.Bookmark else Icons.Outlined.BookmarkBorder,
                        contentDescription = "Save document",
                        tint = if (doc.isSaved) ColorAmber else ColorTextOnLightSecondary,
                        modifier = Modifier.size(15.dp)
                    )
                }
            }
        }
    }
}

// ─── Component 6b: Document List Item ──────────────────────────────────────────
@Composable
private fun LibraryDocListItem(
    doc: LibraryDoc,
    onToggleSaved: (String) -> Unit,
    onDocClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bgGradients = listOf(
        Brush.linearGradient(listOf(Color(0xFFFEF9F0), Color(0xFFFDF0D5))),
        Brush.linearGradient(listOf(Color(0xFFF0F9F4), Color(0xFFD4EDDA))),
        Brush.linearGradient(listOf(Color(0xFFF0F4FF), Color(0xFFDDE8FF))),
        Brush.linearGradient(listOf(Color(0xFFFFF0F0), Color(0xFFFDD8D8))),
        Brush.linearGradient(listOf(Color(0xFFF5F0FF), Color(0xFFEAD5FF))),
        Brush.linearGradient(listOf(Color(0xFFF0FCFF), Color(0xFFD5F0FF)))
    )
    val gradient = bgGradients[doc.bgIndex % bgGradients.size]

    val emoji = when (doc.type) {
        LibraryDocType.NOTE -> "📝"
        LibraryDocType.QUIZ -> "🧩"
        LibraryDocType.SLIDE -> "📊"
        LibraryDocType.SUMMARY -> "📌"
    }

    val badgeBg = when (doc.type) {
        LibraryDocType.NOTE -> ColorAmber.copy(alpha = 0.15f)
        LibraryDocType.QUIZ -> ColorForest.copy(alpha = 0.12f)
        LibraryDocType.SLIDE -> ColorTextOnLightSecondary.copy(alpha = 0.12f)
        LibraryDocType.SUMMARY -> Color(0xFFC0392B).copy(alpha = 0.1f)
    }

    val badgeTextColor = when (doc.type) {
        LibraryDocType.NOTE -> ColorAmberDark
        LibraryDocType.QUIZ -> ColorForest
        LibraryDocType.SLIDE -> ColorTextOnLight
        LibraryDocType.SUMMARY -> Color(0xFFC0392B)
    }

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, ColorBorder, RoundedCornerShape(12.dp))
            .background(Color.White)
            .clickable { onDocClick() }
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left Thumbnail square
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(gradient),
            contentAlignment = Alignment.Center
        ) {
            Text(text = emoji, fontSize = 22.sp)
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Center Info
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = doc.courseCode,
                    fontFamily = JetBrainsMonoFamily,
                    fontSize = 9.sp,
                    color = ColorTextOnLightSecondary,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.width(8.dp))

                Box(
                    modifier = Modifier
                        .background(badgeBg, RoundedCornerShape(3.dp))
                        .padding(horizontal = 4.dp, vertical = 1.dp)
                ) {
                    Text(
                        text = doc.type.label.uppercase(),
                        fontFamily = JetBrainsMonoFamily,
                        fontSize = 7.sp,
                        fontWeight = FontWeight.Bold,
                        color = badgeTextColor
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                Text(
                    text = doc.quantityLabel,
                    fontFamily = JetBrainsMonoFamily,
                    fontSize = 8.sp,
                    color = ColorTextOnLightSecondary
                )
            }

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = doc.title,
                fontFamily = DmSansFamily,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = ColorInk,
                maxLines = 1,
                lineHeight = 16.sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Outlined.Star,
                    contentDescription = "Rating",
                    tint = ColorAmber,
                    modifier = Modifier.size(10.dp)
                )
                Spacer(modifier = Modifier.width(2.dp))
                Text(
                    text = if (doc.rating <= 0f) "Chưa đánh giá" else "${doc.rating}",
                    fontFamily = DmSansFamily,
                    fontSize = 10.sp,
                    color = ColorTextOnLight,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.Outlined.RemoveRedEye,
                    contentDescription = "Views",
                    tint = ColorTextOnLightSecondary,
                    modifier = Modifier.size(10.dp)
                )
                Spacer(modifier = Modifier.width(2.dp))
                Text(
                    text = doc.views,
                    fontFamily = DmSansFamily,
                    fontSize = 10.sp,
                    color = ColorTextOnLightSecondary
                )
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Right Save Bookmark action
        IconButton(
            onClick = { onToggleSaved(doc.id) },
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                imageVector = if (doc.isSaved) Icons.Outlined.Bookmark else Icons.Outlined.BookmarkBorder,
                contentDescription = "Save document",
                tint = if (doc.isSaved) ColorAmber else ColorTextOnLightSecondary,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

// ─── Component 7: Pagination ───────────────────────────────────────────────────
@Composable
private fun PaginationRow(
    currentPage: Int,
    totalPages: Int,
    onPageSelected: (Int) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Prev button
        IconButton(
            onClick = { if (currentPage > 1) onPageSelected(currentPage - 1) },
            enabled = currentPage > 1,
            modifier = Modifier
                .size(34.dp)
                .border(1.dp, ColorBorder, RoundedCornerShape(8.dp))
                .background(Color.White, RoundedCornerShape(8.dp))
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                contentDescription = "Trang trước",
                tint = if (currentPage > 1) ColorTextOnLight else ColorTextOnLightSecondary,
                modifier = Modifier.size(14.dp)
            )
        }

        Spacer(modifier = Modifier.width(6.dp))

        // First 3 Pages or current pages
        val pagesToDraw = remember(currentPage, totalPages) {
            val list = mutableListOf<Int>()
            if (totalPages <= 5) {
                for (i in 1..totalPages) list.add(i)
            } else {
                list.add(1)
                if (currentPage > 3) {
                    list.add(-1) // -1 is dot indicator
                }
                val start = maxOf(2, currentPage - 1)
                val end = minOf(totalPages - 1, currentPage + 1)
                for (i in start..end) {
                    if (!list.contains(i)) list.add(i)
                }
                if (currentPage < totalPages - 2) {
                    list.add(-2) // -2 is dot indicator
                }
                if (!list.contains(totalPages)) list.add(totalPages)
            }
            list
        }

        pagesToDraw.forEach { page ->
            if (page < 0) {
                Text(
                    text = "...",
                    fontFamily = JetBrainsMonoFamily,
                    fontSize = 12.sp,
                    color = ColorTextOnLightSecondary,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            } else {
                val isSelected = currentPage == page
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) ColorInk else Color.White)
                        .border(1.dp, if (isSelected) ColorInk else ColorBorder, RoundedCornerShape(8.dp))
                        .clickable { onPageSelected(page) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "$page",
                        fontFamily = JetBrainsMonoFamily,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) Color.White else ColorTextOnLight
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
            }
        }

        // Next button
        IconButton(
            onClick = { if (currentPage < totalPages) onPageSelected(currentPage + 1) },
            enabled = currentPage < totalPages,
            modifier = Modifier
                .size(34.dp)
                .border(1.dp, ColorBorder, RoundedCornerShape(8.dp))
                .background(Color.White, RoundedCornerShape(8.dp))
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.ArrowForward,
                contentDescription = "Trang sau",
                tint = if (currentPage < totalPages) ColorTextOnLight else ColorTextOnLightSecondary,
                modifier = Modifier.size(14.dp)
            )
        }
    }
}

// ─── Preview ───────────────────────────────────────────────────────────────────
@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun LibraryScreenPreview() {
    EduVaultTheme {
        LibraryContent(
            paddingValues = PaddingValues(0.dp),
            onUploadClick = {}
        )
    }
}

// ─── Credit Block Dialog (Give-and-Take Rules) ──────────────────────────────────
@Composable
fun CreditBlockDialog(
    onDismiss: () -> Unit,
    onUploadClick: () -> Unit
) {
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
                colors = androidx.compose.material3.CardDefaults.cardColors(containerColor = Color.White),
                elevation = androidx.compose.material3.CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(ColorAmber.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "🧩", fontSize = 32.sp)
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Text(
                        text = "Hết lượt sử dụng tài liệu!",
                        fontFamily = PlayfairDisplayFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = ColorInk,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "EduVault áp dụng Quy tắc Đóng góp - Sử dụng công bằng (Give-and-Take).\n\nBạn cần đóng góp đăng tải 1 tài liệu học tập mới của mình để nhận ngay +1 lượt sử dụng tài liệu chất lượng khác từ cộng đồng.",
                        fontFamily = DmSansFamily,
                        fontSize = 12.5.sp,
                        color = ColorTextOnLightSecondary,
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .border(1.dp, ColorBorder, RoundedCornerShape(10.dp))
                                .background(ColorCream)
                                .clickable { onDismiss() }
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Bỏ qua",
                                fontFamily = DmSansFamily,
                                fontWeight = FontWeight.Medium,
                                fontSize = 13.sp,
                                color = ColorTextOnLightSecondary
                            )
                        }

                        Box(
                            modifier = Modifier
                                .weight(1.5f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(ColorAmber)
                                .clickable {
                                    onDismiss()
                                    onUploadClick()
                                }
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Đăng tài liệu ngay 📥",
                                fontFamily = DmSansFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = ColorInk
                            )
                        }
                    }
                }
            }
        }
    }
}

// ─── Custom Document Viewer Dialog ──────────────────────────────────────────────
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
    currentUserId: String = "",
    currentUserRole: String = "user"
) {
    val state = readerState
    val isFullyUnlocked = state is DocReaderState.Unlocked
    val coroutineScope = androidx.compose.runtime.rememberCoroutineScope()

    // Font size state
    var fontSize by remember { mutableStateOf(14) }

    // Tab selection
    val tabLabels = listOf("📖 Tổng quan", "✨ Smart Summary", "💬 Hỏi đáp AI")
    var selectedTab by remember { mutableStateOf(0) }

    // AI Summary state
    var aiSummaryState by remember { mutableStateOf<String?>(null) }
    var isSummarizing by remember { mutableStateOf(false) }

    // Build chapters/sections from activeContent
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
                    Mã môn học: ${doc.courseCode}
                    Phân loại: ${doc.type.label}
                    Lượt xem: ${doc.views}
                    Đánh giá: ${if (doc.rating <= 0f) "Chưa đánh giá" else "${doc.rating} ★"}
                """.trimIndent(),
                "💡 Trọng tâm" to "Đang chờ tải nội dung trọng tâm...",
                "✏️ Luyện tập" to "Đang chờ tải bài luyện tập...",
                "✨ Smart Summary" to "",
                "💬 Hỏi đáp AI" to ""
            )
        }
    }

    // Map tab index to chapter index
    val chipLabels = remember(chapters) {
        chapters.map { it.first }
    }
    var selectedChip by remember { mutableStateOf(0) }

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
                    ) { /* block */ }
            ) {
                // ══════════════ DARK HEADER ══════════════
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
                        // Course code chip
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFF2E2416)) // Tối hơi nâu/vàng
                                .border(1.dp, Color(0xFFC48B36), RoundedCornerShape(6.dp))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = doc.courseCode,
                                fontFamily = JetBrainsMonoFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = Color(0xFFE5A642) // Vàng cam ấm áp giống hình
                            )
                        }

                        // Right controls: Font size controls + Close button
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

                            // Close button (X) tròn đen mờ
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.12f))
                                    .clickable { onDismiss() },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = androidx.compose.material.icons.Icons.Default.Close,
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

                    // Row 3: Metadata (views, rating, reading time, progress)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "👁 ${doc.views}",
                            fontFamily = DmSansFamily,
                            fontSize = 11.5.sp,
                            color = Color.White.copy(alpha = 0.6f)
                        )
                        Text(
                            text = if (doc.rating <= 0f) "⭐ Chưa đánh giá" else "⭐ ${doc.rating}",
                            fontFamily = DmSansFamily,
                            fontSize = 11.5.sp,
                            color = Color(0xFFE5A642)
                        )
                        Text(
                            text = "~5 phút đọc",
                            fontFamily = DmSansFamily,
                            fontSize = 11.5.sp,
                            color = Color.White.copy(alpha = 0.5f)
                        )
                        Text(
                            text = "0%",
                            fontFamily = DmSansFamily,
                            fontSize = 11.5.sp,
                            color = Color.White.copy(alpha = 0.4f)
                        )
                    }

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

                // ══════════════ CONTENT AREA ══════════════
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
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .verticalScroll(rememberScrollState())
                                    .padding(horizontal = 16.dp, vertical = 14.dp)
                            ) {
                                // Section title header
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

                                // Content routing based on section type
                                when {
                                    // Locked chapter overlay (for chapters beyond the first, if not unlocked)
                                    index > 0 && sectionTitle !in listOf("✨ Smart Summary", "💬 Hỏi đáp AI") && !isFullyUnlocked -> {
                                        LockedChapterOverlay(
                                            readerState = state,
                                            onNavigateToLogin = onNavigateToLogin,
                                            onNavigateToRegister = onNavigateToRegister,
                                            onUnlockWithCredit = onUnlockWithCredit
                                        )
                                    }
                                    // Smart Summary tab
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
                                    // AI Chat tab
                                    sectionTitle == "💬 Hỏi đáp AI" -> {
                                        val libraryViewModel: LibraryViewModel = hiltViewModel()
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
                                    // Loading state for dynamic content tabs
                                    sectionTitle in listOf("📖 Nội dung", "💡 Trọng tâm", "✏️ Luyện tập") && isLoadingContent -> {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 40.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.spacedBy(16.dp)
                                        ) {
                                            androidx.compose.material3.CircularProgressIndicator(
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
                                    // Error state
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
                                    // Normal content display
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

                                Spacer(modifier = Modifier.height(80.dp)) // Space for bottom bar
                            }
                        }
                    }
                }

                // ══════════════ BOTTOM ACTION BAR ══════════════
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
                    val canDelete = doc.authorId == currentUserId || currentUserRole == "admin"
                    if (canDelete) {
                        // Xóa button
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
                        // Tố cáo button
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

                    // Ghi chú button
                    Box(
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

                    // Hỏi AI button
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFF3E8FF))
                            .clickable {
                                // Navigate to the AI Chat tab
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

                    // Đã đọc button
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
                            text = "✓ Đã đọc",
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

// ─── AI Summary Content ────────────────────────────────────────────────────────
@Composable
fun AISummaryContent(
    summary: String?,
    isSummarizing: Boolean,
    onGenerateClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(ColorCream)
            .border(1.dp, ColorBorder, RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("✨", fontSize = 18.sp)
            Text(
                text = "Bản tóm tắt thông minh (Gemini AI)",
                fontFamily = PlayfairDisplayFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = ColorAmberDark
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (isSummarizing) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                androidx.compose.material3.CircularProgressIndicator(
                    color = ColorAmber,
                    modifier = Modifier.size(32.dp)
                )
                Text(
                    text = "Gemini AI đang phân tích và tóm tắt nội dung...",
                    fontFamily = DmSansFamily,
                    fontSize = 12.sp,
                    color = ColorTextOnLightSecondary,
                    textAlign = TextAlign.Center
                )
            }
        } else if (summary != null) {
            Text(
                text = parseMarkdownToAnnotatedString(summary, 13f),
                fontFamily = DmSansFamily,
                fontSize = 13.sp,
                color = ColorInk.copy(alpha = 0.85f),
                lineHeight = 20.sp
            )
        } else {
            Text(
                text = "Nhấp vào nút bên dưới để yêu cầu Gemini AI đọc hiểu tài liệu và biên soạn một bản tóm tắt súc tích hoàn toàn miễn phí.",
                fontFamily = DmSansFamily,
                fontSize = 12.sp,
                color = ColorTextOnLightSecondary,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(14.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(ColorAmber, Color(0xFFFF8F00))
                        )
                    )
                    .clickable { onGenerateClick() }
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "✨ Tạo tóm tắt AI miễn phí",
                    fontFamily = DmSansFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = Color.White
                )
            }
        }
    }
}

// ─── AI Chat Content ────────────────────────────────────────────────────────────
@Composable
fun AIChatContent(
    docTitle: String,
    docType: String,
    docContent: String,
    onAskAi: (String, (Result<String>) -> Unit) -> Unit
) {
    var messageQuery by remember { mutableStateOf("") }
    var chatMessages by remember {
        mutableStateOf(
            listOf(
                com.example.eduvault.domain.model.AiChatMessage(
                    isUser = false,
                    text = "Chào bạn! Mình là AI Tutor, trợ lý học tập riêng biệt cho tài liệu '$docTitle'. Bạn cần mình giải đáp, phân tích hay đặt câu hỏi luyện tập nào về nội dung tài liệu này? 🧠💬"
                )
            )
        )
    }
    var isAiTyping by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(ColorCream)
            .border(1.dp, ColorBorder, RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("💬", fontSize = 16.sp)
                Text(
                    text = "AI Tutor — Hỏi đáp về tài liệu",
                    fontFamily = PlayfairDisplayFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = ColorAmberDark
                )
            }
            Spacer(modifier = Modifier.height(10.dp))

            // Chat messages area
            val chatScrollState = rememberScrollState()
            LaunchedEffect(chatMessages.size, isAiTyping) {
                chatScrollState.animateScrollTo(chatScrollState.maxValue)
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color.White)
                    .border(1.dp, ColorBorder.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                    .verticalScroll(chatScrollState)
                    .padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                chatMessages.forEach { msg ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = if (msg.isUser) Arrangement.End else Arrangement.Start
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.85f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (msg.isUser) ColorForest.copy(alpha = 0.1f) else ColorAmber.copy(alpha = 0.1f)
                                )
                                .padding(10.dp)
                        ) {
                            Text(
                                text = parseMarkdownToAnnotatedString(msg.text, 12.5f),
                                fontFamily = DmSansFamily,
                                fontSize = 12.5.sp,
                                color = ColorInk,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
                if (isAiTyping) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(ColorAmber.copy(alpha = 0.1f))
                                .padding(10.dp)
                        ) {
                            Text(
                                text = "AI đang suy nghĩ...",
                                fontFamily = DmSansFamily,
                                fontSize = 12.sp,
                                color = ColorTextOnLightSecondary,
                                fontStyle = FontStyle.Italic
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Input row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = messageQuery,
                    onValueChange = { messageQuery = it },
                    placeholder = {
                        Text(
                            "Hỏi AI về tài liệu...",
                            fontFamily = DmSansFamily,
                            fontSize = 12.sp,
                            color = ColorTextOnLightSecondary
                        )
                    },
                    modifier = Modifier.weight(1f),
                    textStyle = androidx.compose.ui.text.TextStyle(
                        fontFamily = DmSansFamily,
                        fontSize = 13.sp,
                        color = ColorInk
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ColorAmber,
                        unfocusedBorderColor = ColorBorder,
                        cursorColor = ColorAmber
                    ),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(
                        onSend = {
                            if (messageQuery.isNotBlank() && !isAiTyping) {
                                val query = messageQuery.trim()
                                messageQuery = ""
                                chatMessages = chatMessages + com.example.eduvault.domain.model.AiChatMessage(
                                    isUser = true, text = query
                                )
                                isAiTyping = true
                                onAskAi(query) { result ->
                                    result.onSuccess { answer ->
                                        chatMessages = chatMessages + com.example.eduvault.domain.model.AiChatMessage(
                                            isUser = false, text = answer
                                        )
                                    }.onFailure { err ->
                                        chatMessages = chatMessages + com.example.eduvault.domain.model.AiChatMessage(
                                            isUser = false, text = "Lỗi: ${err.localizedMessage ?: "Không thể kết nối AI"}"
                                        )
                                    }
                                    isAiTyping = false
                                }
                            }
                        }
                    )
                )
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(if (messageQuery.isNotBlank() && !isAiTyping) ColorAmber else ColorBorder)
                        .clickable(enabled = messageQuery.isNotBlank() && !isAiTyping) {
                            val query = messageQuery.trim()
                            messageQuery = ""
                            chatMessages = chatMessages + com.example.eduvault.domain.model.AiChatMessage(
                                isUser = true, text = query
                            )
                            isAiTyping = true
                            onAskAi(query) { result ->
                                result.onSuccess { answer ->
                                    chatMessages = chatMessages + com.example.eduvault.domain.model.AiChatMessage(
                                        isUser = false, text = answer
                                    )
                                }.onFailure { err ->
                                    chatMessages = chatMessages + com.example.eduvault.domain.model.AiChatMessage(
                                        isUser = false, text = "Lỗi: ${err.localizedMessage ?: "Không thể kết nối AI"}"
                                    )
                                }
                                isAiTyping = false
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text("➤", fontSize = 16.sp, color = Color.White)
                }
            }
        }
    }
}

// ─── Locked Chapter Overlay ─────────────────────────────────────────────────────
@Composable
fun LockedChapterOverlay(
    readerState: DocReaderState,
    onNavigateToLogin: () -> Unit = {},
    onNavigateToRegister: () -> Unit = {},
    onUnlockWithCredit: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFF5F0E0).copy(alpha = 0.7f))
            .border(1.dp, ColorAmber.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("🔒", fontSize = 32.sp)

        Text(
            text = when (readerState) {
                is DocReaderState.GuestLocked -> "Bạn cần đăng nhập để mở khóa nội dung này"
                is DocReaderState.NoCredits -> "Bạn đã hết credit. Upload tài liệu để nhận thêm."
                is DocReaderState.HasCredits -> "Sử dụng credit để mở khóa toàn bộ nội dung"
                else -> ""
            },
            fontFamily = DmSansFamily,
            fontSize = 13.sp,
            color = ColorInk.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )

        when (readerState) {
            is DocReaderState.GuestLocked -> {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(ColorForest)
                            .clickable { onNavigateToLogin() }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Đăng nhập", fontFamily = DmSansFamily, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.White)
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .border(1.dp, ColorForest, RoundedCornerShape(10.dp))
                            .clickable { onNavigateToRegister() }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Đăng ký", fontFamily = DmSansFamily, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = ColorForest)
                    }
                }
            }
            is DocReaderState.HasCredits -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(ColorForest)
                        .clickable { onUnlockWithCredit() }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Mở khóa (${readerState.count} credit)",
                        fontFamily = DmSansFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = Color.White
                    )
                }
            }
            is DocReaderState.NoCredits -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(ColorAmber)
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Hết credit – Upload tài liệu để nhận thêm",
                        fontFamily = DmSansFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = ColorInk
                    )
                }
            }
            else -> { /* Unlocked */ }
        }
    }
}

// ─── Markdown Parser helper ───────────────────────────────────────────────────
fun parseMarkdownToAnnotatedString(markdown: String, baseFontSizeSp: Float): AnnotatedString {
    val lines = markdown.split("\n")
    return buildAnnotatedString {
        lines.forEachIndexed { index, line ->
            var processedLine = line
                .replace("\\Omega", "Ω")
                .replace("\\omega", "ω")
                .replace("\\alpha", "α")
                .replace("\\beta", "β")
                .replace("\\gamma", "γ")
                .replace("\\delta", "δ")
                .replace("\\pi", "π")
                .replace("\\sigma", "σ")
                .replace("\\mu", "μ")
                .replace("\\lambda", "λ")
                .replace("\\theta", "θ")
                .replace("\\phi", "φ")
                .replace("\\epsilon", "ε")
                .replace("\\rho", "ρ")
                .replace("\\Delta", "Δ")
                .replace("\\Sigma", "Σ")
            var isHeader = false
            var headerLevel = 0
            
            if (line.trimStart().startsWith("#")) {
                val match = Regex("^(#{1,6})\\s+(.*)$").find(line.trim())
                if (match != null) {
                    headerLevel = match.groupValues[1].length
                    processedLine = match.groupValues[2]
                    isHeader = true
                }
            } else {
                // Xử lý bullet list (*, -, +) ở đầu dòng thành dấu • để tránh bị trùng với cú pháp in nghiêng *
                val bulletMatch = Regex("^(\\s*)[*+-]\\s+(.*)$").find(line)
                if (bulletMatch != null) {
                    val spaces = bulletMatch.groupValues[1]
                    val content = bulletMatch.groupValues[2]
                    processedLine = "$spaces• $content"
                }
            }
            
            if (isHeader) {
                val scale = when (headerLevel) {
                    1 -> 1.4f
                    2 -> 1.3f
                    3 -> 1.2f
                    else -> 1.1f
                }
                val start = length
                append(processedLine)
                addStyle(
                    style = SpanStyle(
                        fontSize = (baseFontSizeSp * scale).sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFC48B36)
                    ),
                    start = start,
                    end = length
                )
            } else {
                appendInlineFormattedText(processedLine, baseFontSizeSp)
            }
            
            if (index < lines.lastIndex) {
                append("\n")
            }
        }
    }
}

fun AnnotatedString.Builder.appendInlineFormattedText(text: String, baseFontSizeSp: Float) {
    var i = 0
    val len = text.length
    while (i < len) {
        if (text[i] == '$') {
            val endIdx = text.indexOf("$", i + 1)
            if (endIdx != -1) {
                val mathContent = text.substring(i + 1, endIdx)
                appendMathText(mathContent, baseFontSizeSp)
                i = endIdx + 1
                continue
            }
        }
        
        if (i < len - 2 && text[i] == '*' && text[i + 1] == '*') {
            val endIdx = text.indexOf("**", i + 2)
            if (endIdx != -1) {
                val boldContent = text.substring(i + 2, endIdx)
                val start = length
                append(boldContent)
                addStyle(
                    style = SpanStyle(
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E1E1E)
                    ),
                    start = start,
                    end = length
                )
                i = endIdx + 2
                continue
            }
        }
        
        if (text[i] == '*') {
            val endIdx = text.indexOf("*", i + 1)
            if (endIdx != -1) {
                val italicContent = text.substring(i + 1, endIdx)
                val start = length
                append(italicContent)
                addStyle(
                    style = SpanStyle(
                        fontStyle = FontStyle.Italic
                    ),
                    start = start,
                    end = length
                )
                i = endIdx + 1
                continue
            }
        }
        
        if (text[i] == '`') {
            val endIdx = text.indexOf("`", i + 1)
            if (endIdx != -1) {
                val codeContent = text.substring(i + 1, endIdx)
                val start = length
                append(codeContent)
                addStyle(
                    style = SpanStyle(
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        background = Color(0x1A808080)
                    ),
                    start = start,
                    end = length
                )
                i = endIdx + 1
                continue
            }
        }
        
        append(text[i])
        i++
    }
}

// ─── Native PDF Page Renderer ────────────────────────────────────────────────
@Composable
fun PdfPage(
    renderer: PdfRenderer,
    pageIndex: Int,
    rendererMutex: kotlinx.coroutines.sync.Mutex,
    modifier: Modifier = Modifier
) {
    var bitmap by remember(pageIndex) { mutableStateOf<Bitmap?>(null) }

    LaunchedEffect(pageIndex) {
        withContext(Dispatchers.IO) {
            try {
                rendererMutex.withLock {
                    if (pageIndex < renderer.pageCount) {
                        val page = renderer.openPage(pageIndex)
                        val width = 800
                        val height = (width.toFloat() / page.width * page.height).toInt()
                        val pageBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                        val canvas = android.graphics.Canvas(pageBitmap)
                        canvas.drawColor(android.graphics.Color.WHITE)
                        page.render(pageBitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                        page.close()
                        bitmap = pageBitmap
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("PdfPage", "Lỗi render trang $pageIndex: ${e.localizedMessage}")
            }
        }
    }

    androidx.compose.runtime.DisposableEffect(pageIndex) {
        onDispose {
            bitmap?.recycle()
            bitmap = null
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        val currentBitmap = bitmap
        if (currentBitmap != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Image(
                    bitmap = currentBitmap.asImageBitmap(),
                    contentDescription = "Trang ${pageIndex + 1}",
                    modifier = Modifier.fillMaxWidth(),
                    contentScale = ContentScale.FillWidth
                )
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp),
                contentAlignment = Alignment.Center
            ) {
                androidx.compose.material3.CircularProgressIndicator(
                    color = ColorAmber,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}

@Composable
fun PdfViewer(
    file: File,
    downloadUrl: String,
    modifier: Modifier = Modifier
) {
    var isDownloading by remember(file) { mutableStateOf(false) }
    var downloadError by remember(file) { mutableStateOf<String?>(null) }
    var pageCount by remember(file) { mutableStateOf(0) }
    var pdfRenderer by remember(file) { mutableStateOf<PdfRenderer?>(null) }
    var fileDescriptor by remember(file) { mutableStateOf<ParcelFileDescriptor?>(null) }

    LaunchedEffect(file) {
        withContext(Dispatchers.IO) {
            try {
                if (!file.exists() && downloadUrl.isNotEmpty()) {
                    isDownloading = true
                    android.util.Log.d("PdfViewer", "Đang tải xuống tài liệu từ URL: $downloadUrl")
                    if (downloadUrl.startsWith("documents/")) {
                        val parent = file.parentFile
                        if (parent != null && !parent.exists()) parent.mkdirs()
                        val tempFile = File(file.absolutePath + ".tmp")
                        com.google.firebase.storage.FirebaseStorage.getInstance()
                            .reference.child(downloadUrl)
                            .getFile(tempFile)
                            .await()
                        if (!tempFile.renameTo(file)) {
                            tempFile.copyTo(file, overwrite = true)
                            tempFile.delete()
                        }
                    } else {
                    val url = java.net.URL(downloadUrl)
                    val connection = url.openConnection() as java.net.HttpURLConnection
                    connection.requestMethod = "GET"
                    connection.connectTimeout = 15000
                    connection.readTimeout = 15000
                    connection.doInput = true
                    connection.connect()
                    if (connection.responseCode == java.net.HttpURLConnection.HTTP_OK) {
                        val parent = file.parentFile
                        if (parent != null && !parent.exists()) {
                            parent.mkdirs()
                        }
                        val tempFile = File(file.absolutePath + ".tmp")
                        connection.inputStream.use { input ->
                            tempFile.outputStream().use { output ->
                                input.copyTo(output)
                            }
                        }
                        tempFile.renameTo(file)
                    } else {
                        throw Exception("Lỗi tải tệp: HTTP ${connection.responseCode}")
                    }
                    }
                    isDownloading = false
                }

                if (file.exists()) {
                    val pfd = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
                    val renderer = PdfRenderer(pfd)
                    fileDescriptor = pfd
                    pdfRenderer = renderer
                    pageCount = renderer.pageCount
                } else {
                    downloadError = "Không tìm thấy tệp tài liệu cục bộ."
                }
            } catch (e: Exception) {
                android.util.Log.e("PdfViewer", "Lỗi khi mở/nạp PDF", e)
                downloadError = "Lỗi khi nạp PDF: ${e.localizedMessage}"
            } finally {
                isDownloading = false
            }
        }
    }

    androidx.compose.runtime.DisposableEffect(file) {
        onDispose {
            try {
                pdfRenderer?.close()
                fileDescriptor?.close()
            } catch (e: Exception) {
                // bỏ qua
            }
        }
    }

    if (isDownloading) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            androidx.compose.material3.CircularProgressIndicator(
                color = ColorAmber,
                modifier = Modifier.size(36.dp)
            )
            Text(
                text = "Đang tải xuống tài liệu từ Cloud...",
                fontFamily = DmSansFamily,
                fontSize = 13.sp,
                color = ColorTextOnLightSecondary,
                textAlign = TextAlign.Center
            )
        }
    } else if (downloadError != null) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "⚠️ ${downloadError!!}",
                color = Color.Red.copy(alpha = 0.8f),
                fontFamily = DmSansFamily,
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
    } else {
        val renderer = pdfRenderer
        if (renderer != null && pageCount > 0) {
            val rendererMutex = remember { kotlinx.coroutines.sync.Mutex() }
            LazyColumn(
                modifier = modifier
                    .fillMaxSize()
                    .padding(horizontal = 4.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 90.dp)
            ) {
                items(pageCount) { index ->
                    PdfPage(
                        renderer = renderer,
                        pageIndex = index,
                        rendererMutex = rendererMutex
                    )
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 40.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                androidx.compose.material3.CircularProgressIndicator(
                    color = ColorAmber,
                    modifier = Modifier.size(36.dp)
                )
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


