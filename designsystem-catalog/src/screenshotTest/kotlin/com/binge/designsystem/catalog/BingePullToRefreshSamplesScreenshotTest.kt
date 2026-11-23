package com.binge.designsystem.catalog

import androidx.compose.runtime.Composable
import com.android.tools.screenshot.PreviewTest
import com.binge.designsystem.preview.PreviewSystemBarInsets
import com.binge.designsystem.preview.ScreenStatePreview

/** Screenshot coverage for [BingePullToRefreshSample]: the spinner refreshing, resting below a transparent bar. */
class BingePullToRefreshSamplesScreenshotTest {
    /** Under a status bar too, so the frame shows the spinner clearing the scaffold's whole top padding. */
    @PreviewTest
    @ScreenStatePreview
    @Composable
    fun refreshingUnderBar() = PreviewSystemBarInsets { BingePullToRefreshSample() }
}
