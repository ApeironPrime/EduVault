package com.example.eduvault.feature.home.ui.components

import android.os.Build
import android.graphics.RenderEffect
import android.graphics.Shader
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.unit.dp

/** Records only screen content, excluding the dock, so blur never includes labels or itself. */
class DockBackdrop(private val layer: GraphicsLayer) {
    private var sourceOrigin by mutableStateOf(Offset.Zero)

    fun Modifier.capture(): Modifier = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) this else this
        .onGloballyPositioned { sourceOrigin = it.positionInRoot() }
        .drawWithContent {
            layer.record { this@drawWithContent.drawContent() }
            drawLayer(layer)
        }

    @Composable
    fun Background(modifier: Modifier) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return
        var dockOrigin by androidx.compose.runtime.remember { mutableStateOf(Offset.Zero) }
        Box(modifier.onGloballyPositioned { dockOrigin = it.positionInRoot() }
            .graphicsLayer {
                val radius = 16.dp.toPx()
                renderEffect = RenderEffect.createBlurEffect(radius, radius, Shader.TileMode.CLAMP)
                    .asComposeRenderEffect()
            }
            .drawWithContent {
                val delta = sourceOrigin - dockOrigin
                translate(delta.x, delta.y) { drawLayer(layer) }
            })
    }
}
