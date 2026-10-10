package com.binge.designsystem.component

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import com.binge.designsystem.DISABLED_ALPHA
import com.binge.designsystem.R
import com.binge.designsystem.shortMonthName
import com.binge.designsystem.theme.BingeShapes
import java.time.Month
import java.util.Locale

private const val MONTH_COLUMNS = 3
private const val YEAR_COLUMNS = 3
private const val MONTH_ROWS = 12 / MONTH_COLUMNS

/**
 * The height the grids share: four rows of month cells, never less than the design height. A cell is its
 * label's line plus padding, or the touch target, whichever is taller, so the box grows with the font size
 * instead of clipping the last row. Both grids get it, so the dialog does not resize when the years open.
 */
@Composable
internal fun monthGridHeight(): Dp {
    val lineHeight = with(LocalDensity.current) {
        MaterialTheme.typography.bodyLarge.lineHeight
            .toDp()
    }
    return monthGridHeight(
        lineHeight = lineHeight,
        cellMin = dimensionResource(R.dimen.min_touch_target),
        cellPadding = dimensionResource(R.dimen.padding_s),
        gap = dimensionResource(R.dimen.padding_s),
        floor = dimensionResource(R.dimen.month_year_picker_grid_height),
    )
}

internal fun monthGridHeight(
    lineHeight: Dp,
    cellMin: Dp,
    cellPadding: Dp,
    gap: Dp,
    floor: Dp,
): Dp {
    val row = maxOf(cellMin, lineHeight + cellPadding * 2)
    return maxOf(floor, row * MONTH_ROWS + gap * (MONTH_ROWS - 1))
}

@Composable
internal fun MonthGrid(
    selected: Month?,
    locale: Locale,
    isEnabled: (Month) -> Boolean,
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
                        enabled = isEnabled(month),
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
    defaultYear: Int,
    onPick: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val gap = dimensionResource(R.dimen.padding_s)
    val state = rememberLazyGridState(initialFirstVisibleItemIndex = yearGridStartIndex(selected, range, YEAR_COLUMNS, defaultYear))
    LazyVerticalGrid(
        columns = GridCells.Fixed(YEAR_COLUMNS),
        state = state,
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(gap),
        horizontalArrangement = Arrangement.spacedBy(gap),
    ) {
        items(items = range.toList(), key = { it }) { year ->
            PickerCell(label = year.toString(), selected = year == selected, enabled = true, onClick = { onPick(year) })
        }
    }
}

/** One month or year: a plain label, outlined in a pill sized to it when [selected], inside a full-width touch cell. */
@Composable
private fun PickerCell(
    label: String,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val accent = MaterialTheme.colorScheme.primary
    Box(
        modifier = modifier
            .heightIn(min = dimensionResource(R.dimen.min_touch_target))
            .alpha(if (enabled) 1f else DISABLED_ALPHA)
            .selectable(selected = selected, enabled = enabled, role = Role.RadioButton, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .then(
                    if (selected) {
                        Modifier.border(dimensionResource(R.dimen.month_year_picker_selected_border), accent, BingeShapes.Pill)
                    } else {
                        Modifier
                    },
                ).padding(
                    horizontal = dimensionResource(R.dimen.month_year_picker_pill_padding_h),
                    vertical = dimensionResource(R.dimen.padding_s),
                ),
            contentAlignment = Alignment.Center,
        ) {
            Text(text = label, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}
