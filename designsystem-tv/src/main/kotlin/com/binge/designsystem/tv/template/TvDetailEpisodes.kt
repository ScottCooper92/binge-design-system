package com.binge.designsystem.tv.template

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.res.dimensionResource
import com.binge.designsystem.tv.focus.rememberTvRowEntry
import com.binge.designsystem.tv.focus.tvEntryFocusGroup
import com.binge.designsystem.tv.nav.tvContentGutterStart
import com.binge.designsystem.R as DesR
import com.binge.designsystem.tv.R as TvR

/**
 * A season pill: its name, and its episode count beneath once it is the selected one. [key] is the app's identity for the season, text, the one
 * [TvDetailEpisode.seasonKey], [TvDetailEpisodes]' `currentSeason` and `onSeasonChange` carry. It is never shown: [name] is the pill's words, so a
 * specials season is just a name.
 */
class TvDetailSeason(
    val key: String,
    val name: String,
    val episodeCount: String,
) {
    /** For a season identified by a number: its [key] is the number as text. */
    constructor(number: Int, name: String, episodeCount: String) : this(number.toString(), name, episodeCount)
}

/**
 * An episode card: its still, the [badge] drawn on it, and the title and synopsis shown under the strip while it is the one in focus.
 * [focusedBadge] is the badge while focused, the season and episode rather than the bare number. [facts] are the air year, runtime
 * and the like, already formatted, shown after the rating. [key] is the app's identity for the episode, text, which the strip's focus memory
 * is kept by; [seasonKey] is its season's [TvDetailSeason.key].
 */
class TvDetailEpisode(
    val key: String,
    val seasonKey: String,
    val episodeNumber: Int,
    val stillUrl: String?,
    val badge: String,
    val focusedBadge: String = badge,
    val title: String,
    val overview: String,
    val rating: Float? = null,
    val facts: List<String> = emptyList(),
) {
    /** For an episode and a season identified by numbers: their keys are the numbers as text. */
    constructor(
        key: Int,
        seasonNumber: Int,
        episodeNumber: Int,
        stillUrl: String?,
        badge: String,
        focusedBadge: String = badge,
        title: String,
        overview: String,
        rating: Float? = null,
        facts: List<String> = emptyList(),
    ) : this(key.toString(), seasonNumber.toString(), episodeNumber, stillUrl, badge, focusedBadge, title, overview, rating, facts)
}

/**
 * A show's Episodes tab as Netflix draws it: the logo and the show's meta, the season pills, then one strip of episodes running
 * on across seasons ([episodes] holds the run of seasons loaded around [currentSeason], in order). The pill for the focused episode's
 * season stays selected as the strip is walked; choosing a pill jumps the strip to that season's first episode. The strip opens on
 * [initialEpisodeKey] when it has one, as the episode page's does on its own episode.
 *
 * @need show a show's episodes as one strip across seasons, with season pills that follow it on a tv screen
 * @group tv
 * @replaces a hand-built tv episodes tab with its own season and focus-memory rules
 */
@Composable
fun TvDetailEpisodes(
    title: TvDetailTitle,
    seasons: List<TvDetailSeason>,
    currentSeason: String,
    onSeasonChange: (String) -> Unit,
    episodes: List<TvDetailEpisode>,
    onEpisodeClick: (TvDetailEpisode) -> Unit,
    modifier: Modifier = Modifier,
    initialEpisodeKey: String? = null,
) {
    // The episode focus last rested on, by id: indices move as seasons load ahead of it, ids do not. Saved for the return from an episode.
    var lastKey by rememberSaveable { mutableStateOf(initialEpisodeKey) }
    val lastIndex = episodes.indexOfFirst { it.key == lastKey }
    val entryIndex = lastIndex.takeIf { it >= 0 } ?: episodes.indexOfFirst { it.seasonKey == currentSeason }.coerceAtLeast(0)
    val entry = rememberTvRowEntry(episodes.size, overrideIndex = entryIndex)
    // Not saveable: a saved index outlives the list it indexed, so a return refilled with fewer episodes landed on the tail spacer.
    val rowState = remember { LazyListState(firstVisibleItemIndex = entryIndex) }
    var placed by remember { mutableStateOf(lastIndex >= 0) }
    // Placed by the saved key instead, once its episode is in the list, wherever that now sits.
    LaunchedEffect(lastIndex) {
        if (placed || lastIndex < 0) return@LaunchedEffect
        placed = true
        rowState.scrollToItem(lastIndex)
    }
    // A pill chosen for another season jumps the strip to that season's first episode; walking the strip into it does not.
    LaunchedEffect(currentSeason, episodes.size) {
        if (episodes.getOrNull(lastIndex)?.seasonKey == currentSeason) return@LaunchedEffect
        val first = episodes.indexOfFirst { it.seasonKey == currentSeason }
        if (first < 0) return@LaunchedEffect
        lastKey = episodes[first].key
        rowState.scrollToItem(first)
    }
    val pillsEntry = remember { FocusRequester() }
    // Saved, so a return from an episode's page lands back on the episode rather than on the season pills above it.
    var lastInEpisodes by rememberSaveable { mutableStateOf(false) }
    Column(
        modifier = modifier
            .fillMaxSize()
            .tvEntryFocusGroup(if (lastInEpisodes && episodes.isNotEmpty()) entry.entryFocus else pillsEntry)
            .padding(start = tvContentGutterStart()),
        verticalArrangement = Arrangement.spacedBy(dimensionResource(DesR.dimen.padding_s)),
    ) {
        // Small here: the episodes and the focused one's synopsis need the height.
        TvDetailLogo(title, compact = true)
        TvDetailMeta(title, Modifier.width(dimensionResource(TvR.dimen.tv_detail_copy_width)))
        TvDetailSeasonPills(
            seasons,
            currentSeason,
            onSeasonChange,
            down = entry.entryFocus,
            entry = pillsEntry,
        ) { lastInEpisodes = false }
        TvDetailEpisodeStrip(
            episodes = episodes,
            entry = entry,
            rowState = rowState,
            shown = episodes.getOrNull(lastIndex) ?: episodes.getOrNull(entryIndex),
            onEpisodeFocused = { episode ->
                lastKey = episode.key
                if (episode.seasonKey != currentSeason) onSeasonChange(episode.seasonKey)
            },
            onEpisodeClick = onEpisodeClick,
        ) { lastInEpisodes = true }
    }
}
