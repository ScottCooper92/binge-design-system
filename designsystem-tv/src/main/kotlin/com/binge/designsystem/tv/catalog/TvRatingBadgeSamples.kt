@file:SelfDescribing

package com.binge.designsystem.tv.catalog

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.dimensionResource
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.binge.designsystem.catalog.SelfDescribing
import com.binge.designsystem.theme.BingeShapes
import com.binge.designsystem.tv.component.TvMetaLine
import com.binge.designsystem.tv.component.TvRatingBadge
import com.binge.designsystem.tv.component.TvRatingBadgeSize
import com.binge.designsystem.tv.component.TvRowEmphasis
import com.binge.designsystem.tv.component.TvSkeletonLine
import com.binge.designsystem.tv.component.containerColor
import com.binge.designsystem.tv.component.contentColor
import com.binge.designsystem.R as DesR

@Composable
private fun SampleColumn(content: @Composable () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(dimensionResource(DesR.dimen.padding_l)),
        verticalArrangement = Arrangement.spacedBy(dimensionResource(DesR.dimen.padding_m)),
    ) {
        content()
    }
}

/** Both sizes, and an unrated title: an unrated zero draws nothing rather than a `0.0` badge. */
@Composable
fun TvRatingBadgeSample() {
    SampleColumn {
        Row(
            horizontalArrangement = Arrangement.spacedBy(dimensionResource(DesR.dimen.padding_l)),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TvRatingBadge(rating = 8.6f)
            TvRatingBadge(rating = 8.6f, size = TvRatingBadgeSize.Card)
            TvRatingBadge(rating = 0f)
            Text(text = "unrated draws nothing", style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun AmberRow(emphasis: TvRowEmphasis) {
    Row(
        modifier = Modifier
            .clip(BingeShapes.TvListItem)
            .background(emphasis.containerColor(resting = MaterialTheme.colorScheme.surface))
            .padding(dimensionResource(DesR.dimen.padding_m)),
        horizontalArrangement = Arrangement.spacedBy(dimensionResource(DesR.dimen.padding_m)),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "E1 · The Pilot",
            style = MaterialTheme.typography.titleMedium,
            color = emphasis.contentColor(resting = MaterialTheme.colorScheme.onSurface),
        )
        TvRatingBadge(rating = 8.6f, size = TvRatingBadgeSize.Card, emphasis = emphasis)
    }
}

/** The badge inverted on a focused row's fill and on the current row's amber fill: both must still read as a rating, not a hole in the row. */
@Composable
fun TvRatingBadgeOnAmberRowsSample() {
    SampleColumn {
        AmberRow(TvRowEmphasis.Focused)
        AmberRow(TvRowEmphasis.Current)
    }
}

/** The badge leading a real metadata line, then the same line with no rating. */
@Composable
fun TvMetaLineSample() {
    SampleColumn {
        TvMetaLine(
            rating = 8.6f,
            text = "2026  ·  2h 14m  ·  Action  ·  Thriller",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        TvMetaLine(
            rating = null,
            text = "2026  ·  2h 14m  ·  Action  ·  Thriller",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/** Lines of copy as loading plates, each the height of the text line it stands for. */
@Composable
fun TvSkeletonLineSample() {
    SampleColumn {
        TvSkeletonLine(style = MaterialTheme.typography.displaySmall, widthFraction = 0.5f)
        TvSkeletonLine(style = MaterialTheme.typography.titleMedium, widthFraction = 0.35f)
        TvSkeletonLine(style = MaterialTheme.typography.bodyMedium, widthFraction = 0.6f)
    }
}
