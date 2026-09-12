package com.binge.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
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
import com.binge.designsystem.formatRating
import com.binge.designsystem.theme.BingeExpressiveTheme
import com.binge.designsystem.theme.BingeTheme

/**
 * A TV episode as a full-width [ListRow]: a backdrop still leading an eyebrow-led meta column.
 * Episodes are a distinct, denser row archetype than the poster-leading rows — the "EP NN" overline
 * plus a tight [titleSmall] title is deliberate — so it keeps its own header rather than the shared
 * [ListRowHeader], while still reusing `ListRow`'s surface/leading/content chrome.
 */
@Composable
fun EpisodeListRow(
    episodeNumber: Int,
    name: String,
    stillUrl: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    rating: Float = 0f,
    airDate: String? = null,
) {
    ListRow(
        modifier = modifier,
        onClick = onClick,
        leading = { EpisodeStill(stillUrl = stillUrl, contentDescription = name) },
    ) { contentModifier ->
        EpisodeRowMeta(
            episodeNumber = episodeNumber,
            name = name,
            rating = rating,
            airDate = airDate,
            modifier = contentModifier,
        )
    }
}

@Composable
private fun EpisodeStill(stillUrl: String?, contentDescription: String?) {
    ListRowPoster(
        imageUrl = stillUrl,
        contentDescription = contentDescription,
        width = dimensionResource(R.dimen.episode_row_still_width),
        aspectRatio = BACKDROP_ASPECT_RATIO,
    )
}

@Composable
private fun EpisodeRowMeta(
    episodeNumber: Int,
    name: String,
    rating: Float,
    airDate: String?,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Text(
            text = stringResource(R.string.episode_row_overline, episodeNumber),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary,
        )
        Spacer(Modifier.size(dimensionResource(R.dimen.detail_cast_avatar_label_spacing)))
        Text(
            text = name,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        EpisodeRowSubtitle(rating = rating, airDate = airDate)
    }
}

/** The optional rating · air-date line; renders nothing when neither is present. */
@Composable
private fun EpisodeRowSubtitle(rating: Float, airDate: String?) {
    val hasRating = rating > 0f
    val date = airDate?.takeIf { it.isNotBlank() }
    if (!hasRating && date == null) return
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
                text = rating.formatRating(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        if (date != null) {
            if (hasRating) {
                Text(
                    text = stringResource(R.string.list_row_meta_separator),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(
                text = date,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
fun EpisodeListRowSkeleton(modifier: Modifier = Modifier) {
    ListRow(
        modifier = modifier,
        leading = { EpisodeStill(stillUrl = null, contentDescription = null) },
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
