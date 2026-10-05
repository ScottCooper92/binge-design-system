package com.binge.designsystem.tv.template

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.dimensionResource
import com.binge.designsystem.theme.LocalReduceMotion
import com.binge.designsystem.tv.focus.TvStableFocusScroll
import com.binge.designsystem.tv.R as TvR

private const val DEFAULT_COLUMNS = 6

/**
 * A headed, paged grid under the same backdrop as [TvImmersiveHub]: the focused row of cells is anchored to the
 * top of an inset viewport, the rows above it are clipped away so the backdrop shows through, and [copy] describes
 * the focused item. The shape every "see all" destination of a hub takes.
 *
 * A page template, like [TvBoard]: [hosting] decides who places first focus. As an overlay (how a see-all
 * destination is shown) the grid claims focus itself once it has cells, on the cell it remembers; in a rail
 * destination the shell offers focus and the grid only routes it there.
 *
 * Paged by access: [itemAt] is read for each visible cell, which is what asks a paging source for the next page. A
 * null return is a cell that is not loaded yet and draws nothing. [itemKey] names an index stably without loading
 * it. Before focus has entered the grid the backdrop shows the first item. The focused cell is remembered across a
 * pop; [rememberedCellModifier] rides that cell, so a caller can aim restored focus at it. [initialFocusedIndex]
 * seeds the ring for a screenshot, which runs no coroutines and so dispatches no focus events.
 */
@Composable
fun <T : Any> TvImmersiveGrid(
    heading: String,
    count: Int,
    itemAt: (Int) -> T?,
    itemKey: (Int) -> Any,
    artwork: @Composable (T) -> Unit,
    copy: @Composable ColumnScope.(T) -> Unit,
    modifier: Modifier = Modifier,
    columns: Int = DEFAULT_COLUMNS,
    hosting: TvPageHosting = currentTvPageHosting(),
    rememberedCellModifier: Modifier = Modifier,
    initialFocusedIndex: Int? = null,
    cell: @Composable (item: T, isFocused: Boolean, onFocusChanged: (Boolean) -> Unit, cellModifier: Modifier) -> Unit,
) {
    var focusedIndex by rememberSaveable { mutableIntStateOf(initialFocusedIndex ?: 0) }
    var entered by rememberSaveable { mutableStateOf(initialFocusedIndex != null) }
    var ringIndex by remember { mutableStateOf(initialFocusedIndex) }
    val reduceMotion = LocalReduceMotion.current
    val entry = remember { FocusRequester() }
    val rememberedIndex = focusedIndex.coerceIn(0, (count - 1).coerceAtLeast(0))

    val anchor = (rememberedIndex / columns) * columns
    val gridState = rememberSaveable(saver = LazyGridState.Saver) { LazyGridState(firstVisibleItemIndex = anchor) }
    val backdropIndex = if (entered) rememberedIndex else 0
    val settledIndex = rememberSettledFocus(backdropIndex, reduceMotion)
    val bursting = rememberBursting(anchor)
    val reportBodyFocus =
        rememberAnchorKeeper(rememberedIndex) {
            if (reduceMotion) gridState.scrollToItem(anchor) else gridState.animateScrollToItem(anchor)
        }
    val backdropItem = if (count > 0) itemAt(settledIndex.coerceIn(0, count - 1)) else null
    val padding = tvPagePadding(hosting)
    val ringBleedPx = with(LocalDensity.current) { dimensionResource(TvR.dimen.tv_focus_ring_bleed).toPx() }
    val direction = LocalLayoutDirection.current

    Box(modifier = modifier.fillMaxSize()) {
        TvImmersiveBackdrop(
            item = backdropItem,
            artwork = artwork,
            copy = copy,
            copyTopInset = dimensionResource(TvR.dimen.tv_screen_heading_height),
        )
        TvScreenHeading(
            title = heading,
            modifier =
                Modifier
                    .align(Alignment.TopStart)
                    .padding(
                        start = padding.calculateStartPadding(direction),
                        top = padding.calculateTopPadding(),
                        end = padding.calculateEndPadding(direction),
                    ),
        )
        TvStableFocusScroll {
            LazyVerticalGrid(
                columns = GridCells.Fixed(columns),
                state = gridState,
                modifier =
                    Modifier
                        .fillMaxSize()
                        // The ring's bleed goes into the grid's own padding, so the first row's ring is inside the viewport.
                        .padding(
                            top =
                                dimensionResource(TvR.dimen.tv_immersive_content_top) - dimensionResource(TvR.dimen.tv_focus_ring_bleed),
                        )
                        // Clipped a ring's bleed above the row, so the focus ring on its first row is not cut off.
                        .immersiveTopClip(
                            enabled = count > 0 && !bursting,
                        ) { (gridTopClip(gridState) { anchor }() - ringBleedPx).coerceAtLeast(0f) }
                        .onFocusChanged(reportBodyFocus)
                        .then(tvPageArrival(hosting, entry, enabled = count > 0, key = Unit)),
                contentPadding =
                    PaddingValues(
                        start = padding.calculateStartPadding(direction),
                        top = dimensionResource(TvR.dimen.tv_focus_ring_bleed),
                        end = padding.calculateEndPadding(direction),
                        bottom = dimensionResource(TvR.dimen.tv_immersive_rows_bottom),
                    ),
                verticalArrangement = Arrangement.spacedBy(dimensionResource(TvR.dimen.tv_immersive_grid_row_gap)),
                horizontalArrangement = Arrangement.spacedBy(dimensionResource(TvR.dimen.tv_media_row_card_gap)),
            ) {
                items(count = count, key = itemKey) { index ->
                    // Reading the item is what pages the next chunk in.
                    val item = itemAt(index)
                    if (item != null) {
                        cell(
                            item,
                            ringIndex == index,
                            { focused ->
                                if (focused) {
                                    ringIndex = index
                                    focusedIndex = index
                                    entered = true
                                } else if (ringIndex == index) {
                                    ringIndex = null
                                }
                            },
                            Modifier
                                .then(if (index == rememberedIndex) Modifier.focusRequester(entry) else Modifier)
                                .then(if (index == rememberedIndex) rememberedCellModifier else Modifier),
                        )
                    }
                }
            }
        }
    }
}
