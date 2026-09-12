package com.binge.designsystem.catalog

import androidx.compose.runtime.Composable
import com.binge.designsystem.component.GridScreenHeader
import com.binge.designsystem.preview.ScreenshotTheme

/** Grid-screen header — a back button above a large display title for category/genre screens. */
@Composable
fun GridScreenHeaderSample() {
    ScreenshotTheme {
        GridScreenHeader(title = "Action", onBack = {})
    }
}
