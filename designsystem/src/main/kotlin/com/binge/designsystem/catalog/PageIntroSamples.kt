@file:CatalogGroup("Layout")
@file:SelfDescribing

package com.binge.designsystem.catalog

import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.runtime.Composable
import com.binge.designsystem.component.ItemGroup
import com.binge.designsystem.component.ListItem
import com.binge.designsystem.component.PageIntro
import com.binge.designsystem.preview.ScreenshotTheme

private val sampleRows =
    listOf(ListItem(icon = Icons.Filled.Home, label = "Home screen", detail = "Your libraries’ order", clickable = false))

/** A one-line intro above a group: it, the heading and the card edge share a left edge. */
@Composable
fun PageIntroSample() {
    ScreenshotTheme {
        Column {
            PageIntro("How your home looks on every device you sign in to.")
            ItemGroup(title = "Layout", rows = sampleRows)
        }
    }
}

/** An intro long enough to wrap to a second line, which keeps the same inset and the same gap below it. */
@Composable
fun PageIntroLongSample() {
    ScreenshotTheme {
        Column {
            PageIntro("How your home looks on every device you sign in to, and which libraries show first on each of them.")
            ItemGroup(title = "Layout", rows = sampleRows)
        }
    }
}
