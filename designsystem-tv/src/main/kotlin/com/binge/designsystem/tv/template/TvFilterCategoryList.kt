package com.binge.designsystem.tv.template

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import com.binge.designsystem.tv.component.TvFilterCategory
import com.binge.designsystem.tv.component.summary
import com.binge.designsystem.tv.focus.tvPaneEntry
import com.binge.designsystem.tv.layout.TvLayoutAnchors
import com.binge.designsystem.tv.layout.tvLayoutAnchor
import com.binge.designsystem.R as DesR
import com.binge.designsystem.tv.R as TvR

/**
 * The category list, under a line naming how many filters are set with Clear beside it. Each category shows its current
 * choice; [entryKey] is the one focus returns to.
 */
@Composable
internal fun TvFilterCategoryList(
    categories: List<TvFilterCategory>,
    entryKey: String?,
    filterCount: Int,
    copy: TvFilterPaneCopy,
    onClear: () -> Unit,
    onFocused: (String) -> Unit,
    onOpen: (String) -> Unit,
    entry: FocusRequester,
    modifier: Modifier = Modifier,
    seed: TvFilterSeed = TvFilterSeed(),
    onEntryPlaced: () -> Unit = {},
    entryLooksFocused: Boolean = false,
) {
    val noneLabel = copy.any
    val excludedFormat = copy.excludedFormat
    val separator = stringResource(DesR.string.list_separator)
    val focusKey = categories.firstOrNull { it.key == entryKey }?.key ?: categories.firstOrNull()?.key
    // The header sits above the list, not in it, so scrolling the categories never hides the count and Clear.
    Column(modifier) {
        TvFilterHeader(filterCount, copy, onClear, initiallyFocused = seed.header)
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(dimensionResource(DesR.dimen.padding_xs)),
            contentPadding = PaddingValues(bottom = dimensionResource(TvR.dimen.tv_overscan_vertical)),
        ) {
            items(categories, key = { it.key }) { category ->
                TvFilterRow(
                    label = category.label,
                    detail = category.summary(noneLabel, excludedFormat, separator),
                    icon = category.icon,
                    onClick = { onOpen(category.key) },
                    onFocused = { onFocused(category.key) },
                    initiallyFocused = category.key == seed.key || (entryLooksFocused && category.key == focusKey),
                    modifier = Modifier
                        .then(
                            if (category.key == categories.first().key) {
                                Modifier.tvLayoutAnchor(TvLayoutAnchors.entry(TV_FILTER_ANCHOR_FIRST_CATEGORY))
                            } else {
                                Modifier
                            },
                        ).then(if (category.key == focusKey) Modifier.tvPaneEntry(entry, onEntryPlaced) else Modifier),
                )
            }
        }
    }
}

/**
 * "2 filters … Clear" as one row: focusable and pressed as a whole to clear, with Clear as plain text at its end, not a
 * shaped button. With nothing set it is just "No filters", and nothing to land on.
 */
@Composable
private fun TvFilterHeader(
    count: Int,
    copy: TvFilterPaneCopy,
    onClear: () -> Unit,
    initiallyFocused: Boolean,
) {
    TvFilterClearRow(
        text = if (count == 0) copy.noFilters else copy.filterCount(count),
        canClear = count > 0,
        clearLabel = copy.clear,
        onClear = onClear,
        initiallyFocused = initiallyFocused,
    )
}
