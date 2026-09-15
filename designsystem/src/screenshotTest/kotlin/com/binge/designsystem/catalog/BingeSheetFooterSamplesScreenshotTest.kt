package com.binge.designsystem.catalog

import androidx.compose.runtime.Composable
import com.android.tools.screenshot.PreviewTest
import com.binge.designsystem.preview.ComponentPreviews

/**
 * Screenshot coverage for the containers / chrome catalog samples. Each renders the shared
 * `…Sample()`, the component's one public fixture. See the convention KDoc on [MediaCardRatedSample].
 */
class BingeSheetFooterSamplesScreenshotTest {
    @PreviewTest
    @ComponentPreviews
    @Composable
    fun Footer() {
        BingeSheetFooterSample()
    }

    /** The band's absence is the thing under test, so it needs its own frame beside [Footer]. */
    @PreviewTest
    @ComponentPreviews
    @Composable
    fun FooterOnPage() {
        BingeSheetFooterOnPageSample()
    }
}
