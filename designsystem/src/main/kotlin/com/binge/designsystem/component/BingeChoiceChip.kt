package com.binge.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.semantics.Role
import com.binge.designsystem.R
import com.binge.designsystem.theme.BingeShapes

/**
 * One choice among a few, as a chip: outlined while off, filled with the primary colour and a check mark once [selected]. A
 * [icon] leads the label while it is off and the check takes its place once it is on, as on Material's filter chip. It reads
 * as a radio button to a screen reader, so a group of them is one single choice.
 *
 * @need pick one option from a short list in a form or a sheet
 * @group cards
 * @replaces a hand-built outlined chip that fills and gains a check when picked
 */
@Composable
fun BingeChoiceChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
) {
    val shape = BingeShapes.Chip
    val scheme = MaterialTheme.colorScheme
    Row(
        modifier =
            modifier
                .clip(shape)
                .background(if (selected) scheme.primary else Color.Transparent)
                .border(
                    width = dimensionResource(R.dimen.hairline_thickness),
                    color = if (selected) Color.Transparent else scheme.outline,
                    shape = shape,
                ).selectable(selected = selected, role = Role.RadioButton, onClick = onClick)
                .padding(
                    horizontal = dimensionResource(R.dimen.discover_filter_chip_padding_h),
                    vertical = dimensionResource(R.dimen.discover_filter_chip_padding_v),
                ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.discover_filter_chip_gap)),
    ) {
        val leading = if (selected) Icons.Filled.Check else icon
        if (leading != null) {
            Icon(
                imageVector = leading,
                contentDescription = null,
                tint = if (selected) scheme.onPrimary else scheme.onSurfaceVariant,
                modifier = Modifier.size(dimensionResource(R.dimen.discover_filter_chip_icon)),
            )
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = if (selected) scheme.onPrimary else scheme.onSurface,
        )
    }
}
