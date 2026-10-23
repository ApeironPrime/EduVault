package com.example.eduvault.feature.home.ui.components

import android.widget.Toast
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.CloudUpload
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material.icons.outlined.PlayCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.withStyle
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.remember
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.eduvault.core.theme.ColorNavyDark
import com.example.eduvault.core.theme.PlusJakartaSansFamily
import com.example.eduvault.domain.model.TrainingDashboard
import com.example.eduvault.domain.model.stageStatusLabel
import com.example.eduvault.domain.model.evidenceInstructions

@Composable
fun TrainingMilestonesCard(dashboard: TrainingDashboard, modifier: Modifier = Modifier, onSubmitEvidence: () -> Unit) {
    val context = LocalContext.current
    val modules = dashboard.orderedModules
    val completed = dashboard.assignment.completedModuleIds
    Column(modifier.fillMaxWidth()) {
        Text("Lộ trình ${modules.size} chặng", fontFamily = PlusJakartaSansFamily,
            fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = Color(0xFF334155),
            modifier = Modifier.padding(bottom = 10.dp))
        Card(modifier = Modifier.fillMaxWidth().trainingCardShadow(RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
            Column {
                modules.forEachIndexed { index, module ->
                    val done = module.id in completed
                    val active = dashboard.canSubmit(module.id)
                    val locked = !done && !active
                    val color = if (done) Color(0xFF059669) else if (locked) Color(0xFF64748B) else Color(0xFFD97706)
                    val background = if (done) Color(0xFF10B981) else if (locked) Color(0xFFF1F5F9) else Color(0xFFFEF3C7)
                    MilestoneItemRow(
                        iconBgColor = background,
                        iconContent = { Icon(if (done) Icons.Outlined.Check else if (locked) Icons.Filled.Lock else Icons.Outlined.PlayCircle,
                            contentDescription = null, tint = if (done) Color.White else color, modifier = Modifier.size(20.dp)) },
                        title = if (module.title.startsWith("Chặng")) module.title else "Chặng ${index + 1}: ${module.title}",
                        subtitle = module.evidenceInstructions,
                        statusText = dashboard.stageStatusLabel(module.id),
                        statusColor = color, isLocked = locked,
                        onClick = { if (active) onSubmitEvidence() else Toast.makeText(context,
                            if (locked) "Chờ mentor duyệt chặng trước để mở khóa." else "Chặng này đã hoàn thành.", Toast.LENGTH_LONG).show() },
                        incomingComplete = if (index > 0) modules[index - 1].id in completed else null,
                        outgoingComplete = if (index < modules.lastIndex) done else null,
                        isActive = active,
                    )
                }
            }
        }
    }
}

/** Progress remains driven by the assignment; lighting is decorative only. */
@Composable
fun HeroProgressCard(
    modifier: Modifier = Modifier,
    completionPercent: Int = 33,
    targetText: String = "Sẵn sàng tham gia dự án trước 15/10",
    statusNote: String = "Đã nghiệm thu chặng 1 • Đang làm chặng 2",
) {
    val shape = RoundedCornerShape(18.dp)
    Column(
        modifier = modifier.fillMaxWidth()
            .shadow(6.dp, shape).clip(shape)
            .background(Color(0xFF0A0E17))
            .drawBehind {
                drawRect(Brush.radialGradient(
                    colors = listOf(Color(0xFF10B981).copy(alpha = 0.12f), Color.Transparent),
                    center = Offset(size.width * 0.9f, size.height * 0.12f),
                    radius = size.width * 0.65f,
                ))
            }
            .border(0.5.dp, Brush.verticalGradient(listOf(
                Color.White.copy(alpha = 0.15f), Color.White.copy(alpha = 0.02f),
            )), shape)
            .padding(20.dp),
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically) {
            Text("Tiến độ thử việc", fontFamily = PlusJakartaSansFamily,
                fontWeight = FontWeight.Medium, fontSize = 12.sp, color = Color(0xFF94A3B8))
            Text("${completionPercent.coerceIn(0, 100)}%", fontFamily = PlusJakartaSansFamily,
                fontWeight = FontWeight.SemiBold, fontSize = 28.sp, color = Color.White,
                style = TextStyle(fontFeatureSettings = "tnum"))
        }
        Spacer(Modifier.height(14.dp))
        val progress = completionPercent.coerceIn(0, 100) / 100f
        Box(Modifier.fillMaxWidth().height(8.dp).clip(CircleShape)
            .background(Color.White.copy(alpha = 0.08f))
            .drawBehind {
                val width = size.width * progress
                if (width > 0f) {
                    drawRoundRect(Brush.horizontalGradient(listOf(Color(0xFF059669), Color(0xFF10B981)),
                        endX = width), size = androidx.compose.ui.geometry.Size(width, size.height),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(size.height / 2))
                    drawCircle(Brush.radialGradient(listOf(Color(0xFFA7F3D0).copy(alpha = 0.65f),
                        Color.Transparent), center = Offset((width - 1.dp.toPx()).coerceAtLeast(0f), size.height / 2),
                        radius = 3.dp.toPx()), radius = 3.dp.toPx(),
                        center = Offset((width - 1.dp.toPx()).coerceAtLeast(0f), size.height / 2))
                }
            })
        Spacer(Modifier.height(16.dp))
        Text("🎯 Mục tiêu: $targetText", fontFamily = PlusJakartaSansFamily,
            fontWeight = FontWeight.Medium, fontSize = 13.sp, color = Color(0xFFF1F5F9))
        Spacer(Modifier.height(4.dp))
        Text(statusNote, fontFamily = PlusJakartaSansFamily, fontSize = 11.5.sp,
            color = Color(0xFF94A3B8))
    }
}

