package com.binge.designsystem

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.union
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.unit.Dp

/**
 * The width of the pane a screen is actually rendered in, when it is one of several panes sharing
 * the window — null (the default) for a screen that has the whole window to itself. A list-detail
 * scene provides this around each pane's content; [resolvedContentInset] is what reads it.
 */
val LocalPaneWidth = staticCompositionLocalOf<Dp?> { null }

/**
 * The edge, or edges, a pane shares with another, in layout-direction terms so it mirrors under RTL along with the
 * `Row` it sits in. [Both] is the middle pane of three side by side, which borders a pane on each side.
 */
enum class PaneEdge {
    Start,
    End,
    Both,
    ;

    internal val sharesStart: Boolean get() = this == Start || this == Both
    internal val sharesEnd: Boolean get() = this == End || this == Both
}

/**
 * The edge of this pane that borders another pane on screen — null (the default) for a screen alone
 * in the window, or a pane whose scene is showing one pane at a time. [PaneContent] provides it;
 * [resolvedContentPadding] is what reads it.
 */
val LocalPaneInnerEdge = staticCompositionLocalOf<PaneEdge?> { null }

/**
 * Whether this screen shares the window with another pane — true for both panes of a list-detail
 * scene showing side by side, false for a screen alone in the window or a single-pane scene. Reads
 * [LocalPaneInnerEdge], so it answers for the pane as laid out, not for the device.
 */
@Composable
fun hasPaneBeside(): Boolean = LocalPaneInnerEdge.current != null

/**
 * A screen's own side padding, resolved against the width it is actually rendered at rather than
 * the window's. [R.dimen.screen_content_inset] ramps with the window through resource qualifiers —
 * 16 / 24 / 32dp at 0 / 600 / 840dp — which is right for a screen alone in the window and wrong for
 * one beside another pane, a fraction of that window. [LocalPaneWidth] null falls back to the window
 * value unchanged; set, the same three-step ramp applies to the pane's own width instead.
 *
 * It is symmetric: the same value on both sides, and no knowledge of any pane beside this one. A body that
 * may sit in a pane beside another wants less on the edge they share, so it reads [resolvedContentPadding]
 * instead, which gives each side its own value. Reach for this one only where a single [Dp] is genuinely
 * wanted, such as the width of a centred column.
 */
@Composable
fun resolvedContentInset(): Dp {
    val paneWidth = LocalPaneWidth.current ?: return dimensionResource(R.dimen.screen_content_inset)
    val mediumBreakpoint = dimensionResource(R.dimen.content_inset_medium_breakpoint)
    val expandedBreakpoint = dimensionResource(R.dimen.content_inset_expanded_breakpoint)
    return when {
        paneWidth >= expandedBreakpoint -> dimensionResource(R.dimen.content_inset_expanded)
        paneWidth >= mediumBreakpoint -> dimensionResource(R.dimen.content_inset_medium)
        else -> dimensionResource(R.dimen.content_inset_compact)
    }
}

/**
 * A screen's side padding as [PaddingValues]: [resolvedContentInset] on every edge except the one
 * [LocalPaneInnerEdge] names, which gets [R.dimen.pane_inner_inset] — the window-edge ramp exists to
 * clear a bezel, and the edge beside another pane has none to clear. With no inner edge (a screen
 * alone in the window, or a single-pane scene) both sides are [resolvedContentInset], exactly as
 * before, so a screen can read this unconditionally without knowing how many panes are showing.
 *
 * This is the one to read for a body that may sit in a pane; [resolvedContentInset] is the symmetric single
 * value and keeps the full window inset on the shared edge. [top] and [bottom] pad those edges. [vertical]
 * adds the same amount to both, so a block with the inset above and below reads
 * `padding(resolvedContentPadding(vertical = inset))`.
 */
@Composable
fun resolvedContentPadding(
    top: Dp = dimensionResource(R.dimen.zero),
    bottom: Dp = dimensionResource(R.dimen.zero),
    vertical: Dp = dimensionResource(R.dimen.zero),
): PaddingValues =
    paneContentPadding(
        innerEdge = LocalPaneInnerEdge.current,
        outer = resolvedContentInset(),
        inner = dimensionResource(R.dimen.pane_inner_inset),
        top = top + vertical,
        bottom = bottom + vertical,
    )

/** [resolvedContentPadding]'s arithmetic, apart from the composition locals it reads. */
internal fun paneContentPadding(
    innerEdge: PaneEdge?,
    outer: Dp,
    inner: Dp,
    top: Dp,
    bottom: Dp,
): PaddingValues =
    PaddingValues(
        start = if (innerEdge?.sharesStart == true) inner else outer,
        top = top,
        end = if (innerEdge?.sharesEnd == true) inner else outer,
        bottom = bottom,
    )

/**
 * The window's horizontal system-bar and display-cutout insets that this pane has to clear. A screen
 * alone in the window clears both sides. A pane beside another clears only its outer side: its inner
 * edge sits mid-window, nowhere near a cutout or a curved screen edge.
 *
 * Apply it with `Modifier.windowInsetsPadding`, or exclude what an ancestor already consumed, so a
 * screen whose scaffold has already reserved these insets does not reserve them twice.
 */
@Composable
fun paneSideInsets(): WindowInsets = paneSideInsets(WindowInsets.systemBars.union(WindowInsets.displayCutout), LocalPaneInnerEdge.current)

/**
 * The side insets a top bar clears: [paneSideInsets], and with no pane beside it the rail's start inset too, so the bar
 * starts where the body below it does, the larger of the two on that side. Beside a pane the bar offsets its own title
 * by the overlay, so it is not added here a second time.
 */
@Composable
fun topBarSideInsets(): WindowInsets {
    val sides = paneSideInsets()
    return if (hasPaneBeside()) sides else sides.union(navOverlayStartInsets())
}

/**
 * What a bar in a `bottomBar` slot clears: the navigation bar below it, and the side insets on the pane's outer
 * edges. The slot sits outside the scaffold's padded body, so nothing above the bar has cleared a side cutout. Pad a
 * consumer's own `bottomBar` content by this, with `Modifier.windowInsetsPadding`, rather than `navigationBarsPadding()`.
 */
@Composable
fun bottomBarInsets(): WindowInsets = WindowInsets.navigationBars.only(WindowInsetsSides.Bottom).union(paneSideInsets())

/** [paneSideInsets]'s side selection, apart from the window and the composition locals it reads. */
internal fun paneSideInsets(windowInsets: WindowInsets, innerEdge: PaneEdge?): WindowInsets =
    when (innerEdge) {
        null -> windowInsets.only(WindowInsetsSides.Horizontal)
        PaneEdge.Start -> windowInsets.only(WindowInsetsSides.End)
        PaneEdge.End -> windowInsets.only(WindowInsetsSides.Start)
        // A middle pane has no outer edge: both of its sides sit mid-window.
        PaneEdge.Both -> WindowInsets(0)
    }
