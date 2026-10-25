@file:CatalogGroup("Page templates")

package com.binge.designsystem.tv.catalog

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.binge.designsystem.CARD_ASPECT_RATIO
import com.binge.designsystem.catalog.CatalogGroup
import com.binge.designsystem.theme.BingeShapes
import com.binge.designsystem.tv.component.TvButton
import com.binge.designsystem.tv.component.TvMessagePlate
import com.binge.designsystem.tv.component.TvSeeAllTile
import com.binge.designsystem.tv.focus.tvClickable
import com.binge.designsystem.tv.focus.tvFocusIndicator
import com.binge.designsystem.tv.nav.LocalTvContentInset
import com.binge.designsystem.tv.nav.LocalTvHostedAsOverlay
import com.binge.designsystem.tv.nav.tvContentGutterStart
import com.binge.designsystem.tv.template.TvHubRow
import com.binge.designsystem.tv.template.TvHubRowFailure
import com.binge.designsystem.tv.template.TvImmersiveCopyLayout
import com.binge.designsystem.tv.template.TvImmersiveGrid
import com.binge.designsystem.tv.template.TvImmersiveGridAction
import com.binge.designsystem.tv.template.TvImmersiveGridContent
import com.binge.designsystem.tv.template.TvImmersiveGridInPlace
import com.binge.designsystem.tv.template.TvImmersiveHub
import com.binge.designsystem.tv.theme.TvButtonStyle
import com.binge.designsystem.R as DesR
import com.binge.designsystem.tv.R as TvR

private const val ROW_ITEM_COUNT = 8
private const val TONE_COUNT = 7

private data class SampleTitle(
    val id: Int,
    val title: String,
    val meta: String,
    val synopsis: String,
)

private fun sampleRow(
    key: String,
    title: String,
    firstId: Int,
    seeAll: Boolean = false,
) = TvHubRow(
    key = key,
    title = title,
    items =
        List(ROW_ITEM_COUNT) { index ->
            SampleTitle(
                id = firstId + index,
                title = "A title in $title ${index + 1}",
                meta = "Movie  ·  2026  ·  12A",
                synopsis = "A synopsis long enough to fill two lines of the copy band over the rows, so the sample shows how it clamps.",
            )
        },
    onSeeAll = if (seeAll) ({}) else null,
)

private val SampleRows =
    listOf(
        sampleRow("pending", "Pending (8)", firstId = 100),
        sampleRow("approved", "Approved (24)", firstId = 200, seeAll = true),
        sampleRow("available", "Available (31)", firstId = 300),
    )

/** Stands a page in for a rail destination: the rail's collapsed width is cleared, as the shell provides it. */
@Composable
private fun RailHosted(content: @Composable () -> Unit) {
    CompositionLocalProvider(
        LocalTvContentInset provides dimensionResource(TvR.dimen.tv_nav_rail_collapsed_width),
        content = content,
    )
}

/** The hub's cards, in a spread of container tones so a frame reads as a run of different posters. */
@Composable
private fun sampleTone(id: Int): Color =
    when (id % TONE_COUNT) {
        0 -> MaterialTheme.colorScheme.primaryContainer
        1 -> MaterialTheme.colorScheme.secondaryContainer
        2 -> MaterialTheme.colorScheme.tertiaryContainer
        else -> MaterialTheme.colorScheme.surfaceVariant
    }

@Composable
private fun SampleHub(
    hero: (@Composable () -> Unit)? = null,
    initialFocused: Pair<String, Int>? = null,
    rows: List<TvHubRow<SampleTitle>> = SampleRows,
    copyLayout: TvImmersiveCopyLayout = TvImmersiveCopyLayout.Wide,
) {
    RailHosted {
        TvImmersiveHub(
            rows = rows,
            itemId = { it.id },
            cardWidth = dimensionResource(TvR.dimen.tv_immersive_card_width),
            onItemClick = {},
            artwork = { title -> SampleArtwork(title) },
            copy = { title -> SampleCopy(title) },
            hero = hero,
            initialFocused = initialFocused,
            seeAllLabel = "See all",
            copyLayout = copyLayout,
        ) { title, isFocused, onFocusChanged, onClick, cellModifier ->
            SampleCard(title = title, isFocused = isFocused, onFocusChanged = onFocusChanged, onClick = onClick, modifier = cellModifier)
        }
    }
}

/** A gradient in the card's own tone, since a sample has no network to fetch art from. */
@Composable
private fun SampleArtwork(title: SampleTitle) {
    val tone = sampleTone(title.id)
    Box(modifier = Modifier.fillMaxSize().background(Brush.linearGradient(listOf(tone, MaterialTheme.colorScheme.background))))
}

