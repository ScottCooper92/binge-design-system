package com.binge.designsystem.template

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import com.binge.designsystem.CenteredContent
import com.binge.designsystem.ErrorKind
import com.binge.designsystem.R
import com.binge.designsystem.component.BingeConfirmDialog
import com.binge.designsystem.component.BingeFilterChipRow
import com.binge.designsystem.component.BingeSearchField
import com.binge.designsystem.component.BingeSearchFieldHeader
import com.binge.designsystem.component.FilterChipItem
import com.binge.designsystem.component.FilterChipRowSkeleton
import com.binge.designsystem.component.FilterPager
import com.binge.designsystem.component.MediaResult
import com.binge.designsystem.component.MediaResultRow
import com.binge.designsystem.component.MediaResultRowSkeleton
import com.binge.designsystem.component.OnTabReselect
import com.binge.designsystem.component.TrendingSearchChip
import com.binge.designsystem.component.WrappingRow
import com.binge.designsystem.component.rememberFilterPager
import com.binge.designsystem.layout.LayoutAnchors
import com.binge.designsystem.layout.layoutAnchorIf
import com.binge.designsystem.navOverlayPadding
import com.binge.designsystem.navOverlayStart
import com.binge.designsystem.searchResultColumns
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter

/** The lazy key of the append footer. An app's own keys are text too, so it carries the reserved `binge:` prefix and cannot collide with them. */
private const val FOOTER_KEY = "binge:search-footer"
private const val SEARCH_PAGE_KEY_PREFIX = "binge:search-page:"

/** How many rows of plates the loading skeleton draws. */
const val SEARCH_SKELETON_ROWS: Int = 6

/** How many results from the end the list asks for the next page. */
const val SEARCH_LOAD_MORE_AHEAD: Int = 3

/** One type chip under the field. [key] is the app's identity for the type, text; [count] is optional, so an app with none passes none. */
@Immutable
class SearchTypeChip(
    val key: String,
    val label: String,
    val count: Int? = null,
)

/** The type chips under the field, and the [selectedKey] of the one chosen. `onType` hands a chip's key back, never its position. */
@Immutable
class SearchTypes(
    val chips: List<SearchTypeChip>,
    val selectedKey: String,
)

/** What a [SearchScreen] draws. The type chips show only in [Ready] and [NoResults], where there is something to narrow. */
@Immutable
sealed interface SearchState {
    /**
     * Nothing searched yet, or too little typed: the [recents], newest first, and whatever the app adds under them. [hasExtras] says
     * the app's `idleExtras` draws something (rails, or a skeleton while they load), so the empty plate stays away.
     */
    class Idle(
        val recents: List<String>,
        val hasExtras: Boolean = false,
    ) : SearchState

    /** A search is on its way. */
    data object Loading : SearchState

    /**
     * Results. [loaded] is how many have arrived across every type, so a page that adds nothing to the chosen type still re-arms
     * load-more. A settled list with no results shows [emptyTitle] (null says "Nothing here yet") and [emptyMessage].
     *
     * [pages] turns the body into a pager over [types]' chips: each chip's key holds that type's results, in the order of the chips, and a
     * type with no entry shows loading rows. It applies only when [types] is set with more than one chip; [results] then plays no part.
     * The paging flags stay shared by every page, and each page asks for more near its own end.
     *
     * [query] is the query these results answer. An app that debounces and keeps the old results on screen while the field changes sets it, so
     * the pages keep their place until the new results arrive and then start at the top. Null reads the live `text` instead. While loading or after a failure the last query answered is kept; idle forgets it.
     */
    class Ready(
        val results: List<MediaResult>,
        val loaded: Int,
        val hasMore: Boolean,
        val appending: Boolean = false,
        val appendFailed: Boolean = false,
        val types: SearchTypes? = null,
        val emptyTitle: String? = null,
        val emptyMessage: String = "",
        val pages: Map<String, List<MediaResult>>? = null,
        val query: String? = null,
    ) : SearchState

