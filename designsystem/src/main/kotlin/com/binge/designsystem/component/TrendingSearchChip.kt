package com.binge.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.tooling.preview.Preview
import com.binge.designsystem.R
import com.binge.designsystem.theme.BingeExpressiveTheme
import com.binge.designsystem.theme.BingeShapes

/**
 * A tappable search-suggestion pill with one leading mark: its [rank] in a ranked list, or failing that
 * [leadingIcon].
 *
 * The two share a slot and are mutually exclusive, [rank] winning. That is what lets one row mix a
 * trending chart with the user's own history and stay readable — a numbered pill is trending, a glyphed
 * one is theirs — without a second chip type or a legend.
 */
@Composable
fun TrendingSearchChip(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    rank: Int? = null,
    leadingIcon: ImageVector? = null,
) {
    Row(
        modifier = modifier
            .clip(BingeShapes.Pill)
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .clickable(onClick = onClick)
            .padding(
                horizontal = dimensionResource(R.dimen.trending_pill_padding_h),
                vertical = dimensionResource(R.dimen.trending_pill_padding_v),
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        when {
            rank != null -> {
                Text(
                    text = "%02d".format(rank),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                )
                Spacer(Modifier.width(dimensionResource(R.dimen.padding_s)))
            }
            leadingIcon != null -> {
                Icon(
                    imageVector = leadingIcon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(dimensionResource(R.dimen.trending_pill_icon)),
                )
                Spacer(Modifier.width(dimensionResource(R.dimen.padding_s)))
            }
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewTrendingSearchChip() {
    BingeExpressiveTheme {
        TrendingSearchChip(rank = 1, label = "Severance", onClick = {})
    }
}
