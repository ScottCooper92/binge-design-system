package com.binge.designsystem.tv.template

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.unit.Dp
import androidx.tv.material3.MaterialTheme
import com.binge.designsystem.startHorizontalGradient
import com.binge.designsystem.theme.LocalReduceMotion
import com.binge.designsystem.tv.TV_IMMERSIVE_CROSSFADE_MILLIS
import com.binge.designsystem.tv.component.TvCardRow
import com.binge.designsystem.tv.component.TvSeeAllTile
import com.binge.designsystem.tv.focus.TvStableFocusScroll
import com.binge.designsystem.tv.nav.LocalTvPaneShift
import com.binge.designsystem.tv.nav.ReportTvRailArtwork
import com.binge.designsystem.tv.nav.tvContentGutterStart
import com.binge.designsystem.R as DesR
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
)

private const val COPY_WIDTH_FRACTION = 0.75f
private const val SCRIM_START_ALPHA = 0.86f
private const val SCRIM_START_HOLD_FRACTION = 0.75f
private const val SCRIM_START_HOLD_ALPHA = 0.72f
private const val SCRIM_START_MID_FRACTION = 0.87f
private const val SCRIM_START_MID_ALPHA = 0.4f
private const val SCRIM_START_TAIL_FRACTION = 0.95f
private const val SCRIM_START_TAIL_ALPHA = 0.12f
private const val SCRIM_BOTTOM_START_FRACTION = 0.38f
private const val SCRIM_BOTTOM_ALPHA = 0.96f

/** Row key -> the item id focus returns to in that row, saveable so Back from a drill-down retraces the path. */
private val RowMemorySaver: Saver<SnapshotStateMap<String, Int>, Any> =
    mapSaver(
        save = { it.toMap() },
        restore = { saved -> mutableStateMapOf<String, Int>().apply { saved.forEach { (key, id) -> put(key, id as Int) } } },
    )

/**
 * The immersive hub: stacked poster rows over a full-bleed backdrop that follows focus. The focused row is
 * scrolled to the top of an inset viewport, the rows above it are clipped away so the backdrop shows through,
 * and the backdrop's [copy] describes the focused item.
 *
 * Generic over the item: [itemId] is its stable id (focus memory is keyed on it), [cell] draws one focusable
 * card, [artwork] paints the backdrop and [copy] fills the text band above the rows. An optional [hero] sits
 * above the rows at rest and gives way to the backdrop once a card takes focus; without one the first row's
 * first item is the resting backdrop.
 *
 * A page template, like [TvBoard]: [hosting] decides who places first focus on it — in a rail destination the
 * shell offers focus and the hub only routes it to the hero or first row, as an overlay it claims focus itself
 * once it has something to hold it. [entryFocusRequester] is the requester that arrival aims at, for a caller
 * that needs to aim it elsewhere (back onto the hub when a page opened from it closes).
 *
 * The scroll, anchor and clip behaviour here is the one every hub shares, so a surface using it moves the same
 * way Binge's hubs do. Beyond the arrival [hosting] asks for, nothing here calls `requestFocus`: state changes
 * follow focus arriving.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun <T : Any> TvImmersiveHub(
    rows: List<TvHubRow<T>>,
    itemId: (T) -> Int,
    cardWidth: Dp,
    onItemClick: (T) -> Unit,
    artwork: @Composable (T) -> Unit,
    copy: @Composable ColumnScope.(T) -> Unit,
    modifier: Modifier = Modifier,
    hero: (@Composable () -> Unit)? = null,
    // Where a caller aims focus to land it in the hub (the hero when there is one, else the first row).
    // Seeds the ring onto one card — a row's key and the item's id — for a screenshot, which runs no coroutines and
    // so dispatches no focus events. Production passes null.
    initialFocused: Pair<String, Int>? = null,
    entryFocusRequester: FocusRequester? = null,
    hosting: TvPageHosting = currentTvPageHosting(),
    seeAllLabel: String? = null,
    // Where a caller aims focus on the tile of a row (to restore it after the set it opened closes).
    seeAllModifier: (rowKey: String) -> Modifier = { Modifier },
    cell: @Composable (item: T, isFocused: Boolean, onFocusChanged: (Boolean) -> Unit, onClick: () -> Unit, cellModifier: Modifier) -> Unit,
) {
    val hasHero = hero != null
    // The rows the list lays out; every index below is a slot in this list, so an empty row cannot shift them.
    val visibleRows = rows.filter { it.items.isNotEmpty() }
    var focusedRowKey by rememberSaveable { mutableStateOf(initialFocused?.first) }
    var heroHasFocus by remember { mutableStateOf(false) }
    var bodyHasFocus by remember { mutableStateOf(false) }
    val rowMemory =
        rememberSaveable(saver = RowMemorySaver) {
            mutableStateMapOf<String, Int>().apply { initialFocused?.let { (rowKey, id) -> put(rowKey, id) } }
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
    val restingItem = if (hasHero) null else visibleRows.firstOrNull()?.items?.first()
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
    val effectiveEntryKey = entryRowKey ?: if (hasHero) null else visibleRows.firstOrNull()?.key

    Box(modifier = modifier.fillMaxSize().onFocusChanged(reportBodyFocus)) {
        TvImmersiveBackdrop(item = backdropItem, artwork = artwork, copy = copy)
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
                                        if (it.hasFocus) entryRowKey = null
                                    },
                        ) { hero() }
                    } else {
                        Spacer(Modifier.fillMaxWidth().height(contentTop))
                    }
                }
                items(items = visibleRows, key = { it.key }) { row ->
                    TvCardRow(
                        items = row.items,
                        key = itemId,
                        cellWidth = cardWidth,
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
                            val id = key as? Int
                            if (id != null) {
                                rowMemory[row.key] = id
                                if (seeAllRowKey == row.key) seeAllRowKey = null
                            } else {
                                seeAllRowKey = row.key
                            }
                        },
                        trailing =
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
                        cell(item, isFocused, onFocusChanged, { onItemClick(item) }, cellModifier)
                    }
                }
            }
        }
    }
}

/**
 * The backdrop layer: [artwork] full-bleed under a scrim, with [copy] in the band above the rows.
 *
 * [copyTopInset] reserves space at the top of the copy's band for a caller drawing over it, such as the grid's
 * heading. The copy is bottom-anchored, so without it a tall block would overflow up into the heading.
 */
