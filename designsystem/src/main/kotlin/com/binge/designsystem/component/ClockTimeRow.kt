package com.binge.designsystem.component

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TimeInput
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import com.binge.designsystem.clockTimeText
import java.util.Locale
import com.binge.designsystem.R as DesR

private const val CLOCK_MINUTES_PER_DAY = 1440
private const val MINUTES_PER_HOUR = 60

/** From this font scale the dial no longer fits a dialog, so the picker takes its keyboard entry instead. */
private const val KEYBOARD_FONT_SCALE = 1.3f

/**
 * A time of day as a row: [label], and the time in the phone's clock format as its detail. A tap opens Material's time
 * picker in a dialog, which handles a.m. and p.m. and the 12 or 24-hour dial itself; OK hands back the minutes since
 * midnight through [onPick]. Reach for it for a time in a settings row; for a time typed as a form field, use
 * `BingeTimeInput`. At a large font the dialog takes the picker's keyboard entry, which still fits.
 */
@Composable
fun ClockTimeRow(
    label: String,
    minutes: Int,
    onPick: (Int) -> Unit,
    is24Hour: Boolean,
    modifier: Modifier = Modifier,
) {
    var open by rememberSaveable { mutableStateOf(false) }
    val locale = LocalConfiguration.current.locales[0] ?: Locale.ROOT
    ItemGroup(
        modifier = modifier,
        title = null,
        rows =
            listOf(
                ListItem(
                    icon = Icons.Outlined.Schedule,
                    label = label,
                    detail = clockTimeText(minutes, is24Hour, locale),
                    destination = ListItemDestination.Action,
                    onClick = { open = true },
                ),
            ),
    )
    if (open) ClockTimeDialog(label, Math.floorMod(minutes, CLOCK_MINUTES_PER_DAY), is24Hour, onPick = onPick, onDismiss = { open = false })
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ClockTimeDialog(
    label: String,
    minutes: Int,
    is24Hour: Boolean,
    onPick: (Int) -> Unit,
    onDismiss: () -> Unit,
) {
    val state =
        rememberTimePickerState(
            initialHour = minutes / MINUTES_PER_HOUR,
            initialMinute = minutes % MINUTES_PER_HOUR,
            is24Hour = is24Hour,
        )
    val keyboard = LocalDensity.current.fontScale >= KEYBOARD_FONT_SCALE
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(label) },
        text = { if (keyboard) TimeInput(state) else TimePicker(state) },
        confirmButton = {
            BingeTextButton(
                label = stringResource(DesR.string.action_ok),
                onClick = {
                    onPick(state.hour * MINUTES_PER_HOUR + state.minute)
                    onDismiss()
                },
            )
        },
        dismissButton = { BingeTextButton(label = stringResource(DesR.string.text_entry_cancel), onClick = onDismiss) },
    )
}