    /**
     * The query found nothing; [types] keeps the chips, with whatever counts the app gives them. [title] null says "Nothing here yet".
     * With [pages] (see [Ready]) every type still swipes, each to the same plate. [query] is as on [Ready].
     */
    class NoResults(
        val message: String,
        val title: String? = null,
        val types: SearchTypes? = null,
        val pages: Map<String, List<MediaResult>>? = null,
        val query: String? = null,
    ) : SearchState

    /** The search failed. [title] and [message] default to [kind]'s own words; Try again shows only when [retryable], and [secondary] is a second action beside it. */
    class Failed(
        val kind: ErrorKind,
        val title: String? = null,
        val message: String? = null,
        val retryable: Boolean = true,
        val secondary: ScreenAction? = null,
    ) : SearchState
}

/** What the screen reports. [onBack] null draws no back control. */
@Immutable
class SearchCallbacks(
    val onQueryChange: (String) -> Unit,
    val onClear: () -> Unit,
    val onOpenResult: (key: String) -> Unit,
    val onLoadMore: () -> Unit,
    val onRetry: () -> Unit,
    val onSubmit: () -> Unit = {},
    val onType: (key: String) -> Unit = {},
    val onPickRecent: (query: String) -> Unit = {},
    val onClearRecents: () -> Unit = {},
    val onBack: (() -> Unit)? = null,
)

/**
 * A search screen on the large collapsing bar: the title gives way as results scroll, and the field and the type chips stay pinned under
 * it. Below is whichever of the idle offer (recent searches, then the app's own rails), rows of loading plates, a grid of [MediaResultRow]s that loads
 * more near its end, a no-results plate or a failure the state calls for. The grid is capped at the search width and folds into
 * columns on wider windows.
 *
 * Nothing here is an app's type: results are [MediaResult]s with a text key, each result's status is the [resultTrailing] slot, and the
 * app owns the query, the history and the paging. [titleTrailing] and [barActions] are told how far the bar has collapsed (they
 * switch at [BAR_HANDOVER_COLLAPSE]). [idleExtras] adds items under the recents, [noResultsExtras] sits under the no-results plate, and
 * [failedContent] replaces the default failure plate.
 *
 * @param idleTitle the plate shown while idle with nothing to offer, which the app words; the defaults say "Search" and "Type a title or a name."
 * @param loadingLabel what a screen reader says of the first loading row.
 * @param reselectKey the tab this screen is the root of, as the shell emits it on the reselect signal (compared by equality, so pass
 *   the same value); tapping it again focuses the field. Null for a screen with no tab.
 * @param resultTrailing decides each result's status: it returns the status to draw, or null for a result with none, so a row with
 *   no status has no gap where one would go.
 * @param reserveTypeChipsWhileLoading true when the results will bring type chips ([SearchState.Ready] carries them): the loading state then
 *   holds a row of chip plates under the field, so the first result does not drop by that row when the chips arrive. False, the default, draws none.
 * @param expectedResultName a string the loaded result names are expected to look like, the longest typical title: each loading row lays it out invisibly
 *   in the row's own style and width, so its name plate takes what the name takes at every font size, wrapping included. Null, the default, is one line.
 *   The status chips are the app's own and are not part of the hint.
 *
 * @param bar the top bar: the large collapsing one by default, or `Small`, whose title row never collapses so nothing is handed over.
 * @param pinned keeps a `Small` bar, with the field and chips under it, in place while the results scroll under them, as the scaffold's `pinned`. False, the
 *   default, lets the small bar slide away as it always has; it has no effect on the other bars.
 * @param fastScrollbar draws a [com.binge.designsystem.component.BingeFastScrollbar] over the results grid (over each page's own grid when the
 *   body pages), at the end edge and inside the bar's and the navigation bar's room, with the options' bubble text. Null, the default, draws none.
 *
 * @need search with a field, type chips, recent searches and a grid of results
 * @group screens
 * @replaces a hand-built scaffold with a search field, chips and a paged results list
 */
