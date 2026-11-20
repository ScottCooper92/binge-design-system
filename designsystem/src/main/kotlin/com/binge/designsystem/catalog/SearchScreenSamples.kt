@file:OnePerScreen(fullScreen = true)
@file:SelfDescribing

package com.binge.designsystem.catalog

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import com.binge.designsystem.ErrorKind
import com.binge.designsystem.R
import com.binge.designsystem.component.BingeMessageDialog
import com.binge.designsystem.component.FilterChipItem
import com.binge.designsystem.component.MediaResult
import com.binge.designsystem.component.MediaResultKind
import com.binge.designsystem.component.StatusChip
import com.binge.designsystem.preview.ScreenshotTheme
import com.binge.designsystem.template.SearchCallbacks
import com.binge.designsystem.template.SearchScreen
import com.binge.designsystem.template.SearchState
import com.binge.designsystem.template.SearchTypes
import com.binge.designsystem.theme.BingeSentiment
import kotlinx.coroutines.delay

private const val SAMPLE_RESULTS = 8

/** Few enough that the footer sits on screen in a frame of the unscrolled list. */
private const val FEW_RESULTS = 3
private const val PAGE_RESULTS = 6
private const val LOAD_MILLIS = 900L

private val SampleNames = listOf("Past Lives", "Paddington 2", "Heat", "Arrival", "Drive", "Aliens", "Her", "Tár")

private fun sampleResults(
    kind: MediaResultKind = MediaResultKind.Poster,
    count: Int = SAMPLE_RESULTS,
    from: Int = 0,
): List<MediaResult> =
    List(count) { i ->
        val n = from + i
        MediaResult(
            key = "r$n",
            kind = kind,
            imageUrl = null,
            name = SampleNames[n % SampleNames.size],
            subtitle = if (kind == MediaResultKind.Avatar) "Actor" else "Movie · ${1990 + n}",
            rating = if (kind == MediaResultKind.Poster && n % 3 != 2) 6f + n % 4 else null,
            meta = if (kind == MediaResultKind.Poster && n % 2 == 0) "Drama, Romance" else null,
        )
    }

private val TypeLabels = listOf("All", "Movies", "Series", "People")

private fun types(selected: Int, counts: Boolean) =
    SearchTypes(TypeLabels.mapIndexed { i, label -> FilterChipItem(label, if (counts) 12 * (i + 1) else null) }, selected)

private val SampleRecents = listOf("severance", "past lives", "greta lee", "heat")

@Composable
private fun SampleSearch(
    state: (selectedType: Int, recents: List<String>) -> SearchState,
    modifier: Modifier = Modifier,
    initialText: String = "",
    withTrailing: Boolean = true,
    idleExtras: (LazyListScope.() -> Unit)? = null,
) {
    var text by remember { mutableStateOf(initialText) }
    var selected by remember { mutableIntStateOf(0) }
    var recents by remember { mutableStateOf(SampleRecents) }
    var opened by remember { mutableStateOf<String?>(null) }
    ScreenshotTheme(modifier = Modifier.fillMaxSize()) {
        SearchScreen(
            title = "Search",
            text = text,
            state = state(selected, recents),
            placeholder = "Movies, series and people",
            idleTitle = "Search the library",
            idleMessage = "Type a title or a name.",
            modifier = modifier,
            callbacks =
                SearchCallbacks(
                    onQueryChange = { text = it },
                    onClear = { text = "" },
                    onOpenResult = { opened = it },
                    onLoadMore = {},
                    onRetry = {},
                    onType = { selected = it },
                    onPickRecent = { text = it },
                    onClearRecents = { recents = emptyList() },
                    onBack = LocalDemoBack.current,
                ),
            resultTrailing =
                if (withTrailing) {
                    { result ->
                        if (result.key.removePrefix("r").toInt() % 2 == 0) StatusChip("Available", BingeSentiment.Positive)
                    }
                } else {
                    null
                },
            idleExtras = idleExtras,
        )
        opened?.let { BingeMessageDialog(title = "Opened", message = it, onDismiss = { opened = null }) }
    }
}

/** Nothing searched yet: the recent searches as chips with Clear behind a confirmation, and no type chips. */
@Composable
fun SearchScreenIdleSample() = SampleSearch({ _, recents -> SearchState.Idle(recents) })

/** Idle with no recents and nothing from the app: the plate that says what to type. */
@Composable
fun SearchScreenIdleEmptySample() = SampleSearch({ _, _ -> SearchState.Idle(emptyList()) })

/** Idle with the app's own content under the recents, here a line of text standing in for a rail. */
@Composable
fun SearchScreenIdleExtrasSample() =
    SampleSearch(
        state = { _, recents -> SearchState.Idle(recents, hasExtras = true) },
        idleExtras = {
            item(key = "extras") {
                Text(
                    "Trending rails go here",
                    modifier = Modifier.padding(dimensionResource(R.dimen.screen_content_inset)),
                )
            }
        },
    )

