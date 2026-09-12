package com.binge.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import com.binge.designsystem.R
import com.binge.designsystem.component.FilterChipItem
import com.binge.designsystem.component.OverlaidHeaderContent
import com.binge.designsystem.navOverlayStart

/**
 * A horizontally-scrolling row of [BingeFilterChip]s — the selected one filled in the primary accent,
 * the rest outlined, each carrying an optional count. Stateless: the caller owns [selectedIndex] and
 * reacts to [onSelect]. The chip counterpart to [BingeTabRow], for filters that narrow a list (rather
 * than switch screens); pair it via [BingeFilterChipPager] for swipe-between-filters.
 */
@Composable
fun BingeFilterChipRow(
    items: List<FilterChipItem>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val listState = rememberLazyListState()
    // A LazyRow won't follow the selection on its own, so a swipe-driven change could leave the
    // active chip off-screen. Scroll it in — but only when not already fully visible, so tapping a
    // visible chip doesn't jump the row (#589).
    LaunchedEffect(selectedIndex, items.size) {
        val target = selectedIndex.coerceIn(0, (items.size - 1).coerceAtLeast(0))
        val info = listState.layoutInfo
        val visible = info.visibleItemsInfo.firstOrNull { it.index == target }
        val fullyVisible = visible != null &&
            visible.offset >= info.viewportStartOffset &&
            visible.offset + visible.size <= info.viewportEndOffset
        if (!fullyVisible) listState.animateScrollToItem(target)
    }
    LazyRow(
        state = listState,
        modifier = modifier,
        contentPadding = filterChipRowPadding(),
        horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_s)),
    ) {
        itemsIndexed(items) { index, item ->
            BingeFilterChip(
                label = item.label,
                selected = index == selectedIndex,
                onClick = { onSelect(index) },
                count = item.count,
            )
        }
    }
}

/**
 * The chip row's own insets, plus whatever an overlaying expanded nav rail covers on the start side.
 *
 * The rail publishes its width through [navOverlayStart] and every screen mounted under the shell opts
 * out of it individually; a chip row is one of the things that has to. Search's field above the chips
 * and its result grids below both clear the rail, and the chips did not — the leading chip rendered
 * against the rail's glass instead of clear of it (#2030).
 *
 * It belongs here rather than at the Search call site because the row is what has to move: insetting
 * `SearchFilterPager` wholesale would inset the paged results underneath it a second time, on top of
 * the `navOverlayPadding` they already apply. [navOverlayStart] reads zero wherever no rail is
 * overlaying, so every other caller is unaffected.
 *
 * [FilterChipRowSkeleton] reads the same function, so the plate and the row it stands in for cannot
 * disagree about where the first chip starts.
 */
@Composable
internal fun filterChipRowPadding(): PaddingValues {
    val edge = dimensionResource(R.dimen.screen_content_inset)
    val vertical = dimensionResource(R.dimen.padding_s)
    return PaddingValues(start = edge + navOverlayStart(), top = vertical, end = edge, bottom = vertical)
}

/**
 * [BingeFilterChipRow] wired to a [HorizontalPager], so tapping a chip and swiping a page stay in
 * sync — both move the selection. The caller owns the selection ([selectedIndex] /
 * [onSelectedIndexChange], typically ViewModel state) and supplies each page's content by index.
 *
 * The chips — and any [header] above them — are **overlaid** on a full-height pager rather than stacked
 * above it, so a page's content runs to the top of the window and travels under them, dissolving through
 * a short scrim. Stacked, a page's top edge was wherever the header ended and rows cropped hard there.
 *
 * That is why [pageContent] is handed a `contentPadding`: it measures the overlay, so a page's first row
 * still starts below the chips while later rows pass beneath. A lazy page folds it into its own
 * `contentPadding`; a page that does not scroll — a loading, empty or error state — applies it as
 * padding, or it sits behind the chips.
 *
 * Window insets are deliberately *not* applied here. A screen nested in a scaffold has already had them
 * handled, and one that owns its window puts them in [header].
 */
@Composable
fun BingeFilterChipPager(
    items: List<FilterChipItem>,
    selectedIndex: Int,
    onSelectedIndexChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    header: (@Composable () -> Unit)? = null,
    headerBackground: Color = MaterialTheme.colorScheme.background,
    scrimFraction: Float = 0f,
    pageContent: @Composable (contentPadding: PaddingValues, page: Int) -> Unit,
) {
    val pagerState = rememberPagerState(
        initialPage = selectedIndex.coerceIn(0, (items.size - 1).coerceAtLeast(0)),
    ) { items.size }
    // A chip tap (or any external selection change) animates the pager to that page.
    LaunchedEffect(selectedIndex) {
        if (selectedIndex != pagerState.currentPage) pagerState.animateScrollToPage(selectedIndex)
    }
    // A settled swipe reports the new page back up. The collect lambda outlives its composition (the
    // effect is keyed only on pagerState), so read selectedIndex + callback through rememberUpdatedState;
    // a value frozen at first composition would drop a swipe *back* to the original page (#1282).
    val latestSelectedIndex by rememberUpdatedState(selectedIndex)
    val latestOnSelectedIndexChange by rememberUpdatedState(onSelectedIndexChange)
    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.settledPage }.collect { page ->
            if (page != latestSelectedIndex) latestOnSelectedIndexChange(page)
        }
    }
    OverlaidHeaderContent(
        modifier = modifier,
        headerBackground = headerBackground,
        scrimFraction = scrimFraction,
        header = {
            header?.invoke()
            BingeFilterChipRow(
                items = items,
                selectedIndex = pagerState.currentPage,
                onSelect = onSelectedIndexChange,
            )
        },
    ) { contentPadding ->
        HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
            pageContent(contentPadding, page)
        }
    }
}
