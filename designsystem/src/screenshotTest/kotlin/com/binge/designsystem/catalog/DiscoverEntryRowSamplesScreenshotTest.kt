package com.binge.designsystem.catalog

import androidx.compose.runtime.Composable
import com.android.tools.screenshot.PreviewTest
import com.binge.designsystem.preview.ComponentPreviews

/** Screenshot coverage for the [DiscoverEntryRow][com.binge.designsystem.component.DiscoverEntryRow] catalog sample (#750). */
class DiscoverEntryRowSamplesScreenshotTest {
    @PreviewTest
    @ComponentPreviews
    @Composable
    fun Row() {
        DiscoverEntryRowSample()
    }
}
