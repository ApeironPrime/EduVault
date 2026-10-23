package com.example.eduvault.feature.library.ui.viewer

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.eduvault.core.theme.ColorAmber
import com.example.eduvault.core.theme.ColorAmberDark
import com.example.eduvault.core.theme.ColorBorder
import com.example.eduvault.core.theme.ColorCream
import com.example.eduvault.core.theme.ColorForest
import com.example.eduvault.core.theme.ColorInk
import com.example.eduvault.core.theme.ColorTextOnLightSecondary
import com.example.eduvault.core.theme.DmSansFamily
import com.example.eduvault.core.theme.PlayfairDisplayFamily
import com.example.eduvault.domain.model.AiChatMessage

@Composable
fun AIChatContent(
    docTitle: String,
    docType: String,
    docContent: String,
    onAskAi: (String, (Result<String>) -> Unit) -> Unit
) {
    var messageQuery by remember { mutableStateOf("") }
    var chatMessages by remember {
        mutableStateOf(
            listOf(
                AiChatMessage(
                    isUser = false,
                    text = "Chào bạn! Mình là AI Tutor của tài liệu '$docTitle'. Bạn muốn làm rõ khái niệm, phân tích tình huống hay tạo câu hỏi luyện tập nào? 🧠💬"
                )
            )
        )
    }
    var isAiTyping by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(ColorCream)
            .border(1.dp, ColorBorder, RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("💬", fontSize = 16.sp)
                Text(
                    text = "AI Tutor — Hỏi đáp về tài liệu",
                    fontFamily = PlayfairDisplayFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = ColorAmberDark
                )
            }
            Spacer(modifier = Modifier.height(10.dp))

            // Chat messages area
            val chatScrollState = rememberScrollState()
            LaunchedEffect(chatMessages.size, isAiTyping) {
                chatScrollState.animateScrollTo(chatScrollState.maxValue)
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color.White)
                    .border(1.dp, ColorBorder.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                    .verticalScroll(chatScrollState)
                    .padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                chatMessages.forEach { msg ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = if (msg.isUser) Arrangement.End else Arrangement.Start
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.85f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (msg.isUser) ColorForest.copy(alpha = 0.1f) else ColorAmber.copy(alpha = 0.1f)
                                )
                                .padding(10.dp)
                        ) {
                            Text(
                                text = parseMarkdownToAnnotatedString(msg.text, 12.5f),
                                fontFamily = DmSansFamily,
                                fontSize = 12.5.sp,
                                color = ColorInk,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
                if (isAiTyping) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(ColorAmber.copy(alpha = 0.1f))
                                .padding(10.dp)
                        ) {
                            Text(
                                text = "AI đang suy nghĩ...",
                                fontFamily = DmSansFamily,
                                fontSize = 12.sp,
                                color = ColorTextOnLightSecondary,
                                fontStyle = FontStyle.Italic
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Input row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = messageQuery,
                    onValueChange = { messageQuery = it },
                    placeholder = {
                        Text(
                            "Hỏi AI về tài liệu...",
                            fontFamily = DmSansFamily,
                            fontSize = 12.sp,
                            color = ColorTextOnLightSecondary
                        )
                    },
                    modifier = Modifier.weight(1f),
                    textStyle = TextStyle(
                        fontFamily = DmSansFamily,
                        fontSize = 13.sp,
                        color = ColorInk
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ColorAmber,
                        unfocusedBorderColor = ColorBorder,
                        cursorColor = ColorAmber
                    ),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(
                        onSend = {
                            if (messageQuery.isNotBlank() && !isAiTyping) {
                                val query = messageQuery.trim()
                                messageQuery = ""
                                chatMessages = chatMessages + AiChatMessage(
                                    isUser = true, text = query
                                )
                                isAiTyping = true
                                onAskAi(query) { result ->
                                    result.onSuccess { answer ->
                                        chatMessages = chatMessages + AiChatMessage(
                                            isUser = false, text = answer
                                        )
                                    }.onFailure { err ->
                                        chatMessages = chatMessages + AiChatMessage(
                                            isUser = false, text = "Lỗi: ${err.localizedMessage ?: "Không thể kết nối AI"}"
                                        )
                                    }
                                    isAiTyping = false
                                }
                            }
                        }
                    )
                )
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(if (messageQuery.isNotBlank() && !isAiTyping) ColorAmber else ColorBorder)
                        .clickable(enabled = messageQuery.isNotBlank() && !isAiTyping) {
                            val query = messageQuery.trim()
                            messageQuery = ""
                            chatMessages = chatMessages + AiChatMessage(
                                isUser = true, text = query
                            )
                            isAiTyping = true
                            onAskAi(query) { result ->
                                result.onSuccess { answer ->
                                    chatMessages = chatMessages + AiChatMessage(
                                        isUser = false, text = answer
                                    )
                                }.onFailure { err ->
                                    chatMessages = chatMessages + AiChatMessage(
                                        isUser = false, text = "Lỗi: ${err.localizedMessage ?: "Không thể kết nối AI"}"
                                    )
                                }
                                isAiTyping = false
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text("➤", fontSize = 16.sp, color = Color.White)
                }
            }
        }
    }
}
