package com.binge.designsystem.component

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import com.android.tools.screenshot.PreviewTest
import com.binge.designsystem.preview.ComponentPreviews
import com.binge.designsystem.preview.ScreenshotTheme

/** Standard top bar — single-row, non-collapsing title with plain nav/action glyphs when opaque. */
@OptIn(ExperimentalMaterial3Api::class)
class BingeTopBarScreenshotTest {
    @PreviewTest
    @ComponentPreviews
    @Composable
    fun TitleAndBack() {
        ScreenshotTheme {
            BingeTopBar(title = "Popular Movies", onBack = {})
        }
    }

    @PreviewTest
    @ComponentPreviews
    @Composable
    fun Scrimmed() {
        ScreenshotTheme {
            TransparentBingeTopBarSample(scrimFraction = 1f)
        }
    }
}
