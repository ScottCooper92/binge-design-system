package com.binge.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import com.binge.designsystem.R
import com.binge.designsystem.formatMonthYear
import java.time.LocalDate
import java.time.YearMonth
import java.util.Locale

/** The first year the fields' picker offers. */
private const val FIRST_PICKABLE_YEAR = 1900

/** How many years past [today][BingeDateBoundFields] the picker offers, so a range can reach what is still to come. */
private const val PICKABLE_YEARS_AHEAD = 5

/**
 * A pair of "From" and "To" fields for a date range, each opening a month and year picker
 * ([MonthYearPickerDialog]). A field with no date shows its label alone; a set one shows the label small over the month
 * and year, with a clear button that sets it back to `null`. The two fields are equal in width, and each grows taller
 * together when its text wraps, so a large font scale or a long label reflows instead of clipping.
 *
 * The bound is a month, so a picked month arrives as a day: the first of it for [onFromChange], the last of it for
 * [onToChange]. That makes the range cover every day of both months it names.
 *
 * [fromMaximum] and [toMinimum] keep the fields from crossing: the From picker offers nothing after [fromMaximum]
 * (pass the To date) and the To picker nothing before [toMinimum] (pass the From date). [today] is the year both
 * pickers open on when nothing is set; the caller passes it so a frame does not move with the clock.
 *
 * The consumer owns the words: [fromLabel] and [toLabel] name the fields and title their pickers, and
 * [fromClearDescription] and [toClearDescription] are what a screen reader hears on each clear button.
 */
@Composable
fun BingeDateBoundFields(
    from: LocalDate?,
    to: LocalDate?,
    fromLabel: String,
    toLabel: String,
    fromClearDescription: String,
    toClearDescription: String,
    onFromChange: (LocalDate?) -> Unit,
    onToChange: (LocalDate?) -> Unit,
    fromMaximum: LocalDate?,
    toMinimum: LocalDate?,
    today: LocalDate,
    modifier: Modifier = Modifier,
) {
    var picking by rememberSaveable { mutableStateOf<DateBoundSide?>(null) }
    Row(
        // Intrinsic height, so the field with the shorter text stretches to its neighbour's height.
        modifier = modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min),
        horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_s)),
    ) {
        DateBoundField(
            label = fromLabel,
            value = from,
            clearDescription = fromClearDescription,
            onClick = { picking = DateBoundSide.From },
            onClear = { onFromChange(null) },
            modifier = Modifier.weight(1f).fillMaxHeight(),
        )
        DateBoundField(
            label = toLabel,
            value = to,
            clearDescription = toClearDescription,
            onClick = { picking = DateBoundSide.To },
            onClear = { onToChange(null) },
            modifier = Modifier.weight(1f).fillMaxHeight(),
        )
    }
    when (picking) {
        DateBoundSide.From -> DateBoundPicker(
            title = fromLabel,
            value = from,
            minimum = null,
            maximum = fromMaximum,
            today = today,
            onDismiss = { picking = null },
            onConfirm = {
                picking = null
                onFromChange(it.asFromBound())
            },
        )
        DateBoundSide.To -> DateBoundPicker(
            title = toLabel,
            value = to,
            minimum = toMinimum,
            maximum = null,
            today = today,
            onDismiss = { picking = null },
            onConfirm = {
                picking = null
                onToChange(it.asToBound())
            },
        )
        null -> Unit
    }
}

/** The day a picked month sets a From field to: the first of it, so the range starts with the month's first day. */
internal fun YearMonth.asFromBound(): LocalDate = atDay(1)

/** The day a picked month sets a To field to: the last of it, so the range ends with the month's last day. */
internal fun YearMonth.asToBound(): LocalDate = atEndOfMonth()

/** Which field's picker is open. Saved with the field, so a rotation keeps the picker up. */
private enum class DateBoundSide { From, To }

@Composable
private fun DateBoundPicker(
    title: String,
    value: LocalDate?,
    minimum: LocalDate?,
    maximum: LocalDate?,
    today: LocalDate,
    onDismiss: () -> Unit,
    onConfirm: (YearMonth) -> Unit,
) {
    MonthYearPickerDialog(
        title = title,
        mode = MonthYearPickerMode.MonthAndYear,
        yearRange = FIRST_PICKABLE_YEAR..today.year + PICKABLE_YEARS_AHEAD,
        onDismissRequest = onDismiss,
        onConfirm = { selection ->
            // The picker only confirms once both parts are chosen.
            val year = selection.year ?: return@MonthYearPickerDialog
            val month = selection.month ?: return@MonthYearPickerDialog
            onConfirm(YearMonth.of(year, month))
        },
        initial = MonthYearSelection(value?.year, value?.month),
        minimum = minimum?.let(YearMonth::from),
        maximum = maximum?.let(YearMonth::from),
        defaultYear = today.year,
    )
}

@Composable
private fun DateBoundField(
    label: String,
    value: LocalDate?,
    clearDescription: String,
    onClick: () -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier,
    locale: Locale = LocalConfiguration.current.locales[0],
) {
    Surface(
        onClick = onClick,
        // A clickable `Surface` is not announced as a button on its own; the field is one, so it says so.
        modifier = modifier
            .heightIn(min = dimensionResource(R.dimen.min_touch_target))
            .semantics { role = Role.Button },
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
    ) {
        Row(
            modifier = Modifier.padding(start = dimensionResource(R.dimen.padding_m)),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Filled.CalendarMonth,
                contentDescription = null,
                modifier = Modifier.size(dimensionResource(R.dimen.icon_size_l)),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.width(dimensionResource(R.dimen.padding_s)))
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = dimensionResource(R.dimen.padding_s)),
            ) {
                if (value == null) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                } else {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = value.formatMonthYear(locale),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
            if (value != null) {
                IconButton(onClick = onClear) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = clearDescription,
                        modifier = Modifier.size(dimensionResource(R.dimen.icon_size_l)),
                    )
                }
            } else {
                Spacer(Modifier.width(dimensionResource(R.dimen.padding_m)))
            }
        }
    }
}
