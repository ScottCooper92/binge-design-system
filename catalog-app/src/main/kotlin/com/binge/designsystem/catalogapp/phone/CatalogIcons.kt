package com.binge.designsystem.catalogapp.phone

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.PictureInPicture
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.VerticalAlignBottom
import androidx.compose.material.icons.filled.VerticalAlignTop
import androidx.compose.ui.graphics.vector.ImageVector
import com.binge.designsystem.catalogapp.registry.CatalogComponent

/**
 * The icon a card shows when its component has only demos, so no sample to render: the live top bars
 * and navigation bars, and the demos about a behaviour rather than a component. A group not listed here
 * gets a generic play icon.
 */
internal fun CatalogComponent.demoIcon(): ImageVector =
    when (group) {
        "BusyState" -> Icons.Filled.HourglassTop
        "Locale" -> Icons.Filled.Translate
        "Top app bars" -> Icons.Filled.VerticalAlignTop
        "Navigation bars" -> Icons.Filled.VerticalAlignBottom
        "Dialogs" -> Icons.Filled.PictureInPicture
        "Sheets" -> Icons.Filled.Layers
        else -> Icons.Filled.PlayCircle
    }
