@file:SelfDescribing

package com.binge.designsystem.tv.catalog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import com.binge.designsystem.catalog.SelfDescribing
import com.binge.designsystem.tv.R
import com.binge.designsystem.tv.template.TvDetailEpisode
import com.binge.designsystem.tv.template.TvDetailEpisodeCard
import com.binge.designsystem.tv.template.TvDetailEpisodes
import com.binge.designsystem.tv.template.TvDetailSeason
import com.binge.designsystem.tv.template.TvDetailSeasonPill
import com.binge.designsystem.tv.template.TvDetailTitle

private val ShowTitle = TvDetailTitle(
    title = "The Long Way Home",
    logoUrl = null,
    backdropUrl = null,
    overview = "A washed-up bounty hunter takes one last job.",
    genres = listOf("Drama", "Thriller"),
    facts = listOf("2022–present", "3 seasons"),
    rating = 8.1f,
    certification = "15",
)

private val Seasons = List(3) { index ->
    TvDetailSeason(number = index + 1, name = "Season ${index + 1}", episodeCount = "8 episodes")
}

private val Episodes = listOf(1 to 3, 2 to 3).flatMap { (season, count) ->
    (1..count).map { number ->
        TvDetailEpisode(
            key = season * 100 + number,
            seasonNumber = season,
            episodeNumber = number,
            stillUrl = null,
            badge = number.toString(),
            focusedBadge = "S$season E$number",
            title = "Episode $number",
            overview = "A washed-up bounty hunter takes one last job, and the job takes the rest of what he had left to lose.",
            rating = 7.5f,
            facts = listOf("2022", "54 min"),
        )
    }
}

@Composable
private fun EpisodesTab(seedSeason: Int, episodes: List<TvDetailEpisode>) {
    var season by remember { mutableStateOf(seedSeason.toString()) }
    SampleFrame {
        TvDetailEpisodes(
            title = ShowTitle,
            seasons = Seasons,
            currentSeason = season,
            onSeasonChange = { season = it },
            episodes = episodes,
            onEpisodeClick = {},
        )
    }
}

@Composable
private fun SampleFrame(content: @Composable () -> Unit) {
    Column(Modifier.fillMaxSize().padding(dimensionResource(R.dimen.tv_sample_content_inset))) { content() }
}

/** The Episodes tab with the strip run on into the second season, so the second season's pill is the selected one. */
@Composable
fun TvDetailEpisodesSample() {
    EpisodesTab(seedSeason = 2, episodes = Episodes)
}

/** The tab before any episode has loaded: the pills over an empty strip, and the copy blank. */
@Composable
fun TvDetailEpisodesEmptySample() {
    EpisodesTab(seedSeason = 1, episodes = emptyList())
}

/** The season pill's looks: plain, selected (tinted, with its episode count), focused (filled), and selected and focused. */
@Composable
fun TvDetailSeasonPillSample() {
    SampleFrame {
        Row(horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.tv_media_row_card_gap))) {
            TvDetailSeasonPill(Seasons[0], selected = false, isFocused = false)
            TvDetailSeasonPill(Seasons[1], selected = true, isFocused = false)
            TvDetailSeasonPill(Seasons[2], selected = false, isFocused = true)
            TvDetailSeasonPill(Seasons[1], selected = true, isFocused = true)
        }
    }
}

/** An episode card at rest with its bare number, and focused with the season and episode and the ring. */
@Composable
fun TvDetailEpisodeCardSample() {
    SampleFrame {
        Row(horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.tv_media_row_card_gap))) {
            TvDetailEpisodeCard(Episodes[0], isFocused = false)
            TvDetailEpisodeCard(Episodes[1], isFocused = true)
        }
    }
}
