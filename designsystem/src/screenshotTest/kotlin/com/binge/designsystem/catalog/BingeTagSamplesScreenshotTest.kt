package com.binge.designsystem.catalog

import androidx.compose.runtime.Composable
import com.android.tools.screenshot.PreviewTest
import com.binge.designsystem.preview.ComponentPreviews

/**
 * Screenshot coverage for the tag catalog samples — renders the shared [BingeTagNeutralSample] and
 * [BingeTagTintedSample].
 */
class BingeTagSamplesScreenshotTest {
    @PreviewTest
    @ComponentPreviews
    @Composable
    fun Neutral() {
        BingeTagNeutralSample()
    }

    @PreviewTest
    @ComponentPreviews
    @Composable
    fun Tinted() {
        BingeTagTintedSample()
    }
}
