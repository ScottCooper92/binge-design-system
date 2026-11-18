package com.binge.designsystem.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.integerResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import coil3.compose.SubcomposeAsyncImage
import com.binge.designsystem.BACKDROP_ASPECT_RATIO
import com.binge.designsystem.R

/**
 * One episode of a season as a flat row: a still with its rating chip on it, then an eyebrow ("E7 · 73 min"),
 * the name over two lines and the synopsis over three.
 *
 * The eyebrow is the number and the runtime when there is one, else the air date when there is one.
 * A null [onClick] makes the row display-only: the same layout, with no click semantics and no ripple.
 */
@Composable
fun EpisodeBrowseRow(
    episode: EpisodeBrowseUi,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
                .padding(
                    horizontal = dimensionResource(R.dimen.screen_content_inset),
                    vertical = dimensionResource(R.dimen.detail_meta_spacing),
                ),
            verticalAlignment = Alignment.Top,
        ) {
            EpisodeBrowseStill(episode = episode)
            Spacer(Modifier.width(dimensionResource(R.dimen.padding_m)))
            EpisodeBrowseMetadata(episode = episode, modifier = Modifier.weight(1f))
        }
        Spacer(Modifier.height(dimensionResource(R.dimen.detail_meta_spacing)))
    }
}

@Composable
private fun EpisodeBrowseStill(episode: EpisodeBrowseUi) {
    Box(
        modifier = Modifier
            .width(dimensionResource(R.dimen.episode_browse_row_still_width))
            .aspectRatio(BACKDROP_ASPECT_RATIO)
            .clip(MaterialTheme.shapes.small),
    ) {
        SubcomposeAsyncImage(
            model = episode.stillUrl,
            contentDescription = episode.name,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
            loading = { ImagePlaceholder(Modifier.fillMaxSize()) },
            error = { ImagePlaceholder(Modifier.fillMaxSize()) },
        )
        val rating = episode.rating
        if (rating != null && rating > 0f) {
            RatingChip(
                rating = rating,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(dimensionResource(R.dimen.card_rating_padding)),
            )
        }
    }
}

@Composable
private fun EpisodeBrowseMetadata(episode: EpisodeBrowseUi, modifier: Modifier = Modifier) {
    val number = stringResource(R.string.detail_episode_label, episode.number)
    val detail = episode.runtimeMinutes?.let { stringResource(R.string.detail_runtime_minutes, it) }
        ?: episode.airDate?.takeIf { it.isNotBlank() }
    val eyebrow = if (detail != null) {
        "$number ${stringResource(R.string.list_row_meta_separator)} $detail"
    } else {
        number
    }
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.detail_cast_avatar_label_spacing)),
    ) {
        Text(
            text = eyebrow,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = episode.name,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        val overview = episode.overview
        if (!overview.isNullOrBlank()) {
            Text(
                text = overview,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = integerResource(R.integer.episode_browse_row_overview_max_lines),
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
