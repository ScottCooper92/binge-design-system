@file:CatalogGroup("Time")

package com.binge.designsystem.catalog

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import com.binge.designsystem.R
import com.binge.designsystem.component.BingeTimeInput
import com.binge.designsystem.preview.ScreenshotTheme

@Composable
private fun TimeInputSampleFrame(
    is24Hour: Boolean,
    initialHour: String,
    enabled: Boolean = true,
) {
    var hour by rememberSaveable { mutableStateOf(initialHour) }
    var minute by rememberSaveable { mutableStateOf("30") }
    var afternoon by rememberSaveable { mutableStateOf(true) }
    ScreenshotTheme {
        BingeTimeInput(
            hour = hour,
            minute = minute,
            onHour = { hour = it },
            onMinute = { minute = it },
            is24Hour = is24Hour,
            afternoon = afternoon,
            onAfternoon = { afternoon = it },
            enabled = enabled,
            modifier = Modifier.padding(dimensionResource(R.dimen.padding_m)),
        )
    }
}

/** A 24-hour time, with neither field focused on opening. */
@Composable
fun BingeTimeInput24Sample() = TimeInputSampleFrame(is24Hour = true, initialHour = "17")

/** A 12-hour time whose period chips grow and wrap with the font. */
@Composable
fun BingeTimeInput12Sample() = TimeInputSampleFrame(is24Hour = false, initialHour = "05")

/** Incomplete input stays with its caller, and says it needs a valid time. */
@Composable
fun BingeTimeInputInvalidSample() = TimeInputSampleFrame(is24Hour = false, initialHour = "")

/** A time while its form is unavailable. */
@Composable
fun BingeTimeInputDisabledSample() = TimeInputSampleFrame(is24Hour = false, initialHour = "05", enabled = false)
