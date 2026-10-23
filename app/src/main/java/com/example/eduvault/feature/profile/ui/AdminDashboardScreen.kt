package com.example.eduvault.feature.profile.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Cancel
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.eduvault.core.theme.*
import com.example.eduvault.domain.model.DocumentCategory
import com.example.eduvault.domain.model.User
import com.example.eduvault.feature.library.ui.LibraryDoc
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    onNavigateBack: () -> Unit,
    viewModel: AdminViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Pending, 1: Reported, 2: Users, 3: Categories

    var showAddCategoryDialog by remember { mutableStateOf(false) }
    var categoryToEdit by remember { mutableStateOf<DocumentCategory?>(null) }
    var docToDelete by remember { mutableStateOf<LibraryDoc?>(null) }
    var docToApprove by remember { mutableStateOf<LibraryDoc?>(null) }
    var docToDismissReport by remember { mutableStateOf<LibraryDoc?>(null) }
    var docReportToDelete by remember { mutableStateOf<LibraryDoc?>(null) }
    var userToToggleBlock by remember { mutableStateOf<User?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Quản trị hệ thống",
                        fontFamily = DmSansFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = ColorInk
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = "Quay lại",
                            tint = ColorInk
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = ColorCream)
            )
        },
        containerColor = ColorCream
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp).testTag("admin_content"),
            contentPadding = PaddingValues(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (uiState.accessDenied) {
                item {
                    Card(colors = CardDefaults.cardColors(containerColor = Color.White)) {
                        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text("Cần quyền quản trị", fontFamily = DmSansFamily, fontWeight = FontWeight.SemiBold)
                            Text("Tài khoản chưa được cấp quyền quản trị. Vui lòng liên hệ người quản lý chương trình.", fontFamily = DmSansFamily)
                            OutlinedButton(onClick = onNavigateBack) { Text("Quay lại hồ sơ") }
                        }
                    }
                }
                return@LazyColumn
            }
            item {
            // Success & Error Messages Alerts
            if (uiState.successMessage != null || uiState.errorMessage != null) {
                Box(modifier = Modifier.padding(bottom = 12.dp)) {
                    if (uiState.successMessage != null) {
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardColors(
                                containerColor = ColorForestLight.copy(alpha = 0.15f),
                                contentColor = ColorForest,
                                disabledContainerColor = Color.Transparent,
                                disabledContentColor = Color.LightGray
                            ),
                            border = androidx.compose.foundation.BorderStroke(1.dp, ColorForestLight),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(12.dp)
                                    .fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = uiState.successMessage ?: "",
                                    fontFamily = DmSansFamily,
                                    fontSize = 12.5.sp,
                                    color = ColorForest,
                                    modifier = Modifier.weight(1f)
                                )
                                Icon(
                                    imageVector = Icons.Outlined.Cancel,
                                    contentDescription = "Đóng",
                                    tint = ColorForest,
                                    modifier = Modifier
                                        .size(16.dp)
                                        .clickable { viewModel.clearMessages() }
                                )
                            }
                        }
                    } else if (uiState.errorMessage != null) {
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardColors(
                                containerColor = ColorError.copy(alpha = 0.12f),
                                contentColor = ColorError,
                                disabledContainerColor = Color.Transparent,
                                disabledContentColor = Color.LightGray
                            ),
                            border = androidx.compose.foundation.BorderStroke(1.dp, ColorError),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(12.dp)
                                    .fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = uiState.errorMessage ?: "",
                                    fontFamily = DmSansFamily,
                                    fontSize = 12.5.sp,
                                    color = ColorError,
                                    modifier = Modifier.weight(1f)
                                )
                                Icon(
                                    imageVector = Icons.Outlined.Cancel,
                                    contentDescription = "Đóng",
                                    tint = ColorError,
                                    modifier = Modifier
                                        .size(16.dp)
                                        .clickable { viewModel.clearMessages() }
                                )
                            }
                        }
                    }
                }
            }

            TrainingMetricsPanel(
                users = uiState.users,
                pendingDocumentCount = uiState.pendingDocs.size,
                modifier = Modifier.padding(bottom = 14.dp)
            )

            // Scrollable Tab Row for 4 Tabs
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(100.dp))
                    .background(Color.White)
                    .border(1.dp, ColorBorder, RoundedCornerShape(100.dp))
                    .horizontalScroll(rememberScrollState())
                    .padding(3.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                listOf(
                    Triple(0, "Chờ duyệt 📑", uiState.pendingDocs.size),
                    Triple(1, "Tố cáo 🚩", uiState.reportedDocs.size),
                    Triple(2, "Thành viên 👥", uiState.users.size),
                    Triple(3, "Danh mục 🗂️", uiState.categories.size)
                ).forEach { (index, title, badgeCount) ->
                    val isSelected = selectedTab == index
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(100.dp))
                            .background(if (isSelected) ColorInk else Color.Transparent)
                            .clickable { selectedTab = index }
                            .padding(horizontal = 14.dp, vertical = 9.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = title,
                                fontFamily = DmSansFamily,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color.White else ColorTextOnLightSecondary
                            )
                            if (badgeCount > 0) {
                                Box(
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .background(if (isSelected) Color.White else ColorAmber.copy(alpha = 0.2f))
                                        .padding(horizontal = 5.dp, vertical = 1.dp)
                                ) {
                                    Text(
                                        text = "$badgeCount",
                                        fontFamily = JetBrainsMonoFamily,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) ColorInk else ColorAmberDark
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            } // Scrollable overview and filters
            when (selectedTab) {
                0 -> when {
                    uiState.isLoadingPending && uiState.pendingDocs.isEmpty() -> item { AdminLoading() }
                    uiState.pendingDocs.isEmpty() -> item { AdminEmpty("Không có học liệu chờ duyệt.") }
                    else -> items(uiState.pendingDocs, key = { it.id }) { doc ->
                        PendingDocCard(doc, { docToApprove = doc }, { docToDelete = doc })
                    }
                }
                1 -> when {
                    uiState.isLoadingReported && uiState.reportedDocs.isEmpty() -> item { AdminLoading() }
                    uiState.reportedDocs.isEmpty() -> item { AdminEmpty("Không có học liệu bị báo cáo.") }
                    else -> items(uiState.reportedDocs, key = { it.id }) { doc ->
                        ReportedDocCard(doc, { docToDismissReport = doc }, { docReportToDelete = doc })
                    }
                }
                2 -> when {
                    uiState.isLoadingUsers && uiState.users.isEmpty() -> item { AdminLoading() }
                    uiState.users.isEmpty() -> item { AdminEmpty("Chưa có học viên trong danh sách.") }
                    else -> items(uiState.users, key = { it.uid }) { user ->
                        UserAdminCard(user, { userToToggleBlock = user })
                    }
                }
                3 -> {
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Danh mục học liệu", fontFamily = DmSansFamily, fontWeight = FontWeight.SemiBold)
                            Button(onClick = { showAddCategoryDialog = true }) { Text("Thêm danh mục") }
                        }
                    }
                    when {
                        uiState.isLoadingCats && uiState.categories.isEmpty() -> item { AdminLoading() }
                        uiState.categories.isEmpty() -> item { AdminEmpty("Chưa có danh mục học liệu.") }
                        else -> items(uiState.categories, key = { it.id }) { cat ->
                            CategoryAdminCard(cat, { categoryToEdit = cat }, { viewModel.deleteCategory(cat.id) })
                        }
                    }
                }
            }
        }
    }

    // Dialog Confirmation & Forms
    if (showAddCategoryDialog) {
        CategoryCrudDialog(
            title = "Thêm danh mục mới",
            onDismiss = { showAddCategoryDialog = false },
            onConfirm = { name, emoji ->
                showAddCategoryDialog = false
                viewModel.addCategory(name, emoji)
            }
        )
    }

    categoryToEdit?.let { cat ->
        CategoryCrudDialog(
            title = "Chỉnh sửa danh mục",
            initialName = cat.name,
            initialEmoji = cat.emoji,
            onDismiss = { categoryToEdit = null },
            onConfirm = { name, emoji ->
                categoryToEdit = null
                viewModel.updateCategory(cat.id, name, emoji)
            }
        )
    }

    // Approve dialog confirmation
    docToApprove?.let { doc ->
        AlertDialog(
            onDismissRequest = { docToApprove = null },
            confirmButton = {
                TextButton(
                    onClick = {
                        docToApprove = null
                        viewModel.approveDocument(doc.id)
                    }
                ) {
                    Text("Phê duyệt đăng", fontFamily = DmSansFamily, fontWeight = FontWeight.Bold, color = ColorForest)
                }
            },
            dismissButton = {
                TextButton(onClick = { docToApprove = null }) {
                    Text("Hủy", fontFamily = DmSansFamily, color = ColorTextOnLightSecondary)
                }
            },
            title = { Text("Phê duyệt xuất bản?", fontFamily = DmSansFamily, fontWeight = FontWeight.Bold, fontSize = 18.sp) },
            text = { Text("Tài liệu \"${doc.title}\" sẽ được xuất bản công khai cho tất cả người dùng trong hệ thống.", fontFamily = DmSansFamily, fontSize = 14.sp, color = ColorTextOnLightSecondary) },
            shape = RoundedCornerShape(18.dp),
            containerColor = Color.White
        )
    }

    // Reject pending doc dialog confirmation
    docToDelete?.let { doc ->
        AlertDialog(
            onDismissRequest = { docToDelete = null },
            confirmButton = {
                TextButton(
                    onClick = {
                        docToDelete = null
                        viewModel.rejectDocument(doc.id)
                    }
                ) {
                    Text("Từ chối & Xóa", fontFamily = DmSansFamily, fontWeight = FontWeight.Bold, color = ColorError)
                }
            },
            dismissButton = {
                TextButton(onClick = { docToDelete = null }) {
                    Text("Hủy", fontFamily = DmSansFamily, color = ColorTextOnLightSecondary)
                }
            },
            title = { Text("Từ chối phê duyệt?", fontFamily = DmSansFamily, fontWeight = FontWeight.Bold, fontSize = 18.sp) },
            text = { Text("Từ chối và xóa bỏ vĩnh viễn tài liệu vi phạm \"${doc.title}\" khỏi hệ thống database Firestore.", fontFamily = DmSansFamily, fontSize = 14.sp, color = ColorTextOnLightSecondary) },
            shape = RoundedCornerShape(18.dp),
            containerColor = Color.White
        )
    }

    // Dismiss report dialog confirmation
    docToDismissReport?.let { doc ->
        AlertDialog(
            onDismissRequest = { docToDismissReport = null },
            confirmButton = {
                TextButton(
                    onClick = {
                        docToDismissReport = null
                        viewModel.dismissReport(doc.id)
                    }
                ) {
                    Text("Bác tố cáo", fontFamily = DmSansFamily, fontWeight = FontWeight.Bold, color = ColorAmberDark)
                }
            },
            dismissButton = {
                TextButton(onClick = { docToDismissReport = null }) {
                    Text("Hủy", fontFamily = DmSansFamily, color = ColorTextOnLightSecondary)
                }
            },
            title = { Text("Bác bỏ tố cáo?", fontFamily = DmSansFamily, fontWeight = FontWeight.Bold, fontSize = 18.sp) },
            text = { Text("Bác bỏ toàn bộ báo cáo vi phạm, giữ lại tài liệu \"${doc.title}\" hiển thị an toàn bình thường trên Thư viện.", fontFamily = DmSansFamily, fontSize = 14.sp, color = ColorTextOnLightSecondary) },
            shape = RoundedCornerShape(18.dp),
            containerColor = Color.White
        )
    }

    // Delete reported doc dialog confirmation
    docReportToDelete?.let { doc ->
        AlertDialog(
            onDismissRequest = { docReportToDelete = null },
            confirmButton = {
                TextButton(
                    onClick = {
                        docReportToDelete = null
                        viewModel.deleteReportedDoc(doc.id)
                    }
                ) {
                    Text("Xóa tài liệu", fontFamily = DmSansFamily, fontWeight = FontWeight.Bold, color = ColorError)
                }
            },
            dismissButton = {
                TextButton(onClick = { docReportToDelete = null }) {
                    Text("Hủy", fontFamily = DmSansFamily, color = ColorTextOnLightSecondary)
                }
            },
            title = { Text("Xóa tài liệu bị tố cáo?", fontFamily = DmSansFamily, fontWeight = FontWeight.Bold, fontSize = 18.sp) },
            text = { Text("Xác nhận tài liệu \"${doc.title}\" thực sự vi phạm và xóa vĩnh viễn khỏi hệ thống database.", fontFamily = DmSansFamily, fontSize = 14.sp, color = ColorTextOnLightSecondary) },
            shape = RoundedCornerShape(18.dp),
            containerColor = Color.White
        )
    }

    // Toggle user block dialog confirmation
    userToToggleBlock?.let { user ->
        val verb = if (user.isBlocked) "Mở khóa" else "Khóa"
        AlertDialog(
            onDismissRequest = { userToToggleBlock = null },
            confirmButton = {
                TextButton(
                    onClick = {
                        userToToggleBlock = null
                        viewModel.toggleUserBlock(user.uid, user.isBlocked)
                    }
                ) {
                    Text(verb, fontFamily = DmSansFamily, fontWeight = FontWeight.Bold, color = if (user.isBlocked) ColorForest else ColorError)
                }
            },
            dismissButton = {
                TextButton(onClick = { userToToggleBlock = null }) {
                    Text("Hủy", fontFamily = DmSansFamily, color = ColorTextOnLightSecondary)
                }
            },
            title = { Text("$verb tài khoản thành viên?", fontFamily = DmSansFamily, fontWeight = FontWeight.Bold, fontSize = 18.sp) },
            text = {
                val blockDesc = if (user.isBlocked) {
                    "Sau khi mở khóa, người dùng \"${user.fullName}\" có thể đăng nhập và truy cập tất cả tài nguyên bình thường."
                } else {
                    "Tài khoản \"${user.fullName}\" sẽ bị vô hiệu hóa trong Firebase Authentication và không thể đăng nhập hoặc làm mới phiên truy cập."
                }
                Text(blockDesc, fontFamily = DmSansFamily, fontSize = 14.sp, color = ColorTextOnLightSecondary)
            },
            shape = RoundedCornerShape(18.dp),
            containerColor = Color.White
        )
    }
}

