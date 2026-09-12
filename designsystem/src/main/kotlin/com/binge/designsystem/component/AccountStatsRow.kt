package com.binge.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.binge.designsystem.R
import com.binge.designsystem.theme.BingeExpressiveTheme
import com.binge.designsystem.theme.BingeShapes

@Composable
fun AccountStatsRow(stats: List<AccountStat>, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.chip_spacing)),
    ) {
        stats.forEach { stat ->
            AccountStatTile(stat = stat, modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun AccountStatTile(stat: AccountStat, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(BingeShapes.Large)
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .padding(
                horizontal = dimensionResource(R.dimen.account_stat_padding_h),
                vertical = dimensionResource(R.dimen.account_stat_padding_v),
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stat.value,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(dimensionResource(R.dimen.padding_xxs)))
        Text(
            text = stat.label.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewAccountStatsRow() {
    BingeExpressiveTheme {
        AccountStatsRow(
            stats = listOf(
                AccountStat("42", "Watchlist"),
                AccountStat("186", "Watched"),
                AccountStat("23", "Reviews"),
            ),
        )
    }
}
