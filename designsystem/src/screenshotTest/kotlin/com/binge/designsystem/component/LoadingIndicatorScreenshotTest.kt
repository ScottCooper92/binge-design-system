package com.binge.designsystem.component

import androidx.compose.runtime.Composable
import com.android.tools.screenshot.PreviewTest
import com.binge.designsystem.preview.ComponentPreviews
import com.binge.designsystem.preview.ScreenshotTheme

/**
 * Atomic circular loading indicator tinted with the primary colour. Only the colour axis
 * matters, so it runs at the standard width in light + dark.
 */
class LoadingIndicatorScreenshotTest {
    @PreviewTest
    @ComponentPreviews
    @Composable
    fun Default() {
        ScreenshotTheme {
            BingeLoadingIndicator()
        }
    }
}
