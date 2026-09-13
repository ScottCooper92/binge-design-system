package com.binge.designsystem.tv.nav

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.binge.designsystem.tv.R as TvR

/*
 * The rail-as-overlay contract. The rail's content pane is measured at the FULL panel width and the rail is
 * painted over it behind a scrim, so any surface is full-bleed by default — artwork and lazy rows simply reach
 * the panel edge. What must stay CLEAR of the rail opts out via padding built on [LocalTvContentInset], rather
 * than surfaces that want to bleed opting in via layout tricks (the inverted model this replaced widened each
 * bleeding surface past its constraints by hand).
 */

/**
 * The start inset in-pane content clears the rail by — the rail's collapsed width where a rail hosts the
 * content, and `0` everywhere else (an overlay-hosted screen has no rail beside it, and a preview renders the
 * surface alone). Because the default is zero, only surfaces that can sit beside the rail consume it, and their
 * previews and overlay hostings are automatically un-inset.
 */
val LocalTvContentInset = compositionLocalOf { 0.dp }

/**
 * The pane's animated rightward translation while the rail expands. A backdrop cancels it in the draw phase
 * ([TvImmersiveBackdrop]'s artwork layer) so the art holds still on screen while the rows and copy slide over
 * it. A [State] rather than a raw value so consumers read it inside `graphicsLayer` and only the draw phase
 * invalidates per frame.
 */
val LocalTvPaneShift = staticCompositionLocalOf<State<Dp>> { mutableStateOf(0.dp) }

/**
 * The start inset plus the standard content gutter — what an in-pane surface's first column of content clears
 * the panel edge by. Reads as the old `tv_content_gutter_start` did when the pane itself was inset; overlay
 * hostings and previews resolve the inset to zero and keep the bare gutter.
 */
@Composable
fun tvContentGutterStart(): Dp = LocalTvContentInset.current + dimensionResource(TvR.dimen.tv_content_gutter_start)

/**
 * Whether any artwork is currently full-bleed behind the rail, so the rail knows when to be **glass** and when
 * to be a solid panel.
 *
 * The rail is only translucent where there is something to see through it to. Left permanently translucent, the
 * hub's 320ms hero↔backdrop crossfade plays out *inside the rail* every time focus crosses between the hero and
 * a row — the rail appears to flicker. Tracking presence lets the rail fade its own fill in step with that
 * crossfade instead: solid while the hero (or a flat screen such as Search or Settings) is up, glass once a
 * backdrop is behind it.
 *
 * A count rather than a flag because the crossfade composes the outgoing and incoming backdrops together — a
 * flag would be cleared by the outgoing one's disposal just as the incoming one arrives, flicking the rail solid
 * for a frame.
 */
@Stable
class TvRailArtworkPresence {
    private var sources by mutableIntStateOf(0)

    /** True while at least one backdrop is full-bleed behind the rail. */
    val isPresent: Boolean get() = sources > 0

    internal fun acquire() {
        sources++
    }

    internal fun release() {
        sources = (sources - 1).coerceAtLeast(0)
    }
}

/** Null wherever no rail hosts the content — an overlay-hosted screen, or a preview rendering a surface alone. */
val LocalTvRailArtwork = compositionLocalOf<TvRailArtworkPresence?> { null }

/**
 * Registers this composition as painting artwork behind the rail for as long as it stays composed — the signal
 * [TvRailArtworkPresence] counts. Call it only where a backdrop genuinely has something to paint.
 */
@Composable
fun ReportTvRailArtwork() {
    val presence = LocalTvRailArtwork.current ?: return
    DisposableEffect(presence) {
        presence.acquire()
        onDispose { presence.release() }
    }
}