// ─── Enterprise training overview ────────────────────────────────────────────
@Composable
private fun TrainingMetricsPanel(
    users: List<User>,
    pendingDocumentCount: Int,
    modifier: Modifier = Modifier
) {
    val learners = users.filterNot { it.role == "admin" }
    val activeLearners = learners.count { !it.isBlocked }
    val totalQuizAttempts = learners.sumOf { it.quizCount }
    val contributionCount = learners.sumOf { it.uploadCount }
    val averageTrainingProgress = if (learners.isNotEmpty()) {
        learners.sumOf { it.trainingProgressPercent } / learners.size
    } else 0
    val weightedAverage = if (totalQuizAttempts > 0) {
        learners.sumOf { it.quizAverageScore.toDouble() * it.quizCount } / totalQuizAttempts
    } else {
        0.0
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = ColorInk),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f).padding(end = 8.dp)) {
                    Text(
                        text = "Tổng quan đào tạo",
                        fontFamily = DmSansFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color.White
                    )
                    Text(
                        text = "Số liệu tổng hợp từ hồ sơ học viên",
                        fontFamily = DmSansFamily,
                        fontSize = 10.sp,
                        color = ColorTextOnDarkSecondary
                    )
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(100.dp))
                        .background(ColorAmber.copy(alpha = 0.18f))
                        .padding(horizontal = 9.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = "$pendingDocumentCount chờ duyệt",
                        fontFamily = JetBrainsMonoFamily,
                        fontSize = 8.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = ColorAmber
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AdminMetricCard(
                    value = "$activeLearners/${learners.size}",
                    label = "Học viên hoạt động",
                    modifier = Modifier.weight(1f)
                )
                AdminMetricCard(
                    value = "$totalQuizAttempts",
                    label = "Lượt đánh giá",
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AdminMetricCard(
                    value = if (totalQuizAttempts > 0) String.format(Locale.ROOT, "%.1f/10", weightedAverage) else "Chưa có",
                    label = "Điểm trung bình",
                    modifier = Modifier.weight(1f)
                )
                AdminMetricCard(
                    value = "$averageTrainingProgress%",
                    label = "Tiến độ trung bình",
                    modifier = Modifier.weight(1f)
                )
            }
            Text(
                text = "$contributionCount học liệu đã được học viên đóng góp và duyệt",
                fontFamily = DmSansFamily,
                fontSize = 9.5.sp,
                color = ColorTextOnDarkSecondary,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

@Composable
private fun AdminMetricCard(value: String, label: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(Color.White.copy(alpha = 0.07f))
            .padding(horizontal = 10.dp, vertical = 9.dp)
    ) {
        Text(
            text = value,
            fontFamily = JetBrainsMonoFamily,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            maxLines = 1
        )
        Text(
            text = label,
            fontFamily = DmSansFamily,
            fontSize = 9.5.sp,
            color = ColorTextOnDarkSecondary,
        )
    }
}

// ─── Component 0: Empty State View ───────────────────────────────────────────
@Composable
private fun EmptyStateView(emoji: String, message: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = emoji, fontSize = 52.sp)
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = message,
                fontFamily = DmSansFamily,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = ColorTextOnLightSecondary,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )
        }
    }
}

