package com.binge.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.integerResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import com.binge.designsystem.BACKDROP_ASPECT_RATIO
import com.binge.designsystem.R
import com.binge.designsystem.modifier.skeleton
import com.binge.designsystem.theme.BingeShapes

private const val SKELETON_META_WIDTH_FRACTION = 0.35f

/**
 * A season's heading: [title] at `headlineSmall` on the screen background, with [subtitle] (an episode
 * count, say) under it. It reads as the same kind of section heading the rest of an app uses rather than a
 * banded divider.
 *
 * @need head a season in a list of episodes
 * @group rows
 * @replaces a hand-built Column of a title and a count
 * @sample SeasonSectionsListSample
 */
@Composable
fun SeasonHeader(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                horizontal = dimensionResource(R.dimen.screen_content_inset),
                vertical = dimensionResource(R.dimen.padding_m),
            ),
    ) {
        Text(text = title, style = MaterialTheme.typography.headlineSmall)
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/** A row-shaped placeholder for an episode of a season still loading. It reserves the height of a full
 * [EpisodeBrowseRow] (the eyebrow, a two-line name and a full synopsis), so the list does not jump as a season loads. *
 * @need hold a loading season's place with episode-shaped placeholders
 * @group feedback
 * @replaces a hand-built shimmer plate for an episode row
 * @sample EpisodeBrowseRowSkeletonSample
 */
@Composable
fun EpisodeBrowseRowSkeleton(modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = dimensionResource(R.dimen.screen_content_inset),
                    vertical = dimensionResource(R.dimen.detail_meta_spacing),
                ),
            verticalAlignment = Alignment.Top,
        ) {
            Box(
                modifier = Modifier
                    .width(dimensionResource(R.dimen.episode_browse_row_still_width))
                    .aspectRatio(BACKDROP_ASPECT_RATIO)
                    .skeleton(visible = true, shape = MaterialTheme.shapes.small),
            )
            Spacer(Modifier.width(dimensionResource(R.dimen.padding_m)))
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.detail_cast_avatar_label_spacing)),
            ) {
                SkeletonLine(
                    style = MaterialTheme.typography.labelSmall,
                    widthFraction = SKELETON_META_WIDTH_FRACTION,
                    lines = 1,
                )
                SkeletonLine(
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    widthFraction = 1f,
                    lines = 2,
                )
                SkeletonLine(
                    style = MaterialTheme.typography.bodySmall,
                    widthFraction = 1f,
                    lines = integerResource(R.integer.episode_browse_row_overview_max_lines),
                )
            }
        }
        Spacer(Modifier.height(dimensionResource(R.dimen.detail_meta_spacing)))
    }
}

/**
 * A season whose episodes failed to load: its own [message] and, when [onRetry] is set, a text button
 * labelled [retryLabel], under its header and leaving the other seasons untouched.
 *
 * @need show that one season failed to load, with its own retry
 * @group feedback
 * @replaces a hand-built message and retry button under a season header
 * @sample SeasonSectionsListFailedSample
 */
@Composable
fun SeasonFailedRow(
    message: String,
    retryLabel: String,
    modifier: Modifier = Modifier,
    onRetry: (() -> Unit)? = null,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                horizontal = dimensionResource(R.dimen.screen_content_inset),
                vertical = dimensionResource(R.dimen.detail_meta_spacing),
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f),
        )
        if (onRetry != null) {
            TextButton(onClick = onRetry) { Text(retryLabel) }
        }
    }
}

/** A plate as tall as [lines] lines of [style], so a loading row reserves the loaded row's text block. */
@Composable
private fun SkeletonLine(
    style: TextStyle,
    widthFraction: Float,
    lines: Int,
) {
    Text(
        text = " ",
        style = style,
        minLines = lines,
        maxLines = lines,
        modifier = Modifier
            .fillMaxWidth(widthFraction)
            .skeleton(visible = true, shape = BingeShapes.Tag),
    )
}
