package com.binge.designsystem.catalog

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
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
    ScreenshotTheme {
        BingeRangeSlider(
            values = values,
            onValuesChange = {},
            valueRange = 0f..SAMPLE_MAX,
            steps = SAMPLE_STEPS,
            valueLabel = { if (it >= SAMPLE_MAX) "${it.toInt()}+ min" else "${it.toInt()} min" },
            startThumbDescription = "Minimum",
            endThumbDescription = "Maximum",
            enabled = enabled,
            modifier = Modifier.padding(dimensionResource(R.dimen.padding_m)),
        )
    }
}

/** Both thumbs parked at the ends: nothing is constrained. */
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
