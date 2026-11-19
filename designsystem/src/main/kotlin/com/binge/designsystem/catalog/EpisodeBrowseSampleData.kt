package com.binge.designsystem.catalog

import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import com.binge.designsystem.component.EpisodeBrowseUi
import com.binge.designsystem.component.SeasonSectionState
import com.binge.designsystem.component.SeasonSectionUi
import com.binge.designsystem.component.SeasonSectionsList
import com.binge.designsystem.preview.ScreenshotTheme

// Shared data for the season-list samples; it lists nothing itself.
@Composable
internal fun SeasonList(sections: List<SeasonSectionUi>) {
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

internal fun loadedSeason(
    number: Int,
    title: String,
    subtitle: String,
    airDate: String,
) = SeasonSectionUi(
    key = number.toString(),
    title = title,
    subtitle = subtitle,
    state = SeasonSectionState.Loaded(
        listOf(
            sampleEpisode(1),
            sampleEpisode(2).copy(runtimeMinutes = null, airDate = airDate, rating = null),
            sampleEpisode(3).copy(overview = null, stillUrl = null),
        ),
    ),
)

internal fun sampleEpisode(number: Int) =
    EpisodeBrowseUi(
        number = number,
        name = "The North Remembers and the Long Night Begins",
        overview = "A long synopsis that wraps over several lines so the row shows how it ellipsizes after the third " +
            "line, however much of it the data source returns for the episode.",
        rating = 8.7f,
        runtimeMinutes = 73,
    )
