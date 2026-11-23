package com.binge.designsystem.tv.template

import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import com.binge.designsystem.component.HeroItem
import com.binge.designsystem.tv.focus.TvStableFocusScroll
import com.binge.designsystem.tv.focus.tvEntryFocusGroup
import com.binge.designsystem.tv.focus.tvShellArrivalTarget
import com.binge.designsystem.tv.layout.TvLayoutAnchors
import com.binge.designsystem.tv.layout.tvLayoutAnchor
import com.binge.designsystem.tv.template.TvHubRow
import com.binge.designsystem.tv.template.TvHubRowFailure
import com.binge.designsystem.tv.template.rememberTvVisitDepth
import com.binge.designsystem.R as DesR
import com.binge.designsystem.tv.R as TvR

private const val LOADING_ROW_COUNT = 3

/** The rows a [loading] hub shows: unnamed and without titles, so the page keeps the height it will fill. */
private val LOADING_ROWS =
    List(LOADING_ROW_COUNT) { TvHubRow<TvHubItem>(key = "loading-$it", title = "", items = emptyList(), isLoading = true) }

/**
 * The hub as Netflix-style rows: bare posters at rest, and under focus a row opens a backdrop slot that
 * ←/→ crossfade through the row's titles, with the details below. The focused row scrolls to the top of the pane.
 *
 * With [loading] the hero is its plate and the rows are placeholders, in the same list: the plate takes focus like the hero, so
 * focus placed on it stays on the same node when the slides arrive. [takeFocusOnEntry] is for a hub mounted over the plate that held
 * focus (an error or empty plate a retry replaced): it puts focus on the entry once.
 */
