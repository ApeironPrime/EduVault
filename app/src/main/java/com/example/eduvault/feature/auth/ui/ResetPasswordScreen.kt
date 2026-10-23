package com.example.eduvault.feature.auth.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.eduvault.core.theme.ColorAmber
import com.example.eduvault.core.theme.ColorAmberDark
import com.example.eduvault.core.theme.ColorAmberLight
import com.example.eduvault.core.theme.ColorBorder
import com.example.eduvault.core.theme.ColorCream
import com.example.eduvault.core.theme.ColorError
import com.example.eduvault.core.theme.ColorForest
import com.example.eduvault.core.theme.ColorForestLight
import com.example.eduvault.core.theme.ColorInk
import com.example.eduvault.core.theme.ColorInkLight
import com.example.eduvault.core.theme.ColorPaper
import com.example.eduvault.core.theme.ColorTextOnDark
import com.example.eduvault.core.theme.ColorTextOnDarkSecondary
import com.example.eduvault.core.theme.ColorTextOnLight
import com.example.eduvault.core.theme.ColorTextOnLightSecondary
import com.example.eduvault.core.theme.DmSansFamily
import com.example.eduvault.core.theme.EduVaultTheme
import com.example.eduvault.core.theme.JetBrainsMonoFamily
import com.example.eduvault.core.theme.PlayfairDisplayFamily

// ─── Breakpoint ───────────────────────────────────────────────────────────────
private val RP_SPLIT_BREAKPOINT = 600.dp

// ─── Entry Point ──────────────────────────────────────────────────────────────

