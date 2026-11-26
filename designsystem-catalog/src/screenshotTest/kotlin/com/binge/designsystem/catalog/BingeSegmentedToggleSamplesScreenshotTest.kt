package com.binge.designsystem.catalog

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.android.tools.screenshot.PreviewTest
import com.binge.designsystem.preview.WorstCaseFontPreviews

/** Screenshot coverage for [BingeSegmentedToggleSample][com.binge.designsystem.catalog.BingeSegmentedToggleSample]. */
class BingeSegmentedToggleSamplesScreenshotTest {
    @PreviewTest
    @Preview(name = "toggle", uiMode = 0)
    @Preview(name = "toggle-dark", uiMode = UI_MODE_NIGHT_YES)
    @WorstCaseFontPreviews
    @Composable
    fun Toggle() {
        BingeSegmentedToggleSample()
    }
}
