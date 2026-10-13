package com.binge.designsystem.tv.template

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.binge.designsystem.theme.LocalReduceMotion
import com.binge.designsystem.tv.focus.TvStableFocusScroll
import com.binge.designsystem.tv.focus.tvEndDirectionKey
import com.binge.designsystem.tv.layout.TvLayoutAnchors
import com.binge.designsystem.tv.layout.tvLayoutAnchor
import com.binge.designsystem.tv.nav.LocalTvContentInset
import com.binge.designsystem.R as DesR
import com.binge.designsystem.tv.R as TvR

private const val DEFAULT_COLUMNS = 6

/**
 * A headed, paged grid under the same backdrop as [TvImmersiveHub]: the focused row is anchored to the top of an
 * inset viewport, the rows above it are clipped away so the backdrop shows through, and [copy] describes the focused
 * item. The shape every "see all" destination of a hub takes.
 *
 * @param content the cells, or the loading or message state that stands in for them.
 * @param onItemClick a cell's click; [onItemTapped] gets its index alongside, for impressions.
 * @param onVisitEnded the deepest index focus reached, once as the grid leaves composition.
 * @param band controls sharing the heading's row, given the room the heading leaves.
 * @param trailingAction pinned to the heading row's end, never squeezed out by [band].
 * @param onNavigateToEnd an end-direction press from the grid's end edge, or from any non-cell state.
 * @param entryFocusRequester a requester on the grid's body, for a caller restoring focus to it; entry still routes
 * to the remembered cell.
 * @param queryKey the content's identity: a new one resets the focus memory and the scroll to the top.
 * @param drawBackdrop false when a host draws the backdrop itself; [onBackdropItemChanged] tells it which item.
 * @param messageEndInset keeps a centred message clear of a pane drawn over this one's end.
 * @param loading the loading state, given the padding the cells will take.
 * @param message the message state, centred in the pane.
 * @param pendingCell a cell not loaded yet.
 */
