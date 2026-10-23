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
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
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
// < 600dp → Phone Portrait  (layout dọc: header nhỏ + full-width form)
// ≥ 600dp → Tablet/Landscape (layout ngang: split-screen)
private val SPLIT_SCREEN_BREAKPOINT = 600.dp

// ─── Entry Point ──────────────────────────────────────────────────────────────

@Composable
fun LoginScreen(
    onNavigateToHome: () -> Unit = {},
    onNavigateToRegister: () -> Unit = {},
    onNavigateToForgotPassword: () -> Unit = {},
    viewModel: LoginViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = androidx.compose.ui.platform.LocalContext.current
    val coroutineScope = androidx.compose.runtime.rememberCoroutineScope()
    val webClientId = androidx.compose.ui.res.stringResource(com.example.eduvault.R.string.default_web_client_id)

    // Điều hướng khi login thành công
    LaunchedEffect(uiState.isLoginSuccess) {
        if (uiState.isLoginSuccess) onNavigateToHome()
    }

    // Hiện snackbar khi có lỗi
    LaunchedEffect(uiState.loginError) {
        uiState.loginError?.let { error ->
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
                    .setServerClientId(webClientId)
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
                        "Bạn đã hủy đăng nhập bằng tài khoản Google."
                    }
                    is androidx.credentials.exceptions.NoCredentialException -> {
                        "Không tìm thấy tài khoản Google phù hợp trên thiết bị."
                    }
                    else -> {
                        "Không thể kết nối Google Sign-In. Vui lòng nhập Email/Password."
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
        // BoxWithConstraints đọc kích thước thực tế của màn hình
        BoxWithConstraints(modifier = Modifier.padding(paddingValues)) {
            if (maxWidth < SPLIT_SCREEN_BREAKPOINT) {
                // ── Phone Portrait: Layout dọc ────────────────────────────
                PhonePortraitLayout(
                    uiState = uiState,
                    onEmailChange = viewModel::onEmailChange,
                    onPasswordChange = viewModel::onPasswordChange,
                    onTogglePasswordVisibility = viewModel::onTogglePasswordVisibility,
                    onLoginClick = viewModel::onLoginClick,
                    onForgotPasswordClick = onNavigateToForgotPassword,
                    onRegisterClick = onNavigateToRegister,
                    onGoogleSignInClick = onGoogleSignInClick,
                )
            } else {
                // ── Tablet / Landscape: Split-screen ──────────────────────
                TabletLandscapeLayout(
                    uiState = uiState,
                    onEmailChange = viewModel::onEmailChange,
                    onPasswordChange = viewModel::onPasswordChange,
                    onTogglePasswordVisibility = viewModel::onTogglePasswordVisibility,
                    onLoginClick = viewModel::onLoginClick,
                    onForgotPasswordClick = onNavigateToForgotPassword,
                    onRegisterClick = onNavigateToRegister,
                    onGoogleSignInClick = onGoogleSignInClick,
                )
            }
        }
    }
}


// ═══════════════════════════════════════════════════════════════════════════════
// LAYOUT 1: PHONE PORTRAIT — Dọc (< 600dp)
// Header tối nhỏ gọn ở trên, form cream đầy đủ ở dưới
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
private fun PhonePortraitLayout(
    uiState: LoginUiState,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onTogglePasswordVisibility: () -> Unit,
    onLoginClick: () -> Unit,
    onForgotPasswordClick: () -> Unit,
    onRegisterClick: () -> Unit,
    onGoogleSignInClick: () -> Unit,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "phone_glow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.18f,
        targetValue = 0.36f,
        animationSpec = infiniteRepeatable(
            animation = tween(2800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "phone_glow_alpha"
    )

    Column(modifier = Modifier.fillMaxSize()) {

        // ── Compact Brand Header ───────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 200.dp)
                .background(ColorInk)
                .drawBehind {
                    // Glow ở trung tâm header
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                ColorAmber.copy(alpha = glowAlpha),
                                Color.Transparent
                            ),
                            center = Offset(size.width * 0.5f, size.height * 0.4f),
                            radius = size.width * 0.7f
                        ),
                        radius = size.width * 0.7f,
                        center = Offset(size.width * 0.5f, size.height * 0.4f),
                    )
                    // Forest glow góc phải dưới
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                ColorForest.copy(alpha = 0.15f),
                                Color.Transparent
                            ),
                            center = Offset(size.width * 0.85f, size.height * 0.9f),
                            radius = size.width * 0.5f
                        ),
                        radius = size.width * 0.5f,
                        center = Offset(size.width * 0.85f, size.height * 0.9f),
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            // Decorative circle nhỏ góc trên trái
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .align(Alignment.TopStart)
                    .offset(x = (-20).dp, y = (-20).dp)
                    .clip(CircleShape)
                    .border(1.dp, ColorAmber.copy(alpha = 0.15f), CircleShape)
            )
            // Decorative circle nhỏ góc phải
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .align(Alignment.TopEnd)
                    .offset(x = 10.dp, y = 30.dp)
                    .clip(CircleShape)
                    .border(1.dp, ColorAmber.copy(alpha = 0.10f), CircleShape)
            )

            // Brand content — layout ngang (logo | text)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 28.dp, vertical = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Logo mark
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(13.dp))
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(ColorAmber, ColorAmberDark)
                            )
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    EduVaultMark(modifier = Modifier.size(30.dp))
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
                        text = "Không gian đào tạo nhân sự mới",
                        fontFamily = PlayfairDisplayFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 17.sp,
                        lineHeight = 24.sp,
                        color = ColorTextOnDark,
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        PhoneBrandChip(label = "Học liệu nội bộ")
                        PhoneBrandChip(label = "Tự kiểm tra")
                    }
                }
            }
        }

        // ── Form Panel full-width (chiếm phần còn lại) ────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                .background(ColorCream)
        ) {
            LoginFormPanel(
                uiState = uiState,
                onEmailChange = onEmailChange,
                onPasswordChange = onPasswordChange,
                onTogglePasswordVisibility = onTogglePasswordVisibility,
                onLoginClick = onLoginClick,
                onForgotPasswordClick = onForgotPasswordClick,
                onRegisterClick = onRegisterClick,
                onGoogleSignInClick = onGoogleSignInClick,
                isCompact = true,
            )
        }
    }
}

