@file:CatalogGroup("Detail pages")

package com.binge.designsystem.catalog

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.binge.designsystem.component.DetailEpisodesSection
import com.binge.designsystem.component.EpisodeListRowSkeleton
import com.binge.designsystem.component.EpisodeRow
import com.binge.designsystem.preview.ScreenshotTheme

private val episodes = List(7) { index ->
    EpisodeRow(
        number = index + 1,
        name = listOf(
            "Pilot",
            "Cat's in the Bag",
            "And the Bag's in the River",
            "Cancer Man",
            "Gray Matter",
            "Crazy Handful of Nothin'",
            "A No-Rough-Stuff-Type Deal",
        )[index],
        stillUrl = null,
        rating = 8.5f - index * 0.1f,
        airDate = "Jan ${20 + index}, 2008",
    )
}

/**
 * A series page's episodes: the season dropdown in the header, the season's line under it, the first five episodes and a button for the other
 * two. Tapping the dropdown in the catalog moves to the next season's name; the button counts a tap.
 */
@Composable
fun DetailEpisodesSectionSample() {
    ScreenshotTheme {
        var season by remember { mutableIntStateOf(1) }
        var taps by remember { mutableIntStateOf(0) }
        DetailEpisodesSection(
            title = "Episodes",
            episodes = episodes,
            subtitle = "Season $season · 7 episodes · 2008",
            seasonLabel = "Season $season",
            onSeasonClick = { season = season % 3 + 1 },
            viewAllLabel = if (taps == 0) "View all episodes" else "View all episodes ($taps)",
            onViewAll = { taps++ },
        )
    }
}

/** The same section while the season loads: the caller's placeholder rows stand in for the episodes, under the same header. */
@Composable
fun DetailEpisodesSectionLoadingSample() {
    ScreenshotTheme {
        DetailEpisodesSection(
            title = "Episodes",
            episodes = emptyList(),
            subtitle = "Season 1 · 7 episodes · 2008",
            seasonLabel = "Season 1",
            onSeasonClick = {},
            stateContent = { repeat(2) { EpisodeListRowSkeleton() } },
        )
    }
}

/**
 * The episodes of a season that can be opened: each row has a key and a runtime after the date, and a special has no number, so its row has
 * no overline. Tapping a row in the catalog puts its key in the subtitle.
 */
@Composable
fun DetailEpisodesSectionOpenableSample() {
    ScreenshotTheme {
        var opened by remember { mutableStateOf<String?>(null) }
        DetailEpisodesSection(
            title = "Episodes",
            episodes = listOf(
                EpisodeRow(
                    null,
                    "Behind the scenes",
                    null,
                    rating = 7.4f,
                    airDate = "Mar 1, 2008",
                    key = "special-1",
                    supporting = "45 min",
                ),
                EpisodeRow(1, "Pilot", null, rating = 8.5f, airDate = "Jan 20, 2008", key = "ep-1", supporting = "58 min"),
                EpisodeRow(2, "Cat's in the Bag", null, rating = 8.4f, airDate = "Jan 27, 2008", key = "ep-2", supporting = "48 min"),
            ),
            subtitle = opened?.let { "Opened $it" } ?: "Season 1 · 7 episodes · 2008",
            onOpenEpisode = { opened = it },
        )
    }
}

/** A season short enough to list in full still ends in the view-all button when the app asks for it, as the way into the season's screen. */
@Composable
fun DetailEpisodesSectionViewAllAlwaysSample() {
    ScreenshotTheme {
        DetailEpisodesSection(
            title = "Episodes",
            episodes = listOf(
                EpisodeRow(1, "Pilot", null, rating = 8.5f, airDate = "Jan 20, 2008"),
                EpisodeRow(2, "Cat's in the Bag", null, rating = 8.4f, airDate = "Jan 27, 2008"),
            ),
            subtitle = "Season 1 · 2 episodes · 2008",
            viewAllLabel = "View all episodes",
            onViewAll = {},
            viewAllAlways = true,
        )
    }
}
