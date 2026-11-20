package com.binge.designsystem.template

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import com.binge.designsystem.component.FilterChipItem
import com.binge.designsystem.component.MediaResult
import com.binge.designsystem.component.MediaResultRow
import com.binge.designsystem.component.OnTabReselect
import com.binge.designsystem.component.TrendingSearchChip
import com.binge.designsystem.component.WrappingRow
import com.binge.designsystem.component.rememberPaneTopBarScrollBehavior
import com.binge.designsystem.navOverlayPadding
import com.binge.designsystem.navOverlayStart
import com.binge.designsystem.searchResultColumns
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter

/** How many results from the end the list asks for the next page. */
const val SEARCH_LOAD_MORE_AHEAD: Int = 3

/** The type chips under the field. A chip's count is optional, so an app with none passes none. */
@Immutable
class SearchTypes(
    val chips: List<FilterChipItem>,
    val selected: Int,
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
     * load-more. A settled list with no results shows [emptyTitle] and [emptyMessage].
     */
    class Ready(
        val results: List<MediaResult>,
        val loaded: Int,
        val hasMore: Boolean,
        val appending: Boolean = false,
        val appendFailed: Boolean = false,
        val types: SearchTypes? = null,
        val emptyTitle: String = "",
        val emptyMessage: String = "",
    ) : SearchState

    /** The query found nothing; [types] keeps the chips, with whatever counts the app gives them. */
    class NoResults(
        val title: String,
        val message: String,
        val types: SearchTypes? = null,
    ) : SearchState

    /** The search failed. [title] and [message] default to [kind]'s own words; Try again shows only when [retryable]. */
    class Failed(
        val kind: ErrorKind,
        val title: String? = null,
        val message: String? = null,
        val retryable: Boolean = true,
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
    val onType: (index: Int) -> Unit = {},
    val onPickRecent: (query: String) -> Unit = {},
    val onClearRecents: () -> Unit = {},
    val onBack: (() -> Unit)? = null,
)

/** Whether the last visible result is within [ahead] of the end. */
fun isSearchNearEnd(
    lastVisibleIndex: Int,
    totalCount: Int,
    ahead: Int = SEARCH_LOAD_MORE_AHEAD,
): Boolean = lastVisibleIndex >= totalCount - ahead

/**
 * A search screen on the large collapsing bar: the title gives way as results scroll, and the field and the type chips stay pinned under
 * it. Below is whichever of the idle offer (recent searches, then the app's own rails), a spinner, a grid of [MediaResultRow]s that loads
 * more near its end, a no-results plate or a failure the state calls for. The grid is capped at the search width and folds into
 * columns on wider windows.
 *
 * Nothing here is an app's type: results are [MediaResult]s with a text key, each result's status is the [resultTrailing] slot, and the
 * app owns the query, the history and the paging. [titleTrailing] and [barActions] are told how far the bar has collapsed (they
 * switch at [BAR_HANDOVER_COLLAPSE]). [idleExtras] adds items under the recents, [noResultsExtras] sits under the no-results plate, and
 * [failedContent] replaces the default failure plate.
 *
 * @param reselectKey the tab this screen is the root of; tapping it again focuses the field. Null for a screen with no tab.
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
    idleTitle: String,
    idleMessage: String,
    modifier: Modifier = Modifier,
    reselectKey: String? = null,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    recentsTitle: String = stringResource(R.string.search_recents_title),
    clearLabel: String = stringResource(R.string.search_recents_clear),
    clearTitle: String = stringResource(R.string.search_recents_clear_title),
    clearMessage: String = stringResource(R.string.search_recents_clear_message),
    moreFailedLabel: String = stringResource(R.string.search_more_failed),
    titleTrailing: (@Composable (pillInteractive: Boolean) -> Unit)? = null,
    barActions: @Composable RowScope.(handedOver: Boolean, collapsedFraction: Float) -> Unit = { _, _ -> },
    resultTrailing: (@Composable (MediaResult) -> Unit)? = null,
    idleExtras: (LazyListScope.() -> Unit)? = null,
    noResultsExtras: (@Composable () -> Unit)? = null,
    failedContent: (@Composable (Modifier) -> Unit)? = null,
    overlays: @Composable () -> Unit = {},
) {
    val scrollBehavior = rememberPaneTopBarScrollBehavior()
    val collapsed = scrollBehavior.state.collapsedFraction
    val titleRowShows = collapsed < BAR_HANDOVER_COLLAPSE
    BingeScreenScaffold(
        title = title,
        modifier = modifier,
        onBack = callbacks.onBack,
        bar = ScreenBar.Large,
        snackbarHostState = snackbarHostState,
        scrollBehavior = scrollBehavior,
        titleTrailing = titleTrailing?.let { { it(titleRowShows) } },
        actions = { barActions(!titleRowShows, collapsed) },
        header = { SearchControls(text, state, callbacks, placeholder, reselectKey) },
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
                        recents = RecentsCopy(recentsTitle, clearLabel, clearTitle, clearMessage),
                        moreFailedLabel = moreFailedLabel,
                        resultTrailing = resultTrailing,
                        idleExtras = idleExtras,
                        noResultsExtras = noResultsExtras,
                        failedContent = failedContent,
                    )
                }
            }
        }
    }
    overlays()
}

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
    recents: RecentsCopy,
    moreFailedLabel: String,
    resultTrailing: (@Composable (MediaResult) -> Unit)?,
    idleExtras: (LazyListScope.() -> Unit)?,
    noResultsExtras: (@Composable () -> Unit)?,
    failedContent: (@Composable (Modifier) -> Unit)?,
) {
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
        SearchState.Loading -> LoadingMessageScreen(Modifier.padding(top = top))
        is SearchState.Ready ->
            key(state.types?.selected) {
                ResultsList(
                    state = state,
                    callbacks = callbacks,
                    contentPadding =
                        navOverlayPadding(
                            PaddingValues(
                                start = dimensionResource(R.dimen.screen_content_inset),
                                top = top + dimensionResource(R.dimen.padding_s),
                                end = dimensionResource(R.dimen.screen_content_inset),
                                bottom = bottom + dimensionResource(R.dimen.padding_m),
                            ),
                        ),
                    moreFailedLabel = moreFailedLabel,
                    resultTrailing = resultTrailing,
                )
            }
        is SearchState.NoResults ->
            Column(Modifier.fillMaxSize().padding(top = top)) {
                EmptyScreen(message = state.message, modifier = Modifier.weight(1f), title = state.title)
                noResultsExtras?.invoke()
            }
        is SearchState.Failed ->
            if (failedContent != null) {
                failedContent(Modifier.fillMaxSize().padding(top = top))
            } else {
                ErrorScreen(
                    kind = state.kind,
                    onRetry = callbacks.onRetry.takeIf { state.retryable },
                    modifier = Modifier.fillMaxSize().padding(top = top),
                    title = state.title,
                    message = state.message,
                )
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
    reselectKey: String?,
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
            Box(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = dimensionResource(R.dimen.screen_content_inset))
                    .padding(bottom = dimensionResource(R.dimen.padding_m)),
            ) {
                BingeSearchField(
                    query = text,
                    onQueryChange = callbacks.onQueryChange,
                    onClear = callbacks.onClear,
                    onSubmit = callbacks.onSubmit,
                    placeholder = placeholder,
                    focusRequester = focus,
                )
            }
            if (types != null && types.chips.isNotEmpty()) {
                BingeFilterChipRow(items = types.chips, selectedIndex = types.selected, onSelect = callbacks.onType)
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
            item(key = "recents") {
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
    callbacks: SearchCallbacks,
    contentPadding: PaddingValues,
    moreFailedLabel: String,
    resultTrailing: (@Composable (MediaResult) -> Unit)?,
) {
    val gridState = rememberLazyGridState()
    val nearEnd by remember {
        derivedStateOf {
            val info = gridState.layoutInfo
            isSearchNearEnd(info.visibleItemsInfo.lastOrNull()?.index ?: 0, info.totalItemsCount)
        }
    }
    LaunchedEffect(gridState, state.hasMore, state.loaded) {
        if (state.hasMore) snapshotFlow { nearEnd }.distinctUntilChanged().filter { it }.collect { callbacks.onLoadMore() }
    }
    val empty = state.results.isEmpty()
    val settled = !state.hasMore && !state.appending && !state.appendFailed
    if (empty && settled) {
        EmptyScreen(message = state.emptyMessage, title = state.emptyTitle)
        return
    }
    // One column on a phone; wider windows fold the results into columns rather than running a row the width of a tablet.
    CenteredContent(maxWidth = dimensionResource(R.dimen.search_content_max_width)) { capped ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(searchResultColumns()),
            state = gridState,
            modifier = capped,
            contentPadding = contentPadding,
            horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_s)),
            verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_s)),
        ) {
            items(state.results, key = { it.key }) { result ->
                MediaResultRow(
                    result = result,
                    onClick = { callbacks.onOpenResult(result.key) },
                    trailing = resultTrailing?.let { slot -> { slot(result) } },
                )
            }
            if (state.appending || state.appendFailed || empty) {
                item(key = "footer", span = { GridItemSpan(maxLineSpan) }) {
                    SearchFooter(state.appendFailed, moreFailedLabel, callbacks.onLoadMore)
                }
            }
        }
    }
}

@Composable
private fun SearchFooter(
    failed: Boolean,
    failedLabel: String,
    onRetry: () -> Unit,
) {
    Box(Modifier.fillMaxWidth().padding(vertical = dimensionResource(R.dimen.padding_m)), contentAlignment = Alignment.Center) {
        if (failed) {
            TextButton(onClick = onRetry) {
                Icon(Icons.Filled.Refresh, contentDescription = null)
                Text(
                    failedLabel,
                    modifier = Modifier.padding(start = dimensionResource(R.dimen.padding_s)),
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        } else {
            CircularProgressIndicator()
        }
    }
}
