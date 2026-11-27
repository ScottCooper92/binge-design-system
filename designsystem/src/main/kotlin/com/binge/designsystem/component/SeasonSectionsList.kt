package com.binge.designsystem.component

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import com.binge.designsystem.CenteredContent
import com.binge.designsystem.R
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull

/**
 * Every episode of a show, grouped by season: a [SeasonHeader] then that season's rows, one section after
 * another in a single lazy column.
 *
 * Seasons load on demand. As the list scrolls, [onSeasonNeeded] is called with the key of every section with
 * its header or a row on screen, plus the next one below, so scrolling down never reaches an empty section.
 * It may be called again for a key already loading; the caller dedupes. A [SeasonSectionState.Loading] section
 * holds its place with skeleton rows, so the header positions are known before any episode is.
 *
 * The first time headers are known the list scrolls once to the section whose key is [openAt] (the first
 * section when null or unknown), and never again, across recomposition and rotation.
 *
 * [onCurrentSectionChanged] is called with the key of the section at the top of the list, on opening and again each time scrolling
 * moves into another, so an app can name that season in its top bar.
 *
 * [contentPadding] carries a transparent bar's height, so rows travel under it: only its top and bottom are used, and the rows keep
 * the standard screen inset at the sides. A null [onEpisodeClick] makes
 * the rows display-only; otherwise it receives the section key and the episode number.
 *
 * @need list every episode of a show grouped by season, loading seasons as they scroll into view
 * @group rows
 * @replaces a hand-built LazyColumn of season headers, episode rows and skeletons
 * @sample SeasonSectionsListSample
 */
@Composable
fun SeasonSectionsList(
    sections: List<SeasonSectionUi>,
    listState: LazyListState,
    openAt: String?,
    onSeasonNeeded: (key: String) -> Unit,
    modifier: Modifier = Modifier,
    onEpisodeClick: ((sectionKey: String, episodeNumber: Int) -> Unit)? = null,
    contentPadding: PaddingValues = PaddingValues(),
    onCurrentSectionChanged: ((key: String) -> Unit)? = null,
) {
    var didInitialScroll by rememberSaveable { mutableStateOf(false) }
    val headerIndices = remember(sections) { sections.headerIndices() }
    val currentSections by rememberUpdatedState(sections)
    val currentOpenAt by rememberUpdatedState(openAt)
    val currentOnSeasonNeeded by rememberUpdatedState(onSeasonNeeded)
    val currentOnSectionChanged by rememberUpdatedState(onCurrentSectionChanged)

    LaunchedEffect(headerIndices) {
        if (!didInitialScroll && headerIndices.isNotEmpty()) {
            val target = currentSections.indexOfFirst { it.key == currentOpenAt }.takeIf { it >= 0 } ?: 0
            headerIndices.getOrNull(target)?.let { listState.scrollToItem(it) }
            didInitialScroll = true
        }
    }
    LaunchedEffect(listState, headerIndices) {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo.map { it.index } }
            .collect { visible ->
                currentSections.sectionsNeeded(headerIndices, visible).forEach(currentOnSeasonNeeded)
            }
    }

    val currentHeaderIndices by rememberUpdatedState(headerIndices)
    LaunchedEffect(listState) {
        val currentKey = snapshotFlow {
            currentSections.getOrNull(currentHeaderIndices.indexOfLast { it <= listState.firstVisibleItemIndex })?.key
        }
        currentKey
            .filterNotNull()
            .distinctUntilChanged()
            .collect { key -> currentOnSectionChanged?.invoke(key) }
    }

    CenteredContent(modifier = modifier) { contentModifier ->
        LazyColumn(
            state = listState,
            modifier = contentModifier,
            contentPadding = PaddingValues(
                top = contentPadding.calculateTopPadding(),
                bottom = contentPadding.calculateBottomPadding() + dimensionResource(R.dimen.detail_bottom_spacing),
            ),
        ) {
            sections.forEach { section ->
                item(key = "header_${section.key}") {
                    SeasonHeader(title = section.title, subtitle = section.subtitle)
                }
                when (val state = section.state) {
                    is SeasonSectionState.Loaded ->
                        itemsIndexed(
                            state.episodes,
                            key = { index, episode -> state.episodes.rowKey(section.key, index, episode) },
                        ) { _, episode ->
                            EpisodeBrowseRow(
                                episode = episode,
                                onClick = onEpisodeClick?.let { click -> { click(section.key, episode.number) } },
                            )
                        }
                    is SeasonSectionState.Loading ->
                        items(section.rowCount(), key = { "skeleton_${section.key}_$it" }) {
                            EpisodeBrowseRowSkeleton()
                        }
                    is SeasonSectionState.Failed ->
                        item(key = "failed_${section.key}") {
                            SeasonFailedRow(
                                message = state.message,
                                retryLabel = state.retryLabel,
                                onRetry = state.onRetry,
                            )
                        }
                }
            }
        }
    }
}
