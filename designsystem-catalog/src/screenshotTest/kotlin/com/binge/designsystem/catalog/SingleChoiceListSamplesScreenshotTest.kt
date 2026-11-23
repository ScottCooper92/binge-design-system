package com.binge.designsystem.catalog

import androidx.compose.runtime.Composable
import com.android.tools.screenshot.PreviewTest
import com.binge.designsystem.preview.PreviewSystemBarInsets
import com.binge.designsystem.preview.ScreenStatePreview

/** Screenshot coverage for the headerless choice list: on a page with its own top bar, and at a sheet's foot. */
class SingleChoiceListSamplesScreenshotTest {
    @PreviewTest
    @ScreenStatePreview
    @Composable
    fun inScreen() = SingleChoiceListScreenSample()

    /** The list reserves the navigation bar itself (`underNavigationBar`), which a default frame renders as zero. */
    @PreviewTest
    @ScreenStatePreview
    @Composable
    fun underNavigationBar() = PreviewSystemBarInsets { SingleChoiceListUnderNavigationBarSample() }
}
