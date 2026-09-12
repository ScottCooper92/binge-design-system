package com.binge.designsystem.component

import androidx.compose.runtime.Composable
import com.android.tools.screenshot.PreviewTest
import com.binge.designsystem.preview.ComponentPreviews
import com.binge.designsystem.preview.ScreenshotTheme

class GridScreenHeaderScreenshotTest {
    /** Long title is where width matters — verify truncation across all three. */
    @PreviewTest
    @ComponentPreviews
    @Composable
    fun LongTitle() {
        ScreenshotTheme {
            GridScreenHeader(title = "Science Fiction & Fantasy Adventures", onBack = {})
        }
    }
}
