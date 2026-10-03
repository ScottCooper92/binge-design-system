@file:OnePerScreen

package com.binge.designsystem.catalog

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.binge.designsystem.component.BingeTabRow
import com.binge.designsystem.preview.ScreenshotTheme

/** Scrollable tab strip with the second tab active — the caller owns the selected index. */
@Composable
fun BingeTabRowSample() {
    var selected by remember { mutableIntStateOf(1) }
    ScreenshotTheme {
        BingeTabRow(
            tabs = listOf("All (42)", "Pending (4)", "Downloading (5)", "Available (28)", "Declined (2)"),
            selectedIndex = selected,
            onSelect = { selected = it },
        )
    }
}
