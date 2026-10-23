package com.example.eduvault.feature.profile.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Login
import androidx.compose.material.icons.outlined.PersonAdd
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.eduvault.core.theme.ColorAmber
import com.example.eduvault.core.theme.ColorAmberDark
import com.example.eduvault.core.theme.ColorBorder
import com.example.eduvault.core.theme.ColorCream
import com.example.eduvault.core.theme.ColorInk
import com.example.eduvault.core.theme.ColorPaper
import com.example.eduvault.core.theme.ColorTextOnDark
import com.example.eduvault.core.theme.ColorTextOnDarkSecondary
import com.example.eduvault.core.theme.ColorTextOnLight
import com.example.eduvault.core.theme.DmSansFamily
import com.example.eduvault.core.theme.PlayfairDisplayFamily

@Composable
fun GuestProfileScreen(
    paddingValues: PaddingValues,
    onNavigateToLogin: () -> Unit,
    onNavigateToRegister: () -> Unit
) {
    val features = listOf(
        "📚" to "Lưu tiến độ đào tạo",
        "🤝" to "Đóng góp học liệu cho chương trình",
        "🤖" to "Làm Quiz AI và ôn tập Flashcard",
        "🗂️" to "Quản lý học liệu cá nhân",
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
                text = "Đăng nhập để tham gia đầy đủ\nchương trình đào tạo thông minh",
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
                text = "📈 Lưu tiến độ và kết quả luyện tập của bạn",
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
