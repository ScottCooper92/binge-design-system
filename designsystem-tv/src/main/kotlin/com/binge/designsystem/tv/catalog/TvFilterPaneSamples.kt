@file:SelfDescribing

package com.binge.designsystem.tv.catalog

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.res.dimensionResource
import com.binge.designsystem.catalog.SelfDescribing
import com.binge.designsystem.tv.nav.tvContentGutterStart
import com.binge.designsystem.tv.template.TvFilterPane
import com.binge.designsystem.tv.template.TvFilterPaneSkeleton
import com.binge.designsystem.tv.template.TvFilterPosition
import com.binge.designsystem.tv.template.TvFilterRangeSeed
import com.binge.designsystem.tv.template.TvFilterSeed
import com.binge.designsystem.tv.template.TvRangeBound
import com.binge.designsystem.tv.R as TvR

/** The pane where a filter page puts it: the content gutter in, the overscan down, and the pane's own width. */
@Composable
private fun PaneFrame(content: @Composable () -> Unit) {
    Box(Modifier.fillMaxSize().padding(start = tvContentGutterStart(), top = dimensionResource(TvR.dimen.tv_overscan_vertical))) {
        Box(Modifier.width(dimensionResource(TvR.dimen.tv_filter_pane_width)).fillMaxHeight()) { content() }
    }
}

/**
 * The catalog's pane keeps its own filters, the open category and where focus was, seeded with what the frame shows, so a pick or a
 * drill works in the catalog app. Focus is a parameter ([seed], [rangeSeed]) so a static frame can show it; the live pane, with its
 * swap, is [TvFilterPaneDemo].
 */
@Composable
private fun SamplePane(
    initial: FilterFixture = FilterFixture(),
    openKey: String? = null,
    seed: TvFilterSeed = TvFilterSeed(),
    rangeSeed: TvFilterRangeSeed = TvFilterRangeSeed(),
    ratingBound: TvRangeBound = TvRangeBound.Min,
) {
    var state by remember { mutableStateOf(initial) }
    var open by remember { mutableStateOf(openKey) }
    var position by remember { mutableStateOf(TvFilterPosition()) }
    val categories = state.categories()
    val entry = remember { FocusRequester() }
    PaneFrame {
        TvFilterPane(
            categories = categories,
            open = categories.firstOrNull { it.key == open },
            position = position,
            entry = entry,
            copy = SamplePaneCopy,
            filterCount = state.filterCount,
            onClearAll = { state = FilterFixture(content = state.content, sort = state.sort, savedProviders = state.savedProviders) },
            onDrill = { open = it },
            onPosition = { position = it },
            onToggle = { category, value -> state = state.toggled(category, value) {} },
            onClearCategory = { state = state.cleared(it) },
            ranges = state.rangeSpecs({ state = it }, ratingBound),
            pinnedRow = { option, selected -> servicesRowSpec(option, selected, state.savedProviders) },
            valueExtras = ::localeExtras,
            seed = seed,
            rangeSeed = rangeSeed,
        )
    }
}

/** The category list with nothing set: each category over its current choice, and "No filters" in the header with nothing to clear. */
@Composable
fun TvFilterPaneNothingSetSample() {
    SamplePane()
}

/** The category list with a filter set: the header counts it and ends in Clear, and the category shows what is chosen and ruled out. */
@Composable
fun TvFilterPaneFiltersSetSample() {
    SamplePane(FilterFixture(genres = ThreeGenres))
}

/** The header focused, white across the row, with Clear at its end. */
@Composable
fun TvFilterPaneHeaderFocusedSample() {
    SamplePane(FilterFixture(genres = ThreeGenres), seed = TvFilterSeed(header = true))
}

/** A category row focused. */
@Composable
fun TvFilterPaneCategoryFocusedSample() {
    SamplePane(FilterFixture(genres = ThreeGenres), seed = TvFilterSeed(key = CATEGORY_GENRES))
}

/** A multi-select's values with none chosen: the header leading back, "None selected", the Match all switch and the values below. */
@Composable
fun TvFilterPaneGenresSample() {
    SamplePane(openKey = CATEGORY_GENRES)
}

/** Values chosen and ruled out: a tick on a chosen value, a cross and the error colour on a ruled-out one, and "3 selected" with Clear. */
@Composable
fun TvFilterPaneGenresChosenSample() {
    SamplePane(FilterFixture(genres = ThreeGenres), openKey = CATEGORY_GENRES)
}

/** The values' header focused. */
@Composable
fun TvFilterPaneGenresHeaderFocusedSample() {
    SamplePane(FilterFixture(genres = ThreeGenres), openKey = CATEGORY_GENRES, seed = TvFilterSeed(header = true))
}

/** The count row focused, which Clear empties. */
@Composable
fun TvFilterPaneGenresCountFocusedSample() {
    SamplePane(FilterFixture(genres = ThreeGenres), openKey = CATEGORY_GENRES, seed = TvFilterSeed(pinned = true))
}

