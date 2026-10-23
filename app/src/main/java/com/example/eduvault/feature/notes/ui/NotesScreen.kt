package com.example.eduvault.feature.notes.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Cancel
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.Save
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
import androidx.compose.runtime.remember
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
import com.example.eduvault.core.theme.ColorAmber
import com.example.eduvault.core.theme.ColorAmberDark
import com.example.eduvault.core.theme.ColorBorder
import com.example.eduvault.core.theme.ColorCream
import com.example.eduvault.core.theme.ColorError
import com.example.eduvault.core.theme.ColorForest
import com.example.eduvault.core.theme.ColorInk
import com.example.eduvault.core.theme.ColorPaper
import com.example.eduvault.core.theme.ColorTextOnLight
import com.example.eduvault.core.theme.ColorTextOnLightSecondary
import com.example.eduvault.core.theme.DmSansFamily
import com.example.eduvault.core.theme.PlayfairDisplayFamily
import com.example.eduvault.domain.model.UserNote
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Dialog hiển thị toàn bộ danh sách ghi chú của người dùng.
 */
@Composable
fun NotesDialog(
    viewModel: NotesViewModel,
    onDismiss: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    // Load lại ghi chú khi dialog mở ra
    LaunchedEffect(Unit) {
        viewModel.loadAllNotes()
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
                    .fillMaxWidth(0.95f)
                    .fillMaxHeight(0.85f)
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) { /* chặn click trượt */ },
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = ColorPaper),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp)
                ) {
                    // Header của Dialog
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "📝", fontSize = 22.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Ghi chú của tôi",
                                fontFamily = PlayfairDisplayFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = ColorInk
                            )
                        }
                        Icon(
                            imageVector = Icons.Outlined.Cancel,
                            contentDescription = "Đóng",
                            tint = ColorTextOnLightSecondary,
                            modifier = Modifier
                                .size(24.dp)
                                .clickable { onDismiss() }
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Content Area
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        when {
                            uiState.isLoading -> {
                                CircularProgressIndicator(
                                    color = ColorAmber,
                                    modifier = Modifier.size(36.dp)
                                )
                            }
                            uiState.error != null -> {
                                Text(
                                    text = uiState.error ?: "Đã xảy ra lỗi khi tải ghi chú.",
                                    fontFamily = DmSansFamily,
                                    fontSize = 14.sp,
                                    color = ColorError,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(16.dp)
                                )
                            }
                            uiState.notes.isEmpty() -> {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center,
                                    modifier = Modifier.padding(24.dp)
                                ) {
                                    Text(
                                        text = "📓",
                                        fontSize = 64.sp
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = "Chưa có ghi chú nào",
                                        fontFamily = PlayfairDisplayFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        color = ColorInk,
                                        textAlign = TextAlign.Center
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "Mở một tài liệu đào tạo trong thư viện và bấm \"Ghi chú\" để lưu lại kiến thức quan trọng.",
                                        fontFamily = DmSansFamily,
                                        fontSize = 12.sp,
                                        color = ColorTextOnLightSecondary,
                                        textAlign = TextAlign.Center,
                                        lineHeight = 18.sp
                                    )
                                }
                            }
                            else -> {
                                LazyColumn(
                                    modifier = Modifier.fillMaxSize(),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    items(uiState.notes) { note ->
                                        NoteCard(
                                            note = note,
                                            onClick = {
                                                viewModel.openNoteEditor(
                                                    docId = note.docId,
                                                    docTitle = note.docTitle,
                                                    docCourseCode = note.docCourseCode
                                                )
                                            }
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

    // Hiển thị Note Editor Dialog khi có ghi chú đang chỉnh sửa
    if (uiState.editingNote != null) {
        NoteEditorDialog(
            viewModel = viewModel,
            onDismiss = { viewModel.closeNoteEditor() }
        )
    }
}

/**
 * Thẻ hiển thị một ghi chú trong danh sách.
 */
@Composable
fun NoteCard(
    note: UserNote,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .border(width = 1.dp, color = ColorBorder, shape = RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = ColorCream)
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            // Row Tiêu đề + Course code
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = note.docTitle,
                    fontFamily = DmSansFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = ColorInk,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                if (note.docCourseCode.isNotBlank()) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(ColorForest.copy(alpha = 0.1f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = note.docCourseCode,
                            fontFamily = DmSansFamily,
                            fontWeight = FontWeight.Medium,
                            fontSize = 10.sp,
                            color = ColorForest
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Nội dung preview ghi chú
            Text(
                text = note.noteContent,
                fontFamily = DmSansFamily,
                fontSize = 13.sp,
                color = ColorTextOnLightSecondary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Thời gian cập nhật gần nhất
            val dateStr = remember(note.updatedAt) {
                if (note.updatedAt > 0) {
                    val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                    sdf.format(Date(note.updatedAt))
                } else ""
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (dateStr.isNotEmpty()) "Cập nhật lúc: $dateStr" else "",
                    fontFamily = DmSansFamily,
                    fontSize = 11.sp,
                    color = ColorTextOnLightSecondary.copy(alpha = 0.8f)
                )
                Icon(
                    imageVector = Icons.Outlined.EditNote,
                    contentDescription = "Chỉnh sửa",
                    tint = ColorAmber,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

/**
 * Dialog chỉnh sửa/viết ghi chú.
 */
@Composable
fun NoteEditorDialog(
    viewModel: NotesViewModel,
    onDismiss: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val note = uiState.editingNote ?: return

    // Kiểm tra xem đây là ghi chú mới tinh hay đã tồn tại
    val isExistingNote = remember(uiState.notes, note.docId) {
        uiState.notes.any { it.docId == note.docId }
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
                    .fillMaxWidth(0.92f)
                    .fillMaxHeight(0.7f)
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) { /* chặn click trượt */ },
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = ColorPaper),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp)
                ) {
                    // Header của Editor
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isExistingNote) "Chỉnh sửa ghi chú ✏️" else "Thêm ghi chú mới ✏️",
                                fontFamily = PlayfairDisplayFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = ColorInk
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = note.docTitle,
                                fontFamily = DmSansFamily,
                                fontSize = 12.sp,
                                color = ColorTextOnLightSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Icon(
                            imageVector = Icons.Outlined.Cancel,
                            contentDescription = "Hủy",
                            tint = ColorTextOnLightSecondary,
                            modifier = Modifier
                                .size(24.dp)
                                .clickable { onDismiss() }
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // TextField cho nội dung
                    OutlinedTextField(
                        value = uiState.editorContent,
                        onValueChange = { viewModel.onEditorContentChanged(it) },
                        placeholder = {
                            Text(
                                text = "Nhập nội dung ghi chú của bạn tại đây...",
                                fontFamily = DmSansFamily,
                                fontSize = 13.sp,
                                color = ColorTextOnLightSecondary
                            )
                        },
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = ColorCream,
                            unfocusedContainerColor = ColorCream,
                            focusedBorderColor = ColorAmber,
                            unfocusedBorderColor = ColorBorder,
                            cursorColor = ColorAmber,
                            focusedTextColor = ColorInk,
                            unfocusedTextColor = ColorInk
                        ),
                        textStyle = androidx.compose.ui.text.TextStyle(
                            fontFamily = DmSansFamily,
                            fontSize = 13.sp,
                            lineHeight = 18.sp
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Actions Bar (Lưu / Xóa)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Nếu là ghi chú đã tồn tại → cho phép Xóa
                        if (isExistingNote) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .border(1.dp, ColorError.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                                    .background(ColorError.copy(alpha = 0.05f))
                                    .clickable(enabled = !uiState.isSaving) {
                                        viewModel.deleteCurrentNote()
                                    }
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.DeleteOutline,
                                        contentDescription = "Xóa",
                                        tint = ColorError,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "Xóa",
                                        fontFamily = DmSansFamily,
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 13.sp,
                                        color = ColorError
                                    )
                                }
                            }
                        }

                        // Spacer đẩy nút Lưu về bên phải
                        Spacer(modifier = Modifier.weight(1f))

                        // Nút Hủy
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .border(1.dp, ColorBorder, RoundedCornerShape(10.dp))
                                .clickable(enabled = !uiState.isSaving) {
                                    onDismiss()
                                }
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Hủy",
                                fontFamily = DmSansFamily,
                                fontWeight = FontWeight.Medium,
                                fontSize = 13.sp,
                                color = ColorTextOnLightSecondary
                            )
                        }

                        // Nút Lưu
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    Brush.horizontalGradient(listOf(ColorAmber, ColorAmberDark))
                                )
                                .clickable(enabled = !uiState.isSaving) {
                                    viewModel.saveCurrentNote()
                                }
                                .padding(horizontal = 20.dp, vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            if (uiState.isSaving) {
                                CircularProgressIndicator(
                                    color = ColorInk,
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Save,
                                        contentDescription = "Lưu",
                                        tint = ColorInk,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "Lưu ghi chú",
                                        fontFamily = DmSansFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = ColorInk
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
