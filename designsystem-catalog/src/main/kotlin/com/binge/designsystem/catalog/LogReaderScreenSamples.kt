@file:OnePerScreen(fullScreen = true)
@file:SelfDescribing

package com.binge.designsystem.catalog

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.binge.designsystem.ErrorKind
import com.binge.designsystem.component.BingeMessageDialog
import com.binge.designsystem.component.FilterChipItem
import com.binge.designsystem.component.LogRowEntry
import com.binge.designsystem.component.LogRowLevel
import com.binge.designsystem.preview.ScreenshotTheme
import com.binge.designsystem.template.LogReaderCallbacks
import com.binge.designsystem.template.LogReaderScreen
import com.binge.designsystem.template.LogReaderState
import kotlinx.coroutines.delay

private const val PAGE_ROWS = 12
private const val LOAD_MILLIS = 900L

/** Few enough that the footer is on screen without a scroll. */
private const val SHORT_LIST_ROWS = 3

private val SampleLevels = listOf(FilterChipItem("All"), FilterChipItem("Error"), FilterChipItem("Warning"), FilterChipItem("Info"))

private val SampleFilters = listOf(null, LogRowLevel.Error, LogRowLevel.Warn, LogRowLevel.Info)

private val SampleMessages =
    listOf(
        "Unable to reach the instance",
        "Request failed after retrying: the instance did not answer within the timeout, so the queue was left as it was.",
        "Refresh finished",
        "Disk space is low on the library volume",
    )

private fun sampleRows(count: Int = PAGE_ROWS, from: Int = 0): List<LogRowEntry> =
    List(count) { i ->
        val n = from + i
        LogRowEntry(
            id = "log-$n",
            timestampMillis = null,
            timestampRaw = "2026-09-29T21:14:07.000Z",
            level = LogRowLevel.entries[n % LogRowLevel.entries.size],
            label = if (n % 3 == 0) null else "Sonarr",
            message = SampleMessages[n % SampleMessages.size],
            data = if (n % 4 == 1) "{\n  \"status\": 503\n}" else null,
        )
    }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SampleReader(
    state: LogReaderState,
    initialQuery: String = "",
    selected: Int = 0,
    refreshable: Boolean = false,
    pullState: PullToRefreshState? = null,
) {
    var level by remember { mutableIntStateOf(selected) }
    var query by remember { mutableStateOf(initialQuery) }
    var info by remember { mutableStateOf<String?>(null) }
    ScreenshotTheme(modifier = Modifier.fillMaxSize()) {
        LogReaderScreen(
            title = "Logs",
            levels = SampleLevels,
            selected = level,
            query = query,
            state = state,
            callbacks =
                LogReaderCallbacks(
                    onSelectLevel = { level = it },
                    onQueryChange = { query = it },
                    onLoadMore = {},
                    onRetry = {},
                    onCopy = { info = it.message },
                    onBack = LocalDemoBack.current,
                    onRefresh = if (refreshable) ({}) else null,
                ),
            pullState = pullState,
        )
        info?.let { BingeMessageDialog(title = "Copied", message = it, onDismiss = { info = null }) }
    }
}

/** Log rows of every level under the level chips and the search field; a long press copies a line. */
@Composable
fun LogReaderReadySample() = SampleReader(LogReaderState.Ready(sampleRows(), hasMore = true))

/** The first page of a level on its way: row plates the height of a collapsed log row, with the chips and field already in place. */
@Composable
fun LogReaderLoadingSample() = SampleReader(LogReaderState.Loading, selected = 1)

/** A search that matched nothing: an empty plate that names the query. */
@Composable
fun LogReaderEmptySample() =
    SampleReader(
        LogReaderState.Empty(title = "No matching lines", message = "Nothing in the Error log mentions \"timeout\"."),
        initialQuery = "timeout",
        selected = 1,
    )

/** The load failed: the default failure plate with Try again, under the same chips and field. */
@Composable
fun LogReaderFailedSample() = SampleReader(LogReaderState.Failed(ErrorKind.Network))

/** The next page loading: a short list with a spinner under the last row. */
@Composable
fun LogReaderAppendingSample() = SampleReader(LogReaderState.Ready(sampleRows(SHORT_LIST_ROWS), hasMore = true, appending = true))

/** The next page failed: a short list whose footer is a Retry button. */
@Composable
fun LogReaderAppendFailedSample() = SampleReader(LogReaderState.Ready(sampleRows(SHORT_LIST_ROWS), hasMore = true, appendFailed = true))

/** A pull down on the rows re-reads the list: the spinner rests under the bar while the app's own re-read runs. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogReaderRefreshingSample() =
    SampleReader(LogReaderState.Ready(sampleRows(), hasMore = true, refreshing = true), refreshable = true, pullState = RestingAtThreshold)

/** The reader live: pick a level, type to narrow the lines, scroll to page in more, and pull down to re-read. */
@Composable
fun LogReaderDemo() {
    var level by remember { mutableIntStateOf(0) }
    var query by remember { mutableStateOf("") }
    var rows by remember { mutableStateOf(sampleRows()) }
    var appending by remember { mutableStateOf(false) }
    var copied by remember { mutableStateOf<String?>(null) }
    var refreshing by remember { mutableStateOf(false) }
    LaunchedEffect(refreshing) {
        if (refreshing) {
            delay(LOAD_MILLIS)
            rows = sampleRows()
            refreshing = false
        }
    }
    LaunchedEffect(appending) {
        if (appending) {
            delay(LOAD_MILLIS)
            rows = rows + sampleRows(PAGE_ROWS, rows.size)
            appending = false
        }
    }
    val shown = rows.filter {
        (SampleFilters[level] == null || SampleFilters[level] == it.level) &&
            it.message.contains(query, ignoreCase = true)
    }
    ScreenshotTheme(modifier = Modifier.fillMaxSize()) {
        LogReaderScreen(
            title = "Logs",
            levels = SampleLevels,
            selected = level,
            query = query,
            state =
                if (shown.isEmpty()) {
                    LogReaderState.Empty(message = "No lines match.")
                } else {
                    LogReaderState.Ready(shown, hasMore = rows.size < PAGE_ROWS * 4, appending = appending, refreshing = refreshing)
                },
            callbacks =
                LogReaderCallbacks(
                    onSelectLevel = { level = it },
                    onQueryChange = { query = it },
                    onLoadMore = { appending = true },
                    onRetry = {},
                    onCopy = { copied = it.message },
                    onBack = LocalDemoBack.current,
                    onRefresh = { refreshing = true },
                ),
        )
        copied?.let { BingeMessageDialog(title = "Copied", message = it, onDismiss = { copied = null }) }
    }
}
