package com.binge.designsystem.catalog

import androidx.compose.runtime.Composable
import com.android.tools.screenshot.PreviewTest
import com.binge.designsystem.preview.ComponentPreviews
import com.binge.designsystem.preview.WorstCaseFontPreviews

/** Screenshot coverage for the [AccountStatsRow] catalog sample. */
class AccountStatsRowSamplesScreenshotTest {
    @PreviewTest
    @ComponentPreviews
    @Composable
    fun stats() {
        AccountStatsRowSample()
    }

    /** At a large font the tiles stack one to a line, so no caption breaks. */
    @PreviewTest
    @WorstCaseFontPreviews
    @Composable
    fun statsLargeFont() {
        AccountStatsRowSample()
    }
}
