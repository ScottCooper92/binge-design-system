package com.binge.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.binge.designsystem.DISABLED_ALPHA
import com.binge.designsystem.R
import com.binge.designsystem.theme.BingeShapes

/**
 * A short row of segments in one pill, one of them picked: Any or All, OR or AND. The picked segment is filled with the primary
 * colour. Each segment reads as a radio button and the pill as one group, which [contentDescription] names, so say what the choice is
 * about ("Match any or all genres"), not just its values.
 *
 * It is as tall as its content and at least a touch target; pass a height in [modifier] to match a neighbour, such as the button
 * it sits beside in a sheet's footer. While not [enabled] it is dimmed and a tap does nothing. Two to four short labels fit; for a
 * choice among many, or among long labels, use [BingeChoiceChip]s or a choice sheet.
 *
 * @need pick one of two or three short values in a compact pill, such as whether picks join with OR or AND
 * @group cards
 * @replaces a hand-built pill of clickable boxes
 * @sample BingeSegmentedToggleSample
 */
@Composable
fun BingeSegmentedToggle(
    labels: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    contentDescription: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val shape = BingeShapes.Pill
    val scheme = MaterialTheme.colorScheme
    Row(
        modifier =
            modifier
                .heightIn(min = dimensionResource(R.dimen.min_touch_target))
                // Segments fill the pill's height, so the pill is as tall as its tallest label unless the caller sets a height.
                .height(IntrinsicSize.Min)
                .alpha(if (enabled) 1f else DISABLED_ALPHA)
                .clip(shape)
                .background(scheme.surfaceContainerHighest)
                .border(dimensionResource(R.dimen.hairline_thickness), scheme.outlineVariant, shape)
                .padding(dimensionResource(R.dimen.padding_xxs))
                .selectableGroup()
                .semantics { this.contentDescription = contentDescription },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        labels.forEachIndexed { index, label ->
            val selected = index == selectedIndex
            Box(
                contentAlignment = Alignment.Center,
                modifier =
                    Modifier
                        .fillMaxHeight()
                        .clip(shape)
                        .background(if (selected) scheme.primary else Color.Transparent)
                        .selectable(selected = selected, enabled = enabled, role = Role.RadioButton, onClick = { onSelect(index) })
                        .padding(horizontal = dimensionResource(R.dimen.padding_m)),
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    color = if (selected) scheme.onPrimary else scheme.onSurfaceVariant,
                )
            }
        }
    }
}
