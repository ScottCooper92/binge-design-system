package com.binge.designsystem.catalog

import androidx.compose.runtime.Composable
import com.android.tools.screenshot.PreviewTest
import com.binge.designsystem.preview.ComponentPreviews

/** Screenshot coverage for the icon plate: a glyph, a tinted glyph and text. */
class BingeIconPlateSamplesScreenshotTest {
    @PreviewTest
    @ComponentPreviews
    @Composable
    fun Plates() {
        BingeIconPlateSample()
    }
}
