package com.example.eduvault.feature.home.ui.preview

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import com.example.eduvault.core.theme.EduVaultTheme
import com.example.eduvault.feature.home.ui.DocType
import com.example.eduvault.feature.home.ui.HomeContent
import com.example.eduvault.feature.home.ui.HomeUiState
import com.example.eduvault.feature.home.ui.RecentDoc
import com.example.eduvault.feature.home.ui.TrainingUiState

@Preview(name = "Roadmap • Phone", widthDp = 390, heightDp = 844)
@Preview(name = "Roadmap • Compact", widthDp = 320, heightDp = 740)
@Preview(name = "Roadmap • Large text", widthDp = 390, heightDp = 844, fontScale = 1.3f)
@Composable
fun RoadmapPreview() {
    EduVaultTheme(darkTheme = false) {
        Surface(color = Color(0xFFF8FAFC)) {
            HomeContent(
                uiState = HomeUiState(),
                trainingUiState = TrainingUiState(dashboard = PreviewFresherDashboard),
                recentDocs = listOf(
                    RecentDoc(
                        id = "preview_conventions", title = "Quy chuẩn mã nguồn & Git Flow",
                        course = "GIT-02", type = DocType.NOTE, emoji = "📋",
                        views = 0, fileName = "conventions.pdf",
                        quantityLabel = "12 trang",
                    ),
                    RecentDoc(
                        id = "preview_quiz", title = "Đánh giá quy chuẩn & bảo mật",
                        course = "SEC-01", type = DocType.QUIZ, emoji = "🧩",
                        views = 0, quantityLabel = "10 câu",
                    ),
                ),
                activityItems = emptyList(),
                paddingValues = PaddingValues(),
                onStartQuizSetup = {}, onTabSelected = {}, onUploadClick = {},
                onOpenDoc = {}, onOpenFlashcards = {}, onOpenStudyAi = {},
                onOpenNotes = {}, onNotificationClick = {},
                onRetryTraining = {}, onSubmitEvidenceClick = {},
            )
        }
    }
}
