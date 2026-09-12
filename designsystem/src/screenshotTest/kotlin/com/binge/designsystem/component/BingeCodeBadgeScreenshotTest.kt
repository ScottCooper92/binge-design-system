package com.binge.designsystem.component

import androidx.compose.runtime.Composable
import com.android.tools.screenshot.PreviewTest
import com.binge.designsystem.preview.ComponentPreviews
import com.binge.designsystem.preview.ScreenshotTheme

/** Small bordered code badge — colour axis only. */
class BingeCodeBadgeScreenshotTest {
    @PreviewTest
    @ComponentPreviews
    @Composable
    fun Code() {
        ScreenshotTheme {
            BingeCodeBadge(label = "EN")
        }
    }
}
