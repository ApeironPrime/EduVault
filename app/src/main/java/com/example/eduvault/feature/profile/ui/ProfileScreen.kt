package com.example.eduvault.feature.profile.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.imePadding
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.eduvault.core.theme.ColorAmber
import com.example.eduvault.core.theme.ColorError
import com.example.eduvault.core.theme.ColorInk
import com.example.eduvault.core.theme.ColorTextOnLightSecondary
import com.example.eduvault.core.theme.DmSansFamily
import com.example.eduvault.feature.home.ui.TrainingUiState
import com.example.eduvault.domain.model.AuthState
import com.example.eduvault.feature.profile.ui.components.AlertBox
import com.example.eduvault.feature.profile.ui.components.ProfileEditForm
import com.example.eduvault.feature.profile.ui.components.ProfileHeaderSection
import com.example.eduvault.feature.profile.ui.components.ProfileViewDetails

@Composable
fun ProfileContent(
    paddingValues: PaddingValues,
    onLogout: () -> Unit,
    onNavigateToAdmin: () -> Unit = {},
    onNavigateToLogin: () -> Unit = {},
    onNavigateToRegister: () -> Unit = {},
    trainingState: TrainingUiState = TrainingUiState(),
    onRetryTraining: () -> Unit = {},
    onProfileUpdated: (com.example.eduvault.domain.model.User) -> Unit = {},
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    LaunchedEffect(uiState.successMessage) {
        if (uiState.successMessage != null) uiState.user?.let(onProfileUpdated)
    }

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
    BackHandler(enabled = uiState.isEditing) { viewModel.onCancelEditing() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
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
                    .imePadding()
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
                        onEditClick = viewModel::onStartEditing,
                        trainingState = trainingState,
                        onRetryTraining = onRetryTraining
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
                    fontFamily = DmSansFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = ColorInk
                )
            },
            text = {
                Text(
                    text = "Bạn có chắc chắn muốn đăng xuất khỏi nền tảng đào tạo EduVault?",
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