@Composable
fun ResetPasswordScreen(
    onNavigateBack: () -> Unit = {},
    onResetSuccess: () -> Unit = {},
    viewModel: ResetPasswordViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    // Điều hướng khi đặt lại mật khẩu thành công
    LaunchedEffect(uiState.isResetSuccess) {
        if (uiState.isResetSuccess) onResetSuccess()
    }

    // Hiện snackbar khi có lỗi
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { error ->
            snackbarHostState.showSnackbar(message = error, duration = SnackbarDuration.Short)
            viewModel.onDismissError()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = ColorInk,
    ) { paddingValues ->
        BoxWithConstraints(modifier = Modifier.padding(paddingValues)) {
            if (maxWidth < RP_SPLIT_BREAKPOINT) {
                ResetPasswordPhoneLayout(
                    uiState = uiState,
                    onNewPasswordChange = viewModel::onNewPasswordChange,
                    onConfirmPasswordChange = viewModel::onConfirmPasswordChange,
                    onTogglePasswordVisibility = viewModel::onTogglePasswordVisibility,
                    onToggleConfirmPasswordVisibility = viewModel::onToggleConfirmPasswordVisibility,
                    onResetClick = viewModel::onResetClick,
                    onNavigateBack = onNavigateBack,
                )
            } else {
                ResetPasswordTabletLayout(
                    uiState = uiState,
                    onNewPasswordChange = viewModel::onNewPasswordChange,
                    onConfirmPasswordChange = viewModel::onConfirmPasswordChange,
                    onTogglePasswordVisibility = viewModel::onTogglePasswordVisibility,
                    onToggleConfirmPasswordVisibility = viewModel::onToggleConfirmPasswordVisibility,
                    onResetClick = viewModel::onResetClick,
                    onNavigateBack = onNavigateBack,
                )
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// LAYOUT 1: PHONE PORTRAIT (< 600dp)
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
private fun ResetPasswordPhoneLayout(
    uiState: ResetPasswordUiState,
    onNewPasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit,
    onTogglePasswordVisibility: () -> Unit,
    onToggleConfirmPasswordVisibility: () -> Unit,
    onResetClick: () -> Unit,
    onNavigateBack: () -> Unit,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "rp_phone_glow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.15f,
        targetValue = 0.32f,
        animationSpec = infiniteRepeatable(
            animation = tween(2800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "rp_phone_glow_alpha"
    )

    Column(modifier = Modifier.fillMaxSize()) {

        // ── Brand Header ───────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .background(ColorInk)
                .drawBehind {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(ColorAmber.copy(alpha = glowAlpha), Color.Transparent),
                            center = Offset(size.width * 0.5f, size.height * 0.45f),
                            radius = size.width * 0.8f
                        ),
                        radius = size.width * 0.8f,
                        center = Offset(size.width * 0.5f, size.height * 0.45f),
                    )
                    // Success green glow nhỏ ở góc
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(ColorForest.copy(alpha = 0.18f), Color.Transparent),
                            center = Offset(size.width * 0.88f, size.height * 0.85f),
                            radius = size.width * 0.45f
                        ),
                        radius = size.width * 0.45f,
                        center = Offset(size.width * 0.88f, size.height * 0.85f),
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .align(Alignment.TopStart)
                    .offset(x = (-20).dp, y = (-20).dp)
                    .clip(CircleShape)
                    .border(1.dp, ColorAmber.copy(alpha = 0.15f), CircleShape)
            )
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .align(Alignment.BottomEnd)
                    .offset(x = 10.dp, y = 15.dp)
                    .clip(CircleShape)
                    .border(1.dp, ColorAmberLight.copy(alpha = 0.12f), CircleShape)
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 20.dp),
            ) {
                // Back button
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(ColorInkLight)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                        contentDescription = "Quay lại",
                        tint = ColorTextOnDark,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(ColorAmber, ColorAmberDark)
                                )
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "E",
                            fontFamily = PlayfairDisplayFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 24.sp,
                            color = ColorInk,
                        )
                    }
                    Column {
                        Text(
                            text = "EDUVAULT",
                            fontFamily = JetBrainsMonoFamily,
                            fontWeight = FontWeight.Medium,
                            fontSize = 9.sp,
                            color = ColorAmber,
                            letterSpacing = 3.sp,
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = buildAnnotatedString {
                                append("Tạo ")
                                withStyle(SpanStyle(fontStyle = FontStyle.Italic, color = ColorAmber)) {
                                    append("mật khẩu mới")
                                }
                            },
                            fontFamily = PlayfairDisplayFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = ColorTextOnDark,
                        )
                    }
                }
            }
        }

        // ── Form Panel ────────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                .background(ColorCream)
        ) {
            ResetPasswordFormPanel(
                uiState = uiState,
                onNewPasswordChange = onNewPasswordChange,
                onConfirmPasswordChange = onConfirmPasswordChange,
                onTogglePasswordVisibility = onTogglePasswordVisibility,
                onToggleConfirmPasswordVisibility = onToggleConfirmPasswordVisibility,
                onResetClick = onResetClick,
                isCompact = true,
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// LAYOUT 2: TABLET / LANDSCAPE (≥ 600dp)
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
private fun ResetPasswordTabletLayout(
    uiState: ResetPasswordUiState,
    onNewPasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit,
    onTogglePasswordVisibility: () -> Unit,
    onToggleConfirmPasswordVisibility: () -> Unit,
    onResetClick: () -> Unit,
    onNavigateBack: () -> Unit,
) {
    Box(modifier = Modifier.fillMaxSize()) {
        ResetPasswordLeftPanel(
            onNavigateBack = onNavigateBack,
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(0.42f)
                .align(Alignment.CenterStart)
        )
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(0.58f)
                .align(Alignment.CenterEnd)
                .clip(RoundedCornerShape(topStart = 28.dp, bottomStart = 28.dp))
                .background(ColorCream)
        ) {
            ResetPasswordFormPanel(
                uiState = uiState,
                onNewPasswordChange = onNewPasswordChange,
                onConfirmPasswordChange = onConfirmPasswordChange,
                onTogglePasswordVisibility = onTogglePasswordVisibility,
                onToggleConfirmPasswordVisibility = onToggleConfirmPasswordVisibility,
                onResetClick = onResetClick,
                isCompact = false,
            )
        }
    }
}

// ─── Left Brand Panel (Tablet) ────────────────────────────────────────────────

@Composable
private fun ResetPasswordLeftPanel(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "rp_left_glow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.10f,
        targetValue = 0.26f,
        animationSpec = infiniteRepeatable(
            animation = tween(3200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "rp_left_glow_alpha"
    )

    Box(
        modifier = modifier
            .background(ColorInk)
            .drawBehind {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(ColorAmber.copy(alpha = glowAlpha), Color.Transparent),
                        center = Offset(size.width * 0.45f, size.height * 0.38f),
                        radius = size.width * 1.1f
                    ),
                    radius = size.width * 1.1f,
                    center = Offset(size.width * 0.45f, size.height * 0.38f),
                )
                // Forest glow — gợi ý thành công
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(ColorForest.copy(alpha = glowAlpha * 0.5f), Color.Transparent),
                        center = Offset(size.width * 0.8f, size.height * 0.78f),
                        radius = size.width * 0.65f
                    ),
                    radius = size.width * 0.65f,
                    center = Offset(size.width * 0.8f, size.height * 0.78f),
                )
            },
        contentAlignment = Alignment.Center
    ) {
        // Decorative circles
        Box(
            modifier = Modifier
                .size(115.dp)
                .offset(x = (-28).dp, y = (-125).dp)
                .clip(CircleShape)
                .border(1.dp, ColorAmberLight.copy(alpha = 0.20f), CircleShape)
        )
        Box(
            modifier = Modifier
                .size(60.dp)
                .offset(x = 30.dp, y = (-175).dp)
                .clip(CircleShape)
                .border(1.dp, ColorAmber.copy(alpha = 0.10f), CircleShape)
        )
        Box(
            modifier = Modifier
                .size(48.dp)
                .offset(x = (-28).dp, y = 155.dp)
                .clip(CircleShape)
                .border(1.dp, ColorForestLight.copy(alpha = 0.20f), CircleShape)
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Center,
        ) {
            // Back button
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(ColorInkLight)
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) { onNavigateBack() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                    contentDescription = "Quay lại",
                    tint = ColorTextOnDark,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Logo mark
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        brush = Brush.linearGradient(colors = listOf(ColorAmber, ColorAmberDark))
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "E",
                    fontFamily = PlayfairDisplayFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 28.sp,
                    color = ColorInk,
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = "EduVault",
                fontFamily = JetBrainsMonoFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 11.sp,
                color = ColorAmber,
                letterSpacing = 3.sp,
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = buildAnnotatedString {
                    append("Tạo mật khẩu\n")
                    withStyle(
                        SpanStyle(
                            fontStyle = FontStyle.Italic,
                            color = ColorAmber,
                            fontFamily = PlayfairDisplayFamily,
                        )
                    ) { append("mới") }
                    append(" cho bạn")
                },
                fontFamily = PlayfairDisplayFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
                lineHeight = 30.sp,
                color = ColorTextOnDark,
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Mật khẩu mới phải có ít nhất 8 ký tự. Hãy chọn mật khẩu dễ nhớ nhưng khó đoán.",
                fontFamily = DmSansFamily,
                fontSize = 12.sp,
                lineHeight = 18.sp,
                color = ColorTextOnDarkSecondary,
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Security tips
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                PasswordTip(icon = "🔐", text = "Ít nhất 8 ký tự")
                PasswordTip(icon = "🔤", text = "Kết hợp chữ hoa và chữ thường")
                PasswordTip(icon = "🔢", text = "Thêm số và ký tự đặc biệt")
            }
        }
    }
}

