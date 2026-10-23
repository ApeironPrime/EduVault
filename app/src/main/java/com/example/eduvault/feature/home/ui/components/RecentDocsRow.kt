package com.example.eduvault.feature.home.ui.components

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Bookmarks
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.eduvault.core.theme.ColorBorder
import com.example.eduvault.core.theme.ColorInk
import com.example.eduvault.core.theme.ColorTextOnLight
import com.example.eduvault.core.theme.ColorTextOnLightSecondary
import com.example.eduvault.core.theme.DmSansFamily
import com.example.eduvault.core.theme.JetBrainsMonoFamily
import com.example.eduvault.feature.home.ui.DocType
import com.example.eduvault.feature.home.ui.RecentDoc

@Composable
fun RecentDocsRow(
    docs: List<RecentDoc>,
    onOpenDoc: (RecentDoc) -> Unit
) {
    if (docs.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(Color(0xFFF8FAFC))
                .border(1.dp, ColorBorder, RoundedCornerShape(14.dp))
                .padding(vertical = 28.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(DocType.NOTE.materialIcon, contentDescription = null,
                    tint = Color(0xFF64748B), modifier = Modifier.size(28.dp))
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Chưa có học liệu gần đây",
                    fontFamily = DmSansFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    color = ColorInk
                )
                Text(
                    text = "Mở thư viện để bắt đầu nội dung trong chương trình.",
                    fontFamily = DmSansFamily,
                    fontSize = 11.sp,
                    color = ColorTextOnLightSecondary
                )
            }
        }
        return
    }

    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(11.dp),
    ) {
        items(docs) { doc ->
            DocCard(doc = doc, onClick = { onOpenDoc(doc) })
        }
    }
}

@Composable
fun DocCard(
    doc: RecentDoc,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(170.dp)
            .trainingCardShadow(RoundedCornerShape(13.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(13.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column {
            // Thumb
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(88.dp)
                    .background(Color(0xFFF1F5F9)),
                contentAlignment = Alignment.Center
            ) {
                Icon(doc.type.materialIcon, contentDescription = null,
                    tint = Color(0xFF334155), modifier = Modifier.size(30.dp))
                // Badge
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp)
                        .clip(RoundedCornerShape(5.dp))
                        .background(Color.White.copy(alpha = 0.85f))
                        .padding(horizontal = 7.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = doc.type.label,
                        fontFamily = DmSansFamily,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF475569),
                        letterSpacing = 0.sp,
                    )
                }
                // Pages
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(7.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color.White.copy(alpha = 0.85f))
                        .padding(horizontal = 5.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = doc.quantityLabel.replace(Regex("\\btr\\b"), "trang"),
                        fontFamily = JetBrainsMonoFamily,
                        fontSize = 9.sp,
                        color = ColorTextOnLightSecondary,
                    )
                }
            }

            // Info
            Column(modifier = Modifier.padding(horizontal = 11.dp, vertical = 10.dp)) {
                Text(
                    text = doc.course.uppercase(),
                    fontFamily = DmSansFamily,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF64748B),
                    style = TextStyle(fontFeatureSettings = "tnum"),
                    letterSpacing = 0.08.em,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Text(
                    text = doc.title,
                    fontFamily = DmSansFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp,
                    color = ColorTextOnLight,
                    lineHeight = 16.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    val format = doc.fileName.substringAfterLast('.', "")
                        .uppercase().takeIf { it in setOf("PDF", "DOC", "DOCX", "PPT", "PPTX", "MP4", "PNG", "JPG", "JPEG") }
                    Text(
                        text = format?.let { "Định dạng: $it" } ?: "Loại: ${doc.type.label}",
                        fontFamily = DmSansFamily,
                        fontSize = 10.sp,
                        color = ColorTextOnLightSecondary,
                        modifier = Modifier.weight(1f),
                    )
                    val context = LocalContext.current
                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .clip(RoundedCornerShape(7.dp))
                            .background(Color(0xFFF1F5F9))
                            .clickable {
                                Toast.makeText(
                                    context,
                                    "Đã lưu \"${doc.title}\" vào mục yêu thích! 🔖",
                                    Toast.LENGTH_SHORT
                                ).show()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Bookmarks,
                            contentDescription = "Lưu",
                            tint = ColorTextOnLightSecondary,
                            modifier = Modifier.size(13.dp)
                        )
                    }
                }
            }
        }
    }
}
