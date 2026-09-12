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
 * what portrait stacks vertically (e.g. onboarding watch-type choices). Independent of
 * [isExpandedLayout]: a landscape phone is short *and* unexpanded.
 */
@Composable
fun isLandscape(): Boolean = booleanResource(R.bool.binge_landscape)

/**
 * Whether the window can hold a list and a detail side by side. Gates the two-pane split on both
 * Lists and Account.
 *
 * Two buckets resolve it, and the second exists because the first asks for the wrong thing.
 * `values-sw600dp-land` is the original: a landscape tablet. Its `land` came from #818, where a
 * 998x448dp landscape phone had the width for two panes and nowhere to put them — so orientation
 * was standing in for *height*, and it excluded the unfolded foldable, which is expanded and
 * portrait (~852x883dp), as collateral (#2395). `values-w840dp-h600dp` says it directly: >=840dp
 * wide, Material's expanded-width breakpoint, and >=600dp tall. The landscape phone still misses
 * on its 448dp of height; the Fold no longer does.
 *
 * Still narrower than [isExpandedLayout], which is width-only and over-triggered on portrait
 * tablets. A tablet under 840dp wide in portrait keeps the width-capped single column; one above it
 * gets the split, which is the point — the gate is the window's shape, not the device's class.
 */
@Composable
fun isTwoPaneLayout(): Boolean = booleanResource(R.bool.binge_layout_two_pane)

/**
 * Whether Discover is reachable as its own navigation destination (tablets). Hubs use this to drop
 * their in-content discover-entry tile where the nav tab makes it redundant; phones keep the tile.
 */
@Composable
fun isDiscoverInNav(): Boolean = booleanResource(R.bool.binge_nav_show_discover)

/**
 * Number of columns for a poster/media grid at the current window width: 2 (compact), 3 (>=600dp),
 * 4 (>=840dp). Any width-driven media grid reads it rather than hardcoding a column count.
 */
@Composable
fun mediaGridColumns(): Int = integerResource(R.integer.media_grid_columns)

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
