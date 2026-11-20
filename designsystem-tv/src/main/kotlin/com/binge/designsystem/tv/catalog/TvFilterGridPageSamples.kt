@file:SelfDescribing

package com.binge.designsystem.tv.catalog

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.binge.designsystem.ErrorKind
import com.binge.designsystem.catalog.SelfDescribing
import com.binge.designsystem.tv.component.TvPosterCell
import com.binge.designsystem.tv.template.TvFilterGridCopy
import com.binge.designsystem.tv.template.TvFilterGridPage
import com.binge.designsystem.tv.template.TvFilterGridPageLoading
import com.binge.designsystem.tv.template.TvGridResults

private const val SAMPLE_RESULTS = 24
private const val UNWATCHED_EPISODES = 3
private const val HALF_WAY = 0.5f
private const val MOST_OF_IT = 0.8f
private const val MARKED_CELL_COUNT = 4

private val SampleGridCopy = TvFilterGridCopy(
    empty = "Nothing matches these filters",
    stale = "Couldn't update these results.",
    retry = "Try again",
)

private fun sampleResults(count: Int = SAMPLE_RESULTS, stale: Boolean = false): TvGridResults<String> =
    TvGridResults.Items(count = count, key = { it }, at = { "Title ${it + 1}" }, stale = stale)

private fun plain(index: Int, title: String) = TvPosterCell(imageUrl = null, title = title, onClick = {})

/** Marks on the first four cells, each a different one, so the frame shows every mark and a cell without any. */
private fun marked(index: Int, title: String) =
    when (index) {
        0 -> TvPosterCell(null, title, {}, watched = true)
        1 -> TvPosterCell(null, title, {}, unwatchedCount = UNWATCHED_EPISODES)
        2 -> TvPosterCell(null, title, {}, favourite = true, progress = HALF_WAY)
        MARKED_CELL_COUNT - 1 -> TvPosterCell(null, title, {}, watched = true, favourite = true, progress = MOST_OF_IT)
        else -> plain(index, title)
    }

/**
 * The catalog's page keeps its own filters, seeded with what the frame shows, so a pick works in the catalog app, and shows
 * [results] beside them. The live page, with its pane swap and focus hand-offs, is [TvFilterGridPageDemo].
 */
@Composable
private fun SamplePage(
    results: TvGridResults<String>,
    initial: FilterFixture = FilterFixture(genres = ThreeGenres),
    cell: (Int, String) -> TvPosterCell = ::plain,
    modifier: Modifier = Modifier,
) {
    var state by remember { mutableStateOf(initial) }
    TvFilterGridPage(
        categories = state.categories(),
        paneCopy = SamplePaneCopy,
        gridCopy = SampleGridCopy,
        filterCount = state.filterCount,
        results = results,
        cell = cell,
        listKey = state,
        onClearAll = { state = FilterFixture(content = state.content, sort = state.sort, savedProviders = state.savedProviders) },
        onToggle = { category, value -> state = state.toggled(category, value) {} },
        onClearCategory = { state = state.cleared(it) },
        onRetry = {},
        modifier = modifier,
        ranges = state.rangeSpecs(onState = { state = it }),
        pinnedRow = { option, selected -> servicesRowSpec(option, selected, state.savedProviders) },
        valueExtras = ::localeExtras,
    )
}

/** The filters beside a grid of results that follow them: genres chosen on the left, the posters on the right. */
@Composable
fun TvFilterGridPageResultsSample() {
    SamplePage(results = sampleResults())
}

/** The page with no filters set and the results in: "No filters" over the categories, the grid beside them. */
@Composable
fun TvFilterGridPageNoFiltersSample() {
    SamplePage(results = sampleResults(), initial = FilterFixture())
}

/** The grid while a query loads: the pane is live and the results are plates the posters will fill. */
@Composable
fun TvFilterGridPageLoadingSample() {
    SamplePage(results = TvGridResults.Loading)
}

/** A query that found nothing: the empty message in the grid's place, the filters still there to change. */
@Composable
fun TvFilterGridPageEmptySample() {
    SamplePage(results = TvGridResults.Empty)
}

/** A query that failed: the reason and Try again in the grid's place. */
@Composable
fun TvFilterGridPageFailedSample() {
    SamplePage(results = TvGridResults.Error(ErrorKind.Generic))
}

/** A failed query for a device that is offline reads "No connection", not the generic sentence. */
@Composable
fun TvFilterGridPageOfflineSample() {
    SamplePage(results = TvGridResults.Error(ErrorKind.Network))
}

/** The new query failed but the earlier results stay, with a line above them and Try again: [TvGridResults.Items.stale]. */
@Composable
fun TvFilterGridPageStaleSample() {
    SamplePage(results = sampleResults(stale = true))
}

/** Marks on the cells: watched, unwatched episodes, favourite and progress, beside a cell with none. */
@Composable
fun TvFilterGridPageMarksSample() {
    SamplePage(results = sampleResults(), cell = ::marked)
}

/** The page before there is a state to draw: the pane and the grid as plates, so the page does not jump when it resolves. */
@Composable
fun TvFilterGridPageNoStateYetSample() {
    TvFilterGridPageLoading()
}

/** The live page: filters, results that follow them, and a failure to retry, as in a running screen. */
@Composable
fun TvFilterGridPageDemo() {
    var failed by remember { mutableStateOf(false) }
    SamplePage(
        results = if (failed) TvGridResults.Error(ErrorKind.Generic) else sampleResults(),
        cell = { index, title -> TvPosterCell(null, title, onClick = { failed = index % 2 == 0 }) },
    )
}
