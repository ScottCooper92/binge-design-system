package com.binge.designsystem.tv.preview

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewWrapperProvider

/*
 * The two canvases a TV multipreview can bake, one provider each — see [TvPreviews] /
 * [TvPreviewsOnBlack] for which to reach for and why the pair exists.
 *
 * Both fill the frame, matching what every hand-written call site already passes: the point of baking
 * the wrapper is that a test can stop wrapping by hand, and a wrap-content canvas would size to the
 * content instead of the 960×540 panel the moment it did.
 */

/** Applies [TvScreenshotTheme] — the theme's `background` canvas — to every cell of [TvPreviews]. */
class TvScreenshotThemeWrapper : PreviewWrapperProvider {
    @Composable
    override fun Wrap(content: @Composable () -> Unit) {
        TvScreenshotTheme(modifier = Modifier.fillMaxSize()) {
            content()
        }
    }
}

/** Applies [TvScreenshotThemeOnBlack] — the window's black canvas — to every cell of [TvPreviewsOnBlack]. */
class TvScreenshotThemeOnBlackWrapper : PreviewWrapperProvider {
    @Composable
    override fun Wrap(content: @Composable () -> Unit) {
        TvScreenshotThemeOnBlack(modifier = Modifier.fillMaxSize()) {
            content()
        }
    }
}
