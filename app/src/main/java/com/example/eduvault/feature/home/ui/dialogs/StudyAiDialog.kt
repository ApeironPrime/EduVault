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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.eduvault.core.theme.ColorAmber
import com.example.eduvault.core.theme.ColorAmberDark
import com.example.eduvault.core.theme.ColorBorder
import com.example.eduvault.core.theme.ColorCream
import com.example.eduvault.core.theme.ColorForest
import com.example.eduvault.core.theme.ColorInk
import com.example.eduvault.core.theme.ColorTextOnLightSecondary
import com.example.eduvault.core.theme.DmSansFamily
import com.example.eduvault.core.theme.PlayfairDisplayFamily
import com.example.eduvault.core.ui.parseMarkdownToAnnotatedString
import com.example.eduvault.feature.home.ui.HomeViewModel

@Composable
fun StudyAiDialog(
    onDismiss: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    var messageQuery by remember { mutableStateOf("") }
    val chatMessages by viewModel.chatMessages.collectAsState()
    val isAiTyping by viewModel.isAiTyping.collectAsState()

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
                    ) { /* chặn click trượt ra ngoài */ },
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "🤖", fontSize = 22.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text(
                                    text = "StudyAI Companion",
                                    fontFamily = PlayfairDisplayFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = ColorInk
                                )
                                Text(
                                    text = if (isAiTyping) "StudyAI đang soạn câu trả lời..." else "Trợ lý AI đang hoạt động",
                                    fontFamily = DmSansFamily,
                                    fontSize = 10.sp,
                                    color = if (isAiTyping) ColorAmberDark else ColorForest
                                )
                            }
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

                    Spacer(modifier = Modifier.height(12.dp))

                    // Chat messages list
                    val scrollState = rememberScrollState()
                    LaunchedEffect(chatMessages.size, isAiTyping) {
                        scrollState.animateScrollTo(scrollState.maxValue)
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(280.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(ColorCream)
                            .border(1.dp, ColorBorder, RoundedCornerShape(12.dp))
                            .padding(12.dp)
                            .verticalScroll(scrollState),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        chatMessages.forEach { msg ->
                            val isUser = msg.isUser
                            val text = msg.text
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(0.82f)
                                        .clip(
                                            RoundedCornerShape(
                                                topStart = 12.dp,
                                                topEnd = 12.dp,
                                                bottomStart = if (isUser) 12.dp else 0.dp,
                                                bottomEnd = if (isUser) 0.dp else 12.dp
                                            )
                                        )
                                        .background(if (isUser) ColorAmber else ColorInk)
                                        .padding(10.dp)
                                ) {
                                    if (isUser) {
                                        Text(
                                            text = text,
                                            fontFamily = DmSansFamily,
                                            fontSize = 12.sp,
                                            color = ColorInk,
                                            lineHeight = 16.sp
                                        )
                                    } else {
                                        Text(
                                            text = parseMarkdownToAnnotatedString(text),
                                            fontFamily = DmSansFamily,
                                            fontSize = 12.sp,
                                            color = Color.White,
                                            lineHeight = 16.sp
                                        )
                                    }
                                }
                            }
                        }

                        // Typing indicator
                        if (isAiTyping) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Start
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(0.82f)
                                        .clip(
                                            RoundedCornerShape(
                                                topStart = 12.dp,
                                                topEnd = 12.dp,
                                                bottomStart = 0.dp,
                                                bottomEnd = 12.dp
                                            )
                                        )
                                        .background(ColorInk)
                                        .padding(10.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(12.dp),
                                            color = ColorAmber,
                                            strokeWidth = 1.5.dp
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "StudyAI đang suy nghĩ...",
                                            fontFamily = DmSansFamily,
                                            fontSize = 11.sp,
                                            color = Color.White.copy(alpha = 0.8f),
                                            fontStyle = FontStyle.Italic
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Suggestions
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val suggestions = listOf("Tóm tắt tài liệu", "Giải thích Git Flow")
                        suggestions.forEach { suggestion ->
                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(ColorCream)
                                    .border(1.dp, ColorBorder, CircleShape)
                                    .clickable(enabled = !isAiTyping) {
                                        viewModel.sendChatMessage(suggestion)
                                    }
                                    .padding(horizontal = 10.dp, vertical = 5.dp)
                            ) {
                                Text(
                                    text = suggestion,
                                    fontFamily = DmSansFamily,
                                    fontSize = 10.5.sp,
                                    color = if (isAiTyping) ColorTextOnLightSecondary.copy(alpha = 0.5f) else ColorTextOnLightSecondary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Input Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = messageQuery,
                            onValueChange = { messageQuery = it },
                            placeholder = { Text("Hỏi AI bất kỳ điều gì...", fontSize = 12.sp) },
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = ColorAmber,
                                unfocusedBorderColor = ColorBorder
                            ),
                            enabled = !isAiTyping,
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isAiTyping || messageQuery.trim().isEmpty()) ColorAmber.copy(alpha = 0.5f) else ColorAmber)
                            .clickable(enabled = !isAiTyping && messageQuery.trim().isNotEmpty()) {
                                viewModel.sendChatMessage(messageQuery)
                                messageQuery = ""
                            },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "✈️", fontSize = 16.sp)
                        }
                    }
                }
            }
        }
    }
}
