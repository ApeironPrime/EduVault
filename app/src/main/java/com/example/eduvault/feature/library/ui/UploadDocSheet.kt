package com.example.eduvault.feature.library.ui

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Cancel
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.CloudUpload
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.automirrored.outlined.InsertDriveFile
import androidx.compose.material.icons.outlined.School
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.eduvault.core.theme.ColorAmber
import com.example.eduvault.core.theme.ColorAmberDark
import com.example.eduvault.core.theme.ColorBorder
import com.example.eduvault.core.theme.ColorCream
import com.example.eduvault.core.theme.ColorError
import com.example.eduvault.core.theme.ColorForest
import com.example.eduvault.core.theme.ColorInk
import com.example.eduvault.core.theme.ColorTextOnLight
import com.example.eduvault.core.theme.ColorTextOnLightSecondary
import com.example.eduvault.core.theme.DmSansFamily
import com.example.eduvault.core.theme.JetBrainsMonoFamily
import com.example.eduvault.core.theme.PlayfairDisplayFamily
import com.example.eduvault.domain.repository.AuthRepository
import com.example.eduvault.domain.repository.DocumentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

// ═══════════════════════════════════════════════════════════════════════════════
// VIEWMODEL & UISTATE DEFINITIONS
// ═══════════════════════════════════════════════════════════════════════════════

@HiltViewModel
class UploadDocViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val documentRepository: DocumentRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(UploadUiState())
    val uiState: StateFlow<UploadUiState> = _uiState.asStateFlow()

    init {
        loadUserUniversity()
    }

    private fun loadUserUniversity() {
        viewModelScope.launch {
            authRepository.getCurrentUser().onSuccess { user ->
                _uiState.update { it.copy(userUniversity = user?.university ?: "Tất cả các trường") }
            }
        }
    }

    fun onTitleChange(title: String) {
        _uiState.update { it.copy(title = title) }
    }

    fun onSubjectCodeChange(code: String) {
        _uiState.update { it.copy(subjectCode = code) }
    }

    fun onDocTypeChange(type: String) {
        _uiState.update { it.copy(docType = type) }
    }

    /**
     * Xử lý lấy file PDF/Word cục bộ từ điện thoại của người dùng
     */
    fun handleFilePicked(context: Context, uri: Uri) {
        viewModelScope.launch {
            try {
                val fileName = getFileName(context, uri) ?: "tailieu_${System.currentTimeMillis()}.pdf"
                
                val folder = context.getExternalFilesDir("uploaded_docs") ?: File(context.filesDir, "uploaded_docs")
                if (!folder.exists()) folder.mkdirs()
                
                val file = File(folder, fileName)
                val inputStream = context.contentResolver.openInputStream(uri)
                val outputStream = FileOutputStream(file)
                
                inputStream?.use { input ->
                    outputStream.use { output ->
                        input.copyTo(output)
                    }
                }
                
                _uiState.update { 
                    it.copy(
                        selectedFileUri = file.absolutePath,
                        selectedFileName = fileName,
                        selectedFileSize = "${String.format(java.util.Locale.US, "%.2f", file.length() / (1024.0 * 1024.0))} MB",
                        errorMessage = null
                    ) 
                }
            } catch (e: java.lang.Exception) {
                _uiState.update { it.copy(errorMessage = "Không thể đọc tệp tài liệu: ${e.localizedMessage}") }
            }
        }
    }

    /**
     * Lưu thông tin đăng tải và tích điểm credit
     */
    fun uploadDocument(onSuccess: () -> Unit) {
        val state = _uiState.value
        if (state.title.trim().isEmpty()) {
            _uiState.update { it.copy(errorMessage = "Vui lòng nhập tiêu đề tài liệu.") }
            return
        }
        if (state.selectedFileUri.isEmpty()) {
            _uiState.update { it.copy(errorMessage = "Vui lòng chọn file tài liệu từ thiết bị của bạn.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isUploading = true, errorMessage = null) }
            documentRepository.uploadUserDocument(
                title = state.title.trim(),
                subjectCode = state.subjectCode.trim().ifBlank { "TÀI LIỆU" },
                docType = state.docType,
                fileUri = state.selectedFileUri
            ).onSuccess {
                _uiState.update { it.copy(isUploading = false, isSuccess = true) }
                // Delay to let users enjoy the success credit animation
                delay(1200)
                onSuccess()
            }.onFailure { error ->
                _uiState.update { it.copy(isUploading = false, errorMessage = error.message) }
            }
        }
    }

    private fun getFileName(context: Context, uri: Uri): String? {
        var name: String? = null
        if (uri.scheme == "content") {
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    val index = it.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                    if (index != -1) name = it.getString(index)
                }
            }
        }
        if (name == null) {
            val path = uri.path
            val cut = path?.lastIndexOf('/') ?: -1
            if (cut != -1) name = path?.substring(cut + 1)
        }
        return name
    }
}

data class UploadUiState(
    val title: String = "",
    val subjectCode: String = "",
    val docType: String = "Ghi chú",
    val userUniversity: String = "",
    val selectedFileUri: String = "",
    val selectedFileName: String = "",
    val selectedFileSize: String = "",
    val isUploading: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null
)

