package com.binge.designsystem.component

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription

/**
 * A two-thumb range slider with the two chosen values written above the track, for entering a
 * from/to pair. It knows nothing of what the numbers mean: the caller supplies the [valueRange],
 * the number of [steps] between the ends, and [valueLabel] to turn a value into text.
 *
 * Each thumb is announced as [startThumbDescription] or [endThumbDescription] with the formatted
 * value as its state, so a screen reader hears "Minimum runtime, 90 min" rather than a bare
 * percentage. [onValuesChange] fires on every drag tick, so a caller that requeries on change
 * should edit a draft and apply it separately.
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
        RangeSlider(
            value = values,
            onValueChange = onValuesChange,
            valueRange = valueRange,
            steps = steps,
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
