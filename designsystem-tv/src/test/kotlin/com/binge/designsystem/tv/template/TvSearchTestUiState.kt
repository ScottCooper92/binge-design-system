package com.binge.designsystem.tv.template

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * What a search test gives the page: the query, the lists to show and the [state] the grid is in. It stands in for the
 * app's own state type, so a test reads as an app driving the page would.
 */
internal data class SearchUiState(
    val query: String = "",
    val isTv: Boolean = true,
    val recentSearches: List<String> = emptyList(),
    val trendingTitles: List<String> = emptyList(),
    val state: TvSearchState = TvSearchState.Idle,
    val cells: List<TvSearchCell> = emptyList(),
)

private const val SUGGESTION_LIMIT = 30
private const val RECOMMENDATION_COUNT = 10

internal fun tvRecentSearches(): List<String> = listOf("Inception", "Dune", "The Bear")

internal fun tvTrendingTitles(): List<String> =
    listOf("The Punisher: One Last Kill", "Project Hail Mary", "Obsession", "Mortal Kombat II", "Michael")

internal fun tvRecommendationCells(onClick: (String) -> Unit = {}): List<TvSearchCell> =
    List(RECOMMENDATION_COUNT) { index ->
        val kind = if (index % 2 == 0) "Film" else "Show"
        val title = "Trending $kind ${index / 2 + 1}"
        TvSearchCell(key = "recommendation_${index + 1}", imageUrl = null, title = title, onClick = { onClick(title) }, rowId = kind)
    }

internal fun tvResultTitles(): List<String> =
    listOf(
        "Inception",
        "Interstellar",
        "The Dark Knight",
        "Oppenheimer",
        "Westworld",
        "Tenet",
        "Dunkirk",
        "Memento",
        "Person of Interest",
        "The Prestige",
        "Batman Begins",
        "Insomnia",
    )

internal fun resultCells(titles: List<String>): List<TvSearchCell> =
    titles.mapIndexed { index, title -> TvSearchCell(key = "result_${index + 1}", imageUrl = null, title = title, onClick = {}) }

private fun withLists(state: SearchUiState): SearchUiState =
    state.copy(recentSearches = listOf("Christopher Nolan", "Nolan documentary") + tvRecentSearches(), trendingTitles = tvTrendingTitles())

internal fun tvSearchIdleUiState(): SearchUiState =
    SearchUiState(recentSearches = tvRecentSearches(), trendingTitles = tvTrendingTitles(), cells = tvRecommendationCells())

internal fun tvSearchLoadingIdleUiState(): SearchUiState = SearchUiState(state = TvSearchState.LoadingIdle)

internal fun tvSearchLoadingResultsUiState(): SearchUiState =
    withLists(SearchUiState(query = "nolan", state = TvSearchState.LoadingResults))

internal fun tvResultsUiState(): SearchUiState =
    withLists(SearchUiState(query = "nolan", state = TvSearchState.Results("nolan"), cells = resultCells(tvResultTitles())))

internal fun tvSearchEmptyUiState(): SearchUiState =
    SearchUiState(
        query = "zxqwerty",
        state = TvSearchState.Empty("zxqwerty", headline = "No results for \"zxqwerty\"", body = "Try another spelling."),
        cells = tvRecommendationCells(),
    )

/** The page as an app would host it, the suggestions merged from the two lists and filtered by the query. */
@Composable
internal fun TvSearchContent(
    uiState: SearchUiState,
    onQueryChange: (String) -> Unit = {},
    onSubmit: () -> Unit = {},
    onSuggestionSelected: (String) -> Unit = {},
    modifier: Modifier = Modifier.fillMaxSize(),
    seededFocus: TvSearchSeededFocus = TvSearchSeededFocus(),
    onCellFocused: (key: String, rowId: String?) -> Unit = { _, _ -> },
    onResultsVisitEnded: (deepestIndex: Int?) -> Unit = {},
) {
    val needle = uiState.query.trim().lowercase()
    val suggestions = (
        uiState.recentSearches.map { TvSearchSuggestion(it, TvSearchSuggestionSource.Recent) } +
            uiState.trendingTitles.map { TvSearchSuggestion(it, TvSearchSuggestionSource.Suggested) }
    ).filter { needle.isEmpty() || it.text.lowercase().contains(needle) }
        .distinctBy { it.text.lowercase() }
        .take(SUGGESTION_LIMIT)
    TvSearchPage(
        query = uiState.query,
        queryRevision = 0,
        placeholder = "Search",
        onQueryChange = onQueryChange,
        onSubmit = onSubmit,
        suggestions = suggestions,
        onSuggestionSelected = onSuggestionSelected,
        state = uiState.state,
        cells = uiState.cells,
        modifier = modifier,
        seededFocus = seededFocus,
        onCellFocused = onCellFocused,
        onResultsVisitEnded = onResultsVisitEnded,
    )
}
