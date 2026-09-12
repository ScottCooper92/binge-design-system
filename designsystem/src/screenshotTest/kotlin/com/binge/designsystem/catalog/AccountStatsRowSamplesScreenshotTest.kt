package com.binge.designsystem.catalog

import androidx.compose.runtime.Composable
import com.android.tools.screenshot.PreviewTest
import com.binge.designsystem.preview.ComponentPreviews

/** Screenshot coverage for the [AccountStatsRow] catalog sample (#751). */
class AccountStatsRowSamplesScreenshotTest {
    @PreviewTest
    @ComponentPreviews
    @Composable
    fun stats() {
        AccountStatsRowSample()
    }
}
