package com.binge.designsystem.tv.template

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.onGloballyPositioned
import com.binge.designsystem.tv.focus.TvRetryHold
import com.binge.designsystem.tv.focus.rememberTvRetryHandoff
import com.binge.designsystem.tv.focus.tvShellArrivalClaims

private const val TV_GRID_PAGE_COLUMNS = 5

/**
 * A headed grid filling the content pane, the shape every "see all" destination takes on a television. A thin page over
 * [TvImmersiveGrid], which owns the layout, the focus memory and the backdrop: this adds the one message state (a failure with
 * Try again, or an empty result with its own action), the hand-off that carries focus across a retry, and the claim of the
 * shell's arrival. The item type is the caller's: [artwork] and [copy] draw the focused item's backdrop and text, and [cell]
 * draws a cell, a plate while [item][TvInPlaceCell] is null.
 *
 * Retry removes the plate holding focus, so a held node keeps it until the retry's cells are placed and take it. Results must
 * pass through a non-failure state on a retry, or the plate would keep the hold unseen.
 *
 * @param onItemClick the whole item, so a mixed grid routes each cell by what it is rather than by a bare id.
 * @param onItemTapped the position, alongside [onItemClick]'s whole item: what a grid reports must never be an id.
 * @param onVisitEnded the deepest cell focus reached, once as the page leaves.
 * @param emptyIcon the emblem for the empty state; the plate draws a default when there is none.
 * @param retryHolding draws the held-retry state, which a press sets at runtime, so a frame can render it.
 *
 * @need show a headed grid of items that fills a tv pane, with loading, empty and failed states
 * @group tv
 * @replaces a hand-built tv grid page with its own retry and focus hand-off
 */
@Composable
fun <T : Any> TvGridPage(
    heading: String,
    results: TvGridResults<T>,
    onItemClick: (T) -> Unit,
    onRetry: () -> Unit,
    emptyMessage: String,
    artwork: @Composable (T) -> Unit,
    copy: @Composable ColumnScope.(T) -> Unit,
    cell: TvInPlaceCell<T>,
    modifier: Modifier = Modifier,
    onItemTapped: (index: Int) -> Unit = {},
    onVisitEnded: (deepestIndex: Int?) -> Unit = {},
    columns: Int = TV_GRID_PAGE_COLUMNS,
    initialFocused: TvGridFocus<T>? = null,
    band: (@Composable () -> Unit)? = null,
    trailingAction: (@Composable () -> Unit)? = null,
    onNavigateToEnd: (() -> Unit)? = null,
    entryFocusRequester: FocusRequester? = null,
    emptyAction: TvGridAction? = null,
    emptyIcon: ImageVector? = null,
    queryKey: Any? = null,
    drawBackdrop: Boolean = true,
    onBackdropItemChanged: ((T?) -> Unit)? = null,
    retryHolding: Boolean = false,
) {
    val collapsed = results is TvGridResults.Error || results == TvGridResults.Empty
    val gridEntry = entryFocusRequester ?: remember { FocusRequester() }
    val retry = rememberTvRetryHandoff(
        showingFailure = collapsed,
        hasResults = results is TvGridResults.Items,
        takeFocus = { runCatching { gridEntry.requestFocus() } },
    )
    Box(modifier = modifier.tvShellArrivalClaims(gridEntry)) {
        TvImmersiveGrid(
            heading = heading,
            content = results.toGridContent(),
            onItemClick = onItemClick,
            artwork = artwork,
            copy = copy,
            modifier = Modifier,
            columns = columns,
            // The screens place first focus themselves through [entryFocusRequester]; the grid only routes entry.
            hosting = TvPageHosting.Destination,
            copyLayout = TvImmersiveCopyLayout.Narrow,
            onItemTapped = onItemTapped,
            onVisitEnded = onVisitEnded,
            band = band,
            trailingAction = trailingAction,
            onNavigateToEnd = onNavigateToEnd,
            entryFocusRequester = gridEntry.takeUnless { collapsed },
            queryKey = queryKey,
            drawBackdrop = drawBackdrop,
            onBackdropItemChanged = onBackdropItemChanged,
            initialFocusedIndex = initialFocused?.index,
            // While the hold has focus the entry cell's ring is drawn, on a plate too, so focus is never invisible during a retry.
            seededIndex = (initialFocused?.index ?: 0).takeIf { retryHolding || retry.holding },
            message = {
                TvGridCollapsedMessage(
                    results = results,
                    onRetry = {
                        retry.begin()
                        onRetry()
                    },
                    emptyMessage = emptyMessage,
                    emptyIcon = emptyIcon,
                    emptyAction = emptyAction,
                    focus = gridEntry,
                )
            },
            inPlace = TvImmersiveGridInPlace<T>(
                cell = { item, isFocused, onFocusChanged, onClick, cellModifier ->
                    cell(item, isFocused, onFocusChanged, onClick, cellModifier.fillMaxWidth().onGloballyPositioned { retry.cellPlaced() })
                },
            ),
        ) { _, _, _, _, _ -> }
        TvRetryHold(retry)
    }
}

/**
 * Which cell the page is built around, for a caller seeding a focused frame.
 *
 * A pair rather than a bare item because the index is what the anchor arithmetic needs, and a paged accessor offers no way
 * back from an id to an index without scanning it.
 */
data class TvGridFocus<T : Any>(
    val index: Int,
    val item: T,
)

private fun <T : Any> TvGridResults<T>.toGridContent(): TvImmersiveGridContent<T> =
    when (this) {
        TvGridResults.Loading -> TvImmersiveGridContent.Loading
        is TvGridResults.Error -> TvImmersiveGridContent.Message(key = TvGridResults.Error::class)
        TvGridResults.Empty -> TvImmersiveGridContent.Message(key = TvGridResults.Empty)
        is TvGridResults.Items -> TvImmersiveGridContent.Items(count = count, key = key, at = at)
    }
