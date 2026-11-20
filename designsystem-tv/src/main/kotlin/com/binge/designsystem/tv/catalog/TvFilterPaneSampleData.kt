package com.binge.designsystem.tv.catalog

import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Subscriptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.res.dimensionResource
import com.binge.designsystem.tv.component.TvFilterCategory
import com.binge.designsystem.tv.component.TvFilterValue
import com.binge.designsystem.tv.component.TvFilterValueState
import com.binge.designsystem.tv.component.chosenIf
import com.binge.designsystem.tv.template.TvFilterPaneCopy
import com.binge.designsystem.tv.template.TvFilterPaneHost
import com.binge.designsystem.tv.template.TvFilterPinnedRowSpec
import com.binge.designsystem.tv.template.TvFilterRangeSeed
import com.binge.designsystem.tv.template.TvFilterRangeSpec
import com.binge.designsystem.tv.template.TvFilterSeed
import com.binge.designsystem.tv.template.TvFilterValueExtras
import com.binge.designsystem.tv.template.TvRangeBound
import com.binge.designsystem.tv.template.TvRangeStep
import com.binge.designsystem.tv.R as TvR

internal const val CATEGORY_CONTENT = "content"
internal const val CATEGORY_SORT = "sort"
internal const val CATEGORY_GENRES = "genres"
internal const val CATEGORY_PROVIDERS = "providers"
internal const val CATEGORY_RATING = "rating"
internal const val CATEGORY_CERT = "cert"
internal const val CATEGORY_COMPANIES = "companies"
internal const val CATEGORY_LANGUAGE = "language"
internal const val CATEGORY_COUNTRY = "country"
internal const val KEY_MY_SERVICES = "my_services"
internal const val KEY_MY_SERVICES_EMPTY = "my_services_empty"

internal const val LABEL_CONTENT = "Content"
internal const val LABEL_SORT = "Sort by"
internal const val LABEL_GENRES = "Genres"
internal const val LABEL_PROVIDERS = "Where to watch"
internal const val LABEL_RATING = "Rating"
internal const val LABEL_CERT = "Content rating"
internal const val LABEL_COMPANIES = "Production company"
internal const val LABEL_LANGUAGE = "Language"
internal const val LABEL_COUNTRY = "Country"
internal const val LABEL_MATCH_ALL = "Match all selected"
internal const val LABEL_MINIMUM = "Minimum"
internal const val LABEL_MAXIMUM = "Maximum"

private val LanguageKeys = listOf("en", "ja", "fr")
private val CountryKeys = listOf("US", "JP", "FR")

private fun languageName(key: String) = mapOf("en" to "English", "ja" to "Japanese", "fr" to "French").getValue(key)

private fun countryName(key: String) = mapOf("US" to "United States", "JP" to "Japan", "FR" to "France").getValue(key)

/** What a language or country row adds: a flag for a country, the language's own name for a language, and the code for both. */
internal fun localeExtras(categoryKey: String, value: TvFilterValue): TvFilterValueExtras? =
    when (categoryKey) {
        CATEGORY_COUNTRY -> TvFilterValueExtras(leadingText = flagEmoji(value.key), code = value.key)
        CATEGORY_LANGUAGE -> TvFilterValueExtras(detail = endonym(value.key), code = value.key)
        else -> null
    }

private fun endonym(key: String) = mapOf("en" to "English", "ja" to "日本語", "fr" to "Français").getValue(key)

private fun flagEmoji(country: String): String = country.map { String(Character.toChars(0x1F1E6 + (it - 'A'))) }.joinToString("")

private val GenreLabels = listOf("Action", "Comedy", "Drama", "Horror")
private val ProviderLabels = listOf("Netflix", "Disney+", "Prime Video")
private val SortOptions = listOf("popularity" to "Popularity", "release" to "Release date", "rating" to "Rating")
private val CertSteps = listOf("G", "PG", "PG-13", "R", "NC-17").map { TvRangeStep(it, it) }
internal val RatingSteps: List<TvRangeStep> = (1..9).map { TvRangeStep("$it.0", "$it.0") }

private fun ratingRank(key: String): Float = key.toFloatOrNull() ?: Float.NEGATIVE_INFINITY

/** Three genres set, two chosen and one ruled out, as the moved pane tests start from. */
internal val ThreeGenres: Map<String, TvFilterValueState> =
    mapOf("action" to TvFilterValueState.Included, "comedy" to TvFilterValueState.Included, "horror" to TvFilterValueState.Excluded)

/**
 * The filter state the catalog's pane and the moved pane tests apply their picks to: a plain stand-in for an app's own, with the shape
 * the tests assert (which values are chosen, the rating and certification bounds, and the opens of the services screen).
 */

