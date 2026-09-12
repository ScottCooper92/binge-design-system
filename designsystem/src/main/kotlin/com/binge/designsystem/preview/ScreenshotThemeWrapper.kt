package com.binge.designsystem.preview

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.PreviewWrapperProvider

/** Applies [ScreenshotTheme] to every cell of the multipreview that carries it. */
class ScreenshotThemeWrapper : PreviewWrapperProvider {
    @Composable
    override fun Wrap(content: @Composable () -> Unit) {
        ScreenshotTheme {
            content()
        }
    }
}
