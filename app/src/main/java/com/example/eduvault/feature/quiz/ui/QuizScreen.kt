package com.example.eduvault.feature.quiz.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.HourglassEmpty
import androidx.compose.material.icons.outlined.LocalFireDepartment
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.automirrored.outlined.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.eduvault.core.theme.ColorAmber
import com.example.eduvault.core.theme.ColorAmberDark
import com.example.eduvault.core.theme.ColorAmberLight
import com.example.eduvault.core.theme.ColorBorder
import com.example.eduvault.core.theme.ColorCream
import com.example.eduvault.core.theme.ColorError
import com.example.eduvault.core.theme.ColorForest
import com.example.eduvault.core.theme.ColorForestLight
import com.example.eduvault.core.theme.ColorInk
import com.example.eduvault.core.theme.ColorInkLight
import com.example.eduvault.core.theme.ColorPaper
import com.example.eduvault.core.theme.ColorSuccess
import com.example.eduvault.core.theme.ColorTextOnDark
import com.example.eduvault.core.theme.ColorTextOnDarkSecondary
import com.example.eduvault.core.theme.ColorTextOnLight
import com.example.eduvault.core.theme.ColorTextOnLightSecondary
import com.example.eduvault.core.theme.DmSansFamily
import com.example.eduvault.core.theme.JetBrainsMonoFamily
import com.example.eduvault.core.theme.PlayfairDisplayFamily

@Composable
fun QuizContent(
    paddingValues: PaddingValues,
    viewModel: QuizViewModel = hiltViewModel(),
    onStartQuizSetup: (String) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ColorPaper)
    ) {
        when (uiState.screenMode) {
            QuizScreenMode.DASHBOARD -> {
                QuizDashboard(
                    uiState = uiState,
                    paddingValues = paddingValues,
                    viewModel = viewModel,
                    onStartQuizSetup = onStartQuizSetup
                )
            }
            QuizScreenMode.PLAYER -> {
                QuizPlayer(
                    uiState = uiState,
                    viewModel = viewModel
                )
            }
            QuizScreenMode.RESULTS -> {
                QuizResults(
                    uiState = uiState,
                    viewModel = viewModel
                )
            }
        }

        // Loading Overlay khi AI đang sinh câu hỏi trắc nghiệm
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.45f))
                    .clickable(enabled = false) {},
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    androidx.compose.material3.CircularProgressIndicator(
                        color = ColorAmber,
                        modifier = Modifier.size(44.dp)
                    )
                    Text(
                        text = "AI đang thiết kế đề thi trắc nghiệm cho bạn...",
                        fontFamily = DmSansFamily,
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// 1. QUIZ DASHBOARD SCREEN
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
private fun QuizDashboard(
    uiState: QuizUiState,
    paddingValues: PaddingValues,
    viewModel: QuizViewModel,
    onStartQuizSetup: (String) -> Unit
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        // Banner AI Luyện tập
        QuizBanner(onStartClick = { onStartQuizSetup("") })

        Spacer(modifier = Modifier.height(16.dp))

        // Progress Stats Grid
        QuizProgressGrid(progress = uiState.progress)

        Spacer(modifier = Modifier.height(20.dp))

        // ─── Available Quizzes Section ───────────────────────────────────────────
        SectionTitle(title = "Bộ đề ôn luyện trắc nghiệm")
        Spacer(modifier = Modifier.height(10.dp))

        if (uiState.quizSets.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White)
                    .border(1.5.dp, ColorBorder, RoundedCornerShape(16.dp))
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Chưa có đề luyện tập nào.\nHãy tạo đề thi AI từ tài liệu của bạn!",
                    fontFamily = DmSansFamily,
                    fontSize = 12.sp,
                    color = ColorTextOnLightSecondary,
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp
                )
            }
        } else {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 8.dp)
            ) {
                items(uiState.quizSets) { quizSet ->
                    QuizSetCard(
                        quizSet = quizSet,
                        onPlayClick = { viewModel.startQuiz(quizSet) },
                        modifier = Modifier.width(160.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Leaderboard List
        SectionTitle(title = "Bảng xếp hạng tuần")
        Spacer(modifier = Modifier.height(10.dp))
        LeaderboardCard(leaderboard = uiState.leaderboard)

        Spacer(modifier = Modifier.height(24.dp))
    }
}

// ─── Quiz Banner ──────────────────────────────────────────────────────────────

@Composable
private fun QuizBanner(onStartClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(ColorInk)
            .drawBehind {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFF9B59B6).copy(alpha = 0.22f), Color.Transparent),
                        center = Offset(size.width * 0.9f, size.height * 0.25f),
                        radius = size.width * 0.6f
                    ),
                    radius = size.width * 0.6f,
                    center = Offset(size.width * 0.9f, size.height * 0.25f),
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(ColorAmber.copy(alpha = 0.15f), Color.Transparent),
                        center = Offset(size.width * 0.3f, size.height * 0.8f),
                        radius = size.width * 0.5f
                    ),
                    radius = size.width * 0.5f,
                    center = Offset(size.width * 0.3f, size.height * 0.8f),
                )
            }
            .padding(22.dp)
    ) {
        Column {
            Text(
                text = "✦  AI-POWERED ASSISTANT",
                fontFamily = JetBrainsMonoFamily,
                fontSize = 9.sp,
                color = ColorAmber,
                letterSpacing = 1.5.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Text(
                text = buildAnnotatedString {
                    append("Luyện Quiz ")
                    withStyle(SpanStyle(color = ColorAmber, fontStyle = FontStyle.Italic)) {
                        append("thông minh")
                    }
                },
                fontFamily = PlayfairDisplayFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
                color = ColorTextOnDark,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Text(
                text = "Sinh câu hỏi từ chính file tài liệu hoặc bài giảng của bạn. Tự động tinh chỉnh độ khó bám sát tiến độ học tập.",
                fontFamily = DmSansFamily,
                fontSize = 12.5.sp,
                lineHeight = 18.sp,
                color = ColorTextOnDarkSecondary,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(ColorAmber)
                        .clickable { onStartClick() }
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Bắt đầu ôn luyện  →",
                        fontFamily = DmSansFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.5.sp,
                        color = ColorInk,
                    )
                }
            }
        }
    }
}

