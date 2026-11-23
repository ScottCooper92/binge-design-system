package com.binge.designsystem.catalog

import androidx.compose.runtime.Composable
import com.binge.designsystem.component.EpisodeBrowseRow
import com.binge.designsystem.component.EpisodeBrowseRowSkeleton
import com.binge.designsystem.component.EpisodeBrowseUi
import com.binge.designsystem.preview.ScreenshotTheme

/**
 * Public samples for [EpisodeBrowseRow] and its skeleton. See the convention KDoc on [MediaCardRatedSample].
 */
@Composable
fun EpisodeBrowseRowSample() {
    ScreenshotTheme {
        EpisodeBrowseRow(episode = sampleEpisode(7), onClick = {})
    }
}

/** An episode with no runtime or rating: the eyebrow falls back to the air date and the still carries no chip. */
@Composable
fun EpisodeBrowseRowAirDateSample() {
    ScreenshotTheme {
        EpisodeBrowseRow(
            episode = EpisodeBrowseUi(
                number = 8,
                name = "Garden of Bones",
                overview = "A short synopsis.",
                airDate = "12 Mar 2023",
            ),
        )
    }
}

/** The loading placeholder of one episode row. */
@Composable
fun EpisodeBrowseRowSkeletonSample() {
    ScreenshotTheme {
        EpisodeBrowseRowSkeleton()
    }
}
