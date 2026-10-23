package com.example.eduvault.feature.mentor.ui

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.eduvault.domain.model.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.saveable.rememberSaveable

@Composable
fun MentorScreen(user: User, onLogout: () -> Unit, viewModel: MentorViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var section by rememberSaveable { mutableIntStateOf(0) }
    var editing by remember { mutableStateOf<Pair<MentorLearner, TrainingModule>?>(null) }
    var resetting by remember { mutableStateOf<Pair<MentorLearner, TrainingModule>?>(null) }
    var reviewing by remember { mutableStateOf<MentorLearner?>(null) }
    var approved by remember { mutableStateOf(true) }
    var feedback by remember { mutableStateOf("") }
    val uriHandler = LocalUriHandler.current
    val context = LocalContext.current
    val navy = Color(0xFF0F172A)

    LaunchedEffect(state.reviewVersion) {
        reviewing = null
        editing = null
        resetting = null
        feedback = ""
    }
    LazyColumn(Modifier.fillMaxSize().safeDrawingPadding().testTag("mentor_learners"),
        contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item {
            Text("Quản lý đào tạo", fontSize = 24.sp, fontWeight = FontWeight.SemiBold, color = navy)
            Text(user.fullName, fontSize = 14.sp, color = Color(0xFF64748B))
            Text(user.email, fontSize = 12.sp, color = Color(0xFF64748B))
            TextButton(onClick = { viewModel.logout(); onLogout() }, enabled = !state.saving) { Text("Đăng xuất") }
        }
        item {
            Card(colors = CardDefaults.cardColors(containerColor = navy)) {
                Column(Modifier.fillMaxWidth().padding(20.dp)) {
                    Text("Học viên được phân công", color = Color.White)
                    Text(state.learners.size.toString(), color = Color.White, fontSize = 30.sp)
                    Text("${state.learners.count { it.dashboard.currentEvidence?.status == "PENDING" }} bằng chứng chờ nghiệm thu",
                        color = Color.White.copy(alpha = 0.8f))
                }
            }
            OutlinedButton(onClick = viewModel::reload, enabled = !state.loading && !state.saving) { Text("Tải lại danh sách") }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Học viên", "Nhiệm vụ", "Bài nộp").forEachIndexed { index, title ->
                    FilterChip(selected = section == index, onClick = { section = index }, label = { Text(title) })
                }
            }
        }
        state.message?.let { message -> item { Text(message, color = Color(0xFF047857)) } }
        if (reviewing == null && editing == null && resetting == null) state.error?.let { error -> item { Text(error, color = Color(0xFFB91C1C)) } }
        if (state.loading) item { CircularProgressIndicator() }
        if (!state.loading && state.error == null && state.learners.isEmpty()) item {
            Text("Bạn chưa được phân công học viên. Liên hệ người quản lý đào tạo để nhận phân công.")
        }
        items(state.learners.sortedByDescending { it.dashboard.currentEvidence?.status == "PENDING" }, key = { it.uid }) { learner ->
            val dashboard = learner.dashboard
            val evidence = dashboard.evidence
            if (section == 1) {
                MentorTasksCard(learner, enabled = !state.saving && !state.loading,
                    onEdit = { module -> editing = learner to module; viewModel.clearError() },
                    onReset = { module -> resetting = learner to module; viewModel.clearError() })
            } else Card(colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(learner.name, fontWeight = FontWeight.SemiBold, color = navy, fontSize = 18.sp)
                    Text(learner.email, color = Color(0xFF64748B), fontSize = 12.sp)
                    Text("${dashboard.assignment.completionPercent}% • ${dashboard.assignment.completedModuleIds.size}/${dashboard.program.modules.size} chặng đã đạt")
                    Text(dashboard.activeModule?.title ?: "Đã hoàn thành chương trình", color = navy)
                    if (section == 0) {
                        OutlinedButton(onClick = { section = 1 }) { Text("Quản lý nhiệm vụ") }
                    }
                    if (section == 2 && evidence == null && dashboard.reviewedEvidence.isEmpty()) {
                        Text("Chưa có bài nộp. Học viên cần hoàn thành nhiệm vụ của chặng đang mở.")
                    }
                    if (evidence != null) {
                        Text(dashboard.orderedModules.find { it.id == evidence.moduleId }?.evidenceTaskTitle ?: "Bài thực hành",
                            fontWeight = FontWeight.Medium)
                        Text("Nộp lúc: ${java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.forLanguageTag("vi-VN")).format(java.util.Date(evidence.submittedAtMillis))}",
                            fontSize = 12.sp, color = Color(0xFF64748B))
                        Text(when (evidence.status) {
                            "PENDING" -> "Bằng chứng chờ nghiệm thu"
                            "REJECTED" -> "Đã yêu cầu bổ sung"
                            "APPROVED" -> "Bằng chứng đã được nghiệm thu"
                            else -> "Chưa có bằng chứng chờ nghiệm thu"
                        }, color = Color(0xFF64748B))
                        if (evidence.note.isNotBlank()) Text(evidence.note)
                        TextButton(onClick = {
                            runCatching { uriHandler.openUri(evidence.link) }.onFailure {
                                Toast.makeText(context, "Không mở được liên kết bằng chứng.", Toast.LENGTH_LONG).show()
                            }
                        }) { Text("Mở bằng chứng") }
                        if (evidence.feedback.isNotBlank()) Text("Nhận xét: ${evidence.feedback}")
                    }
                    if (section == 2) {
                        dashboard.reviewedEvidence.values.sortedBy { it.submittedAtMillis }
                            .filter { it != evidence }.forEach { record ->
                                HorizontalDivider()
                                val module = dashboard.orderedModules.find { it.id == record.moduleId }
                                Text(module?.evidenceTaskTitle ?: "Bài thực hành", fontWeight = FontWeight.Medium)
                                Text(if (record.status == "APPROVED") "Đã nghiệm thu" else "Cần bổ sung")
                                if (record.note.isNotBlank()) Text(record.note)
                                if (record.feedback.isNotBlank()) Text("Nhận xét: ${record.feedback}")
                                TextButton(onClick = {
                                    runCatching { uriHandler.openUri(record.link) }.onFailure {
                                        Toast.makeText(context, "Không mở được liên kết bằng chứng.", Toast.LENGTH_LONG).show()
                                    }
                                }) { Text("Xem bài nộp đã lưu") }
                            }
                    }
                    if (dashboard.currentEvidence?.status == "PENDING") {
                        Button(onClick = { approved = true; reviewing = learner; feedback = ""; viewModel.clearError() },
                            enabled = !state.saving && !state.loading,
                            colors = ButtonDefaults.buttonColors(containerColor = navy)) { Text("Nghiệm thu đạt") }
                        OutlinedButton(onClick = { approved = false; reviewing = learner; feedback = ""; viewModel.clearError() },
                            enabled = !state.saving && !state.loading) { Text("Yêu cầu bổ sung") }
                    }
                }
            }
        }
    }
    editing?.let { (learner, module) ->
        MentorTaskDialog(learner, module, state, onDismiss = { editing = null; viewModel.clearError() },
            onSave = { task -> viewModel.saveTask(learner, module.id, task) })
    }
    resetting?.let { (learner, module) ->
        AlertDialog(onDismissRequest = { if (!state.saving) resetting = null },
            title = { Text("Khôi phục nhiệm vụ chương trình?") },
            text = { Column {
                Text("Nội dung riêng của ${module.title} sẽ được thay bằng nhiệm vụ mặc định. Tiến độ và bài nộp được giữ nguyên.")
                state.error?.let { Text(it, color = Color(0xFFB91C1C)) }
            } },
            confirmButton = { TextButton(onClick = { viewModel.saveTask(learner, module.id, null) }, enabled = !state.saving) { Text("Khôi phục") } },
            dismissButton = { TextButton(onClick = { resetting = null; viewModel.clearError() }, enabled = !state.saving) { Text("Hủy") } })
    }
    reviewing?.let { learner ->
        AlertDialog(onDismissRequest = { if (!state.saving) { reviewing = null; viewModel.clearError() } },
            title = { Text(if (approved) "Nghiệm thu chặng học" else "Yêu cầu bổ sung bằng chứng") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(learner.name)
                    Text(learner.dashboard.currentEvidence?.let { evidence ->
                        learner.dashboard.program.modules.find { it.id == evidence.moduleId }?.title
                    } ?: "Bằng chứng thực hành")
                    OutlinedTextField(value = feedback, onValueChange = { feedback = it },
                        label = { Text(if (approved) "Nhận xét (không bắt buộc)" else "Nội dung cần bổ sung") },
                        enabled = !state.saving, modifier = Modifier.fillMaxWidth(), maxLines = 5)
                    state.error?.let { Text(it, color = Color(0xFFB91C1C)) }
                }
            },
            confirmButton = {
                TextButton(onClick = { viewModel.review(learner, approved, feedback) }, enabled = !state.saving) {
                    Text(if (state.saving) "Đang lưu…" else "Xác nhận")
                }
            },
            dismissButton = { TextButton(onClick = { reviewing = null; viewModel.clearError() }, enabled = !state.saving) { Text("Hủy") } })
    }
}