@Composable
private fun PasswordTip(icon: String, text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(text = icon, fontSize = 13.sp)
        Text(
            text = text,
            fontFamily = DmSansFamily,
            fontSize = 12.sp,
            color = ColorTextOnDarkSecondary,
        )
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// SHARED: Reset Password Form Panel
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
private fun ResetPasswordFormPanel(
    uiState: ResetPasswordUiState,
    onNewPasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit,
    onTogglePasswordVisibility: () -> Unit,
    onToggleConfirmPasswordVisibility: () -> Unit,
    onResetClick: () -> Unit,
    isCompact: Boolean,
    modifier: Modifier = Modifier,
) {
    val focusManager = LocalFocusManager.current
    val horizontalPadding = if (isCompact) 24.dp else 32.dp
    val verticalPadding = if (isCompact) 28.dp else 40.dp
    val titleFontSize = if (isCompact) 24.sp else 28.sp

    val buttonScale by animateFloatAsState(
        targetValue = if (uiState.isLoading) 0.97f else 1f,
        animationSpec = tween(150),
        label = "rp_btn_scale"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .imePadding()
            .padding(horizontal = horizontalPadding, vertical = verticalPadding),
        verticalArrangement = Arrangement.Center,
    ) {
        // ── Heading ───────────────────────────────────────────────────────
        Text(
            text = "Đặt lại",
            fontFamily = PlayfairDisplayFamily,
            fontWeight = FontWeight.Bold,
            fontSize = titleFontSize,
            color = ColorInk,
        )
        Text(
            text = buildAnnotatedString {
                withStyle(SpanStyle(fontStyle = FontStyle.Italic, color = ColorAmberDark)) {
                    append("mật khẩu")
                }
                append(" mới")
            },
            fontFamily = PlayfairDisplayFamily,
            fontWeight = FontWeight.Bold,
            fontSize = titleFontSize,
            color = ColorInk,
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Mật khẩu mới phải có ít nhất 8 ký tự",
            fontFamily = DmSansFamily,
            fontWeight = FontWeight.Normal,
            fontSize = if (isCompact) 12.sp else 13.sp,
            color = ColorTextOnLightSecondary,
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Badge hiển thị chế độ hoạt động cực kỳ chuyên nghiệp
        if (uiState.isDemoMode) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(ColorAmberLight.copy(alpha = 0.2f))
                    .border(1.dp, ColorAmber.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "Liên kết khôi phục chưa hợp lệ",
                    fontFamily = DmSansFamily,
                    fontSize = 10.sp,
                    color = ColorAmberDark,
                    fontWeight = FontWeight.SemiBold
                )
            }
        } else {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(ColorForestLight.copy(alpha = 0.15f))
                    .border(1.dp, ColorForest.copy(alpha = 0.25f), RoundedCornerShape(6.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "Liên kết khôi phục hợp lệ",
                    fontFamily = DmSansFamily,
                    fontSize = 10.sp,
                    color = ColorForest,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // ── Password strength indicator ───────────────────────────────────
        PasswordStrengthIndicator(password = uiState.newPassword)

        Spacer(modifier = Modifier.height(16.dp))

        // ── Mật khẩu mới ─────────────────────────────────────────────────
        ResetTextField(
            value = uiState.newPassword,
            onValueChange = onNewPasswordChange,
            label = "Mật khẩu mới",
            placeholder = "Ít nhất 8 ký tự",
            leadingIcon = {
                Icon(
                    imageVector = Icons.Outlined.Lock,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
            },
            trailingIcon = {
                IconButton(onClick = onTogglePasswordVisibility) {
                    Icon(
                        imageVector = if (uiState.isPasswordVisible)
                            Icons.Outlined.Visibility else Icons.Outlined.VisibilityOff,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                }
            },
            visualTransformation = if (uiState.isPasswordVisible)
                VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Next,
            ),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) }
            ),
            isError = uiState.hasAttemptedReset && uiState.newPasswordError != null,
            errorMessage = uiState.newPasswordError,
        )

        Spacer(modifier = Modifier.height(14.dp))

        // ── Xác nhận mật khẩu ────────────────────────────────────────────
        ResetTextField(
            value = uiState.confirmPassword,
            onValueChange = onConfirmPasswordChange,
            label = "Xác nhận mật khẩu mới",
            placeholder = "Nhập lại mật khẩu",
            leadingIcon = {
                Icon(
                    imageVector = if (uiState.confirmPassword.isNotEmpty() && uiState.confirmPassword == uiState.newPassword)
                        Icons.Outlined.CheckCircle else Icons.Outlined.Lock,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
            },
            trailingIcon = {
                IconButton(onClick = onToggleConfirmPasswordVisibility) {
                    Icon(
                        imageVector = if (uiState.isConfirmPasswordVisible)
                            Icons.Outlined.Visibility else Icons.Outlined.VisibilityOff,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                }
            },
            visualTransformation = if (uiState.isConfirmPasswordVisible)
                VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done,
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    focusManager.clearFocus()
                    onResetClick()
                }
            ),
            isError = uiState.hasAttemptedReset && uiState.confirmPasswordError != null,
            errorMessage = uiState.confirmPasswordError,
            // Highlight xanh khi 2 mật khẩu khớp
            matchSuccess = uiState.confirmPassword.isNotEmpty() && uiState.confirmPassword == uiState.newPassword,
        )

        Spacer(modifier = Modifier.height(24.dp))

        // ── Nút đặt lại mật khẩu ─────────────────────────────────────────
        Button(
            onClick = {
                focusManager.clearFocus()
                onResetClick()
            },
            enabled = !uiState.isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .graphicsLayer { scaleX = buttonScale; scaleY = buttonScale },
            shape = RoundedCornerShape(11.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = ColorAmber,
                contentColor = ColorInk,
                disabledContainerColor = ColorAmberLight.copy(alpha = 0.7f),
                disabledContentColor = ColorInk.copy(alpha = 0.5f),
            ),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp, pressedElevation = 0.dp)
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = ColorInk, strokeWidth = 2.dp)
            } else {
                Text(
                    text = "Đặt lại mật khẩu",
                    fontFamily = DmSansFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    letterSpacing = 0.3.sp,
                )
            }
        }
    }
}

