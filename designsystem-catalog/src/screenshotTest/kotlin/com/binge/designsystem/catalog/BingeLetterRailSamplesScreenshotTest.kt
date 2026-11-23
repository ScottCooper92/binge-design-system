package com.binge.designsystem.catalog

import androidx.compose.runtime.Composable
import com.android.tools.screenshot.PreviewTest
import com.binge.designsystem.preview.ComponentPreviews

class BingeLetterRailSamplesScreenshotTest {
    @PreviewTest
    @ComponentPreviews
    @Composable
    fun Rail() {
        BingeLetterRailSample()
    }

    /** A rail too short for every letter: alternate letters with a dot between. */
    @PreviewTest
    @ComponentPreviews
    @Composable
    fun ShortRail() {
        BingeLetterRailShortSample()
    }
}
