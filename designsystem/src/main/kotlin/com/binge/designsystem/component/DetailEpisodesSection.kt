package com.binge.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import com.binge.designsystem.R
import com.binge.designsystem.template.readingInset

/** How many episodes a [DetailEpisodesSection] shows before it offers the rest. */
private const val EPISODES_SHOWN = 5

/**
 * One episode in a [DetailEpisodesSection]: its [number], [name], the [stillUrl], already built, its [rating] (null or zero draws none) and
 * its [airDate], already formatted. The caller owns the words and the date format.
 */
@Immutable
class EpisodeRow(
    val number: Int,
    val name: String,
    val stillUrl: String?,
    val rating: Float? = null,
    val airDate: String? = null,
)

/**
 * The episodes of one season on a series page: the [title] header with the season dropdown ([seasonLabel] names the season shown and
 * [onSeasonClick] opens the picker; with either null there is no dropdown, as a show with one season has nothing to choose), a [subtitle]
 * line ("Season 1 · 9 episodes · 2022"), then the first [shown] of [episodes] as display-only [EpisodeListRow]s and, when there are more
 * and [onViewAll] is given, a button labelled [viewAllLabel] for the rest. While the season loads or has failed, [stateContent] is drawn in
 * place of the rows, so the caller's own loading and error views stay in place; it is drawn inside the same inset, so a skeleton row lines up with the row it stands for. The header, the subtitle and the rows start at the page's
 * reading inset, so they line up with the sections around them.
 *
 * @need list a series' episodes under a season dropdown, the first few with a button for the rest
 * @group detail
 * @replaces a hand-built episodes list with a season header
 * @sample DetailEpisodesSectionSample
 */
@Composable
fun DetailEpisodesSection(
    title: String,
    episodes: List<EpisodeRow>,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    seasonLabel: String? = null,
    onSeasonClick: (() -> Unit)? = null,
    viewAllLabel: String? = null,
    onViewAll: (() -> Unit)? = null,
    shown: Int = EPISODES_SHOWN,
    stateContent: (@Composable () -> Unit)? = null,
) {
    val inset = readingInset()
    Column(modifier, verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_s))) {
        SectionHeader(
            title = title,
            horizontalPadding = inset,
            trailingContent = if (seasonLabel != null && onSeasonClick != null) {
                { SeasonDropdown(label = seasonLabel, onClick = onSeasonClick) }
            } else {
                null
            },
        )
        if (subtitle != null) {
            Text(
                subtitle,
                Modifier.padding(horizontal = inset),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        if (stateContent != null) {
            Box(Modifier.padding(horizontal = inset)) { stateContent() }
        } else {
            episodes.take(shown.coerceAtLeast(0)).forEach { episode ->
                EpisodeListRow(
                    episodeNumber = episode.number,
                    name = episode.name,
                    stillUrl = episode.stillUrl,
                    onClick = null,
                    modifier = Modifier.padding(horizontal = inset),
                    rating = episode.rating ?: 0f,
                    airDate = episode.airDate,
                )
            }
            if (episodes.size > shown.coerceAtLeast(0) && onViewAll != null && viewAllLabel != null) {
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    TextButton(onClick = onViewAll) {
                        Text(viewAllLabel, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
    }
}
