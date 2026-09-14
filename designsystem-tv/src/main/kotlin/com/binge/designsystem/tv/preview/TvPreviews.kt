package com.binge.designsystem.tv.preview

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Surface
import androidx.tv.material3.SurfaceDefaults
import com.binge.designsystem.tv.theme.BingeTvTheme

/*
 * TV preview harness. Kept separate from the phone annotations in
 * core/designsystem/.../preview/DevicePreviews.kt on purpose: adding a TV cell to @ScreenPreviews
 * would re-render every existing screen baseline (~950 PNGs) for no signal.
 *
 * Preview `name`s stay short, lowercase and space-free — the screenshot plugin bakes them into
 * reference filenames, and long ones blow past Windows' MAX_PATH under a worktree.
 */

/** 1920x1080 at xhdpi, the reference Android TV panel. */
const val TV_PREVIEW_WIDTH_DP = 960
const val TV_PREVIEW_HEIGHT_DP = 540

/**
 * The TV screen matrix — a single cell, because TV has one device class and one theme. Stack it on a
 * `fillMaxSize` composable; the canvas arrives with the annotation, so a frame need not wrap one by hand.
 *
 * **This one bakes [TvScreenshotTheme] — the theme's `background`.** Reach for it for components and for
 * any screen already known to paint its own base. Its pair, [TvPreviewsOnBlack], bakes the window's black
 * instead, and the choice between them is load-bearing rather than cosmetic: see that annotation.
 *
 * Wrapping the theme anyway is harmless — the nested Surface resolves to the same colour — so hand-written
 * `TvScreenshotTheme { … }` frames render identically and can lose the wrapper as they are touched. Mixing
 * the *other* canvas in by hand is not: an inner [TvScreenshotThemeOnBlack] only covers this canvas where
 * it fills, so use [TvPreviewsOnBlack] rather than nesting the two.
 *
 * A screen-level harness extends this one; foundation components use it as-is.
 */
@PreviewWrapper(TvScreenshotThemeWrapper::class)
@Preview(
    name = "tv",
    device = "spec:width=${TV_PREVIEW_WIDTH_DP}dp,height=${TV_PREVIEW_HEIGHT_DP}dp,orientation=landscape",
    uiMode = UI_MODE_NIGHT_YES,
)
annotation class TvPreviews

/**
 * [TvPreviews]' pair, baking [TvScreenshotThemeOnBlack] — the **window's** black canvas — for a
 * **screen-root** frame.
 *
 * Two annotations rather than one baked default because the canvas is a test, not a backdrop: a screen
 * that paints its own base renders identically either way, so a baseline that moves between them names a
 * screen relying on the harness to paint for it. Baking one canvas everywhere would make that
 * question unaskable for every frame that takes the annotation; keeping the pair keeps it one token away.
 *
 * So: a screen root goes here, a component goes on [TvPreviews], and a screen you are auditing moves from
 * one to the other to see whether anything changes.
 */
@PreviewWrapper(TvScreenshotThemeOnBlackWrapper::class)
@Preview(
    name = "tv",
    device = "spec:width=${TV_PREVIEW_WIDTH_DP}dp,height=${TV_PREVIEW_HEIGHT_DP}dp,orientation=landscape",
    uiMode = UI_MODE_NIGHT_YES,
)
annotation class TvPreviewsOnBlack

/**
 * Theme wrapper for TV previews and screenshot tests — the TV counterpart of `ScreenshotTheme`.
 *
 * `reduceMotion = true` collapses the focus-scale animation to a snap so a captured frame is
 * stable. The theme is dark-only, so unlike the phone harness there is no `uiMode` axis to drive.
 *
 * The canvas is painted with **`background`, not `surface`**. tv-material's `Surface` defaults to
 * `surface`, which is the same token a card paints itself with — using it here would render cards
 * invisible against their own backdrop, and the baseline would happily lock that in.
 *
 * That painted canvas is also a **blind spot**, which is why [TvScreenshotThemeOnBlack] exists: it makes
 * every frame render on `background` whether or not the composable under test paints anything, so a screen
 * that forgets its own base still looks right here while shipping on the window's black. Component
 * frames want this readable canvas; a **screen-root** frame should use the other one.
 */
@Composable
fun TvScreenshotTheme(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    TvScreenshotCanvas(modifier = modifier, canvas = null, content = content)
}

/**
 * The harness for a **screen-root** frame: the canvas is the *window's* colour, not the theme's.
 *
 * `app/src/main/res/values/themes.xml` sets `android:windowBackground` to `@android:color/black`, while
 * `background` is #0E0E0F — so a screen that fails to paint its own base ships on black and renders on
 * #0E0E0F in every baseline. The two are close enough to read the same by eye and different enough that
 * the frame is not the truth.
 *
 * Using this, a screen that paints its base renders **identically** — the fill covers the canvas either
 * way — so adopting it is safe, and any baseline that *does* move is a screen that was relying on the
 * harness. That is the whole test: it cannot be asserted, only rendered.
 */
@Composable
fun TvScreenshotThemeOnBlack(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    TvScreenshotCanvas(modifier = modifier, canvas = Color.Black, content = content)
}

@Composable
private fun TvScreenshotCanvas(
    canvas: Color?,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    BingeTvTheme(reduceMotion = true) {
        Surface(
            modifier = modifier,
            colors = SurfaceDefaults.colors(
                // Resolved *inside* BingeTvTheme: read outside it, `background` is tv-material's default
                // rather than Binge's, which silently repaints every frame.
                containerColor = canvas ?: MaterialTheme.colorScheme.background,
                contentColor = MaterialTheme.colorScheme.onBackground,
            ),
        ) {
            content()
        }
    }
}
