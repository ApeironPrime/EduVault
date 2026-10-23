package com.example.eduvault.feature.home.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Cancel
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.eduvault.core.theme.ColorAmber
import com.example.eduvault.core.theme.ColorAmberDark
import com.example.eduvault.core.theme.ColorBorder
import com.example.eduvault.core.theme.ColorCream
import com.example.eduvault.core.theme.ColorError
import com.example.eduvault.core.theme.ColorForest
import com.example.eduvault.core.theme.ColorInk
import com.example.eduvault.core.theme.ColorTextOnLightSecondary
import com.example.eduvault.core.theme.DmSansFamily
import com.example.eduvault.core.theme.JetBrainsMonoFamily
import com.example.eduvault.core.theme.PlayfairDisplayFamily
import com.example.eduvault.feature.quiz.ui.Difficulty
import com.example.eduvault.feature.quiz.ui.QuizViewModel

@Composable
fun CreateQuizSetupDialog(
    quizViewModel: QuizViewModel,
    presetTitle: String,
    onDismiss: () -> Unit,
    onConfirm: (docTitle: String, questionCount: Int, difficulty: Difficulty, format: String) -> Unit
) {
    val availableDocs by quizViewModel.availableDocs.collectAsStateWithLifecycle()

    val docTitles = remember(availableDocs) {
        availableDocs.map { it.title }
    }

    // Pre-select document based on preset title or default to first
    var selectedDoc by remember(docTitles, presetTitle) {
        mutableStateOf(
            docTitles.find { it.contains(presetTitle, ignoreCase = true) || presetTitle.contains(it, ignoreCase = true) }
                ?: docTitles.firstOrNull() ?: ""
        )
    }
    var selectedCount by remember { mutableIntStateOf(5) }
    var selectedDifficulty by remember { mutableStateOf(Difficulty.EASY) }
    val formatOptions = listOf("Trắc nghiệm", "Đúng / Sai", "Điền khuyết")
    var selectedFormat by remember { mutableStateOf("Trắc nghiệm") }

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
                    .fillMaxWidth(0.92f)
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
                    modifier = Modifier
                        .padding(20.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    // Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "✦", fontSize = 20.sp, color = ColorAmber)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Tùy chỉnh bộ đề Quiz",
                                fontFamily = PlayfairDisplayFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = ColorInk
                            )
                        }
                        Icon(
                            imageVector = Icons.Outlined.Cancel,
                            contentDescription = "Đóng",
                            tint = ColorTextOnLightSecondary,
                            modifier = Modifier
                                .size(20.dp)
                                .clickable { onDismiss() }
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // 1. Select Document
                    Text(
                        text = "1. CHỌN TÀI LIỆU ÔN TẬP",
                        fontFamily = JetBrainsMonoFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 9.sp,
                        color = ColorAmberDark,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        docTitles.forEach { doc ->
                            val isSelected = doc == selectedDoc
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isSelected) ColorAmber.copy(alpha = 0.08f) else ColorCream)
                                    .border(
                                        width = if (isSelected) 1.5.dp else 1.dp,
                                        color = if (isSelected) ColorAmber else ColorBorder,
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                    .clickable { selectedDoc = doc }
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(16.dp)
                                        .clip(CircleShape)
                                        .background(if (isSelected) ColorAmber else Color.White)
                                        .border(1.dp, if (isSelected) ColorAmber else ColorBorder, CircleShape)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = doc,
                                    fontFamily = DmSansFamily,
                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                                    fontSize = 11.5.sp,
                                    color = ColorInk,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // 2. Select Question Count
                    Text(
                        text = "2. SỐ LƯỢNG CÂU HỎI",
                        fontFamily = JetBrainsMonoFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 9.sp,
                        color = ColorAmberDark,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val countOptions = listOf(5, 10, 15, 20)
                        countOptions.forEach { count ->
                            val isSelected = count == selectedCount
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) ColorInk else ColorCream)
                                    .border(1.dp, if (isSelected) ColorInk else ColorBorder, RoundedCornerShape(8.dp))
                                    .clickable { selectedCount = count }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "$count câu",
                                    fontFamily = DmSansFamily,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) ColorAmber else ColorTextOnLightSecondary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // 3. Select Difficulty
                    Text(
                        text = "3. ĐỘ KHÓ BỘ ĐỀ",
                        fontFamily = JetBrainsMonoFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 9.sp,
                        color = ColorAmberDark,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val difficulties = listOf(Difficulty.EASY, Difficulty.MEDIUM, Difficulty.HARD)
                        difficulties.forEach { diff ->
                            val isSelected = diff == selectedDifficulty
                            val activeBg = when (diff) {
                                Difficulty.EASY -> ColorForest
                                Difficulty.MEDIUM -> ColorAmber
                                Difficulty.HARD -> ColorError
                            }
                            val activeText = if (diff == Difficulty.MEDIUM) ColorInk else Color.White

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) activeBg else ColorCream)
                                    .border(1.dp, if (isSelected) activeBg else ColorBorder, RoundedCornerShape(8.dp))
                                    .clickable { selectedDifficulty = diff }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = diff.label,
                                    fontFamily = DmSansFamily,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) activeText else ColorTextOnLightSecondary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // 4. Select Format
                    Text(
                        text = "4. HÌNH THỨC CÂU HỎI",
                        fontFamily = JetBrainsMonoFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 9.sp,
                        color = ColorAmberDark,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        formatOptions.forEach { format ->
                            val isSelected = format == selectedFormat
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) ColorInk else ColorCream)
                                    .border(1.dp, if (isSelected) ColorInk else ColorBorder, RoundedCornerShape(8.dp))
                                    .clickable { selectedFormat = format }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = format,
                                    fontFamily = DmSansFamily,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) ColorAmber else ColorTextOnLightSecondary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Action Buttons
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
                                .clickable { onDismiss() }
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Hủy bỏ",
                                fontFamily = DmSansFamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp,
                                color = ColorTextOnLightSecondary
                            )
                        }

                        Box(
                            modifier = Modifier
                                .weight(1.3f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(ColorInk)
                                .clickable { onConfirm(selectedDoc, selectedCount, selectedDifficulty, selectedFormat) }
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Bắt đầu tạo bộ đề ✦",
                                fontFamily = DmSansFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = ColorAmber
                            )
                        }
                    }
                }
            }
        }
    }
}
