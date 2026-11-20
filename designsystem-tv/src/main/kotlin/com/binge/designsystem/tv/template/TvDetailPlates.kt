package com.binge.designsystem.tv.template

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.dimensionResource
import androidx.tv.material3.MaterialTheme
import com.binge.designsystem.theme.BingeShapes
import com.binge.designsystem.tv.component.TvSkeletonMetaLine
import com.binge.designsystem.tv.component.TvSkeletonParagraph
import com.binge.designsystem.tv.layout.tvLayoutAnchor
import com.binge.designsystem.R as DesR
import com.binge.designsystem.tv.R as TvR

private const val META_LINE_FRACTION = 0.7f
private const val SYNOPSIS_LAST_LINE_FRACTION = 0.6f
private const val SYNOPSIS_LINES = 3

/** The logo block, meta line and synopsis as plates, stacked as [TvDetailCopy] stacks them; what the page draws while its title loads. */
@Composable
internal fun TvDetailCopyPlates() {
    Column(
        modifier = Modifier.width(dimensionResource(TvR.dimen.tv_detail_v2_copy_width)),
        verticalArrangement = Arrangement.spacedBy(dimensionResource(DesR.dimen.padding_s)),
    ) {
        Box(
            Modifier
                .size(dimensionResource(TvR.dimen.tv_detail_v2_logo_width), dimensionResource(TvR.dimen.tv_detail_v2_logo_height))
                .clip(BingeShapes.Tag)
                .background(MaterialTheme.colorScheme.surfaceVariant),
        )
        TvSkeletonMetaLine(META_LINE_FRACTION, Modifier.fillMaxWidth().tvLayoutAnchor(TvDetailAnchors.META))
        TvSkeletonParagraph(MaterialTheme.typography.bodyLarge, SYNOPSIS_LINES, SYNOPSIS_LAST_LINE_FRACTION)
    }
}

/**
 * One icon action's plate, as `TvMediaDetailActionRow` draws it while the page waits: a square at the buttons' height, with small
 * corners compact or round otherwise, matching `TvCompactIconButton` and `TvIconButton`. Not focusable; the real button replaces it.
 */
@Composable
internal fun TvDetailIconPlate(compact: Boolean, modifier: Modifier = Modifier) {
    val height = dimensionResource(if (compact) TvR.dimen.tv_compact_button_height else TvR.dimen.tv_button_height)
    Box(
        modifier
            .size(height)
            .clip(if (compact) MaterialTheme.shapes.small else CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant),
    )
}
