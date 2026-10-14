package com.binge.designsystem.tv.template

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.mapSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.unit.Dp
import com.binge.designsystem.theme.LocalReduceMotion
import com.binge.designsystem.tv.component.TRAILING_KEY
import com.binge.designsystem.tv.component.TvCardRow
import com.binge.designsystem.tv.component.TvSeeAllTile
import com.binge.designsystem.tv.focus.TvStableFocusScroll
import com.binge.designsystem.tv.layout.TvLayoutAnchors
import com.binge.designsystem.tv.layout.tvLayoutAnchor
import com.binge.designsystem.tv.R as TvR

/**
 * One titled row of an immersive hub. [key] is the row's stable identity (the list and every piece of focus
 * memory are keyed on it, never on [title], which is localised and reworded).
 */
data class TvHubRow<T>(
    val key: String,
    val title: String,
    val items: List<T>,
    /** The row's end tile, opening the whole set behind it; null means the row has none. Shown only when the hub is given a `seeAllLabel`. */
    val onSeeAll: (() -> Unit)? = null,
    /**
     * Whether the row's items are still on their way. An empty loading row holds its slot with the hub's
     * `pendingRow` until they arrive, so the row does not push in under the user; it takes no focus meanwhile.
     */
    val isLoading: Boolean = false,
    /**
     * Why the row's items could not be read. An empty row that failed shows this in place of its cards, with a retry that takes
     * focus when the row is entered, rather than vanishing as an empty row does. Ignored while the row has items.
     */
    val failure: TvHubRowFailure? = null,
)

/** What a failed [TvHubRow] says, and the retry it offers. The caller owns the copy, as it does the row's title. */
data class TvHubRowFailure(
    val message: String,
    val retryLabel: String,
    val onRetry: () -> Unit,
)

/** Enough placeholder cards to run a pending row off the edge, as a loaded row does. */
private const val PENDING_ROW_CARDS = 8

/** Row key -> the item id focus returns to in that row, saveable so Back from a drill-down retraces the path. */
private val RowMemorySaver: Saver<SnapshotStateMap<String, Any>, Any> =
    mapSaver(
        save = { it.toMap() },
        restore = { saved -> mutableStateMapOf<String, Any>().apply { saved.forEach { (key, id) -> if (id != null) put(key, id) } } },
    )