@Composable
private fun ColumnScope.SampleCopy(title: SampleTitle) {
    Text(text = title.meta, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
    Text(
        text = title.title,
        style = MaterialTheme.typography.displaySmall,
        color = MaterialTheme.colorScheme.onSurface,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
    Text(
        text = title.synopsis,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis,
    )
}

@Composable
private fun SampleCard(
    title: SampleTitle,
    isFocused: Boolean,
    onFocusChanged: (Boolean) -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .aspectRatio(CARD_ASPECT_RATIO)
                .tvFocusIndicator(isFocused = isFocused, shape = BingeShapes.MediaCard)
                .clip(BingeShapes.MediaCard)
                .background(sampleTone(title.id))
                .tvClickable(onFocusChanged = onFocusChanged, onClick = onClick),
    )
}

/** A row of posters per filter over a backdrop that follows focus, with no hero: at rest it shows the first title. */
@Composable
fun TvImmersiveHubSample() {
    SampleHub()
}

/** The same hub with a hero above the rows, which gives way to the backdrop once a card takes focus. */
@Composable
fun TvImmersiveHubHeroSample() {
    SampleHub(hero = { SampleHero() })
}

@Composable
private fun SampleHero() {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(start = tvContentGutterStart(), top = dimensionResource(TvR.dimen.tv_overscan_vertical)),
        verticalArrangement = Arrangement.spacedBy(dimensionResource(DesR.dimen.padding_s)),
    ) {
        Text(text = "Server status", style = MaterialTheme.typography.displaySmall, color = MaterialTheme.colorScheme.onSurface)
        Text(
            text = "Connected  ·  version 2.7",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/** Focus on the second row: the row is pulled to the top of the inset viewport and the rows above it are clipped away. */
@Composable
fun TvImmersiveHubFocusedSample() {
    SampleHub(initialFocused = "approved" to 202)
}

private val PendingSampleRow = TvHubRow<SampleTitle>(key = "pending", title = "Pending", items = emptyList(), isLoading = true)

/** The first row is still loading: a skeleton holds its slot, so it fills in place rather than pushing the rows below down. */
@Composable
fun TvImmersiveHubPendingRowSample() {
    SampleHub(
        rows = listOf(PendingSampleRow) + SampleRows.drop(1),
    )
}

private val FailedSampleRow =
    TvHubRow<SampleTitle>(
        key = "failed",
        title = "Your requests",
        items = emptyList(),
        failure = TvHubRowFailure(message = "These requests could not be loaded.", retryLabel = "Retry", onRetry = {}),
    )

/** The second row could not be read: it keeps its slot and says so, with its retry focused, in place of its cards. */
@Composable
fun TvImmersiveHubFailedRowSample() {
    SampleHub(
        initialFocused = FailedSampleRow.key to 0,
        rows = SampleRows.take(1) + FailedSampleRow + SampleRows.drop(1),
    )
}

/** The narrow copy layout: the copy and the scrim that beds it stop just past half the screen, so more artwork shows. */
@Composable
fun TvImmersiveHubNarrowCopySample() {
    SampleHub(initialFocused = "approved" to 202, copyLayout = TvImmersiveCopyLayout.Narrow)
}

/** An empty first row is skipped: focus on the row after it still anchors that row, not the one below. */
@Composable
fun TvImmersiveHubEmptyRowSample() {
    SampleHub(
        initialFocused = "approved" to 202,
        rows = listOf(TvHubRow<SampleTitle>(key = "pending", title = "Pending (0)", items = emptyList())) + SampleRows.drop(1),
    )
}

/** The tile that closes a row, at rest and focused, beside each other. */
@Composable
fun TvSeeAllTileSample() {
    Row(
        modifier = Modifier.padding(dimensionResource(DesR.dimen.padding_l)),
        horizontalArrangement = Arrangement.spacedBy(dimensionResource(DesR.dimen.padding_l)),
    ) {
        TvSeeAllTile(
            label = "See all",
            isFocused = false,
            onFocusChanged = {},
            onClick = {},
            modifier = Modifier.width(dimensionResource(TvR.dimen.tv_immersive_card_width)),
        )
        TvSeeAllTile(
            label = "See all",
            isFocused = true,
            onFocusChanged = {},
            onClick = {},
            modifier = Modifier.width(dimensionResource(TvR.dimen.tv_immersive_card_width)),
        )
    }
}

private const val GRID_ITEM_COUNT = 30
private const val GRID_FOCUSED_INDEX = 8

/** A row and a half of the six-column grid, so a frame shows a loaded row, a part-loaded row and the edge between. */
private const val GRID_LOADED_COUNT = 9

private val SampleGridTitles =
    List(GRID_ITEM_COUNT) { index ->
        SampleTitle(
            id = 400 + index,
            title = "A title in the grid ${index + 1}",
            meta = "TV show  ·  2025  ·  15",
            synopsis = "A synopsis long enough to fill two lines of the copy band over the grid, so the sample shows how it clamps.",
        )
    }

@Composable
private fun SampleGrid(
    titles: List<SampleTitle> = SampleGridTitles,
    initialFocusedIndex: Int? = null,
    loadedCount: Int = titles.size,
    asOverlay: Boolean = false,
) {
    val host: @Composable (@Composable () -> Unit) -> Unit =
        if (asOverlay) {
            { content -> CompositionLocalProvider(LocalTvHostedAsOverlay provides true, content = content) }
        } else {
            { content -> RailHosted(content) }
        }
    host {
        TvImmersiveGrid(
            heading = "All titles (${titles.size})",
            count = titles.size,
            itemAt = { index -> titles.getOrNull(index)?.takeIf { index < loadedCount } },
            itemKey = { titles[it].id },
            artwork = { title -> SampleArtwork(title) },
            copy = { title -> SampleCopy(title) },
            initialFocusedIndex = initialFocusedIndex,
        ) { title, isFocused, onFocusChanged, cellModifier ->
            SampleCard(title, isFocused, onFocusChanged, onClick = {}, modifier = cellModifier)
        }
    }
}

/** Every title behind a see-all tile as a paged grid: at rest it shows the first title's backdrop. */
@Composable
fun TvImmersiveGridSample() {
    SampleGrid()
}

/** The grid as an overlay, how a see-all page is shown: no rail beside it, so the copy starts at the overscan margin with the heading and cards. */
@Composable
fun TvImmersiveGridOverlaySample() {
    SampleGrid(asOverlay = true)
}

/** Only the first page loaded: the cells past it are not loaded yet and draw nothing, while the heading counts them all. */
@Composable
fun TvImmersiveGridUnloadedSample() {
    SampleGrid(loadedCount = GRID_LOADED_COUNT)
}

/** Focus on the second row: the row is anchored under the heading and the first is clipped away. */
@Composable
fun TvImmersiveGridFocusedSample() {
    SampleGrid(initialFocusedIndex = GRID_FOCUSED_INDEX)
}

/** A grid of [content], rail-hosted, with the sample cells and the given heading controls and message. */
@Composable
private fun SampleStateGrid(
    content: TvImmersiveGridContent<SampleTitle>,
    band: (@Composable () -> Unit)? = null,
    trailingAction: (@Composable () -> Unit)? = null,
    message: @Composable BoxScope.() -> Unit = {},
) {
    RailHosted {
        TvImmersiveGrid(
            heading = "All titles",
            content = content,
            onItemClick = {},
            artwork = { title -> SampleArtwork(title) },
            copy = { title -> SampleCopy(title) },
            band = band,
            trailingAction = trailingAction,
            message = message,
        ) { title, isFocused, onFocusChanged, onClick, cellModifier ->
            SampleCard(title, isFocused, onFocusChanged, onClick = onClick, modifier = cellModifier)
        }
    }
}

/** The first page still loading: two rows of poster blocks where the cells will land, and no backdrop yet. */
@Composable
fun TvImmersiveGridLoadingSample() {
    SampleStateGrid(content = TvImmersiveGridContent.Loading)
}

/** Nothing to show as cells: the caller's message is centred in the pane, with the one action that resolves it. */
@Composable
fun TvImmersiveGridMessageSample() {
    SampleStateGrid(
        content = TvImmersiveGridContent.Message(),
        message = {
            TvMessagePlate(
                body = "Nothing matches these filters.",
                alignment = Alignment.Center,
                actions = { TvButton(label = "Clear filters", onClick = {}, style = TvButtonStyle.Primary) },
            )
        },
    )
}

/** A grid of [content] that loads and fails in place: its plates are the cells, and the entry plate is the message's control. */
@Composable
private fun SampleInPlaceGrid(
    content: TvImmersiveGridContent<SampleTitle>,
    focusedIndex: Int?,
    message: @Composable BoxScope.() -> Unit = {},
) {
    RailHosted {
        TvImmersiveGrid(
            heading = "All titles",
            content = content,
            onItemClick = {},
            artwork = { title -> SampleArtwork(title) },
            copy = { title -> SampleCopy(title) },
            initialFocusedIndex = focusedIndex,
            message = message,
            inPlace = TvImmersiveGridInPlace<SampleTitle>(
                cell = { title, isFocused, onFocusChanged, onClick, cellModifier, label ->
                    SampleInPlaceCell(title, label, isFocused, onFocusChanged, onClick, cellModifier)
                },
                messageAction = TvImmersiveGridAction("Try again") {},
            ),
        ) { _, _, _, _, _ -> }
    }
}

/** One cell of the in-place sample: the title's card, or a plate with the same node chain and its label. */
@Composable
private fun SampleInPlaceCell(
    title: SampleTitle?,
    label: String?,
    isFocused: Boolean,
    onFocusChanged: (Boolean) -> Unit,
    onClick: () -> Unit,
    modifier: Modifier,
) {
    Box(
        modifier =
            modifier
                .aspectRatio(CARD_ASPECT_RATIO)
                .tvFocusIndicator(isFocused = isFocused, shape = BingeShapes.MediaCard)
                .clip(BingeShapes.MediaCard)
                .background(title?.let { sampleTone(it.id) } ?: MaterialTheme.colorScheme.surfaceVariant)
                .tvClickable(onFocusChanged = onFocusChanged, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        if (label != null) Text(text = label, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
    }
}

/** An in-place grid still loading, with focus already on its first plate: the plate takes focus, and the cell it becomes keeps it. */
@Composable
fun TvImmersiveGridInPlaceLoadingSample() {
    SampleInPlaceGrid(content = TvImmersiveGridContent.Loading, focusedIndex = 0)
}

/** An in-place grid that failed: the entry plate is the control and says so, the message sits beside it, and the other cells are empty. */
@Composable
fun TvImmersiveGridInPlaceMessageSample() {
    SampleInPlaceGrid(
        content = TvImmersiveGridContent.Message(),
        focusedIndex = 0,
        message = {
            TvMessagePlate(body = "Check your connection and try again.", headline = "No connection", alignment = Alignment.TopStart)
        },
    )
}

/** An in-place grid that failed with focus on a cell in the third column: the entry plate is that cell, and the message sits under it. */
@Composable
fun TvImmersiveGridInPlaceMessageMidRowSample() {
    SampleInPlaceGrid(
        content = TvImmersiveGridContent.Message(),
        focusedIndex = 2,
        message = {
            TvMessagePlate(body = "Check your connection and try again.", headline = "No connection", alignment = Alignment.TopStart)
        },
    )
}

/** An in-place grid that failed with focus on the last column: the message starts one column earlier, under the entry plate. */
@Composable
fun TvImmersiveGridInPlaceMessageLastColumnSample() {
    SampleInPlaceGrid(
        content = TvImmersiveGridContent.Message(),
        focusedIndex = 4,
        message = {
            TvMessagePlate(body = "Check your connection and try again.", headline = "No connection", alignment = Alignment.TopStart)
        },
    )
}

/** Controls sharing the heading's row: a band of filters after the heading, and an action pinned to the end. */
@Composable
fun TvImmersiveGridHeadingControlsSample() {
    SampleStateGrid(
        content = TvImmersiveGridContent.Items(
            count = SampleGridTitles.size,
            key = { SampleGridTitles[it].id },
            at = SampleGridTitles::getOrNull,
        ),
        band = {
            Row(horizontalArrangement = Arrangement.spacedBy(dimensionResource(DesR.dimen.padding_s))) {
                TvButton(label = "Drama", onClick = {})
                TvButton(label = "2025", onClick = {})
            }
        },
        trailingAction = { TvButton(label = "Filters", onClick = {}) },
    )
}

private const val GRID_DEMO_ITEM_COUNT = 90

private val DemoGridTitles =
    List(GRID_DEMO_ITEM_COUNT) { index ->
        SampleTitle(
            id = 500 + index,
            title = "A title in the grid ${index + 1}",
            meta = "TV show  ·  2025  ·  15",
            synopsis = "A synopsis long enough to fill two lines of the copy band over the grid, so the demo shows how it clamps.",
        )
    }

/** The grid running live over enough rows to scroll: the anchor scroll, the top clip as it moves and the backdrop following focus. */
@Composable
fun TvImmersiveGridDemo() {
    SampleGrid(titles = DemoGridTitles)
}

private val DemoHubRows =
    listOf(
        sampleRow("pending", "Pending (8)", firstId = 600),
        sampleRow("approved", "Approved (24)", firstId = 700, seeAll = true),
        sampleRow("processing", "Processing (5)", firstId = 800),
        sampleRow("available", "Available (31)", firstId = 900, seeAll = true),
        sampleRow("declined", "Declined (3)", firstId = 1000),
    )

/**
 * The hub running live, under a hero and over enough rows to scroll: the anchor scroll and the top clip as focus
 * moves down and back up, the hero giving way to the backdrop, and a see-all tile closing two of the rows.
 */
@Composable
fun TvImmersiveHubDemo() {
    SampleHub(hero = { SampleHero() }, rows = DemoHubRows)
}
