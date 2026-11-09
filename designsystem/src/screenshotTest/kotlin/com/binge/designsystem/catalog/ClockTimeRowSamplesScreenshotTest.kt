package com.binge.designsystem.catalog

import androidx.compose.runtime.Composable
import com.android.tools.screenshot.PreviewTest
import com.binge.designsystem.preview.ComponentPreviews
import com.binge.designsystem.preview.WorstCaseFontPreviews

class ClockTimeRowSamplesScreenshotTest {
    @PreviewTest
    @ComponentPreviews
    @WorstCaseFontPreviews
    @Composable
    fun Clock12() = ClockTimeRow12Sample()

    @PreviewTest
    @ComponentPreviews
    @Composable
    fun Clock24() = ClockTimeRow24Sample()
}
