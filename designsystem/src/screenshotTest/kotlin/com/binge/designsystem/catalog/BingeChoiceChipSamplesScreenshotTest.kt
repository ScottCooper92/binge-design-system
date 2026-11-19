package com.binge.designsystem.catalog

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.android.tools.screenshot.PreviewTest

/** Screenshot coverage for [BingeChoiceChipSample][com.binge.designsystem.catalog.BingeChoiceChipSample]. */
class BingeChoiceChipSamplesScreenshotTest {
    @PreviewTest
    @Preview(name = "choice", uiMode = 0)
    @Preview(name = "choice-dark", uiMode = UI_MODE_NIGHT_YES)
    @Composable
    fun Choice() {
        BingeChoiceChipSample()
    }
}