// ═══════════════════════════════════════════════════════════════════════════════
// COMPOSABLE COMPONENT
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
fun UploadDocSheet(
    onDismiss: () -> Unit,
    viewModel: UploadDocViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    // Launcher for Word/PDF document files
    val fileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.handleFilePicked(context, uri)
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        // Dark blurred backdrop overlay
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
                if (uiState.isSuccess) {
                    // Success full-width confetti layout
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .background(ColorForest.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "🎉", fontSize = 38.sp)
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        Text(
                            text = "ĐĂNG TẢI THÀNH CÔNG!",
                            fontFamily = PlayfairDisplayFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = ColorForest,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Cảm ơn đóng góp của bạn! Hệ thống đã tự động cộng +1 lượt xem tài liệu (Credits) vào ví tài khoản của bạn.",
                            fontFamily = DmSansFamily,
                            fontSize = 13.sp,
                            color = ColorTextOnLightSecondary,
                            textAlign = TextAlign.Center,
                            lineHeight = 18.sp
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(ColorAmber.copy(alpha = 0.15f))
                                .padding(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "+1 Lượt sử dụng tài liệu 🧩",
                                fontFamily = JetBrainsMonoFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = ColorAmberDark
                            )
                        }
                    }
                } else {
                    // Regular Input form
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Đăng tài liệu mới",
                                fontFamily = PlayfairDisplayFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = ColorInk
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

                        Spacer(modifier = Modifier.height(14.dp))

                        // Success/Error Alert messages
                        AnimatedVisibility(visible = uiState.errorMessage != null) {
                            Box(modifier = Modifier.padding(bottom = 12.dp)) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(Color(0xFFFFF0F0))
                                        .border(1.dp, Color(0xFFFFDDDD), RoundedCornerShape(10.dp))
                                        .padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Info,
                                        contentDescription = null,
                                        tint = ColorError,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        text = uiState.errorMessage ?: "",
                                        fontFamily = DmSansFamily,
                                        fontSize = 12.sp,
                                        color = ColorError
                                    )
                                }
                            }
                        }

                        // Auto-school assignment badge (Read Only)
                        if (uiState.userUniversity.isNotEmpty()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(ColorCream)
                                    .border(1.5.dp, ColorBorder, RoundedCornerShape(10.dp))
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.School,
                                    contentDescription = null,
                                    tint = ColorAmberDark,
                                    modifier = Modifier.size(15.dp)
                                )
                                Text(
                                    text = "Trường tự đăng: ${uiState.userUniversity}",
                                    fontFamily = DmSansFamily,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 11.5.sp,
                                    color = ColorInk,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                        }

                        // Title
                        OutlinedTextField(
                            value = uiState.title,
                            onValueChange = viewModel::onTitleChange,
                            label = { Text("Tiêu đề tài liệu (ví dụ: Tóm tắt Kinh tế vi mô Ch.4)", fontFamily = DmSansFamily) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp),
                            shape = RoundedCornerShape(11.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = ColorAmber,
                                unfocusedBorderColor = ColorBorder,
                                focusedLabelColor = ColorAmberDark,
                                unfocusedLabelColor = ColorTextOnLightSecondary
                            )
                        )

                        // Document Type Selection Chips
                        Text(
                            text = "Chọn phân loại tài liệu:",
                            fontFamily = DmSansFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = ColorInk,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        val docTypes = listOf("Ghi chú", "Slide", "Tóm tắt", "Quiz")
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            docTypes.forEach { type ->
                                val isSelected = uiState.docType == type
                                Box(
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .background(if (isSelected) ColorAmber else ColorCream)
                                        .border(
                                            1.5.dp,
                                            if (isSelected) ColorAmber else ColorBorder,
                                            CircleShape
                                        )
                                        .clickable { viewModel.onDocTypeChange(type) }
                                        .padding(horizontal = 14.dp, vertical = 7.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = type,
                                        fontFamily = DmSansFamily,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        fontSize = 11.5.sp,
                                        color = if (isSelected) ColorInk else ColorTextOnLightSecondary
                                    )
                                }
                            }
                        }

                        // File picker selector area
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = ColorCream.copy(alpha = 0.5f)),
                            border = androidx.compose.foundation.BorderStroke(1.5.dp, ColorBorder),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    // Accept Word and PDF mime types
                                    fileLauncher.launch("*/*")
                                }
                                .padding(bottom = 18.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 16.dp, horizontal = 12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                if (uiState.selectedFileName.isNotEmpty()) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Outlined.InsertDriveFile,
                                        contentDescription = null,
                                        tint = ColorForest,
                                        modifier = Modifier.size(32.dp)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = uiState.selectedFileName,
                                        fontFamily = DmSansFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = ColorInk,
                                        textAlign = TextAlign.Center,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = uiState.selectedFileSize,
                                        fontFamily = JetBrainsMonoFamily,
                                        fontSize = 10.sp,
                                        color = ColorTextOnLightSecondary
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Outlined.CloudUpload,
                                        contentDescription = null,
                                        tint = ColorAmberDark,
                                        modifier = Modifier.size(32.dp)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Chọn file tài liệu của bạn",
                                        fontFamily = DmSansFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = ColorInk
                                    )
                                    Text(
                                        text = "Chấp nhận định dạng tệp PDF, Word (.docx, .doc)",
                                        fontFamily = DmSansFamily,
                                        fontSize = 10.5.sp,
                                        color = ColorTextOnLightSecondary
                                    )
                                }
                            }
                        }

                        // Submit action button
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(11.dp))
                                .background(if (uiState.isUploading) ColorAmber.copy(alpha = 0.5f) else ColorAmber)
                                .clickable(enabled = !uiState.isUploading) {
                                    viewModel.uploadDocument(onDismiss)
                                }
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            if (uiState.isUploading) {
                                CircularProgressIndicator(color = ColorInk, modifier = Modifier.size(16.dp))
                            } else {
                                Text(
                                    text = "Đăng tải tài liệu học",
                                    fontFamily = DmSansFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
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