// ─── Progress Stats Grid ──────────────────────────────────────────────────────

@Composable
private fun QuizProgressGrid(progress: QuizProgress) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        ProgressCard(
            emoji = "📊",
            value = if (progress.avgScore > 0f) String.format(Locale.ROOT, "%.1f", progress.avgScore) else "-",
            label = "Điểm trung bình",
            fraction = (progress.avgScore / 10f).coerceIn(0f, 1f),
            bgCircleColor = ColorForest.copy(alpha = 0.08f),
            modifier = Modifier.weight(1f)
        )
        ProgressCard(
            emoji = "🧩",
            value = "${progress.completedCount}",
            label = "Đề đã hoàn thành",
            fraction = if (progress.completedCount > 0) 1f else 0f,
            bgCircleColor = ColorAmber.copy(alpha = 0.08f),
            modifier = Modifier.weight(1f)
        )
        ProgressCard(
            emoji = "🎯",
            value = "${progress.completionRate}%",
            label = "Tỷ lệ chính xác",
            fraction = (progress.completionRate / 100f).coerceIn(0f, 1f),
            bgCircleColor = Color(0xFF9F7AEA).copy(alpha = 0.08f),
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun ProgressCard(
    emoji: String,
    value: String,
    label: String,
    fraction: Float,
    bgCircleColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, ColorBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(bgCircleColor),
                contentAlignment = Alignment.Center
            ) {
                Text(text = emoji, fontSize = 16.sp)
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = value,
                fontFamily = JetBrainsMonoFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = ColorInk
            )

            Text(
                text = label,
                fontFamily = DmSansFamily,
                fontSize = 9.5.sp,
                color = ColorTextOnLightSecondary,
                lineHeight = 13.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Mini progress bar — width reflects actual fraction
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .clip(CircleShape)
                    .background(ColorBorder)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(if (value.contains("/")) 0.85f else 0.92f)
                        .fillMaxSize()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(ColorAmber, ColorForest)
                            )
                        )
                )
            }
        }
    }
}

// ─── Subject Chips Row ────────────────────────────────────────────────────────