@Composable
private fun EduVaultMark(modifier: Modifier = Modifier) {
    // An open book inside a vault: a geometric mark that stays crisp at any density.
    val vaultOutline = remember {
        Path().apply {
            moveTo(18f, 2f)
            lineTo(32f, 10f)
            lineTo(32f, 26f)
            lineTo(18f, 34f)
            lineTo(4f, 26f)
            lineTo(4f, 10f)
            close()
        }
    }
    val bookOutline = remember {
        Path().apply {
            moveTo(10f, 12f)
            lineTo(18f, 14f)
            lineTo(26f, 12f)
            lineTo(26f, 23f)
            lineTo(18f, 25f)
            lineTo(10f, 23f)
            close()
        }
    }
    Canvas(modifier = modifier) {
        scale(scaleX = size.width / 36f, scaleY = size.height / 36f, pivot = Offset.Zero) {
            drawPath(vaultOutline, ColorInk, style = Stroke(width = 1.8f))
            drawPath(bookOutline, ColorInk, style = Stroke(width = 1.8f))
            drawLine(ColorInk, Offset(18f, 14f), Offset(18f, 25f), strokeWidth = 1.8f, cap = StrokeCap.Round)
        }
    }
}

@Composable
private fun PhoneBrandChip(label: String) {
    Box(
        modifier = Modifier
            .background(Color.White.copy(alpha = 0.06f), RoundedCornerShape(50))
            .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(50))
            .padding(horizontal = 10.dp, vertical = 5.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            fontFamily = DmSansFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 11.sp,
            color = ColorTextOnDark,
        )
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// LAYOUT 2: TABLET / LANDSCAPE — Split-screen (≥ 600dp)
// Panel trái 42%, Form phải 58%
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
private fun TabletLandscapeLayout(
    uiState: LoginUiState,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onTogglePasswordVisibility: () -> Unit,
    onLoginClick: () -> Unit,
    onForgotPasswordClick: () -> Unit,
    onRegisterClick: () -> Unit,
    onGoogleSignInClick: () -> Unit,
) {
    Box(modifier = Modifier.fillMaxSize()) {
        // Left Panel
        LeftBrandPanel(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(0.42f)
                .align(Alignment.CenterStart)
        )

        // Right Panel
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(0.58f)
                .align(Alignment.CenterEnd)
                .clip(
                    RoundedCornerShape(
                        topStart = 28.dp,
                        bottomStart = 28.dp,
                        topEnd = 0.dp,
                        bottomEnd = 0.dp
                    )
                )
                .background(ColorCream)
        ) {
            LoginFormPanel(
                uiState = uiState,
                onEmailChange = onEmailChange,
                onPasswordChange = onPasswordChange,
                onTogglePasswordVisibility = onTogglePasswordVisibility,
                onLoginClick = onLoginClick,
                onForgotPasswordClick = onForgotPasswordClick,
                onRegisterClick = onRegisterClick,
                onGoogleSignInClick = onGoogleSignInClick,
                isCompact = false,
            )
        }
    }
}

// ─── Left Brand Panel (Tablet/Landscape only) ─────────────────────────────────

