package com.binge.designsystem.catalog

import androidx.compose.runtime.Composable
import com.binge.designsystem.component.BingeTabRow
import com.binge.designsystem.preview.ScreenshotTheme

/** Scrollable tab strip with the second tab active — the caller owns the selected index. */
@Composable
fun BingeTabRowSample() {
    ScreenshotTheme {
        BingeTabRow(
            tabs = listOf("All (42)", "Pending (4)", "Downloading (5)", "Available (28)", "Declined (2)"),
            selectedIndex = 1,
            onSelect = {},
        )
    }
}
