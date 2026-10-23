package com.example.eduvault.feature.quiz.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.eduvault.core.theme.ColorAmber
import com.example.eduvault.core.theme.DmSansFamily
import com.example.eduvault.feature.tasks.ui.TaskCenterScreen
import com.example.eduvault.feature.home.ui.TrainingUiState
import com.example.eduvault.feature.quiz.ui.components.QuizPlayer
import com.example.eduvault.feature.quiz.ui.components.QuizResults

@Composable
fun QuizContent(
    paddingValues: PaddingValues,
    viewModel: QuizViewModel = hiltViewModel(),
    trainingState: TrainingUiState,
    onSubmitEvidence: () -> Unit,
    onRetryTraining: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    BackHandler(enabled = uiState.screenMode != QuizScreenMode.DASHBOARD) { viewModel.exitQuiz() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
    ) {
        when (uiState.screenMode) {
            QuizScreenMode.DASHBOARD -> {
                TaskCenterScreen(trainingState, uiState.quizSets, paddingValues,
                    onSubmitEvidence = onSubmitEvidence, onStartQuiz = viewModel::startQuiz,
                    onRetry = { onRetryTraining(); viewModel.loadQuizSets() }, quizError = uiState.errorMessage)
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
                    CircularProgressIndicator(
                        color = ColorAmber,
                        modifier = Modifier.size(44.dp)
                    )
                    Text(
                        text = "Đang chuẩn bị bài tự kiểm tra…",
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
