package com.example.eduvault.feature.auth.ui

import androidx.compose.animation.AnimatedVisibility
import kotlinx.coroutines.launch
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
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
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
private val SPLIT_SCREEN_BREAKPOINT = 600.dp

// ─── Entry Point ──────────────────────────────────────────────────────────────

@Composable
fun RegisterScreen(
    onNavigateToLogin: () -> Unit = {},
    onNavigateToHome: () -> Unit = {},
    viewModel: RegisterViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = androidx.compose.ui.platform.LocalContext.current
    val coroutineScope = androidx.compose.runtime.rememberCoroutineScope()

    LaunchedEffect(uiState.isRegisterSuccess) {
        if (uiState.isRegisterSuccess) onNavigateToHome()
    }

    LaunchedEffect(uiState.registerError) {
        uiState.registerError?.let { error ->
            snackbarHostState.showSnackbar(message = error, duration = SnackbarDuration.Short)
            viewModel.onDismissError()
        }
    }

    val onGoogleSignInClick: () -> Unit = {
        coroutineScope.launch {
            try {
                val credentialManager = androidx.credentials.CredentialManager.create(context)
                
                // Cấu hình Web Client ID từ Firebase Console
                val googleIdOption = com.google.android.libraries.identity.googleid.GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(false)
                    .setServerClientId(context.getString(com.example.eduvault.R.string.default_web_client_id))
                    .setAutoSelectEnabled(false)
                    .build()

                val request = androidx.credentials.GetCredentialRequest.Builder()
                    .addCredentialOption(googleIdOption)
                    .build()

                val result = credentialManager.getCredential(context, request)
                val credential = result.credential

                try {
                    val googleIdTokenCredential = com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.createFrom(credential.data)
                    val idToken = googleIdTokenCredential.idToken
                    viewModel.loginWithGoogle(idToken)
                } catch (parseErr: Exception) {
                    android.util.Log.e("GoogleSignIn", "Lỗi parse ID Token, sử dụng fallback: ${parseErr.localizedMessage}")
                    if (credential is com.google.android.libraries.identity.googleid.GoogleIdTokenCredential) {
                        val idToken = credential.idToken
                        viewModel.loginWithGoogle(idToken)
                    } else {
                        android.util.Log.e("GoogleSignIn", "Loại credential không được hỗ trợ: ${credential.type}")
                        snackbarHostState.showSnackbar(
                            message = "Loại xác thực không được hỗ trợ trên thiết bị này.",
                            duration = SnackbarDuration.Short
                        )
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("GoogleSignIn", "Lỗi Credential Manager: ${e.localizedMessage}", e)
                
                val errorMessage = when (e) {
                    is androidx.credentials.exceptions.GetCredentialCancellationException -> {
                        "Bạn đã hủy đăng ký bằng tài khoản Google."
                    }
                    is androidx.credentials.exceptions.NoCredentialException -> {
                        "Không tìm thấy tài khoản Google phù hợp trên thiết bị."
                    }
                    else -> {
                        "Không thể kết nối Google Sign-In. Vui lòng tạo tài khoản bằng Email/Password."
                    }
                }
                snackbarHostState.showSnackbar(
                    message = errorMessage,
                    duration = SnackbarDuration.Long
                )
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = ColorInk,
    ) { paddingValues ->
        BoxWithConstraints(modifier = Modifier.padding(paddingValues)) {
            if (maxWidth < SPLIT_SCREEN_BREAKPOINT) {
                RegisterPhonePortraitLayout(
                    uiState = uiState,
                    onFullNameChange = viewModel::onFullNameChange,
                    onEmailChange = viewModel::onEmailChange,
                    onPasswordChange = viewModel::onPasswordChange,
                    onConfirmPasswordChange = viewModel::onConfirmPasswordChange,
                    onTogglePasswordVisibility = viewModel::onTogglePasswordVisibility,
                    onToggleConfirmPasswordVisibility = viewModel::onToggleConfirmPasswordVisibility,
                    onRegisterClick = viewModel::onRegisterClick,
                    onLoginClick = onNavigateToLogin,
                    onGoogleSignInClick = onGoogleSignInClick,
                )
            } else {
                RegisterTabletLandscapeLayout(
                    uiState = uiState,
                    onFullNameChange = viewModel::onFullNameChange,
                    onEmailChange = viewModel::onEmailChange,
                    onPasswordChange = viewModel::onPasswordChange,
                    onConfirmPasswordChange = viewModel::onConfirmPasswordChange,
                    onTogglePasswordVisibility = viewModel::onTogglePasswordVisibility,
                    onToggleConfirmPasswordVisibility = viewModel::onToggleConfirmPasswordVisibility,
                    onRegisterClick = viewModel::onRegisterClick,
                    onLoginClick = onNavigateToLogin,
                    onGoogleSignInClick = onGoogleSignInClick,
                )
            }
        }
    }
}

@Composable
private fun RegisterPhonePortraitLayout(
    uiState: RegisterUiState,
    onFullNameChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit,
    onTogglePasswordVisibility: () -> Unit,
    onToggleConfirmPasswordVisibility: () -> Unit,
    onRegisterClick: () -> Unit,
    onLoginClick: () -> Unit,
    onGoogleSignInClick: () -> Unit,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "register_phone_glow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.18f,
        targetValue = 0.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(3200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "register_phone_glow_alpha"
    )

    Column(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .background(ColorInk)
                .drawBehind {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                ColorAmber.copy(alpha = glowAlpha),
                                Color.Transparent
                            ),
                            center = Offset(size.width * 0.5f, size.height * 0.35f),
                            radius = size.width * 0.95f
                        ),
                        radius = size.width * 0.95f,
                        center = Offset(size.width * 0.5f, size.height * 0.35f),
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .align(Alignment.TopStart)
                    .offset(x = (-30).dp, y = (-20).dp)
                    .clip(CircleShape)
                    .border(1.dp, ColorAmberLight.copy(alpha = 0.18f), CircleShape)
            )
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .align(Alignment.TopEnd)
                    .offset(x = 15.dp, y = 20.dp)
                    .clip(CircleShape)
                    .border(1.dp, ColorAmber.copy(alpha = 0.12f), CircleShape)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 24.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
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
                        fontSize = 10.sp,
                        color = ColorAmber,
                        letterSpacing = 3.sp,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = buildAnnotatedString {
                            append("Bắt đầu hành trình ")
                            withStyle(
                                SpanStyle(
                                    fontStyle = FontStyle.Italic,
                                    color = ColorAmber,
                                )
                            ) { append("học tập") }
                        },
                        fontFamily = PlayfairDisplayFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        lineHeight = 22.sp,
                        color = ColorTextOnDark,
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                .background(ColorCream)
        ) {
            RegisterFormPanel(
                uiState = uiState,
                onFullNameChange = onFullNameChange,
                onEmailChange = onEmailChange,
                onPasswordChange = onPasswordChange,
                onConfirmPasswordChange = onConfirmPasswordChange,
                onTogglePasswordVisibility = onTogglePasswordVisibility,
                onToggleConfirmPasswordVisibility = onToggleConfirmPasswordVisibility,
                onRegisterClick = onRegisterClick,
                onLoginClick = onLoginClick,
                onGoogleSignInClick = onGoogleSignInClick,
                isCompact = true,
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// LAYOUT 2: TABLET / LANDSCAPE (≥ 600dp)
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
private fun RegisterTabletLandscapeLayout(
    uiState: RegisterUiState,
    onFullNameChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit,
    onTogglePasswordVisibility: () -> Unit,
    onToggleConfirmPasswordVisibility: () -> Unit,
    onRegisterClick: () -> Unit,
    onLoginClick: () -> Unit,
    onGoogleSignInClick: () -> Unit,
) {
    Box(modifier = Modifier.fillMaxSize()) {
        RegisterLeftBrandPanel(
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
                .clip(
                    RoundedCornerShape(
                        topStart = 28.dp,
                        bottomStart = 28.dp,
                    )
                )
                .background(ColorCream)
        ) {
            RegisterFormPanel(
                uiState = uiState,
                onFullNameChange = onFullNameChange,
                onEmailChange = onEmailChange,
                onPasswordChange = onPasswordChange,
                onConfirmPasswordChange = onConfirmPasswordChange,
                onTogglePasswordVisibility = onTogglePasswordVisibility,
                onToggleConfirmPasswordVisibility = onToggleConfirmPasswordVisibility,
                onRegisterClick = onRegisterClick,
                onLoginClick = onLoginClick,
                onGoogleSignInClick = onGoogleSignInClick,
                isCompact = false,
            )
        }
    }
}

// ─── Left Brand Panel (Tablet/Landscape) ──────────────────────────────────────

@Composable
private fun RegisterLeftBrandPanel(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "reg_glow_pulse")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.12f,
        targetValue = 0.28f,
        animationSpec = infiniteRepeatable(
            animation = tween(3200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "reg_glow_alpha"
    )

    Box(
        modifier = modifier
            .background(ColorInk)
            .drawBehind {
                // Glow chính — Amber glow
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            ColorAmber.copy(alpha = glowAlpha),
                            Color.Transparent
                        ),
                        center = Offset(size.width * 0.45f, size.height * 0.35f),
                        radius = size.width * 1.1f
                    ),
                    radius = size.width * 1.1f,
                    center = Offset(size.width * 0.45f, size.height * 0.35f),
                )
                // Forest glow phụ góc dưới phải
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            ColorForest.copy(alpha = glowAlpha * 0.4f),
                            Color.Transparent
                        ),
                        center = Offset(size.width * 0.85f, size.height * 0.80f),
                        radius = size.width * 0.7f
                    ),
                    radius = size.width * 0.7f,
                    center = Offset(size.width * 0.85f, size.height * 0.80f),
                )
            },
        contentAlignment = Alignment.Center
    ) {
        // Decorative circles
        Box(
            modifier = Modifier
                .size(110.dp)
                .offset(x = (-25).dp, y = (-130).dp)
                .clip(CircleShape)
                .border(1.dp, ColorAmberLight.copy(alpha = 0.22f), CircleShape)
        )
        Box(
            modifier = Modifier
                .size(70.dp)
                .offset(x = 35.dp, y = (-170).dp)
                .clip(CircleShape)
                .border(1.dp, ColorAmber.copy(alpha = 0.12f), CircleShape)
        )
        Box(
            modifier = Modifier
                .size(55.dp)
                .offset(x = (-35).dp, y = 150.dp)
                .clip(CircleShape)
                .border(1.dp, ColorAmber.copy(alpha = 0.18f), CircleShape)
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Center,
        ) {
            // Logo mark — Amber gradient
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(14.dp))
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
                    fontSize = 28.sp,
                    color = ColorInk,
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

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
                    append("Bắt đầu\nhành trình ")
                    withStyle(
                        SpanStyle(
                            fontStyle = FontStyle.Italic,
                            color = ColorAmber,
                            fontFamily = PlayfairDisplayFamily,
                        )
                    ) { append("học tập") }
                },
                fontFamily = PlayfairDisplayFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
                lineHeight = 30.sp,
                color = ColorTextOnDark,
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Tham gia cùng hàng nghìn sinh viên đang học tập và phát triển mỗi ngày.",
                fontFamily = DmSansFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 12.sp,
                lineHeight = 18.sp,
                color = ColorTextOnDarkSecondary,
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Feature pills
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                RegisterFeaturePill(icon = "📚", text = "500+ tài liệu học tập")
                RegisterFeaturePill(icon = "🧠", text = "Quiz thông minh với AI")
                RegisterFeaturePill(icon = "🆓", text = "Hoàn toàn miễn phí")
            }
        }
    }
}