@Composable
fun SearchScreen(
    title: String,
    text: String,
    state: SearchState,
    callbacks: SearchCallbacks,
    placeholder: String,
    modifier: Modifier = Modifier,
    idleTitle: String = stringResource(R.string.search_idle_title),
    idleMessage: String = stringResource(R.string.search_idle_message),
    loadingLabel: String = stringResource(R.string.search_loading),
    reselectKey: Any? = null,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    recentsTitle: String = stringResource(R.string.search_recents_title),
    clearLabel: String = stringResource(R.string.search_recents_clear),
    clearTitle: String = stringResource(R.string.search_recents_clear_title),
    clearMessage: String = stringResource(R.string.search_recents_clear_message),
    moreFailedLabel: String = stringResource(R.string.list_more_failed),
    titleTrailing: (@Composable (pillInteractive: Boolean) -> Unit)? = null,
    barActions: @Composable RowScope.(handedOver: Boolean, collapsedFraction: Float) -> Unit = { _, _ -> },
    resultTrailing: ((MediaResult) -> (@Composable () -> Unit)?)? = null,
    idleExtras: (LazyListScope.() -> Unit)? = null,
    noResultsExtras: (@Composable () -> Unit)? = null,
    failedContent: (@Composable (Modifier) -> Unit)? = null,
    bar: ScreenBar = ScreenBar.Large,
    pinned: Boolean = false,
    reserveTypeChipsWhileLoading: Boolean = false,
    expectedResultName: String? = null,
    fastScrollbar: FastScrollbarOptions? = null,
    overlays: @Composable () -> Unit = {},
) {
    val scrollBehavior = rememberScreenBarScrollBehavior(bar, pinned)
    // A small bar's title row never collapses, so nothing is ever handed over on it.
    val collapsed = if (bar == ScreenBar.Small) 0f else scrollBehavior?.state?.collapsedFraction ?: 0f
    val titleRowShows = collapsed < BAR_HANDOVER_COLLAPSE
    val paged = state.pagedTypes()
    // Keyed on the query the shown results answer: they open at the top of every page when a new query's arrive, while a type tap keeps each page's place.
    val answered = (state as? SearchState.Ready)?.query ?: (state as? SearchState.NoResults)?.query
    val lastAnswered = remember { AnsweredQuery() }
    if (answered != null) {
        lastAnswered.value = answered
    } else if (state is SearchState.Idle) {
        lastAnswered.value = null
    }
    // Loading and failed keep the last answered query, so the key does not fall to the live text and back. Idle searched nothing: a search
    // of the same query after it is a new one and opens at the top.
    val shownQuery = answered ?: lastAnswered.value ?: text
    val pageStates = rememberSaveable(shownQuery, saver = SearchPageStates.Saver) { SearchPageStates() }
    val pager = rememberFilterPager(
        selectedIndex = paged?.selectedIndex ?: 0,
        onSelectedIndexChange = { index -> paged?.chips?.getOrNull(index)?.let { callbacks.onType(it.key) } },
        pageCount = paged?.chips?.size ?: 0,
    )
    val pageScrim = rememberShownPageScrim(pager, paged, pageStates)
    BingeScreenScaffold(
        title = title,
        modifier = modifier,
        onBack = callbacks.onBack,
        bar = bar,
        snackbarHostState = snackbarHostState,
        scrollBehavior = scrollBehavior,
        scrimFloor = pageScrim,
        titleTrailing = titleTrailing?.let { { it(titleRowShows) } },
        actions = { barActions(!titleRowShows, collapsed) },
        header = {
            SearchControls(text, state, callbacks, placeholder, reselectKey, pager.takeIf { paged != null }, reserveTypeChipsWhileLoading)
        },
    ) { padding ->
        ScreenBody(padding) { body ->
            val top = body.calculateTopPadding()
            CenteredContent(maxWidth = dimensionResource(R.dimen.search_content_max_width)) { capped ->
                Box(capped.fillMaxSize()) {
                    SearchBody(
                        state = state,
                        callbacks = callbacks,
                        top = top,
                        bottom = body.calculateBottomPadding(),
                        idleTitle = idleTitle,
                        idleMessage = idleMessage,
                        loadingLabel = loadingLabel,
                        recents = RecentsCopy(recentsTitle, clearLabel, clearTitle, clearMessage),
                        moreFailedLabel = moreFailedLabel,
                        resultTrailing = resultTrailing,
                        idleExtras = idleExtras,
                        noResultsExtras = noResultsExtras,
                        failedContent = failedContent,
                        paged = paged,
                        pager = pager,
                        pageStates = pageStates,
                        expectedResultName = expectedResultName,
                        fastScrollbar = fastScrollbar,
                    )
                }
            }
        }
    }
    overlays()
}