// ─── Component 1: Pending Review Document Card ────────────────────────────────
@Composable
private fun PendingDocCard(
    doc: LibraryDoc,
    onApproveClick: () -> Unit,
    onRejectClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, ColorBorder)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(ColorAmber.copy(alpha = 0.15f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = doc.courseCode,
                        fontFamily = JetBrainsMonoFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        color = ColorAmberDark
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = doc.type.label,
                    fontFamily = DmSansFamily,
                    fontSize = 11.sp,
                    color = ColorTextOnLightSecondary
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = doc.title,
                fontFamily = DmSansFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 14.5.sp,
                color = ColorInk,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(12.dp))

            // AI Check Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "AI Quét từ cấm: ",
                        fontFamily = DmSansFamily,
                        fontSize = 11.5.sp,
                        color = ColorTextOnLightSecondary
                    )
                    val (badgeText, badgeBg, badgeCol) = when (doc.aiCheckResult) {
                        "DANGER" -> Triple("🔴 Khiếm nhã nặng", ColorError.copy(alpha = 0.12f), ColorError)
                        else -> Triple("🟡 Từ nhạy cảm", ColorAmber.copy(alpha = 0.15f), ColorAmberDark)
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(100.dp))
                            .background(badgeBg)
                            .padding(horizontal = 9.dp, vertical = 2.5.dp)
                    ) {
                        Text(
                            text = badgeText,
                            fontFamily = DmSansFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.5.sp,
                            color = badgeCol
                        )
                    }
                }

                // Control Action buttons
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(ColorForestLight.copy(alpha = 0.15f))
                            .clickable { onApproveClick() }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "Duyệt Đăng",
                            fontFamily = DmSansFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = ColorForest
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(ColorError.copy(alpha = 0.1f))
                            .clickable { onRejectClick() }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "Xóa Bỏ",
                            fontFamily = DmSansFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = ColorError
                        )
                    }
                }
            }
        }
    }
}

