package com.binge.designsystem.catalog

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Movie
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import com.binge.designsystem.R
import com.binge.designsystem.component.BingeNumberSlider
import com.binge.designsystem.component.ItemGroup
import com.binge.designsystem.component.bingeNumberItem
import com.binge.designsystem.preview.ScreenshotTheme

private val SAMPLE_RANGE = 1..100
private const val SAMPLE_OPEN_END = "Unlimited"

@Composable
private fun NumberSliderSampleFrame(
    value: Int?,
    openEndLabel: String? = SAMPLE_OPEN_END,
    enabled: Boolean = true,
) {
    // Each sample starts at its own value, which is all a frame sees, and then drags for real.
    var current by remember { mutableStateOf(value) }
    ScreenshotTheme {
        BingeNumberSlider(
            value = current,
            onValueChange = { current = it },
            range = SAMPLE_RANGE,
            format = { it.toString() },
            description = "Movie requests",
            enabled = enabled,
            openEndLabel = openEndLabel,
            modifier = Modifier.padding(dimensionResource(R.dimen.padding_m)),
        )
    }
}

/** A value inside the range, with the open end past its top. */
@Composable
fun BingeNumberSliderValueSample() {
    NumberSliderSampleFrame(10)
}

/** The thumb on the open end: no limit, read as the open end's label. */
@Composable
fun BingeNumberSliderOpenEndSample() {
    NumberSliderSampleFrame(null)
}

/** At the bottom of the range: − is spent and dims. */
@Composable
fun BingeNumberSliderMinimumSample() {
    NumberSliderSampleFrame(1)
}

/** A closed range, whose top is a value like any other and is named under the track. */
@Composable
fun BingeNumberSliderClosedSample() {
    NumberSliderSampleFrame(50, openEndLabel = null)
}

/** Not enabled: the readout, track and steppers dim and take no input. */
@Composable
fun BingeNumberSliderDisabledSample() {
    NumberSliderSampleFrame(10, enabled = false)
}

/** The row that opens the slider in a sheet, its value as the detail. */
@Composable
fun BingeNumberItemSample() {
    ScreenshotTheme {
        var value by rememberSaveable { mutableStateOf<Int?>(null) }
        ItemGroup(
            title = null,
            rows =
                listOf(
                    bingeNumberItem(
                        icon = Icons.Filled.Movie,
                        title = "Movie requests",
                        value = value,
                        range = SAMPLE_RANGE,
                        format = { it.toString() },
                        onChange = { value = it },
                        openEndLabel = SAMPLE_OPEN_END,
                    ),
                ),
        )
    }
}