@Composable
private fun QuizSubjectChipsRow(
    selectedSubject: String,
    onSubjectSelected: (String) -> Unit
) {
    val subjects = listOf("Tất cả", "Kinh tế vi mô", "Marketing", "Thống kê", "Luật", "Tài chính")
    
    // Hash code color helper with safety against negative hashcodes
    val colors = listOf(Color(0xFF2D6A4F), Color(0xFFE8A020), Color(0xFF4A5568), Color(0xFFC0392B), Color(0xFF9F7AEA))
    
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 2.dp)
    ) {
        items(subjects) { subject ->
            val isSelected = selectedSubject == subject
            
            val hashCode = subject.hashCode()
            val size = colors.size
            val index = (hashCode % size).let { if (it < 0) it + size else it }
            val accentColor = if (subject == "Tất cả") ColorInk else colors[index]

            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(if (isSelected) accentColor else Color.White)
                    .border(
                        1.5.dp,
                        if (isSelected) accentColor else ColorBorder,
                        CircleShape
                    )
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) {
                        onSubjectSelected(subject)
                    }
                    .padding(horizontal = 14.dp, vertical = 7.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = subject,
                    fontFamily = DmSansFamily,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                    fontSize = 12.sp,
                    color = if (isSelected) Color.White else ColorTextOnLightSecondary
                )
            }
        }
    }
}

// ─── Quiz Set Card ────────────────────────────────────────────────────────────

