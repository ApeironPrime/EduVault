package com.example.eduvault.feature.profile.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Cancel
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material.icons.outlined.Business
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.eduvault.core.theme.ColorAmber
import com.example.eduvault.core.theme.ColorAmberDark
import com.example.eduvault.core.theme.ColorBorder
import com.example.eduvault.core.theme.ColorCream
import com.example.eduvault.core.theme.ColorError
import com.example.eduvault.core.theme.ColorForest
import com.example.eduvault.core.theme.ColorInk
import com.example.eduvault.core.theme.ColorTextOnDark
import com.example.eduvault.core.theme.ColorTextOnDarkSecondary
import com.example.eduvault.core.theme.ColorTextOnLightSecondary
import com.example.eduvault.core.theme.DmSansFamily
import com.example.eduvault.feature.profile.ui.ProfileUiState
import com.example.eduvault.feature.profile.ui.ProfileViewModel
import com.example.eduvault.feature.profile.ui.dialogs.ChangePasswordDialog

@Composable
fun ProfileEditForm(
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
                .statusBarsPadding()
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
                    fontFamily = DmSansFamily,
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
                ) { Icon(Icons.Outlined.PhotoCamera, null, Modifier.size(18.dp), tint = ColorAmberDark) }
                Column {
                    Text(
                        text = "Ảnh đại diện",
                        fontFamily = DmSansFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = ColorInk
                    )
                    Text(
                        text = "Dùng ảnh từ thiết bị hoặc chữ viết tắt tên",
                        fontFamily = DmSansFamily,
                        fontSize = 11.sp,
                        color = ColorTextOnLightSecondary
                    )
                }
            }

            // ── Photo from device button ────────────────────────────────────
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
                        text = "Chọn ảnh từ thiết bị",
                        fontFamily = DmSansFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                        color = ColorAmberDark
                    )
                    Text(
                        text = "Xem trước ảnh trước khi lưu",
                        fontFamily = DmSansFamily,
                        fontSize = 10.5.sp,
                        color = ColorTextOnLightSecondary
                    )
                }
                Text("→", fontSize = 16.sp, color = ColorAmberDark)
            }

            Spacer(modifier = Modifier.height(16.dp))

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
                ) { Icon(Icons.Outlined.PersonOutline, null, Modifier.size(18.dp), tint = ColorForest) }
                Column {
                    Text(
                        text = "Thông tin cá nhân",
                        fontFamily = DmSansFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = ColorInk
                    )
                    Text(
                        text = "Thông tin tài khoản đào tạo",
                        fontFamily = DmSansFamily,
                        fontSize = 11.sp,
                        color = ColorTextOnLightSecondary
                    )
                }
            }

            // Email (read-only)
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

            // Organisational unit input (keeps the legacy field name for Firebase compatibility)
            OutlinedTextField(
                value = uiState.editUniversity,
                onValueChange = viewModel::onUniversityChange,
                label = {
                    Text(
                        "Đơn vị / phòng ban",
                        fontFamily = DmSansFamily,
                        fontSize = 12.sp
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.Business,
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
                ) { Icon(Icons.Outlined.Lock, null, Modifier.size(18.dp), tint = ColorAmberDark) }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Đổi mật khẩu tài khoản",
                        fontFamily = DmSansFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                        color = ColorInk
                    )
                    Text(
                        text = "Thay đổi mật khẩu đăng nhập",
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

@Composable
fun AlertBox(
    message: String,
    isSuccess: Boolean,
    icon: ImageVector
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
