package com.binge.designsystem.catalog

import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.binge.designsystem.component.EpisodeBrowseRow
import com.binge.designsystem.component.EpisodeBrowseRowSkeleton
import com.binge.designsystem.component.EpisodeBrowseUi
import com.binge.designsystem.component.SeasonSectionState
import com.binge.designsystem.component.SeasonSectionUi
import com.binge.designsystem.component.SeasonSectionsList
import com.binge.designsystem.preview.ScreenshotTheme

/**
 * Public samples for the season-grouped episode list: [EpisodeBrowseRow], its skeleton, and
 * [SeasonSectionsList] with a loaded, a loading and a failed season. See the convention KDoc on
 * [MediaCardRatedSample].
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
                airDate = "Mar 12, 2023",
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

/** Season 1 loaded, season 2 loading its place with skeleton rows. */
@Composable
fun SeasonSectionsListSample() {
    SeasonList(
        listOf(
            loadedSeason(1, "Season 1", "3 episodes"),
            SeasonSectionUi("2", "Season 2", "10 episodes", SeasonSectionState.Loading(rowCount = 4)),
        ),
    )
}

/** Every season still loading: skeleton rows under each header. */
@Composable
fun SeasonSectionsListLoadingSample() {
    SeasonList(
        listOf(
            SeasonSectionUi("1", "Season 1", "10 episodes", SeasonSectionState.Loading(rowCount = 4)),
            SeasonSectionUi("2", "Season 2", "10 episodes", SeasonSectionState.Loading(rowCount = 4)),
        ),
    )
}

/** Season 2 failed: its message and a Retry button under its header, season 1 untouched. Retry sends it back to loading. */
@Composable
fun SeasonSectionsListFailedSample() {
    var failed by remember { mutableStateOf(true) }
    SeasonList(
        listOf(
            loadedSeason(1, "Season 1", "3 episodes"),
            SeasonSectionUi(
                key = "2",
                title = "Season 2",
                subtitle = "10 episodes",
                state = if (failed) {
                    SeasonSectionState.Failed(
                        message = "Couldn't load this season.",
                        retryLabel = "Retry",
                        onRetry = { failed = false },
                    )
                } else {
                    SeasonSectionState.Loading(rowCount = 4)
                },
            ),
        ),
    )
}

/** The loaded list in Spanish: the copy is the consumer's, the "E7" and "min" come from the design system. */
@Composable
fun SeasonSectionsListSpanishSample() {
    SeasonList(
        listOf(
            loadedSeason(1, "Temporada 1", "3 episodios"),
            SeasonSectionUi(
                key = "2",
                title = "Temporada 2",
                subtitle = "10 episodios",
                state = SeasonSectionState.Failed(
                    message = "No se ha podido cargar esta temporada.",
                    retryLabel = "Reintentar",
                    onRetry = {},
                ),
            ),
        ),
    )
}

@Composable
private fun SeasonList(sections: List<SeasonSectionUi>) {
    ScreenshotTheme {
        SeasonSectionsList(
            sections = sections,
            listState = rememberLazyListState(),
            openAt = sections.first().key,
            onSeasonNeeded = {},
            onEpisodeClick = { _, _ -> },
        )
    }
}

private fun loadedSeason(
    number: Int,
    title: String,
    subtitle: String,
) = SeasonSectionUi(
    key = number.toString(),
    title = title,
    subtitle = subtitle,
    state = SeasonSectionState.Loaded(
        listOf(
            sampleEpisode(1),
            sampleEpisode(2).copy(runtimeMinutes = null, airDate = "Mar 5, 2023", rating = null),
            sampleEpisode(3).copy(overview = null, stillUrl = null),
        ),
    ),
)

private fun sampleEpisode(number: Int) =
    EpisodeBrowseUi(
        number = number,
        name = "The North Remembers and the Long Night Begins",
        overview = "A long synopsis that wraps over several lines so the row shows how it ellipsizes after the third " +
            "line, however much of it the data source returns for the episode.",
        rating = 8.7f,
        runtimeMinutes = 73,
    )
