package com.binge.designsystem.catalog

import androidx.compose.runtime.Composable
import com.android.tools.screenshot.PreviewTest
import com.binge.designsystem.preview.ComponentPreviews

class SaveStatusLineSamplesScreenshotTest {
    @PreviewTest
    @ComponentPreviews
    @Composable
    fun Saving() {
        SaveStatusLineSavingSample()
    }

    @PreviewTest
    @ComponentPreviews
    @Composable
    fun Saved() {
        SaveStatusLineSavedSample()
    }

    @PreviewTest
    @ComponentPreviews
    @Composable
    fun Failed() {
        SaveStatusLineFailedSample()
    }
}
