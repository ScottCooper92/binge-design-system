package com.binge.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import com.binge.designsystem.R

private const val HALF_DAY_HOURS = 12
private const val DAY_HOURS = 24
private const val MINUTES_PER_HOUR = 60
private const val FIELD_DIGITS = 2

/** A typed time as minutes since midnight, or null while either field is blank or out of range. */
fun timeOfDayMinutes(
    hour: String,
    minute: String,
    is24Hour: Boolean,
    afternoon: Boolean = false,
): Int? {
    val h = hour.toIntOrNull() ?: return null
    val m = minute.toIntOrNull()?.takeIf { it in 0 until MINUTES_PER_HOUR } ?: return null
    val hours =
        if (is24Hour) {
            h.takeIf { it in 0 until DAY_HOURS } ?: return null
        } else {
            if (h !in 1..HALF_DAY_HOURS) return null
            h % HALF_DAY_HOURS + if (afternoon) HALF_DAY_HOURS else 0
        }
    return hours * MINUTES_PER_HOUR + m
}

/**
 * A time of day with two numeric fields and, in 12-hour mode, wrapping period chips. No field requests focus on opening;
 * when a field takes focus its caret starts after the existing digits. [hour] and [minute] stay with the caller, including
 * incomplete input, so a form can hold them above its modal window and disable submission while [timeOfDayMinutes] is null.
 * In 12-hour mode supply [afternoon] and [onAfternoon] as well. A 24-hour value runs from 00:00 to 23:59.
 */
@Composable
fun BingeTimeInput(
    hour: String,
    minute: String,
    onHour: (String) -> Unit,
    onMinute: (String) -> Unit,
    is24Hour: Boolean,
    modifier: Modifier = Modifier,
    afternoon: Boolean = false,
    onAfternoon: (Boolean) -> Unit = {},
    enabled: Boolean = true,
) {
    val validHour = hour.toIntOrNull()?.let { if (is24Hour) it in 0 until DAY_HOURS else it in 1..HALF_DAY_HOURS } == true
    val validMinute = minute.toIntOrNull()?.let { it in 0 until MINUTES_PER_HOUR } == true
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_s)),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_s)), verticalAlignment = Alignment.Top) {
            TimeField(
                hour,
                onHour,
                stringResource(R.string.time_input_hour),
                validHour,
                enabled,
                ImeAction.Next,
                Modifier.weight(1f, fill = false).widthIn(max = dimensionResource(R.dimen.time_input_field_max_width)),
            )
            Text(
                ":",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(top = dimensionResource(R.dimen.padding_sm)),
            )
            TimeField(
                minute,
                onMinute,
                stringResource(R.string.time_input_minute),
                validMinute,
                enabled,
                ImeAction.Done,
                Modifier.weight(1f, fill = false).widthIn(max = dimensionResource(R.dimen.time_input_field_max_width)),
            )
        }
        if (!is24Hour) {
            WrappingRow {
                BingeFilterChip(
                    stringResource(R.string.time_input_am),
                    selected = !afternoon,
                    onClick = { onAfternoon(false) },
                    enabled = enabled,
                )
                BingeFilterChip(
                    stringResource(R.string.time_input_pm),
                    selected = afternoon,
                    onClick = { onAfternoon(true) },
                    enabled = enabled,
                )
            }
        }
        if (!validHour || !validMinute) {
            Text(
                stringResource(R.string.time_input_invalid),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error,
            )
        }
    }
}

/** The specialized numeric field owns its selection; the public component exposes just the draft text. */
@Composable
private fun TimeField(
    text: String,
    onText: (String) -> Unit,
    label: String,
    valid: Boolean,
    enabled: Boolean,
    imeAction: ImeAction,
    modifier: Modifier,
) {
    var editing by rememberSaveable(stateSaver = TextFieldValue.Saver) { mutableStateOf(TextFieldValue(text, TextRange(text.length))) }
    var focused by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
    SideEffect { if (editing.text != text) editing = TextFieldValue(text, TextRange(text.length)) }
    LaunchedEffect(focused) {
        if (focused) {
            // A tap first positions the caret; move it after that event has been applied, only on the focus transition.
            withFrameNanos { }
            editing = editing.copy(selection = TextRange(editing.text.length))
        }
    }
    OutlinedTextField(
        value = editing,
        onValueChange = { next ->
            val digits = next.text.filter(Char::isDigit).take(FIELD_DIGITS)
            editing =
                next.copy(
                    text = digits,
                    selection = TextRange(next.selection.start.coerceAtMost(digits.length), next.selection.end.coerceAtMost(digits.length)),
                )
            onText(digits)
        },
        label = { Text(label) },
        enabled = enabled,
        singleLine = true,
        isError = !valid,
        keyboardOptions = fieldKeyboardOptions(
            secret = false,
            keyboardType = KeyboardType.Number,
            autoCorrect = false,
            imeAction = imeAction,
        ),
        keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Next) }, onDone = { focusManager.clearFocus() }),
        modifier = modifier.onFocusChanged { focused = it.isFocused },
    )
}