/**
 * The scrim the page that is shown asks for, apart from the bar's own: up once that page is scrolled off its top, and down at its top.
 * The bar's collapse is not set from a page, so a swipe or a chip tap never makes the header jump; this is what keeps rows from showing
 * under an expanded header when the page that arrives is already scrolled. It fades, as the pagers' scrim does.
 */
@Composable
private fun rememberShownPageScrim(
    pager: FilterPager,
    paged: PagedTypes?,
    pageStates: SearchPageStates,
): Float {
    val scrolled by remember(pager, paged, pageStates) {
        derivedStateOf {
            val chip = paged?.chips?.getOrNull(pager.shownIndex)
            val grid = chip?.let { pageStates.stateFor(it.key) }
            grid != null && (grid.firstVisibleItemIndex > 0 || grid.firstVisibleItemScrollOffset > 0)
        }
    }
    val scrim by animateFloatAsState(
        targetValue = if (scrolled) 1f else 0f,
        animationSpec = tween(durationMillis = PAGE_SCRIM_FADE_MILLIS),
        label = "shown page scrim",
    )
    return scrim
}

private const val PAGE_SCRIM_FADE_MILLIS = 150

private class RecentsCopy(
    val title: String,
    val clearLabel: String,
    val clearTitle: String,
    val clearMessage: String,
)

@Composable
private fun SearchBody(
    state: SearchState,
    callbacks: SearchCallbacks,
    top: Dp,
    bottom: Dp,
    idleTitle: String,
    idleMessage: String,
    loadingLabel: String,
    recents: RecentsCopy,
    moreFailedLabel: String,
    resultTrailing: ((MediaResult) -> (@Composable () -> Unit)?)?,
    idleExtras: (LazyListScope.() -> Unit)?,
    noResultsExtras: (@Composable () -> Unit)?,
    failedContent: (@Composable (Modifier) -> Unit)?,
    paged: PagedTypes?,
    pager: FilterPager,
    pageStates: SearchPageStates,
    expectedResultName: String?,
    fastScrollbar: FastScrollbarOptions?,
) {
    // The plates (no results, failed, a type with nothing in it) clear the navigation bar and the floating pill as the lists do.
    val platePadding = navOverlayPadding(PaddingValues(top = top, bottom = bottom))
    when (state) {
        is SearchState.Idle ->
            IdleBody(
                state = state,
                callbacks = callbacks,
                contentPadding = navOverlayPadding(PaddingValues(top = top, bottom = bottom), bleedStart = true),
                idleTitle = idleTitle,
                idleMessage = idleMessage,
                recents = recents,
                idleExtras = idleExtras,
            )
        SearchState.Loading ->
            ResultsSkeleton(
                contentPadding = resultsPadding(top, bottom),
                loadingLabel = loadingLabel,
                expectedResultName = expectedResultName,
            )
        is SearchState.Ready ->
            if (paged != null) {
                HorizontalPager(
                    state = pager.state,
                    modifier = Modifier.fillMaxSize(),
                    key = { paged.pageKey(it) },
                ) { page ->
                    val results = paged.pages[paged.chips[page].key]
                    if (results == null) {
                        ResultsSkeleton(
                            contentPadding = resultsPadding(top, bottom),
                            loadingLabel = loadingLabel,
                            expectedResultName = expectedResultName,
                        )
                    } else {
                        ResultsList(
                            state = state,
                            results = results,
                            callbacks = callbacks,
                            contentPadding = resultsPadding(top, bottom),
                            moreFailedLabel = moreFailedLabel,
                            resultTrailing = resultTrailing,
                            platePadding = platePadding,
                            fastScrollbar = fastScrollbar,
                            gridState = pageStates.stateFor(paged.chips[page].key),
                        )
                    }
                }
            } else {
                key(state.types?.selectedKey) {
                    ResultsList(
                        state = state,
                        results = state.results,
                        callbacks = callbacks,
                        contentPadding = resultsPadding(top, bottom),
                        moreFailedLabel = moreFailedLabel,
                        resultTrailing = resultTrailing,
                        platePadding = platePadding,
                        fastScrollbar = fastScrollbar,
                    )
                }
            }
        is SearchState.NoResults ->
            if (paged != null) {
                HorizontalPager(
                    state = pager.state,
                    modifier = Modifier.fillMaxSize(),
                    key = { paged.pageKey(it) },
                ) { NoResultsBody(state, platePadding, noResultsExtras) }
            } else {
                NoResultsBody(state, platePadding, noResultsExtras)
            }
        is SearchState.Failed ->
            if (failedContent != null) {
                failedContent(Modifier.fillMaxSize().padding(platePadding))
            } else {
                ErrorScreen(
                    kind = state.kind,
                    onRetry = callbacks.onRetry.takeIf { state.retryable },
                    modifier = Modifier.fillMaxSize().padding(platePadding),
                    title = state.title,
                    message = state.message,
                    secondary = state.secondary,
                )
            }
    }
}