@Composable
private fun MentorTasksCard(learner: MentorLearner, enabled: Boolean,
    onEdit: (TrainingModule) -> Unit, onReset: (TrainingModule) -> Unit) {
    Card(colors = CardDefaults.cardColors(containerColor = Color.White)) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(learner.name, fontWeight = FontWeight.SemiBold, fontSize = 18.sp)
            Text(learner.dashboard.program.title, color = Color(0xFF64748B))
            Text("Mỗi chặng có một nhiệm vụ thực hành để nghiệm thu. Có thể ghi nhiều đầu việc và tiêu chí trong yêu cầu.", fontSize = 12.sp)
            learner.dashboard.tasks.forEach { task ->
                HorizontalDivider()
                val module = task.module
                Text("Chặng ${task.stage} • ${module.title}", fontWeight = FontWeight.Medium)
                Text(module.evidenceTaskTitle)
                Text(module.evidenceInstructions, fontSize = 12.sp, color = Color(0xFF64748B))
                module.taskDefinition?.dueDate?.takeIf { it.isNotBlank() }?.let { Text("Hạn nộp: $it", fontSize = 12.sp) }
                val editable = task.status != TrainingTaskStatus.DONE && task.status != TrainingTaskStatus.PENDING
                if (editable) {
                    OutlinedButton(onClick = { onEdit(module) }, enabled = enabled) {
                        Text(if (module.taskDefinition == null) "Soạn nhiệm vụ chặng ${task.stage}" else "Sửa nhiệm vụ chặng ${task.stage}")
                    }
                    if (module.taskDefinition != null) TextButton(onClick = { onReset(module) }, enabled = enabled) { Text("Dùng nhiệm vụ chương trình") }
                } else Text(if (task.status == TrainingTaskStatus.DONE) "Đã nghiệm thu — giữ nguyên đề bài."
                    else "Đang chờ duyệt — xử lý bài nộp trước khi đổi đề.", fontSize = 12.sp, color = Color(0xFF64748B))
            }
        }
    }
}

