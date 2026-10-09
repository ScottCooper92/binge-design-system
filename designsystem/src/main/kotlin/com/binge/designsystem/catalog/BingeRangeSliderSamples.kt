package com.binge.designsystem.catalog

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import com.binge.designsystem.R
import com.binge.designsystem.component.BingeRangeSlider
import com.binge.designsystem.preview.ScreenshotTheme

private const val SAMPLE_MAX = 240f
private const val SAMPLE_STEP = 15f
private const val SAMPLE_STEPS = 15

@Composable
private fun RangeSliderSampleFrame(values: ClosedFloatingPointRange<Float>, enabled: Boolean = true) {
    // Each sample starts at its own values, which is all a frame sees, and then drags for real.
    var current by remember { mutableStateOf(values) }
    ScreenshotTheme {
        BingeRangeSlider(
            values = current,
            onValuesChange = { current = it },
            valueRange = 0f..SAMPLE_MAX,
            steps = SAMPLE_STEPS,
            valueLabel = { "${it.toInt()} min" },
            startThumbDescription = "Minimum",
            endThumbDescription = "Maximum",
            enabled = enabled,
            openEndLabel = "${SAMPLE_MAX.toInt()}+ min",
            modifier = Modifier.padding(dimensionResource(R.dimen.padding_m)),
        )
    }
}

/** Both thumbs parked at the ends: nothing is constrained, and the open end reads "240+ min". */
@Composable
fun BingeRangeSliderUnsetSample() {
    RangeSliderSampleFrame(0f..SAMPLE_MAX)
}

/** Only the start thumb moved: a floor with no ceiling. */
@Composable
fun BingeRangeSliderMinOnlySample() {
    RangeSliderSampleFrame(90f..SAMPLE_MAX)
}

/** Only the end thumb moved: a ceiling with no floor. */
@Composable
fun BingeRangeSliderMaxOnlySample() {
    RangeSliderSampleFrame(0f..90f)
}

/** Both thumbs moved into the track: a bounded range. */
@Composable
fun BingeRangeSliderRangeSample() {
    RangeSliderSampleFrame(60f..150f)
}

/** The thumbs one step apart, the closest they can sit without crossing. */
@Composable
fun BingeRangeSliderAdjacentSample() {
    RangeSliderSampleFrame(120f..(120f + SAMPLE_STEP))
}

/** Not enabled: the track and thumbs dim and take no drag. */
@Composable
fun BingeRangeSliderDisabledSample() {
    RangeSliderSampleFrame(60f..150f, enabled = false)
}

/** A closed track, where the top is a value like any other: a rating from 0 to 10 in half points. */
@Composable
fun BingeRangeSliderClosedSample() {
    var current by remember { mutableStateOf(6.5f..9f) }
    ScreenshotTheme {
        BingeRangeSlider(
            values = current,
            onValuesChange = { current = it },
            valueRange = 0f..RATING_MAX,
            steps = RATING_STEPS,
            valueLabel = { "%.1f".format(java.util.Locale.ROOT, it) },
            startThumbDescription = "Minimum rating",
            endThumbDescription = "Maximum rating",
            modifier = Modifier.padding(dimensionResource(R.dimen.padding_m)),
        )
    }
}

private const val RATING_MAX = 10f
private const val RATING_STEPS = 19
