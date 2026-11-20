package com.binge.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import com.binge.designsystem.BACKDROP_ASPECT_RATIO
import com.binge.designsystem.R
import com.binge.designsystem.component.ListRow
import com.binge.designsystem.component.ListRowPoster
import com.binge.designsystem.formatRatingForPage
import com.binge.designsystem.modifier.describedAs
import com.binge.designsystem.theme.BingeExpressiveTheme
import com.binge.designsystem.theme.BingeTheme

/**
 * A TV episode as a full-width [ListRow]: a backdrop still leading an eyebrow-led meta column.
 * Episodes are a distinct, denser row archetype than the poster-leading rows — the "EP NN" overline
 * plus a tight [titleSmall] title is deliberate — so it keeps its own header rather than the shared
 * [ListRowHeader], while still reusing `ListRow`'s surface/leading/content chrome.
 *
 * A null [onClick] makes the row display-only: the layout is the same, but it is not a button, so it has no click
 * semantics, no ripple and no focus target. For an app that lists episodes to be read, not opened.
 *
 * [progress], from 0 to 1, draws a thin bar along the foot of the row for an episode part way through; null draws none. [trailingContent] is
 * a slot at the end of the row, for a mark or a toggle the caller owns (a watched mark, say); with none the row is as it was. A null
 * [episodeNumber], a special that has none, draws the row without its "EP" overline. [supporting] is one more short fact (a runtime) at the end of
 * the rating and date line, worded by the caller.
 *
 * The still has no content description: it sits beside the episode's name, so a screen reader would say the name twice.
 *
 * @need show an episode in a list with its still and details
 * @group rows
 * @replaces a hand-built row with an image and a text column
 */
@Composable
fun EpisodeListRow(
    episodeNumber: Int?,
    name: String,
    stillUrl: String?,
    onClick: (() -> Unit)?,
    modifier: Modifier = Modifier,
    rating: Float = 0f,
    airDate: String? = null,
    progress: Float? = null,
    trailingContent: (@Composable () -> Unit)? = null,
    supporting: String? = null,
) {
    ListRow(
        modifier = modifier,
        onClick = onClick,
        leading = { EpisodeStill(stillUrl = stillUrl) },
        trailingContent = trailingContent,
        footer = progress?.let { fraction -> { EpisodeProgress(fraction) } },
    ) { contentModifier ->
        EpisodeRowMeta(
            episodeNumber = episodeNumber,
            name = name,
            rating = rating,
            airDate = airDate,
            supporting = supporting,
            modifier = contentModifier,
        )
    }
}

/** The thin bar along the foot of a row for an episode that is part way through. */
@Composable
private fun EpisodeProgress(fraction: Float) {
    LinearProgressIndicator(
        progress = { fraction.coerceIn(0f, 1f) },
        modifier = Modifier.fillMaxWidth().height(dimensionResource(R.dimen.episode_row_progress_height)),
        color = MaterialTheme.colorScheme.primary,
        trackColor = MaterialTheme.colorScheme.surfaceVariant,
        drawStopIndicator = {},
    )
}

/** The still is decorative beside the episode's name, which the row's text already carries, so it has no description of its own. */
@Composable
private fun EpisodeStill(stillUrl: String?) {
    ListRowPoster(
        imageUrl = stillUrl,
        contentDescription = null,
        width = dimensionResource(R.dimen.episode_row_still_width),
        aspectRatio = BACKDROP_ASPECT_RATIO,
    )
}

@Composable
private fun EpisodeRowMeta(
    episodeNumber: Int?,
    name: String,
    rating: Float,
    airDate: String?,
    supporting: String?,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        if (episodeNumber != null) {
            Text(
                text = stringResource(R.string.episode_row_overline, episodeNumber),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
            )
            Spacer(Modifier.size(dimensionResource(R.dimen.detail_cast_avatar_label_spacing)))
        }
        Text(
            text = name,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        EpisodeRowSubtitle(rating = rating, airDate = airDate, supporting = supporting)
    }
}

/** The optional rating · air-date · supporting line; renders nothing when none is present. */
@Composable
private fun EpisodeRowSubtitle(
    rating: Float,
    airDate: String?,
    supporting: String?,
) {
    val hasRating = rating > 0f
    val date = airDate?.takeIf { it.isNotBlank() }
    val extra = supporting?.takeIf { it.isNotBlank() }
    if (!hasRating && date == null && extra == null) return
    Spacer(Modifier.size(dimensionResource(R.dimen.detail_cast_avatar_label_spacing)))
    Row(
        horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.detail_cast_avatar_label_spacing)),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (hasRating) {
            Icon(
                imageVector = Icons.Filled.Star,
                contentDescription = null,
                tint = BingeTheme.colors.ratingStar,
                modifier = Modifier.size(dimensionResource(R.dimen.rating_chip_star_size)),
            )
            Text(
                text = rating.formatRatingForPage(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                softWrap = false,
            )
        }
        // One line, never wrapped: the row is measured at its minimum intrinsic height, which would clip a wrapped line.
        // The pieces are measured in order, so the supporting text, last, is the one that gives way to an ellipsis.
        val lastIndex = listOfNotNull(date, extra).lastIndex
        listOfNotNull(date, extra).forEachIndexed { index, text ->
            if (hasRating || index > 0) {
                Text(
                    text = stringResource(R.string.list_row_meta_separator),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    softWrap = false,
                )
            }
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Ellipsis,
                modifier = if (index == lastIndex && index > 0) Modifier.weight(1f, fill = false) else Modifier,
            )
        }
    }
}

/**
 * Loading placeholder for an [EpisodeListRow]: the still plate and the row's two text lines, blank.
 * [description] names the loading state for a screen reader; without one it stays silent.
 *
 * @need show a loading placeholder for an episode row
 * @group feedback
 */
@Composable
fun EpisodeListRowSkeleton(modifier: Modifier = Modifier, description: String? = null) {
    ListRow(
        modifier = modifier.describedAs(description),
        leading = { EpisodeStill(stillUrl = null) },
    ) { contentModifier ->
        Column(modifier = contentModifier) {
            Text(text = " ", style = MaterialTheme.typography.labelSmall)
            Text(
                text = " ",
                style = MaterialTheme.typography.titleSmall,
                minLines = 2,
                maxLines = 2,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewEpisodeListRow() {
    BingeExpressiveTheme {
        EpisodeListRow(
            episodeNumber = 3,
            name = "The North Remembers",
            stillUrl = null,
            rating = 8.7f,
            airDate = "Mar 5, 2023",
            onClick = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewEpisodeListRowNoMetadata() {
    BingeExpressiveTheme {
        EpisodeListRow(
            episodeNumber = 1,
            name = "Pilot",
            stillUrl = null,
            onClick = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewEpisodeListRowSkeleton() {
    BingeExpressiveTheme {
        EpisodeListRowSkeleton()
    }
}
