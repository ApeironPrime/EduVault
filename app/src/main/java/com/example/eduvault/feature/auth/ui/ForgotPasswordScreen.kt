package com.example.eduvault.feature.auth.ui

import androidx.compose.animation.AnimatedContent
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
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
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
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
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
import androidx.compose.ui.text.style.TextAlign
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
private val FP_SPLIT_BREAKPOINT = 600.dp

// ─── Entry Point ──────────────────────────────────────────────────────────────

@Composable
fun ForgotPasswordScreen(
    onNavigateBack: () -> Unit = {},
    onNavigateToResetPassword: (String?) -> Unit = {},
    viewModel: ForgotPasswordViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    // Điều hướng sang ResetPassword khi xác thực OTP thành công
    LaunchedEffect(uiState.isVerifySuccess) {
        if (uiState.isVerifySuccess) onNavigateToResetPassword(uiState.oobCode)
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
            if (maxWidth < FP_SPLIT_BREAKPOINT) {
                ForgotPasswordPhoneLayout(
                    uiState = uiState,
                    onEmailChange = viewModel::onEmailChange,
                    onOtpChange = viewModel::onOtpChange,
                    onLinkInputChange = viewModel::onLinkInputChange,
                    onSendOtpClick = viewModel::onSendOtpClick,
                    onVerifyOtpClick = viewModel::onVerifyOtpClick,
                    onResendOtpClick = viewModel::onResendOtpClick,
                    onBackToEmailStep = viewModel::onBackToEmailStep,
                    onNavigateBack = onNavigateBack,
                )
            } else {
                ForgotPasswordTabletLayout(
                    uiState = uiState,
                    onEmailChange = viewModel::onEmailChange,
                    onOtpChange = viewModel::onOtpChange,
                    onLinkInputChange = viewModel::onLinkInputChange,
                    onSendOtpClick = viewModel::onSendOtpClick,
                    onVerifyOtpClick = viewModel::onVerifyOtpClick,
                    onResendOtpClick = viewModel::onResendOtpClick,
                    onBackToEmailStep = viewModel::onBackToEmailStep,
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
private fun ForgotPasswordPhoneLayout(
    uiState: ForgotPasswordUiState,
    onEmailChange: (String) -> Unit,
    onOtpChange: (String) -> Unit,
    onLinkInputChange: (String) -> Unit,
    onSendOtpClick: () -> Unit,
    onVerifyOtpClick: () -> Unit,
    onResendOtpClick: () -> Unit,
    onBackToEmailStep: () -> Unit,
    onNavigateBack: () -> Unit,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "fp_phone_glow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.15f,
        targetValue = 0.30f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "fp_phone_glow_alpha"
    )

    Column(modifier = Modifier.fillMaxSize()) {

        // ── Compact Brand Header ───────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .background(ColorInk)
                .drawBehind {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(ColorAmber.copy(alpha = glowAlpha), Color.Transparent),
                            center = Offset(size.width * 0.4f, size.height * 0.5f),
                            radius = size.width * 0.75f
                        ),
                        radius = size.width * 0.75f,
                        center = Offset(size.width * 0.4f, size.height * 0.5f),
                    )
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(ColorForest.copy(alpha = 0.12f), Color.Transparent),
                            center = Offset(size.width * 0.9f, size.height * 0.3f),
                            radius = size.width * 0.55f
                        ),
                        radius = size.width * 0.55f,
                        center = Offset(size.width * 0.9f, size.height * 0.3f),
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            // Decorative circles
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .align(Alignment.TopStart)
                    .offset(x = (-22).dp, y = (-22).dp)
                    .clip(CircleShape)
                    .border(1.dp, ColorAmberLight.copy(alpha = 0.18f), CircleShape)
            )
            Box(
                modifier = Modifier
                    .size(55.dp)
                    .align(Alignment.TopEnd)
                    .offset(x = 12.dp, y = 35.dp)
                    .clip(CircleShape)
                    .border(1.dp, ColorAmber.copy(alpha = 0.10f), CircleShape)
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 20.dp),
            ) {
                // Back button
                IconButton(
                    onClick = if (uiState.isOtpSent) onBackToEmailStep else onNavigateBack,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(ColorInkLight)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.ArrowBack,
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
                    // Logo mark
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
                                append("Khôi phục ")
                                withStyle(SpanStyle(fontStyle = FontStyle.Italic, color = ColorAmber)) {
                                    append("tài khoản")
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
            ForgotPasswordFormPanel(
                uiState = uiState,
                onEmailChange = onEmailChange,
                onOtpChange = onOtpChange,
                onLinkInputChange = onLinkInputChange,
                onSendOtpClick = onSendOtpClick,
                onVerifyOtpClick = onVerifyOtpClick,
                onResendOtpClick = onResendOtpClick,
                isCompact = true,
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// LAYOUT 2: TABLET / LANDSCAPE (≥ 600dp)
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
private fun ForgotPasswordTabletLayout(
    uiState: ForgotPasswordUiState,
    onEmailChange: (String) -> Unit,
    onOtpChange: (String) -> Unit,
    onLinkInputChange: (String) -> Unit,
    onSendOtpClick: () -> Unit,
    onVerifyOtpClick: () -> Unit,
    onResendOtpClick: () -> Unit,
    onBackToEmailStep: () -> Unit,
    onNavigateBack: () -> Unit,
) {
    Box(modifier = Modifier.fillMaxSize()) {
        ForgotPasswordLeftPanel(
            isOtpSent = uiState.isOtpSent,
            onNavigateBack = if (uiState.isOtpSent) onBackToEmailStep else onNavigateBack,
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
            ForgotPasswordFormPanel(
                uiState = uiState,
                onEmailChange = onEmailChange,
                onOtpChange = onOtpChange,
                onLinkInputChange = onLinkInputChange,
                onSendOtpClick = onSendOtpClick,
                onVerifyOtpClick = onVerifyOtpClick,
                onResendOtpClick = onResendOtpClick,
                isCompact = false,
            )
        }
    }
}

// ─── Left Brand Panel (Tablet) ────────────────────────────────────────────────

@Composable
private fun ForgotPasswordLeftPanel(
    isOtpSent: Boolean,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "fp_left_glow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.10f,
        targetValue = 0.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(3200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "fp_left_glow_alpha"
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
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(ColorForest.copy(alpha = glowAlpha * 0.4f), Color.Transparent),
                        center = Offset(size.width * 0.85f, size.height * 0.82f),
                        radius = size.width * 0.7f
                    ),
                    radius = size.width * 0.7f,
                    center = Offset(size.width * 0.85f, size.height * 0.82f),
                )
            },
        contentAlignment = Alignment.Center
    ) {
        // Decorative circles
        Box(
            modifier = Modifier
                .size(120.dp)
                .offset(x = (-30).dp, y = (-120).dp)
                .clip(CircleShape)
                .border(1.dp, ColorAmberLight.copy(alpha = 0.20f), CircleShape)
        )
        Box(
            modifier = Modifier
                .size(65.dp)
                .offset(x = 35.dp, y = (-175).dp)
                .clip(CircleShape)
                .border(1.dp, ColorAmber.copy(alpha = 0.10f), CircleShape)
        )
        Box(
            modifier = Modifier
                .size(50.dp)
                .offset(x = (-30).dp, y = 160.dp)
                .clip(CircleShape)
                .border(1.dp, ColorAmber.copy(alpha = 0.15f), CircleShape)
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
                    imageVector = Icons.Outlined.ArrowBack,
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

            AnimatedContent(
                targetState = isOtpSent,
                transitionSpec = {
                    (fadeIn(tween(300)) + slideInHorizontally { it / 4 })
                        .togetherWith(fadeOut(tween(200)) + slideOutHorizontally { -it / 4 })
                },
                label = "fp_left_title"
            ) { otpSent ->
                Column {
                    if (!otpSent) {
                        Text(
                            text = buildAnnotatedString {
                                append("Quên\n")
                                withStyle(
                                    SpanStyle(
                                        fontStyle = FontStyle.Italic,
                                        color = ColorAmber,
                                        fontFamily = PlayfairDisplayFamily,
                                    )
                                ) { append("mật khẩu?") }
                            },
                            fontFamily = PlayfairDisplayFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 24.sp,
                            lineHeight = 32.sp,
                            color = ColorTextOnDark,
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Nhập địa chỉ email đã đăng ký để nhận liên kết đặt lại mật khẩu.",
                            fontFamily = DmSansFamily,
                            fontSize = 12.sp,
                            lineHeight = 18.sp,
                            color = ColorTextOnDarkSecondary,
                        )
                    } else {
                        Text(
                            text = buildAnnotatedString {
                                append("Kiểm tra\n")
                                withStyle(
                                    SpanStyle(
                                        fontStyle = FontStyle.Italic,
                                        color = ColorAmber,
                                        fontFamily = PlayfairDisplayFamily,
                                    )
                                ) { append("email") }
                                append(" của bạn")
                            },
                            fontFamily = PlayfairDisplayFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp,
                            lineHeight = 30.sp,
                            color = ColorTextOnDark,
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Mở liên kết trong email để đặt lại mật khẩu, hoặc dán liên kết vào đây để tiếp tục trong ứng dụng.",
                            fontFamily = DmSansFamily,
                            fontSize = 12.sp,
                            lineHeight = 18.sp,
                            color = ColorTextOnDarkSecondary,
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Step indicator
            ForgotPasswordStepIndicator(isOtpSent = isOtpSent)
        }
    }
}

@Composable
private fun ForgotPasswordStepIndicator(isOtpSent: Boolean) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
        // Bước 1
        Box(
            modifier = Modifier
                .size(if (!isOtpSent) 28.dp else 20.dp)
                .clip(CircleShape)
                .background(if (!isOtpSent) ColorAmber else ColorAmberDark.copy(alpha = 0.5f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (isOtpSent) "✓" else "1",
                fontFamily = JetBrainsMonoFamily,
                fontSize = if (!isOtpSent) 11.sp else 10.sp,
                color = ColorInk,
                fontWeight = FontWeight.Bold,
            )
        }

        Box(modifier = Modifier.height(1.dp).width(20.dp).background(ColorAmberDark.copy(alpha = 0.4f)))

        // Bước 2
        Box(
            modifier = Modifier
                .size(if (isOtpSent) 28.dp else 20.dp)
                .clip(CircleShape)
                .background(if (isOtpSent) ColorAmber else ColorAmberDark.copy(alpha = 0.3f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "2",
                fontFamily = JetBrainsMonoFamily,
                fontSize = if (isOtpSent) 11.sp else 10.sp,
                color = if (isOtpSent) ColorInk else ColorTextOnDarkSecondary,
                fontWeight = FontWeight.Bold,
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = if (!isOtpSent) "Nhập email" else "Xác minh liên kết",
            fontFamily = DmSansFamily,
            fontSize = 11.sp,
            color = ColorTextOnDarkSecondary,
        )
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// SHARED: Form Panel (hiển thị 2 bước với animation)
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
private fun ForgotPasswordFormPanel(
    uiState: ForgotPasswordUiState,
    onEmailChange: (String) -> Unit,
    onOtpChange: (String) -> Unit,
    onLinkInputChange: (String) -> Unit,
    onSendOtpClick: () -> Unit,
    onVerifyOtpClick: () -> Unit,
    onResendOtpClick: () -> Unit,
    isCompact: Boolean,
    modifier: Modifier = Modifier,
) {
    val horizontalPadding = if (isCompact) 24.dp else 32.dp
    val verticalPadding = if (isCompact) 28.dp else 40.dp
    val titleFontSize = if (isCompact) 24.sp else 28.sp

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .imePadding()
            .padding(horizontal = horizontalPadding, vertical = verticalPadding),
        verticalArrangement = Arrangement.Center,
    ) {
        AnimatedContent(
            targetState = uiState.isOtpSent,
            transitionSpec = {
                (fadeIn(tween(350)) + slideInHorizontally { it / 3 })
                    .togetherWith(fadeOut(tween(250)) + slideOutHorizontally { -it / 3 })
            },
            label = "fp_form_content"
        ) { isOtpSent ->
            if (!isOtpSent) {
                // ── Bước 1: Nhập email ────────────────────────────────────
                EmailStepContent(
                    uiState = uiState,
                    onEmailChange = onEmailChange,
                    onSendOtpClick = onSendOtpClick,
                    titleFontSize = titleFontSize,
                    isCompact = isCompact,
                )
            } else {
                // ── Bước 2: Nhập OTP ─────────────────────────────────────
                OtpStepContent(
                    uiState = uiState,
                    onOtpChange = onOtpChange,
                    onLinkInputChange = onLinkInputChange,
                    onVerifyOtpClick = onVerifyOtpClick,
                    onResendOtpClick = onResendOtpClick,
                    titleFontSize = titleFontSize,
                    isCompact = isCompact,
                )
            }
        }
    }
}

// ─── Bước 1: Form nhập email ──────────────────────────────────────────────────

@Composable
private fun EmailStepContent(
    uiState: ForgotPasswordUiState,
    onEmailChange: (String) -> Unit,
    onSendOtpClick: () -> Unit,
    titleFontSize: androidx.compose.ui.unit.TextUnit,
    isCompact: Boolean,
) {
    val focusManager = LocalFocusManager.current
    val buttonScale by animateFloatAsState(
        targetValue = if (uiState.isLoading) 0.97f else 1f,
        animationSpec = tween(150),
        label = "fp_email_btn_scale"
    )

    Column {
        // ── Heading ───────────────────────────────────────────────────────
        Text(
            text = "Quên",
            fontFamily = PlayfairDisplayFamily,
            fontWeight = FontWeight.Bold,
            fontSize = titleFontSize,
            color = ColorInk,
        )
        Text(
            text = buildAnnotatedString {
                withStyle(SpanStyle(fontStyle = FontStyle.Italic, color = ColorAmberDark)) {
                    append("mật khẩu?")
                }
            },
            fontFamily = PlayfairDisplayFamily,
            fontWeight = FontWeight.Bold,
            fontSize = titleFontSize,
            color = ColorInk,
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Nhập email đã đăng ký để nhận liên kết đặt lại mật khẩu",
            fontFamily = DmSansFamily,
            fontWeight = FontWeight.Normal,
            fontSize = if (isCompact) 12.sp else 13.sp,
            color = ColorTextOnLightSecondary,
        )

        Spacer(modifier = Modifier.height(28.dp))

        // ── Email field ───────────────────────────────────────────────────
        AuthTextField(
            value = uiState.email,
            onValueChange = onEmailChange,
            label = "Email",
            placeholder = "email@example.com",
            leadingIcon = {
                Icon(
                    imageVector = Icons.Outlined.Email,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Done,
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    focusManager.clearFocus()
                    onSendOtpClick()
                }
            ),
            isError = uiState.hasAttemptedSend && uiState.emailError != null,
            errorMessage = uiState.emailError,
        )

        Spacer(modifier = Modifier.height(24.dp))

        // ── Nút gửi OTP ───────────────────────────────────────────────────
        Button(
            onClick = {
                focusManager.clearFocus()
                onSendOtpClick()
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
                    text = "Gửi liên kết khôi phục",
                    fontFamily = DmSansFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    letterSpacing = 0.3.sp,
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // ── Hint icon + text ──────────────────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(ColorAmberLight.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "✉", fontSize = 10.sp)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Kiểm tra cả thư mục Spam nếu không thấy email",
                fontFamily = DmSansFamily,
                fontSize = 11.sp,
                color = ColorTextOnLightSecondary,
                textAlign = TextAlign.Center,
            )
        }
    }
}

// ─── Bước 2: Form nhập OTP ────────────────────────────────────────────────────

@Composable
private fun OtpStepContent(
    uiState: ForgotPasswordUiState,
    onOtpChange: (String) -> Unit,
    onLinkInputChange: (String) -> Unit,
    onVerifyOtpClick: () -> Unit,
    onResendOtpClick: () -> Unit,
    titleFontSize: androidx.compose.ui.unit.TextUnit,
    isCompact: Boolean,
) {
    val focusManager = LocalFocusManager.current
    val buttonScale by animateFloatAsState(
        targetValue = if (uiState.isLoading) 0.97f else 1f,
        animationSpec = tween(150),
        label = "fp_otp_btn_scale"
    )

    Column {
        // ── Heading ───────────────────────────────────────────────────────
        Text(
            text = "Nhập mã",
            fontFamily = PlayfairDisplayFamily,
            fontWeight = FontWeight.Bold,
            fontSize = titleFontSize,
            color = ColorInk,
        )
        Text(
            text = buildAnnotatedString {
                withStyle(SpanStyle(fontStyle = FontStyle.Italic, color = ColorAmberDark)) {
                    append("xác nhận")
                }
            },
            fontFamily = PlayfairDisplayFamily,
            fontWeight = FontWeight.Bold,
            fontSize = titleFontSize,
            color = ColorInk,
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = buildAnnotatedString {
                append("Liên kết khôi phục đã gửi đến ")
                withStyle(SpanStyle(color = ColorAmberDark, fontWeight = FontWeight.SemiBold)) {
                    append(uiState.email)
                }
            },
            fontFamily = DmSansFamily,
            fontWeight = FontWeight.Normal,
            fontSize = if (isCompact) 12.sp else 13.sp,
            color = ColorTextOnLightSecondary,
        )

        Spacer(modifier = Modifier.height(28.dp))

        // ── Ô dán Link thật ────────────────────────────────────────────────
        AuthTextField(
            value = uiState.linkInput,
            onValueChange = onLinkInputChange,
            label = "Liên kết đặt lại mật khẩu",
            placeholder = "Dán liên kết khôi phục từ email",
            leadingIcon = {
                Icon(
                    imageVector = Icons.Outlined.Email,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
            },
            isError = uiState.linkInput.isNotEmpty() && !uiState.isRealLinkValid,
            errorMessage = if (uiState.linkInput.isNotEmpty() && !uiState.isRealLinkValid) "Liên kết khôi phục không hợp lệ hoặc đã hết hạn" else null
        )

        // Hiển thị Badge thông báo Email khôi phục hợp lệ
        if (uiState.isRealLinkValid && uiState.verifiedEmail != null) {
            Spacer(modifier = Modifier.height(10.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(ColorForest.copy(alpha = 0.15f))
                    .border(1.dp, ColorForest.copy(alpha = 0.25f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "✓ Mã khôi phục hợp lệ cho tài khoản: ${uiState.verifiedEmail}",
                    fontFamily = DmSansFamily,
                    fontSize = 11.sp,
                    color = ColorForest,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // ── Resend OTP ────────────────────────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (uiState.resendCountdown > 0) {
                Text(
                    text = "Gửi lại sau ${uiState.resendCountdown}s",
                    fontFamily = DmSansFamily,
                    fontSize = 12.sp,
                    color = ColorTextOnLightSecondary,
                )
            } else {
                Text(
                    text = "Gửi lại mã",
                    fontFamily = DmSansFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp,
                    color = ColorAmberDark,
                    modifier = Modifier.clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) { onResendOtpClick() }
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // ── Nút xác nhận ──────────────────────────────────────────────────
        Button(
            onClick = {
                focusManager.clearFocus()
                onVerifyOtpClick()
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
                    text = "Tiếp tục đặt mật khẩu",
                    fontFamily = DmSansFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    letterSpacing = 0.3.sp,
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // ── Security note ─────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(ColorAmberLight.copy(alpha = 0.15f))
                .border(1.dp, ColorAmber.copy(alpha = 0.25f), RoundedCornerShape(10.dp))
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Text(
                text = "Không chia sẻ liên kết khôi phục với người khác.",
                fontFamily = DmSansFamily,
                fontSize = 11.sp,
                color = ColorAmberDark,
                lineHeight = 16.sp,
            )
        }
    }
}

// ─── Shared TextField ─────────────────────────────────────────────────────────

@Composable
private fun AuthTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    isError: Boolean = false,
    errorMessage: String? = null,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    Column {
        Text(
            text = label,
            fontFamily = DmSansFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 12.sp,
            color = when {
                isError -> ColorError
                isFocused -> ColorAmber
                else -> ColorTextOnLightSecondary
            },
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
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            isError = isError,
            interactionSource = interactionSource,
            singleLine = true,
            shape = RoundedCornerShape(11.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = ColorAmber,
                unfocusedBorderColor = ColorBorder,
                errorBorderColor = ColorError,
                focusedLeadingIconColor = ColorAmber,
                unfocusedLeadingIconColor = ColorTextOnLightSecondary,
                errorLeadingIconColor = ColorError,
                focusedTextColor = ColorTextOnLight,
                unfocusedTextColor = ColorTextOnLight,
                cursorColor = ColorAmber,
                errorTextColor = ColorTextOnLight,
                errorCursorColor = ColorError,
                focusedContainerColor = Color.White,
                unfocusedContainerColor = ColorPaper,
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
    }
}

// ─── Previews ─────────────────────────────────────────────────────────────────

@Preview(name = "ForgotPassword — Email Step — Phone", showBackground = true, widthDp = 360, heightDp = 780)
@Composable
private fun ForgotPasswordEmailPreview() {
    EduVaultTheme(darkTheme = false) {
        ForgotPasswordPhoneLayout(
            uiState = ForgotPasswordUiState(),
            onEmailChange = {},
            onOtpChange = {},
            onLinkInputChange = {},
            onSendOtpClick = {},
            onVerifyOtpClick = {},
            onResendOtpClick = {},
            onBackToEmailStep = {},
            onNavigateBack = {},
        )
    }
}

@Preview(name = "ForgotPassword — OTP Step — Phone", showBackground = true, widthDp = 360, heightDp = 780)
@Composable
private fun ForgotPasswordOtpPreview() {
    EduVaultTheme(darkTheme = false) {
        ForgotPasswordPhoneLayout(
            uiState = ForgotPasswordUiState(
                email = "user@gmail.com",
                isOtpSent = true,
                resendCountdown = 45,
            ),
            onEmailChange = {},
            onOtpChange = {},
            onLinkInputChange = {},
            onSendOtpClick = {},
            onVerifyOtpClick = {},
            onResendOtpClick = {},
            onBackToEmailStep = {},
            onNavigateBack = {},
        )
    }
}

@Preview(name = "ForgotPassword — Tablet", showBackground = true, widthDp = 840, heightDp = 600)
@Composable
private fun ForgotPasswordTabletPreview() {
    EduVaultTheme(darkTheme = false) {
        ForgotPasswordTabletLayout(
            uiState = ForgotPasswordUiState(),
            onEmailChange = {},
            onOtpChange = {},
            onLinkInputChange = {},
            onSendOtpClick = {},
            onVerifyOtpClick = {},
            onResendOtpClick = {},
            onBackToEmailStep = {},
            onNavigateBack = {},
        )
    }
}
