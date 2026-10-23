package com.example.eduvault.feature.quiz.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.HourglassEmpty
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.eduvault.core.theme.ColorAmber
import com.example.eduvault.core.theme.ColorAmberDark
import com.example.eduvault.core.theme.ColorBorder
import com.example.eduvault.core.theme.ColorCream
import com.example.eduvault.core.theme.ColorError
import com.example.eduvault.core.theme.ColorForest
import com.example.eduvault.core.theme.ColorInk
import com.example.eduvault.core.theme.ColorPaper
import com.example.eduvault.core.theme.ColorTextOnDark
import com.example.eduvault.core.theme.ColorTextOnDarkSecondary
import com.example.eduvault.core.theme.ColorTextOnLight
import com.example.eduvault.core.theme.ColorTextOnLightSecondary
import com.example.eduvault.core.theme.DmSansFamily
import com.example.eduvault.core.theme.JetBrainsMonoFamily
import com.example.eduvault.feature.quiz.ui.QuizAnswer
import com.example.eduvault.feature.quiz.ui.QuizQuestion
import com.example.eduvault.feature.quiz.ui.QuizSet
import com.example.eduvault.feature.quiz.ui.QuizUiState
import com.example.eduvault.feature.quiz.ui.QuizViewModel

@Composable
fun QuizPlayer(
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
                border = BorderStroke(1.5.dp, ColorBorder),
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

@Composable
fun QuizPlayerHeader(
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

@Composable
fun AnswerButton(
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
        isLocked && isCorrect -> BorderStroke(2.dp, ColorForest)
        isLocked && isSelected && !isCorrect -> BorderStroke(2.dp, ColorError)
        isLocked && !isSelected && isCorrect -> BorderStroke(1.5.dp, ColorForest)
        isSelected -> BorderStroke(2.dp, ColorAmber)
        else -> BorderStroke(1.5.dp, ColorBorder)
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

@Composable
fun ExplanationBox(explanation: String) {
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

@Composable
fun QuizPlayerActionBar(
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
                        selectedOption == "" -> ColorError.copy(alpha = 0.5f)
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
            // Skip / Back button
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
