package com.binge.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AllInclusive
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.style.TextAlign
import com.binge.designsystem.R
import kotlin.math.roundToInt

/**
 * A whole number from [range] on a one-thumb slider, for a value where a list of a hundred choices is the wrong shape.
 * The value reads large above the track, the bounds sit under its ends, and − and + step it by one, since a hundred
 * steps across a phone's width are too fine to land on by dragging.
 *
 * [openEndLabel] gives the slider one stop past [range]'s last, which means "no limit" or "or more" and is [value]
 * `null`: drag to the end, or + from the top, and the readout says [openEndLabel], with ∞ marking that end of the track.
 * A consumer that stores "no limit" some other way (a 0, say) maps it to and from `null`. [onValueChange] fires on
 * every step, so a caller that saves as it changes should batch it.
 */
@Composable
fun BingeNumberSlider(
    value: Int?,
    onValueChange: (Int?) -> Unit,
    range: IntRange,
    format: (Int) -> String,
    description: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    openEndLabel: String? = null,
) {
    val open = openEndLabel != null
    // The slider's own scale: the range, and with an open end, one more stop past its last.
    val top = if (open) range.last + 1 else range.last
    val position = if (value == null && open) top else (value ?: range.first).coerceIn(range)
    val readout = if (position == top && open) openEndLabel.orEmpty() else format(position)
    val pick = { at: Int -> onValueChange(if (open && at == top) null else at.coerceIn(range)) }
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_s))) {
        Text(
            readout,
            style = MaterialTheme.typography.displaySmall,
            color = if (enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { pick(position - 1) }, enabled = enabled && position > range.first) {
                Icon(Icons.Filled.Remove, contentDescription = stringResource(R.string.number_slider_decrease, description))
            }
            Slider(
                value = position.toFloat(),
                onValueChange = { picked -> picked.roundToInt().let { if (it != position) pick(it) } },
                valueRange = range.first.toFloat()..top.toFloat(),
                steps = (top - range.first - 1).coerceAtLeast(0),
                enabled = enabled,
                // A hundred stops' tick marks blur into a dotted texture; the steppers are what land on one.
                colors =
                    SliderDefaults.colors(
                        activeTickColor = Color.Transparent,
                        inactiveTickColor = Color.Transparent,
                        disabledActiveTickColor = Color.Transparent,
                        disabledInactiveTickColor = Color.Transparent,
                    ),
                modifier =
                    Modifier.weight(1f).semantics {
                        contentDescription = description
                        stateDescription = readout
                    },
            )
            IconButton(onClick = { pick(position + 1) }, enabled = enabled && position < top) {
                Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.number_slider_increase, description))
            }
        }
        // The track's two ends named, inset by the steppers so each label sits under its end.
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = dimensionResource(R.dimen.min_touch_target)),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SliderBound(format(range.first), Modifier.weight(1f))
            if (open) {
                Icon(Icons.Filled.AllInclusive, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                SliderBound(format(range.last))
            }
        }
    }
}

@Composable
internal fun SliderBound(text: String, modifier: Modifier = Modifier) =
    Text(text, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = modifier)

/**
 * A number setting as a [ListItem] for an [ItemGroup]: its [title], and its value ([format] of it, or [openEndLabel]) as
 * the detail. A tap opens a sheet holding a [BingeNumberSlider] under the title; each change is handed to [onChange]
 * as it happens. A row that is not [enabled] dims, and closes its sheet if open.
 */
@Composable
fun bingeNumberItem(
    icon: ImageVector,
    title: String,
    value: Int?,
    range: IntRange,
    format: (Int) -> String,
    onChange: (Int?) -> Unit,
    enabled: Boolean = true,
    openEndLabel: String? = null,
): ListItem {
    var open by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(enabled) { if (!enabled) open = false }
    if (open) {
        BingeBottomSheet(onDismissRequest = { open = false }) {
            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = dimensionResource(R.dimen.padding_m))
                        .padding(bottom = dimensionResource(R.dimen.padding_l)),
                verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_m)),
            ) {
                Text(title, style = MaterialTheme.typography.titleMedium)
                BingeNumberSlider(
                    value = value,
                    onValueChange = onChange,
                    range = range,
                    format = format,
                    description = title,
                    openEndLabel = openEndLabel,
                )
            }
        }
    }
    return ListItem(
        icon = icon,
        label = title,
        detail = if (value == null && openEndLabel != null) openEndLabel else format((value ?: range.first).coerceIn(range)),
        clickable = enabled,
        disabled = !enabled,
        onClick = { open = true },
    )
}
