package com.binge.designsystem.catalog

import androidx.compose.runtime.Composable
import com.android.tools.screenshot.PreviewTest
import com.binge.designsystem.preview.ComponentPreviews

/**
 * Screenshot coverage for the include/exclude-chip catalog sample — renders the shared
 * [IncludeExcludeChipStatesSample] (all three states) (#745, #749).
 */
class IncludeExcludeChipSamplesScreenshotTest {
    @PreviewTest
    @ComponentPreviews
    @Composable
    fun States() {
        IncludeExcludeChipStatesSample()
    }
}
