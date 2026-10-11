package com.binge.designsystem.catalog

import androidx.compose.runtime.Composable
import com.android.tools.screenshot.PreviewTest
import com.binge.designsystem.preview.ComponentPreviews

class BingeTopBarSamplesScreenshotTest {
    @PreviewTest
    @ComponentPreviews
    @Composable
    fun Standard() {
        BingeTopBarSample()
    }

    @PreviewTest
    @ComponentPreviews
    @Composable
    fun Transparent() {
        BingeTopBarTransparentSample()
    }

    @PreviewTest
    @ComponentPreviews
    @Composable
    fun ThemeFollowingScrim() {
        BingeTopBarThemeFollowingScrimSample()
    }

    @PreviewTest
    @ComponentPreviews
    @Composable
    fun Scrimmed() {
        BingeTopBarScrimmedSample()
    }

    @PreviewTest
    @ComponentPreviews
    @Composable
    fun TitleBeforeScrimSwitch() {
        BingeTopBarTitleBeforeScrimSwitchSample()
    }

    @PreviewTest
    @ComponentPreviews
    @Composable
    fun TitleAfterScrimSwitch() {
        BingeTopBarTitleAfterScrimSwitchSample()
    }

    @PreviewTest
    @ComponentPreviews
    @Composable
    fun ScrimTailFade() {
        BingeTopBarScrimTailFadeSample()
    }
}
