package com.binge.designsystem.catalog

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

/** The episode row's loading placeholder — shimmering still, number, and meta lines. */
@Composable
fun EpisodeListRowSkeletonSample() {
    ScreenshotTheme {
        EpisodeListRowSkeleton()
    }
}
