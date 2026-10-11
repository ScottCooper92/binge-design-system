package com.binge.designsystem.tv.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.StarHalf
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.binge.designsystem.theme.BingeShapes
import com.binge.designsystem.tv.focus.tvEndDirectionKey
import com.binge.designsystem.tv.focus.tvFocusIndicator
import com.binge.designsystem.tv.focus.tvFocusTarget
import com.binge.designsystem.tv.focus.tvStartDirectionKey
import com.binge.designsystem.R as DesR
import com.binge.designsystem.tv.R as TvR

/**
 * A rating a D-pad can set: **one focus stop** whose ← and → step the value by one, with a five-star read-out
 * that follows every step.
 *
 * The stars are display only. Five focusable stars would make ← → mean "move" rather than "adjust", and OK on
 * a star would be the phone's tap smuggled onto a remote. Here the stepper is the whole control: [value] runs
 * from 0 (unrated) to 10, two points to a star, so an odd value draws a half star. At either end the press
 * is consumed and does nothing, so focus does not escape the control past the last step. Direction follows the
 * layout, so → raises the value under LTR and ← under RTL.
 *
 * OK does not commit anything: the control only reports [onValueChange], and the screen puts its own explicit
 * action beside it. [valueLabel] is the number as the screen words it ("7 / 10"), shown beside the stars and
 * announced as the control's state. [initiallyFocused] seeds the lit state for a frame; production passes false.
 */
@Composable
fun TvRatingStepper(
    value: Int,
    onValueChange: (Int) -> Unit,
    valueLabel: String,
    modifier: Modifier = Modifier,
    initiallyFocused: Boolean = false,
) {
    var focused by remember { mutableStateOf(initiallyFocused) }
    val lowerKey = tvStartDirectionKey()
    val raiseKey = tvEndDirectionKey()
    val current = value.coerceIn(0, MAX)
    Box(modifier = modifier) {
        Row(
            modifier =
                Modifier
                    .tvFocusIndicator(isFocused = focused, shape = BingeShapes.Pill)
                    .onPreviewKeyEvent { event ->
                        val step =
                            when {
                                event.type != KeyEventType.KeyDown -> null
                                event.key == lowerKey -> -1
                                event.key == raiseKey -> 1
                                else -> null
                            }
                        if (step == null) {
                            false
                        } else {
                            val next = (current + step).coerceIn(0, MAX)
                            if (next != current) onValueChange(next)
                            true
                        }
                    }.tvFocusTarget { focused = it }
                    .padding(
                        horizontal = dimensionResource(DesR.dimen.padding_m),
                        vertical = dimensionResource(DesR.dimen.padding_s),
                    ).semantics(mergeDescendants = true) {
                        stateDescription = valueLabel
                        progressBarRangeInfo = ProgressBarRangeInfo(current.toFloat(), 0f..MAX.toFloat(), steps = MAX - 1)
                        setProgress { target ->
                            val next = target.toInt().coerceIn(0, MAX)
                            if (next != current) onValueChange(next)
                            true
                        }
                    },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(dimensionResource(DesR.dimen.padding_m)),
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(dimensionResource(TvR.dimen.tv_rating_stepper_star_gap))) {
                repeat(STARS) { index -> StarGlyph(points = (current - index * POINTS_PER_STAR).coerceIn(0, POINTS_PER_STAR)) }
            }
            Text(
                text = valueLabel,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                softWrap = false,
                modifier = Modifier.clearAndSetSemantics {},
            )
        }
    }
}

/** One star of the read-out: empty at 0 points, half at 1, full at 2. Filled stars take the accent at rest. */
@Composable
private fun StarGlyph(points: Int) {
    val filled = points > 0
    Icon(
        imageVector =
            when (points) {
                0 -> Icons.Filled.StarBorder
                1 -> Icons.AutoMirrored.Filled.StarHalf
                else -> Icons.Filled.Star
            },
        contentDescription = null,
        tint = if (filled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.size(dimensionResource(TvR.dimen.tv_rating_stepper_star_size)),
    )
}

private const val MAX = 10
private const val STARS = 5
private const val POINTS_PER_STAR = 2