@Composable
private fun LeftBrandPanel(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "glow_pulse")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.15f,
        targetValue = 0.30f,
        animationSpec = infiniteRepeatable(
            animation = tween(2800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_alpha"
    )

    Box(
        modifier = modifier
            .background(ColorInk)
            .drawBehind {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            ColorAmber.copy(alpha = glowAlpha),
                            Color.Transparent
                        ),
                        center = Offset(size.width * 0.55f, size.height * 0.28f),
                        radius = size.width * 1.1f
                    ),
                    radius = size.width * 1.1f,
                    center = Offset(size.width * 0.55f, size.height * 0.28f),
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            ColorForest.copy(alpha = 0.12f),
                            Color.Transparent
                        ),
                        center = Offset(size.width * 0.2f, size.height * 0.85f),
                        radius = size.width * 0.8f
                    ),
                    radius = size.width * 0.8f,
                    center = Offset(size.width * 0.2f, size.height * 0.85f),
                )
            },
        contentAlignment = Alignment.Center
    ) {
        // Decorative circles
        Box(
            modifier = Modifier
                .size(120.dp)
                .offset(x = (-20).dp, y = (-120).dp)
                .clip(CircleShape)
                .border(1.dp, ColorAmber.copy(alpha = 0.18f), CircleShape)
        )
        Box(
            modifier = Modifier
                .size(80.dp)
                .offset(x = 30.dp, y = (-160).dp)
                .clip(CircleShape)
                .border(1.dp, ColorAmber.copy(alpha = 0.10f), CircleShape)
        )
        Box(
            modifier = Modifier
                .size(60.dp)
                .offset(x = (-40).dp, y = 140.dp)
                .clip(CircleShape)
                .border(1.dp, ColorForest.copy(alpha = 0.22f), CircleShape)
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Center,
        ) {
            // Logo mark
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
                EduVaultMark(modifier = Modifier.size(32.dp))
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
                text = "Không gian đào tạo nhân sự mới",
                fontFamily = PlayfairDisplayFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 22.sp,
                lineHeight = 30.sp,
                color = ColorTextOnDark,
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Thư viện học liệu thông minh giúp bạn ôn tập và nắm vững từng khái niệm.",
                fontFamily = DmSansFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 12.sp,
                lineHeight = 18.sp,
                color = ColorTextOnDarkSecondary,
            )

            Spacer(modifier = Modifier.height(28.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                BrandStat(number = "NỘI BỘ", label = "Học liệu")
                BrandStat(number = "Quiz", label = "Tự kiểm tra")
            }
        }
    }
}

@Composable
private fun BrandStat(number: String, label: String) {
    Column {
        Text(
            text = number,
            fontFamily = JetBrainsMonoFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 16.sp,
            color = ColorAmber,
        )
        Text(
            text = label,
            fontFamily = DmSansFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 10.sp,
            color = ColorTextOnDarkSecondary,
        )
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// SHARED: Form Panel — dùng cho cả 2 layout, isCompact điều chỉnh padding/size
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
private fun LoginFormPanel(
    uiState: LoginUiState,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onTogglePasswordVisibility: () -> Unit,
    onLoginClick: () -> Unit,
    onForgotPasswordClick: () -> Unit,
    onRegisterClick: () -> Unit,
    onGoogleSignInClick: () -> Unit,
    isCompact: Boolean,                    // true = phone, false = tablet
    modifier: Modifier = Modifier,
) {
    val focusManager = LocalFocusManager.current

    // Padding linh hoạt theo kích thước màn hình
    val horizontalPadding = if (isCompact) 24.dp else 28.dp
    val verticalPadding   = if (isCompact) 28.dp else 40.dp
    val titleFontSize     = if (isCompact) 24.sp  else 28.sp
    val subtitleFontSize  = if (isCompact) 12.sp  else 13.sp
    val sectionSpacing    = if (isCompact) 20.dp  else 28.dp

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
            text = "Chào mừng trở lại",
            fontFamily = PlayfairDisplayFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = titleFontSize,
            color = ColorInk,
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Đăng nhập để tiếp tục chương trình đào tạo",
            fontFamily = DmSansFamily,
            fontWeight = FontWeight.Normal,
            fontSize = subtitleFontSize,
            color = ColorTextOnLightSecondary,
        )

        Spacer(modifier = Modifier.height(sectionSpacing))

        // ── Email Field ───────────────────────────────────────────────────
        LoginTextField(
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
                imeAction = ImeAction.Next
            ),
            // Lỗi chỉ hiện sau khi người dùng đã bấm đăng nhập ít nhất 1 lần
            isError = uiState.hasAttemptedLogin && uiState.emailError != null,
            errorMessage = uiState.emailError,
        )

        Spacer(modifier = Modifier.height(14.dp))

        // ── Password Field ────────────────────────────────────────────────
        LoginTextField(
            value = uiState.password,
            onValueChange = onPasswordChange,
            label = "Mật khẩu",
            placeholder = "Nhập mật khẩu",
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
                        contentDescription = if (uiState.isPasswordVisible) "Ẩn" else "Hiện",
                        modifier = Modifier.size(18.dp)
                    )
                }
            },
            visualTransformation = if (uiState.isPasswordVisible)
                VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    focusManager.clearFocus()
                    onLoginClick()
                }
            ),
            // Lỗi chỉ hiện sau khi người dùng đã bấm đăng nhập ít nhất 1 lần
            isError = uiState.hasAttemptedLogin && uiState.passwordError != null,
            errorMessage = uiState.passwordError,
        )

        // ── Quên mật khẩu ────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 6.dp),
            horizontalArrangement = Arrangement.End
        ) {
            Text(
                text = "Quên mật khẩu?",
                fontFamily = DmSansFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 12.sp,
                color = ColorAmberDark,
                modifier = Modifier.clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) { onForgotPasswordClick() }
            )
        }

        Spacer(modifier = Modifier.height(22.dp))

        // ── Nút đăng nhập ────────────────────────────────────────────────
        LoginButton(
            isLoading = uiState.isLoading,
            onClick = {
                focusManager.clearFocus()
                onLoginClick()
            }
        )

        Spacer(modifier = Modifier.height(20.dp))

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

        Spacer(modifier = Modifier.height(16.dp))

        // ── Google button ─────────────────────────────────────────────────
        GoogleSignInButton(onClick = onGoogleSignInClick)

        Spacer(modifier = Modifier.height(24.dp))

        // ── Link đăng ký ──────────────────────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Chưa có tài khoản? ",
                fontFamily = DmSansFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 13.sp,
                color = ColorTextOnLightSecondary,
            )
            Text(
                text = "Đăng ký ngay",
                fontFamily = DmSansFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp,
                color = ColorAmberDark,
                modifier = Modifier.clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) { onRegisterClick() }
            )
        }
    }
}