// ─── Component 2: Reported Document Card ──────────────────────────────────────
@Composable
private fun ReportedDocCard(
    doc: LibraryDoc,
    onDismissClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, ColorBorder)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(ColorAmber.copy(alpha = 0.15f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = doc.courseCode,
                            fontFamily = JetBrainsMonoFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            color = ColorAmberDark
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = doc.type.label,
                        fontFamily = DmSansFamily,
                        fontSize = 11.sp,
                        color = ColorTextOnLightSecondary
                    )
                }

                // Report count badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(100.dp))
                        .background(ColorError.copy(alpha = 0.12f))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "🚩 Tố cáo: ${doc.reportCount} lượt",
                        fontFamily = JetBrainsMonoFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        color = ColorError
                    )
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = doc.title,
                fontFamily = DmSansFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 14.5.sp,
                color = ColorInk,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // AI Baseline Check Result indicator
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "AI Quét trước đó: ",
                        fontFamily = DmSansFamily,
                        fontSize = 11.sp,
                        color = ColorTextOnLightSecondary
                    )
                    Text(
                        text = if (doc.aiCheckResult == "SAFE") "🟢 Sạch" else "🟡 Nghi vấn",
                        fontFamily = DmSansFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = if (doc.aiCheckResult == "SAFE") ColorForest else ColorAmberDark
                    )
                }

                // Dismiss Report & Delete Document action buttons
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(ColorAmber.copy(alpha = 0.15f))
                            .clickable { onDismissClick() }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "Bác Tố Cáo",
                            fontFamily = DmSansFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = ColorAmberDark
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(ColorError.copy(alpha = 0.1f))
                            .clickable { onDeleteClick() }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "Xóa Tài Liệu",
                            fontFamily = DmSansFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = ColorError
                        )
                    }
                }
            }
        }
    }
}