@Composable
private fun NoResultsBody(
    state: SearchState.NoResults,
    platePadding: PaddingValues,
    noResultsExtras: (@Composable () -> Unit)?,
) {
    Column(Modifier.fillMaxSize().padding(platePadding)) {
        EmptyScreen(
            message = state.message,
            modifier = Modifier.weight(1f),
            title = state.title ?: stringResource(R.string.empty_screen_title),
        )
        noResultsExtras?.invoke()
    }
}

/**
 * The grid state of each page, by the page's chip key and nothing else. It is held by [SearchScreen] itself, above everything that follows the state,
 * so a page the pager disposes, another state shown in between (loading, say), a new state or types or map on every tap and a recreated activity all
 * leave a page where its list was. A new query starts every page afresh. Saved as the first visible item and its offset, so it outlives the process too.
 */
@Stable
private class SearchPageStates {
    private val states = mutableMapOf<String, LazyGridState>()

    fun stateFor(chipKey: String): LazyGridState = states.getOrPut(chipKey) { LazyGridState() }

    companion object {
        val Saver: Saver<SearchPageStates, Any> =
            listSaver(
                save = { holder ->
                    holder.states.flatMap { (key, state) -> listOf(key, state.firstVisibleItemIndex, state.firstVisibleItemScrollOffset) }
                },
                restore = { saved ->
                    SearchPageStates().also { holder ->
                        saved.chunked(PAGE_STATE_FIELDS).forEach { (key, index, offset) ->
                            holder.states[key as String] = LazyGridState(index as Int, offset as Int)
                        }
                    }
                },
            )
    }
}

private const val PAGE_STATE_FIELDS = 3

/** The query the last results answered, held across the states that carry none. Written with the same value on every recomposition, so it is not snapshot state. */
private class AnsweredQuery {
    var value: String? = null
}

/** The chips a paged body swipes between, the one chosen, and each chip's results; null when the state does not page. */
@Immutable
private class PagedTypes(
    val chips: List<SearchTypeChip>,
    val selectedIndex: Int,
    val pages: Map<String, List<MediaResult>>,
) {
    /** Under the reserved prefix, so an app's own key can never collide with a page's. */
    fun pageKey(page: Int): String = SEARCH_PAGE_KEY_PREFIX + chips[page].key
}

private fun SearchState.pagedTypes(): PagedTypes? {
    val (types, pages) =
        when (this) {
            is SearchState.Ready -> types to this.pages
            is SearchState.NoResults -> types to this.pages
            else -> return null
        }
    if (types == null || pages == null || types.chips.size < 2) return null
    return PagedTypes(types.chips, types.chips.indexOfFirst { it.key == types.selectedKey }.coerceAtLeast(0), pages)
}

/** The content padding of the results grid and of its skeleton, so the one is laid out exactly as the other. */
@Composable
private fun resultsPadding(top: Dp, bottom: Dp): PaddingValues =
    navOverlayPadding(
        PaddingValues(
            start = dimensionResource(R.dimen.screen_content_inset),
            top = top + dimensionResource(R.dimen.padding_s),
            end = dimensionResource(R.dimen.screen_content_inset),
            bottom = bottom + dimensionResource(R.dimen.padding_m),
        ),
    )

