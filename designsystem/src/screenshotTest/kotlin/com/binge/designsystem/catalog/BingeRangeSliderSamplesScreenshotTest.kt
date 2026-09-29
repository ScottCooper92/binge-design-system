package com.binge.designsystem.catalog

import androidx.compose.runtime.Composable
import com.android.tools.screenshot.PreviewTest
import com.binge.designsystem.preview.ComponentPreviews

class BingeRangeSliderSamplesScreenshotTest {
    @PreviewTest
    @ComponentPreviews
    @Composable
    fun Unset() {
        BingeRangeSliderUnsetSample()
    }

    @PreviewTest
    @ComponentPreviews
    @Composable
    fun MinOnly() {
        BingeRangeSliderMinOnlySample()
    }

    @PreviewTest
    @ComponentPreviews
    @Composable
    fun MaxOnly() {
        BingeRangeSliderMaxOnlySample()
    }

    @PreviewTest
    @ComponentPreviews
    @Composable
    fun Range() {
        BingeRangeSliderRangeSample()
    }

    @PreviewTest
    @ComponentPreviews
    @Composable
    fun Adjacent() {
        BingeRangeSliderAdjacentSample()
    }

    @PreviewTest
    @ComponentPreviews
    @Composable
    fun Disabled() {
        BingeRangeSliderDisabledSample()
    }
}
