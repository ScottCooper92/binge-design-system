@file:OnePerScreen(fullScreen = true)
@file:CatalogGroup("Detail pages")

package com.binge.designsystem.catalog

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.binge.designsystem.component.BingeStatusButton
import com.binge.designsystem.component.BingeStatusButtonEmphasis
import com.binge.designsystem.component.EpisodeRow
import com.binge.designsystem.component.SeasonRow
import com.binge.designsystem.preview.ScreenshotTheme
import com.binge.designsystem.template.DetailState
import com.binge.designsystem.template.EpisodesState
import com.binge.designsystem.template.MediaDetail
import com.binge.designsystem.template.MediaDetailCallbacks
import com.binge.designsystem.template.SeriesDetail
import com.binge.designsystem.template.SeriesDetailCallbacks
import com.binge.designsystem.template.SeriesDetailScreen
import com.binge.designsystem.theme.BingeSentiment

private val seasons = listOf(
    SeasonRow(1, "Season 1", "7 episodes · 2008"),
    SeasonRow(2, "Season 2", "13 episodes · 2009"),
    SeasonRow(3, "Season 3", "13 episodes · 2010"),
)

private val episodeNames =
    listOf(
        "Pilot",
        "Cat's in the Bag",
        "And the Bag's in the River",
        "Cancer Man",
        "Gray Matter",
        "Crazy Handful of Nothin'",
        "A No-Rough-Stuff-Type Deal",
    )

private fun episodes() =
    episodeNames.mapIndexed { index, name ->
        EpisodeRow(
            number = index + 1,
            name = name,
            stillUrl = null,
            rating = 8.5f - index * 0.1f,
            airDate = "Jan ${20 + index}, 2008",
            played = index == 0,
            playedLabel = "Watched",
            progress = if (index == 1) 0.4f else null,
        )
    }

/** A lean series page, so the episodes section sits in the frame: the title, the stats and the synopsis, then the episodes. */
private fun series() =
    MediaDetail(title = MEDIA_PAGE_TITLE, genres = MEDIA_PAGE_GENRES, stats = MediaPageStats, overview = MEDIA_PAGE_OVERVIEW)

@Composable
private fun SeriesPage(
    episodes: EpisodesState,
    seasonCount: Int = seasons.size,
    shown: Int? = null,
) {
    var selected by remember { mutableIntStateOf(1) }
    ScreenshotTheme(modifier = Modifier.fillMaxSize()) {
        SeriesDetailScreen(
            state = DetailState.Content(series()),
            series = SeriesDetail(
                seasons = seasons.take(seasonCount),
                selectedSeason = selected,
                episodes = episodes,
                episodesTitle = "Episodes",
                seasonSubtitle = seasons.first { it.number == selected }.let { "${it.label} · ${it.subtitle}" },
                viewAllLabel = "View all episodes",
                shown = shown,
            ),
            callbacks = MediaDetailCallbacks(onBack = LocalDemoBack.current),
            seriesCallbacks = SeriesDetailCallbacks(onSelectSeason = { selected = it }),
            primary = { modifier ->
                BingeStatusButton(
                    label = "Play S1 · E1",
                    onClick = {},
                    sentiment = BingeSentiment.Neutral,
                    modifier = modifier,
                    icon = Icons.Filled.PlayArrow,
                    emphasis = BingeStatusButtonEmphasis.Filled,
                )
            },
        )
    }
}

/**
 * A series' page assembled by the template: everything a movie's page has, and the episodes section with the season dropdown, the season's
 * line, the first five episodes (the first marked as watched, the second part way through) and a button for the other two. In the catalog the
 * dropdown opens the season picker as a sheet over the page, and choosing a season moves the subtitle.
 */
@Composable
fun SeriesDetailScreenSample() {
    SeriesPage(EpisodesState.Ready(episodes()))
}

/** The same page while a season's episodes load: the header and the subtitle stay, and placeholder rows stand in for the episodes. */
@Composable
fun SeriesDetailScreenEpisodesLoadingSample() {
    SeriesPage(EpisodesState.Loading)
}

/** A season whose episodes did not load: the app's own message and Try again, under the same header. */
@Composable
fun SeriesDetailScreenEpisodesFailedSample() {
    SeriesPage(EpisodesState.Failed("The episodes could not be read."))
}

/** A show with a single season has nothing to choose, so its Episodes header carries no dropdown. */
@Composable
fun SeriesDetailScreenSingleSeasonSample() {
    SeriesPage(EpisodesState.Ready(episodes()), seasonCount = 1)
}

/** A season that lists every episode, as an app's own season page does: all seven rows and no View all button. */
@Composable
fun SeriesDetailScreenAllEpisodesSample() {
    SeriesPage(EpisodesState.Ready(episodes()), shown = episodes().size)
}
