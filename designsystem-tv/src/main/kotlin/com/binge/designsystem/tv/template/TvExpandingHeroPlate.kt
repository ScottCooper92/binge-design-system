package com.binge.designsystem.tv.template

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.dimensionResource
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.binge.designsystem.theme.BingeShapes
import com.binge.designsystem.tv.component.ON_CARD_PLATE_ALPHA
import com.binge.designsystem.tv.component.TvSkeletonLine
import com.binge.designsystem.tv.component.TvSkeletonPlate
import com.binge.designsystem.R as DesR
import com.binge.designsystem.tv.R as TvR

private const val LOGO_WIDTH_FRACTION = 0.65f
private const val META_LINE_FRACTION = 0.7f
private const val SYNOPSIS_LAST_LINE_FRACTION = 0.6f
private const val SYNOPSIS_LINES = 3

/** What [TvExpandingHeroCard] shows before it has a title: the slide's copy bottom-left and the trending pill bottom-right as plates. */
@Composable
internal fun TvExpandingHeroPlate() {
    val inset = dimensionResource(DesR.dimen.padding_l)
    val plate = MaterialTheme.colorScheme.onSurface.copy(alpha = ON_CARD_PLATE_ALPHA)
    Box(Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(inset)
                .width(dimensionResource(TvR.dimen.tv_expanding_row_details_width)),
            verticalArrangement = Arrangement.spacedBy(dimensionResource(DesR.dimen.padding_s)),
        ) {
            TvSkeletonPlate(LOGO_WIDTH_FRACTION, dimensionResource(TvR.dimen.tv_expanding_hero_logo_height), color = plate)
            TvSkeletonLine(MaterialTheme.typography.titleMedium, META_LINE_FRACTION, color = plate)
            repeat(SYNOPSIS_LINES) { line ->
                val fraction = if (line == SYNOPSIS_LINES - 1) SYNOPSIS_LAST_LINE_FRACTION else 1f
                TvSkeletonLine(MaterialTheme.typography.bodyMedium, fraction, color = plate)
            }
        }
        Text(
            text = "",
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(inset)
                .width(dimensionResource(TvR.dimen.tv_detail_skeleton_action_width))
                .clip(BingeShapes.Pill)
                .background(plate)
                .padding(vertical = dimensionResource(TvR.dimen.tv_hero_pill_padding_vertical)),
        )
    }
}
