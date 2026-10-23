package com.example.eduvault.feature.tasks.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Assignment
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.PendingActions
import androidx.compose.material.icons.automirrored.outlined.FactCheck
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.LocalContext
import android.widget.Toast
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.example.eduvault.core.theme.PlusJakartaSansFamily
import com.example.eduvault.domain.model.*
import com.example.eduvault.feature.home.ui.TrainingUiState
import com.example.eduvault.feature.home.ui.components.TrainingAvailabilityCard
import com.example.eduvault.feature.home.ui.components.SectionHeader

private enum class TaskFilter(val label: String, val status: TrainingTaskStatus? = null) {
    ALL("Tất cả"), TODO("Cần làm", TrainingTaskStatus.TODO),
    PENDING("Chờ duyệt", TrainingTaskStatus.PENDING), DONE("Đã đạt", TrainingTaskStatus.DONE)
}

@Composable
fun TaskCenterScreen(
    trainingState: TrainingUiState,
    quizzes: List<QuizSet>,
    paddingValues: PaddingValues,
    onSubmitEvidence: () -> Unit,
    onStartQuiz: (QuizSet) -> Unit,
    onRetry: () -> Unit,
    quizError: String? = null,
) {
    var filter by rememberSaveable { mutableStateOf(TaskFilter.ALL) }
    val dashboard = trainingState.dashboard
    val tasks = dashboard?.tasks.orEmpty()
    val relevantQuizzes = quizzes.filter { quiz -> tasks.any { it.module.id == quiz.trainingModuleId } }
    LazyColumn(Modifier.fillMaxSize().padding(bottom = paddingValues.calculateBottomPadding())
        .statusBarsPadding().testTag("task_center"), contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item {
            Text("Trung tâm Nhiệm vụ", fontFamily = PlusJakartaSansFamily,
                fontSize = 24.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF0F172A))
            Spacer(Modifier.height(6.dp))
            Text("Theo dõi đầu việc thực hành và tự kiểm tra kiến thức trong chương trình onboarding.",
                fontSize = 12.sp, lineHeight = 18.sp, color = Color(0xFF64748B))
        }
        if (quizError != null) item {
            Text(quizError, color = Color(0xFFB91C1C), fontSize = 12.sp)
        }
        if (dashboard == null || trainingState.error != null) item {
            TrainingAvailabilityCard(isLoading = trainingState.isLoading, message = trainingState.error,
                onRetry = onRetry)
        }
        if (dashboard?.currentEvidence?.status == "REJECTED") item {
            Text("Mentor yêu cầu bổ sung: ${dashboard.currentEvidence?.feedback}", color = Color(0xFFB45309), fontSize = 13.sp)
        }
        if (dashboard != null) {
            item {
                val dueDate = SimpleDateFormat("dd/MM/yyyy", Locale.forLanguageTag("vi-VN"))
                    .format(Date(dashboard.assignment.dueAtMillis))
                Text("Thời hạn chương trình: $dueDate", fontSize = 11.sp,
                    color = Color(0xFF64748B), style = TextStyle(fontFeatureSettings = "tnum"))
            }
            item {
                Row(Modifier.height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(TrainingTaskStatus.TODO, TrainingTaskStatus.PENDING, TrainingTaskStatus.DONE).forEach { status ->
                        Surface(Modifier.weight(1f).fillMaxHeight(), shape = RoundedCornerShape(16.dp), color = Color.White) {
                            Column(Modifier.padding(12.dp)) {
                                Text(tasks.count { it.status == status }.toString(), fontSize = 24.sp,
                                    fontWeight = FontWeight.SemiBold, color = Color(0xFF0F172A),
                                    style = TextStyle(fontFeatureSettings = "tnum"))
                                Text(if (status == TrainingTaskStatus.DONE) "Hoàn thành" else status.label,
                                    fontSize = 11.sp, lineHeight = 16.sp, color = Color(0xFF64748B))
                            }
                        }
                    }
                }
            }
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(TaskFilter.entries) { option ->
                        FilterChip(selected = filter == option, onClick = { filter = option },
                            label = { Text(option.label) })
                    }
                }
            }
            item { SectionHeader("Đầu việc được giao") }
            val visible = tasks.filter { filter.status == null || it.status == filter.status }
            if (visible.isEmpty()) item {
                Text("Không có đầu việc trong mục này.", color = Color(0xFF64748B), fontSize = 12.sp)
            }
            items(visible, key = { it.module.id }) { task ->
                TaskCard(task, dashboard.reviewedEvidence[task.module.id] ?: dashboard.evidence?.takeIf { it.moduleId == task.module.id }, onSubmitEvidence)
            }
            if (relevantQuizzes.isNotEmpty() && filter == TaskFilter.ALL) {
                item { SectionHeader("Bài tự kiểm tra kiến thức") }
                item {
                    Text("Kết quả luyện tập không tự mở khóa chặng. Mentor nghiệm thu bằng chứng thực hành.",
                        color = Color(0xFF64748B), fontSize = 11.sp, lineHeight = 17.sp)
                }
                items(relevantQuizzes, key = { "quiz-${it.id}" }) { quiz ->
                    val task = tasks.first { it.module.id == quiz.trainingModuleId }
                    Surface(shape = RoundedCornerShape(16.dp), color = Color.White) {
                        Column(Modifier.fillMaxWidth().padding(16.dp)) {
                            Icon(Icons.AutoMirrored.Outlined.FactCheck, null, tint = Color(0xFF475569))
                            Spacer(Modifier.height(8.dp))
                            Text(quiz.title, fontWeight = FontWeight.SemiBold, fontSize = 14.sp,
                                color = Color(0xFF0F172A))
                            Text("Chặng ${task.stage} • ${quiz.questionCount} câu hỏi", fontSize = 11.sp,
                                color = Color(0xFF64748B))
                            Spacer(Modifier.height(12.dp))
                            OutlinedButton(
                                onClick = { onStartQuiz(quiz) },
                                enabled = task.status != TrainingTaskStatus.LOCKED,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF0F172A)),
                            ) {
                                Text(if (task.status == TrainingTaskStatus.LOCKED) "Chưa mở" else "Làm bài tự kiểm tra")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TaskCard(task: TrainingTask, evidence: TrainingEvidence?, onSubmitEvidence: () -> Unit) {
    val uri = LocalUriHandler.current
    val context = LocalContext.current
    fun openLink(link: String) {
        runCatching { uri.openUri(link) }.onFailure {
            Toast.makeText(context, "Không mở được liên kết. Vui lòng thử lại.", Toast.LENGTH_LONG).show()
        }
    }
    val color = when (task.status) {
        TrainingTaskStatus.DONE -> Color(0xFF047857)
        TrainingTaskStatus.TODO, TrainingTaskStatus.PENDING -> Color(0xFFB45309)
        TrainingTaskStatus.LOCKED -> Color(0xFF64748B)
    }
    Surface(shape = RoundedCornerShape(16.dp), color = Color.White) {
        Column(Modifier.fillMaxWidth().padding(16.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Icon(when (task.status) {
                    TrainingTaskStatus.DONE -> Icons.Outlined.Check
                    TrainingTaskStatus.PENDING -> Icons.Outlined.PendingActions
                    TrainingTaskStatus.LOCKED -> Icons.Outlined.Lock
                    else -> Icons.AutoMirrored.Outlined.Assignment
                }, null, tint = color, modifier = Modifier.size(22.dp))
                Column(Modifier.weight(1f)) {
                    Text(task.module.evidenceTaskTitle, fontFamily = PlusJakartaSansFamily,
                        fontSize = 14.sp, fontWeight = FontWeight.SemiBold,
                        color = if (task.status == TrainingTaskStatus.LOCKED) Color(0xFF64748B) else Color(0xFF0F172A))
                    Spacer(Modifier.height(4.dp))
                    Text("Chặng ${task.stage} • ${task.status.label}", fontSize = 11.sp, color = color)
                    Spacer(Modifier.height(6.dp))
                    Text(if (task.status == TrainingTaskStatus.LOCKED) "Mở sau khi mentor nghiệm thu chặng trước."
                        else task.module.evidenceInstructions, fontSize = 12.sp, lineHeight = 18.sp, color = Color(0xFF64748B))
                }
            }
            if (task.status != TrainingTaskStatus.LOCKED) {
                task.module.taskDefinition?.dueDate?.takeIf { it.isNotBlank() }?.let {
                    Text("Hạn nộp: $it", fontSize = 12.sp, color = Color(0xFF64748B))
                }
                task.module.taskDefinition?.resourceLink?.takeIf { it.isNotBlank() }?.let { link ->
                    TextButton(onClick = { openLink(link) }) { Text("Mở học liệu nhiệm vụ") }
                }
                if (evidence?.status == "APPROVED") {
                    if (evidence.feedback.isNotBlank()) Text("Nhận xét: ${evidence.feedback}", fontSize = 12.sp)
                    TextButton(onClick = { openLink(evidence.link) }) { Text("Xem bài đã nghiệm thu") }
                }
            }
            if (task.status == TrainingTaskStatus.TODO || task.status == TrainingTaskStatus.PENDING) {
                Spacer(Modifier.height(12.dp))
                Button(onClick = onSubmitEvidence, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F172A)),
                    shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
                    Text(if (task.status == TrainingTaskStatus.PENDING) "Xem / cập nhật bằng chứng" else "Nộp bằng chứng")
                }
            }
        }
    }
}