/** What a search draws while it loads: result rows' plates in the results grid's own columns, so the grid does not jump when they arrive. */
@Composable
private fun ResultsSkeleton(
    contentPadding: PaddingValues,
    loadingLabel: String,
    expectedResultName: String?,
) {
    val columns = searchResultColumns()
    CenteredContent(maxWidth = dimensionResource(R.dimen.search_content_max_width)) { capped ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(columns),
            modifier = capped,
            contentPadding = contentPadding,
            horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_s)),
            verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_s)),
            userScrollEnabled = false,
        ) {
            items(columns * SEARCH_SKELETON_ROWS) { index ->
                MediaResultRowSkeleton(
                    modifier = Modifier.layoutAnchorIf(index == 0, LayoutAnchors.section(LayoutAnchors.Collection.FIRST_ITEM)),
                    description = if (index == 0) loadingLabel else null,
                    expectedName = expectedResultName,
                )
            }
        }
    }
}

/** The field and, once there is something to narrow, the type chips: the bar's header, pinned while the results scroll. */
@Composable
private fun SearchControls(
    text: String,
    state: SearchState,
    callbacks: SearchCallbacks,
    placeholder: String,
    reselectKey: Any?,
    pager: FilterPager?,
    reserveTypeChips: Boolean,
) {
    val focus = remember { FocusRequester() }
    val keyboard = LocalSoftwareKeyboardController.current
    if (reselectKey != null) {
        OnTabReselect(reselectKey) {
            focus.requestFocus()
            keyboard?.show()
        }
    }
    val types =
        when (state) {
            is SearchState.Ready -> state.types
            is SearchState.NoResults -> state.types
            else -> null
        }
    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.TopCenter) {
        Column(Modifier.widthIn(max = dimensionResource(R.dimen.search_content_max_width)).fillMaxWidth()) {
            BingeSearchFieldHeader {
                BingeSearchField(
                    query = text,
                    onQueryChange = callbacks.onQueryChange,
                    onClear = callbacks.onClear,
                    onSubmit = callbacks.onSubmit,
                    placeholder = placeholder,
                    focusRequester = focus,
                )
            }
            if (reserveTypeChips && state is SearchState.Loading) {
                // The results will bring their type chips, so the row is held while they load and the first result does not drop when they arrive.
                FilterChipRowSkeleton()
            } else if (types != null && types.chips.isNotEmpty()) {
                BingeFilterChipRow(
                    items = types.chips.map { FilterChipItem(label = it.label, count = it.count) },
                    selectedIndex = pager?.shownIndex ?: types.chips.indexOfFirst { it.key == types.selectedKey }.coerceAtLeast(0),
                    onSelect = { callbacks.onType(types.chips[it].key) },
                )
            }
        }
    }
}

@Composable
private fun IdleBody(
    state: SearchState.Idle,
    callbacks: SearchCallbacks,
    contentPadding: PaddingValues,
    idleTitle: String,
    idleMessage: String,
    recents: RecentsCopy,
    idleExtras: (LazyListScope.() -> Unit)?,
) {
    if (state.recents.isEmpty() && !state.hasExtras) {
        EmptyScreen(
            message = idleMessage,
            title = idleTitle,
            icon = Icons.Filled.Search,
            modifier = Modifier.padding(contentPadding).padding(start = navOverlayStart()),
        )
        return
    }
    val direction = LocalLayoutDirection.current
    val listPadding =
        PaddingValues(
            start = contentPadding.calculateStartPadding(direction),
            top = contentPadding.calculateTopPadding(),
            end = contentPadding.calculateEndPadding(direction),
            bottom = contentPadding.calculateBottomPadding() + dimensionResource(R.dimen.padding_m),
        )
    LazyColumn(contentPadding = listPadding) {
        if (state.recents.isNotEmpty()) {
            item(key = "binge:search-recents") {
                RecentSearches(state.recents, recents, onSelect = callbacks.onPickRecent, onClear = callbacks.onClearRecents)
            }
        }
        idleExtras?.invoke(this)
    }
}