@Composable
fun <T : Any> TvImmersiveGrid(
    heading: String,
    content: TvImmersiveGridContent<T>,
    onItemClick: (T) -> Unit,
    artwork: @Composable (T) -> Unit,
    copy: @Composable ColumnScope.(T) -> Unit,
    modifier: Modifier = Modifier,
    columns: Int = DEFAULT_COLUMNS,
    hosting: TvPageHosting = currentTvPageHosting(),
    copyLayout: TvImmersiveCopyLayout = TvImmersiveCopyLayout.Wide,
    onItemTapped: (index: Int) -> Unit = {},
    onVisitEnded: (deepestIndex: Int?) -> Unit = {},
    band: (@Composable () -> Unit)? = null,
    trailingAction: (@Composable () -> Unit)? = null,
    onNavigateToEnd: (() -> Unit)? = null,
    entryFocusRequester: FocusRequester? = null,
    queryKey: Any? = null,
    drawBackdrop: Boolean = true,
    onBackdropItemChanged: ((T?) -> Unit)? = null,
    messageEndInset: Dp = Dp.Hairline,
    rememberedCellModifier: Modifier = Modifier,
    initialFocusedIndex: Int? = null,
    loading: @Composable (contentPadding: PaddingValues) -> Unit = { TvImmersiveGridSkeleton(columns, it) },
    message: @Composable BoxScope.() -> Unit = {},
    pendingCell: @Composable () -> Unit = {},
    cell: @Composable (item: T, isFocused: Boolean, onFocusChanged: (Boolean) -> Unit, onClick: () -> Unit, cellModifier: Modifier) -> Unit,
) {
    val items = content as? TvImmersiveGridContent.Items
    // A visit is the screen, not the query, so this is deliberately not keyed on queryKey.
    val depth = rememberTvVisitDepth(onVisitEnded)
    var memory by rememberSaveable(queryKey, stateSaver = TvGridMemorySaver) {
        mutableStateOf(initialFocusedIndex?.let { TvGridMemory(it, items?.key?.invoke(it)) })
    }
    var ringIndex by remember(queryKey) { mutableStateOf(initialFocusedIndex) }
    val remembered = memory.resolveIn(content)
    val gridState =
        rememberSaveable(queryKey, saver = LazyGridState.Saver) {
            LazyGridState(firstVisibleItemIndex = anchorOf(initialFocusedIndex ?: 0, columns))
        }
    val reduceMotion = LocalReduceMotion.current
    ResetScrollWhenStale(memory.isStaleIn(content), gridState, reduceMotion)

    val anchor = anchorOf(remembered ?: 0, columns)
    val keepBodyFocused = rememberBodyFocusKeeper(content.bodyKind(), entryFocusRequester)
    val reportBodyFocus =
        rememberAnchorKeeper(remembered, alsoReport = keepBodyFocused) {
            if (reduceMotion) gridState.scrollToItem(anchor) else gridState.animateScrollToItem(anchor)
        }
    val bursting = rememberBursting(anchor)
    // At rest, before focus has entered, the backdrop shows the first cell, where entry lands.
    val settled = rememberSettledFocus(remembered, reduceMotion)
    val backdropItem = items?.takeIf { it.count > 0 }?.let { it.at(settled ?: 0) ?: it.at(0) }
    ReportBackdropItem(backdropItem, onBackdropItemChanged)

    val entry = remember { FocusRequester() }
    val entryIndex = remembered ?: 0
    val entryLoaded = items != null && entryIndex < items.count && items.at(entryIndex) != null
    val padding = tvPagePadding(hosting)
    val direction = LocalLayoutDirection.current

    Box(modifier = modifier.fillMaxSize()) {
        TvImmersiveBackdrop(
            item = backdropItem.takeIf { drawBackdrop },
            artwork = artwork,
            copy = copy,
            copyTopInset = dimensionResource(TvR.dimen.tv_screen_heading_height),
            copyStart = padding.calculateStartPadding(direction),
            layout = copyLayout,
        )
        TvGridHeading(
            heading = heading,
            band = band,
            trailingAction = trailingAction,
            modifier =
                Modifier
                    .align(Alignment.TopStart)
                    .padding(
                        start = padding.calculateStartPadding(direction),
                        top = padding.calculateTopPadding(),
                        end = padding.calculateEndPadding(direction),
                    ),
        )
        val cellPadding =
            PaddingValues(
                start = padding.calculateStartPadding(direction),
                top = dimensionResource(TvR.dimen.tv_focus_ring_bleed),
                end = padding.calculateEndPadding(direction),
                bottom = dimensionResource(TvR.dimen.tv_immersive_rows_bottom),
            )
        val endKey = tvEndDirectionKey()
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .then(
                        if (content is TvImmersiveGridContent.Message) {
                            Modifier.padding(start = LocalTvContentInset.current, end = messageEndInset)
                        } else {
                            // The ring's bleed goes into the cells' own padding, so the first row's ring is inside the viewport.
                            Modifier.padding(
                                top =
                                    dimensionResource(
                                        TvR.dimen.tv_immersive_content_top,
                                    ) - dimensionResource(TvR.dimen.tv_focus_ring_bleed),
                            )
                        },
                    ).immersiveTopClip(
                        enabled = drawBackdrop && items != null && items.count > 0 && !bursting,
                        topPx = gridTopClip(gridState) { anchor },
                    ).onFocusChanged(reportBodyFocus)
                    .then(
                        if (onNavigateToEnd == null) {
                            Modifier
                        } else {
                            // On the body, not the root, so an end press on a control in the band moves along the band.
                            Modifier.onPreviewKeyEvent { event ->
                                val edge = items == null || remembered?.let { atEndEdge(it, columns, items.count) } == true
                                val press = event.type == KeyEventType.KeyDown && event.key == endKey
                                if (press && edge) onNavigateToEnd()
                                press && edge
                            }
                        },
                    ),
        ) {
            when (content) {
                TvImmersiveGridContent.Loading -> loading(cellPadding)
                is TvImmersiveGridContent.Message -> message()
                is TvImmersiveGridContent.Items ->
                    TvStableFocusScroll {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(columns),
                            state = gridState,
                            modifier =
                                Modifier
                                    .fillMaxSize()
                                    .then(entryFocusRequester?.let { Modifier.focusRequester(it) } ?: Modifier)
                                    .then(tvPageArrival(hosting, entry, enabled = entryLoaded, key = queryKey)),
                            contentPadding = cellPadding,
                            verticalArrangement = Arrangement.spacedBy(dimensionResource(TvR.dimen.tv_immersive_grid_row_gap)),
                            horizontalArrangement = Arrangement.spacedBy(dimensionResource(TvR.dimen.tv_media_row_card_gap)),
                        ) {
                            items(count = content.count, key = content.key) { index ->
                                // Reading the item is what pages the next chunk in.
                                val item = content.at(index)
                                if (item == null) {
                                    pendingCell()
                                } else {
                                    // The anchor rides a wrapper, so it does not displace a tag the cell sets on itself.
                                    Box(
                                        modifier = if (index == 0) Modifier.tvLayoutAnchor(TvLayoutAnchors.GRID_FIRST_CELL) else Modifier,
                                        propagateMinConstraints = true,
                                    ) {
                                        cell(
                                            item,
                                            ringIndex == index,
                                            { focused ->
                                                if (focused) {
                                                    ringIndex = index
                                                    memory = TvGridMemory(index, content.key(index))
                                                    depth.reach(index)
                                                } else if (ringIndex == index) {
                                                    ringIndex = null
                                                }
                                            },
                                            {
                                                onItemTapped(index)
                                                onItemClick(item)
                                            },
                                            if (index ==
                                                entryIndex
                                            ) {
                                                Modifier.focusRequester(entry).then(rememberedCellModifier)
                                            } else {
                                                Modifier
                                            },
                                        )
                                    }
                                }
                            }
                        }
                    }
            }
        }
    }
}