@Composable
private fun MentorTaskDialog(learner: MentorLearner, module: TrainingModule, state: MentorUiState,
    onDismiss: () -> Unit, onSave: (TrainingTaskDefinition) -> Unit) {
    var title by remember(learner.uid, module.id) { mutableStateOf(module.evidenceTaskTitle) }
    var instructions by remember(learner.uid, module.id) { mutableStateOf(module.evidenceInstructions) }
    var dueDate by remember(learner.uid, module.id) { mutableStateOf(module.taskDefinition?.dueDate.orEmpty()) }
    var resourceLink by remember(learner.uid, module.id) { mutableStateOf(module.taskDefinition?.resourceLink.orEmpty()) }
    AlertDialog(onDismissRequest = { if (!state.saving) onDismiss() },
        title = { Text("Giao nhiệm vụ thực hành") },
        text = { Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("${learner.name} • ${module.title}")
            OutlinedTextField(title, { title = it }, label = { Text("Tên nhiệm vụ") }, modifier = Modifier.fillMaxWidth(), enabled = !state.saving)
            OutlinedTextField(instructions, { instructions = it }, label = { Text("Yêu cầu & tiêu chí nghiệm thu") },
                modifier = Modifier.fillMaxWidth(), enabled = !state.saving, minLines = 3)
            OutlinedTextField(dueDate, { dueDate = it }, label = { Text("Hạn nộp (dd/MM/yyyy)") },
                modifier = Modifier.fillMaxWidth(), enabled = !state.saving, singleLine = true)
            OutlinedTextField(resourceLink, { resourceLink = it }, label = { Text("Liên kết học liệu (HTTPS)") },
                modifier = Modifier.fillMaxWidth(), enabled = !state.saving)
            Text("Hạn nộp dùng để theo dõi; nộp muộn vẫn được ghi nhận. Nhiệm vụ ở chặng sau chỉ mở sau khi nghiệm thu chặng trước.", fontSize = 12.sp)
            state.error?.let { Text(it, color = Color(0xFFB91C1C)) }
        } },
        confirmButton = { TextButton(onClick = { onSave(TrainingTaskDefinition(title, instructions, dueDate, resourceLink)) }, enabled = !state.saving) {
            Text(if (state.saving) "Đang lưu…" else "Lưu & giao nhiệm vụ")
        } },
        dismissButton = { TextButton(onClick = onDismiss, enabled = !state.saving) { Text("Hủy") } })
}
