package com.binge.designsystem.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import com.binge.designsystem.R
import com.binge.designsystem.theme.BingeExpressiveTheme
import com.binge.designsystem.theme.BingeShapes

@Composable
fun DiscoverQuickClearChip(
    label: String,
    tone: QuickClearTone,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = BingeShapes.Pill
    val errorTint = MaterialTheme.colorScheme.error.copy(alpha = 0.14f)
    val errorOutline = MaterialTheme.colorScheme.error.copy(alpha = 0.35f)
    val errorClose = MaterialTheme.colorScheme.error.copy(alpha = 0.25f)
    val backgroundColor = when (tone) {
        // primary, to match the Filters button this chip sits beside.
        QuickClearTone.Include -> MaterialTheme.colorScheme.primary
        QuickClearTone.Exclude -> errorTint
    }
    val contentColor = when (tone) {
        QuickClearTone.Include -> MaterialTheme.colorScheme.onPrimary
        QuickClearTone.Exclude -> MaterialTheme.colorScheme.error
    }
    val borderColor = when (tone) {
        QuickClearTone.Include -> Color.Transparent
        QuickClearTone.Exclude -> errorOutline
    }
    val closeBackground = when (tone) {
        QuickClearTone.Include -> MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f)
        QuickClearTone.Exclude -> errorClose
    }

    Row(
        modifier = modifier
            .clip(shape)
            .background(backgroundColor)
            .border(BorderStroke(dimensionResource(R.dimen.hairline_thickness), borderColor), shape)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(
                start = dimensionResource(R.dimen.discover_quickclear_chip_padding_start),
                end = dimensionResource(R.dimen.discover_quickclear_chip_padding_end),
                top = dimensionResource(R.dimen.discover_quickclear_chip_padding_v),
                bottom = dimensionResource(R.dimen.discover_quickclear_chip_padding_v),
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.discover_quickclear_chip_gap)),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = contentColor,
        )
        Box(
            modifier = Modifier
                .size(dimensionResource(R.dimen.discover_quickclear_chip_close_size))
                .clip(CircleShape)
                .background(closeBackground),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Filled.Close,
                contentDescription = stringResource(R.string.cd_remove_filter),
                tint = contentColor,
                modifier = Modifier.size(dimensionResource(R.dimen.discover_quickclear_chip_close_icon)),
            )
        }
    }
}

/**
 * [DiscoverQuickClearChip] under its old name for the tap, `onClear`; it is `onClick` now, as on every other chip.
 * Kotlin cannot hold two overloads that differ only in parameter names, so this one takes `onClear` ahead of `tone`.
 * Call it by name, as the old signature was.
 */
@Deprecated(
    message = "Renamed: onClear is onClick.",
    replaceWith = ReplaceWith("DiscoverQuickClearChip(label, tone, onClick = onClear, modifier = modifier)"),
)
@Suppress("ModifierParameter")
@Composable
fun DiscoverQuickClearChip(
    label: String,
    onClear: () -> Unit,
    tone: QuickClearTone,
    modifier: Modifier = Modifier,
) = DiscoverQuickClearChip(label = label, tone = tone, onClick = onClear, modifier = modifier)

@Preview(showBackground = true)
@Composable
private fun PreviewDiscoverQuickClearChipInclude() {
    BingeExpressiveTheme {
        DiscoverQuickClearChip(label = "Action", tone = QuickClearTone.Include, onClick = {})
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewDiscoverQuickClearChipExclude() {
    BingeExpressiveTheme {
        DiscoverQuickClearChip(label = "Horror", tone = QuickClearTone.Exclude, onClick = {})
    }
}
