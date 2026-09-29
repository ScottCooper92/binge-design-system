package com.binge.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.semantics.Role
import com.binge.designsystem.R
import com.binge.designsystem.shortMonthName
import com.binge.designsystem.theme.BingeShapes
import java.time.Month
import java.util.Locale

private const val MONTH_COLUMNS = 3
private const val YEAR_COLUMNS = 3

@Composable
internal fun MonthGrid(
    selected: Month?,
    locale: Locale,
    onPick: (Month) -> Unit,
    modifier: Modifier = Modifier,
) {
    val gap = dimensionResource(R.dimen.padding_s)
    Column(modifier = modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(gap)) {
        Month.entries.chunked(MONTH_COLUMNS).forEach { row ->
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(gap)) {
                row.forEach { month ->
                    PickerCell(
                        label = shortMonthName(month, locale),
                        selected = month == selected,
                        onClick = { onPick(month) },
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

@Composable
internal fun YearGrid(
    selected: Int?,
    range: IntRange,
    onPick: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val gap = dimensionResource(R.dimen.padding_s)
    val state = rememberLazyGridState(initialFirstVisibleItemIndex = yearGridStartIndex(selected, range, YEAR_COLUMNS))
    LazyVerticalGrid(
        columns = GridCells.Fixed(YEAR_COLUMNS),
        state = state,
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(gap),
        horizontalArrangement = Arrangement.spacedBy(gap),
    ) {
        items(items = range.toList(), key = { it }) { year ->
            PickerCell(label = year.toString(), selected = year == selected, onClick = { onPick(year) })
        }
    }
}

/** One month or year: a tonal cell that fills with the accent when [selected]. */
@Composable
private fun PickerCell(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    Box(
        modifier = modifier
            .heightIn(min = dimensionResource(R.dimen.min_touch_target))
            .clip(BingeShapes.Chip)
            .background(if (selected) scheme.primary else scheme.surfaceContainerLow)
            .selectable(selected = selected, role = Role.RadioButton, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = if (selected) scheme.onPrimary else scheme.onSurface,
        )
    }
}
