package com.binge.designsystem.catalog

import androidx.compose.runtime.Composable
import com.android.tools.screenshot.PreviewTest
import com.binge.designsystem.preview.ComponentPreviews

class BingeMediumTopBarSamplesScreenshotTest {
    @PreviewTest
    @ComponentPreviews
    @Composable
    fun Standard() {
        BingeMediumTopBarSample()
    }

    @PreviewTest
    @ComponentPreviews
    @Composable
    fun ThemeFollowingScrim() {
        BingeMediumTopBarThemeFollowingScrimSample()
    }
}
