package com.binge.designsystem

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
 * A screen's own side padding, resolved against the width it is actually rendered at rather than
 * the window's. [R.dimen.screen_content_inset] ramps with the window through resource qualifiers —
 * 16 / 24 / 32dp at 0 / 600 / 840dp — which is right for a screen alone in the window and wrong for
 * one beside another pane, a fraction of that window. [LocalPaneWidth] null falls back to the window
 * value unchanged; set, the same three-step ramp applies to the pane's own width instead.
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
