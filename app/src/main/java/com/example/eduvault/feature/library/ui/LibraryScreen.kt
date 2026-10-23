package com.example.eduvault.feature.library.ui

import com.example.eduvault.feature.notes.ui.NotesViewModel

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.eduvault.core.theme.ColorPaper
import com.example.eduvault.core.theme.ColorTextOnLightSecondary
import com.example.eduvault.core.theme.DmSansFamily
import com.example.eduvault.core.theme.EduVaultTheme
import com.example.eduvault.domain.model.DocReaderState
import com.example.eduvault.feature.library.ui.components.CourseChipsRow
import com.example.eduvault.feature.library.ui.components.DocSortHeaderRow
import com.example.eduvault.feature.library.ui.components.DocTypeTabsRow
import com.example.eduvault.feature.library.ui.components.DocsBanner
import com.example.eduvault.feature.library.ui.components.LibraryDocCard
import com.example.eduvault.feature.library.ui.components.LibraryDocListItem
import com.example.eduvault.feature.library.ui.components.PaginationRow
import com.example.eduvault.feature.library.ui.components.SearchBarAndActions
import com.example.eduvault.feature.library.ui.viewer.DocViewerDialog
import kotlinx.coroutines.CompletableDeferred

@Composable
fun LibraryContent(
    paddingValues: PaddingValues,
    onUploadClick: () -> Unit,
    onNavigateToLogin: () -> Unit = {},
    onNavigateToRegister: () -> Unit = {},
    viewModel: LibraryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val notesViewModel: NotesViewModel = hiltViewModel()
    val scrollState = rememberScrollState()

    var selectedViewDoc by remember { mutableStateOf<LibraryDoc?>(null) }

    // Refresh the current learner profile and access state when entering the tab.
    LaunchedEffect(Unit) {
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

        // 3. Document Type Tabs
        DocTypeTabsRow(
            selectedDocType = uiState.selectedDocType,
            onDocTypeSelected = viewModel::onDocTypeSelected,
            counts = uiState.docTypeCounts
        )

        Spacer(modifier = Modifier.height(14.dp))

        // 4. Document List/Grid showing count and quick pills
        DocSortHeaderRow(
            totalCount = uiState.totalCount,
            selectedSort = uiState.selectedSort,
            onSortSelected = viewModel::onSortSelected
        )

        Spacer(modifier = Modifier.height(12.dp))

        // 5. Documents list grid or list column
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
                                        onBlocked = onNavigateToLogin
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
                                onBlocked = onNavigateToLogin
                            )
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 6. Pagination
        PaginationRow(
            currentPage = uiState.currentPage,
            totalPages = uiState.totalPages,
            onPageSelected = viewModel::onPageSelected
        )

        Spacer(modifier = Modifier.height(16.dp))
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

        // Training resources are available to every authenticated learner.
        val readerState = if (uiState.currentUserId.isBlank()) {
            DocReaderState.GuestLocked
        } else {
            DocReaderState.Unlocked
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
            onUnlockWithCredit = {},
            onGenerateSummary = {
                val deferred = CompletableDeferred<Result<String>>()
                viewModel.generateDocumentSummary(
                    docId = doc.id,
                    docTitle = doc.title,
                    docType = doc.type.label,
                    content = uiState.activeDocContent?.content ?: "Tóm tắt tài liệu đào tạo ${doc.title} thuộc kỹ năng ${doc.courseCode}."
                ) { result ->
                    deferred.complete(result)
                }
                deferred.await()
            },
            onNoteClick = { selected ->
                if (uiState.currentUserId.isBlank()) onNavigateToLogin()
                else notesViewModel.openNoteEditor(selected.id, selected.title, selected.courseCode)
            },
            currentUserId = uiState.currentUserId,
            currentUserRole = uiState.currentUserRole,
            onDownloadPdf = { url, file -> viewModel.downloadDocumentPdf(url, file) },
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
