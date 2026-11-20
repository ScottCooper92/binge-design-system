package com.binge.designsystem.tv.template

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import coil3.compose.AsyncImage
import com.binge.designsystem.theme.BingeTheme
import com.binge.designsystem.tv.component.TvExpandingMeta
import com.binge.designsystem.tv.layout.tvLayoutAnchor
import com.binge.designsystem.R as DesR
import com.binge.designsystem.tv.R as TvR

private const val SYNOPSIS_LINES = 3

/** Logo (or the name), meta, and the synopsis held to three lines. */
@Composable
internal fun TvDetailCopy(title: TvDetailTitle, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.width(dimensionResource(TvR.dimen.tv_detail_copy_width)),
        verticalArrangement = Arrangement.spacedBy(dimensionResource(DesR.dimen.padding_s)),
    ) {
        TvDetailLogo(title)
        TvDetailMeta(title, Modifier.fillMaxWidth().tvLayoutAnchor(TvDetailAnchors.META))
        Text(
            text = title.overview,
            style = MaterialTheme.typography.bodyLarge,
            color = BingeTheme.colors.onScrim,
            maxLines = SYNOPSIS_LINES,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

/** The title's meta line, as the hub rows draw theirs. */
@Composable
internal fun TvDetailMeta(title: TvDetailTitle, modifier: Modifier = Modifier) {
    TvExpandingMeta(
        genres = title.genres,
        trailingFacts = title.facts,
        rating = title.rating,
        certification = title.certification,
        modifier = modifier,
    )
}

/** The title logo, or the name in its place when the title has none; [compact] is the small form a crowded tab uses. */
@Composable
internal fun TvDetailLogo(
    title: TvDetailTitle,
    modifier: Modifier = Modifier,
    compact: Boolean = false,
) {
    val logo = title.logoUrl
    if (logo != null) {
        val width = dimensionResource(if (compact) TvR.dimen.tv_detail_logo_compact_width else TvR.dimen.tv_detail_logo_width)
        val height = dimensionResource(if (compact) TvR.dimen.tv_detail_logo_compact_height else TvR.dimen.tv_detail_logo_height)
        AsyncImage(
            model = logo,
            contentDescription = title.title,
            contentScale = ContentScale.Fit,
            alignment = Alignment.BottomStart,
            modifier = modifier.size(width, height),
        )
    } else {
        Text(
            text = title.title,
            style = if (compact) MaterialTheme.typography.headlineMedium else MaterialTheme.typography.displayMedium,
            color = BingeTheme.colors.onScrim,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = modifier,
        )
    }
}
