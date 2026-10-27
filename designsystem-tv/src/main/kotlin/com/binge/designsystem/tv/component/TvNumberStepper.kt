package com.binge.designsystem.tv.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.setProgress
import androidx.compose.ui.semantics.stateDescription
import androidx.tv.material3.Icon
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.binge.designsystem.DISABLED_ALPHA
import com.binge.designsystem.theme.BingeShapes
import com.binge.designsystem.tv.focus.tvEndDirectionKey
import com.binge.designsystem.tv.focus.tvFocusIndicator
import com.binge.designsystem.tv.focus.tvFocusTarget
import com.binge.designsystem.tv.focus.tvStartDirectionKey
import com.binge.designsystem.R as DesR
import com.binge.designsystem.tv.R as TvR

/**
 * A whole number a D-pad can set: **one focus stop** whose ← and → step [value] by [step] within [range], between two
 * chevrons that dim at either end. The number reads as [valueLabel], which the screen words ("5 requests",
 * "Unlimited") and which is announced as the control's state. Like [TvRatingStepper], a press past either end is
 * consumed and does nothing, direction follows the layout, OK commits nothing, and the control only reports
 * [onValueChange]. [step] must be positive. [initiallyFocused] seeds the lit state for a frame; production passes false.
 */
@Composable
internal fun TvNumberStepper(
    value: Int,
    onValueChange: (Int) -> Unit,
    range: IntRange,
    valueLabel: String,
    modifier: Modifier = Modifier,
    step: Int = 1,
    initiallyFocused: Boolean = false,
) {
    require(step > 0) { "step must be positive, was $step" }
    var focused by remember { mutableStateOf(initiallyFocused) }
    val lowerKey = tvStartDirectionKey()
    val raiseKey = tvEndDirectionKey()
    val current = value.coerceIn(range)
    Box(modifier = modifier) {
        Row(
            modifier =
                Modifier
                    .tvFocusIndicator(isFocused = focused, shape = BingeShapes.Pill)
                    .onPreviewKeyEvent { event ->
                        val delta =
                            when {
                                event.type != KeyEventType.KeyDown -> null
                                event.key == lowerKey -> -step
                                event.key == raiseKey -> step
                                else -> null
                            }
                        if (delta == null) {
                            false
                        } else {
                            val next = (current + delta).coerceIn(range)
                            if (next != current) onValueChange(next)
                            true
                        }
                    }.tvFocusTarget { focused = it }
                    .padding(
                        horizontal = dimensionResource(DesR.dimen.padding_s),
                        vertical = dimensionResource(DesR.dimen.padding_s),
                    ).semantics(mergeDescendants = true) {
                        stateDescription = valueLabel
                        progressBarRangeInfo =
                            ProgressBarRangeInfo(
                                current.toFloat(),
                                range.first.toFloat()..range.last.toFloat(),
                                steps = ((range.last - range.first) / step - 1).coerceAtLeast(0),
                            )
                        setProgress { target ->
                            val next = target.toInt().coerceIn(range)
                            if (next != current) onValueChange(next)
                            true
                        }
                    },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(dimensionResource(DesR.dimen.padding_s)),
        ) {
            Chevron(Icons.AutoMirrored.Filled.KeyboardArrowLeft, enabled = current > range.first)
            Text(
                text = valueLabel,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                softWrap = false,
                modifier = Modifier.clearAndSetSemantics {},
            )
            Chevron(Icons.AutoMirrored.Filled.KeyboardArrowRight, enabled = current < range.last)
        }
    }
}

/** One end's chevron: full strength while a press that way would step, dimmed once the value sits at that end. */
@Composable
private fun Chevron(icon: ImageVector, enabled: Boolean) {
    Icon(
        imageVector = icon,
        contentDescription = null,
        tint =
            if (enabled) {
                MaterialTheme.colorScheme.onSurface
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = DISABLED_ALPHA)
            },
        modifier = Modifier.size(dimensionResource(TvR.dimen.tv_number_stepper_chevron_size)),
    )
}
