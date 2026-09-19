package com.binge.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.binge.designsystem.R
import com.binge.designsystem.resolvedContentInset
import com.binge.designsystem.theme.BingeExpressiveTheme

@Composable
fun DetailStatRow(stats: List<DetailStat>, modifier: Modifier = Modifier) {
    if (stats.isEmpty()) return

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = resolvedContentInset())
            .height(IntrinsicSize.Min),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        stats.forEachIndexed { index, stat ->
            if (index > 0) {
                VerticalDivider(
                    modifier = Modifier
                        .height(dimensionResource(R.dimen.detail_stat_divider_height))
                        .padding(vertical = dimensionResource(R.dimen.detail_meta_spacing)),
                )
            }
            StatCell(stat = stat, modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun StatCell(stat: DetailStat, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(vertical = dimensionResource(R.dimen.detail_meta_spacing)),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.detail_cast_avatar_label_spacing)),
    ) {
        Icon(
            imageVector = stat.icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(dimensionResource(R.dimen.detail_stat_icon_size)),
        )
        Text(text = stat.value, style = MaterialTheme.typography.titleSmall, textAlign = TextAlign.Center)
        Text(
            text = stat.label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewDetailStatRow() {
    BingeExpressiveTheme {
        DetailStatRow(
            stats = listOf(
                DetailStat(Icons.Filled.Star, "8.5", "Rating"),
                DetailStat(Icons.Filled.ThumbUp, "12.4K", "Votes"),
            ),
        )
    }
}
