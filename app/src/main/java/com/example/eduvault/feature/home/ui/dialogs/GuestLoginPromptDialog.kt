package com.example.eduvault.feature.home.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.eduvault.core.theme.ColorAmber
import com.example.eduvault.core.theme.ColorAmberDark
import com.example.eduvault.core.theme.ColorBorder
import com.example.eduvault.core.theme.ColorInk
import com.example.eduvault.core.theme.ColorPaper
import com.example.eduvault.core.theme.ColorTextOnLightSecondary
import com.example.eduvault.core.theme.DmSansFamily
import com.example.eduvault.core.theme.PlayfairDisplayFamily

@Composable
fun GuestLoginPromptDialog(
    onDismiss: () -> Unit,
    onNavigateToLogin: () -> Unit,
    onNavigateToRegister: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.88f)
                .clip(RoundedCornerShape(20.dp))
                .background(ColorPaper)
                .border(1.dp, ColorBorder, RoundedCornerShape(20.dp))
                .padding(24.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(text = "🔐", fontSize = 36.sp)

                Text(
                    text = "Tính năng dành cho thành viên",
                    fontFamily = PlayfairDisplayFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = ColorInk,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "Đăng nhập để truy cập học liệu đào tạo,\nđóng góp tài liệu và lưu tiến độ của bạn.",
                    fontFamily = DmSansFamily,
                    fontSize = 13.sp,
                    color = ColorTextOnLightSecondary,
                    textAlign = TextAlign.Center,
                    lineHeight = 19.sp
                )

                Spacer(Modifier.height(4.dp))

                // Nút đăng nhập
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            Brush.horizontalGradient(listOf(ColorAmber, ColorAmberDark))
                        )
                        .clickable {
                            onDismiss()
                            onNavigateToLogin()
                        }
                        .padding(vertical = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Đăng nhập",
                        fontFamily = DmSansFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = ColorInk
                    )
                }

                // Nút tạo tài khoản
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.5.dp, ColorInk.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                        .clickable {
                            onDismiss()
                            onNavigateToRegister()
                        }
                        .padding(vertical = 13.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Tạo tài khoản miễn phí",
                        fontFamily = DmSansFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp,
                        color = ColorInk
                    )
                }

                TextButton(onClick = onDismiss) {
                    Text(
                        text = "Để sau",
                        fontFamily = DmSansFamily,
                        fontSize = 13.sp,
                        color = ColorTextOnLightSecondary
                    )
                }
            }
        }
    }
}