internal data class FilterFixture(
    val content: String = "movie",
    val sort: String = "release",
    val genres: Map<String, TvFilterValueState> = emptyMap(),
    val matchAll: Boolean = false,
    val providers: Set<String> = emptySet(),
    val savedProviders: Int = 0,
    val ratingFrom: Float? = null,
    val ratingTo: Float? = null,
    val certGte: String? = null,
    val certLte: String? = null,
    val companies: Set<String> = emptySet(),
    val localeRows: Boolean = false,
    val languages: Set<String> = emptySet(),
    val countries: Set<String> = emptySet(),
) {
    /** The categories with something set, not counting Content and Sort by, which always hold a choice. */
    val filterCount: Int
        get() = listOf(
            genres.isNotEmpty(),
            providers.isNotEmpty(),
            ratingFrom != null || ratingTo != null,
            certGte != null || certLte != null,
            companies.isNotEmpty(),
        ).count { it }

    fun categories(): List<TvFilterCategory> =
        buildList {
            add(single(CATEGORY_CONTENT, LABEL_CONTENT, listOf("movie" to "Movies", "tv" to "TV shows"), content))
            add(single(CATEGORY_SORT, LABEL_SORT, SortOptions, sort))
            add(
                TvFilterCategory(
                    key = CATEGORY_GENRES,
                    label = LABEL_GENRES,
                    values = GenreLabels.map { TvFilterValue(it.lowercase(), it, genres[it.lowercase()] ?: TvFilterValueState.Neutral) },
                    multiSelect = true,
                    pinnedOptions = listOf(TvFilterValue("match_all", LABEL_MATCH_ALL, chosenIf(matchAll))),
                ),
            )
            add(
                TvFilterCategory(
                    key = CATEGORY_PROVIDERS,
                    label = LABEL_PROVIDERS,
                    values = ProviderLabels.map { TvFilterValue(it.lowercase(), it, chosenIf(it.lowercase() in providers)) },
                    multiSelect = true,
                    pinnedOptions = listOf(
                        TvFilterValue(if (savedProviders == 0) KEY_MY_SERVICES_EMPTY else KEY_MY_SERVICES, "My services"),
                    ),
                ),
            )
            add(TvFilterCategory(CATEGORY_RATING, LABEL_RATING, values = emptyList()))
            add(TvFilterCategory(CATEGORY_CERT, LABEL_CERT, values = emptyList()))
            if (localeRows) {
                add(
                    TvFilterCategory(
                        CATEGORY_LANGUAGE,
                        LABEL_LANGUAGE,
                        LanguageKeys.map { TvFilterValue(it, languageName(it), chosenIf(it in languages)) },
                        multiSelect = true,
                    ),
                )
                add(
                    TvFilterCategory(
                        CATEGORY_COUNTRY,
                        LABEL_COUNTRY,
                        CountryKeys.map { TvFilterValue(it, countryName(it), chosenIf(it in countries)) },
                        multiSelect = true,
                    ),
                )
            }
            if (companies.isNotEmpty()) {
                add(
                    TvFilterCategory(
                        key = CATEGORY_COMPANIES,
                        label = LABEL_COMPANIES,
                        values = companies.map { TvFilterValue(it, "Studio Ghibli", TvFilterValueState.Included) },
                        multiSelect = true,
                    ),
                )
            }
        }

    fun toggled(
        categoryKey: String,
        valueKey: String,
        onOpenServices: () -> Unit,
    ): FilterFixture =
        when (categoryKey) {
            CATEGORY_CONTENT -> copy(content = valueKey)
            CATEGORY_SORT -> copy(sort = valueKey)
            CATEGORY_GENRES ->
                if (valueKey == "match_all") {
                    copy(matchAll = !matchAll)
                } else {
                    val next = when (genres[valueKey]) {
                        null -> TvFilterValueState.Included
                        TvFilterValueState.Included -> TvFilterValueState.Excluded
                        else -> null
                    }
                    copy(genres = if (next == null) genres - valueKey else genres + (valueKey to next))
                }
            CATEGORY_PROVIDERS ->
                when (valueKey) {
                    KEY_MY_SERVICES_EMPTY -> this.also { onOpenServices() }
                    KEY_MY_SERVICES -> copy(providers = ProviderLabels.take(savedProviders).map { it.lowercase() }.toSet())
                    else -> copy(providers = if (valueKey in providers) providers - valueKey else providers + valueKey)
                }
            CATEGORY_COMPANIES -> copy(companies = companies - valueKey)
            CATEGORY_LANGUAGE -> copy(languages = if (valueKey in languages) languages - valueKey else languages + valueKey)
            CATEGORY_COUNTRY -> copy(countries = if (valueKey in countries) countries - valueKey else countries + valueKey)
            else -> this
        }

    fun cleared(categoryKey: String): FilterFixture =
        when (categoryKey) {
            CATEGORY_GENRES -> copy(genres = emptyMap(), matchAll = matchAll)
            CATEGORY_PROVIDERS -> copy(providers = emptySet())
            CATEGORY_COMPANIES -> copy(companies = emptySet())
            CATEGORY_LANGUAGE -> copy(languages = emptySet())
            CATEGORY_COUNTRY -> copy(countries = emptySet())
            CATEGORY_RATING -> copy(ratingFrom = null, ratingTo = null)
            CATEGORY_CERT -> copy(certGte = null, certLte = null)
            else -> this
        }

    /** The rating and content-rating scales, applying each pick to this state. [ratingBound] is the bound the rating scale sets on entry. */
    fun rangeSpecs(onState: (FilterFixture) -> Unit, ratingBound: TvRangeBound = TvRangeBound.Min): Map<String, TvFilterRangeSpec> =
        mapOf(
            CATEGORY_RATING to TvFilterRangeSpec(
                steps = RatingSteps,
                from = ratingFrom?.toString(),
                to = ratingTo?.toString(),
                onChange = { (from, to) -> onState(copy(ratingFrom = from?.toFloatOrNull(), ratingTo = to?.toFloatOrNull())) },
                initialBound = ratingBound,
                offScaleLabel = { it },
                rank = ::ratingRank,
            ),
            CATEGORY_CERT to TvFilterRangeSpec(
                steps = CertSteps,
                from = certGte,
                to = certLte,
                onChange = { (from, to) -> onState(copy(certGte = from, certLte = to)) },
            ),
        )

    private fun single(
        key: String,
        label: String,
        options: List<Pair<String, String>>,
        chosen: String,
    ) = TvFilterCategory(key, label, options.map { (k, l) -> TvFilterValue(k, l, chosenIf(k == chosen)) })
}

