package com.binge.designsystem.catalog

import androidx.compose.runtime.Composable
import com.android.tools.screenshot.PreviewTest
import com.binge.designsystem.preview.ScreenStatePreview

/** Screenshot coverage for [SingleChoiceListScreenSample]: the headerless list on a page with its own top bar. */
class SingleChoiceListSamplesScreenshotTest {
    @PreviewTest
    @ScreenStatePreview
    @Composable
    fun inScreen() = SingleChoiceListScreenSample()
}
