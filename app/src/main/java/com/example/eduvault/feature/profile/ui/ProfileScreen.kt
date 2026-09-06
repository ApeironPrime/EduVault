package com.example.eduvault.feature.profile.ui

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.automirrored.outlined.ExitToApp
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.automirrored.outlined.Login
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.PersonAdd
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material3.IconButton
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.eduvault.domain.model.AuthState
import com.example.eduvault.core.theme.ColorAmber
import com.example.eduvault.core.theme.ColorAmberDark
import com.example.eduvault.core.theme.ColorAmberLight
import com.example.eduvault.core.theme.ColorBorder
import com.example.eduvault.core.theme.ColorCream
import com.example.eduvault.core.theme.ColorError
import com.example.eduvault.core.theme.ColorForest
import com.example.eduvault.core.theme.ColorForestLight
import com.example.eduvault.core.theme.ColorInk
import com.example.eduvault.core.theme.ColorPaper
import com.example.eduvault.core.theme.ColorTextOnDark
import com.example.eduvault.core.theme.ColorTextOnDarkSecondary
import com.example.eduvault.core.theme.ColorTextOnLight
import com.example.eduvault.core.theme.ColorTextOnLightSecondary
import com.example.eduvault.core.theme.DmSansFamily
import com.example.eduvault.core.theme.JetBrainsMonoFamily
import com.example.eduvault.core.theme.PlayfairDisplayFamily
import java.io.File

@Composable
fun ProfileContent(
    paddingValues: PaddingValues,
    onLogout: () -> Unit,
    onNavigateToAdmin: () -> Unit = {},
    onNavigateToLogin: () -> Unit = {},
    onNavigateToRegister: () -> Unit = {},
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // Nếu là Guest — hiển thị màn hình chưa đăng nhập
    if (uiState.authState is AuthState.Guest) {
        GuestProfileScreen(
            paddingValues = paddingValues,
            onNavigateToLogin = onNavigateToLogin,
            onNavigateToRegister = onNavigateToRegister
        )
        return
    }

    // Đã đăng nhập — hiển thị profile bình thường
    val scrollState = rememberScrollState()
    var showLogoutDialog by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ColorPaper)
    ) {
        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = ColorAmber)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = paddingValues.calculateBottomPadding())
                    .verticalScroll(scrollState)
            ) {
                // Header Profile Info Banner
                if (!uiState.isEditing) {
                    ProfileHeaderSection(
                        uiState = uiState,
                        paddingValues = paddingValues,
                        onEditClick = viewModel::onStartEditing
                    )
                }

                // Message Alerts (Success/Error)
                AnimatedVisibility(visible = uiState.successMessage != null || uiState.errorMessage != null) {
                    Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                        if (uiState.successMessage != null) {
                            AlertBox(
                                message = uiState.successMessage ?: "",
                                isSuccess = true,
                                icon = Icons.Outlined.CheckCircle
                            )
                        } else if (uiState.errorMessage != null) {
                            AlertBox(
                                message = uiState.errorMessage ?: "",
                                isSuccess = false,
                                icon = Icons.Outlined.Info
                            )
                        }
                    }
                }

                if (uiState.isEditing) {
                    // ─── EDIT MODE ───
                    ProfileEditForm(
                        uiState = uiState,
                        viewModel = viewModel,
                        onSaveClick = viewModel::saveProfile,
                        onCancelClick = viewModel::onCancelEditing
                    )
                } else {
                    // ─── VIEW MODE ───
                    ProfileViewDetails(
                        uiState = uiState,
                        onLogoutClick = { showLogoutDialog = true },
                        onNavigateToAdmin = onNavigateToAdmin,
                        onEditClick = viewModel::onStartEditing
                    )
                }
            }
        }
    }

    // Confirmation Logout dialog
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLogoutDialog = false
                        viewModel.logout(onLogout)
                    }
                ) {
                    Text(
                        text = "Đăng xuất",
                        fontFamily = DmSansFamily,
                        fontWeight = FontWeight.Bold,
                        color = ColorError
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text(
                        text = "Hủy bỏ",
                        fontFamily = DmSansFamily,
                        color = ColorTextOnLightSecondary
                    )
                }
            },
            title = {
                Text(
                    text = "Xác nhận đăng xuất?",
                    fontFamily = PlayfairDisplayFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = ColorInk
                )
            },
            text = {
                Text(
                    text = "Bạn có chắc chắn muốn đăng xuất khỏi ứng dụng tài liệu học tập EduVault?",
                    fontFamily = DmSansFamily,
                    fontSize = 14.sp,
                    color = ColorTextOnLightSecondary,
                    lineHeight = 18.sp
                )
            },
            shape = RoundedCornerShape(18.dp),
            containerColor = Color.White
        )
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// UI COMPONENTS
// ═══════════════════════════════════════════════════════════════════════════════

