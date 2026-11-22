package com.binge.designsystem.catalog

import androidx.compose.runtime.Composable
import com.android.tools.screenshot.PreviewTest
import com.binge.designsystem.preview.ScreenStatePreview
import com.binge.designsystem.preview.ScreenWidthPreviews

/** Screenshot coverage for [com.binge.designsystem.template.CreateFooter]: at rest, disabled and under the system bars. The in-flight spinner is an animation a frame cannot hold still, so the tests cover it. */
class CreateFooterSamplesScreenshotTest {
    @PreviewTest
    @ScreenWidthPreviews
    @Composable
    fun atRest() = CreateFooterSample()

    @PreviewTest
    @ScreenStatePreview
    @Composable
    fun disabled() = CreateFooterDisabledSample()

    @PreviewTest
    @ScreenStatePreview
    @Composable
    fun underSystemBars() = CreateFooterUnderSystemBarsSample()
}
