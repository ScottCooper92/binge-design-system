@file:OnePerScreen

package com.binge.designsystem.catalog

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.NewReleases
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.Tv
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import com.binge.designsystem.R
import com.binge.designsystem.preview.ScreenshotTheme
import com.binge.designsystem.template.FilterControlUi
import com.binge.designsystem.template.FilterOptionUi
import com.binge.designsystem.template.FilterRowUi
import com.binge.designsystem.template.FilterSectionUi
import com.binge.designsystem.template.FilterSheetContent

/**
 * The filter sheet's body: content type and sort as chip groups, genres as include/exclude chips, then a row per filter that opens
 * on its own. Drawn without a host, since the sheet is a modal window. Every control works: a tap picks a chip, a long press
 * excludes a genre, and Clear all empties the lot.
 */
@Composable
fun FilterSheetSample() {
    FilterSheetFrame(copy = EnglishFilterCopy)
}

/** The same sheet in Spanish: the longer labels wrap where the English ones do not, and the footer and header keep their room. */
@Composable
fun FilterSheetSpanishSample() {
    FilterSheetFrame(copy = SpanishFilterCopy)
}

private class FilterCopy(
    val title: String,
    val applied: (Int) -> String,
    val clearAll: String,
    val showResults: String,
    val contentTitle: String,
    val movies: String,
    val shows: String,
    val sortTitle: String,
    val popular: String,
    val rating: String,
    val newest: String,
    val genresTitle: String,
    val genres: List<String>,
    val rowsTitle: String,
    val rowGenres: String,
    val rowYear: String,
    val rowRating: String,
    val rowLanguage: String,
    val rowCountry: String,
    val any: String,
    val ratingSummary: String,
)

private val EnglishFilterCopy =
    FilterCopy(
        title = "Filters",
        applied = { "$it applied" },
        clearAll = "Clear all",
        showResults = "Show results",
        contentTitle = "Content",
        movies = "Movies",
        shows = "TV shows",
        sortTitle = "Sort by",
        popular = "Popularity",
        rating = "Rating",
        newest = "Newest",
        genresTitle = "Genres",
        genres = listOf("Action", "Comedy", "Drama", "Horror", "Romance"),
        rowsTitle = "Filter by",
        rowGenres = "Genres",
        rowYear = "Release date",
        rowRating = "Rating",
        rowLanguage = "Language",
        rowCountry = "Country",
        any = "Any",
        ratingSummary = "7.0 to 10",
    )

private val SpanishFilterCopy =
    FilterCopy(
        title = "Filtros",
        applied = { "$it aplicados" },
        clearAll = "Borrar todo",
        showResults = "Mostrar resultados",
        contentTitle = "Contenido",
        movies = "Películas",
        shows = "Series de televisión",
        sortTitle = "Ordenar por",
        popular = "Popularidad",
        rating = "Valoración",
        newest = "Más recientes",
        genresTitle = "Géneros",
        genres = listOf("Acción", "Comedia", "Drama", "Terror", "Romance"),
        rowsTitle = "Filtrar por",
        rowGenres = "Géneros",
        rowYear = "Fecha de estreno",
        rowRating = "Valoración",
        rowLanguage = "Idioma original",
        rowCountry = "País de origen",
        any = "Cualquiera",
        ratingSummary = "de 7,0 a 10",
    )

private const val KIND_ID = "kind"
private const val SORT_ID = "sort"
private const val GENRES_ID = "genres"
private const val MOVIES_ID = "movies"
private const val SHOWS_ID = "shows"
private const val POPULAR_ID = "popular"
private const val RATING_ID = "rating"
private const val NEWEST_ID = "newest"
private const val DEFAULT_KIND_ID = MOVIES_ID
private const val DEFAULT_SORT_ID = NEWEST_ID

@Composable
private fun FilterSheetFrame(copy: FilterCopy) {
    var kind by remember { mutableStateOf(MOVIES_ID) }
    var sort by remember { mutableStateOf(POPULAR_ID) }
    var included by remember { mutableStateOf(setOf(copy.genres[0])) }
    var excluded by remember { mutableStateOf(setOf(copy.genres[3])) }
    val applied = included.size + excluded.size + (if (kind != DEFAULT_KIND_ID) 1 else 0) + (if (sort != DEFAULT_SORT_ID) 1 else 0)
    ScreenshotTheme {
        Column(Modifier.height(dimensionResource(R.dimen.catalog_filter_sheet_height))) {
            FilterSheetContent(
                title = copy.title,
                clearAllLabel = copy.clearAll,
                showResultsLabel = copy.showResults,
                appliedLabel = if (applied > 0) copy.applied(applied) else null,
                sections =
                    listOf(
                        FilterSectionUi(
                            id = KIND_ID,
                            title = copy.contentTitle,
                            control =
                                FilterControlUi.SingleChoice(
                                    options =
                                        listOf(
                                            FilterOptionUi(MOVIES_ID, copy.movies, Icons.Filled.Movie),
                                            FilterOptionUi(SHOWS_ID, copy.shows, Icons.Filled.Tv),
                                        ),
                                    selectedId = kind,
                                ),
                        ),
                        FilterSectionUi(
                            id = SORT_ID,
                            title = copy.sortTitle,
                            control =
                                FilterControlUi.SingleChoice(
                                    options =
                                        listOf(
                                            FilterOptionUi(POPULAR_ID, copy.popular, Icons.AutoMirrored.Filled.TrendingUp),
                                            FilterOptionUi(RATING_ID, copy.rating, Icons.Filled.Star),
                                            FilterOptionUi(NEWEST_ID, copy.newest, Icons.Filled.NewReleases),
                                        ),
                                    selectedId = sort,
                                ),
                        ),
                        FilterSectionUi(
                            id = GENRES_ID,
                            title = copy.genresTitle,
                            control =
                                FilterControlUi.MultiChoice(
                                    options = copy.genres.map { FilterOptionUi(it, it) },
                                    includedIds = included,
                                    excludedIds = excluded,
                                ),
                        ),
                    ),
                rowsTitle = copy.rowsTitle,
                rows =
                    listOf(
                        FilterRowUi("genres", copy.rowGenres, copy.any, Icons.Filled.Category),
                        FilterRowUi("year", copy.rowYear, copy.any, Icons.Filled.CalendarMonth),
                        FilterRowUi("rating", copy.rowRating, copy.ratingSummary, Icons.Filled.Star),
                        FilterRowUi("language", copy.rowLanguage, copy.any, Icons.Filled.Translate),
                        FilterRowUi("country", copy.rowCountry, copy.any, Icons.Filled.Public),
                    ),
                onSingleSelect = { section, option -> if (section == KIND_ID) kind = option else sort = option },
                onOptionClick = { _, id ->
                    when (id) {
                        in included -> included -= id
                        in excluded -> excluded -= id
                        else -> included += id
                    }
                },
                onOptionLongClick = { _, id ->
                    included -= id
                    excluded = if (id in excluded) excluded - id else excluded + id
                },
                onApply = {},
                onClearAll = {
                    kind = DEFAULT_KIND_ID
                    sort = DEFAULT_SORT_ID
                    included = emptySet()
                    excluded = emptySet()
                },
            )
        }
    }
}
