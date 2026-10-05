package com.binge.designsystem.component

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.RangeSliderState
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import kotlin.math.abs

/**
 * A two-thumb range slider with the two chosen values written above the track, for entering a
 * from/to pair. It knows nothing of what the numbers mean: the caller supplies the [valueRange],
 * the number of [steps] between the ends, and [valueLabel] to turn a value into text.
 *
 * Each thumb is announced as [startThumbDescription] or [endThumbDescription] with the formatted
 * value as its state, so a screen reader hears "Minimum runtime, 90 min" rather than a bare
 * percentage. [onValuesChange] fires on every drag tick, so a caller that requeries on change
 * should edit a draft and apply it separately.
 *
 * A value the grid does not contain (a stored 6.3 on a half-point track) is kept when the other thumb is
 * dragged: Material 3 snaps the untouched thumb to the nearest step, so a thumb that moved by less than half
 * a step is reported at its old value.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BingeRangeSlider(
    values: ClosedFloatingPointRange<Float>,
    onValuesChange: (ClosedFloatingPointRange<Float>) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    steps: Int,
    valueLabel: (Float) -> String,
    startThumbDescription: String,
    endThumbDescription: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val startInteraction = remember { MutableInteractionSource() }
    val endInteraction = remember { MutableInteractionSource() }
    val halfStep = (valueRange.endInclusive - valueRange.start) / (steps + 1) / 2f
    val startLabel = valueLabel(values.start)
    val endLabel = valueLabel(values.endInclusive)
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(text = startLabel, style = MaterialTheme.typography.labelLarge)
            Text(text = endLabel, style = MaterialTheme.typography.labelLarge)
        }
        // Material keeps the slider's state; this component stays controlled, so the state is written from
        // [values] on every composition and a drag only reports through onValueChange.
        val state = remember(steps, valueRange) {
            RangeSliderState(values.start, values.endInclusive, steps, valueRange)
        }
        state.syncTo(values)
        RangeSlider(
            state = state,
            onValueChange = { next ->
                onValuesChange(if (steps == 0) next else next.keepingUntouchedThumbs(values, halfStep))
            },
            enabled = enabled,
            startThumbInteractionSource = startInteraction,
            endThumbInteractionSource = endInteraction,
            startThumb = {
                SliderDefaults.Thumb(
                    interactionSource = startInteraction,
                    enabled = enabled,
                    modifier = Modifier.semantics {
                        contentDescription = startThumbDescription
                        stateDescription = startLabel
                    },
                )
            },
            endThumb = {
                SliderDefaults.Thumb(
                    interactionSource = endInteraction,
                    enabled = enabled,
                    modifier = Modifier.semantics {
                        contentDescription = endThumbDescription
                        stateDescription = endLabel
                    },
                )
            },
        )
    }
}

/**
 * Writes [values] into the state. Each setter clamps against the other thumb, so the thumb moving
 * away from the other goes first: a range that jumps past the current one is not clamped on the way.
 */
private fun RangeSliderState.syncTo(values: ClosedFloatingPointRange<Float>) {
    if (values.start > endValue) {
        endValue = values.endInclusive
        startValue = values.start
    } else {
        startValue = values.start
        endValue = values.endInclusive
    }
}

/** [next] with each thumb that moved by less than [halfStep] put back at its value in [old], and the start never past the end. */
private fun ClosedFloatingPointRange<Float>.keepingUntouchedThumbs(
    old: ClosedFloatingPointRange<Float>,
    halfStep: Float,
): ClosedFloatingPointRange<Float> {
    val start = if (abs(start - old.start) < halfStep) old.start else start
    val end = if (abs(endInclusive - old.endInclusive) < halfStep) old.endInclusive else endInclusive
    return start..maxOf(start, end)
}
