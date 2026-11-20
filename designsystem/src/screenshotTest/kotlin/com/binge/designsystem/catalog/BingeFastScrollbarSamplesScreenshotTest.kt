package com.binge.designsystem.catalog

import androidx.compose.runtime.Composable
import com.android.tools.screenshot.PreviewTest
import com.binge.designsystem.preview.ComponentPreviews
import com.binge.designsystem.preview.WorstCaseFontPreviews

class BingeFastScrollbarSamplesScreenshotTest {
    /** The thumb held on a sectioned grid, with the department in the bubble, light and dark. */
    @PreviewTest
    @ComponentPreviews
    @Composable
    fun Grid() {
        BingeFastScrollbarSample()
    }

    /** The same at the worst font scales: the bubble stays on one line and ends in an ellipsis rather than clip. */
    @PreviewTest
    @WorstCaseFontPreviews
    @Composable
    fun GridLargeFont() {
        BingeFastScrollbarSample()
    }

    /** The thumb held on a plain list, with no bubble. */
    @PreviewTest
    @ComponentPreviews
    @Composable
    fun List() {
        BingeFastScrollbarListSample()
    }
}
