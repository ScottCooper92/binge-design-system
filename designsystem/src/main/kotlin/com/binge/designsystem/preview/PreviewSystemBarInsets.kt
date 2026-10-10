package com.binge.designsystem.preview

import android.view.View
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.statusBars
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.graphics.Insets
import androidx.core.view.WindowInsetsCompat

/** A 24dp status bar — the height most phones report. */
private val PreviewStatusBarHeight = 24.dp

/** A 48dp navigation bar — three-button navigation, the tallest a phone reports at the bottom. */
private val PreviewNavigationBarHeight = 48.dp

/**
 * Renders [content] under a real-sized status bar and navigation bar, so a screenshot frame exercises
 * the padding a screen reserves from `WindowInsets.statusBars` / `navigationBars` — which the preview
 * renderer otherwise reports as zero. Screen code keeps reading the standard `WindowInsets` APIs;
 * nothing here is visible to it.
 *
 * Compose reads insets from a `WindowInsetsHolder` keyed on `LocalView`, fed by an
 * `OnApplyWindowInsetsListener` it installs on that view once something reads an inset. The layoutlib
 * renderer does dispatch insets to it — all zero — and neither `@Preview(showSystemUi = true)` nor a
 * real device id changes that: both render the frame byte-identical to the plain spec, with no system
 * bars drawn. This wrapper makes the renderer deliver real-sized insets instead, with three steps,
 * each measured necessary:
 *
 * 1. **Read the insets before the effect.** The holder's listener is installed by the first reader's
 *    `DisposableEffect`; reading `statusBars`/`navigationBars` at the top of the wrapper puts that
 *    effect ahead of ours, so the listener is there when we dispatch.
 * 2. **Answer at the parent, not the Compose view.** Dispatching a synthetic `WindowInsetsCompat` to
 *    the Compose view does reach the holder, but the renderer's own all-zero dispatch arrives after
 *    the effect and overwrites it before measure. A `ViewGroup` hands its children whatever its
 *    listener returns, so a listener on the parent `ComposeView` returning the synthetic insets makes
 *    *every* dispatch — ours and the renderer's — deliver them.
 * 3. **Compose the content in a `SubcomposeLayout`.** The renderer captures the first frame only: a
 *    state write from an effect never shows up in the PNG. Effects run when the composition is
 *    applied, and a `SubcomposeLayout` composes its content during measure, afterwards — so the
 *    content's first and only composition already sees the synthetic insets.
 *
 * The wrapper then `check`s, at measure, that the insets the content will read are the ones asked
 * for, so a Compose or renderer upgrade that breaks any step fails the frame loudly instead of
 * quietly re-recording a zero-inset baseline.
 *
 * It is opt-in rather than applied by the shared multipreview annotations in this file's own package:
 * nearly every `@ScreenPreviews`/`@ScreenStatePreview` frame has a top bar reading the status-bar
 * inset, so defaulting it on would re-record every one of them at once.
 */
@Composable
fun PreviewSystemBarInsets(
    statusBar: Dp = PreviewStatusBarHeight,
    navigationBar: Dp = PreviewNavigationBarHeight,
    content: @Composable () -> Unit,
) {
    val view = LocalView.current
    val density = LocalDensity.current
    val insets =
        remember(density, statusBar, navigationBar) {
            with(density) {
                WindowInsetsCompat
                    .Builder()
                    .setInsets(WindowInsetsCompat.Type.statusBars(), Insets.of(0, statusBar.roundToPx(), 0, 0))
                    .setInsets(WindowInsetsCompat.Type.navigationBars(), Insets.of(0, 0, 0, navigationBar.roundToPx()))
                    .build()
            }
        }
    // Reading these before the effect keeps Compose's own insets listener installed on `view` when the
    // effect dispatches, and gives the check below something to verify against.
    val statusBars = WindowInsets.statusBars
    val navigationBars = WindowInsets.navigationBars
    DisposableEffect(view, insets) {
        val host = checkNotNull(view.parent as? View) { "PreviewSystemBarInsets needs a hosted Compose view" }
        val platformInsets = checkNotNull(insets.toWindowInsets())
        // The renderer dispatches its own all-zero insets after this effect; answering every dispatch
        // at the parent means the Compose view only ever receives these.
        host.setOnApplyWindowInsetsListener { _, _ -> platformInsets }
        host.dispatchApplyWindowInsets(platformInsets)
        onDispose { host.setOnApplyWindowInsetsListener(null) }
    }
    // Composed during measure, i.e. after the effect above: the renderer captures the first frame only,
    // so content composed alongside the effect would keep the zero insets it read first.
    SubcomposeLayout { constraints ->
        check(statusBars.getTop(this) == statusBar.roundToPx() && navigationBars.getBottom(this) == navigationBar.roundToPx()) {
            "PreviewSystemBarInsets: synthetic insets did not reach the content"
        }
        val placeables = subcompose(Unit, content).map { it.measure(constraints) }
        val width = placeables.maxOfOrNull { it.width } ?: constraints.minWidth
        val height = placeables.maxOfOrNull { it.height } ?: constraints.minHeight
        layout(width, height) { placeables.forEach { it.place(0, 0) } }
    }
}
