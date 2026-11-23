@file:CatalogGroup("Time")

package com.binge.designsystem.catalog

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import com.binge.designsystem.R
import com.binge.designsystem.component.ClockTimeRow
import com.binge.designsystem.preview.ScreenshotTheme

private const val EVENING_MINUTES = 21 * 60 + 30

@Composable
private fun ClockTimeRowSampleFrame(is24Hour: Boolean) {
    var minutes by rememberSaveable { mutableIntStateOf(EVENING_MINUTES) }
    ScreenshotTheme {
        ClockTimeRow(
            label = "Start time",
            minutes = minutes,
            onPick = { minutes = it },
            is24Hour = is24Hour,
            modifier = Modifier.padding(dimensionResource(R.dimen.padding_m)),
        )
    }
}

/** A time of day in a 12-hour phone's clock format. Tap the row for the picker. */
@Composable
fun ClockTimeRow12Sample() = ClockTimeRowSampleFrame(is24Hour = false)

/** The same time in a 24-hour phone's clock format. */
@Composable
fun ClockTimeRow24Sample() = ClockTimeRowSampleFrame(is24Hour = true)