@Composable
fun <T : Any> TvImmersiveBackdrop(
    item: T?,
    artwork: @Composable (T) -> Unit,
    copy: @Composable ColumnScope.(T) -> Unit,
    modifier: Modifier = Modifier,
    copyTopInset: Dp = Dp.Hairline,
) {
    Crossfade(
        targetState = item,
        modifier = modifier.fillMaxSize(),
        animationSpec = if (LocalReduceMotion.current) snap() else tween(TV_IMMERSIVE_CROSSFADE_MILLIS),
        label = "tv-immersive-backdrop",
    ) { focused ->
        if (focused != null) {
            val paneShift = LocalTvPaneShift.current
            Box(modifier = Modifier.fillMaxSize()) {
                // Cancel the pane's expansion shift so the artwork holds still while rows and copy slide over it.
                Box(modifier = Modifier.fillMaxSize().graphicsLayer { translationX = -paneShift.value.toPx() }) {
                    artwork(focused)
                    TvImmersiveScrim()
                }
                ReportTvRailArtwork()
                TvImmersiveCopyBand(copyTopInset) { copy(focused) }
            }
        }
    }
}

/** The text band above the rows, bottom-anchored so the copy does not shift as a title wraps. */
@Composable
private fun BoxScope.TvImmersiveCopyBand(copyTopInset: Dp, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier =
            Modifier
                .align(Alignment.TopStart)
                .height(dimensionResource(TvR.dimen.tv_immersive_content_top))
                .fillMaxWidth(COPY_WIDTH_FRACTION)
                .padding(
                    start = tvContentGutterStart(),
                    end = dimensionResource(DesR.dimen.padding_m),
                    top = dimensionResource(TvR.dimen.tv_overscan_vertical) + copyTopInset,
                    bottom = dimensionResource(DesR.dimen.padding_l),
                ),
        verticalArrangement = Arrangement.spacedBy(dimensionResource(DesR.dimen.padding_s), Alignment.Bottom),
        content = content,
    )
}

/** Two gradients: the horizontal one beds the copy, the vertical one beds the rows below. */
@Composable
private fun TvImmersiveScrim() {
    val background = MaterialTheme.colorScheme.background
    Box(
        modifier =
            Modifier.fillMaxSize().background(
                startHorizontalGradient(
                    0f to background.copy(alpha = SCRIM_START_ALPHA),
                    SCRIM_START_HOLD_FRACTION to background.copy(alpha = SCRIM_START_HOLD_ALPHA),
                    SCRIM_START_MID_FRACTION to background.copy(alpha = SCRIM_START_MID_ALPHA),
                    SCRIM_START_TAIL_FRACTION to background.copy(alpha = SCRIM_START_TAIL_ALPHA),
                    1f to background.copy(alpha = 0f),
                ),
            ),
    )
    Box(
        modifier =
            Modifier.fillMaxSize().background(
                Brush.verticalGradient(
                    colorStops =
                        arrayOf(
                            SCRIM_BOTTOM_START_FRACTION to background.copy(alpha = 0f),
                            1f to background.copy(alpha = SCRIM_BOTTOM_ALPHA),
                        ),
                ),
            ),
    )
}
