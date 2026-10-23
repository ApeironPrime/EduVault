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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.eduvault.core.theme.ColorAmber
import com.example.eduvault.core.theme.ColorAmberDark
import com.example.eduvault.core.theme.ColorBorder
import com.example.eduvault.core.theme.ColorCream
import com.example.eduvault.core.theme.ColorForest
import com.example.eduvault.core.theme.ColorInk
import com.example.eduvault.core.theme.ColorTextOnDarkSecondary
import com.example.eduvault.core.theme.ColorTextOnLight
import com.example.eduvault.core.theme.ColorTextOnLightSecondary
import com.example.eduvault.core.theme.DmSansFamily
import com.example.eduvault.core.theme.JetBrainsMonoFamily
import com.example.eduvault.core.theme.PlayfairDisplayFamily
import com.example.eduvault.feature.library.ui.SortType
import java.util.Locale

@Composable
fun SearchBarAndActions(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    selectedSort: SortType,
    onSortSelected: (SortType) -> Unit,
    selectedSchool: String,
    onSchoolSelected: (String) -> Unit,
    isGridView: Boolean,
    onToggleViewMode: () -> Unit,
    documentCredits: Int,
    onUploadClick: () -> Unit
) {
    val focusManager = LocalFocusManager.current
    var schoolMenuExpanded by remember { mutableStateOf(false) }
    var sortMenuExpanded by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Main Search Text Field
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                placeholder = {
                    Text(
                        text = "Tìm tài liệu, kỹ năng...",
                        fontFamily = DmSansFamily,
                        fontSize = 13.sp,
                        color = ColorTextOnLightSecondary
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.Search,
                        contentDescription = "Search",
                        tint = ColorTextOnLightSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedBorderColor = ColorAmber,
                    unfocusedBorderColor = ColorBorder
                )
            )

            Spacer(modifier = Modifier.width(8.dp))

            // Entry point for mentors and learners to contribute training material.
            Row(
                modifier = Modifier
                    .height(48.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(ColorAmber.copy(alpha = 0.12f))
                    .border(1.5.dp, ColorAmber, RoundedCornerShape(10.dp))
                    .clickable { onUploadClick() }
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "＋ ĐÓNG GÓP",
                    fontFamily = JetBrainsMonoFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.5.sp,
                    color = ColorAmberDark
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Pill Dropdowns
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                // School Select
                Box {
                    Row(
                        modifier = Modifier
                            .background(Color.White, RoundedCornerShape(8.dp))
                            .border(1.dp, ColorBorder, RoundedCornerShape(8.dp))
                            .clickable { schoolMenuExpanded = true }
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = selectedSchool,
                            fontFamily = DmSansFamily,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = ColorTextOnLight
                        )
                    }

                    DropdownMenu(
                        expanded = schoolMenuExpanded,
                        onDismissRequest = { schoolMenuExpanded = false }
                    ) {
                        listOf("Tất cả đơn vị", "Đào tạo", "Phát triển phần mềm", "Kiểm thử", "Vận hành").forEach { school ->
                            DropdownMenuItem(
                                text = { Text(school, fontFamily = DmSansFamily, fontSize = 12.sp) },
                                onClick = {
                                    onSchoolSelected(school)
                                    schoolMenuExpanded = false
                                }
                            )
                        }
                    }
                }

                // Sort Dropdown
                Box {
                    Row(
                        modifier = Modifier
                            .background(Color.White, RoundedCornerShape(8.dp))
                            .border(1.dp, ColorBorder, RoundedCornerShape(8.dp))
                            .clickable { sortMenuExpanded = true }
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = selectedSort.label,
                            fontFamily = DmSansFamily,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = ColorTextOnLight
                        )
                    }

                    DropdownMenu(
                        expanded = sortMenuExpanded,
                        onDismissRequest = { sortMenuExpanded = false }
                    ) {
                        SortType.values().forEach { sort ->
                            DropdownMenuItem(
                                text = { Text(sort.label, fontFamily = DmSansFamily, fontSize = 12.sp) },
                                onClick = {
                                    onSortSelected(sort)
                                    sortMenuExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            // Grid / List Layout Toggle
            IconButton(
                onClick = onToggleViewMode,
                modifier = Modifier
                    .size(34.dp)
                    .background(ColorCream, RoundedCornerShape(8.dp))
            ) {
                Icon(
                    imageVector = if (isGridView) Icons.AutoMirrored.Filled.List else Icons.Filled.GridOn,
                    contentDescription = "Toggle view",
                    tint = ColorTextOnLight,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
fun DocsBanner(
    totalDocs: Int,
    totalSubjects: Int,
    totalSaved: Int
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(ColorInk)
            .drawBehind {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(ColorAmber.copy(alpha = 0.18f), Color.Transparent),
                        center = Offset(size.width * 0.9f, size.height * 0.1f),
                        radius = size.width * 0.5f
                    )
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(ColorForest.copy(alpha = 0.14f), Color.Transparent),
                        center = Offset(size.width * 0.4f, size.height * 0.9f),
                        radius = size.width * 0.4f
                    )
                )
            }
            .padding(20.dp)
    ) {
        Column {
            Text(
                text = "✦ THƯ VIỆN ĐÀO TẠO",
                fontFamily = JetBrainsMonoFamily,
                fontSize = 9.sp,
                color = ColorAmber,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.5.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = buildAnnotatedString {
                    append("Học liệu ")
                    withStyle(style = SpanStyle(color = ColorAmber, fontStyle = FontStyle.Italic)) {
                        append("theo chương trình")
                    }
                },
                fontFamily = PlayfairDisplayFamily,
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Tìm kiếm tài liệu đã kiểm duyệt theo kỹ năng, công nghệ và chương trình dành cho intern/fresher.",
                fontFamily = DmSansFamily,
                fontSize = 11.sp,
                color = ColorTextOnDarkSecondary,
                lineHeight = 16.sp,
                modifier = Modifier.fillMaxWidth(0.85f)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = if (totalDocs >= 1000) "${String.format(Locale.ROOT, "%.1f", totalDocs / 1000f)}k" else "$totalDocs",
                        fontFamily = JetBrainsMonoFamily,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = ColorAmber
                    )
                    Text(
                        text = "TÀI LIỆU",
                        fontFamily = DmSansFamily,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = ColorTextOnDarkSecondary,
                        letterSpacing = 0.5.sp
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "$totalSubjects",
                        fontFamily = JetBrainsMonoFamily,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = ColorAmber
                    )
                    Text(
                        text = "KỸ NĂNG",
                        fontFamily = DmSansFamily,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = ColorTextOnDarkSecondary,
                        letterSpacing = 0.5.sp
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "$totalSaved",
                        fontFamily = JetBrainsMonoFamily,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = ColorAmber
                    )
                    Text(
                        text = "ĐÃ LƯU",
                        fontFamily = DmSansFamily,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = ColorTextOnDarkSecondary,
                        letterSpacing = 0.5.sp
                    )
                }
            }
        }
    }
}
