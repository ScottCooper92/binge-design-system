package com.binge.designsystem.tv.preview

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewWrapperProvider
import com.binge.designsystem.theme.BingeBrand
import com.binge.designsystem.tv.theme.toTvColorScheme

/*
 * The two canvases a TV multipreview can bake, one provider each — see [TvPreviews] /
 * [TvPreviewsOnBlack] for which to reach for and why the pair exists.
 *
 * Both fill the frame, matching what every hand-written call site already passes: the point of baking
 * the wrapper is that a test can stop wrapping by hand, and a wrap-content canvas would size to the
 * content instead of the 960×540 panel the moment it did.
 *
 * Both are open, as the phone's `ScreenshotThemeWrapper` is, so an app with its own brand subclasses one, overrides
 * `brand`, and binds the subclass on annotations of its own built from [TV_PREVIEW_SPEC]. The TV theme is dark-only,
 * so a brand reaches it as its dark scheme, projected the same way `BingeTvColorScheme` is.
 */

/** Applies [TvScreenshotTheme] — the theme's `background` canvas — to every cell of [TvPreviews]. */
open class TvScreenshotThemeWrapper : PreviewWrapperProvider {
    /** The brand every cell renders in. Binge's by default. */
    open val brand: BingeBrand get() = BingeBrand.Binge

    @Composable
    override fun Wrap(content: @Composable () -> Unit) {
        TvScreenshotTheme(modifier = Modifier.fillMaxSize(), colorScheme = brand.dark.toTvColorScheme()) {
            content()
        }
    }
}

/** Applies [TvScreenshotThemeOnBlack] — the window's black canvas — to every cell of [TvPreviewsOnBlack]. */
open class TvScreenshotThemeOnBlackWrapper : PreviewWrapperProvider {
    /** The brand every cell renders in. Binge's by default. */
    open val brand: BingeBrand get() = BingeBrand.Binge

    @Composable
    override fun Wrap(content: @Composable () -> Unit) {
        TvScreenshotThemeOnBlack(modifier = Modifier.fillMaxSize(), colorScheme = brand.dark.toTvColorScheme()) {
            content()
        }
    }
}
