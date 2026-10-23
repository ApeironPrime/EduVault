package com.example.eduvault.feature.home.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Assignment
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.eduvault.core.theme.PlusJakartaSansFamily
import com.example.eduvault.feature.home.ui.HomeTab
import com.example.eduvault.feature.home.ui.HomeUiState

/**
 * Khối 1: Top Bar tinh gọn & Định danh chuyên nghiệp
 * - Status bar trong suốt, nền Slate 50 (#F8FAFC) ăn liền mạch từ đỉnh màn hình xuống.
 * - Avatar bo tròn, tên Aether, mã NV: FPT-8821 • Lô Fresher K26.
 * - Nút chuông thông báo có badge [🔔 2].
 */
@Composable
fun HomeTopBar(
    uiState: HomeUiState,
    onNotificationClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 18.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        // Bên trái: Avatar + Tên + Định danh chuyên nghiệp
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.weight(1f).padding(end = 12.dp)
        ) {
            // Avatar bo tròn doanh nghiệp có online dot
            Box {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(Color(0xFF2563EB), Color(0xFF1E40AF))
                            )
                        )
                        .border(1.5.dp, Color.White, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = uiState.userName.trim().firstOrNull()?.uppercase() ?: "E",
                        fontFamily = PlusJakartaSansFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color.White,
                    )
                }

                // Chấm xanh trạng thái đang hoạt động / Onboarding
                Box(
                    modifier = Modifier
                        .size(11.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF10B981))
                        .border(2.dp, Color(0xFFF8FAFC), CircleShape)
                        .align(Alignment.BottomEnd)
                )
            }

            Column(Modifier.weight(1f)) {
                val displayName = uiState.userName.ifBlank { "Tài khoản EduVault" }

                Text(
                    text = displayName,
                    fontFamily = PlusJakartaSansFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = Color(0xFF0F172A),
                )

                val subText = uiState.university.ifBlank { "Chương trình đào tạo" }

                Text(
                    text = subText,
                    fontFamily = PlusJakartaSansFamily,
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF64748B),
                )
            }
        }

        // Bên phải: Nút chuông thông báo [🔔 2]
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(Color.White)
                .border(1.dp, Color(0xFFE2E8F0), CircleShape)
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) { onNotificationClick() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.Notifications,
                contentDescription = "Thông báo",
                tint = Color(0xFF0F172A),
                modifier = Modifier.size(20.dp)
            )

            if (uiState.unreadNotificationCount > 0) Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 5.dp, end = 5.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFEF4444))
                    .padding(horizontal = 4.dp, vertical = 1.dp)
            ) {
                Text(
                    text = if (uiState.unreadNotificationCount > 99) "99+" else uiState.unreadNotificationCount.toString(),
                    color = Color.White,
                    fontSize = 8.5.sp,
                    lineHeight = 10.sp,
                    maxLines = 1,
                    fontWeight = FontWeight.Bold,
                    fontFamily = PlusJakartaSansFamily
                )
            }
        }
    }
}

/** Translucent floating surface. Its contents are never blurred. */
@Composable
fun HomeBottomNav(
    selectedTab: HomeTab,
    onTabSelected: (HomeTab) -> Unit,
    backdrop: DockBackdrop? = null,
) {
    val selectedIndex = when (selectedTab) {
        HomeTab.HOME -> 0
        HomeTab.QUIZ, HomeTab.DOCUMENTS -> 1
        HomeTab.PROFILE -> 2
    }
    val haptic = LocalHapticFeedback.current
    val select: (HomeTab) -> Unit = {
        if (it != selectedTab) {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            onTabSelected(it)
        }
    }
    val shape = RoundedCornerShape(32.dp)
    Box(Modifier.fillMaxWidth().navigationBarsPadding()
        .padding(start = 24.dp, end = 24.dp, bottom = 16.dp, top = 8.dp)) {
        BoxWithConstraints(Modifier.fillMaxWidth().height(64.dp)
            .dropShadow(shape, Shadow(radius = 24.dp, offset = DpOffset(0.dp, 8.dp),
                color = Color(0xFF0F172A).copy(alpha = 0.06f)))
            .clip(shape)) {
            backdrop?.Background(Modifier.matchParentSize())
            Box(Modifier.matchParentSize().background(Color.White.copy(alpha = 0.92f)))
            val itemWidth = (maxWidth - 12.dp) / 3
            val indicatorX by animateDpAsState(itemWidth * selectedIndex,
                animationSpec = spring(), label = "dock_indicator")
            Box(Modifier.padding(vertical = 6.dp).offset { IntOffset((indicatorX + 6.dp).roundToPx(), 0) }.width(itemWidth).fillMaxHeight()
                .clip(RoundedCornerShape(26.dp)).background(Color(0xFFF1F5F9)))
            Row(Modifier.fillMaxWidth().fillMaxHeight().padding(6.dp).selectableGroup()) {
                BottomNavItem(selectedIndex == 0, { select(HomeTab.HOME) },
                    Icons.Outlined.Home, "Lộ trình", Modifier.weight(1f).fillMaxHeight())
                BottomNavItem(selectedIndex == 1, { select(HomeTab.QUIZ) },
                    Icons.AutoMirrored.Outlined.Assignment, "Nhiệm vụ", Modifier.weight(1f).fillMaxHeight())
                BottomNavItem(selectedIndex == 2, { select(HomeTab.PROFILE) },
                    Icons.Outlined.Person, "Cá nhân", Modifier.weight(1f).fillMaxHeight())
            }
        }
    }
}

@Composable
private fun BottomNavItem(
    selected: Boolean,
    onClick: () -> Unit,
    icon: ImageVector,
    label: String,
    modifier: Modifier = Modifier,
) {
    Column(modifier.selectable(selected = selected, onClick = onClick, role = Role.Tab),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center) {
        Icon(icon, contentDescription = null,
            tint = if (selected) Color(0xFF0F172A) else Color(0xFF64748B),
            modifier = Modifier.size(21.dp))
        Spacer(Modifier.height(3.dp))
        Text(label, color = if (selected) Color(0xFF0F172A) else Color(0xFF64748B),
            fontFamily = PlusJakartaSansFamily, fontSize = 10.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
            maxLines = 1)
    }
}
