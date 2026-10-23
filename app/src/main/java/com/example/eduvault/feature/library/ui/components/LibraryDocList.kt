package com.example.eduvault.feature.library.ui.components

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.outlined.Bookmark
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.eduvault.core.theme.ColorAmber
import com.example.eduvault.core.theme.ColorAmberDark
import com.example.eduvault.core.theme.ColorBorder
import com.example.eduvault.core.theme.ColorForest
import com.example.eduvault.core.theme.ColorInk
import com.example.eduvault.core.theme.ColorTextOnLight
import com.example.eduvault.core.theme.ColorTextOnLightSecondary
import com.example.eduvault.core.theme.DmSansFamily
import com.example.eduvault.core.theme.JetBrainsMonoFamily
import com.example.eduvault.feature.library.ui.LibraryDoc
import com.example.eduvault.feature.home.ui.DocType
import com.example.eduvault.feature.home.ui.components.materialIcon
import com.example.eduvault.feature.library.ui.LibraryDocType

@Composable
fun LibraryDocCard(
    doc: LibraryDoc,
    onToggleSaved: (String) -> Unit,
    onDocClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bgGradients = listOf(
        Brush.linearGradient(listOf(Color(0xFFFEF9F0), Color(0xFFFDF0D5))), // Note
        Brush.linearGradient(listOf(Color(0xFFF0F9F4), Color(0xFFD4EDDA))), // Quiz
        Brush.linearGradient(listOf(Color(0xFFF0F4FF), Color(0xFFDDE8FF))), // Slide
        Brush.linearGradient(listOf(Color(0xFFFFF0F0), Color(0xFFFDD8D8))), // Summary
        Brush.linearGradient(listOf(Color(0xFFF5F0FF), Color(0xFFEAD5FF))),
        Brush.linearGradient(listOf(Color(0xFFF0FCFF), Color(0xFFD5F0FF)))
    )
    val gradient = bgGradients[Math.floorMod(doc.bgIndex, bgGradients.size)]



    val badgeBg = when (doc.type) {
        LibraryDocType.NOTE -> ColorAmber.copy(alpha = 0.15f)
        LibraryDocType.QUIZ -> ColorForest.copy(alpha = 0.12f)
        LibraryDocType.SLIDE -> ColorTextOnLightSecondary.copy(alpha = 0.12f)
        LibraryDocType.SUMMARY -> Color(0xFFC0392B).copy(alpha = 0.1f)
    }

    val badgeTextColor = when (doc.type) {
        LibraryDocType.NOTE -> ColorAmberDark
        LibraryDocType.QUIZ -> ColorForest
        LibraryDocType.SLIDE -> ColorTextOnLight
        LibraryDocType.SUMMARY -> Color(0xFFC0392B)
    }

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, ColorBorder, RoundedCornerShape(12.dp))
            .background(Color.White)
            .clickable { onDocClick() }
    ) {
        // Thumbnail section
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(86.dp)
                .background(gradient),
            contentAlignment = Alignment.Center
        ) {
            // Type badge
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(8.dp)
                    .background(badgeBg, RoundedCornerShape(4.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = doc.type.label.uppercase(),
                    fontFamily = JetBrainsMonoFamily,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold,
                    color = badgeTextColor
                )
            }

            // Quantity Pages tag
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(8.dp)
                    .background(Color.White.copy(alpha = 0.85f), RoundedCornerShape(4.dp))
                    .padding(horizontal = 5.dp, vertical = 2.dp)
            ) {
                Text(
                    text = doc.quantityLabel,
                    fontFamily = JetBrainsMonoFamily,
                    fontSize = 8.sp,
                    color = ColorTextOnLightSecondary,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Icon(DocType.valueOf(doc.type.name).materialIcon, contentDescription = null, tint = ColorInk, modifier = Modifier.size(28.dp))
        }

        // Info Section
        Column(modifier = Modifier.padding(10.dp)) {
            Text(
                text = doc.courseCode,
                fontFamily = JetBrainsMonoFamily,
                fontSize = 9.sp,
                color = ColorTextOnLightSecondary,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )

            Spacer(modifier = Modifier.height(3.dp))

            Text(
                text = doc.title,
                fontFamily = DmSansFamily,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = ColorInk,
                maxLines = 2,
                minLines = 2,
                lineHeight = 15.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = doc.type.label,
                    fontFamily = DmSansFamily,
                    fontSize = 10.sp,
                    color = ColorTextOnLightSecondary,
                    modifier = Modifier.weight(1f)
                )

                IconButton(
                    onClick = { onToggleSaved(doc.id) },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = if (doc.isSaved) Icons.Outlined.Bookmark else Icons.Outlined.BookmarkBorder,
                        contentDescription = if (doc.isSaved) "Bỏ lưu học liệu" else "Lưu học liệu",
                        tint = if (doc.isSaved) ColorAmber else ColorTextOnLightSecondary,
                        modifier = Modifier.size(15.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun LibraryDocListItem(
    doc: LibraryDoc,
    onToggleSaved: (String) -> Unit,
    onDocClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bgGradients = listOf(
        Brush.linearGradient(listOf(Color(0xFFFEF9F0), Color(0xFFFDF0D5))),
        Brush.linearGradient(listOf(Color(0xFFF0F9F4), Color(0xFFD4EDDA))),
        Brush.linearGradient(listOf(Color(0xFFF0F4FF), Color(0xFFDDE8FF))),
        Brush.linearGradient(listOf(Color(0xFFFFF0F0), Color(0xFFFDD8D8))),
        Brush.linearGradient(listOf(Color(0xFFF5F0FF), Color(0xFFEAD5FF))),
        Brush.linearGradient(listOf(Color(0xFFF0FCFF), Color(0xFFD5F0FF)))
    )
    val gradient = bgGradients[Math.floorMod(doc.bgIndex, bgGradients.size)]



    val badgeBg = when (doc.type) {
        LibraryDocType.NOTE -> ColorAmber.copy(alpha = 0.15f)
        LibraryDocType.QUIZ -> ColorForest.copy(alpha = 0.12f)
        LibraryDocType.SLIDE -> ColorTextOnLightSecondary.copy(alpha = 0.12f)
        LibraryDocType.SUMMARY -> Color(0xFFC0392B).copy(alpha = 0.1f)
    }

    val badgeTextColor = when (doc.type) {
        LibraryDocType.NOTE -> ColorAmberDark
        LibraryDocType.QUIZ -> ColorForest
        LibraryDocType.SLIDE -> ColorTextOnLight
        LibraryDocType.SUMMARY -> Color(0xFFC0392B)
    }

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, ColorBorder, RoundedCornerShape(12.dp))
            .background(Color.White)
            .clickable { onDocClick() }
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left Thumbnail square
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(gradient),
            contentAlignment = Alignment.Center
        ) {
            Icon(DocType.valueOf(doc.type.name).materialIcon, contentDescription = null, tint = ColorInk, modifier = Modifier.size(24.dp))
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Center Info
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = doc.courseCode,
                    fontFamily = JetBrainsMonoFamily,
                    fontSize = 9.sp,
                    color = ColorTextOnLightSecondary,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.width(8.dp))

                Box(
                    modifier = Modifier
                        .background(badgeBg, RoundedCornerShape(3.dp))
                        .padding(horizontal = 4.dp, vertical = 1.dp)
                ) {
                    Text(
                        text = doc.type.label.uppercase(),
                        fontFamily = JetBrainsMonoFamily,
                        fontSize = 7.sp,
                        fontWeight = FontWeight.Bold,
                        color = badgeTextColor
                    )
                }

            }

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = doc.title,
                fontFamily = DmSansFamily,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = ColorInk,
                maxLines = 2,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                lineHeight = 16.sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = doc.quantityLabel,
                fontFamily = DmSansFamily,
                fontSize = 10.sp,
                color = ColorTextOnLightSecondary
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Right Save Bookmark action
        IconButton(
            onClick = { onToggleSaved(doc.id) },
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                imageVector = if (doc.isSaved) Icons.Outlined.Bookmark else Icons.Outlined.BookmarkBorder,
                contentDescription = if (doc.isSaved) "Bỏ lưu học liệu" else "Lưu học liệu",
                tint = if (doc.isSaved) ColorAmber else ColorTextOnLightSecondary,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
fun PaginationRow(
    currentPage: Int,
    totalPages: Int,
    onPageSelected: (Int) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Prev button
        IconButton(
            onClick = { if (currentPage > 1) onPageSelected(currentPage - 1) },
            enabled = currentPage > 1,
            modifier = Modifier
                .size(34.dp)
                .border(1.dp, ColorBorder, RoundedCornerShape(8.dp))
                .background(Color.White, RoundedCornerShape(8.dp))
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                contentDescription = "Trang trước",
                tint = if (currentPage > 1) ColorTextOnLight else ColorTextOnLightSecondary,
                modifier = Modifier.size(14.dp)
            )
        }

        Spacer(modifier = Modifier.width(6.dp))

        val pagesToDraw = remember(currentPage, totalPages) {
            val list = mutableListOf<Int>()
            if (totalPages <= 5) {
                for (i in 1..totalPages) list.add(i)
            } else {
                list.add(1)
                if (currentPage > 3) {
                    list.add(-1)
                }
                val start = maxOf(2, currentPage - 1)
                val end = minOf(totalPages - 1, currentPage + 1)
                for (i in start..end) {
                    if (!list.contains(i)) list.add(i)
                }
                if (currentPage < totalPages - 2) {
                    list.add(-2)
                }
                if (!list.contains(totalPages)) list.add(totalPages)
            }
            list
        }

        pagesToDraw.forEach { page ->
            if (page < 0) {
                Text(
                    text = "...",
                    fontFamily = JetBrainsMonoFamily,
                    fontSize = 12.sp,
                    color = ColorTextOnLightSecondary,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            } else {
                val isSelected = currentPage == page
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) ColorInk else Color.White)
                        .border(1.dp, if (isSelected) ColorInk else ColorBorder, RoundedCornerShape(8.dp))
                        .clickable { onPageSelected(page) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "$page",
                        fontFamily = JetBrainsMonoFamily,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) Color.White else ColorTextOnLight
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
            }
        }

        // Next button
        IconButton(
            onClick = { if (currentPage < totalPages) onPageSelected(currentPage + 1) },
            enabled = currentPage < totalPages,
            modifier = Modifier
                .size(34.dp)
                .border(1.dp, ColorBorder, RoundedCornerShape(8.dp))
                .background(Color.White, RoundedCornerShape(8.dp))
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.ArrowForward,
                contentDescription = "Trang sau",
                tint = if (currentPage < totalPages) ColorTextOnLight else ColorTextOnLightSecondary,
                modifier = Modifier.size(14.dp)
            )
        }
    }
}