/**
 * The immersive hub: stacked poster rows over a full-bleed backdrop that follows focus. The focused row is
 * scrolled to the top of an inset viewport, the rows above it are clipped away so the backdrop shows through,
 * and the backdrop's [copy] describes the focused item.
 *
 * Generic over the item: [itemId] is its stable id (focus memory is keyed on it, and saved with the page, so it is a
 * type a `Bundle` holds, as a lazy list's key is: a `String`, an `Int`, a `Long`), [cell] draws one focusable
 * card, [artwork] paints the backdrop and [copy] fills the text band above the rows. An optional [hero] sits
 * above the rows at rest and gives way to the backdrop once a card takes focus; without one the first row's
 * first item is the resting backdrop. An empty row is skipped, unless it is [TvHubRow.isLoading]: then [pendingRow]
 * holds its place, unfocusable, until its items arrive. An empty row with a [TvHubRow.failure] keeps its slot too, and says
 * why in place of its cards, with a retry that is the row's one focus stop.
 *
 * A page template, like [TvBoard]: [hosting] decides who places first focus on it — in a rail destination the
 * shell offers focus and the hub only routes it to the hero or first row, as an overlay it claims focus itself
 * once it has something to hold it. [entryFocusRequester] is the requester that arrival aims at, for a caller
 * that needs to aim it elsewhere (back onto the hub when a page opened from it closes).
 *
 * The scroll, anchor and clip behaviour here is the one every hub shares, so a surface using it moves the same
 * way Binge's hubs do. Beyond the arrival [hosting] asks for, nothing here calls `requestFocus`: state changes
 * follow focus arriving.
 *
 * The visit hooks report what focus did, for impressions: [onHeroVisible] and [onRowVisible] as focus arrives in
 * the hero or a row, [onItemTapped] with a click's row and position, and [onVisitEnded] once as the hub leaves, with
 * the deepest place focus reached. That place counts the hero as 0 and each row by its position in [rows] after it.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun <T : Any> TvImmersiveHub(
    rows: List<TvHubRow<T>>,
    itemId: (T) -> Any,
    cardWidth: Dp,
    onItemClick: (T) -> Unit,
    artwork: @Composable (T) -> Unit,
    copy: @Composable ColumnScope.(T) -> Unit,
    modifier: Modifier = Modifier,
    hero: (@Composable () -> Unit)? = null,
    // Where a caller aims focus to land it in the hub (the hero when there is one, else the first row).
    // Seeds the ring onto one card — a row's key and the item's id — for a screenshot, which runs no coroutines and
    // so dispatches no focus events. Production passes null.
    initialFocused: Pair<String, Any>? = null,
    entryFocusRequester: FocusRequester? = null,
    hosting: TvPageHosting = currentTvPageHosting(),
    seeAllLabel: String? = null,
    // Where a caller aims focus on the tile of a row (to restore it after the set it opened closes).
    seeAllModifier: (rowKey: String) -> Modifier = { Modifier },
    copyLayout: TvImmersiveCopyLayout = TvImmersiveCopyLayout.Wide,
    pendingRow: @Composable (Modifier) -> Unit = { rowModifier -> PosterRowSkeleton(PENDING_ROW_CARDS, cardWidth, rowModifier) },
    onHeroVisible: () -> Unit = {},
    onRowVisible: (rowKey: String) -> Unit = {},
    onItemTapped: (rowKey: String, index: Int) -> Unit = { _, _ -> },
    onVisitEnded: (deepestIndex: Int?) -> Unit = {},
    cell: @Composable (item: T, isFocused: Boolean, onFocusChanged: (Boolean) -> Unit, onClick: () -> Unit, cellModifier: Modifier) -> Unit,
) {
    val hasHero = hero != null
    // The rows the list lays out; every index below is a slot in this list, so an empty row cannot shift them.
    val visibleRows = rows.filter { it.items.isNotEmpty() || it.isLoading || it.failure != null }
    val depth = rememberTvVisitDepth(onVisitEnded)
    val heroDepth = if (hasHero) 1 else 0
    var focusedRowKey by rememberSaveable { mutableStateOf(initialFocused?.first) }
    var heroHasFocus by remember { mutableStateOf(false) }
    var bodyHasFocus by remember { mutableStateOf(false) }
    val rowMemory =
        rememberSaveable(saver = RowMemorySaver) {
            mutableStateMapOf<String, Any>().apply { initialFocused?.let { (rowKey, id) -> put(rowKey, id) } }
        }
    val hubEntry = entryFocusRequester ?: remember { FocusRequester() }
    var entryRowKey by rememberSaveable { mutableStateOf(initialFocused?.first) }
    // The row whose see-all tile last held focus, so re-entry lands on the tile rather than the last card.
    var seeAllRowKey by rememberSaveable { mutableStateOf<String?>(null) }
    val reduceMotion = LocalReduceMotion.current

    val focusedItem =
        rows
            .firstOrNull { it.key == focusedRowKey }
            ?.let { row -> row.items.firstOrNull { itemId(it) == rowMemory[row.key] } }
    val settledItem = rememberSettledFocus(focusedItem, reduceMotion)
    val settledRowKey = rememberSettledFocus(focusedRowKey, reduceMotion)
    // The hero is the resting state; without one the backdrop is never "resting", it just shows the first item.
    val showHero = hasHero && (heroHasFocus || settledItem == null || (!bodyHasFocus && entryRowKey == null))
    val restingItem = if (hasHero) null else visibleRows.firstOrNull { it.items.isNotEmpty() }?.items?.first()
    val backdropItem = if (showHero) null else (settledItem ?: restingItem)
    val bursting = rememberBursting(focusedRowKey)

    val contentTop = dimensionResource(TvR.dimen.tv_immersive_content_top)
    val contentTopPx = with(LocalDensity.current) { contentTop.roundToPx() }
    // Seeded, not only driven: a static frame runs no coroutines, so a baseline relying on the anchor effect below
    // would capture an unscrolled list.
    val initialIndex = initialFocused?.let { (rowKey, _) -> visibleRows.indexOfFirst { it.key == rowKey }.takeIf { it >= 0 }?.plus(1) } ?: 0
    val listState =
        rememberLazyListState(
            initialFirstVisibleItemIndex = initialIndex,
            initialFirstVisibleItemScrollOffset = if (initialIndex == 0) 0 else -contentTopPx,
        )
    // List slot 0 is the hero, or a spacer of the same band so the rows sit under the copy either way.
    val heroSlots = 1
    val focusedIndex = visibleRows.indexOfFirst { it.key == focusedRowKey }
    val scrollTarget = if (showHero || focusedIndex < 0) 0 else focusedIndex + heroSlots
    val settledRowIndex = visibleRows.indexOfFirst { it.key == settledRowKey }
    val movingUp = focusedIndex >= 0 && settledRowIndex >= 0 && focusedIndex < settledRowIndex
    val clipTarget = if ((bursting || focusedRowKey != settledRowKey) && movingUp) settledRowIndex + heroSlots else scrollTarget
    val scrollOffset = if (scrollTarget == 0) 0 else -contentTopPx
    val reportBodyFocus =
        rememberAnchorKeeper(
            scrollTarget,
            focusedItem?.let(itemId),
            alsoReport = { bodyHasFocus = it.hasFocus },
            shouldScroll = {
                scrollTarget != listState.firstVisibleItemIndex || scrollOffset != listState.firstVisibleItemScrollOffset
            },
        ) {
            if (reduceMotion) {
                listState.scrollToItem(
                    scrollTarget,
                    scrollOffset,
                )
            } else {
                listState.animateScrollToItem(scrollTarget, scrollOffset)
            }
        }
    // Without a hero, entry from outside (the rail) lands on the first row.
    val effectiveEntryKey =
        entryRowKey ?: if (hasHero) null else visibleRows.firstOrNull { it.items.isNotEmpty() || it.failure != null }?.key

    Box(modifier = modifier.fillMaxSize().onFocusChanged(reportBodyFocus)) {
        TvImmersiveBackdrop(item = backdropItem, artwork = artwork, copy = copy, layout = copyLayout)
        TvStableFocusScroll {
            LazyColumn(
                state = listState,
                modifier =
                    Modifier
                        .fillMaxSize()
                        .immersiveTopClip(enabled = !showHero, listTopClip(listState) { clipTarget })
                        // Do not set userScrollEnabled = false: focus search reaches off-screen lazy items through the scroll machinery.
                        .then(tvPageArrival(hosting, hubEntry, enabled = visibleRows.isNotEmpty(), key = Unit)),
                verticalArrangement = Arrangement.spacedBy(dimensionResource(TvR.dimen.tv_immersive_row_gap)),
                // Lets the last row scroll up to the anchor.
                contentPadding = PaddingValues(bottom = dimensionResource(TvR.dimen.tv_immersive_rows_bottom)),
            ) {
                item(key = "tv-immersive-hero") {
                    if (hero != null) {
                        Box(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .then(if (entryRowKey == null) Modifier.focusRequester(hubEntry) else Modifier)
                                    .focusGroup()
                                    .onFocusChanged {
                                        heroHasFocus = it.hasFocus
                                        if (it.hasFocus) {
                                            entryRowKey = null
                                            onHeroVisible()
                                            depth.reach(0)
                                        }
                                    },
                        ) { hero() }
                    } else {
                        Spacer(Modifier.fillMaxWidth().height(contentTop))
                    }
                }
                itemsIndexed(items = visibleRows, key = { _, row -> row.key }) { index, row ->
                    // So the skeleton's promise to lay its rows out here is assertable; see TvLayoutAnchors.
                    val rowAnchor = Modifier.tvLayoutAnchor(TvLayoutAnchors.hubRow(index))
                    val failure = row.failure
                    if (row.items.isEmpty() && failure != null) {
                        TvHubFailedRow(
                            title = row.title,
                            failure = failure,
                            cardWidth = cardWidth,
                            modifier = rowAnchor,
                            entryFocusRequester = hubEntry.takeIf { row.key == effectiveEntryKey },
                            initiallyFocused = initialFocused?.first == row.key,
                            onFocused = {
                                focusedRowKey = row.key
                                entryRowKey = row.key
                                onRowVisible(row.key)
                                depth.reach(rows.indexOfFirst { it.key == row.key } + heroDepth)
                            },
                        )
                        return@itemsIndexed
                    }
                    if (row.items.isEmpty()) {
                        pendingRow(rowAnchor)
                        return@itemsIndexed
                    }
                    TvCardRow(
                        items = row.items,
                        key = itemId,
                        cellWidth = cardWidth,
                        modifier = rowAnchor,
                        heading = row.title,
                        entryFocusRequester = hubEntry.takeIf { row.key == effectiveEntryKey },
                        initiallyFocusedKey = initialFocused?.takeIf { it.first == row.key }?.second,
                        overrideIndex =
                            if (seeAllRowKey == row.key && row.onSeeAll != null && seeAllLabel != null) {
                                row.items.size
                            } else {
                                rowMemory[row.key]?.let { id -> row.items.indexOfFirst { itemId(it) == id }.takeIf { it >= 0 } }
                            },
                        onCellFocused = { key ->
                            focusedRowKey = row.key
                            entryRowKey = row.key
                            onRowVisible(row.key)
                            depth.reach(rows.indexOfFirst { it.key == row.key } + heroDepth)
                            if (key != TRAILING_KEY) {
                                rowMemory[row.key] = key
                                if (seeAllRowKey == row.key) seeAllRowKey = null
                            } else {
                                seeAllRowKey = row.key
                            }
                        },
                        trailingContent =
                            row.onSeeAll?.takeIf { seeAllLabel != null }?.let { onSeeAll ->
                                { isFocused, onFocusChanged, cellModifier ->
                                    TvSeeAllTile(
                                        label = seeAllLabel.orEmpty(),
                                        isFocused = isFocused,
                                        onFocusChanged = onFocusChanged,
                                        onClick = onSeeAll,
                                        modifier = cellModifier.width(cardWidth).then(seeAllModifier(row.key)),
                                    )
                                }
                            },
                    ) { item, isFocused, onFocusChanged, cellModifier ->
                        val onClick = {
                            onItemTapped(row.key, row.items.indexOfFirst { itemId(it) == itemId(item) })
                            onItemClick(item)
                        }
                        cell(item, isFocused, onFocusChanged, onClick, cellModifier)
                    }
                }
            }
        }
    }
}
