package com.binge.designsystem.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.invisibleToUser
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.style.TextAlign
import com.binge.designsystem.R
import kotlin.math.roundToInt

/** How a [BingeStop] reads: [Warning] draws the track, thumb and the selected stop in the error colour. */
enum class BingeStopEmphasis { Normal, Warning }

/**
 * One labelled stop on a [BingeStopSlider]: its short [label], the [icon] above it, and how it reads when selected.
 * [contentDescription] is what a screen reader hears as the slider's state in place of [label], for a stop whose short name
 * drops part of its meaning ("CC BY" for "Public domain and CC BY"); null reads the [label].
 */
data class BingeStop(
    val label: String,
    val icon: ImageVector,
    val emphasis: BingeStopEmphasis = BingeStopEmphasis.Normal,
    val contentDescription: String? = null,
)

/**
 * A choice among a few ordered levels, as a stepped slider with a labelled stop under each tick: the stop's icon centred on
 * its tick and its short name under that, each of them a tap target. The thumb follows a drag locally and [onPositionChange]
 * reports where it is, so the caller can show what each level means as the thumb passes; [onCommit] fires **once**, when the
 * thumb lands or a stop is tapped, never per step. A level the caller must ask about before applying is the case it is for.
 *
 * [holdIndex] keeps the thumb on a stop while the caller asks a question about it (a confirm), and is null otherwise: until it
 * clears, the thumb stays there whatever [selectedIndex] is, and returns to [selectedIndex] when it does. Without a hold, the
 * caller must accept a commit by setting [selectedIndex] to it: a caller that declines and changes nothing leaves the thumb
 * on the stop it was dragged to. Tapping the stop already selected commits that index again. [description]
 * names the slider for a screen reader, which hears the shown stop's [BingeStop.contentDescription], else its label, as its state. Needs at least two [stops]. Keep a
 * stop's name to a short word or two: it wraps inside the room its stop owns, and at the largest font a single long word breaks
 * across lines there rather than clipping.
 */
@Composable
fun BingeStopSlider(
    stops: List<BingeStop>,
    selectedIndex: Int,
    onCommit: (Int) -> Unit,
    description: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    holdIndex: Int? = null,
    onPositionChange: (Int) -> Unit = {},
) {
    require(stops.size >= 2) { "A stop slider needs at least two stops" }
    val last = stops.lastIndex
    val shownIndex = (holdIndex ?: selectedIndex).coerceIn(0, last)
    var position by remember(shownIndex) { mutableFloatStateOf(shownIndex.toFloat()) }
    val current = position.roundToInt().coerceIn(0, last)
    val shown = stops[current]
    val accent = if (shown.emphasis == BingeStopEmphasis.Warning) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
    val move = { index: Int ->
        position = index.toFloat()
        onPositionChange(index)
    }
    Column(modifier = modifier.fillMaxWidth()) {
        Slider(
            value = position,
            onValueChange = { moved ->
                position = moved
                onPositionChange(moved.roundToInt().coerceIn(0, last))
            },
            onValueChangeFinished = { onCommit(position.roundToInt().coerceIn(0, last)) },
            enabled = enabled,
            valueRange = 0f..last.toFloat(),
            steps = stops.size - 2,
            colors =
                SliderDefaults.colors(
                    thumbColor = accent,
                    activeTrackColor = accent,
                    activeTickColor = MaterialTheme.colorScheme.contentColorFor(accent),
                    inactiveTrackColor = accent.copy(alpha = INACTIVE_TRACK_ALPHA),
                    inactiveTickColor = accent,
                ),
            modifier =
                Modifier.semantics {
                    contentDescription = description
                    stateDescription = shown.contentDescription ?: shown.label
                },
        )
        StopLabels(stops = stops, selectedIndex = current, accent = accent, enabled = enabled) { index ->
            move(index)
            onCommit(index)
        }
    }
}

/** The stops' icons and names: each icon centred on its tick, its name centred under it and held inside the row at the ends. */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun StopLabels(
    stops: List<BingeStop>,
    selectedIndex: Int,
    accent: Color,
    enabled: Boolean,
    onPick: (Int) -> Unit,
) {
    val inset = dimensionResource(R.dimen.stop_slider_inset)
    Layout(
        content = {
            stops.forEachIndexed { index, stop ->
                val base = if (index == selectedIndex) accent else MaterialTheme.colorScheme.onSurfaceVariant
                val tint = if (enabled) base else base.copy(alpha = DISABLED_LABEL_ALPHA)
                // The slider above announces the stop and is the one control for a screen reader, so the labels stay out of its way.
                val pick = Modifier.clickable(enabled = enabled) { onPick(index) }.semantics { invisibleToUser() }
                Icon(
                    stop.icon,
                    contentDescription = null,
                    tint = tint,
                    modifier =
                        pick
                            .padding(top = dimensionResource(R.dimen.padding_xs))
                            .size(dimensionResource(R.dimen.icon_size_l)),
                )
                Text(
                    text = stop.label,
                    style = MaterialTheme.typography.labelMedium,
                    color = tint,
                    textAlign = TextAlign.Center,
                    modifier = pick.padding(bottom = dimensionResource(R.dimen.padding_xs)),
                )
            }
        },
        modifier = Modifier.fillMaxWidth(),
    ) { measurables, constraints ->
        val width = constraints.maxWidth
        val inner = inset.roundToPx()
        val loose = constraints.copy(minWidth = 0, minHeight = 0)
        // `content` alternates an icon and its name, so even indexes are icons and odd ones are names.
        val count = measurables.size / 2
        val step = (width - 2 * inner).toFloat() / (count - 1)
        val icons = measurables.filterIndexed { i, _ -> i % 2 == 0 }.map { it.measure(loose) }
        // A name may use only the room its stop owns, so a long one wraps under its icon instead of running into
        // the next: half a step either side of its tick, and at the ends from the row's edge.
        val labels =
            measurables.filterIndexed { i, _ -> i % 2 == 1 }.mapIndexed { i, measurable ->
                val room = if (i == 0 || i == count - 1) inner + step / 2 else step
                measurable.measure(loose.copy(maxWidth = room.toInt().coerceAtMost(width)))
            }
        val iconHeight = icons.maxOf { it.height }
        layout(width, iconHeight + labels.maxOf { it.height }) {
            icons.forEachIndexed { i, icon ->
                val centre = inner + (step * i).toInt()
                icon.placeRelative(centre - icon.width / 2, 0)
                val label = labels[i]
                label.placeRelative((centre - label.width / 2).coerceIn(0, (width - label.width).coerceAtLeast(0)), iconHeight)
            }
        }
    }
}

/** The unfilled part of the track, a tint of the filled part as Material draws it. */
private const val INACTIVE_TRACK_ALPHA = 0.24f

/** The stop icons and names while the slider is disabled, dimmed as Material dims the track and thumb. */
private const val DISABLED_LABEL_ALPHA = 0.38f
