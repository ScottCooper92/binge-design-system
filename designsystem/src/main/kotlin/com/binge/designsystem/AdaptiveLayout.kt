package com.binge.designsystem

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.booleanResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.integerResource
import androidx.compose.ui.unit.Dp

/**
 * Whether the current window is in the expanded tier (>=840dp). Drives expanded-only affordances
 * such as the cinematic detail header; compact / portrait windows stay single-column.
 */
@Composable
fun isExpandedLayout(): Boolean = booleanResource(R.bool.binge_layout_expanded)

/**
 * Whether the current window is landscape (wide-but-short). Drives layouts that place side by side
 * what portrait stacks vertically (a row of large choice cards, say). Independent of
 * [isExpandedLayout]: a landscape phone is short *and* unexpanded.
 */
@Composable
fun isLandscape(): Boolean = booleanResource(R.bool.binge_landscape)

/**
 * Whether Discover is reachable as its own navigation destination (tablets). Hubs use this to drop
 * their in-content discover-entry tile where the nav tab makes it redundant; phones keep the tile.
 */
@Composable
fun isDiscoverInNav(): Boolean = booleanResource(R.bool.binge_nav_show_discover)

/**
 * Number of columns for a poster/media grid: 2 (compact), 3 (>=600dp), 4 (>=840dp). Any width-driven
 * media grid reads it rather than hardcoding a column count.
 *
 * Resolved against [LocalPaneWidth] when the grid is one of several panes sharing the window, the
 * same width [resolvedContentInset] reads — a detail pane on a >=840dp window is not itself 840dp
 * wide, so the window-qualifier default (via `R.integer.media_grid_columns`) would hand it the
 * columns sized for the whole window rather than the fraction it actually has. [LocalPaneWidth] null
 * (a screen alone in the window) falls back to that default unchanged.
 */
@Composable
fun mediaGridColumns(): Int {
    val paneWidth = LocalPaneWidth.current ?: return integerResource(R.integer.media_grid_columns)
    val mediumBreakpoint = dimensionResource(R.dimen.content_inset_medium_breakpoint)
    val expandedBreakpoint = dimensionResource(R.dimen.content_inset_expanded_breakpoint)
    return when {
        paneWidth >= expandedBreakpoint -> integerResource(R.integer.media_grid_columns_expanded)
        paneWidth >= mediumBreakpoint -> integerResource(R.integer.media_grid_columns_medium)
        else -> integerResource(R.integer.media_grid_columns_compact)
    }
}

/**
 * Number of columns for the search results grid at the current window width: 1 (compact), 2 (>=600dp),
 * 3 (>=1240dp). Lower than [mediaGridColumns] at every tier because a result is a full `ListRow`
 * (poster + title + meta), not a poster — it needs roughly twice a poster cell's width to stay legible.
 */
@Composable
fun searchResultColumns(): Int = integerResource(R.integer.search_result_columns)

/**
 * Caps [content] at the standard reading width (640dp) and centres it, so a full-width scrolling
 * screen shows a tidy centred column on a tablet/foldable. Below the cap (phones) it is a no-op.
 *
 * The cap is applied to the [Modifier] handed to [content] so it lands on the scrolling child; the
 * centring comes from a parent `Box` because a `wrapContentWidth` cap misbehaves around a `LazyColumn`.
 */
@Composable
fun CenteredContent(
    modifier: Modifier = Modifier,
    maxWidth: Dp = dimensionResource(R.dimen.content_max_width),
    content: @Composable (Modifier) -> Unit,
) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        content(Modifier.widthIn(max = maxWidth).fillMaxSize())
    }
}

/**
 * Caps a non-lazy screen's content at [maxWidth] and centres the resulting column on a
 * tablet/foldable; below the cap (phones) it is a no-op.
 *
 * Order must stay exactly `fillMaxSize -> wrapContentWidth -> widthIn`: `wrapContentWidth` shrinks
 * and centres within the filled space, then `widthIn(max)` caps it. A `fillMaxSize`/`fillMaxWidth`
 * placed *after* `widthIn` would re-expand to the parent and defeat the cap, so callers must not
 * re-fill afterwards. Unlike [CenteredContent], this is a plain `Modifier` chain for a self-sizing
 * `Column`.
 */
@Composable
fun Modifier.centredReadingColumn(maxWidth: Dp = dimensionResource(R.dimen.content_max_width)): Modifier =
    fillMaxSize().wrapContentWidth().widthIn(max = maxWidth)
