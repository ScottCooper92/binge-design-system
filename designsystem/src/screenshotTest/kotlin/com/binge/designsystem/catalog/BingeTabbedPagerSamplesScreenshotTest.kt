package com.binge.designsystem.catalog

import androidx.compose.runtime.Composable
import com.android.tools.screenshot.PreviewTest
import com.binge.designsystem.preview.ComponentPreviews
import com.binge.designsystem.preview.WorstCaseFontPreviews

/** Screenshot coverage for the tabbed pager: light and dark at 1x, and the same pager at 1.3x and 2x font. */
class BingeTabbedPagerSamplesScreenshotTest {
    @PreviewTest
    @ComponentPreviews
    @Composable
    fun Pager() {
        BingeTabbedPagerSample()
    }

    /** Five scrolling tabs at 1.3x and 2x font: the row scrolls and every label stays whole. */
    @PreviewTest
    @WorstCaseFontPreviews
    @Composable
    fun ManyTabsLargeFont() {
        BingeTabbedPagerManyTabsSample()
    }
}
