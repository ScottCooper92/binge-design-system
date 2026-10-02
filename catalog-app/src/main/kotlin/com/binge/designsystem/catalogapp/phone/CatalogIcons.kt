package com.binge.designsystem.catalogapp.phone

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.VerticalAlignTop
import androidx.compose.ui.graphics.vector.ImageVector
import com.binge.designsystem.catalogapp.registry.CatalogEntry

/**
 * The icon a demo's card shows, by the group of its source file. A demo from a group not listed here
 * still gets a card, with a generic play icon, so adding a demo file needs no edit here.
 */
internal fun CatalogEntry.demoIcon(): ImageVector =
    when (group) {
        "Modal" -> Icons.Filled.Layers
        "TopBar" -> Icons.Filled.VerticalAlignTop
        "BusyState" -> Icons.Filled.HourglassTop
        "Locale" -> Icons.Filled.Translate
        else -> Icons.Filled.PlayCircle
    }
