package com.example.eduvault.feature.home.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Article
import androidx.compose.material.icons.automirrored.outlined.Assignment
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Slideshow
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.eduvault.feature.home.ui.DocType

internal val DocType.materialIcon: ImageVector
    get() = when (this) {
        DocType.NOTE -> Icons.Outlined.Description
        DocType.QUIZ -> Icons.AutoMirrored.Outlined.Assignment
        DocType.SLIDE -> Icons.Outlined.Slideshow
        DocType.SUMMARY -> Icons.AutoMirrored.Outlined.Article
    }
