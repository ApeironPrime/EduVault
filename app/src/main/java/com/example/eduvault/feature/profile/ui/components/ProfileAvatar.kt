package com.example.eduvault.feature.profile.ui.components

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.produceState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.ImageBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.eduvault.core.theme.DmSansFamily
import java.io.File

@Composable
fun ProfileAvatar(
    avatarUrl: String,
    fullName: String,
    modifier: Modifier = Modifier,
    borderWidth: Dp = 3.dp,
    initialSize: TextUnit = 32.sp
) {
    val bitmap by produceState<ImageBitmap?>(null, avatarUrl) {
        value = withContext(Dispatchers.IO) {
            if (avatarUrl.isBlank() || avatarUrl.startsWith("emoji:")) return@withContext null
            val file = File(avatarUrl)
            if (!file.isFile) return@withContext null
            try {
                val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                BitmapFactory.decodeFile(file.absolutePath, bounds)
                var sample = 1
                while (maxOf(bounds.outWidth, bounds.outHeight) / sample > 512) sample *= 2
                BitmapFactory.decodeFile(file.absolutePath, BitmapFactory.Options().apply { inSampleSize = sample })?.asImageBitmap()
            } catch (_: Exception) { null }
        }
    }

    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(
                Brush.linearGradient(
                    colors = listOf(Color(0xFF334155), Color(0xFF0F172A))
                )
            )
            .border(borderWidth, Color.White, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        val loadedBitmap = bitmap
        if (loadedBitmap != null) {
            Image(
                bitmap = loadedBitmap,
                contentDescription = "Ảnh đại diện thiết bị",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            val initial = fullName.trim().split(Regex("\\s+"))
                .filter { it.isNotBlank() }.take(2).joinToString("") { it.first().uppercase() }.ifBlank { "E" }
            Text(text = initial, fontFamily = DmSansFamily, fontWeight = FontWeight.SemiBold,
                fontSize = initialSize, color = Color.White)
        }
    }
}
