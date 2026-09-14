package com.binge.designsystem.catalog

import androidx.compose.runtime.Composable
import com.android.tools.screenshot.PreviewTest
import com.binge.designsystem.preview.ComponentPreviews

/**
 * Screenshot coverage for the code-badge catalog sample — renders the shared [BingeCodeBadgeSample].
 */
class BingeCodeBadgeSamplesScreenshotTest {
    @PreviewTest
    @ComponentPreviews
    @Composable
    fun Codes() {
        BingeCodeBadgeSample()
    }
}
