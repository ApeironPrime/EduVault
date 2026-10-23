package com.example.eduvault.feature.profile.ui.dialogs

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.eduvault.core.theme.ColorAmber
import com.example.eduvault.core.theme.ColorAmberDark
import com.example.eduvault.core.theme.ColorBorder
import com.example.eduvault.core.theme.ColorCream
import com.example.eduvault.core.theme.ColorError
import com.example.eduvault.core.theme.ColorForest
import com.example.eduvault.core.theme.ColorInk
import com.example.eduvault.core.theme.ColorTextOnLightSecondary
import com.example.eduvault.core.theme.DmSansFamily
import com.example.eduvault.feature.profile.ui.ProfileUiState
import com.example.eduvault.feature.profile.ui.ProfileViewModel

@Composable
fun ChangePasswordDialog(
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
    DisposableEffect(Unit) {
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
                    Icon(Icons.Outlined.Lock, null, Modifier.size(18.dp), tint = ColorAmberDark)
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
                modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
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