// ─── Password Strength Indicator ─────────────────────────────────────────────

@Composable
private fun PasswordStrengthIndicator(password: String) {
    if (password.isEmpty()) return

    val strength = when {
        password.length < 6 -> 0          // Yếu
        password.length < 8 -> 1          // Trung bình
        password.length < 12 && !password.any { !it.isLetterOrDigit() } -> 2 // Khá
        else -> 3                          // Mạnh
    }

    val (label, color, filledBars) = when (strength) {
        0 -> Triple("Yếu", ColorError, 1)
        1 -> Triple("Trung bình", ColorAmber, 2)
        2 -> Triple("Khá", ColorAmberDark, 3)
        else -> Triple("Mạnh", ColorForest, 4)
    }

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Độ mạnh mật khẩu",
                fontFamily = DmSansFamily,
                fontSize = 11.sp,
                color = ColorTextOnLightSecondary,
            )
            Text(
                text = label,
                fontFamily = DmSansFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 11.sp,
                color = color,
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            repeat(4) { index ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(if (index < filledBars) color else ColorBorder)
                )
            }
        }
    }
}

// ─── Reset TextField (hỗ trợ matchSuccess) ───────────────────────────────────

@Composable
private fun ResetTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    isError: Boolean = false,
    errorMessage: String? = null,
    matchSuccess: Boolean = false,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    val borderColor = when {
        isError -> ColorError
        matchSuccess -> ColorForest
        isFocused -> ColorAmber
        else -> ColorBorder
    }
    val labelColor = when {
        isError -> ColorError
        matchSuccess -> ColorForest
        isFocused -> ColorAmber
        else -> ColorTextOnLightSecondary
    }

    Column {
        Text(
            text = label,
            fontFamily = DmSansFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 12.sp,
            color = labelColor,
            modifier = Modifier.padding(bottom = 6.dp)
        )

        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text(
                    text = placeholder,
                    fontFamily = DmSansFamily,
                    fontSize = 14.sp,
                    color = ColorBorder,
                )
            },
            leadingIcon = leadingIcon,
            trailingIcon = trailingIcon,
            visualTransformation = visualTransformation,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            isError = isError,
            interactionSource = interactionSource,
            singleLine = true,
            shape = RoundedCornerShape(11.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = borderColor,
                unfocusedBorderColor = borderColor,
                errorBorderColor = ColorError,
                focusedLeadingIconColor = borderColor,
                unfocusedLeadingIconColor = if (matchSuccess) ColorForest else ColorTextOnLightSecondary,
                errorLeadingIconColor = ColorError,
                focusedTrailingIconColor = ColorAmber,
                unfocusedTrailingIconColor = ColorTextOnLightSecondary,
                focusedTextColor = ColorTextOnLight,
                unfocusedTextColor = ColorTextOnLight,
                cursorColor = ColorAmber,
                errorTextColor = ColorTextOnLight,
                errorCursorColor = ColorError,
                focusedContainerColor = Color.White,
                unfocusedContainerColor = if (matchSuccess) ColorForest.copy(alpha = 0.04f) else ColorPaper,
                errorContainerColor = Color.White,
            )
        )

        AnimatedVisibility(
            visible = isError && errorMessage != null,
            enter = fadeIn() + slideInVertically(),
            exit = fadeOut()
        ) {
            Text(
                text = errorMessage ?: "",
                fontFamily = DmSansFamily,
                fontSize = 11.sp,
                color = ColorError,
                modifier = Modifier.padding(top = 4.dp, start = 4.dp)
            )
        }

        // Match success message
        AnimatedVisibility(
            visible = matchSuccess && !isError,
            enter = fadeIn() + slideInVertically(),
            exit = fadeOut()
        ) {
            Row(
                modifier = Modifier.padding(top = 4.dp, start = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.CheckCircle,
                    contentDescription = null,
                    tint = ColorForest,
                    modifier = Modifier.size(12.dp)
                )
                Text(
                    text = "Mật khẩu khớp",
                    fontFamily = DmSansFamily,
                    fontSize = 11.sp,
                    color = ColorForest,
                )
            }
        }
    }
}