@Composable
private fun MilestoneItemRow(
    iconBgColor: Color,
    iconContent: @Composable () -> Unit,
    title: String,
    subtitle: String,
    statusText: String,
    statusColor: Color,
    isLocked: Boolean,
    onClick: () -> Unit,
    incomingComplete: Boolean? = null,
    outgoingComplete: Boolean? = null,
    isActive: Boolean = false,
) {
    val pulse = if (isActive) {
        val transition = rememberInfiniteTransition(label = "active_stage")
        val phase by transition.animateFloat(0f, 1f,
            infiniteRepeatable(tween(1800), RepeatMode.Restart), label = "stage_pulse")
        phase
    } else 1f

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .drawBehind {
                val x = 35.dp.toPx()
                val centerY = 33.dp.toPx()
                val radius = 19.dp.toPx()
                fun connector(start: Float, end: Float, complete: Boolean) {
                    if (complete) {
                        drawLine(Color(0xFF10B981), Offset(x, start), Offset(x, end),
                            strokeWidth = 2.dp.toPx())
                    } else {
                        // Align dots to the node, so adjacent rows share a continuous rhythm.
                        val spacing = 4.dp.toPx()
                        var y = start + spacing / 2
                        while (y < end) {
                            drawCircle(Color(0xFFCBD5E1), 1.dp.toPx(), Offset(x, y))
                            y += spacing
                        }
                    }
                }
                incomingComplete?.let { connector(0f, centerY - radius, it) }
                outgoingComplete?.let { connector(centerY + radius, size.height, it) }
                if (isActive) drawCircle(Color(0xFFF59E0B).copy(alpha = 0.4f * (1f - pulse)),
                    radius * (1f + 0.3f * pulse), Offset(x, centerY), style = Stroke(1.5.dp.toPx()))
            }
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.Top
    ) {
        // Icon tròn bên trái
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(iconBgColor),
            contentAlignment = Alignment.Center
        ) {
            iconContent()
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.Top) {
                Text(title, modifier = Modifier.weight(1f), fontFamily = PlusJakartaSansFamily,
                    fontWeight = FontWeight.SemiBold, fontSize = 13.sp,
                    color = if (isLocked) Color(0xFF64748B) else Color(0xFF0F172A))
                if (isActive) Icon(Icons.AutoMirrored.Outlined.KeyboardArrowRight,
                    contentDescription = null, tint = Color(0xFF94A3B8),
                    modifier = Modifier.padding(start = 6.dp).size(16.dp))
            }
            Spacer(Modifier.height(4.dp))
            Text(buildAnnotatedString {
                withStyle(SpanStyle(color = statusColor, fontWeight = FontWeight.Medium)) {
                    append(statusText)
                }
                if (subtitle.isNotBlank()) append(" • $subtitle")
            }, fontFamily = PlusJakartaSansFamily, fontSize = 11.5.sp,
                color = Color(0xFF64748B), lineHeight = 17.sp)
        }

    }
}

/** Task and receipt state belong to the currently unlocked stage. */
@Composable
fun FocusActionCard(
    onSubmitClick: () -> Unit,
    modifier: Modifier = Modifier,
    hasSubmitted: Boolean = false,
    stageNumber: Int = 1,
    taskTitle: String = "Nộp checklist quy chuẩn & bảo mật",
    taskDescription: String = "Đọc quy chuẩn, kiểm tra tài khoản và chia sẻ link checklist để mentor kiểm tra.",
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) 0.96f else 1f,
        spring(dampingRatio = Spring.DampingRatioMediumBouncy), label = "submit_press")
    val haptic = LocalHapticFeedback.current
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "Nhiệm vụ cần làm ngay",
            fontFamily = PlusJakartaSansFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 15.sp,
            color = Color(0xFF334155),
            letterSpacing = 0.sp,
            modifier = Modifier.padding(bottom = 10.dp)
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .trainingCardShadow(RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Tiêu đề nhiệm vụ
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "📌 $taskTitle",
                        fontFamily = PlusJakartaSansFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.5.sp,
                        color = Color(0xFF0F172A)
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Stage and review information.
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFFFEF3C7))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "Chặng $stageNumber",
                            fontFamily = PlusJakartaSansFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.5.sp,
                            color = Color(0xFFB45309)
                        )
                    }

                    Text(
                        text = "•",
                        color = Color(0xFFCBD5E1),
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "Mentor kiểm tra sau khi nộp",
                        fontFamily = PlusJakartaSansFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 11.5.sp,
                        color = Color(0xFFEA580C)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Mô tả
                Text(
                    text = taskDescription,
                    fontFamily = PlusJakartaSansFamily,
                    fontSize = 12.sp,
                    color = Color(0xFF64748B),
                    lineHeight = 17.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Press feedback does not change the submission flow.
                Button(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onSubmitClick()
                    },
                    interactionSource = interactionSource,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp).scale(scale),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (hasSubmitted) Color(0xFF047857) else ColorNavyDark,
                        contentColor = Color.White,
                    )
                ) {
                    Icon(
                        imageVector = if (hasSubmitted) Icons.Outlined.CheckCircle else Icons.Outlined.CloudUpload,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (hasSubmitted) "Đã gửi • Chờ duyệt" else "Nộp bằng chứng",
                        fontFamily = PlusJakartaSansFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.5.sp,
                        color = Color.White
                    )
                }
            }
        }
    }
}

