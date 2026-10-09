package com.binge.designsystem.component

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AllInclusive
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.RangeSliderState
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.style.TextAlign
import com.binge.designsystem.R
import kotlin.math.abs

/**
 * A two-thumb range slider for entering a from/to pair. The chosen range reads centred above the track
 * ("6.5 – 9.0"), and the track's two ends are named under it, the same layout as [BingeNumberSlider]. It
 * knows nothing of what the numbers mean: the caller supplies the [valueRange], the number of [steps]
 * between the ends, and [valueLabel] to turn a value into text.
 *
 * [openEndLabel] makes the track's top an open end, "or more": a thumb resting there reads [openEndLabel]
 * in place of [valueLabel], and ∞ marks that end of the track. What the open end means to a query (no upper
 * bound at all) is the caller's to decide.
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
    openEndLabel: String? = null,
) {
    val startInteraction = remember { MutableInteractionSource() }
    val endInteraction = remember { MutableInteractionSource() }
    val halfStep = (valueRange.endInclusive - valueRange.start) / (steps + 1) / 2f
    val label = { value: Float ->
        if (openEndLabel != null && value >= valueRange.endInclusive) openEndLabel else valueLabel(value)
    }
    val startLabel = label(values.start)
    val endLabel = label(values.endInclusive)
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = if (startLabel == endLabel) startLabel else stringResource(R.string.range_slider_readout, startLabel, endLabel),
            style = MaterialTheme.typography.titleLarge,
            color = if (enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
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
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            SliderBound(valueLabel(valueRange.start), Modifier.weight(1f))
            if (openEndLabel != null) {
                Icon(Icons.Filled.AllInclusive, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                SliderBound(valueLabel(valueRange.endInclusive))
            }
        }
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
