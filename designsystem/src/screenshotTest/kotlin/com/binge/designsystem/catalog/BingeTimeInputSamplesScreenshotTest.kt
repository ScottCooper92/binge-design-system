package com.binge.designsystem.catalog

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.android.tools.screenshot.PreviewTest
import com.binge.designsystem.preview.ComponentPreviews
import com.binge.designsystem.preview.STANDARD_PHONE_WIDTH_DP
import com.binge.designsystem.preview.WorstCaseFontPreviews

class BingeTimeInputSamplesScreenshotTest {
    @PreviewTest
    @ComponentPreviews
    @WorstCaseFontPreviews
    @Preview(name = "es", widthDp = STANDARD_PHONE_WIDTH_DP, locale = "es")
    @Composable
    fun Clock24() = BingeTimeInput24Sample()

    @PreviewTest
    @ComponentPreviews
    @WorstCaseFontPreviews
    @Preview(name = "es", widthDp = STANDARD_PHONE_WIDTH_DP, locale = "es")
    @Composable
    fun Clock12() = BingeTimeInput12Sample()

    @PreviewTest
    @ComponentPreviews
    @Composable
    fun Invalid() = BingeTimeInputInvalidSample()

    @PreviewTest
    @ComponentPreviews
    @Composable
    fun Disabled() = BingeTimeInputDisabledSample()
}
