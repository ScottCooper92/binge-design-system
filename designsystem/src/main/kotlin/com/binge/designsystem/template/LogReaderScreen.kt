package com.binge.designsystem.template

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.pulltorefresh.PullToRefreshState
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import com.binge.designsystem.ErrorKind
import com.binge.designsystem.R
import com.binge.designsystem.component.BingeFilterChipRow
import com.binge.designsystem.component.BingePullToRefresh
import com.binge.designsystem.component.BingeSearchField
import com.binge.designsystem.component.BingeSearchFieldHeader
import com.binge.designsystem.component.FilterChipItem
import com.binge.designsystem.component.FontScaleHeight
import com.binge.designsystem.component.ListRowSkeletonColumn
import com.binge.designsystem.component.LogRow
import com.binge.designsystem.component.LogRowEntry
import com.binge.designsystem.layout.LayoutAnchors
import com.binge.designsystem.layout.layoutAnchorIf
import com.binge.designsystem.navOverlayPadding

/** How many rows from the end the list asks for the next page. */
const val LOG_READER_LOAD_MORE_AHEAD: Int = 6

/** The lazy key of the append footer. It carries the reserved `binge:` prefix so it cannot collide with an app's row ids. */
private const val FOOTER_KEY = "binge:log-reader-footer"

/** What a [LogReaderScreen] draws under its level chips and search field. */
@Immutable
sealed interface LogReaderState {
    /** The first page of the selected level is on its way. */
    data object Loading : LogReaderState

    /**
     * [rows] of the selected level that match the query. The footer shows while [appending] a page, or when the last page [appendFailed].
     * [refreshing] is the app's own re-read of the list, which the pull's spinner shows while it is true; it has no effect unless
     * [LogReaderCallbacks.onRefresh] is set.
     */
    class Ready(
        val rows: List<LogRowEntry>,
        val hasMore: Boolean,
        val appending: Boolean = false,
        val appendFailed: Boolean = false,
        val refreshing: Boolean = false,
    ) : LogReaderState

    /** Nothing to list, said under its [icon] with an optional [action]. [title] null says "Nothing here yet". */
    class Empty(
        val message: String,
        val title: String? = null,
        val icon: ImageVector = Icons.Filled.SearchOff,
        val action: ScreenAction? = null,
    ) : LogReaderState

    /** The load failed. [title] and [message] default to [kind]'s own words; Try again shows only when [retryable]. */
    class Failed(
        val kind: ErrorKind,
        val title: String? = null,
        val message: String? = null,
        val retryable: Boolean = true,
    ) : LogReaderState
}

/**
 * What the screen reports. [onSelectLevel] gets the chip's index in `levels`; [onCopy] gets the row to put on the clipboard (a long press, or the
 * row's Copy line button). [onBack] null draws no back control.
 *
 * [onRefresh], when set, lets a pull down on the rows re-read the list (the spinner follows [LogReaderState.Ready.refreshing]). [onAtTopChange],
 * when set, is told whether the list is scrolled to its first row, so an app can re-read the newest lines on its own interval only while the
 * reader is at the top; it is told `true` while there are no rows to scroll.
 */
@Immutable
class LogReaderCallbacks(
    val onSelectLevel: (Int) -> Unit,
    val onQueryChange: (String) -> Unit,
    val onLoadMore: () -> Unit,
    val onRetry: () -> Unit,
    val onCopy: (LogRowEntry) -> Unit,
    val onBack: (() -> Unit)? = null,
    val onRefresh: (() -> Unit)? = null,
    val onAtTopChange: ((Boolean) -> Unit)? = null,
)

/** Whether the list's footer (a spinner, or Retry) is drawn. */
fun showsLogReaderFooter(ready: LogReaderState.Ready): Boolean = ready.appending || ready.appendFailed

