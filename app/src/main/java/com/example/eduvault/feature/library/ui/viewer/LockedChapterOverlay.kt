package com.example.eduvault.feature.library.ui.viewer

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.eduvault.core.theme.ColorAmber
import com.example.eduvault.core.theme.ColorForest
import com.example.eduvault.core.theme.ColorInk
import com.example.eduvault.core.theme.DmSansFamily
import com.example.eduvault.domain.model.DocReaderState

@Composable
fun LockedChapterOverlay(
    readerState: DocReaderState,
    onNavigateToLogin: () -> Unit = {},
    onNavigateToRegister: () -> Unit = {},
    onUnlockWithCredit: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFF5F0E0).copy(alpha = 0.7f))
            .border(1.dp, ColorAmber.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("🔒", fontSize = 32.sp)

        Text(
            text = when (readerState) {
                is DocReaderState.GuestLocked -> "Bạn cần đăng nhập để truy cập nội dung đào tạo"
                is DocReaderState.NoCredits -> "Tài khoản chưa được cấp quyền cho nội dung này"
                is DocReaderState.HasCredits -> "Xác nhận tham gia để truy cập toàn bộ nội dung"
                else -> ""
            },
            fontFamily = DmSansFamily,
            fontSize = 13.sp,
            color = ColorInk.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )

        when (readerState) {
            is DocReaderState.GuestLocked -> {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(ColorForest)
                            .clickable { onNavigateToLogin() }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Đăng nhập", fontFamily = DmSansFamily, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.White)
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .border(1.dp, ColorForest, RoundedCornerShape(10.dp))
                            .clickable { onNavigateToRegister() }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Đăng ký", fontFamily = DmSansFamily, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = ColorForest)
                    }
                }
            }
            is DocReaderState.HasCredits -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(ColorForest)
                        .clickable { onUnlockWithCredit() }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Bắt đầu học",
                        fontFamily = DmSansFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = Color.White
                    )
                }
            }
            is DocReaderState.NoCredits -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(ColorAmber)
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Liên hệ bộ phận đào tạo để được cấp quyền",
                        fontFamily = DmSansFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = ColorInk
                    )
                }
            }
            else -> { /* Unlocked */ }
        }
    }
}
