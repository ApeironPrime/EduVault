package com.example.eduvault.feature.home.ui.dialogs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.eduvault.core.theme.DmSansFamily
import com.example.eduvault.domain.model.EduNotification
import com.example.eduvault.feature.home.ui.HomeViewModel

@Composable
fun NotificationsDialog(onDismiss: () -> Unit, viewModel: HomeViewModel = hiltViewModel()) {
    val notifications by viewModel.notifications.collectAsState()
    Dialog(onDismissRequest = onDismiss) {
        NotificationsPanel(notifications, onDismiss)
    }
}

@Composable
fun NotificationsPanel(notifications: List<EduNotification>, onDismiss: () -> Unit, modifier: Modifier = Modifier) {
    Card(modifier.fillMaxWidth().heightIn(max = 520.dp), shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.Notifications, null, tint = Color(0xFF475569))
                Spacer(Modifier.width(8.dp))
                Text("Thông báo", Modifier.weight(1f), fontFamily = DmSansFamily, fontWeight = FontWeight.SemiBold)
                IconButton(onClick = onDismiss) { Icon(Icons.Outlined.Close, "Đóng") }
            }
            if (notifications.isEmpty()) {
                Text("Chưa có thông báo.", fontFamily = DmSansFamily, color = Color(0xFF475569))
            } else {
                LazyColumn(Modifier.fillMaxWidth().weight(1f, fill = false),
                    verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(notifications, key = { it.id }) { notice ->
                        Surface(color = Color(0xFFF8FAFC), shape = RoundedCornerShape(12.dp)) {
                            Column(Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(notice.title, fontFamily = DmSansFamily, fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp, color = Color(0xFF0F172A))
                                Text(notice.getFormattedTime(), fontFamily = DmSansFamily, fontSize = 11.sp,
                                    color = Color(0xFF64748B))
                                Text(notice.content, fontFamily = DmSansFamily, fontSize = 13.sp,
                                    color = Color(0xFF475569))
                            }
                        }
                    }
                }
            }
            Button(onClick = onDismiss, modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F172A))) {
                Text("Đóng thông báo", fontFamily = DmSansFamily)
            }
        }
    }
}
