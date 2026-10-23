package com.example.eduvault.feature.home.ui.components

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.eduvault.core.theme.ColorBorder
import com.example.eduvault.core.theme.ColorForestLight
import com.example.eduvault.core.theme.ColorTextOnLight
import com.example.eduvault.core.theme.ColorTextOnLightSecondary
import com.example.eduvault.core.theme.DmSansFamily
import com.example.eduvault.feature.home.ui.DocType
import com.example.eduvault.feature.home.ui.ActivityItem
import com.example.eduvault.feature.home.ui.ActivityType
import com.example.eduvault.feature.home.ui.HomeTab

@Composable
fun ActivitySection(
    items: List<ActivityItem>,
    onTabSelected: (HomeTab) -> Unit,
    onStartQuizSetup: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth().trainingCardShadow(RoundedCornerShape(14.dp)),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(modifier = Modifier.padding(vertical = 6.dp)) {
            // Activity rows
            items.forEachIndexed { index, item ->
                ActivityRow(
                    item = item,
                    onClick = {
                        if (item.type == ActivityType.QUIZ) {
                            onStartQuizSetup(item.title)
                        } else {
                            onTabSelected(HomeTab.DOCUMENTS)
                        }
                    }
                )
                if (index < items.size - 1) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(ColorBorder)
                    )
                }
            }
        }
    }
}

@Composable
fun ActivityRow(item: ActivityItem, onClick: () -> Unit) {
    val materialType = when (item.type) {
        ActivityType.NOTE -> DocType.NOTE
        ActivityType.QUIZ -> DocType.QUIZ
        ActivityType.SLIDE -> DocType.SLIDE
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // Icon
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(9.dp))
                .background(Color(0xFFF1F5F9)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(materialType.materialIcon, contentDescription = null,
                tint = Color(0xFF475569), modifier = Modifier.size(19.dp))
        }

        // Info
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.title,
                fontFamily = DmSansFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 13.sp,
                color = ColorTextOnLight,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = item.subtitle,
                fontFamily = DmSansFamily,
                fontSize = 11.sp,
                color = ColorTextOnLightSecondary,
            )
            if (item.progress != null) {
                Spacer(modifier = Modifier.height(5.dp))
                LinearProgressIndicator(
                    progress = { item.progress },
                    modifier = Modifier
                        .width(70.dp)
                        .height(3.5.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = ColorForestLight,
                    trackColor = ColorBorder,
                    strokeCap = StrokeCap.Round,
                )
            }
        }

        // Time
        Text(
            text = item.time,
            style = TextStyle(fontFeatureSettings = "tnum"),
            fontFamily = DmSansFamily,
            fontSize = 10.sp,
            letterSpacing = 0.sp,
            color = ColorTextOnLightSecondary,
        )
    }
}

