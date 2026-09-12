package com.binge.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import com.binge.designsystem.R
import com.binge.designsystem.badgeCountLabel
import com.binge.designsystem.theme.BingeExpressiveTheme
import com.binge.designsystem.theme.BingeShapes

/** Darkens the count sub-pill on a selected (primary-filled) chip so it reads against the accent. */
private const val FILTER_CHIP_COUNT_SCRIM_ALPHA = 0.22f

/**
 * The app's filter chip: a pill that fills with the primary accent when [selected] and is outlined
 * otherwise. An optional [leadingIcon] (vector) or [leadingPainter] (drawable, e.g. a brand glyph)
 * sits before the [label], and an optional [count] trails it as a tonal sub-pill (e.g. a filter's
 * result total). Used standalone and as the cell of [BingeFilterChipRow].
 */
@Composable
fun BingeFilterChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = null,
    leadingPainter: Painter? = null,
    count: Int? = null,
) {
    val scheme = MaterialTheme.colorScheme
    val foreground = if (selected) scheme.onPrimary else scheme.onSurfaceVariant
    val isSelected = selected
    Row(
        modifier = modifier
            .clip(BingeShapes.Pill)
            .background(if (selected) scheme.primary else scheme.surfaceContainerLow)
            .then(
                if (selected) {
                    Modifier
                } else {
                    Modifier.border(dimensionResource(R.dimen.hairline_thickness), scheme.outlineVariant, BingeShapes.Pill)
                },
            ).clickable(onClick = onClick, role = Role.Button)
            .semantics { this.selected = isSelected }
            .padding(
                horizontal = dimensionResource(R.dimen.chip_padding_h),
                vertical = dimensionResource(R.dimen.chip_padding_v),
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (leadingIcon != null) {
            Icon(
                imageVector = leadingIcon,
                contentDescription = null,
                tint = foreground,
                modifier = Modifier.size(dimensionResource(R.dimen.icon_size_m)),
            )
            Spacer(Modifier.width(dimensionResource(R.dimen.padding_s)))
        } else if (leadingPainter != null) {
            Icon(
                painter = leadingPainter,
                contentDescription = null,
                tint = foreground,
                modifier = Modifier.size(dimensionResource(R.dimen.icon_size_m)),
            )
            Spacer(Modifier.width(dimensionResource(R.dimen.padding_s)))
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = foreground,
        )
        if (count != null) {
            Spacer(Modifier.width(dimensionResource(R.dimen.padding_s)))
            Text(
                text = badgeCountLabel(count),
                style = MaterialTheme.typography.labelSmall,
                color = foreground,
                modifier = Modifier
                    .clip(BingeShapes.Pill)
                    .background(
                        if (selected) scheme.scrim.copy(alpha = FILTER_CHIP_COUNT_SCRIM_ALPHA) else scheme.surfaceContainerHigh,
                    ).padding(horizontal = dimensionResource(R.dimen.padding_xs)),
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewBingeFilterChipSelected() {
    BingeExpressiveTheme {
        BingeFilterChip(label = "All", selected = true, onClick = {}, count = 142)
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewBingeFilterChipUnselected() {
    BingeExpressiveTheme {
        BingeFilterChip(label = "Action", selected = false, onClick = {})
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewBingeFilterChipWithLeadingIcon() {
    BingeExpressiveTheme {
        BingeFilterChip(
            label = "Trending",
            selected = false,
            onClick = {},
            leadingIcon = Icons.Filled.Star,
        )
    }
}

@Preview(showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun PreviewBingeFilterChipDark() {
    BingeExpressiveTheme {
        BingeFilterChip(label = "Pending", selected = true, onClick = {}, count = 3)
    }
}
