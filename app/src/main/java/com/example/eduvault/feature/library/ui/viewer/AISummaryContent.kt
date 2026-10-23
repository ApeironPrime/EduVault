package com.example.eduvault.feature.library.ui.viewer

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
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
import com.example.eduvault.core.theme.ColorTextOnLightSecondary
import com.example.eduvault.core.theme.DmSansFamily
import com.example.eduvault.core.theme.PlayfairDisplayFamily

@Composable
fun AISummaryContent(
    summary: String?,
    isSummarizing: Boolean,
    onGenerateClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(ColorCream)
            .border(1.dp, ColorBorder, RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("✨", fontSize = 18.sp)
            Text(
                text = "Bản tóm tắt thông minh (Gemini AI)",
                fontFamily = PlayfairDisplayFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = ColorAmberDark
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (isSummarizing) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                CircularProgressIndicator(
                    color = ColorAmber,
                    modifier = Modifier.size(32.dp)
                )
                Text(
                    text = "Gemini AI đang phân tích và tóm tắt nội dung...",
                    fontFamily = DmSansFamily,
                    fontSize = 12.sp,
                    color = ColorTextOnLightSecondary,
                    textAlign = TextAlign.Center
                )
            }
        } else if (summary != null) {
            Text(
                text = parseMarkdownToAnnotatedString(summary, 13f),
                fontFamily = DmSansFamily,
                fontSize = 13.sp,
                color = ColorInk.copy(alpha = 0.85f),
                lineHeight = 20.sp
            )
        } else {
            Text(
                text = "Nhấp vào nút bên dưới để yêu cầu Gemini AI đọc hiểu tài liệu và biên soạn một bản tóm tắt súc tích hoàn toàn miễn phí.",
                fontFamily = DmSansFamily,
                fontSize = 12.sp,
                color = ColorTextOnLightSecondary,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(14.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(ColorAmber, Color(0xFFFF8F00))
                        )
                    )
                    .clickable { onGenerateClick() }
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "✨ Tạo tóm tắt AI miễn phí",
                    fontFamily = DmSansFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = Color.White
                )
            }
        }
    }
}
