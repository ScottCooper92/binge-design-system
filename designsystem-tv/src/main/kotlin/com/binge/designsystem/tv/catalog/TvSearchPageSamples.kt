@file:SelfDescribing

package com.binge.designsystem.tv.catalog

import androidx.compose.runtime.Composable
import com.binge.designsystem.ErrorKind
import com.binge.designsystem.catalog.SelfDescribing
import com.binge.designsystem.tv.component.TvPosterShape
import com.binge.designsystem.tv.template.TvSearchCell
import com.binge.designsystem.tv.template.TvSearchPage
import com.binge.designsystem.tv.template.TvSearchSeededFocus
import com.binge.designsystem.tv.template.TvSearchState
import com.binge.designsystem.tv.template.TvSearchSuggestion
import com.binge.designsystem.tv.template.TvSearchSuggestionSource

private const val RESULT_COUNT = 12
private const val RECOMMENDATION_COUNT = 10
private const val NOLAN = "nolan"
private const val RECOMMENDATION_ROW = "films"

private val Suggestions = listOf("Inception", "Dune", "The Bear").map { TvSearchSuggestion(it, TvSearchSuggestionSource.Recent) } +
    listOf("Project Hail Mary", "Obsession", "Michael").map { TvSearchSuggestion(it, TvSearchSuggestionSource.Suggested) }

private val Recommendations = List(RECOMMENDATION_COUNT) { index ->
    TvSearchCell(
        key = "recommendation_${index + 1}",
        imageUrl = null,
        title = "Recommendation ${index + 1}",
        onClick = {},
        rowId = RECOMMENDATION_ROW,
    )
}

private val Results = List(RESULT_COUNT) { index ->
    TvSearchCell(key = "result_${index + 1}", imageUrl = null, title = "Result ${index + 1}", onClick = {})
}

private val CaptionedResults = List(RESULT_COUNT) { index ->
    TvSearchCell(
        key = "captioned_${index + 1}",
        imageUrl = null,
        title = "Result ${index + 1}",
        onClick = {},
        subtitle = if (index % 3 == 2) "Studio ${index + 1}" else "Film · 20${10 + index}",
        shape = if (index % 3 == 2) TvPosterShape.Square else TvPosterShape.Poster,
    )
}

@Composable
private fun SamplePage(
    state: TvSearchState,
    cells: List<TvSearchCell>,
    query: String = "",
    seededFocus: TvSearchSeededFocus = TvSearchSeededFocus(),
) {
    TvSearchPage(
        query = query,
        queryRevision = 0,
        placeholder = "Search movies and shows",
        onQueryChange = {},
        onSubmit = {},
        suggestions = Suggestions,
        onSuggestionSelected = {},
        state = state,
        cells = cells,
        seededFocus = seededFocus,
    )
}

/** The page at rest: the field, recent then suggested searches down the left, and the recommendations in the grid. */
@Composable
fun TvSearchPageSample() {
    SamplePage(state = TvSearchState.Idle, cells = Recommendations)
}

/** The field holding focus. */
@Composable
fun TvSearchPageFieldFocusedSample() {
    SamplePage(state = TvSearchState.Idle, cells = Recommendations, seededFocus = TvSearchSeededFocus(field = true))
}

/** A suggestion focused: the clock of a recent search against the magnifier of a suggested one below it. */
@Composable
fun TvSearchPageSuggestionFocusedSample() {
    SamplePage(
        state = TvSearchState.Idle,
        cells = Recommendations,
        seededFocus = TvSearchSeededFocus(suggestionKey = Suggestions.first().key),
    )
}

/** A recommendation focused in the first row of the grid. */
@Composable
fun TvSearchPageCellFocusedSample() {
    SamplePage(state = TvSearchState.Idle, cells = Recommendations, seededFocus = TvSearchSeededFocus(cellKey = "recommendation_1"))
}

/** Results for a query, the grid holding them where the recommendations were. */
@Composable
fun TvSearchPageResultsSample() {
    SamplePage(state = TvSearchState.Results(NOLAN), cells = Results, query = NOLAN)
}

/** Results with a caption under each poster, a studio's square art among the posters: the footprint follows the cell's shape. */
@Composable
fun TvSearchPageCaptionedResultsSample() {
    SamplePage(state = TvSearchState.Results(NOLAN), cells = CaptionedResults, query = NOLAN)
}

/** A result in the second row focused. The scroll that pins its row is a runtime effect a static frame does not run. */
@Composable
fun TvSearchPageResultFocusedSample() {
    SamplePage(
        state = TvSearchState.Results(NOLAN),
        cells = Results,
        query = NOLAN,
        seededFocus = TvSearchSeededFocus(cellKey = "result_6"),
    )
}

/** The first load: the list under the field and the grid are both plates. */
@Composable
fun TvSearchPageLoadingIdleSample() {
    TvSearchPage(
        query = "",
        queryRevision = 0,
        placeholder = "Search movies and shows",
        onQueryChange = {},
        onSubmit = {},
        suggestions = emptyList(),
        onSuggestionSelected = {},
        state = TvSearchState.LoadingIdle,
        cells = emptyList(),
    )
}

/** A query is out: the list stays live under the field and only the grid is plates. */
@Composable
fun TvSearchPageLoadingResultsSample() {
    SamplePage(state = TvSearchState.LoadingResults, cells = emptyList(), query = NOLAN)
}

/** Nothing matched: the struck-through search icon, what was searched and what to try, over the recommendations. */
@Composable
fun TvSearchPageEmptySample() {
    SamplePage(
        state = TvSearchState.Empty(
            query = "zxqwerty",
            headline = "No results for \"zxqwerty\"",
            body = "Try another spelling or a shorter search.",
        ),
        cells = Recommendations,
        query = "zxqwerty",
    )
}

/** A failed load with Try again focused: the single OK that recovers from across the room. */
@Composable
fun TvSearchPageFailedSample() {
    SamplePage(
        state = TvSearchState.Failed(kind = ErrorKind.Network, onRetry = {}),
        cells = emptyList(),
        query = NOLAN,
        seededFocus = TvSearchSeededFocus(retry = true),
    )
}