@Composable
internal fun TvExpandingRowsHubContent(
    rows: List<TvHubRow<TvHubItem>>,
    onItemClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
    onRowVisible: (key: String) -> Unit = {},
    onItemTapped: (key: String, index: Int) -> Unit = { _, _ -> },
    titleExtras: suspend (TvHubItem) -> TvTitleExtras? = { null },
    rowPages: @Composable (rowKey: String) -> TvRowPages<TvHubItem>? = { null },
    heroItems: List<HeroItem> = emptyList(),
    heroKey: (HeroItem) -> String = { "hero_${it.id}" },
    backdropModel: (String?) -> String? = { it },
    onVisitEnded: (deepestIndex: Int?) -> Unit = {},
    loading: Boolean = false,
    takeFocusOnEntry: Boolean = false,
    onRowItemClick: (rowKey: String, item: TvHubItem) -> Unit = { _, _ -> },
    // True while a sheet the hub's caller opened over it is up; when it goes, focus returns to the hub's entry.
    overlayOpen: Boolean = false,
    // Seeds focus onto one row for a screenshot, which runs no coroutines and so dispatches no focus events. Production passes null.
    initialFocusedKey: String? = null,
) {
    val depth = rememberTvVisitDepth(onVisitEnded)
    val hero = heroItems.takeIf { it.isNotEmpty() }
    val hasHeroSlot = hero != null || loading
    val extras = rememberTvTitleExtrasCache(titleExtras)
    // A loading row keeps its slot, so a row arriving late cannot push in above the one focus already took.
    val candidateRows = hubVisibleRows(rows, loading)
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = initialIndex(initialFocusedKey, candidateRows, hasHeroSlot))
    val focus = rememberTvExpandingHubFocus(listState, initialFocusedKey)
    val visibleRows = rememberTvExpandingLateRowGuard(candidateRows, focus)
    // No row takes focus until the first has its titles (or its retry), else startup focus lands on whichever row loaded first.
    val ready = hero != null || visibleRows.firstOrNull()?.let(::isSettled) == true
    val firstKey = hubFirstKey(hasHeroSlot, visibleRows)
    val rowKeys = visibleRows.map { it.key }
    val entryKey = tvExpandingEntryKey(focus.savedKey, hasHeroSlot, rowKeys, focus.nextKey)
    TvExpandingHubHeadFollow(focus, firstKey, listState)
    TvExpandingEntryFocus(takeFocusOnEntry, hasHeroSlot, focus)
    TvExpandingOverlayReturn(overlayOpen, focus)
    TvExpandingFollowFocusedRow(focus, listState)
    TvExpandingRecoveredRowFocus(visibleRows, focus)
    TvExpandingVanishedRowFocus(rowKeys, focus)
    TvStableFocusScroll {
        LazyColumn(
            state = listState,
            modifier = modifier
                .fillMaxSize()
                .onPreviewKeyEvent { event ->
                    if (event.type == KeyEventType.KeyDown) focus.userMoved = true
                    false
                }.onFocusChanged { focus.hubHasFocus = it.hasFocus }
                .tvEntryFocusGroup(focus.entry),
            verticalArrangement = Arrangement.spacedBy(dimensionResource(TvR.dimen.tv_immersive_row_gap)),
            contentPadding = PaddingValues(bottom = dimensionResource(TvR.dimen.tv_immersive_rows_bottom)),
        ) {
            if (hasHeroSlot) {
                item(key = TV_EXPANDING_HERO_KEY) {
                    HeroSlot(
                        heroItems,
                        heroKey,
                        backdropModel,
                        extras,
                        focus,
                        entryKey == TV_EXPANDING_HERO_KEY,
                        onRowVisible,
                        onItemClick,
                        onItemTapped,
                    ) { depth.reach(0) }
                }
            }
            itemsIndexed(visibleRows, key = { _, row -> row.key }) { rowIndex, row ->
                val index = rowIndex + if (hasHeroSlot) 1 else 0
                // Where entry goes if this row empties while it has focus: the row below, or above for the last.
                val beside = rowKeys.getOrNull(rowIndex + 1) ?: rowKeys.getOrNull(rowIndex - 1)
                Box(Modifier.tvLayoutAnchor(TvLayoutAnchors.hubRow(rowIndex))) {
                    // With no hero, the entry row is the plate focus arrives on, and the row its titles arrive in.
                    val entryPlate = row.items.isEmpty() && !hasHeroSlot && row.key == entryKey
                    val failure = row.failure.takeIf { row.items.isEmpty() }
                    if (failure != null) {
                        // Reached, but no impression: the row shows no titles.
                        FailedRowSlot(row.title, failure, rowIndex, focus, isEntry = row.key == entryKey, ready = ready) {
                            focus.onFocused(row.key, index, beside)
                            depth.reachRow(rows, row.key, hasHero = hasHeroSlot)
                        }
                    } else if (row.items.isEmpty() && !entryPlate) {
                        TvExpandingPendingRow(row.title, rowIndex)
                    } else {
                        TvExpandingRow(
                            pages = rowPages(row.key),
                            // Back to the top bar leaves the row you were on open, as Netflix does; any other row collapses as focus leaves it.
                            heldOpen = row.key == entryKey && !focus.hubHasFocus,
                            settleIn = focus.movedDown,
                            focusable = ready || entryPlate,
                            extras = extras,
                            row = row,
                            entryRequester = focus.entry.takeIf { row.key == entryKey },
                            onRowFocused = {
                                focus.onFocused(row.key, index, beside)
                                depth.reachRow(rows, row.key, hasHero = hasHeroSlot)
                                onRowVisible(row.key)
                            },
                            onItemClick = { itemIndex, item ->
                                onItemTapped(row.key, itemIndex)
                                onRowItemClick(row.key, item)
                            },
                            headingAnchor = TvExpandingAnchors.heading(rowIndex),
                            initiallyFocused = row.key == initialFocusedKey,
                            onEntryPlaced = focus::entryPlaced,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FailedRowSlot(
    title: String,
    failure: TvHubRowFailure,
    rowIndex: Int,
    focus: TvExpandingHubFocus,
    isEntry: Boolean,
    ready: Boolean,
    onFocused: () -> Unit,
) {
    TvExpandingFailedRow(
        title = title,
        failure = failure,
        rowIndex = rowIndex,
        focusable = ready || isEntry,
        entryRequester = focus.entry.takeIf { isEntry },
        onFocused = onFocused,
        onEntryPlaced = focus::entryPlaced,
    )
}

private fun hubVisibleRows(rows: List<TvHubRow<TvHubItem>>, loading: Boolean) =
    if (loading) LOADING_ROWS else rows.filter { it.items.isNotEmpty() || it.isLoading || it.failure != null }

/** The list index a seeded [initialFocusedKey] row sits at, so a screenshot frames it at the top as focus would scroll it. */
private fun initialIndex(
    initialFocusedKey: String?,
    rows: List<TvHubRow<TvHubItem>>,
    hasHeroSlot: Boolean,
): Int {
    val rowIndex = rows.indexOfFirst { it.key == initialFocusedKey }
    return if (rowIndex < 0) 0 else rowIndex + if (hasHeroSlot) 1 else 0
}

private fun hubFirstKey(hasHeroSlot: Boolean, visibleRows: List<TvHubRow<TvHubItem>>) =
    if (hasHeroSlot) TV_EXPANDING_HERO_KEY else visibleRows.firstOrNull(::isSettled)?.key

/** Whether a row has something to focus: its titles, or the retry of a read that failed. */
private fun isSettled(row: TvHubRow<TvHubItem>) = row.items.isNotEmpty() || row.failure != null

@Composable
private fun HeroSlot(
    hero: List<HeroItem>,
    heroKey: (HeroItem) -> String,
    backdropModel: (String?) -> String?,
    extras: TvTitleExtrasCache,
    focus: TvExpandingHubFocus,
    isEntry: Boolean,
    onRowVisible: (key: String) -> Unit,
    onItemClick: (Int) -> Unit,
    onItemTapped: (key: String, index: Int) -> Unit,
    onReached: () -> Unit,
) {
    var heroHasFocus by remember { mutableStateOf(false) }
    // The plate is focusable but not yet a row anyone has seen, so reaching it counts once the slides are there.
    val loaded = hero.isNotEmpty()

    fun reach() {
        focus.onFocused(TV_EXPANDING_HERO_KEY, 0)
        onReached()
        onRowVisible(TV_EXPANDING_HERO_KEY)
    }
    LaunchedEffect(loaded) { if (loaded && heroHasFocus) reach() }
    Box(
        modifier = Modifier
            .tvLayoutAnchor(TvLayoutAnchors.entry(TvLayoutAnchors.HERO_KEY))
            .fillMaxWidth()
            .then(if (isEntry) Modifier.tvShellArrivalTarget(focus.entry).onGloballyPositioned { focus.entryPlaced() } else Modifier)
            .onFocusChanged {
                if (it.hasFocus && !heroHasFocus && loaded) reach()
                heroHasFocus = it.hasFocus
            }.focusGroup(),
    ) {
        TvExpandingHero(
            items = hero,
            heroKey = heroKey,
            backdropModel = backdropModel,
            extras = extras,
            onItemClick = onItemClick,
            onItemTapped = { index -> onItemTapped(TV_EXPANDING_HERO_KEY, index) },
        )
    }
}

/**
 * The hub as Netflix-style rows: bare posters at rest, and under focus a row opens a backdrop slot that ←/→ crossfade through the
 * row's titles, with the details below. The focused row scrolls to the top of the pane.
 *
 * The items are the caller's own: [item] words each one for the hub, and every callback hands the caller's item back. With [loading]
 * the hero is its plate and the rows are placeholders in the same list, so focus placed on the plate stays on the same node when
 * the slides arrive. [takeFocusOnEntry] is for a hub mounted over the plate that held focus (an error or empty plate a retry
 * replaced): it puts focus on the entry once.
 *
 * @param item what the hub draws for a title; its key must be stable across reloads, as focus position and extras follow it.
 * @param onItemClick the hero's id, as [HeroItem] has one; [onRowItemClick] is the row titles' click.
 * @param titleExtras loads a title's logo and certification once it is open; null until they arrive.
 * @param heroExtras the same for a hero slide, which has no row item behind it.
 * @param heroKey the [TvHubItem.key] a hero slide shares with a row title, so the two share one extras load.
 * @param backdropModel maps the hero's backdrop URL to what the image loader is given, for an app that wants a larger size.
 * @param overlayOpen true while a sheet the caller opened over the hub is up; when it goes, focus returns to the hub's entry.
 * @param initialFocusedKey seeds focus onto one row for a screenshot, which dispatches no focus events; null in production.
 *
 * A row's [TvHubRow.onSeeAll], [TvHubRow.seeAllLabel] and [TvHubRow.seeAllPeek] are not read here: this hub draws no See-all card.
 * For one, use [TvExpandingSingleRow].
 *
 * [onRowVisible] and [onItemTapped] report the hero as `binge:tv-expanding-hero`; an app's own row keys must not use it.
 *
 * @sample TvExpandingHubWithHeroSample
 * @need show titles as rows that expand under focus into a backdrop slot, with a hero above, on a tv screen
 * @group tv
 * @replaces a hand-built tv home hub with its own expanding rows, hero and focus hand-off
 */
@Composable
fun <T : Any> TvExpandingRowsHub(
    rows: List<TvHubRow<T>>,
    item: (T) -> TvHubItem,
    onItemClick: (Int) -> Unit,
    onRowItemClick: (rowKey: String, item: T) -> Unit,
    modifier: Modifier = Modifier,
    onRowVisible: (key: String) -> Unit = {},
    onItemTapped: (key: String, index: Int) -> Unit = { _, _ -> },
    titleExtras: suspend (T) -> TvTitleExtras? = { null },
    heroExtras: suspend (HeroItem) -> TvTitleExtras? = { null },
    rowPages: @Composable (rowKey: String) -> TvRowPages<T>? = { null },
    heroItems: List<HeroItem> = emptyList(),
    heroKey: (HeroItem) -> String = { "hero_${it.id}" },
    backdropModel: (String?) -> String? = { it },
    onVisitEnded: (deepestIndex: Int?) -> Unit = {},
    loading: Boolean = false,
    takeFocusOnEntry: Boolean = false,
    overlayOpen: Boolean = false,
    initialFocusedKey: String? = null,
) {
    val shown = remember(rows, item) { rows.map { row -> row.mapItems { item(it).copy(source = it) } } }
    TvExpandingRowsHubContent(
        rows = shown,
        onItemClick = onItemClick,
        modifier = modifier,
        onRowVisible = onRowVisible,
        onItemTapped = onItemTapped,
        titleExtras = { shown ->
            val hero = shown.heroSource
            if (hero != null) heroExtras(hero) else titleExtras(shown.sourceAs())
        },
        rowPages = { key ->
            rowPages(key)?.let { pages ->
                TvRowPages(pages.count, pages.endReached) { index ->
                    pages.at(index)?.let {
                        item(it).copy(source = it)
                    }
                }
            }
        },
        heroItems = heroItems,
        heroKey = heroKey,
        backdropModel = backdropModel,
        onVisitEnded = onVisitEnded,
        loading = loading,
        takeFocusOnEntry = takeFocusOnEntry,
        onRowItemClick = { key, shownItem -> onRowItemClick(key, shownItem.sourceAs()) },
        overlayOpen = overlayOpen,
        initialFocusedKey = initialFocusedKey,
    )
}

private fun <T : Any, R : Any> TvHubRow<T>.mapItems(transform: (T) -> R): TvHubRow<R> =
    TvHubRow(
        key = key,
        title = title,
        items = items.map(transform),
        onSeeAll = onSeeAll,
        isLoading = isLoading,
        failure = failure,
        loops = loops,
        seeAllPeek = seeAllPeek,
        seeAllLabel = seeAllLabel,
    )

/** The caller's item a [TvHubItem] was drawn from; the wrappers above are the only places one is made, so the cast always holds. */
@Suppress("UNCHECKED_CAST")
private fun <T : Any> TvHubItem.sourceAs(): T = source as T

/**
 * One of the hub's expanding rows on its own, for a page that shows a single set of titles (a detail page's More like this). It
 * opens already expanded, at the detail page's taller height, and the logo and certification come from [titleExtras] as in the
 * hub. A [title] of "" draws no heading, for a tab that already names the set.
 *
 * With [onSeeAll] the row ends in a See-all card, one step right of the last title, for a set whose full list is another screen
 * (a collection). It reads "All [seeAllCount]" when the caller knows how many that set holds, else "See all".
 *
 * @need show one set of titles as a single row that opens already expanded, for a tab or page section
 * @group tv
 * @replaces a hand-built single titles row with its own expanding slot
 */
@Composable
fun <T : Any> TvExpandingSingleRow(
    title: String,
    items: List<T>,
    item: (T) -> TvHubItem,
    onItemClick: (index: Int, item: T) -> Unit,
    modifier: Modifier = Modifier,
    titleExtras: suspend (T) -> TvTitleExtras? = { null },
    onSeeAll: (() -> Unit)? = null,
    seeAllCount: Int? = null,
) {
    val shown = remember(items, item) { items.map { item(it).copy(source = it) } }
    TvExpandingSingleRowContent(
        title = title,
        items = shown,
        onItemClick = { index, shownItem -> onItemClick(index, shownItem.sourceAs()) },
        modifier = modifier,
        titleExtras = { titleExtras(it.sourceAs()) },
        onSeeAll = onSeeAll,
        seeAllCount = seeAllCount,
    )
}

@Composable
internal fun TvExpandingSingleRowContent(
    title: String,
    items: List<TvHubItem>,
    onItemClick: (index: Int, item: TvHubItem) -> Unit,
    modifier: Modifier = Modifier,
    titleExtras: suspend (TvHubItem) -> TvTitleExtras? = { null },
    onSeeAll: (() -> Unit)? = null,
    seeAllCount: Int? = null,
) {
    if (items.isEmpty()) return
    val extras = rememberTvTitleExtrasCache(titleExtras)
    val seeAllLabel = seeAllCount?.let { stringResource(TvR.string.tv_see_all_count, it) } ?: stringResource(DesR.string.gallery_see_all)
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.CenterStart) {
        TvExpandingRow(
            row = TvHubRow(key = SINGLE_ROW_KEY, title = title, items = items),
            pages = null,
            heldOpen = true,
            settleIn = false,
            focusable = true,
            extras = extras,
            entryRequester = null,
            onRowFocused = {},
            onItemClick = onItemClick,
            openHeight = dimensionResource(TvR.dimen.tv_detail_similar_height),
            seeAll = onSeeAll?.let { TvExpandingSeeAll(seeAllLabel, it) },
        )
    }
}

private const val SINGLE_ROW_KEY = "single"
