package com.binge.designsystem.catalog

import androidx.compose.runtime.Composable
import com.android.tools.screenshot.PreviewTest
import com.binge.designsystem.preview.ComponentPreviews

class BingeNumberSliderSamplesScreenshotTest {
    @PreviewTest
    @ComponentPreviews
    @Composable
    fun Value() {
        BingeNumberSliderValueSample()
    }

    @PreviewTest
    @ComponentPreviews
    @Composable
    fun OpenEnd() {
        BingeNumberSliderOpenEndSample()
    }

    @PreviewTest
    @ComponentPreviews
    @Composable
    fun Minimum() {
        BingeNumberSliderMinimumSample()
    }

    @PreviewTest
    @ComponentPreviews
    @Composable
    fun Closed() {
        BingeNumberSliderClosedSample()
    }

    @PreviewTest
    @ComponentPreviews
    @Composable
    fun Disabled() {
        BingeNumberSliderDisabledSample()
    }

    @PreviewTest
    @ComponentPreviews
    @Composable
    fun Item() {
        BingeNumberItemSample()
    }
}