// ─── Profile Header Section ───

@Composable
private fun ProfileHeaderSection(
    uiState: ProfileUiState,
    paddingValues: PaddingValues,
    onEditClick: () -> Unit
) {
    val user = uiState.user ?: return

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(ColorInk)
            .drawBehind {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(ColorAmber.copy(alpha = 0.15f), Color.Transparent),
                        center = Offset(size.width * 0.85f, size.height * 0.4f),
                        radius = size.width * 0.55f
                    ),
                    radius = size.width * 0.55f,
                    center = Offset(size.width * 0.85f, size.height * 0.4f)
                )
            }
            .padding(top = paddingValues.calculateTopPadding() + 16.dp, bottom = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Profile Avatar Display
            ProfileAvatar(
                avatarUrl = user.avatarUrl,
                fullName = user.fullName,
                modifier = Modifier.size(100.dp),
                borderWidth = 3.5.dp,
                emojiSize = 48.sp,
                initialSize = 44.sp
            )

            Spacer(modifier = Modifier.height(14.dp))

            // User Name
            Text(
                text = user.fullName.ifEmpty { "Gamer" },
                fontFamily = PlayfairDisplayFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
                color = ColorTextOnDark,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Email text
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Email,
                    contentDescription = null,
                    tint = ColorTextOnDarkSecondary,
                    modifier = Modifier.size(12.dp)
                )
                Text(
                    text = user.email,
                    fontFamily = DmSansFamily,
                    fontSize = 12.5.sp,
                    color = ColorTextOnDarkSecondary
                )
            }

            Spacer(modifier = Modifier.height(10.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // University Badge (if present)
                if (user.university.isNotEmpty()) {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(ColorTextOnDark.copy(alpha = 0.08f))
                            .border(1.dp, ColorTextOnDark.copy(alpha = 0.12f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.School,
                            contentDescription = null,
                            tint = ColorAmber,
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = user.university,
                            fontFamily = JetBrainsMonoFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = ColorAmber
                        )
                    }
                }

                // XP Badge (màu hổ phách rực rỡ)
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(ColorTextOnDark.copy(alpha = 0.08f))
                        .border(1.dp, ColorTextOnDark.copy(alpha = 0.12f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "🔥 ${user.totalXp} XP",
                        fontFamily = JetBrainsMonoFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = ColorAmber
                    )
                }
            }

            // Edit button when NOT in edit mode
            if (!uiState.isEditing) {
                Spacer(modifier = Modifier.height(16.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(ColorAmber)
                        .clickable { onEditClick() }
                        .padding(horizontal = 18.dp, vertical = 9.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Edit,
                            contentDescription = null,
                            tint = ColorInk,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "Chỉnh sửa hồ sơ",
                            fontFamily = DmSansFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.5.sp,
                            color = ColorInk
                        )
                    }
                }
            }
        }
    }
}

// ─── Custom Avatar Loader Component ───