@Composable
private fun RegisterFeaturePill(icon: String, text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(text = icon, fontSize = 13.sp)
        Text(
            text = text,
            fontFamily = DmSansFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 12.sp,
            color = ColorTextOnDarkSecondary,
        )
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// SHARED: Register Form Panel
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
private fun RegisterFormPanel(
    uiState: RegisterUiState,
    onFullNameChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit,
    onTogglePasswordVisibility: () -> Unit,
    onToggleConfirmPasswordVisibility: () -> Unit,
    onRegisterClick: () -> Unit,
    onLoginClick: () -> Unit,
    onGoogleSignInClick: () -> Unit,
    isCompact: Boolean,
    modifier: Modifier = Modifier,
) {
    val focusManager = LocalFocusManager.current

    val horizontalPadding = if (isCompact) 24.dp else 28.dp
    val verticalPadding   = if (isCompact) 22.dp else 32.dp
    val titleFontSize     = if (isCompact) 24.sp  else 28.sp
    val fieldSpacing      = if (isCompact) 12.dp  else 14.dp

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
            text = "Tạo tài",
            fontFamily = PlayfairDisplayFamily,
            fontWeight = FontWeight.Bold,
            fontSize = titleFontSize,
            color = ColorInk,
        )
        Text(
            text = buildAnnotatedString {
                withStyle(SpanStyle(fontStyle = FontStyle.Italic, color = ColorAmberDark)) {
                    append("khoản")
                }
                append(" mới")
            },
            fontFamily = PlayfairDisplayFamily,
            fontWeight = FontWeight.Bold,
            fontSize = titleFontSize,
            color = ColorInk,
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Bắt đầu hành trình học tập của bạn",
            fontFamily = DmSansFamily,
            fontWeight = FontWeight.Normal,
            fontSize = if (isCompact) 12.sp else 13.sp,
            color = ColorTextOnLightSecondary,
        )

        Spacer(modifier = Modifier.height(if (isCompact) 18.dp else 24.dp))

        // ── Họ và tên ────────────────────────────────────────────────────
        RegisterTextField(
            value = uiState.fullName,
            onValueChange = onFullNameChange,
            label = "Họ và tên",
            placeholder = "Nguyễn Văn A",
            leadingIcon = {
                Icon(
                    imageVector = Icons.Outlined.Person,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Next,
            ),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) }
            ),
            isError = uiState.hasAttemptedRegister && uiState.fullNameError != null,
            errorMessage = uiState.fullNameError,
        )

        Spacer(modifier = Modifier.height(fieldSpacing))

        // ── Email ─────────────────────────────────────────────────────────
        RegisterTextField(
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
                imeAction = ImeAction.Next,
            ),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) }
            ),
            isError = uiState.hasAttemptedRegister && uiState.emailError != null,
            errorMessage = uiState.emailError,
        )

        Spacer(modifier = Modifier.height(fieldSpacing))

        // ── Mật khẩu ─────────────────────────────────────────────────────
        RegisterTextField(
            value = uiState.password,
            onValueChange = onPasswordChange,
            label = "Mật khẩu",
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
            isError = uiState.hasAttemptedRegister && uiState.passwordError != null,
            errorMessage = uiState.passwordError,
        )

        Spacer(modifier = Modifier.height(fieldSpacing))

        // ── Xác nhận mật khẩu ────────────────────────────────────────────
        RegisterTextField(
            value = uiState.confirmPassword,
            onValueChange = onConfirmPasswordChange,
            label = "Xác nhận mật khẩu",
            placeholder = "Nhập lại mật khẩu",
            leadingIcon = {
                Icon(
                    imageVector = Icons.Outlined.Lock,
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
                    onRegisterClick()
                }
            ),
            isError = uiState.hasAttemptedRegister && uiState.confirmPasswordError != null,
            errorMessage = uiState.confirmPasswordError,
        )

        Spacer(modifier = Modifier.height(20.dp))

        // ── Nút đăng ký ───────────────────────────────────────────────────
        RegisterButton(
            isLoading = uiState.isLoading,
            onClick = {
                focusManager.clearFocus()
                onRegisterClick()
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // ── Điều khoản ────────────────────────────────────────────────────
        Text(
            text = buildAnnotatedString {
                append("Bằng cách đăng ký, bạn đồng ý với ")
                withStyle(
                    SpanStyle(
                        color = ColorAmberDark,
                        fontWeight = FontWeight.SemiBold,
                    )
                ) { append("Điều khoản sử dụng") }
            },
            fontFamily = DmSansFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 11.sp,
            color = ColorTextOnLightSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // ── Divider ───────────────────────────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            HorizontalDivider(modifier = Modifier.weight(1f), color = ColorBorder, thickness = 1.dp)
            Text(
                text = "  Hoặc  ",
                fontFamily = DmSansFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 12.sp,
                color = ColorTextOnLightSecondary,
            )
            HorizontalDivider(modifier = Modifier.weight(1f), color = ColorBorder, thickness = 1.dp)
        }

        Spacer(modifier = Modifier.height(14.dp))

        // ── Google button ─────────────────────────────────────────────────
        GoogleRegisterButton(onClick = onGoogleSignInClick)

        Spacer(modifier = Modifier.height(20.dp))

        // ── Link đăng nhập ────────────────────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Đã có tài khoản? ",
                fontFamily = DmSansFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 13.sp,
                color = ColorTextOnLightSecondary,
            )
            Text(
                text = "Đăng nhập",
                fontFamily = DmSansFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp,
                color = ColorAmberDark,
                modifier = Modifier.clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) { onLoginClick() }
            )
        }
    }
}

