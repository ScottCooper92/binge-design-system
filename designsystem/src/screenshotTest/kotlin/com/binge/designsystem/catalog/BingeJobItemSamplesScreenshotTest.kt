package com.binge.designsystem.catalog

import androidx.compose.runtime.Composable
import com.android.tools.screenshot.PreviewTest
import com.binge.designsystem.preview.ComponentPreviews
import com.binge.designsystem.preview.WorstCaseFontPreviews

/** Screenshot coverage for [BingeJobItemSample], at the default and the large fonts. */
class BingeJobItemSamplesScreenshotTest {
    @PreviewTest
    @ComponentPreviews
    @WorstCaseFontPreviews
    @Composable
    fun rows() = BingeJobItemSample()
}
