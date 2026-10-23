package com.example.eduvault.feature.home.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.eduvault.core.theme.ColorInk
import com.example.eduvault.core.theme.ColorTextOnLightSecondary
import com.example.eduvault.core.theme.DmSansFamily

@Composable
fun TrainingAvailabilityCard(isLoading: Boolean, message: String?, onRetry: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth().trainingCardShadow(RoundedCornerShape(18.dp)),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = if (isLoading) "Đang tải lộ trình" else "Lộ trình chưa sẵn sàng",
                fontFamily = DmSansFamily, fontWeight = FontWeight.SemiBold,
                fontSize = 18.sp, color = ColorInk,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = if (isLoading) "Đang lấy chương trình và tiến độ đào tạo của bạn."
                    else message ?: "Chưa có dữ liệu chương trình đào tạo. Vui lòng tải lại.",
                fontFamily = DmSansFamily, fontSize = 13.sp,
                lineHeight = 20.sp, color = ColorTextOnLightSecondary,
            )
            Spacer(Modifier.height(16.dp))
            if (isLoading) {
                CircularProgressIndicator(Modifier.size(24.dp), color = ColorInk, strokeWidth = 2.dp)
            } else {
                Button(onClick = onRetry, colors = ButtonDefaults.buttonColors(containerColor = ColorInk)) {
                    Text("Tải lại lộ trình")
                }
            }
        }
    }
}
