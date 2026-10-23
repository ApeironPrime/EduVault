package com.example.eduvault.feature.quiz.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import com.example.eduvault.feature.home.ui.components.SectionHeader
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.eduvault.core.theme.ColorAmber
import com.example.eduvault.core.theme.ColorAmberDark
import com.example.eduvault.core.theme.ColorBorder
import com.example.eduvault.core.theme.ColorCream
import com.example.eduvault.core.theme.ColorError
import com.example.eduvault.core.theme.ColorForest
import com.example.eduvault.core.theme.ColorForestLight
import com.example.eduvault.core.theme.ColorInk
import com.example.eduvault.core.theme.ColorPaper
import com.example.eduvault.core.theme.ColorTextOnLight
import com.example.eduvault.core.theme.ColorTextOnLightSecondary
import com.example.eduvault.core.theme.DmSansFamily
import com.example.eduvault.core.theme.JetBrainsMonoFamily
import com.example.eduvault.core.theme.PlayfairDisplayFamily
import com.example.eduvault.feature.quiz.ui.QuizQuestion
import com.example.eduvault.feature.quiz.ui.QuizUiState
import com.example.eduvault.feature.quiz.ui.QuizViewModel
import java.util.Locale

@Composable
fun QuizResults(
    uiState: QuizUiState,
    viewModel: QuizViewModel
) {
    val totalQuestions = uiState.questions.size
    val correctCount = uiState.correctAnswersCount
    val score = (correctCount.toFloat() / totalQuestions) * 10f
    
    val minutes = uiState.timeSpentSeconds / 60
    val seconds = uiState.timeSpentSeconds % 60
    val timeLabel = String.format(Locale.US, "%02d:%02d", minutes, seconds)

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ColorPaper)
            .statusBarsPadding()
            .navigationBarsPadding()
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
                    text = "Kết quả tự kiểm tra không thay thế nghiệm thu của mentor.",
                    fontFamily = DmSansFamily,
                    fontSize = 12.sp,
                    color = ColorTextOnLightSecondary,
                    textAlign = TextAlign.Center
                )

                uiState.attemptSaveMessage?.let { message ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = message,
                        fontFamily = DmSansFamily,
                        fontSize = 12.sp,
                        color = ColorTextOnLightSecondary,
                        textAlign = TextAlign.Center
                    )
                }

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
                value = "Mentor",
                label = "Nghiệm thu chặng",
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
                    text = "Về Nhiệm vụ",
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
        SectionHeader(title = "Xem lại đáp án chi tiết")
        Spacer(modifier = Modifier.height(10.dp))
        ReviewQuestionsList(
            questions = uiState.questions,
            answersMap = uiState.answersMap
        )

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun ScoreRingCanvas(correctCount: Int, totalQuestions: Int) {
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

            drawArc(
                color = ColorBorder,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

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

@Composable
fun ResultStatBox(
    value: String,
    label: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.5.dp, ColorBorder),
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

@Composable
fun ReviewQuestionsList(
    questions: List<QuizQuestion>,
    answersMap: Map<Int, String?>
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.5.dp, ColorBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column {
            questions.forEachIndexed { index, question ->
                val selectedKey = answersMap[index]
                val correctKey = question.correctOption

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
