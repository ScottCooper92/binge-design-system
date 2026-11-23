package com.binge.designsystem.catalog

import androidx.compose.runtime.Composable
import com.android.tools.screenshot.PreviewTest
import com.binge.designsystem.preview.ComponentPreviews

/** Screenshot coverage for the relative-date formatter's catalog sample. */
class RelativeDateSamplesScreenshotTest {
    @PreviewTest
    @ComponentPreviews
    @Composable
    fun RelativeDate() {
        RelativeDateSample()
    }
}
