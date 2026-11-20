package com.binge.designsystem.catalog

import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import com.binge.designsystem.component.EpisodeListRow
import com.binge.designsystem.component.EpisodeListRowSkeleton
import com.binge.designsystem.preview.ScreenshotTheme

/**
 * Public samples for [EpisodeListRow] — a `ListRow`-based episode item with a still, number, title,
 * rating and air date, plus its loading skeleton. See the convention KDoc on [MediaCardRatedSample].
 */
@Composable
fun EpisodeListRowSample() {
    ScreenshotTheme {
        EpisodeListRow(
            episodeNumber = 3,
            name = "The North Remembers and the Long Night Begins",
            stillUrl = null,
            rating = 8.7f,
            airDate = "Mar 5, 2023",
            onClick = {},
        )
    }
}

/** A display-only row, with no `onClick`: the same layout, but not a button, so no ripple or focus target. */
@Composable
fun EpisodeListRowDisplayOnlySample() {
    ScreenshotTheme {
        EpisodeListRow(
            episodeNumber = 4,
            name = "Garden of Bones",
            stillUrl = null,
            rating = 8.1f,
            airDate = "Mar 12, 2023",
            onClick = null,
        )
    }
}

/** The episode row's loading placeholder — shimmering still, number, and meta lines. */
@Composable
fun EpisodeListRowSkeletonSample() {
    ScreenshotTheme {
        EpisodeListRowSkeleton()
    }
}

/**
 * An episode part way through, with a bar along the foot of the row, and a mark at the end of the row the caller owns, here a check for a
 * watched episode. The row is display-only, so neither is a button.
 */
@Composable
fun EpisodeListRowMarksSample() {
    ScreenshotTheme {
        Column {
            EpisodeListRow(
                episodeNumber = 5,
                name = "Gray Matter",
                stillUrl = null,
                rating = 8.1f,
                airDate = "Mar 19, 2023",
                onClick = null,
                progress = 0.4f,
            )
            EpisodeListRow(
                episodeNumber = 6,
                name = "Crazy Handful of Nothin'",
                stillUrl = null,
                rating = 9.2f,
                airDate = "Mar 26, 2023",
                onClick = null,
                trailingContent = {
                    Icon(
                        Icons.Filled.CheckCircle,
                        contentDescription = "Watched",
                        tint = MaterialTheme.colorScheme.primary,
                    )
                },
            )
        }
    }
}
