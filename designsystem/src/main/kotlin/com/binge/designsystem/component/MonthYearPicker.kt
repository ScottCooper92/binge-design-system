package com.binge.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.window.Dialog
import com.binge.designsystem.R
import java.time.Month
import java.time.YearMonth
import java.util.Locale

/**
 * A month and/or year picker in a dialog, for a bound that is a period rather than a day. [mode]
 * says which parts to ask for; [onConfirm] gets a [MonthYearSelection] with every asked part set and
 * the others `null`. [title] is the caller's copy, since only it knows what is being picked.
 *
 * [minimum] and [maximum] bound what can be picked, for an end that must not precede its start: years
 * outside them are not offered, and months outside them in the boundary year are dimmed and inert.
 * They apply where a year is picked, so the month-only mode ignores them.
 *
 * The visible body is [MonthYearPickerContent], stateless so screenshot tests can render it: the
 * modal [Dialog] window does not capture in previews.
 */
@Composable
fun MonthYearPickerDialog(
    title: String,
    mode: MonthYearPickerMode,
    yearRange: IntRange,
    onDismiss: () -> Unit,
    onConfirm: (MonthYearSelection) -> Unit,
    initial: MonthYearSelection = MonthYearSelection(),
    minimum: YearMonth? = null,
    maximum: YearMonth? = null,
) {
    var year by rememberSaveable { mutableStateOf(initial.year) }
    var monthValue by rememberSaveable { mutableStateOf(initial.month?.value) }
    var yearsOpen by rememberSaveable { mutableStateOf(false) }
    val selection = MonthYearSelection(year, monthValue?.let(Month::of))
    Dialog(onDismissRequest = onDismiss) {
        MonthYearPickerContent(
            title = title,
            mode = mode,
            selection = selection,
            yearRange = yearRange,
            minimum = minimum,
            maximum = maximum,
            yearsOpen = yearsOpen,
            onYearsOpenChange = { yearsOpen = it },
            onSelectionChange = {
                year = it.year
                monthValue = it.month?.value
            },
            onConfirm = { onConfirm(selection) },
            onDismiss = onDismiss,
        )
    }
}

/**
 * Stateless body of [MonthYearPickerDialog]. [yearsOpen] swaps the month grid for a year grid when
 * the mode asks for both; the year-only mode always shows the year grid.
 */
@Composable
internal fun MonthYearPickerContent(
    title: String,
    mode: MonthYearPickerMode,
    selection: MonthYearSelection,
    yearRange: IntRange,
    yearsOpen: Boolean,
    onYearsOpenChange: (Boolean) -> Unit,
    onSelectionChange: (MonthYearSelection) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    minimum: YearMonth? = null,
    maximum: YearMonth? = null,
    locale: Locale = LocalConfiguration.current.locales[0],
) {
    val range = yearRange.within(minimum, maximum)
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.extraLarge,
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
    ) {
        Column {
            PickerHeader(
                overline = title,
                headline = pickerHeadline(mode, selection, locale, stringResource(R.string.month_year_picker_empty)),
                locale = locale,
            )
            Column(
                modifier = Modifier.padding(dimensionResource(R.dimen.padding_m)),
                verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_m)),
            ) {
                if (mode == MonthYearPickerMode.MonthAndYear) {
                    YearStepper(
                        year = selection.displayYear(range),
                        yearRange = range,
                        yearsOpen = yearsOpen,
                        onYearsOpenChange = onYearsOpenChange,
                        onStep = { onSelectionChange(selection.stepYear(it, range)) },
                    )
                }
                Box(modifier = Modifier.fillMaxWidth().height(dimensionResource(R.dimen.month_year_picker_grid_height))) {
                    if (mode == MonthYearPickerMode.Month || (mode == MonthYearPickerMode.MonthAndYear && !yearsOpen)) {
                        MonthGrid(
                            selected = selection.month,
                            locale = locale,
                            isEnabled = { monthAllowed(selection.displayYear(range), it, minimum, maximum) },
                            onPick = { onSelectionChange(selection.pickMonth(it, mode, range)) },
                        )
                    } else {
                        YearGrid(
                            selected = selection.year,
                            range = range,
                            onPick = {
                                onSelectionChange(selection.pickYear(it))
                                onYearsOpenChange(false)
                            },
                        )
                    }
                }
                PickerActions(
                    canConfirm = selection.isCompleteFor(mode) && selection.isWithin(mode, minimum, maximum),
                    onConfirm = onConfirm,
                    onDismiss = onDismiss,
                )
            }
        }
    }
}

/** The accent block on top: the caller's title as a small overline, and what is picked so far, large. */
@Composable
private fun PickerHeader(
    overline: String,
    headline: String,
    locale: Locale,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primary)
            .padding(dimensionResource(R.dimen.padding_l)),
        verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_s)),
    ) {
        Text(
            text = overline.uppercase(locale),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onPrimary,
        )
        Text(
            text = headline,
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onPrimary,
        )
    }
}

@Composable
private fun YearStepper(
    year: Int,
    yearRange: IntRange,
    yearsOpen: Boolean,
    onYearsOpenChange: (Boolean) -> Unit,
    onStep: (Int) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = { onStep(-1) }, enabled = !yearsOpen && canStepYear(year, -1, yearRange)) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.month_year_picker_previous_year))
        }
        Row(
            modifier = Modifier
                .heightIn(min = dimensionResource(R.dimen.min_touch_target))
                .clickable(role = Role.Button) { onYearsOpenChange(!yearsOpen) },
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(text = year.toString(), style = MaterialTheme.typography.headlineSmall)
            Icon(
                imageVector = if (yearsOpen) Icons.Filled.ArrowDropUp else Icons.Filled.ArrowDropDown,
                contentDescription = stringResource(R.string.month_year_picker_choose_year),
            )
        }
        IconButton(onClick = { onStep(1) }, enabled = !yearsOpen && canStepYear(year, 1, yearRange)) {
            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = stringResource(R.string.month_year_picker_next_year))
        }
    }
}

@Composable
private fun PickerActions(
    canConfirm: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_s), Alignment.End),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        BingeTextButton(
            label = stringResource(R.string.month_year_picker_cancel),
            onClick = onDismiss,
            contentColor = MaterialTheme.colorScheme.onSurface,
        )
        BingeTextButton(label = stringResource(R.string.month_year_picker_confirm), onClick = onConfirm, enabled = canConfirm)
    }
}