/** The Match all switch focused. */
@Composable
fun TvFilterPaneGenresSwitchFocusedSample() {
    SamplePane(FilterFixture(genres = ThreeGenres, matchAll = true), openKey = CATEGORY_GENRES, seed = TvFilterSeed(switch = true))
}

/** A value focused. */
@Composable
fun TvFilterPaneGenresValueFocusedSample() {
    SamplePane(FilterFixture(genres = ThreeGenres), openKey = CATEGORY_GENRES, seed = TvFilterSeed(key = "comedy"))
}

/** A pinned row an app words itself, with no saved services: it leads to where they are added. */
@Composable
fun TvFilterPaneServicesAddSample() {
    SamplePane(openKey = CATEGORY_PROVIDERS)
}

/** The same pinned row with three saved services: it picks them at once. */
@Composable
fun TvFilterPaneServicesUseSavedSample() {
    SamplePane(FilterFixture(savedProviders = 3), openKey = CATEGORY_PROVIDERS)
}

/** The saved-services row focused. */
@Composable
fun TvFilterPaneServicesUseSavedFocusedSample() {
    SamplePane(FilterFixture(savedProviders = 3), openKey = CATEGORY_PROVIDERS, seed = TvFilterSeed(pinned = true))
}

/** Services chosen: the same row now counts them and ends in Clear. */
@Composable
fun TvFilterPaneServicesSelectedSample() {
    SamplePane(FilterFixture(savedProviders = 3, providers = setOf("netflix", "disney+")), openKey = CATEGORY_PROVIDERS)
}

/** Language rows: the language's own name under its name where they differ, and its code at the end. */
@Composable
fun TvFilterPaneLanguageSample() {
    SamplePane(FilterFixture(localeRows = true, languages = setOf("ja")), openKey = CATEGORY_LANGUAGE)
}

/** Country rows: a flag before the name and the code at the end. */
@Composable
fun TvFilterPaneCountrySample() {
    SamplePane(FilterFixture(localeRows = true, countries = setOf("JP")), openKey = CATEGORY_COUNTRY)
}

/** A single-select category: radio-like rows, one ticked, with nothing pinned above them. */
@Composable
fun TvFilterPaneSingleSelectSample() {
    SamplePane(openKey = CATEGORY_SORT)
}

/** A range with nothing set: Minimum and Maximum tabs over "Any", then the scale. */
@Composable
fun TvFilterPaneRangeNothingSetSample() {
    SamplePane(openKey = CATEGORY_RATING)
}

/** A range set to both bounds: each tab shows its bound and the scale ticks the one being set. */
@Composable
fun TvFilterPaneRangeSetSample() {
    SamplePane(FilterFixture(ratingFrom = 6f, ratingTo = 8f), openKey = CATEGORY_RATING)
}

/** The Maximum tab selected, so the scale sets the upper bound. */
@Composable
fun TvFilterPaneRangeSettingMaximumSample() {
    SamplePane(FilterFixture(ratingFrom = 6f, ratingTo = 8f), openKey = CATEGORY_RATING, ratingBound = TvRangeBound.Max)
}

/** The range's header focused. */
@Composable
fun TvFilterPaneRangeHeaderFocusedSample() {
    SamplePane(FilterFixture(ratingFrom = 6f), openKey = CATEGORY_RATING, rangeSeed = TvFilterRangeSeed(header = true))
}

/** The Minimum tab focused. */
@Composable
fun TvFilterPaneRangeMinimumFocusedSample() {
    SamplePane(FilterFixture(ratingFrom = 6f, ratingTo = 8f), openKey = CATEGORY_RATING, rangeSeed = TvFilterRangeSeed(minTab = true))
}

/** The Maximum tab focused. */
@Composable
fun TvFilterPaneRangeMaximumFocusedSample() {
    SamplePane(FilterFixture(ratingFrom = 6f, ratingTo = 8f), openKey = CATEGORY_RATING, rangeSeed = TvFilterRangeSeed(maxTab = true))
}

/** A step on the scale focused. */
@Composable
fun TvFilterPaneRangeStepFocusedSample() {
    SamplePane(FilterFixture(ratingFrom = 6f), openKey = CATEGORY_RATING, rangeSeed = TvFilterRangeSeed(key = "7.0"))
}

/** A scale of the app's own steps (content ratings), with both bounds set. */
@Composable
fun TvFilterPaneRangeCustomScaleSample() {
    SamplePane(FilterFixture(certGte = "PG", certLte = "R"), openKey = CATEGORY_CERT)
}

/** The pane before the page has a state: plates for the header and the category rows, laid out as the real rows are. */
@Composable
fun TvFilterPaneSkeletonSample() {
    PaneFrame { TvFilterPaneSkeleton() }
}

/** The live pane: OK opens a category, Back or ← leaves it in one press, and focus holds through the swap. */
@Composable
fun TvFilterPaneDemo() {
    var state by remember { mutableStateOf(FilterFixture(savedProviders = 2)) }
    PaneFrame {
        SampleFilterPane(state = state, onState = { state = it }, entry = remember { FocusRequester() }, onOpenServices = {})
    }
}
