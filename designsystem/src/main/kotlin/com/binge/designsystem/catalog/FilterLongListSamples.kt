@file:OnePerScreen
@file:SelfDescribing

package com.binge.designsystem.catalog

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import com.binge.designsystem.R
import com.binge.designsystem.component.BingeFilledButton
import com.binge.designsystem.preview.ScreenshotTheme
import com.binge.designsystem.template.FilterControlHandlers
import com.binge.designsystem.template.FilterControlUi
import com.binge.designsystem.template.FilterOptionUi
import com.binge.designsystem.template.FilterRowUi
import com.binge.designsystem.template.FilterSearchUi
import com.binge.designsystem.template.FilterSectionContent
import com.binge.designsystem.template.FilterSectionUi
import com.binge.designsystem.template.FilterSheet

private const val YEARS_ID = "years"
private const val LATEST_YEAR = 2026
private const val YEAR_COUNT = 80

/** A library's release years, newest first: eighty of them, too many for the sheet's body. */
private val Years = List(YEAR_COUNT) { (LATEST_YEAR - it).toString() }

@Composable
private fun YearsSecondLevel(initialQuery: String, initialIncluded: Set<String>) {
    var query by remember { mutableStateOf(initialQuery) }
    var included by remember { mutableStateOf(initialIncluded) }
    ScreenshotTheme {
        Column(Modifier.height(dimensionResource(R.dimen.catalog_filter_sheet_height))) {
            FilterSectionContent(
                section =
                    FilterSectionUi(
                        id = YEARS_ID,
                        title = "Release year",
                        control =
                            FilterControlUi.MultiChoice(
                                options = Years.map { FilterOptionUi(it, it) },
                                includedIds = included,
                                excludedIds = emptySet(),
                            ),
                    ),
                clearLabel = "Clear",
                clearEnabled = included.isNotEmpty(),
                doneLabel = "Done",
                onClear = { included = emptySet() },
                onDone = {},
                handlers = FilterControlHandlers(onOptionClick = { _, id ->
                    included = if (id in
                        included
                    ) {
                        included - id
                    } else {
                        included + id
                    }
                }),
                onBack = {},
                search =
                    FilterSearchUi(
                        query = query,
                        onQueryChange = { query = it },
                        placeholder = "Search years",
                        noResultsLabel = "No year matches your search",
                    ),
            )
        }
    }
}

/**
 * A filter with eighty options as the sheet's second level: a back arrow, a search field and every year as a chip, with Done at the foot.
 * It is what [FilterSectionContent] draws when a row opens it, with `search` set. Type to narrow the years and tap one to choose it.
 */
@Composable
fun FilterSectionLongListSample() {
    YearsSecondLevel(initialQuery = "", initialIncluded = setOf("2024", "2020"))
}

/** The same level with "199" typed: only the nineties are left. */
@Composable
fun FilterSectionLongListQuerySample() {
    YearsSecondLevel(initialQuery = "199", initialIncluded = setOf("1994"))
}

/** The same level with a search that matches nothing: the no-results line stands in for the chips. */
@Composable
fun FilterSectionLongListNoResultsSample() {
    YearsSecondLevel(initialQuery = "xyz", initialIncluded = setOf("2020"))
}

/**
 * The filter sheet with a second level, opened for real: tap "Release year" and it slides in as a page of the sheet with a search field
 * over the eighty years; the back arrow (a phone draws it, and the side sheet has a Back bar of its own), system Back and a swipe go back to the sheet where it was left, and Show results closes it. It
 * opens part-way on a phone and is a side sheet in a window 840dp wide or more.
 */
@Composable
fun FilterSheetSecondLevelDemo() {
    var open by remember { mutableStateOf(false) }
    var included by remember { mutableStateOf(setOf("2020")) }
    var query by remember { mutableStateOf("") }
    ScreenshotTheme {
        Column(Modifier.fillMaxWidth().padding(dimensionResource(R.dimen.padding_m))) {
            BingeFilledButton(label = "Open the filters", onClick = { open = true })
        }
        if (open) {
            FilterSheet(
                title = "Filters",
                clearAllLabel = "Clear all",
                showResultsLabel = "Show results",
                appliedLabel = if (included.isEmpty()) null else "${included.size} applied",
                sections = emptyList(),
                rows =
                    listOf(
                        FilterRowUi(
                            id = YEARS_ID,
                            title = "Release year",
                            summary = if (included.isEmpty()) "Any" else included.sorted().joinToString(", "),
                            icon = Icons.Filled.CalendarMonth,
                        ),
                    ),
                onDismiss = { open = false },
                onApply = { open = false },
                onClearAll = { included = emptySet() },
                onDetailBack = { query = "" },
                detail = { _, nav, onBack ->
                    FilterSectionContent(
                        section =
                            FilterSectionUi(
                                id = YEARS_ID,
                                title = "Release year",
                                control =
                                    FilterControlUi.MultiChoice(
                                        options = Years.map { FilterOptionUi(it, it) },
                                        includedIds = included,
                                        excludedIds = emptySet(),
                                    ),
                            ),
                        clearLabel = "Clear",
                        clearEnabled = included.isNotEmpty(),
                        doneLabel = "Done",
                        onClear = { included = emptySet() },
                        onDone = { nav.pop() },
                        handlers = FilterControlHandlers(onOptionClick = { _, id ->
                            included =
                                if (id in included) included - id else included + id
                        }),
                        onBack = onBack,
                        search =
                            FilterSearchUi(
                                query = query,
                                onQueryChange = { query = it },
                                placeholder = "Search years",
                                noResultsLabel = "No year matches your search",
                            ),
                    )
                },
            )
        }
    }
}