// ─── Component 3: User List Item Card ─────────────────────────────────────────
@Composable
private fun UserAdminCard(
    user: User,
    onToggleBlockClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, ColorBorder)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // User role initial avatar icon
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(if (user.role == "admin") ColorAmber.copy(alpha = 0.15f) else ColorInk.copy(alpha = 0.05f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (user.role == "admin") "👑" else "👤",
                    fontSize = 18.sp
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = user.fullName,
                        fontFamily = DmSansFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = ColorInk
                    )
                    if (user.role == "admin") {
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(100.dp))
                                .background(ColorAmber.copy(alpha = 0.2f))
                                .padding(horizontal = 6.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = "ADMIN",
                                fontFamily = JetBrainsMonoFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 8.5.sp,
                                color = ColorAmberDark
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = user.email,
                    fontFamily = DmSansFamily,
                    fontSize = 11.5.sp,
                    color = ColorTextOnLightSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // User status badge & Block actions
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Block status badge
                val (statusText, statusBg, statusCol) = if (user.isBlocked) {
                    Triple("Khóa 🚫", ColorError.copy(alpha = 0.1f), ColorError)
                } else {
                    Triple("Active 🟢", ColorForest.copy(alpha = 0.15f), ColorForest)
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(100.dp))
                        .background(statusBg)
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = statusText,
                        fontFamily = DmSansFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        color = statusCol
                    )
                }

                // Prevent admin from blocking themselves
                if (user.role != "admin") {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (user.isBlocked) ColorForestLight.copy(alpha = 0.15f) else ColorError.copy(alpha = 0.1f))
                            .clickable { onToggleBlockClick() }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = if (user.isBlocked) "Mở Khóa" else "Khóa",
                            fontFamily = DmSansFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.5.sp,
                            color = if (user.isBlocked) ColorForest else ColorError
                        )
                    }
                }
            }
        }
    }
}

