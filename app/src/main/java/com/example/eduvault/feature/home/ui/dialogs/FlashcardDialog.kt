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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.outlined.Cancel
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.eduvault.core.theme.ColorAmber
import com.example.eduvault.core.theme.ColorAmberDark
import com.example.eduvault.core.theme.ColorBorder
import com.example.eduvault.core.theme.ColorCream
import com.example.eduvault.core.theme.ColorForest
import com.example.eduvault.core.theme.ColorInk
import com.example.eduvault.core.theme.ColorTextOnLight
import com.example.eduvault.core.theme.ColorTextOnLightSecondary
import com.example.eduvault.core.theme.DmSansFamily
import com.example.eduvault.core.theme.JetBrainsMonoFamily
import com.example.eduvault.core.theme.PlayfairDisplayFamily
import com.example.eduvault.domain.model.FlashcardSet
import com.example.eduvault.feature.home.ui.FlashcardViewModel

@Composable
fun FlashcardDialog(
    onDismiss: () -> Unit,
    viewModel: FlashcardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var currentIndex by remember { mutableIntStateOf(0) }
    var isFlipped by remember { mutableStateOf(false) }
    var selectedSet by remember { mutableStateOf<FlashcardSet?>(null) }

    LaunchedEffect(Unit) {
        viewModel.load()
    }

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
                    ) { /* Chặn click trượt ra ngoài */ }
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
                            text = if (selectedSet == null) "Bộ thẻ ghi nhớ 🃏" else selectedSet!!.title,
                            fontFamily = PlayfairDisplayFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = ColorInk,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        Icon(
                            imageVector = Icons.Outlined.Cancel,
                            contentDescription = "Đóng",
                            tint = ColorTextOnLightSecondary,
                            modifier = Modifier
                                .size(20.dp)
                                .clickable { onDismiss() }
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    if (selectedSet == null) {
                        Text(
                            text = "Chọn một bộ thẻ để ôn luyện kiến thức",
                            fontFamily = DmSansFamily,
                            fontSize = 11.sp,
                            color = ColorTextOnLightSecondary
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        if (uiState.isLoading) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(color = ColorAmber)
                            }
                        } else if (uiState.sets.isEmpty()) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(ColorAmber.copy(alpha = 0.1f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = "🃏", fontSize = 32.sp)
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "Chưa có bộ thẻ ghi nhớ nào",
                                    fontFamily = DmSansFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = ColorInk
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Bạn chưa có bộ flashcard nào. Hãy chọn tài liệu đào tạo hoặc làm Quiz để hệ thống tạo thẻ ôn tập phù hợp.",
                                    fontFamily = DmSansFamily,
                                    fontSize = 11.sp,
                                    color = ColorTextOnLightSecondary,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(horizontal = 16.dp)
                                )
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 280.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                items(uiState.sets) { set ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(ColorCream)
                                            .border(1.dp, ColorBorder, RoundedCornerShape(12.dp))
                                            .clickable {
                                                selectedSet = set
                                                currentIndex = 0
                                                isFlipped = false
                                            }
                                            .padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(ColorAmber.copy(alpha = 0.15f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(text = "🃏", fontSize = 16.sp)
                                        }

                                        Spacer(modifier = Modifier.width(12.dp))

                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = set.title,
                                                fontFamily = DmSansFamily,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp,
                                                color = ColorInk,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = "${set.cardCount} thẻ ghi nhớ · ID: ${set.flashcardSetId}",
                                                fontFamily = DmSansFamily,
                                                fontSize = 11.sp,
                                                color = ColorTextOnLightSecondary
                                            )
                                        }

                                        Icon(
                                            imageVector = Icons.AutoMirrored.Outlined.ArrowForward,
                                            contentDescription = "Học",
                                            tint = ColorAmberDark,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        val currentSet = selectedSet!!
                        val cards = currentSet.cards

                        if (cards.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "Bộ thẻ này không có thẻ nào!",
                                        fontFamily = DmSansFamily,
                                        fontSize = 13.sp,
                                        color = ColorTextOnLightSecondary
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(ColorInk)
                                            .clickable { selectedSet = null }
                                            .padding(horizontal = 14.dp, vertical = 8.dp)
                                    ) {
                                        Text(
                                            text = "Quay lại",
                                            fontFamily = DmSansFamily,
                                            fontSize = 12.sp,
                                            color = Color.White
                                        )
                                    }
                                }
                            }
                        } else {
                            Text(
                                text = "Thẻ số ${currentIndex + 1}/${cards.size} · Nhấp vào thẻ để lật mặt",
                                fontFamily = DmSansFamily,
                                fontSize = 11.sp,
                                color = ColorTextOnLightSecondary
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(
                                        if (!isFlipped) {
                                            Brush.verticalGradient(listOf(ColorAmber.copy(alpha = 0.12f), ColorAmber.copy(alpha = 0.04f)))
                                        } else {
                                            Brush.verticalGradient(listOf(ColorForest.copy(alpha = 0.12f), ColorForest.copy(alpha = 0.04f)))
                                        }
                                    )
                                    .border(
                                        2.dp,
                                        if (!isFlipped) ColorAmber else ColorForest,
                                        RoundedCornerShape(16.dp)
                                    )
                                    .clickable { isFlipped = !isFlipped }
                                    .padding(20.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = if (!isFlipped) "MẶT TRƯỚC (THUẬT NGỮ)" else "MẶT SAU (ĐỊNH NGHĨA)",
                                        fontFamily = JetBrainsMonoFamily,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (!isFlipped) ColorAmberDark else ColorForest,
                                        letterSpacing = 1.sp
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .verticalScroll(rememberScrollState()),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = if (!isFlipped) cards[currentIndex].term else cards[currentIndex].definition,
                                            fontFamily = PlayfairDisplayFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = if (!isFlipped) 20.sp else 13.sp,
                                            color = ColorInk,
                                            textAlign = TextAlign.Center,
                                            lineHeight = 20.sp
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .border(1.dp, ColorBorder, RoundedCornerShape(8.dp))
                                        .background(ColorCream)
                                        .clickable {
                                            isFlipped = false
                                            currentIndex = (currentIndex - 1 + cards.size) % cards.size
                                        }
                                        .padding(horizontal = 14.dp, vertical = 8.dp)
                                ) {
                                    Text(
                                        text = "← Trước",
                                        fontFamily = DmSansFamily,
                                        fontSize = 12.sp,
                                        color = ColorTextOnLight
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .border(1.dp, ColorBorder, RoundedCornerShape(8.dp))
                                        .background(Color.White)
                                        .clickable {
                                            selectedSet = null
                                        }
                                        .padding(horizontal = 14.dp, vertical = 8.dp)
                                ) {
                                    Text(
                                        text = "Danh sách",
                                        fontFamily = DmSansFamily,
                                        fontSize = 12.sp,
                                        color = ColorTextOnLightSecondary
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(ColorInk)
                                        .clickable {
                                            isFlipped = false
                                            currentIndex = (currentIndex + 1) % cards.size
                                        }
                                        .padding(horizontal = 14.dp, vertical = 8.dp)
                                ) {
                                    Text(
                                        text = "Tiếp theo →",
                                        fontFamily = DmSansFamily,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
