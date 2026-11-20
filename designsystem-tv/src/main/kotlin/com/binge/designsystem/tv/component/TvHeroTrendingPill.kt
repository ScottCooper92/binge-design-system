package com.binge.designsystem.tv.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.tv.material3.Icon
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.binge.designsystem.theme.BingeShapes
import com.binge.designsystem.R as DesR
import com.binge.designsystem.tv.R as TvR

/** Matches the phone hero's HERO_PILL_ALPHA. */
private const val TRENDING_PILL_ALPHA = 0.16f

/** The '#N trending today' rank badge, the phone hero's pill rebuilt on tv-material; the expanding hero wears it. */
@Composable
internal fun TvHeroTrendingPill(rank: Int) {
    Row(
        modifier = Modifier
            .clip(BingeShapes.Pill)
            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = TRENDING_PILL_ALPHA))
            .padding(
                horizontal = dimensionResource(TvR.dimen.tv_hero_pill_padding_horizontal),
                vertical = dimensionResource(TvR.dimen.tv_hero_pill_padding_vertical),
            ),
        horizontalArrangement = Arrangement.spacedBy(dimensionResource(DesR.dimen.padding_s)),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.Filled.LocalFireDepartment,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.size(dimensionResource(TvR.dimen.tv_hero_pill_icon)),
        )
        Text(
            text = stringResource(DesR.string.hero_trending_today, rank),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}