@Composable
private fun QuizSetCard(
    quizSet: QuizSet,
    onPlayClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val thumbBgs = listOf(
        listOf(Color(0xFFE8F4FD), Color(0xFFBEE3F8)),
        listOf(Color(0xFFFEF9F0), Color(0xFFFDF0D5)),
        listOf(Color(0xFFF0FAF5), Color(0xFFD4EDDA)),
        listOf(Color(0xFFFDF2F8), Color(0xFFF3D5E8)),
        listOf(Color(0xFFF5F0FF), Color(0xFFEAD5FF)),
        listOf(Color(0xFFFFF5F0), Color(0xFFFDE8D8))
    )

    val currentBg = thumbBgs.getOrElse(quizSet.bgIndex) { thumbBgs[0] }

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, ColorBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column {
            // Cover Area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .background(Brush.linearGradient(currentBg)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = when (quizSet.bgIndex) {
                        0 -> "📈"
                        1 -> "🎯"
                        2 -> "📊"
                        3 -> "⚖️"
                        4 -> "💰"
                        else -> "🧫"
                    },
                    fontSize = 32.sp
                )

                // Difficulty badge on the top right
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .clip(CircleShape)
                        .background(
                            when (quizSet.difficulty) {
                                Difficulty.EASY -> ColorForest.copy(alpha = 0.15f)
                                Difficulty.MEDIUM -> ColorAmber.copy(alpha = 0.15f)
                                Difficulty.HARD -> ColorError.copy(alpha = 0.12f)
                            }
                        )
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = quizSet.difficulty.label,
                        fontFamily = JetBrainsMonoFamily,
                        fontSize = 8.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = when (quizSet.difficulty) {
                            Difficulty.EASY -> ColorForest
                            Difficulty.MEDIUM -> ColorAmberDark
                            Difficulty.HARD -> ColorError
                        }
                    )
                }

                // New badge on top left
                if (quizSet.isNew) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(8.dp)
                            .clip(CircleShape)
                            .background(ColorAmber)
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "MỚI",
                            fontFamily = JetBrainsMonoFamily,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = ColorInk
                        )
                    }
                }
            }

            // Body content
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = quizSet.subject.uppercase(),
                    fontFamily = JetBrainsMonoFamily,
                    fontSize = 8.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = ColorTextOnLightSecondary,
                    letterSpacing = 0.5.sp
                )

                Spacer(modifier = Modifier.height(3.dp))

                Text(
                    text = quizSet.title,
                    fontFamily = DmSansFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.5.sp,
                    color = ColorInk,
                    lineHeight = 17.sp,
                    maxLines = 2,
                    minLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Footer metadata and Play Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Text(text = "🧩", fontSize = 10.sp)
                            Text(
                                text = "${quizSet.questionCount} câu",
                                fontFamily = DmSansFamily,
                                fontSize = 10.5.sp,
                                color = ColorTextOnLightSecondary
                            )
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Text(text = "👥", fontSize = 10.sp)
                            Text(
                                text = quizSet.playCount,
                                fontFamily = DmSansFamily,
                                fontSize = 10.5.sp,
                                color = ColorTextOnLightSecondary
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(ColorInk)
                            .clickable { onPlayClick() }
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Luyện",
                            fontFamily = DmSansFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

// ─── Leaderboard Card ─────────────────────────────────────────────────────────

@Composable
private fun LeaderboardCard(leaderboard: List<LeaderboardUser>) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, ColorBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column {
            leaderboard.forEachIndexed { index, user ->
                val isMe = user.isMe
                val itemBackground = if (isMe) {
                    Brush.linearGradient(
                        colors = listOf(ColorAmber.copy(alpha = 0.08f), ColorForest.copy(alpha = 0.05f))
                    )
                } else {
                    Brush.linearGradient(colors = listOf(Color.White, Color.White))
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(itemBackground)
                        .drawBehind {
                            if (isMe) {
                                // Draw accent left border for user row
                                drawRect(
                                    color = ColorAmber,
                                    topLeft = Offset.Zero,
                                    size = Size(width = 3.dp.toPx(), height = size.height)
                                )
                            }
                        }
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Rank representation
                    Text(
                        text = when (user.rank) {
                            1 -> "🥇"
                            2 -> "🥈"
                            3 -> "🥉"
                            else -> "#${user.rank}"
                        },
                        fontFamily = JetBrainsMonoFamily,
                        fontSize = if (user.rank <= 3) 16.sp else 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = when (user.rank) {
                            12 -> ColorAmberDark
                            else -> ColorInk
                        },
                        modifier = Modifier.width(32.dp),
                        textAlign = TextAlign.Center
                    )

                    // Avatar placeholder
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(ColorForestLight, ColorAmber)
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = user.name.firstOrNull()?.toString() ?: "?",
                            fontFamily = DmSansFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = Color.White
                        )
                    }

                    // User text info
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = user.name + if (isMe) " (Bạn)" else "",
                            fontFamily = DmSansFamily,
                            fontWeight = if (isMe) FontWeight.Bold else FontWeight.SemiBold,
                            fontSize = 13.sp,
                            color = ColorInk
                        )
                        if (user.university.isNotEmpty()) {
                            Text(
                                text = user.university,
                                fontFamily = DmSansFamily,
                                fontSize = 10.5.sp,
                                color = ColorTextOnLightSecondary
                            )
                        }
                    }

                    // Score
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "${user.score}",
                            fontFamily = JetBrainsMonoFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = ColorInk
                        )
                        Text(
                            text = "XP",
                            fontFamily = DmSansFamily,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = ColorTextOnLightSecondary
                        )
                    }
                }

                // Add thin divider
                if (index < leaderboard.size - 1) {
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

// ═══════════════════════════════════════════════════════════════════════════════
// 2. QUIZ PLAYER SCREEN (GAMEPLAY)
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
private fun QuizPlayer(
    uiState: QuizUiState,
    viewModel: QuizViewModel
) {
    val totalQuestions = uiState.questions.size
    val currentQuestion = uiState.questions.getOrNull(uiState.currentQuestionIndex) ?: return

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ColorPaper)
    ) {
        // Player Header
        QuizPlayerHeader(
            activeQuizSet = uiState.activeQuizSet,
            currentIndex = uiState.currentQuestionIndex,
            totalCount = totalQuestions,
            remainingSeconds = uiState.remainingSeconds,
            quizFormat = uiState.quizFormat,
            onExitClick = viewModel::exitQuiz
        )

        // Player Body Scrollable Column
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Main Question Card
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, ColorBorder),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    // Header row: câu số + format badge
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "CÂU HỎI ${uiState.currentQuestionIndex + 1} CỦA $totalQuestions",
                            fontFamily = JetBrainsMonoFamily,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = ColorAmberDark,
                            letterSpacing = 1.sp
                        )
                        // Badge hình thức câu hỏi (chỉ hiển thị khi không phải Trắc nghiệm)
                        if (uiState.quizFormat != "Trắc nghiệm") {
                            val (badgeLabel, badgeBg, badgeFg) = when (uiState.quizFormat) {
                                "Đúng / Sai" -> Triple("✔ Đ/S", Color(0xFFE8F5E9), ColorForest)
                                "Điền khuyết" -> Triple("✏ Điền", ColorAmber.copy(alpha = 0.15f), ColorAmberDark)
                                else -> Triple(uiState.quizFormat, ColorCream, ColorInk)
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(5.dp))
                                    .background(badgeBg)
                                    .border(1.dp, badgeFg.copy(alpha = 0.4f), RoundedCornerShape(5.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = badgeLabel,
                                    fontFamily = JetBrainsMonoFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 8.5.sp,
                                    color = badgeFg
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Hiển thị text câu hỏi: highlight ___ với màu Amber cho Điền khuyết
                    val questionText = currentQuestion.text
                    if (questionText.contains("___")) {
                        val annotated = buildAnnotatedString {
                            val parts = questionText.split("___")
                            parts.forEachIndexed { i, part ->
                                append(part)
                                if (i < parts.size - 1) {
                                    withStyle(
                                        style = SpanStyle(
                                            color = ColorAmberDark,
                                            fontWeight = FontWeight.ExtraBold,
                                            background = ColorAmber.copy(alpha = 0.18f)
                                        )
                                    ) {
                                        append("  ▢ điền vào đây  ")
                                    }
                                }
                            }
                        }
                        Text(
                            text = annotated,
                            fontFamily = DmSansFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = ColorInk,
                            lineHeight = 24.sp
                        )
                    } else {
                        Text(
                            text = questionText,
                            fontFamily = DmSansFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = ColorInk,
                            lineHeight = 22.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Answers Options List
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                currentQuestion.options.forEach { answer ->
                    AnswerButton(
                        answer = answer,
                        isSelected = uiState.selectedOption == answer.key,
                        isCorrect = currentQuestion.correctOption == answer.key,
                        isLocked = uiState.isAnswerLocked,
                        selectedOptionKey = uiState.selectedOption,
                        onClick = { viewModel.selectOption(answer.key) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Explanation Box
            AnimatedVisibility(
                visible = uiState.isAnswerLocked,
                enter = fadeIn() + slideInVertically(initialOffsetY = { 20 }),
                exit = fadeOut()
            ) {
                ExplanationBox(explanation = currentQuestion.explanation)
            }
        }

        // Bottom Action Bar
        QuizPlayerActionBar(
            currentIndex = uiState.currentQuestionIndex,
            totalCount = totalQuestions,
            isLocked = uiState.isAnswerLocked,
            answersMap = uiState.answersMap,
            questions = uiState.questions,
            onSkipClick = viewModel::skipQuestion,
            onNextClick = viewModel::nextQuestion
        )
    }
}

// ─── Player Header Component ──────────────────────────────────────────────────

@Composable
private fun QuizPlayerHeader(
    activeQuizSet: QuizSet?,
    currentIndex: Int,
    totalCount: Int,
    remainingSeconds: Int,
    quizFormat: String = "Trắc nghiệm",
    onExitClick: () -> Unit
) {
    val progress = (currentIndex + 1).toFloat() / totalCount

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(ColorInk)
            .statusBarsPadding()
            .padding(top = 8.dp)
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Exit button
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(ColorTextOnDark.copy(alpha = 0.1f))
                        .clickable { onExitClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                        contentDescription = "Thoát",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                // Subject title
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = activeQuizSet?.subject ?: "Quiz",
                        fontFamily = JetBrainsMonoFamily,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        color = ColorTextOnDarkSecondary
                    )
                    Text(
                        text = activeQuizSet?.title ?: "Bài trắc nghiệm",
                        fontFamily = DmSansFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                // Countdown Timer Box
                val timerBg = if (remainingSeconds <= 15) Color(0xFFC0392B).copy(alpha = 0.25f) else ColorTextOnDark.copy(alpha = 0.08f)
                val timerBorder = if (remainingSeconds <= 15) Color(0xFFE53E3E) else ColorTextOnDark.copy(alpha = 0.12f)
                val timerTextColor = if (remainingSeconds <= 15) Color(0xFFFC8181) else Color.White

                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(timerBg)
                        .border(1.dp, timerBorder, RoundedCornerShape(10.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.HourglassEmpty,
                        contentDescription = "Hẹn giờ",
                        tint = timerTextColor,
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        text = "${remainingSeconds}s",
                        fontFamily = JetBrainsMonoFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = timerTextColor
                    )
                }
            }

            // Top Horizontal Progress indicator
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .background(ColorTextOnDark.copy(alpha = 0.15f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progress)
                        .fillMaxSize()
                        .background(ColorAmber)
                )
            }
        }
    }
}

// ─── Answer Button Component ──────────────────────────────────────────────────

@Composable
private fun AnswerButton(
    answer: QuizAnswer,
    isSelected: Boolean,
    isCorrect: Boolean,
    isLocked: Boolean,
    selectedOptionKey: String?,
    onClick: () -> Unit
) {
    val containerBg = when {
        isLocked && isCorrect -> ColorForest.copy(alpha = 0.08f)
        isLocked && isSelected && !isCorrect -> ColorError.copy(alpha = 0.08f)
        isSelected -> ColorAmber.copy(alpha = 0.06f)
        else -> Color.White
    }

    val borderStroke = when {
        isLocked && isCorrect -> androidx.compose.foundation.BorderStroke(2.dp, ColorForest)
        isLocked && isSelected && !isCorrect -> androidx.compose.foundation.BorderStroke(2.dp, ColorError)
        isLocked && !isSelected && isCorrect -> androidx.compose.foundation.BorderStroke(1.5.dp, ColorForest)
        isSelected -> androidx.compose.foundation.BorderStroke(2.dp, ColorAmber)
        else -> androidx.compose.foundation.BorderStroke(1.5.dp, ColorBorder)
    }

    val keyBg = when {
        isLocked && isCorrect -> ColorForest
        isLocked && isSelected && !isCorrect -> ColorError
        isSelected -> ColorAmber
        else -> ColorCream
    }

    val keyText = when {
        isLocked && isCorrect -> Color.White
        isLocked && isSelected && !isCorrect -> Color.White
        isSelected -> ColorInk
        else -> ColorTextOnLightSecondary
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(13.dp))
            .background(containerBg)
            .border(borderStroke.width, borderStroke.brush, RoundedCornerShape(13.dp))
            .clickable(enabled = !isLocked) { onClick() }
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Option Key Capsule (A, B, C, D)
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(keyBg),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = answer.key,
                fontFamily = JetBrainsMonoFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = keyText
            )
        }

        // Option Content text
        Text(
            text = answer.text,
            fontFamily = DmSansFamily,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
            fontSize = 13.sp,
            color = ColorInk,
            lineHeight = 17.sp,
            modifier = Modifier.weight(1f)
        )

        // Result visual icon inside button
        if (isLocked) {
            if (isCorrect) {
                Icon(
                    imageVector = Icons.Outlined.CheckCircle,
                    contentDescription = "Đúng",
                    tint = ColorForest,
                    modifier = Modifier.size(18.dp)
                )
            } else if (isSelected) {
                Icon(
                    imageVector = Icons.Outlined.Close,
                    contentDescription = "Sai",
                    tint = ColorError,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

// ─── Explanation Box Component ────────────────────────────────────────────────

@Composable
private fun ExplanationBox(explanation: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(Color(0xFFF0FAF5), Color(0xFFFEFCF0))
                )
            )
            .border(1.5.dp, Color(0xFFD4EDDA), RoundedCornerShape(14.dp))
            .padding(14.dp)
    ) {
        Column {
            Text(
                text = "📚  GIẢI THÍCH ĐÁP ÁN",
                fontFamily = JetBrainsMonoFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 9.5.sp,
                color = ColorForest,
                letterSpacing = 0.5.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = explanation,
                fontFamily = DmSansFamily,
                fontSize = 12.5.sp,
                lineHeight = 18.sp,
                color = ColorTextOnLightSecondary
            )
        }
    }
}

// ─── Player Bottom Action Bar Component ────────────────────────────────────────

@Composable
private fun QuizPlayerActionBar(
    currentIndex: Int,
    totalCount: Int,
    isLocked: Boolean,
    answersMap: Map<Int, String?>,
    questions: List<QuizQuestion>,
    onSkipClick: () -> Unit,
    onNextClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .border(1.dp, ColorBorder)
            .navigationBarsPadding()
            .padding(horizontal = 14.dp, vertical = 12.dp)
    ) {
        // Question Dots progress indicators
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                for (i in 0 until totalCount) {
                    val isCurrent = i == currentIndex
                    val selectedOption = answersMap[i]
                    val correctOption = questions.getOrNull(i)?.correctOption

                    val dotColor = when {
                        isCurrent -> ColorAmber
                        selectedOption == null -> ColorBorder
                        selectedOption == "" -> ColorError.copy(alpha = 0.5f) // skipped/expired
                        selectedOption == correctOption -> ColorForest
                        else -> ColorError
                    }

                    val dotSize = if (isCurrent) 10.dp else 7.dp

                    Box(
                        modifier = Modifier
                            .size(dotSize)
                            .clip(CircleShape)
                            .background(dotColor)
                    )
                }
            }
        }

        // Action Buttons Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Hollow action button: Skip / Back
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .border(1.5.dp, ColorBorder, RoundedCornerShape(10.dp))
                    .background(Color.White)
                    .clickable { onSkipClick() }
                    .padding(vertical = 11.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (isLocked) "Bỏ qua giải thích" else "Bỏ qua câu",
                    fontFamily = DmSansFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    color = ColorTextOnLightSecondary
                )
            }

            // High action button: Next question
            val nextEnabled = isLocked
            val btnBg = if (nextEnabled) ColorInk else ColorInk.copy(alpha = 0.4f)
            val btnTextColor = if (nextEnabled) ColorAmber else ColorTextOnDarkSecondary

            Box(
                modifier = Modifier
                    .weight(1.2f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(btnBg)
                    .clickable(enabled = nextEnabled) { onNextClick() }
                    .padding(vertical = 11.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = if (currentIndex == totalCount - 1) "Hoàn thành" else "Tiếp theo",
                        fontFamily = DmSansFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = btnTextColor
                    )
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ArrowForward,
                        contentDescription = null,
                        tint = btnTextColor,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// 3. QUIZ RESULTS SCREEN
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
private fun QuizResults(
    uiState: QuizUiState,
    viewModel: QuizViewModel
) {
    val totalQuestions = uiState.questions.size
    val correctCount = uiState.correctAnswersCount
    val score = (correctCount.toFloat() / totalQuestions) * 10f
    
    // Formatting total minutes and seconds
    val minutes = uiState.timeSpentSeconds / 60
    val seconds = uiState.timeSpentSeconds % 60
    val timeLabel = String.format(java.util.Locale.US, "%02d:%02d", minutes, seconds)

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ColorPaper)
            .statusBarsPadding()
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        Spacer(modifier = Modifier.height(14.dp))

        // Vòng tròn điểm số score Canvas
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                ScoreRingCanvas(correctCount = correctCount, totalQuestions = totalQuestions)

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = when {
                        score >= 8.5f -> "NỖ LỰC TUYỆT VỜI! 🏆"
                        score >= 6.5f -> "KẾT QUẢ RẤT TỐT! 👍"
                        else -> "CẦN CỐ GẮNG HƠN! 💪"
                    },
                    fontFamily = PlayfairDisplayFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = ColorInk,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(3.dp))

                Text(
                    text = "Bạn đã nắm bắt nội dung chương học khá chi tiết.",
                    fontFamily = DmSansFamily,
                    fontSize = 12.sp,
                    color = ColorTextOnLightSecondary,
                    textAlign = TextAlign.Center
                )

                // Hiển thị hình thức bài kiểm tra
                if (uiState.quizFormat != "Trắc nghiệm") {
                    Spacer(modifier = Modifier.height(10.dp))
                    val (fmtLabel, fmtBg, fmtFg) = when (uiState.quizFormat) {
                        "Đúng / Sai" -> Triple("✔  Bài kiểm tra Đúng / Sai", Color(0xFFE8F5E9), ColorForest)
                        "Điền khuyết" -> Triple("✏  Bài kiểm tra Điền khuyết", ColorAmber.copy(alpha = 0.15f), ColorAmberDark)
                        else -> Triple(uiState.quizFormat, ColorCream, ColorInk)
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(fmtBg)
                            .border(1.dp, fmtFg.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 12.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = fmtLabel,
                            fontFamily = JetBrainsMonoFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 10.sp,
                            color = fmtFg
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(22.dp))

        // Stats Matrix 2x2
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            ResultStatBox(
                value = "$correctCount/$totalQuestions",
                label = "Số câu đúng",
                accentColor = ColorForest,
                modifier = Modifier.weight(1f)
            )
            ResultStatBox(
                value = timeLabel,
                label = "Thời gian làm",
                accentColor = ColorInk,
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(modifier = Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            ResultStatBox(
                value = "${(correctCount.toFloat() / totalQuestions * 100).toInt()}%",
                label = "Độ chính xác",
                accentColor = Color(0xFF9F7AEA),
                modifier = Modifier.weight(1f)
            )
            ResultStatBox(
                value = "+${uiState.xpEarned} XP",
                label = "Thưởng tích lũy",
                accentColor = ColorAmberDark,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(22.dp))

        // Action Buttons Row
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
                    .clickable { viewModel.exitQuiz() }
                    .padding(vertical = 11.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Về Dashboard",
                    fontFamily = DmSansFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.5.sp,
                    color = ColorTextOnLight
                )
            }

            Box(
                modifier = Modifier
                    .weight(1.2f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(ColorAmber)
                    .clickable { viewModel.retryQuiz() }
                    .padding(vertical = 11.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "🔄  Luyện tập lại",
                    fontFamily = DmSansFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.5.sp,
                    color = ColorInk
                )
            }
        }

        Spacer(modifier = Modifier.height(22.dp))

        // Review Questions List
        SectionTitle(title = "Xem lại đáp án chi tiết")
        Spacer(modifier = Modifier.height(10.dp))
        ReviewQuestionsList(
            questions = uiState.questions,
            answersMap = uiState.answersMap
        )

        Spacer(modifier = Modifier.height(24.dp))
    }
}

// ─── Score Canvas Circular Ring Component ──────────────────────────────────────

@Composable
private fun ScoreRingCanvas(correctCount: Int, totalQuestions: Int) {
    val ratio = correctCount.toFloat() / totalQuestions

    Box(
        modifier = Modifier.size(130.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 9.dp.toPx()
            val diameter = size.minDimension - strokeWidth
            val topLeft = Offset(strokeWidth / 2, strokeWidth / 2)
            val arcSize = Size(diameter, diameter)

            // Inner Ring Background outline
            drawArc(
                color = ColorBorder,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            // Circular progress ring drawing
            drawArc(
                brush = Brush.sweepGradient(
                    colors = listOf(ColorForestLight, ColorAmber, ColorForestLight)
                ),
                startAngle = -90f,
                sweepAngle = ratio * 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }

        // Inside text
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "$correctCount/$totalQuestions",
                fontFamily = JetBrainsMonoFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
                color = ColorInk
            )
            Text(
                text = "CÂU ĐÚNG",
                fontFamily = DmSansFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 9.sp,
                color = ColorTextOnLightSecondary,
                letterSpacing = 0.5.sp
            )
        }
    }
}

// ─── Result Stat Box Component ────────────────────────────────────────────────

@Composable
private fun ResultStatBox(
    value: String,
    label: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, ColorBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                fontFamily = JetBrainsMonoFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = accentColor
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                fontFamily = DmSansFamily,
                fontSize = 10.5.sp,
                color = ColorTextOnLightSecondary
            )
        }
    }
}