@Composable
fun ProfileAvatar(
    avatarUrl: String,
    fullName: String,
    modifier: Modifier = Modifier,
    borderWidth: Dp = 3.dp,
    emojiSize: TextUnit = 32.sp,
    initialSize: TextUnit = 32.sp
) {
    val bitmap = remember(avatarUrl) {
        if (avatarUrl.isNotEmpty() && !avatarUrl.startsWith("emoji:")) {
            val file = File(avatarUrl)
            if (file.exists()) {
                try {
                    BitmapFactory.decodeFile(file.absolutePath)?.asImageBitmap()
                } catch (e: Exception) {
                    null
                }
            } else null
        } else null
    }

    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(
                Brush.linearGradient(
                    colors = listOf(ColorForestLight, ColorAmber)
                )
            )
            .border(borderWidth, Color.White, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        if (bitmap != null) {
            Image(
                bitmap = bitmap,
                contentDescription = "Ảnh đại diện thiết bị",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            val emoji = if (avatarUrl.startsWith("emoji:")) {
                avatarUrl.removePrefix("emoji:")
            } else {
                ""
            }

            if (emoji.isNotEmpty()) {
                Text(
                    text = emoji,
                    fontSize = emojiSize
                )
            } else {
                val initial = if (fullName.isNotEmpty()) fullName.first().uppercase() else "E"
                Text(
                    text = initial,
                    fontFamily = DmSansFamily,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = initialSize,
                    color = Color.White
                )
            }
        }
    }
}

// ─── View Profile Details Screen ───

@Composable
private fun ProfileViewDetails(
    uiState: ProfileUiState,
    onLogoutClick: () -> Unit,
    onNavigateToAdmin: () -> Unit = {},
    onEditClick: () -> Unit = {}
) {
    Column(modifier = Modifier.padding(16.dp)) {
        // Study Stats
        Text(
            text = "Thành tích tích lũy",
            fontFamily = PlayfairDisplayFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = ColorInk,
            modifier = Modifier.padding(bottom = 10.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            StatCard(
                emoji = "🧩", 
                value = "${uiState.user?.quizCount ?: 0}", 
                label = "Quiz đã luyện", 
                modifier = Modifier.weight(1f)
            )
            StatCard(
                emoji = "📝", 
                value = "${uiState.user?.uploadCount ?: 0} tài liệu", 
                label = "Tài liệu đã tải lên", 
                modifier = Modifier.weight(1f)
            )
            val avgScore = uiState.user?.quizAverageScore ?: 0.0f
            val scoreText = if (uiState.user != null && uiState.user.quizCount > 0) {
                String.format(java.util.Locale.US, "%.1f", avgScore)
            } else {
                "0.0"
            }
            StatCard(
                emoji = "⭐", 
                value = scoreText, 
                label = "Điểm thi đua", 
                modifier = Modifier.weight(1f)
            )
        }

        // Settings list
        Text(
            text = "Cài đặt & Tiện ích",
            fontFamily = PlayfairDisplayFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = ColorInk,
            modifier = Modifier.padding(bottom = 10.dp)
        )

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = androidx.compose.foundation.BorderStroke(1.5.dp, ColorBorder),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column {
                if (uiState.user?.role == "admin") {
                    MenuSettingsItem(
                        icon = Icons.Outlined.Settings,
                        label = "Bảng quản trị Admin 🛠️",
                        onClick = onNavigateToAdmin
                    )
                }

                // Mục 1: Cấu hình tài khoản học tập
                MenuSettingsItem(
                    icon = Icons.Outlined.Settings,
                    label = "Cấu hình tài khoản học tập",
                    onClick = onEditClick
                )



                // Mục 3: Trợ giúp & Đóng góp phản hồi
                var showHelpDialog by remember { mutableStateOf(false) }
                MenuSettingsItem(
                    icon = Icons.AutoMirrored.Outlined.HelpOutline,
                    label = "Trợ giúp & Đóng góp phản hồi",
                    onClick = { showHelpDialog = true }
                )
                if (showHelpDialog) {
                    AlertDialog(
                        onDismissRequest = { showHelpDialog = false },
                        icon = { Text(text = "💬", fontSize = 28.sp) },
                        title = {
                            Text(
                                text = "Trợ giúp & Phản hồi",
                                fontFamily = PlayfairDisplayFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 17.sp
                            )
                        },
                        text = {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Text(
                                    text = "EduVault rất trân trọng mọi ý kiến đóng góp và báo lỗi từ cộng đồng sinh viên!",
                                    fontFamily = DmSansFamily,
                                    fontSize = 13.sp,
                                    color = ColorTextOnLightSecondary,
                                    lineHeight = 20.sp
                                )
                                Row(verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Icon(
                                        imageVector = Icons.Outlined.Email,
                                        contentDescription = null,
                                        tint = ColorAmberDark,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "hanryit6012@gmail.com",
                                        fontFamily = DmSansFamily,
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 12.sp,
                                        color = ColorAmberDark
                                    )
                                }
                                Row(verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text(text = "💬", fontSize = 14.sp)
                                    Text(
                                        text = "Phản hồi trực tiếp qua tính năng báo cáo trên từng tài liệu",
                                        fontFamily = DmSansFamily,
                                        fontSize = 12.sp,
                                        color = ColorTextOnLightSecondary
                                    )
                                }
                            }
                        },
                        confirmButton = {
                            TextButton(onClick = { showHelpDialog = false }) {
                                Text(
                                    text = "Đã hiểu",
                                    fontFamily = DmSansFamily,
                                    fontWeight = FontWeight.Bold,
                                    color = ColorAmberDark
                                )
                            }
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Red Logout Button
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .border(1.5.dp, ColorError, RoundedCornerShape(12.dp))
                .clickable { onLogoutClick() }
                .padding(vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.ExitToApp,
                    contentDescription = null,
                    tint = ColorError,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = "Đăng xuất tài khoản",
                    fontFamily = DmSansFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.5.sp,
                    color = ColorError
                )
            }
        }
    }
}

@Composable
private fun StatCard(
    emoji: String,
    value: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, ColorBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = emoji, fontSize = 20.sp)
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = value,
                fontFamily = JetBrainsMonoFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = ColorInk
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                fontFamily = DmSansFamily,
                fontSize = 10.sp,
                color = ColorTextOnLightSecondary,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun MenuSettingsItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = ColorInk,
            modifier = Modifier.size(18.dp)
        )
        Text(
            text = label,
            fontFamily = DmSansFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 13.sp,
            color = ColorTextOnLight
        )
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(ColorBorder)
    )
}