// ─── Component 4: Category Card for Admin ────────────────────────────────────
@Composable
private fun CategoryAdminCard(
    cat: DocumentCategory,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, ColorBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(ColorAmber.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = cat.emoji, fontSize = 18.sp)
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = cat.name,
                    fontFamily = DmSansFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.5.sp,
                    color = ColorInk
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${cat.count} tài liệu thuộc danh mục (computed)",
                    fontFamily = DmSansFamily,
                    fontSize = 11.sp,
                    color = ColorTextOnLightSecondary
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                IconButton(onClick = onEditClick, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = Icons.Outlined.Edit,
                        contentDescription = "Sửa",
                        tint = ColorAmberDark,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Không cho phép xóa các danh mục mẫu cốt lõi để giữ hệ thống ổn định
                if (!cat.id.startsWith("cat_")) {
                    IconButton(onClick = onDeleteClick, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = Icons.Outlined.Delete,
                            contentDescription = "Xóa",
                            tint = ColorError,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

// ─── Component 5: Category Add/Edit Dialog Form ──────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategoryCrudDialog(
    title: String,
    initialName: String = "",
    initialEmoji: String = "📁",
    onDismiss: () -> Unit,
    onConfirm: (name: String, emoji: String) -> Unit
) {
    var name by remember { mutableStateOf(initialName) }
    var emoji by remember { mutableStateOf(initialEmoji) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

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
            Card(
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
                            text = title,
                            fontFamily = DmSansFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = ColorInk
                        )
                        IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                            Icon(
                                imageVector = Icons.Outlined.Cancel,
                                contentDescription = "Đóng",
                                tint = ColorTextOnLightSecondary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    if (errorMsg != null) {
                        Text(
                            text = errorMsg ?: "",
                            fontFamily = DmSansFamily,
                            fontSize = 11.5.sp,
                            color = ColorError,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                    }

                    OutlinedTextField(
                        value = emoji,
                        onValueChange = { emoji = it },
                        label = { Text("Biểu tượng Emoji Presets", fontFamily = DmSansFamily) },
                        placeholder = { Text("Ví dụ: 📈", fontSize = 12.sp) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ColorAmber,
                            unfocusedBorderColor = ColorBorder
                        )
                    )

                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Tên nhóm kỹ năng mới", fontFamily = DmSansFamily) },
                        placeholder = { Text("Ví dụ: DevOps cơ bản", fontSize = 12.sp) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 20.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ColorAmber,
                            unfocusedBorderColor = ColorBorder
                        )
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .border(1.dp, ColorBorder, RoundedCornerShape(10.dp))
                                .background(Color.White)
                                .clickable { onDismiss() }
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Hủy bỏ",
                                fontFamily = DmSansFamily,
                                fontSize = 13.sp,
                                color = ColorTextOnLightSecondary
                            )
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(ColorAmber)
                                .clickable {
                                    if (name.trim().isEmpty() || emoji.trim().isEmpty()) {
                                        errorMsg = "Không được để trống Tên hoặc Emoji!"
                                    } else {
                                        onConfirm(name.trim(), emoji.trim())
                                    }
                                }
                                    .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Xác nhận",
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

@Composable
private fun AdminLoading() {
    Box(Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
private fun AdminEmpty(message: String) {
    Text(message, Modifier.fillMaxWidth().padding(vertical = 24.dp), fontFamily = DmSansFamily,
        color = ColorTextOnLightSecondary)
}