/**
 * The grid over a source that is always cells, for a caller with nothing to say while loading or when empty.
 * [itemAt] returns null for a cell not loaded yet, which draws nothing.
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
    TvImmersiveGrid(
        heading = heading,
        content = TvImmersiveGridContent.Items(count = count, key = itemKey, at = itemAt),
        onItemClick = {},
        artwork = artwork,
        copy = copy,
        modifier = modifier,
        columns = columns,
        hosting = hosting,
        rememberedCellModifier = rememberedCellModifier,
        initialFocusedIndex = initialFocusedIndex,
    ) { item, isFocused, onFocusChanged, _, cellModifier ->
        cell(item, isFocused, onFocusChanged, cellModifier)
    }
}

/**
 * The heading's band, as tall as the rail's header line. With a [band] or a [trailingAction] the heading is capped,
 * the band takes what is left, and the action keeps its own width, so neither a long heading nor a long band can
 * push the action off the edge.
 */
@Composable
private fun TvGridHeading(
    heading: String,
    band: (@Composable () -> Unit)?,
    trailingAction: (@Composable () -> Unit)?,
    modifier: Modifier = Modifier,
) {
    if (band == null && trailingAction == null) {
        TvScreenHeading(title = heading, modifier = modifier)
        return
    }
    Row(
        modifier = modifier.fillMaxWidth().height(dimensionResource(TvR.dimen.tv_screen_heading_height)),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(dimensionResource(DesR.dimen.padding_l)),
    ) {
        Text(
            text = heading,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.widthIn(max = dimensionResource(TvR.dimen.tv_immersive_grid_heading_max_width)),
        )
        Box(modifier = Modifier.weight(1f)) { band?.invoke() }
        trailingAction?.invoke()
    }
}

/** Tells a host drawing the backdrop itself which item to paint, once per change rather than every recomposition. */
@Composable
private fun <T : Any> ReportBackdropItem(item: T?, report: ((T?) -> Unit)?) {
    report ?: return
    val currentReport by rememberUpdatedState(report)
    LaunchedEffect(item) { currentReport(item) }
}

/** The first cell of the row [index] falls in: the grid scrolls by cell, but anchors by row. */
private fun anchorOf(index: Int, columns: Int): Int = (index / columns) * columns

/** Whether [index] has nothing after it in its row, which makes an end press mean leave the grid. */
private fun atEndEdge(
    index: Int,
    columns: Int,
    count: Int,
): Boolean = index % columns == columns - 1 || index == count - 1