// ─── Edit Profile Form Screen ───

@Composable
private fun ProfileEditForm(
    uiState: ProfileUiState,
    viewModel: ProfileViewModel,
    onSaveClick: () -> Unit,
    onCancelClick: () -> Unit
) {
    val context = LocalContext.current

    var showChangePasswordDialog by remember { mutableStateOf(false) }

    if (showChangePasswordDialog) {
        ChangePasswordDialog(
            uiState = uiState,
            viewModel = viewModel,
            onDismiss = { showChangePasswordDialog = false }
        )
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) viewModel.handleCustomAvatarPicked(context, uri)
    }

    val quickAvatars = listOf(
        "emoji:👨‍🎓", "emoji:👩‍🎓", "emoji:🧑‍💻", "emoji:👩‍💻",
        "emoji:🦊", "emoji:🦁", "emoji:🦄", "emoji:🐼",
        "emoji:🐯", "emoji:🦋", "emoji:🐉", "emoji:🌟"
    )

    Column {
        // ── Dark hero section with avatar preview ──────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            ColorInk,
                            Color(0xFF1A1A2E)
                        )
                    )
                )
                .padding(vertical = 28.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                // Live avatar preview (shows currently selected)
                Box(contentAlignment = Alignment.BottomEnd) {
                    ProfileAvatar(
                        avatarUrl = uiState.editAvatarUrl.ifBlank { uiState.user?.avatarUrl ?: "" },
                        fullName = uiState.editFullName.ifBlank { uiState.user?.fullName ?: "" },
                        modifier = Modifier.size(96.dp),
                        borderWidth = 3.dp,
                        emojiSize = 46.sp,
                        initialSize = 40.sp
                    )
                    // Camera badge
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(ColorAmber)
                            .border(2.dp, ColorInk, CircleShape)
                            .clickable { launcher.launch("image/*") },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.PhotoCamera,
                            contentDescription = "Chọn ảnh",
                            tint = ColorInk,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = uiState.editFullName.ifBlank { uiState.user?.fullName ?: "Người dùng" },
                    fontFamily = PlayfairDisplayFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = ColorTextOnDark
                )
                Text(
                    text = uiState.user?.email ?: "",
                    fontFamily = DmSansFamily,
                    fontSize = 12.sp,
                    color = ColorTextOnDarkSecondary
                )
            }
        }

        // ── Body ───────────────────────────────────────────────────────────────
        Column(modifier = Modifier.padding(horizontal = 18.dp)) {

            Spacer(modifier = Modifier.height(22.dp))

            // ── Avatar section label ─────────────────────────────────────────
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 14.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(ColorAmber.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) { Text("🖼️", fontSize = 15.sp) }
                Column {
                    Text(
                        text = "Ảnh đại diện",
                        fontFamily = DmSansFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = ColorInk
                    )
                    Text(
                        text = "Chọn emoji hoặc ảnh từ thiết bị",
                        fontFamily = DmSansFamily,
                        fontSize = 11.sp,
                        color = ColorTextOnLightSecondary
                    )
                }
            }

            // ── Photo from device button (refined) ───────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFFFFAF0))
                    .border(1.5.dp, ColorAmber.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                    .clickable { launcher.launch("image/*") }
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(ColorAmber.copy(alpha = 0.18f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.PhotoCamera,
                        contentDescription = null,
                        tint = ColorAmberDark,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Tải ảnh từ thiết bị",
                        fontFamily = DmSansFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                        color = ColorAmberDark
                    )
                    Text(
                        text = "JPG, PNG — hiển thị tức thì",
                        fontFamily = DmSansFamily,
                        fontSize = 10.5.sp,
                        color = ColorTextOnLightSecondary
                    )
                }
                Text("→", fontSize = 16.sp, color = ColorAmberDark)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ── Emoji grid ───────────────────────────────────────────────────
            Text(
                text = "Hoặc chọn nhanh:",
                fontFamily = DmSansFamily,
                fontSize = 11.5.sp,
                color = ColorTextOnLightSecondary,
                modifier = Modifier.padding(bottom = 10.dp)
            )

            val cols = 6
            quickAvatars.chunked(cols).forEach { row ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    row.forEach { avatar ->
                        val isSelected = (uiState.editAvatarUrl == avatar) ||
                            (uiState.editAvatarUrl.isBlank() && uiState.user?.avatarUrl == avatar)
                        val emoji = avatar.removePrefix("emoji:")
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (isSelected) ColorAmber.copy(alpha = 0.2f)
                                    else Color(0xFFF7F4EE)
                                )
                                .border(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) ColorAmber else ColorBorder,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clickable(
                                    indication = null,
                                    interactionSource = remember { MutableInteractionSource() }
                                ) { viewModel.onAvatarSelected(avatar) },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = emoji, fontSize = 22.sp)
                        }
                    }
                    // Fill remaining cells in last row
                    repeat(cols - row.size) {
                        Box(modifier = Modifier.weight(1f))
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ── Personal info section label ───────────────────────────────────
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 14.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(ColorForest.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) { Text("👤", fontSize = 15.sp) }
                Column {
                    Text(
                        text = "Thông tin cá nhân",
                        fontFamily = DmSansFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = ColorInk
                    )
                    Text(
                        text = "Hiển thị trên hồ sơ công khai",
                        fontFamily = DmSansFamily,
                        fontSize = 11.sp,
                        color = ColorTextOnLightSecondary
                    )
                }
            }

            // Email (read-only) — styled as info row, not input field
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(ColorCream)
                    .border(1.dp, ColorBorder, RoundedCornerShape(12.dp))
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Email,
                    contentDescription = null,
                    tint = ColorTextOnLightSecondary,
                    modifier = Modifier.size(17.dp)
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Email đăng nhập",
                        fontFamily = DmSansFamily,
                        fontSize = 10.sp,
                        color = ColorTextOnLightSecondary
                    )
                    Text(
                        text = uiState.user?.email ?: "",
                        fontFamily = DmSansFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 13.sp,
                        color = ColorInk
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Full name input
            OutlinedTextField(
                value = uiState.editFullName,
                onValueChange = viewModel::onFullNameChange,
                label = {
                    Text(
                        "Họ và tên",
                        fontFamily = DmSansFamily,
                        fontSize = 12.sp
                    )
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ColorAmber,
                    unfocusedBorderColor = ColorBorder,
                    focusedLabelColor = ColorAmberDark,
                    unfocusedLabelColor = ColorTextOnLightSecondary,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                )
            )

            Spacer(modifier = Modifier.height(10.dp))

            // University input
            OutlinedTextField(
                value = uiState.editUniversity,
                onValueChange = viewModel::onUniversityChange,
                label = {
                    Text(
                        "Trường đại học",
                        fontFamily = DmSansFamily,
                        fontSize = 12.sp
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.School,
                        contentDescription = null,
                        tint = ColorTextOnLightSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ColorAmber,
                    unfocusedBorderColor = ColorBorder,
                    focusedLabelColor = ColorAmberDark,
                    unfocusedLabelColor = ColorTextOnLightSecondary,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // ── Change Password Card ─────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White)
                    .border(1.dp, ColorBorder, RoundedCornerShape(12.dp))
                    .clickable { showChangePasswordDialog = true }
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(ColorAmber.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) { Text("🔑", fontSize = 15.sp) }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Đổi mật khẩu tài khoản",
                        fontFamily = DmSansFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                        color = ColorInk
                    )
                    Text(
                        text = "Cập nhật mật khẩu bảo mật mới",
                        fontFamily = DmSansFamily,
                        fontSize = 10.5.sp,
                        color = ColorTextOnLightSecondary
                    )
                }
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
                    contentDescription = null,
                    tint = ColorTextOnLightSecondary,
                    modifier = Modifier.size(16.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ── Action buttons ────────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Cancel
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.5.dp, ColorBorder, RoundedCornerShape(12.dp))
                        .background(Color.White)
                        .clickable(enabled = !uiState.isSaving) { onCancelClick() }
                        .padding(vertical = 13.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Cancel,
                            contentDescription = null,
                            tint = ColorTextOnLightSecondary,
                            modifier = Modifier.size(15.dp)
                        )
                        Text(
                            text = "Hủy",
                            fontFamily = DmSansFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp,
                            color = ColorTextOnLightSecondary
                        )
                    }
                }

                // Save
                Box(
                    modifier = Modifier
                        .weight(1.6f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            brush = if (uiState.isSaving)
                                Brush.horizontalGradient(listOf(ColorAmber.copy(alpha = 0.55f), ColorAmber.copy(alpha = 0.55f)))
                            else
                                Brush.horizontalGradient(listOf(ColorAmber, Color(0xFFE8A020)))
                        )
                        .clickable(enabled = !uiState.isSaving) { onSaveClick() }
                        .padding(vertical = 13.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (uiState.isSaving) {
                        CircularProgressIndicator(
                            color = ColorInk,
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Save,
                                contentDescription = null,
                                tint = ColorInk,
                                modifier = Modifier.size(15.dp)
                            )
                            Text(
                                text = "Lưu thay đổi",
                                fontFamily = DmSansFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.5.sp,
                                color = ColorInk
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}


// ─── Styled Alert Box Composable ───

@Composable
private fun AlertBox(
    message: String,
    isSuccess: Boolean,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    val bg = if (isSuccess) Color(0xFFF0FAF5) else Color(0xFFFFF0F0)
    val border = if (isSuccess) Color(0xFFD4EDDA) else Color(0xFFFFDDDD)
    val color = if (isSuccess) ColorForest else ColorError

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(bg)
            .border(1.5.dp, border, RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = message,
                fontFamily = DmSansFamily,
                fontSize = 12.5.sp,
                color = color,
                lineHeight = 16.sp
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// GUEST PROFILE SCREEN — Hiển thị khi người dùng chưa đăng nhập
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
fun GuestProfileScreen(
    paddingValues: PaddingValues,
    onNavigateToLogin: () -> Unit,
    onNavigateToRegister: () -> Unit
) {
    val features = listOf(
        "📚" to "Lưu tiến trình học tập",
        "🪙" to "Nhận Credit khi chia sẻ tài liệu",
        "🤖" to "Làm Quiz AI & ôn tập Flashcard",
        "🗂️" to "Quản lý tài liệu cá nhân",
        "🔖" to "Đánh dấu & lưu tài liệu yêu thích",
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ColorPaper)
            .padding(bottom = paddingValues.calculateBottomPadding())
    ) {
        // Gradient background accent ở trên
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp)
                .background(
                    Brush.verticalGradient(
                        listOf(ColorInk, Color(0xFF252545), ColorPaper)
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(64.dp))

            // Avatar placeholder
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.verticalGradient(listOf(ColorAmber, ColorAmberDark))
                    )
                    .border(3.dp, ColorPaper, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "👤", fontSize = 38.sp)
            }

            Spacer(Modifier.height(20.dp))

            Text(
                text = "Chào mừng đến EduVault",
                fontFamily = PlayfairDisplayFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
                color = ColorTextOnDark,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = "Đăng nhập để trải nghiệm đầy đủ\ntính năng học tập thông minh",
                fontFamily = DmSansFamily,
                fontSize = 14.sp,
                color = ColorTextOnDarkSecondary,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )

            Spacer(Modifier.height(32.dp))

            // Feature highlights card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(ColorCream)
                    .border(1.dp, ColorBorder, RoundedCornerShape(16.dp))
                    .padding(20.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text(
                        text = "Dành cho thành viên EduVault",
                        fontFamily = PlayfairDisplayFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = ColorAmberDark
                    )
                    features.forEach { (emoji, text) ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(ColorAmber.copy(alpha = 0.12f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = emoji, fontSize = 16.sp)
                            }
                            Text(
                                text = text,
                                fontFamily = DmSansFamily,
                                fontWeight = FontWeight.Medium,
                                fontSize = 14.sp,
                                color = ColorTextOnLight
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(28.dp))

            // Login button (primary)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        Brush.horizontalGradient(listOf(ColorAmber, ColorAmberDark))
                    )
                    .clickable { onNavigateToLogin() }
                    .padding(vertical = 15.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.Login,
                        contentDescription = null,
                        tint = ColorInk,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "Đăng nhập",
                        fontFamily = DmSansFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = ColorInk
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            // Register button (outlined)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.5.dp, ColorInk, RoundedCornerShape(12.dp))
                    .background(Color.Transparent)
                    .clickable { onNavigateToRegister() }
                    .padding(vertical = 14.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.PersonAdd,
                        contentDescription = null,
                        tint = ColorInk,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "Tạo tài khoản miễn phí",
                        fontFamily = DmSansFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp,
                        color = ColorInk
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            Text(
                text = "🪙 Nhận ngay 5 Credit khi đăng ký lần đầu",
                fontFamily = DmSansFamily,
                fontSize = 12.sp,
                color = ColorAmberDark,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Medium
            )

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun ChangePasswordDialog(
    uiState: ProfileUiState,
    viewModel: ProfileViewModel,
    onDismiss: () -> Unit
) {
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmNewPassword by remember { mutableStateOf("") }

    var currentPasswordVisible by remember { mutableStateOf(false) }
    var newPasswordVisible by remember { mutableStateOf(false) }
    var confirmNewPasswordVisible by remember { mutableStateOf(false) }

    var localError by remember { mutableStateOf<String?>(null) }

    // Clear viewModel messages when dialog is opened/dismissed
    androidx.compose.runtime.DisposableEffect(Unit) {
        viewModel.clearChangePasswordMessages()
        onDispose {
            viewModel.clearChangePasswordMessages()
        }
    }

    AlertDialog(
        onDismissRequest = { if (!uiState.isChangingPassword) onDismiss() },
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(ColorAmber.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🔑", fontSize = 16.sp)
                }
                Text(
                    text = "Đổi mật khẩu",
                    fontFamily = DmSansFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = ColorInk
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Nhập mật khẩu hiện tại và mật khẩu mới để thay đổi. Mật khẩu mới cần ít nhất 6 ký tự.",
                    fontFamily = DmSansFamily,
                    fontSize = 11.sp,
                    color = ColorTextOnLightSecondary,
                    lineHeight = 16.sp
                )

                // Current Password
                OutlinedTextField(
                    value = currentPassword,
                    onValueChange = { currentPassword = it; localError = null },
                    label = { Text("Mật khẩu hiện tại", fontFamily = DmSansFamily, fontSize = 11.sp) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Outlined.Lock,
                            contentDescription = null,
                            tint = ColorTextOnLightSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    trailingIcon = {
                        IconButton(onClick = { currentPasswordVisible = !currentPasswordVisible }) {
                            Icon(
                                imageVector = if (currentPasswordVisible) Icons.Outlined.Visibility else Icons.Outlined.VisibilityOff,
                                contentDescription = if (currentPasswordVisible) "Ẩn mật khẩu" else "Hiện mật khẩu",
                                tint = ColorTextOnLightSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    },
                    visualTransformation = if (currentPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ColorAmber,
                        unfocusedBorderColor = ColorBorder,
                        focusedLabelColor = ColorAmberDark,
                        unfocusedLabelColor = ColorTextOnLightSecondary,
                        focusedContainerColor = ColorCream,
                        unfocusedContainerColor = ColorCream
                    )
                )

                // New Password
                OutlinedTextField(
                    value = newPassword,
                    onValueChange = { newPassword = it; localError = null },
                    label = { Text("Mật khẩu mới", fontFamily = DmSansFamily, fontSize = 11.sp) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Outlined.Lock,
                            contentDescription = null,
                            tint = ColorTextOnLightSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    trailingIcon = {
                        IconButton(onClick = { newPasswordVisible = !newPasswordVisible }) {
                            Icon(
                                imageVector = if (newPasswordVisible) Icons.Outlined.Visibility else Icons.Outlined.VisibilityOff,
                                contentDescription = if (newPasswordVisible) "Ẩn mật khẩu" else "Hiện mật khẩu",
                                tint = ColorTextOnLightSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    },
                    visualTransformation = if (newPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ColorAmber,
                        unfocusedBorderColor = ColorBorder,
                        focusedLabelColor = ColorAmberDark,
                        unfocusedLabelColor = ColorTextOnLightSecondary,
                        focusedContainerColor = ColorCream,
                        unfocusedContainerColor = ColorCream
                    )
                )

                // Confirm New Password
                OutlinedTextField(
                    value = confirmNewPassword,
                    onValueChange = { confirmNewPassword = it; localError = null },
                    label = { Text("Nhập lại mật khẩu mới", fontFamily = DmSansFamily, fontSize = 11.sp) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Outlined.Lock,
                            contentDescription = null,
                            tint = ColorTextOnLightSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    trailingIcon = {
                        IconButton(onClick = { confirmNewPasswordVisible = !confirmNewPasswordVisible }) {
                            Icon(
                                imageVector = if (confirmNewPasswordVisible) Icons.Outlined.Visibility else Icons.Outlined.VisibilityOff,
                                contentDescription = if (confirmNewPasswordVisible) "Ẩn mật khẩu" else "Hiện mật khẩu",
                                tint = ColorTextOnLightSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    },
                    visualTransformation = if (confirmNewPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ColorAmber,
                        unfocusedBorderColor = ColorBorder,
                        focusedLabelColor = ColorAmberDark,
                        unfocusedLabelColor = ColorTextOnLightSecondary,
                        focusedContainerColor = ColorCream,
                        unfocusedContainerColor = ColorCream
                    )
                )

                // Display error message from local or viewModel
                val displayError = localError ?: uiState.changePasswordError
                if (displayError != null) {
                    Text(
                        text = displayError,
                        fontFamily = DmSansFamily,
                        fontSize = 11.sp,
                        color = ColorError,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Display success message
                if (uiState.changePasswordSuccess != null) {
                    Text(
                        text = uiState.changePasswordSuccess,
                        fontFamily = DmSansFamily,
                        fontSize = 11.sp,
                        color = ColorForest,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        },
        confirmButton = {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        brush = if (uiState.isChangingPassword)
                            Brush.horizontalGradient(listOf(ColorAmber.copy(alpha = 0.55f), ColorAmber.copy(alpha = 0.55f)))
                        else
                            Brush.horizontalGradient(listOf(ColorAmber, Color(0xFFE8A020)))
                    )
                    .clickable(enabled = !uiState.isChangingPassword) {
                        if (currentPassword.isBlank() || newPassword.isBlank() || confirmNewPassword.isBlank()) {
                            localError = "Vui lòng điền đầy đủ các trường."
                            return@clickable
                        }
                        if (newPassword != confirmNewPassword) {
                            localError = "Mật khẩu xác nhận không khớp."
                            return@clickable
                        }
                        viewModel.onChangePassword(currentPassword, newPassword) {
                            // Clear input fields on success
                            currentPassword = ""
                            newPassword = ""
                            confirmNewPassword = ""
                        }
                    }
                    .padding(horizontal = 16.dp, vertical = 9.dp),
                contentAlignment = Alignment.Center
            ) {
                if (uiState.isChangingPassword) {
                    CircularProgressIndicator(
                        color = ColorInk,
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = "Xác nhận",
                        fontFamily = DmSansFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = ColorInk
                    )
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !uiState.isChangingPassword,
                colors = ButtonDefaults.textButtonColors(contentColor = ColorTextOnLightSecondary)
            ) {
                Text(
                    text = "Đóng",
                    fontFamily = DmSansFamily,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        },
        containerColor = Color.White,
        shape = RoundedCornerShape(16.dp)
    )
}
