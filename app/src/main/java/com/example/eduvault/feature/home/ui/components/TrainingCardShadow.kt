package com.example.eduvault.feature.home.ui.components

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp

internal fun Modifier.trainingCardShadow(shape: Shape): Modifier = dropShadow(
    shape = shape,
    shadow = Shadow(
        radius = 16.dp,
        color = Color(0xFF0F172A).copy(alpha = 0.06f),
        offset = DpOffset(0.dp, 4.dp),
    ),
)