// ─── Reusable UI Components ───────────────────────────────────────────────────

@Composable
private fun RegisterTextField(
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
                isError   -> ColorError
                isFocused -> ColorAmber
                else      -> ColorTextOnLightSecondary
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
            visualTransformation = visualTransformation,
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
                focusedTrailingIconColor = ColorAmber,
                unfocusedTrailingIconColor = ColorTextOnLightSecondary,
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

@Composable
private fun RegisterButton(
    isLoading: Boolean,
    onClick: () -> Unit,
) {
    val buttonScale by animateFloatAsState(
        targetValue = if (isLoading) 0.97f else 1f,
        animationSpec = tween(150),
        label = "register_btn_scale"
    )

    Button(
        onClick = onClick,
        enabled = !isLoading,
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
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 2.dp,
            pressedElevation = 0.dp,
        )
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = ColorInk,
                strokeWidth = 2.dp
            )
        } else {
            Text(
                text = "Tạo tài khoản",
                fontFamily = DmSansFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp,
                letterSpacing = 0.3.sp,
            )
        }
    }
}

@Composable
private fun GoogleRegisterButton(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .clip(RoundedCornerShape(11.dp))
            .border(1.5.dp, ColorBorder, RoundedCornerShape(11.dp))
            .background(Color.White)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            Text(
                text = "G",
                fontFamily = PlayfairDisplayFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = Color(0xFF4285F4),
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "Đăng ký với Google",
                fontFamily = DmSansFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
                color = ColorTextOnLight,
            )
        }
    }
}