// ─── Previews ─────────────────────────────────────────────────────────────────

@Preview(name = "ResetPassword — Phone", showBackground = true, widthDp = 360, heightDp = 780)
@Composable
private fun ResetPasswordPhonePreview() {
    EduVaultTheme(darkTheme = false) {
        ResetPasswordPhoneLayout(
            uiState = ResetPasswordUiState(),
            onNewPasswordChange = {},
            onConfirmPasswordChange = {},
            onTogglePasswordVisibility = {},
            onToggleConfirmPasswordVisibility = {},
            onResetClick = {},
            onNavigateBack = {},
        )
    }
}

@Preview(name = "ResetPassword — Match Success", showBackground = true, widthDp = 360, heightDp = 780)
@Composable
private fun ResetPasswordMatchPreview() {
    EduVaultTheme(darkTheme = false) {
        ResetPasswordPhoneLayout(
            uiState = ResetPasswordUiState(
                newPassword = "MyPassword123",
                confirmPassword = "MyPassword123",
            ),
            onNewPasswordChange = {},
            onConfirmPasswordChange = {},
            onTogglePasswordVisibility = {},
            onToggleConfirmPasswordVisibility = {},
            onResetClick = {},
            onNavigateBack = {},
        )
    }
}

@Preview(name = "ResetPassword — Tablet", showBackground = true, widthDp = 840, heightDp = 600)
@Composable
private fun ResetPasswordTabletPreview() {
    EduVaultTheme(darkTheme = false) {
        ResetPasswordTabletLayout(
            uiState = ResetPasswordUiState(),
            onNewPasswordChange = {},
            onConfirmPasswordChange = {},
            onTogglePasswordVisibility = {},
            onToggleConfirmPasswordVisibility = {},
            onResetClick = {},
            onNavigateBack = {},
        )
    }
}
