package com.binge.designsystem.tv.template

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import coil3.compose.AsyncImage
import com.binge.designsystem.theme.BingeTheme
import com.binge.designsystem.theme.LocalReduceMotion
import com.binge.designsystem.tv.component.TvProgressBar
import com.binge.designsystem.tv.component.TvRatingBadge
import com.binge.designsystem.tv.component.TvWatchedMark
import com.binge.designsystem.tv.component.tvExpandingFacts
import com.binge.designsystem.tv.focus.TvRowEntry
import com.binge.designsystem.tv.focus.TvStableFocusScroll
import com.binge.designsystem.tv.focus.tvClickable
import com.binge.designsystem.tv.focus.tvEntryFocusGroup
import com.binge.designsystem.R as DesR
import com.binge.designsystem.tv.R as TvR

private const val BADGE_SCRIM_ALPHA = 0.6f
private const val BADGE_SCRIM_END = 0.35f
private const val TAIL_KEY = "binge:tv-episode-row-tail"
private const val COPY_LINES = 3

/**
 * The episodes as one strip, the focused one pinned at its start as the hub rows pin theirs, with [shown]'s title, synopsis and meta
 * beneath. [onFocused] reports focus arriving anywhere in the strip.
 */
@Composable
internal fun TvDetailEpisodeStrip(
    episodes: List<TvDetailEpisode>,
    entry: TvRowEntry,
    rowState: LazyListState,
    shown: TvDetailEpisode?,
    onEpisodeFocused: (TvDetailEpisode) -> Unit,
    onEpisodeClick: (TvDetailEpisode) -> Unit,
    onFocused: () -> Unit,
) {
    var focusedKey by remember { mutableStateOf<String?>(null) }
    val cardHeight = dimensionResource(TvR.dimen.tv_detail_episode_height)
    val reduceMotion = LocalReduceMotion.current
    // The stable scope stops the default bring-into-view fighting this pin.
    LaunchedEffect(focusedKey) {
        val index = episodes.indexOfFirst { it.key == focusedKey }
        if (index < 0) return@LaunchedEffect
        if (reduceMotion) rowState.scrollToItem(index) else rowState.animateScrollToItem(index)
    }
    TvStableFocusScroll {
        LazyRow(
            state = rowState,
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged { if (it.hasFocus) onFocused() }
                .tvEntryFocusGroup(entry.entryFocus),
            contentPadding = PaddingValues(end = dimensionResource(TvR.dimen.tv_overscan_horizontal)),
            horizontalArrangement = Arrangement.spacedBy(dimensionResource(TvR.dimen.tv_media_row_card_gap)),
        ) {
            itemsIndexed(episodes, key = { _, episode -> episode.key }) { index, episode ->
                TvDetailEpisodeCard(
                    episode = episode,
                    isFocused = focusedKey == episode.key,
                    modifier = entry.entryModifier(index).tvClickable(
                        role = Role.Button,
                        onFocusChanged = {
                            if (it) {
                                focusedKey = episode.key
                                entry.rememberFocused(index)
                                onEpisodeFocused(episode)
                            } else if (focusedKey == episode.key) {
                                focusedKey = null
                            }
                        },
                        onClick = { onEpisodeClick(episode) },
                    ),
                )
            }
            // Room for the last episodes to pin to the strip's start too.
            item(key = TAIL_KEY) { Spacer(Modifier.fillParentMaxWidth().height(cardHeight)) }
        }
    }
    TvDetailEpisodeCopy(shown)
}

/** An episode's still under its badge: the bare number at rest, the season and episode while [isFocused], which also rings it. */
@Composable
internal fun TvDetailEpisodeCard(
    episode: TvDetailEpisode,
    isFocused: Boolean,
    modifier: Modifier = Modifier,
) {
    val height = dimensionResource(TvR.dimen.tv_detail_episode_height)
    val shape = MaterialTheme.shapes.medium
    val ring = if (isFocused) MaterialTheme.colorScheme.onSurface else Color.Transparent
    Box(
        modifier = Modifier
            .size(height * TV_DETAIL_WIDE_ASPECT, height)
            .clip(shape)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .border(dimensionResource(TvR.dimen.tv_focus_ring_width), ring, shape)
            .then(modifier),
    ) {
        AsyncImage(
            model = episode.stillUrl,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize().clip(shape),
        )
        Box(
            Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(0f to Color.Black.copy(alpha = BADGE_SCRIM_ALPHA), BADGE_SCRIM_END to Color.Transparent),
                ),
        )
        Text(
            text = if (isFocused) episode.focusedBadge else episode.badge,
            style = MaterialTheme.typography.titleMedium,
            color = BingeTheme.colors.onScrim,
            modifier = Modifier.padding(dimensionResource(DesR.dimen.padding_s)),
        )
        if (episode.watched) {
            TvWatchedMark(
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.align(Alignment.TopEnd).padding(dimensionResource(DesR.dimen.padding_xs)),
            )
        }
        episode.progress?.let { fraction -> TvProgressBar(fraction, Modifier.align(Alignment.BottomStart)) }
    }
}

/** The episode in view, held to one line, three of synopsis and a meta row at a capped width, so the copy never moves between episodes. */
@Composable
internal fun TvDetailEpisodeCopy(episode: TvDetailEpisode?) {
    Column(
        modifier = Modifier.width(dimensionResource(TvR.dimen.tv_detail_episode_copy_width)),
        verticalArrangement = Arrangement.spacedBy(dimensionResource(DesR.dimen.padding_xs)),
    ) {
        Text(
            text = episode?.title.orEmpty(),
            style = MaterialTheme.typography.titleMedium,
            color = BingeTheme.colors.onScrim,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = episode?.overview.orEmpty(),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            minLines = COPY_LINES,
            maxLines = COPY_LINES,
            overflow = TextOverflow.Ellipsis,
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(dimensionResource(TvR.dimen.tv_detail_meta_gap)),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TvRatingBadge(rating = episode?.rating)
            Text(
                text = tvExpandingFacts(genres = emptyList(), trailing = episode?.facts.orEmpty(), genreCount = 0),
                style = MaterialTheme.typography.titleSmall,
                color = BingeTheme.colors.onScrim,
                maxLines = 1,
            )
        }
    }
}
