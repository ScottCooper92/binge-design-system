package com.binge.designsystem.tv.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.tv.material3.Icon
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.binge.designsystem.theme.BingeShapes
import com.binge.designsystem.R as DesR
import com.binge.designsystem.tv.R as TvR

/** What a [TvPosterCell] says about the title beyond its art; draws nothing for a cell with no marks. */
@Composable
internal fun TvPosterMarks(poster: TvPosterCell) {
    val hasMarks = poster.watched || poster.unwatchedCount > 0 || poster.favourite || poster.progress != null
    if (!hasMarks) return
    val inset = dimensionResource(DesR.dimen.padding_xs)
    Box(Modifier.fillMaxSize()) {
        if (poster.watched) {
            TvWatchedMark(
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(inset),
            )
        } else if (poster.unwatchedCount > 0) {
            val label = pluralStringResource(DesR.plurals.media_card_unwatched_count, poster.unwatchedCount, poster.unwatchedCount)
            Text(
                text = poster.unwatchedCount.toString(),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(inset)
                    .clip(BingeShapes.MediaCard)
                    .background(MaterialTheme.colorScheme.primary)
                    .padding(horizontal = dimensionResource(DesR.dimen.padding_s), vertical = dimensionResource(DesR.dimen.padding_xxs))
                    .semantics { contentDescription = label },
            )
        }
        if (poster.favourite) {
            Icon(
                imageVector = Icons.Filled.Favorite,
                contentDescription = stringResource(DesR.string.media_card_favourite),
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(inset)
                    .size(dimensionResource(TvR.dimen.tv_poster_mark_size)),
            )
        }
        poster.progress?.let { fraction -> TvProgressBar(fraction, Modifier.align(Alignment.BottomStart)) }
    }
}

/** The tick that says a title is watched, announced by the phone's own words; [tint] is the caller's, so it reads on the surface it sits on. */
@Composable
internal fun TvWatchedMark(
    tint: Color,
    modifier: Modifier = Modifier,
    size: Dp = dimensionResource(TvR.dimen.tv_poster_mark_size),
) {
    Icon(
        imageVector = Icons.Filled.CheckCircle,
        contentDescription = stringResource(DesR.string.media_card_watched),
        tint = tint,
        modifier = modifier.size(size),
    )
}

/** The tag [TvProgressBar]'s track carries, so a test can find it. */
internal const val TV_PROGRESS_BAR_TAG = "tv-progress-bar"

/**
 * A bar along the foot of a card, [fraction] of the way across (clamped to 0..1), for how far through a title the viewer is.
 *
 * It sits inside the card by `tv_progress_bar_inset` on its start, end and bottom, whether the card is focused or not, so the focus ring, which is
 * drawn over the card's own edge, never covers it and the bar does not move when focus arrives. The inset is the ring's width plus room for the card's
 * rounded corner, which the ring follows: a bar flush with the foot would run into the ring's curve at both ends.
 */
@Composable
internal fun TvProgressBar(fraction: Float, modifier: Modifier = Modifier) {
    val inset = dimensionResource(TvR.dimen.tv_progress_bar_inset)
    Box(
        modifier
            .padding(start = inset, end = inset, bottom = inset)
            .fillMaxWidth()
            .height(dimensionResource(TvR.dimen.tv_poster_progress_height))
            .testTag(TV_PROGRESS_BAR_TAG)
            .background(MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Box(
            Modifier
                .fillMaxWidth(fraction.coerceIn(0f, 1f))
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.primary),
        )
    }
}