internal val SamplePaneCopy = TvFilterPaneCopy(
    noFilters = "No filters",
    filterCount = { if (it == 1) "1 filter" else "$it filters" },
    noneSelected = "None selected",
    selectedCount = { "$it selected" },
    clear = "Clear",
    any = "Any",
    excludedFormat = "not %1\$s",
    minimum = LABEL_MINIMUM,
    maximum = LABEL_MAXIMUM,
)

/** The services row the host app words: add yours, use the saved ones, or the count with Clear. */
internal fun servicesRowSpec(
    option: TvFilterValue,
    selected: Int,
    saved: Int,
): TvFilterPinnedRowSpec? =
    when (option.key) {
        KEY_MY_SERVICES, KEY_MY_SERVICES_EMPTY ->
            when {
                selected > 0 -> TvFilterPinnedRowSpec("$selected selected", clears = true)
                option.key == KEY_MY_SERVICES_EMPTY -> TvFilterPinnedRowSpec("Add your services", Icons.Filled.Add)
                else -> TvFilterPinnedRowSpec("Use my $saved services", Icons.Filled.Subscriptions)
            }
        else -> null
    }

/** The [TvFilterPaneHost] over [state], applying each pick as the host app's view model would. */
@Composable
internal fun SampleFilterPane(
    state: FilterFixture,
    onState: (FilterFixture) -> Unit,
    entry: FocusRequester,
    onOpenServices: () -> Unit,
    modifier: Modifier = Modifier,
    seed: TvFilterSeed = TvFilterSeed(),
    rangeSeed: TvFilterRangeSeed = TvFilterRangeSeed(),
) {
    val ranges = remember(state) { state.rangeSpecs(onState) }
    TvFilterPaneHost(
        categories = state.categories(),
        copy = SamplePaneCopy,
        filterCount = state.filterCount,
        entry = entry,
        onClearAll = { onState(FilterFixture(content = state.content, sort = state.sort, savedProviders = state.savedProviders)) },
        onToggle = { category, value -> onState(state.toggled(category, value, onOpenServices)) },
        onClearCategory = { onState(state.cleared(it)) },
        modifier = modifier.width(dimensionResource(TvR.dimen.tv_filter_pane_width)),
        ranges = ranges,
        pinnedRow = { option, selected -> servicesRowSpec(option, selected, state.savedProviders) },
        valueExtras = ::localeExtras,
        seed = seed,
        rangeSeed = rangeSeed,
    )
}