// ─── Reusable UI Components ───────────────────────────────────────────────────

@Composable
private fun LoginTextField(
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
                isError  -> ColorError
                isFocused -> ColorAmberDark
                else     -> ColorTextOnLightSecondary
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
                    color = ColorTextOnLightSecondary,
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
                focusedTrailingIconColor = ColorAmberDark,
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
private fun LoginButton(
    isLoading: Boolean,
    onClick: () -> Unit,
) {
    val buttonScale by animateFloatAsState(
        targetValue = if (isLoading) 0.97f else 1f,
        animationSpec = tween(150),
        label = "btn_scale"
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
                text = "Đăng nhập",
                fontFamily = DmSansFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp,
                letterSpacing = 0.3.sp,
            )
        }
    }
}

@Composable
private fun GoogleSignInButton(onClick: () -> Unit) {
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
                text = "Tiếp tục với Google",
                fontFamily = DmSansFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
                color = ColorTextOnLight,
            )
        }
    }
}

// ─── Previews ─────────────────────────────────────────────────────────────────

/** Preview Phone Portrait (360 × 800dp) */
@Preview(name = "Phone Portrait", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun LoginPhonePreview() {
    EduVaultTheme(darkTheme = false) {
        PhonePortraitLayout(
            uiState = LoginUiState(),
            onEmailChange = {},
            onPasswordChange = {},
            onTogglePasswordVisibility = {},
            onLoginClick = {},
            onForgotPasswordClick = {},
            onRegisterClick = {},
            onGoogleSignInClick = {},
        )
    }
}

/** Preview Tablet Landscape (840 × 600dp) */
@Preview(name = "Tablet Landscape", showBackground = true, widthDp = 840, heightDp = 600)
@Composable
private fun LoginTabletPreview() {
    EduVaultTheme(darkTheme = false) {
        TabletLandscapeLayout(
            uiState = LoginUiState(),
            onEmailChange = {},
            onPasswordChange = {},
            onTogglePasswordVisibility = {},
            onLoginClick = {},
            onForgotPasswordClick = {},
            onRegisterClick = {},
            onGoogleSignInClick = {},
        )
    }
}

/** Preview Error State (phone) — mô phỏng sau khi đã bấm đăng nhập */
@Preview(name = "Phone — Error State", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun LoginErrorPreview() {
    EduVaultTheme(darkTheme = false) {
        PhonePortraitLayout(
            uiState = LoginUiState(
                email = "invalid-email",
                emailError = "Định dạng email không hợp lệ",
                passwordError = "Mật khẩu phải có ít nhất 8 ký tự",
                hasAttemptedLogin = true,  // Simulate sau khi đã bấm nút
            ),
            onEmailChange = {},
            onPasswordChange = {},
            onTogglePasswordVisibility = {},
            onLoginClick = {},
            onForgotPasswordClick = {},
            onRegisterClick = {},
            onGoogleSignInClick = {},
        )
    }
}