/**
 * A log reader: the level chips and a search field pinned under the bar, then a skeleton, an empty or failed plate, or a paged list of [LogRow]s
 * that asks for more near its end and ends in a spinner or Retry. The bar, chips and field stay put across all four states, so typing or
 * changing level never moves them.
 *
 * Nothing here is an app's type: [levels] are plain chips (an "All" chip is the caller's first), rows are [LogRowEntry]s, and the app owns its
 * paging per level and its filtering by [query]. Changing level or query is the app's to answer with a new [state]; the list scrolls back to
 * the top when [selected] changes.
 *
 * @param query the search text; [callbacks]' `onQueryChange` gets every edit, and the clear button calls it with an empty string.
 * @param searchPlaceholder the field's hint.
 * @param loadingLabel what a screen reader says of the loading plates.
 * @param moreFailedLabel the footer's Try again label, shown when the last page failed to load.
 * @param snackbarHostState the host for the app's own snackbars, shown over the list.
 * @param pullState the pull's own state, for a still frame that must show the spinner; null leaves it to the default.
 *
 * @need read logs: level filter chips, a search field and log rows in a paged list
 * @group screens
 * @replaces a hand-built pager of level chips, a search field and log rows over a paged flow
 * @sample LogReaderReadySample
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogReaderScreen(
    title: String,
    levels: List<FilterChipItem>,
    selected: Int,
    query: String,
    state: LogReaderState,
    callbacks: LogReaderCallbacks,
    modifier: Modifier = Modifier,
    searchPlaceholder: String = stringResource(R.string.log_reader_search_placeholder),
    loadingLabel: String = stringResource(R.string.log_reader_loading),
    moreFailedLabel: String = stringResource(R.string.list_more_failed),
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    pullState: PullToRefreshState? = null,
    actions: @Composable RowScope.(glassBackgroundAlpha: Float) -> Unit = {},
) {
    BingeScreenScaffold(
        title = title,
        modifier = modifier,
        onBack = callbacks.onBack,
        bar = ScreenBar.Small,
        snackbarHostState = snackbarHostState,
        actions = actions,
        header = {
            BingeSearchFieldHeader {
                BingeSearchField(
                    query = query,
                    onQueryChange = callbacks.onQueryChange,
                    onClear = { callbacks.onQueryChange("") },
                    placeholder = searchPlaceholder,
                )
            }
            BingeFilterChipRow(
                items = levels,
                selectedIndex = selected,
                onSelect = callbacks.onSelectLevel,
                modifier = Modifier.padding(bottom = dimensionResource(R.dimen.padding_s)),
            )
        },
    ) { padding ->
        val inner =
            navOverlayPadding(
                PaddingValues(
                    start = dimensionResource(R.dimen.screen_content_inset),
                    top = padding.calculateTopPadding() + dimensionResource(R.dimen.padding_s),
                    end = dimensionResource(R.dimen.screen_content_inset),
                    bottom = padding.calculateBottomPadding() + dimensionResource(R.dimen.padding_m),
                ),
            )
        // Not at a list's top when there is no list, so an app that follows the newest lines keeps following an empty or loading reader.
        val notReady = state !is LogReaderState.Ready
        LaunchedEffect(notReady) { if (notReady) callbacks.onAtTopChange?.invoke(true) }
        when (state) {
            LogReaderState.Loading ->
                ListRowSkeletonColumn(
                    contentPadding = inner,
                    height = dimensionResource(R.dimen.log_reader_skeleton_row_height),
                    fontScaleHeights =
                        listOf(
                            FontScaleHeight(1f, dimensionResource(R.dimen.log_reader_skeleton_row_height)),
                            FontScaleHeight(2f, dimensionResource(R.dimen.log_reader_skeleton_row_height_2x)),
                        ),
                    description = loadingLabel,
                )
            is LogReaderState.Empty ->
                EmptyScreen(
                    message = state.message,
                    modifier = Modifier.padding(inner),
                    title = state.title ?: stringResource(R.string.empty_screen_title),
                    icon = state.icon,
                    action = state.action,
                )
            is LogReaderState.Failed ->
                ErrorScreen(
                    kind = state.kind,
                    onRetry = callbacks.onRetry.takeIf { state.retryable },
                    modifier = Modifier.padding(inner),
                    title = state.title,
                    message = state.message,
                )
            is LogReaderState.Ready ->
                // Keyed on the level, so each level opens at its own top rather than where the last one was scrolled.
                key(selected) {
                    val onRefresh = callbacks.onRefresh
                    if (onRefresh != null) {
                        BingePullToRefresh(
                            isRefreshing = state.refreshing,
                            onRefresh = onRefresh,
                            modifier = Modifier.fillMaxSize(),
                            indicatorTopInset = padding.calculateTopPadding(),
                            state = pullState ?: rememberPullToRefreshState(),
                        ) { ReadyLogRows(state, inner, callbacks, moreFailedLabel) }
                    } else {
                        ReadyLogRows(state, inner, callbacks, moreFailedLabel)
                    }
                }
        }
    }
}

@Composable
private fun ReadyLogRows(
    state: LogReaderState.Ready,
    contentPadding: PaddingValues,
    callbacks: LogReaderCallbacks,
    moreFailedLabel: String,
) {
    val listState = rememberLazyListState()
    val atTop by remember { derivedStateOf { listState.firstVisibleItemIndex == 0 && listState.firstVisibleItemScrollOffset == 0 } }
    LaunchedEffect(atTop) { callbacks.onAtTopChange?.invoke(atTop) }
    LoadMoreEffect(
        key = listState,
        itemCount = state.rows.size,
        hasMore = state.hasMore,
        appending = state.appending,
        appendFailed = state.appendFailed,
        ahead = LOG_READER_LOAD_MORE_AHEAD,
        lastVisibleIndex = {
            listState.layoutInfo.visibleItemsInfo
                .lastOrNull()
                ?.index ?: 0
        },
        totalCount = { listState.layoutInfo.totalItemsCount },
        onLoadMore = callbacks.onLoadMore,
    )
    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = contentPadding,
        verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.list_row_spacing)),
    ) {
        itemsIndexed(state.rows, key = { _, entry -> entry.id }) { index, entry ->
            // The anchor the skeleton's first plate carries, so a test can hold the plate to the row it stands in for.
            LogRow(
                entry = entry,
                onCopy = { callbacks.onCopy(entry) },
                modifier = Modifier.layoutAnchorIf(index == 0, LayoutAnchors.section(LayoutAnchors.Collection.FIRST_ITEM)),
            )
        }
        if (showsLogReaderFooter(state)) {
            item(key = FOOTER_KEY) { AppendFooter(state.appendFailed, moreFailedLabel, callbacks.onLoadMore) }
        }
    }
}
