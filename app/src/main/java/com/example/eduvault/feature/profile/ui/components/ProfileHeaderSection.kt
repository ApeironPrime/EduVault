package com.example.eduvault.feature.profile.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.eduvault.core.theme.DmSansFamily
import com.example.eduvault.feature.profile.ui.ProfileUiState

@Composable
fun ProfileHeaderSection(uiState: ProfileUiState, paddingValues: PaddingValues, onEditClick: () -> Unit) {
    val user = uiState.user ?: return
    Column(Modifier.fillMaxWidth().statusBarsPadding()
        .padding(top = paddingValues.calculateTopPadding() + 20.dp, start = 24.dp, end = 24.dp, bottom = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Hồ sơ cá nhân", fontFamily = DmSansFamily, fontSize = 26.sp,
            fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            ProfileAvatar(user.avatarUrl, user.fullName, Modifier.size(72.dp), initialSize = 28.sp)
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(user.fullName.ifBlank { "Tài khoản EduVault" }, fontFamily = DmSansFamily,
                    fontWeight = FontWeight.SemiBold, fontSize = 20.sp, color = Color(0xFF0F172A))
                Text(user.email, fontFamily = DmSansFamily, fontSize = 12.sp, color = Color(0xFF475569))
                if (user.university.isNotBlank()) Text(user.university, fontFamily = DmSansFamily,
                    fontSize = 12.sp, color = Color(0xFF475569))
            }
        }
        OutlinedButton(onClick = onEditClick) {
            Icon(Icons.Outlined.Edit, null, Modifier.size(16.dp))
            Spacer(Modifier.width(8.dp))
            Text("Chỉnh sửa hồ sơ", fontFamily = DmSansFamily)
        }
    }
}