// ─── Previews ─────────────────────────────────────────────────────────────────

@Preview(name = "Register — Phone Portrait", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun RegisterPhonePreview() {
    EduVaultTheme(darkTheme = false) {
        RegisterPhonePortraitLayout(
            uiState = RegisterUiState(),
            onFullNameChange = {},
            onEmailChange = {},
            onPasswordChange = {},
            onConfirmPasswordChange = {},
            onTogglePasswordVisibility = {},
            onToggleConfirmPasswordVisibility = {},
            onRegisterClick = {},
            onLoginClick = {},
            onGoogleSignInClick = {},
        )
    }
}

@Preview(name = "Register — Tablet Landscape", showBackground = true, widthDp = 840, heightDp = 600)
@Composable
private fun RegisterTabletPreview() {
    EduVaultTheme(darkTheme = false) {
        RegisterTabletLandscapeLayout(
            uiState = RegisterUiState(),
            onFullNameChange = {},
            onEmailChange = {},
            onPasswordChange = {},
            onConfirmPasswordChange = {},
            onTogglePasswordVisibility = {},
            onToggleConfirmPasswordVisibility = {},
            onRegisterClick = {},
            onLoginClick = {},
            onGoogleSignInClick = {},
        )
    }
}

@Preview(name = "Register — Error State", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun RegisterErrorPreview() {
    EduVaultTheme(darkTheme = false) {
        RegisterPhonePortraitLayout(
            uiState = RegisterUiState(
                fullName = "A",
                email = "invalid",
                password = "123",
                confirmPassword = "456",
                fullNameError = "Họ và tên phải có ít nhất 2 ký tự",
                emailError = "Định dạng email không hợp lệ",
                passwordError = "Mật khẩu phải có ít nhất 8 ký tự",
                confirmPasswordError = "Mật khẩu xác nhận không khớp",
                hasAttemptedRegister = true,
            ),
            onFullNameChange = {},
            onEmailChange = {},
            onPasswordChange = {},
            onConfirmPasswordChange = {},
            onTogglePasswordVisibility = {},
            onToggleConfirmPasswordVisibility = {},
            onRegisterClick = {},
            onLoginClick = {},
            onGoogleSignInClick = {},
        )
    }
}
