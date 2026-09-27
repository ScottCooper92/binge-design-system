package com.binge.designsystem

import androidx.compose.foundation.layout.PaddingValues
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

/** A pane's horizontal edge, in layout-direction terms so it mirrors under RTL along with the `Row` it sits in. */
enum class PaneEdge { Start, End }

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
 * The same value on both sides. A pane beside another wants less on the edge they share — read
 * [resolvedContentPadding] instead wherever the two sides can differ.
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
 */
@Composable
fun resolvedContentPadding(top: Dp = dimensionResource(R.dimen.zero), bottom: Dp = dimensionResource(R.dimen.zero)): PaddingValues =
    paneContentPadding(
        innerEdge = LocalPaneInnerEdge.current,
        outer = resolvedContentInset(),
        inner = dimensionResource(R.dimen.pane_inner_inset),
        top = top,
        bottom = bottom,
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
        start = if (innerEdge == PaneEdge.Start) inner else outer,
        top = top,
        end = if (innerEdge == PaneEdge.End) inner else outer,
        bottom = bottom,
    )