/**
 * Hộp thoại (Dialog) nộp link GitHub / Ảnh minh chứng thẩm định
 */
@Composable
fun SubmitEvidenceDialog(
    onDismiss: () -> Unit,
    onSubmit: (link: String, note: String) -> Unit,
    isSubmitting: Boolean = false,
    errorMessage: String? = null,
    initialLink: String = "",
    initialNote: String = "",
    stageNumber: Int = 1,
    moduleTitle: String = "Quy chuẩn & bảo mật",
    instructions: String = "",
    mentorFeedback: String = "",
    submissionStatus: String? = null,
) {
    var gitLink by rememberSaveable { mutableStateOf(initialLink) }
    var note by rememberSaveable { mutableStateOf(initialNote) }
    val keyboard = LocalSoftwareKeyboardController.current

    Dialog(onDismissRequest = { if (!isSubmitting) onDismiss() }) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 600.dp)
                .shadow(16.dp, RoundedCornerShape(20.dp)),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp)
            ) {
                Text(
                    text = "Nộp bài thẩm định Chặng $stageNumber",
                    fontFamily = PlusJakartaSansFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = Color(0xFF0F172A)
                )
                Text(
                    text = moduleTitle,
                    fontFamily = PlusJakartaSansFamily,
                    fontSize = 12.sp,
                    color = Color(0xFF64748B),
                    modifier = Modifier.padding(top = 2.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (mentorFeedback.isNotBlank()) {
                    Text("Cần bổ sung: $mentorFeedback", color = Color(0xFFB45309), fontSize = 12.sp)
                    Spacer(Modifier.height(12.dp))
                }
                if (instructions.isNotBlank()) {
                    Text(instructions, color = Color(0xFF475569), fontSize = 12.sp)
                    Spacer(Modifier.height(12.dp))
                }
                if (submissionStatus == "PENDING") {
                    Text("Bằng chứng đang chờ mentor nghiệm thu.", color = Color(0xFFB45309), fontSize = 12.sp)
                    Spacer(Modifier.height(12.dp))
                }

                // Input: Link GitHub / PR
                Text(
                    text = "Liên kết bằng chứng (HTTPS)",
                    fontFamily = PlusJakartaSansFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.5.sp,
                    color = Color(0xFF334155)
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = gitLink,
                    onValueChange = { gitLink = it },
                    enabled = !isSubmitting,
                    placeholder = { Text("https://github.com/nhom/du-an/pull/1", fontSize = 12.sp) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Outlined.Link,
                            contentDescription = null,
                            tint = Color(0xFF64748B),
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    modifier = Modifier.fillMaxWidth().testTag("evidence_link"),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFD97706),
                        unfocusedBorderColor = Color(0xFFE2E8F0)
                    )
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Input: Ghi chú & ảnh
                Text(
                    text = "Ghi chú (không bắt buộc)",
                    fontFamily = PlusJakartaSansFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.5.sp,
                    color = Color(0xFF334155)
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    enabled = !isSubmitting,
                    minLines = 3,
                    maxLines = 4,
                    modifier = Modifier.fillMaxWidth().testTag("evidence_note"),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFD97706),
                        unfocusedBorderColor = Color(0xFFE2E8F0)
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Có thể dùng link Pull Request hoặc link ảnh cấu hình đã chia sẻ cho mentor.",
                    fontFamily = PlusJakartaSansFamily,
                    fontSize = 11.5.sp,
                    color = Color(0xFF64748B),
                )
                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(errorMessage, color = Color(0xFFB91C1C), fontSize = 12.sp)
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss, enabled = !isSubmitting) {
                        Text(
                            text = "Hủy",
                            fontFamily = PlusJakartaSansFamily,
                            color = Color(0xFF64748B),
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { keyboard?.hide(); onSubmit(gitLink, note) },
                        enabled = !isSubmitting,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ColorNavyDark)
                    ) {
                        if (isSubmitting) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Text(
                            text = if (isSubmitting) "Đang gửi…" else if (submissionStatus == "PENDING") "Cập nhật bằng chứng" else "Gửi thẩm định",
                            fontFamily = PlusJakartaSansFamily,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

