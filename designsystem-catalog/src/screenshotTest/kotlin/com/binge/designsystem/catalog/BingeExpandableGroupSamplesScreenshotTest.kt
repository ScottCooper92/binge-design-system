package com.binge.designsystem.catalog

import androidx.compose.runtime.Composable
import com.android.tools.screenshot.PreviewTest
import com.binge.designsystem.preview.ComponentPreviews

/** Screenshot coverage for the [BingeExpandableGroup] catalog samples. */
class BingeExpandableGroupSamplesScreenshotTest {
    @PreviewTest
    @ComponentPreviews
    @Composable
    fun closed() {
        BingeExpandableGroupClosedSample()
    }

    @PreviewTest
    @ComponentPreviews
    @Composable
    fun open() {
        BingeExpandableGroupOpenSample()
    }

    @PreviewTest
    @ComponentPreviews
    @Composable
    fun loadingAndFailed() {
        BingeExpandableGroupStatesSample()
    }
}