/** A search under way: a spinner under the field. */
@Composable
fun SearchScreenLoadingSample() = SampleSearch({ _, _ -> SearchState.Loading }, initialText = "past")

/** Results with the type chips and their counts: posters with a rating, genres and a status chip. */
@Composable
fun SearchScreenReadySample() =
    SampleSearch(
        { selected, _ ->
            SearchState.Ready(
                sampleResults(),
                loaded = SAMPLE_RESULTS,
                hasMore = true,
                types = types(selected, counts = true),
            )
        },
        initialText = "past",
    )

/** Results of a type with no counts, as a client that has none: People, as portraits with no status. */
@Composable
fun SearchScreenPeopleSample() =
    SampleSearch(
        { selected, _ ->
            SearchState.Ready(
                sampleResults(MediaResultKind.Avatar),
                loaded = SAMPLE_RESULTS,
                hasMore = false,
                types = types(selected, counts = false),
            )
        },
        initialText = "lee",
        withTrailing = false,
    )

/** The next page loading: the list at its end with a spinner under the last row. */
@Composable
fun SearchScreenAppendingSample() =
    SampleSearch(
        { selected, _ ->
            SearchState.Ready(
                sampleResults(count = FEW_RESULTS),
                FEW_RESULTS,
                hasMore = true,
                appending = true,
                types = types(selected, counts = true),
            )
        },
        initialText = "past",
    )

/** The next page failed: the footer is a Retry button. */
@Composable
fun SearchScreenAppendFailedSample() =
    SampleSearch(
        { selected, _ ->
            SearchState.Ready(
                sampleResults(count = FEW_RESULTS),
                FEW_RESULTS,
                hasMore = true,
                appendFailed = true,
                types = types(selected, counts = true),
            )
        },
        initialText = "past",
    )

/** The query matched nothing: a plate with a next step, and the chips stay, each at zero. */
@Composable
fun SearchScreenNoResultsSample() =
    SampleSearch(
        { selected, _ ->
            SearchState.NoResults(
                "Nothing for \"zzz\"",
                "Check the spelling or try fewer words.",
                types(selected, counts = true),
            )
        },
        initialText = "zzz",
    )

/** A chosen type with nothing in it, once every page has arrived. */
@Composable
fun SearchScreenTypeEmptySample() =
    SampleSearch(
        { selected, _ ->
            SearchState.Ready(
                emptyList(),
                loaded = SAMPLE_RESULTS,
                hasMore = false,
                types = types(selected, counts = true),
                emptyTitle = "No people",
                emptyMessage = "Nothing under People for \"past\".",
            )
        },
        initialText = "past",
    )

/** The search failed: the default failure plate with Try again under the field. */
@Composable
fun SearchScreenFailedSample() = SampleSearch({ _, _ -> SearchState.Failed(ErrorKind.Network) }, initialText = "past")

/** The screen live: typing searches after a pause, results page in near the end, and a type chip narrows them. */
@Composable
fun SearchScreenDemo() {
    var text by remember { mutableStateOf("") }
    var selected by remember { mutableIntStateOf(0) }
    var results by remember { mutableStateOf<List<MediaResult>?>(null) }
    var appending by remember { mutableStateOf(false) }
    var recents by remember { mutableStateOf(SampleRecents) }
    var opened by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(text) {
        results = null
        if (text.length >= 2) {
            delay(LOAD_MILLIS)
            results = sampleResults(count = SAMPLE_RESULTS)
        }
    }
    LaunchedEffect(appending) {
        if (appending) {
            delay(LOAD_MILLIS)
            results = results.orEmpty() + sampleResults(count = PAGE_RESULTS, from = results.orEmpty().size)
            appending = false
        }
    }
    val current = results
    val state =
        when {
            text.length < 2 -> SearchState.Idle(recents)
            current == null -> SearchState.Loading
            else ->
                SearchState.Ready(
                    current,
                    loaded = current.size,
                    hasMore = current.size < SAMPLE_RESULTS + PAGE_RESULTS * 2,
                    appending = appending,
                    types = types(selected, counts = false),
                )
        }
    ScreenshotTheme(modifier = Modifier.fillMaxSize()) {
        SearchScreen(
            title = "Search",
            text = text,
            state = state,
            placeholder = "Movies, series and people",
            idleTitle = "Search the library",
            idleMessage = "Type a title or a name.",
            callbacks =
                SearchCallbacks(
                    onQueryChange = { text = it },
                    onClear = { text = "" },
                    onOpenResult = { opened = it },
                    onLoadMore = { appending = true },
                    onRetry = {},
                    onType = { selected = it },
                    onPickRecent = { text = it },
                    onClearRecents = { recents = emptyList() },
                    onBack = LocalDemoBack.current,
                ),
        )
        opened?.let { BingeMessageDialog(title = "Opened", message = it, onDismiss = { opened = null }) }
    }
}
