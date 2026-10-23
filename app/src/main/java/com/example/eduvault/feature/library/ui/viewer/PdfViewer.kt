package com.example.eduvault.feature.library.ui.viewer

import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.eduvault.core.theme.ColorAmber
import com.example.eduvault.core.theme.ColorTextOnLightSecondary
import com.example.eduvault.core.theme.DmSansFamily
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File

@Composable
fun PdfPage(
    renderer: PdfRenderer,
    pageIndex: Int,
    rendererMutex: Mutex,
    modifier: Modifier = Modifier
) {
    var bitmap by remember(pageIndex) { mutableStateOf<Bitmap?>(null) }

    LaunchedEffect(pageIndex) {
        withContext(Dispatchers.IO) {
            try {
                rendererMutex.withLock {
                    if (pageIndex < renderer.pageCount) {
                        val page = renderer.openPage(pageIndex)
                        val width = 800
                        val height = (width.toFloat() / page.width * page.height).toInt()
                        val pageBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                        val canvas = android.graphics.Canvas(pageBitmap)
                        canvas.drawColor(android.graphics.Color.WHITE)
                        page.render(pageBitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                        page.close()
                        bitmap = pageBitmap
                    }
                }
            } catch (e: Exception) {
                Log.e("PdfPage", "Lỗi render trang $pageIndex: ${e.localizedMessage}")
            }
        }
    }

    DisposableEffect(pageIndex) {
        onDispose {
            bitmap?.recycle()
            bitmap = null
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        val currentBitmap = bitmap
        if (currentBitmap != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Image(
                    bitmap = currentBitmap.asImageBitmap(),
                    contentDescription = "Trang ${pageIndex + 1}",
                    modifier = Modifier.fillMaxWidth(),
                    contentScale = ContentScale.FillWidth
                )
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = ColorAmber,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}

@Composable
fun PdfViewer(
    file: File,
    downloadUrl: String,
    modifier: Modifier = Modifier,
    onDownloadFile: (suspend (downloadUrl: String, destinationFile: File) -> Result<File>)? = null
) {
    var isDownloading by remember(file) { mutableStateOf(false) }
    var downloadError by remember(file) { mutableStateOf<String?>(null) }
    var pageCount by remember(file) { mutableIntStateOf(0) }
    var pdfRenderer by remember(file) { mutableStateOf<PdfRenderer?>(null) }
    var fileDescriptor by remember(file) { mutableStateOf<ParcelFileDescriptor?>(null) }

    LaunchedEffect(file) {
        withContext(Dispatchers.IO) {
            try {
                if (!file.exists() && downloadUrl.isNotEmpty() && onDownloadFile != null) {
                    isDownloading = true
                    Log.d("PdfViewer", "Đang nạp tài liệu từ URL: $downloadUrl")
                    val result = onDownloadFile(downloadUrl, file)
                    result.onFailure { error ->
                        downloadError = "Lỗi khi nạp tài liệu: ${error.localizedMessage}"
                    }
                    isDownloading = false
                }

                if (file.exists()) {
                    val pfd = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
                    val renderer = PdfRenderer(pfd)
                    fileDescriptor = pfd
                    pdfRenderer = renderer
                    pageCount = renderer.pageCount
                } else if (downloadError == null) {
                    downloadError = "Không tìm thấy tệp tài liệu cục bộ."
                }
            } catch (e: Exception) {
                Log.e("PdfViewer", "Lỗi khi mở/nạp PDF", e)
                downloadError = "Lỗi khi nạp PDF: ${e.localizedMessage}"
            } finally {
                isDownloading = false
            }
        }
    }

    DisposableEffect(file) {
        onDispose {
            try {
                pdfRenderer?.close()
                fileDescriptor?.close()
            } catch (e: Exception) {
                // Ignore close error
            }
        }
    }

    if (isDownloading) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            CircularProgressIndicator(
                color = ColorAmber,
                modifier = Modifier.size(36.dp)
            )
            Text(
                text = "Đang tải xuống tài liệu từ Cloud...",
                fontFamily = DmSansFamily,
                fontSize = 13.sp,
                color = ColorTextOnLightSecondary,
                textAlign = TextAlign.Center
            )
        }
    } else if (downloadError != null) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "⚠️ ${downloadError!!}",
                color = Color.Red.copy(alpha = 0.8f),
                fontFamily = DmSansFamily,
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
    } else {
        val renderer = pdfRenderer
        if (renderer != null && pageCount > 0) {
            val rendererMutex = remember { Mutex() }
            LazyColumn(
                modifier = modifier
                    .fillMaxSize()
                    .padding(horizontal = 4.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 90.dp)
            ) {
                items(pageCount) { index ->
                    PdfPage(
                        renderer = renderer,
                        pageIndex = index,
                        rendererMutex = rendererMutex
                    )
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 40.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator(
                    color = ColorAmber,
                    modifier = Modifier.size(36.dp)
                )
            }
        }
    }
}
