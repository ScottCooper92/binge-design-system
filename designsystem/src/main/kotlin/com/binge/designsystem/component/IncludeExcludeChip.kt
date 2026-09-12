package com.binge.designsystem.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
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
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import com.binge.designsystem.R
import com.binge.designsystem.theme.BingeExpressiveTheme
import com.binge.designsystem.theme.BingeShapes

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun IncludeExcludeChip(
    label: String,
    state: IncludeExcludeState,
    onTap: () -> Unit,
    onLongPress: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = BingeShapes.Chip
    val errorTint = MaterialTheme.colorScheme.error.copy(alpha = 0.14f)
    val errorOutline = MaterialTheme.colorScheme.error.copy(alpha = 0.4f)
    val stateLabel = when (state) {
        IncludeExcludeState.Include -> stringResource(R.string.cd_filter_state_included)
        IncludeExcludeState.Exclude -> stringResource(R.string.cd_filter_state_excluded)
        IncludeExcludeState.Neutral -> stringResource(R.string.cd_filter_state_neutral)
    }
    val chipColors = when (state) {
        IncludeExcludeState.Include ->
            ChipColors(
                background = MaterialTheme.colorScheme.secondaryContainer,
                border = Color.Transparent,
                content = MaterialTheme.colorScheme.onSecondaryContainer,
                decoration = TextDecoration.None,
            )
        IncludeExcludeState.Exclude ->
            ChipColors(
                background = errorTint,
                border = errorOutline,
                content = MaterialTheme.colorScheme.error,
                decoration = TextDecoration.LineThrough,
            )
        IncludeExcludeState.Neutral ->
            ChipColors(
                background = Color.Transparent,
                border = MaterialTheme.colorScheme.outline,
                content = MaterialTheme.colorScheme.onSurface,
                decoration = TextDecoration.None,
            )
    }

    Row(
        modifier = modifier
            .clip(shape)
            .background(chipColors.background)
            .border(BorderStroke(dimensionResource(R.dimen.hairline_thickness), chipColors.border), shape)
            .combinedClickable(onClick = onTap, onLongClick = onLongPress)
            .semantics { stateDescription = stateLabel }
            .padding(
                horizontal = dimensionResource(R.dimen.discover_filter_chip_padding_h),
                vertical = dimensionResource(R.dimen.discover_filter_chip_padding_v),
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.discover_filter_chip_gap)),
    ) {
        when (state) {
            IncludeExcludeState.Include ->
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = null,
                    tint = chipColors.content,
                    modifier = Modifier.size(dimensionResource(R.dimen.discover_filter_chip_icon)),
                )
            IncludeExcludeState.Exclude ->
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = null,
                    tint = chipColors.content,
                    modifier = Modifier.size(dimensionResource(R.dimen.discover_filter_chip_icon)),
                )
            IncludeExcludeState.Neutral -> Unit
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge.copy(textDecoration = chipColors.decoration),
            color = chipColors.content,
        )
    }
}

private data class ChipColors(
    val background: Color,
    val border: Color,
    val content: Color,
    val decoration: TextDecoration,
)

@Preview(showBackground = true)
@Composable
private fun PreviewIncludeExcludeChipNeutral() {
    BingeExpressiveTheme {
        IncludeExcludeChip(label = "Action", state = IncludeExcludeState.Neutral, onTap = {}, onLongPress = {})
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewIncludeExcludeChipInclude() {
    BingeExpressiveTheme {
        IncludeExcludeChip(label = "Action", state = IncludeExcludeState.Include, onTap = {}, onLongPress = {})
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewIncludeExcludeChipExclude() {
    BingeExpressiveTheme {
        IncludeExcludeChip(label = "Horror", state = IncludeExcludeState.Exclude, onTap = {}, onLongPress = {})
    }
}
