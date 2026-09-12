package com.binge.designsystem.catalog

import androidx.compose.runtime.Composable
import com.android.tools.screenshot.PreviewTest
import com.binge.designsystem.preview.ComponentPreviews

class SectionHeaderSamplesScreenshotTest {
    @PreviewTest
    @ComponentPreviews
    @Composable
    fun WithMore() {
        SectionHeaderWithMoreSample()
    }

    @PreviewTest
    @ComponentPreviews
    @Composable
    fun TitleOnly() {
        SectionHeaderTitleOnlySample()
    }
}
