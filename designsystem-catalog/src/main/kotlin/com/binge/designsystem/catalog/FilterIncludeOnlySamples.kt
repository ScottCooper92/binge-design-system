@file:OnePerScreen
@file:SelfDescribing

package com.binge.designsystem.catalog

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import com.binge.designsystem.R
import com.binge.designsystem.preview.ScreenshotTheme
import com.binge.designsystem.template.FilterControlHandlers
import com.binge.designsystem.template.FilterControlUi
import com.binge.designsystem.template.FilterOptionUi
import com.binge.designsystem.template.FilterSectionContent
import com.binge.designsystem.template.FilterSectionUi
import com.binge.designsystem.template.FilterSheetContent

private val IncludeOnlyGenres = listOf("Action", "Comedy", "Drama", "Horror", "Romance", "Sci-Fi")

/**
 * The filter sheet's body with a genre filter that can only include, beside one that can also exclude. The include-only chips have two
 * states and a tap toggles one; they take no long press, and a screen reader hears each as selected or not. The other group keeps its
 * include and exclude chips. Tap a chip to change it.
 */
@Composable
fun FilterSheetIncludeOnlySample() {
    var included by remember { mutableStateOf(setOf("Action", "Drama")) }
    var mixedIncluded by remember { mutableStateOf(setOf("Action")) }
    var mixedExcluded by remember { mutableStateOf(setOf("Horror")) }
    ScreenshotTheme {
        Column(Modifier.height(dimensionResource(R.dimen.catalog_filter_sheet_height))) {
            FilterSheetContent(
                title = "Filters",
                clearAllLabel = "Clear all",
                showResultsLabel = "Show results",
                appliedLabel = "${included.size + mixedIncluded.size + mixedExcluded.size} applied",
                sections =
                    listOf(
                        FilterSectionUi(
                            id = "genre",
                            title = "Genre (include only)",
                            control =
                                FilterControlUi.MultiChoice(
                                    options = IncludeOnlyGenres.map { FilterOptionUi(it, it) },
                                    includedIds = included,
                                    excludedIds = emptySet(),
                                    includeOnly = true,
                                ),
                        ),
                        FilterSectionUi(
                            id = "mixed",
                            title = "Genre (include or exclude)",
                            control =
                                FilterControlUi.MultiChoice(
                                    options = IncludeOnlyGenres.map { FilterOptionUi(it, it) },
                                    includedIds = mixedIncluded,
                                    excludedIds = mixedExcluded,
                                ),
                        ),
                    ),
                onOptionClick = { section, id ->
                    if (section == "genre") {
                        included = if (id in included) included - id else included + id
                    } else {
                        mixedIncluded = if (id in mixedIncluded) mixedIncluded - id else mixedIncluded + id
                        mixedExcluded = mixedExcluded - id
                    }
                },
                onOptionLongClick = { _, id ->
                    mixedExcluded = if (id in mixedExcluded) mixedExcluded - id else mixedExcluded + id
                    mixedIncluded = mixedIncluded - id
                },
                onApply = {},
                onClearAll = {
                    included = emptySet()
                    mixedIncluded = emptySet()
                    mixedExcluded = emptySet()
                },
            )
        }
    }
}

/** An include-only filter in its own sheet: two-state chips with a Clear and an Apply. Tap a chip to toggle it. */
@Composable
fun FilterSectionIncludeOnlySample() {
    var included by remember { mutableStateOf(setOf("Action", "Drama")) }
    ScreenshotTheme {
        Column {
            FilterSectionContent(
                section =
                    FilterSectionUi(
                        id = "genre",
                        title = "Genres",
                        control =
                            FilterControlUi.MultiChoice(
                                options = IncludeOnlyGenres.map { FilterOptionUi(it, it) },
                                includedIds = included,
                                excludedIds = emptySet(),
                                includeOnly = true,
                            ),
                    ),
                clearLabel = "Clear",
                clearEnabled = included.isNotEmpty(),
                doneLabel = "Apply",
                onClear = { included = emptySet() },
                onDone = {},
                handlers =
                    FilterControlHandlers(
                        onOptionClick = { _, id -> included = if (id in included) included - id else included + id },
                    ),
            )
        }
    }
}
