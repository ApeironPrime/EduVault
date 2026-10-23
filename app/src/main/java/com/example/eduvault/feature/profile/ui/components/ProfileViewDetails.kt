package com.example.eduvault.feature.profile.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.eduvault.core.theme.DmSansFamily
import com.example.eduvault.feature.home.ui.TrainingUiState
import com.example.eduvault.feature.home.ui.components.TrainingAvailabilityCard
import com.example.eduvault.feature.profile.ui.ProfileUiState

@Composable
fun ProfileViewDetails(
    uiState: ProfileUiState,
    onLogoutClick: () -> Unit,
    onNavigateToAdmin: () -> Unit = {},
    onEditClick: () -> Unit = {},
    trainingState: TrainingUiState = TrainingUiState(),
    onRetryTraining: () -> Unit = {},
) {
    var showHelp by remember { mutableStateOf(false) }
    val navy = Color(0xFF0F172A)
    Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Chương trình đào tạo", fontFamily = DmSansFamily, fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp, color = navy)
        val dashboard = trainingState.dashboard
        if (dashboard == null || trainingState.error != null) {
            TrainingAvailabilityCard(trainingState.isLoading, trainingState.error, onRetryTraining)
        } else {
            Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(2.dp)) {
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(dashboard.program.title, fontFamily = DmSansFamily, fontWeight = FontWeight.SemiBold,
                        fontSize = 17.sp, color = navy)
                    val completed = dashboard.orderedModules.count { it.id in dashboard.assignment.completedModuleIds }
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Text("$completed/${dashboard.orderedModules.size} chặng đã đạt", Modifier.weight(1f),
                            fontFamily = DmSansFamily, fontSize = 13.sp, color = Color(0xFF475569))
                        Text("${dashboard.assignment.completionPercent}%", fontFamily = DmSansFamily,
                            fontSize = 24.sp, fontWeight = FontWeight.SemiBold, color = navy)
                    }
                    LinearProgressIndicator(progress = { dashboard.assignment.completionPercent.coerceIn(0, 100) / 100f },
                        modifier = Modifier.fillMaxWidth(), color = Color(0xFF10B981), trackColor = Color(0xFFE2E8F0),
                        gapSize = 0.dp, drawStopIndicator = {})
                    val active = dashboard.activeModule
                    Text(if (active == null) "Đã hoàn thành chương trình" else
                        "Đang học · Chặng ${dashboard.activeStageNumber}: ${active.title}",
                        fontFamily = DmSansFamily, fontSize = 13.sp, color = Color(0xFF475569))
                    if (dashboard.currentEvidence != null) Text("Bằng chứng thực hành đang chờ mentor duyệt",
                        fontFamily = DmSansFamily, fontSize = 12.sp, color = Color(0xFF92400E))
                }
            }
        }
        Text("Tài khoản", fontFamily = DmSansFamily, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = navy)
        Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(2.dp)) {
            if (uiState.user?.role == "admin") ProfileMenuItem(Icons.Outlined.Settings, "Quản trị hệ thống", onNavigateToAdmin)
            ProfileMenuItem(Icons.Outlined.Settings, "Thông tin và bảo mật", onEditClick)
            ProfileMenuItem(Icons.AutoMirrored.Outlined.HelpOutline, "Hướng dẫn onboarding", { showHelp = true })
        }
        OutlinedButton(onClick = onLogoutClick, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
            Icon(Icons.AutoMirrored.Outlined.Logout, null, Modifier.size(18.dp), tint = Color(0xFFB91C1C))
            Spacer(Modifier.width(8.dp))
            Text("Đăng xuất tài khoản", fontFamily = DmSansFamily, color = Color(0xFFB91C1C))
        }
    }
    if (showHelp) AlertDialog(onDismissRequest = { showHelp = false },
        title = { Text("Hướng dẫn onboarding", fontFamily = DmSansFamily) },
        text = { Text("Học theo thứ tự các chặng trong Lộ trình. Nộp bằng chứng thực hành tại Nhiệm vụ; mentor nghiệm thu để mở chặng tiếp theo. Bài tự kiểm tra giúp ôn luyện và không tự mở khóa chặng.", fontFamily = DmSansFamily) },
        confirmButton = { TextButton(onClick = { showHelp = false }) { Text("Đã hiểu") } })
}

@Composable
private fun ProfileMenuItem(icon: ImageVector, label: String, onClick: () -> Unit) {
    Row(Modifier.fillMaxWidth().clickable(onClick = onClick).padding(18.dp),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Icon(icon, null, Modifier.size(20.dp), tint = Color(0xFF475569))
        Text(label, Modifier.weight(1f), fontFamily = DmSansFamily, fontSize = 14.sp, color = Color(0xFF0F172A))
        Icon(Icons.AutoMirrored.Outlined.KeyboardArrowRight, null, Modifier.size(18.dp), tint = Color(0xFF94A3B8))
    }
}
