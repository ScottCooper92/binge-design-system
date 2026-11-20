@file:SelfDescribing

package com.binge.designsystem.tv.catalog

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.binge.designsystem.ErrorKind
import com.binge.designsystem.catalog.SelfDescribing
import com.binge.designsystem.theme.BingeShapes
import com.binge.designsystem.tv.focus.tvClickable
import com.binge.designsystem.tv.focus.tvFocusIndicator
import com.binge.designsystem.tv.template.TvGridAction
import com.binge.designsystem.tv.template.TvGridFocus
import com.binge.designsystem.tv.template.TvGridPage
import com.binge.designsystem.tv.template.TvGridResults

private const val POSTER_ASPECT = 2f / 3f
private const val COPY_MAX_LINES = 2
private const val THIRD_ROW_CELL = 11

/** What the samples draw: a title and a year the page's copy shows, and a rating the cell shows. */
internal data class SampleGridItem(
    val id: Int,
    val title: String,
    val rating: String,
    val year: String,
)

private val GridTitles = listOf(
    "Sinners",
    "Longlegs",
    "The Substance",
    "Nosferatu",
    "Talk to Me",
    "Hereditary",
    "Midsommar",
    "The Witch",
    "Saint Maud",
    "The Lighthouse",
    "Men",
    "Pearl",
    "X",
    "Barbarian",
    "Smile",
    "Skinamarink",
    "Possessor",
    "The Empty Man",
    "Censor",
    "Lamb",
)

private val GridItems = GridTitles.mapIndexed { index, title ->
    SampleGridItem(id = index + 1, title = title, rating = "%.1f".format(6.0 + (index % 5) * 0.5), year = (2015 + index % 11).toString())
}

private val LongTitleItem = SampleGridItem(
    id = 99,
    title = "Spider-Man: Brand New Day: the long way home through a city that does not remember him",
    rating = "8.0",
    year = "2026",
)

private fun gridResults(items: List<SampleGridItem>): TvGridResults<SampleGridItem> =
    TvGridResults.Items(items.size, { items[it].id }, { items[it] })

/**
 * The catalog's grid page keeps its own results, seeded with the ones the frame shows. Try again and Clear filters move a failure or
 * an empty result to the items, as a real source does: a retry must leave the failure state, or the focus hold would never end.
 */
@Composable
private fun SampleGrid(
    results: TvGridResults<SampleGridItem>,
    modifier: Modifier = Modifier,
    heading: String = "Horror",
    initialFocused: TvGridFocus<SampleGridItem>? = null,
    withEmptyAction: Boolean = false,
    retryHolding: Boolean = false,
) {
    var current by remember { mutableStateOf(results) }
    val toItems = { current = gridResults(GridItems) }
    val emptyAction = TvGridAction(label = "Clear filters", onClick = toItems).takeIf { withEmptyAction }
    TvGridPage(
        heading = heading,
        results = current,
        onItemClick = {},
        onRetry = toItems,
        emptyMessage = "No movies match",
        artwork = { Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surfaceVariant)) },
        copy = { item ->
            Text(text = item.year, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(
                text = item.title,
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = COPY_MAX_LINES,
                overflow = TextOverflow.Ellipsis,
            )
        },
        cell = { item, isFocused, onFocusChanged, onClick, cellModifier ->
            Box(
                modifier = cellModifier
                    .aspectRatio(POSTER_ASPECT)
                    .tvFocusIndicator(isFocused = isFocused, shape = BingeShapes.MediaCard)
                    .clip(BingeShapes.MediaCard)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .semantics { contentDescription = item?.title.orEmpty() }
                    .tvClickable(onFocusChanged = onFocusChanged, onClick = onClick),
                contentAlignment = Alignment.Center,
            ) {
                if (item != null) Text(text = item.rating)
            }
        },
        modifier = modifier,
        initialFocused = initialFocused,
        emptyAction = emptyAction,
        retryHolding = retryHolding,
    )
}

/** The grid page at rest, four rows of cells with nothing focused: the backdrop describes the first cell, where focus lands. */
@Composable
fun TvGridPageSample() {
    SampleGrid(results = gridResults(GridItems))
}

/** The first cell focused, which is where focus lands on arrival: artwork, heading, the focused cell's copy and the grid below. */
@Composable
fun TvGridPageFocusedSample() {
    SampleGrid(results = gridResults(GridItems), initialFocused = TvGridFocus(index = 0, item = GridItems[0]))
}

/** A cell in the third row focused, so the two rows above it are scrolled away rather than covered. */
@Composable
fun TvGridPageAnchoredSample() {
    SampleGrid(results = gridResults(GridItems), initialFocused = TvGridFocus(index = THIRD_ROW_CELL, item = GridItems[THIRD_ROW_CELL]))
}

/** A focused title long enough to wrap to two lines, under a long heading: the copy must clear the heading. */
@Composable
fun TvGridPageLongTitleSample() {
    SampleGrid(
        results = gridResults(listOf(LongTitleItem) + GridItems.drop(1)),
        heading = "Science Fiction & Fantasy",
        initialFocused = TvGridFocus(index = 0, item = LongTitleItem),
    )
}

/** Loading: plates where the cells will be. */
@Composable
fun TvGridPageLoadingSample() {
    SampleGrid(results = TvGridResults.Loading)
}

/** Try again was pressed and the retry is loading: focus is on a hold that is not drawn, so the entry plate wears the ring. */
@Composable
fun TvGridPageRetryHeldSample() {
    SampleGrid(results = TvGridResults.Loading, retryHolding = true)
}

/** A failure: one message plate with Try again, in place of the grid. */
@Composable
fun TvGridPageFailedSample() {
    SampleGrid(results = TvGridResults.Error(ErrorKind.Network))
}

/** An empty result with its one action: message and button sit as one centred block, with no backdrop. */
@Composable
fun TvGridPageEmptySample() {
    SampleGrid(results = TvGridResults.Empty, heading = "Science Fiction", withEmptyAction = true)
}

/** An empty result with nothing to press: the plate takes focus so the remote has somewhere to be. */
@Composable
fun TvGridPageEmptyNoActionSample() {
    SampleGrid(results = TvGridResults.Empty, heading = "Science Fiction")
}