/** The queries searched before, newest first, as chips, with Clear behind a confirmation. A tap searches the query again. */
@Composable
private fun RecentSearches(
    recents: List<String>,
    copy: RecentsCopy,
    onSelect: (String) -> Unit,
    onClear: () -> Unit,
) {
    var confirming by rememberSaveable { mutableStateOf(false) }
    val gutter = dimensionResource(R.dimen.screen_content_inset)
    Column(Modifier.fillMaxWidth().padding(start = gutter + navOverlayStart(), end = gutter)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(text = copy.title, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
            TextButton(onClick = { confirming = true }) {
                Icon(Icons.Filled.DeleteSweep, contentDescription = null)
                Text(copy.clearLabel, modifier = Modifier.padding(start = dimensionResource(R.dimen.padding_s)))
            }
        }
        WrappingRow(
            horizontalSpacing = dimensionResource(R.dimen.padding_s),
            verticalSpacing = dimensionResource(R.dimen.padding_s),
        ) {
            recents.forEach { query ->
                TrendingSearchChip(label = query, onClick = { onSelect(query) }, leadingIcon = Icons.Filled.History)
            }
        }
    }
    if (confirming) {
        BingeConfirmDialog(
            title = copy.clearTitle,
            message = copy.clearMessage,
            confirmLabel = copy.clearLabel,
            destructive = true,
            onConfirm = {
                confirming = false
                onClear()
            },
            onDismissRequest = { confirming = false },
        )
    }
}

@Composable
private fun ResultsList(
    state: SearchState.Ready,
    results: List<MediaResult>,
    callbacks: SearchCallbacks,
    contentPadding: PaddingValues,
    moreFailedLabel: String,
    resultTrailing: ((MediaResult) -> (@Composable () -> Unit)?)?,
    platePadding: PaddingValues,
    fastScrollbar: FastScrollbarOptions?,
    gridState: LazyGridState = rememberLazyGridState(),
) {
    val nearEnd by remember {
        derivedStateOf {
            val info = gridState.layoutInfo
            isNearEnd(info.visibleItemsInfo.lastOrNull()?.index ?: 0, info.totalItemsCount, ahead = SEARCH_LOAD_MORE_AHEAD)
        }
    }
    // Keyed on the paging flags too, so a page that added nothing still asks again while the app says there is more; see PagedGridScreen.
    LaunchedEffect(gridState, state.hasMore, state.loaded, state.appending, state.appendFailed) {
        if (state.hasMore &&
            !state.appending &&
            !state.appendFailed
        ) {
            snapshotFlow { nearEnd }.distinctUntilChanged().filter { it }.collect { callbacks.onLoadMore() }
        }
    }
    val empty = results.isEmpty()
    val settled = !state.hasMore && !state.appending && !state.appendFailed
    if (empty && settled) {
        EmptyScreen(
            message = state.emptyMessage,
            modifier = Modifier.fillMaxSize().padding(platePadding),
            title = state.emptyTitle ?: stringResource(R.string.empty_screen_title),
        )
        return
    }
    // One column on a phone; wider windows fold the results into columns rather than running a row the width of a tablet.
    CenteredContent(maxWidth = dimensionResource(R.dimen.search_content_max_width)) { capped ->
        FastScrollbarOverlay(fastScrollbar, gridState, contentPadding, capped) { gridModifier ->
            LazyVerticalGrid(
                columns = GridCells.Fixed(searchResultColumns()),
                state = gridState,
                modifier = gridModifier,
                contentPadding = contentPadding,
                horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_s)),
                verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_s)),
            ) {
                itemsIndexed(results, key = { _, result -> result.key }) { index, result ->
                    MediaResultRow(
                        result = result,
                        onClick = { callbacks.onOpenResult(result.key) },
                        modifier = Modifier.layoutAnchorIf(index == 0, LayoutAnchors.section(LayoutAnchors.Collection.FIRST_ITEM)),
                        trailing = resultTrailing?.invoke(result),
                    )
                }
                if (state.appending || state.appendFailed || empty) {
                    item(key = FOOTER_KEY, span = { GridItemSpan(maxLineSpan) }) {
                        AppendFooter(state.appendFailed, moreFailedLabel, callbacks.onLoadMore)
                    }
                }
            }
        }
    }
}
