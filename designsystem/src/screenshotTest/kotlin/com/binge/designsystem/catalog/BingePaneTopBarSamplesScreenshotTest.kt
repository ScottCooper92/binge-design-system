package com.binge.designsystem.catalog

import androidx.compose.runtime.Composable
import com.android.tools.screenshot.PreviewTest
import com.binge.designsystem.preview.ComponentPreviews

class BingePaneTopBarSamplesScreenshotTest {
    @PreviewTest
    @ComponentPreviews
    @Composable
    fun Alone() {
        BingePaneTopBarAloneSample()
    }

    @PreviewTest
    @ComponentPreviews
    @Composable
    fun AloneScrolled() {
        BingePaneTopBarAloneScrolledSample()
    }

    @PreviewTest
    @ComponentPreviews
    @Composable
    fun ListPane() {
        BingePaneTopBarListPaneSample()
    }

    @PreviewTest
    @ComponentPreviews
    @Composable
    fun DetailPane() {
        BingePaneTopBarDetailPaneSample()
    }
}