// ─── Review Questions List Component ──────────────────────────────────────────

@Composable
private fun ReviewQuestionsList(
    questions: List<QuizQuestion>,
    answersMap: Map<Int, String?>
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, ColorBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column {
            questions.forEachIndexed { index, question ->
                val selectedKey = answersMap[index]
                val correctKey = question.correctOption
                val isCorrect = selectedKey == correctKey

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Câu ${index + 1}: ${question.text}",
                        fontFamily = DmSansFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = ColorInk,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Review Answer Pills Column
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        question.options.forEach { option ->
                            val isOptionSelected = selectedKey == option.key
                            val isOptionCorrect = correctKey == option.key

                            val rowBg = when {
                                isOptionCorrect -> ColorForest.copy(alpha = 0.08f)
                                isOptionSelected -> ColorError.copy(alpha = 0.08f)
                                else -> ColorCream.copy(alpha = 0.5f)
                            }
                            val rowBorder = when {
                                isOptionCorrect -> ColorForest
                                isOptionSelected -> ColorError
                                else -> ColorBorder
                            }
                            val rowTextColor = when {
                                isOptionCorrect -> ColorForest
                                isOptionSelected -> ColorError
                                else -> ColorTextOnLight
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(rowBg)
                                    .border(1.dp, rowBorder, RoundedCornerShape(8.dp))
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = option.key,
                                        fontFamily = JetBrainsMonoFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = rowTextColor
                                    )
                                    Text(
                                        text = option.text,
                                        fontFamily = DmSansFamily,
                                        fontSize = 11.5.sp,
                                        color = ColorInk,
                                        lineHeight = 15.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                if (isOptionCorrect) {
                                    Text(
                                        text = "ĐÚNG",
                                        fontFamily = JetBrainsMonoFamily,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 8.sp,
                                        color = ColorForest
                                    )
                                } else if (isOptionSelected) {
                                    Text(
                                        text = "BẠN CHỌN",
                                        fontFamily = JetBrainsMonoFamily,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 8.sp,
                                        color = ColorError
                                    )
                                }
                            }
                        }
                    }
                }

                if (index < questions.size - 1) {
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

// ─── Section Title Helper ─────────────────────────────────────────────────────

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        fontFamily = PlayfairDisplayFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 17.sp,
        color = ColorInk
    )
}
