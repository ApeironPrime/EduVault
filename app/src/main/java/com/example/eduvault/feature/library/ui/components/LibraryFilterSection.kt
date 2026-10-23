package com.example.eduvault.feature.library.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.eduvault.core.theme.ColorBorder
import com.example.eduvault.core.theme.ColorCream
import com.example.eduvault.core.theme.ColorForest
import com.example.eduvault.core.theme.ColorInk
import com.example.eduvault.core.theme.ColorAmber
import com.example.eduvault.core.theme.ColorTextOnDarkSecondary
import com.example.eduvault.core.theme.ColorTextOnLight
import com.example.eduvault.core.theme.ColorTextOnLightSecondary
import com.example.eduvault.core.theme.DmSansFamily
import com.example.eduvault.core.theme.JetBrainsMonoFamily
import com.example.eduvault.feature.library.ui.DocTypeFilter
import com.example.eduvault.feature.library.ui.SortType

@Composable
fun CourseChipsRow(
    selectedSubject: String,
    onSubjectSelected: (String) -> Unit
) {
    val subjects = listOf("Tất cả", "Git", "Lập trình", "Kiểm thử", "Agile/Scrum", "DevOps", "Bảo mật")
    val colors = listOf(ColorForest, ColorAmber, Color(0xFFC0392B), ColorInk, Color(0xFF8E44AD), Color(0xFF2980B9))

    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        items(subjects) { subject ->
            val isSelected = selectedSubject == subject
            val color = if (subject == "Tất cả") ColorForest else {
                val index = (subject.hashCode() % colors.size).let { if (it < 0) it + colors.size else it }
                colors[index]
            }

            Row(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(if (isSelected) color else Color.White)
                    .border(1.dp, if (isSelected) color else ColorBorder, CircleShape)
                    .clickable { onSubjectSelected(subject) }
                    .padding(horizontal = 14.dp, vertical = 7.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .background(if (isSelected) Color.White else color, CircleShape)
                )

                Spacer(modifier = Modifier.width(6.dp))

                Text(
                    text = subject,
                    fontFamily = DmSansFamily,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (isSelected) Color.White else ColorTextOnLight
                )
            }
        }
    }
}

@Composable
fun DocTypeTabsRow(
    selectedDocType: DocTypeFilter,
    onDocTypeSelected: (DocTypeFilter) -> Unit,
    counts: Map<DocTypeFilter, Int>
) {
    val tabs = DocTypeFilter.values()

    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        items(tabs) { tab ->
            val isSelected = selectedDocType == tab
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isSelected) ColorInk else Color.White)
                    .border(1.dp, if (isSelected) ColorInk else ColorBorder, RoundedCornerShape(8.dp))
                    .clickable { onDocTypeSelected(tab) }
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val icon = when (tab) {
                    DocTypeFilter.ALL -> "📁"
                    DocTypeFilter.NOTE -> "📝"
                    DocTypeFilter.QUIZ -> "🧩"
                    DocTypeFilter.SLIDE -> "📊"
                    DocTypeFilter.SUMMARY -> "📌"
                    DocTypeFilter.FLASHCARD -> "🃏"
                }

                Text(text = icon, fontSize = 12.sp)
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = tab.label,
                    fontFamily = DmSansFamily,
                    fontSize = 12.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isSelected) Color.White else ColorTextOnLight
                )

                Spacer(modifier = Modifier.width(5.dp))

                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(if (isSelected) Color.White.copy(alpha = 0.15f) else ColorCream)
                        .padding(horizontal = 6.dp, vertical = 1.dp)
                ) {
                    Text(
                        text = "${counts[tab] ?: 0}",
                        fontFamily = JetBrainsMonoFamily,
                        fontSize = 9.sp,
                        color = if (isSelected) ColorTextOnDarkSecondary else ColorTextOnLightSecondary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
fun DocSortHeaderRow(
    totalCount: Int,
    selectedSort: SortType,
    onSortSelected: (SortType) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = buildAnnotatedString {
                append("Hiển thị ")
                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, color = ColorInk)) {
                    append("$totalCount")
                }
                append(" tài liệu")
            },
            fontFamily = DmSansFamily,
            fontSize = 12.sp,
            color = ColorTextOnLightSecondary
        )

        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            SortType.values().forEach { sort ->
                val isSelected = selectedSort == sort
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(if (isSelected) ColorInk else Color.Transparent)
                        .border(1.dp, if (isSelected) ColorInk else ColorBorder, CircleShape)
                        .clickable { onSortSelected(sort) }
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = sort.label,
                        fontFamily = DmSansFamily,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (isSelected) Color.White else ColorTextOnLightSecondary
                    )
                }
            }
        }
    }
}
