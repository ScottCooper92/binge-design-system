package com.binge.designsystem.tv.template

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.semantics.Role
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.binge.designsystem.theme.LocalReduceMotion
import com.binge.designsystem.tv.focus.tvClickable
import com.binge.designsystem.tv.focus.tvEntryFocusGroup
import com.binge.designsystem.R as DesR
import com.binge.designsystem.tv.R as TvR

private const val SELECTED_PILL_ALPHA = 0.2f

/**
 * The season pills, scrolling so a long-running show's run off the edge rather than clip. Focusing a pill selects it, as Netflix's
 * do: the strip below follows the pills as they are walked. [entry] is the row's entry node and [down] the strip's; [onFocused]
 * reports focus arriving anywhere in the row.
 */
@Composable
internal fun TvDetailSeasonPills(
    seasons: List<TvDetailSeason>,
    selected: Int,
    onSelect: (Int) -> Unit,
    down: FocusRequester,
    entry: FocusRequester,
    onFocused: () -> Unit,
) {
    val requesters = remember(seasons.map { it.number }) { seasons.associate { it.number to FocusRequester() } }
    val selectedPill = requesters[selected] ?: requesters.values.firstOrNull() ?: remember { FocusRequester() }
    // Opens on the selected pill, so its requester exists when entry routes to it.
    val pillsState = rememberLazyListState(initialFirstVisibleItemIndex = seasons.indexOfFirst { it.number == selected }.coerceAtLeast(0))
    val reduceMotion = LocalReduceMotion.current
    // The strip walks seasons while focus is in it, so a selection can land off-screen with no pill focused to scroll it in.
    LaunchedEffect(selected, seasons) {
        val index = seasons.indexOfFirst { it.number == selected }
        if (index >= 0 && !pillsState.isFullyVisible(index)) {
            if (reduceMotion) pillsState.scrollToItem(index) else pillsState.animateScrollToItem(index)
        }
    }
    LazyRow(
        state = pillsState,
        modifier = Modifier
            .fillMaxWidth()
            .focusRequester(entry)
            .onFocusChanged { if (it.hasFocus) onFocused() }
            .tvEntryFocusGroup(selectedPill),
        contentPadding = PaddingValues(end = dimensionResource(TvR.dimen.tv_overscan_horizontal)),
        horizontalArrangement = Arrangement.spacedBy(dimensionResource(DesR.dimen.padding_s)),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        items(seasons, key = { it.number }) { season ->
            var focused by remember { mutableStateOf(false) }
            val isSelected = season.number == selected
            TvDetailSeasonPill(
                season = season,
                selected = isSelected,
                isFocused = focused,
                modifier = Modifier
                    .focusRequester(requesters.getValue(season.number))
                    .focusProperties { this.down = down }
                    .clip(MaterialTheme.shapes.small)
                    .tvClickable(
                        role = Role.Tab,
                        onFocusChanged = {
                            focused = it
                            if (it && !isSelected) onSelect(season.number)
                        },
                        onClick = { onSelect(season.number) },
                    ),
            )
        }
    }
}

private fun LazyListState.isFullyVisible(index: Int): Boolean {
    val info = layoutInfo
    val item = info.visibleItemsInfo.firstOrNull { it.index == index } ?: return info.visibleItemsInfo.isEmpty()
    return item.offset >= info.viewportStartOffset && item.offset + item.size <= info.viewportEndOffset - info.afterContentPadding
}

/** One pill: filled when focused, tinted when it is the selected season, bare otherwise; the selected one adds its episode count. */
@Composable
internal fun TvDetailSeasonPill(
    season: TvDetailSeason,
    selected: Boolean,
    isFocused: Boolean,
    modifier: Modifier = Modifier,
) {
    val ink = MaterialTheme.colorScheme.onSurface
    val container = when {
        isFocused -> ink
        selected -> ink.copy(alpha = SELECTED_PILL_ALPHA)
        else -> Color.Transparent
    }
    Column(
        modifier = modifier
            .background(container, MaterialTheme.shapes.small)
            .padding(horizontal = dimensionResource(DesR.dimen.padding_m), vertical = dimensionResource(DesR.dimen.padding_s)),
    ) {
        val text = if (isFocused) MaterialTheme.colorScheme.surface else ink
        Text(text = season.name, style = MaterialTheme.typography.titleSmall, color = text, maxLines = 1)
        if (selected) Text(text = season.episodeCount, style = MaterialTheme.typography.labelSmall, color = text, maxLines = 1)
    }
}
