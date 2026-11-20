package com.binge.designsystem.tv.template

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.binge.designsystem.theme.BingeShapes
import com.binge.designsystem.tv.focus.tvClickable
import com.binge.designsystem.tv.focus.tvFocusIndicator

/** The item the moved grid tests draw: an id to key by, a title the page's copy shows, and a rating the cell shows. */
internal data class GridItem(
    val id: Int,
    val title: String,
    val rating: Float = 7.5f,
)

/** Comfortably past the 90ms settled-focus debounce a TV backdrop, hub anchor and focus memory commit on. */
internal const val SETTLE_PAST_FOCUS_DEBOUNCE_MILLIS = 300L

internal const val GRID_CELL_TEST_TAG = "tv-media-card"

private const val POSTER_ASPECT = 2f / 3f

/** A poster-shaped cell, or a plate while [item] is null, built as Binge's media card is: ring, clip, tag, title, then the click. */
@Composable
internal fun GridTestCell(
    item: GridItem?,
    isFocused: Boolean,
    onFocusChanged: (Boolean) -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .aspectRatio(POSTER_ASPECT)
            .tvFocusIndicator(isFocused = isFocused, shape = BingeShapes.MediaCard)
            .clip(BingeShapes.MediaCard)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .testTag(GRID_CELL_TEST_TAG)
            .semantics { contentDescription = item?.title.orEmpty() }
            .tvClickable(onFocusChanged = onFocusChanged, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        if (item != null) Text(text = item.rating.toString())
    }
}

/** [TvGridPage] over [GridItem]s with the test cell, taking what Binge's `TvGridPane` took so the moved tests read the same. */
@Composable
internal fun TestGridPage(
    heading: String,
    results: TvGridResults<GridItem>,
    onItemClick: (GridItem) -> Unit,
    onRetry: () -> Unit,
    emptyMessage: String,
    modifier: Modifier = Modifier,
    onItemTapped: (index: Int) -> Unit = {},
    onVisitEnded: (deepestIndex: Int?) -> Unit = {},
    columns: Int = 5,
    initialFocused: TvGridFocus<GridItem>? = null,
    entryFocusRequester: FocusRequester? = null,
    emptyAction: TvGridAction? = null,
    emptyIcon: ImageVector? = null,
    retryHolding: Boolean = false,
    trailingAction: (@Composable () -> Unit)? = null,
    band: (@Composable () -> Unit)? = null,
    queryKey: Any? = null,
) {
    TvGridPage(
        heading = heading,
        results = results,
        onItemClick = onItemClick,
        onRetry = onRetry,
        emptyMessage = emptyMessage,
        artwork = {},
        copy = { Text(text = it.title) },
        cell = { item, isFocused, onFocusChanged, onClick, cellModifier ->
            GridTestCell(item, isFocused, onFocusChanged, onClick, cellModifier)
        },
        modifier = modifier,
        onItemTapped = onItemTapped,
        onVisitEnded = onVisitEnded,
        columns = columns,
        initialFocused = initialFocused,
        entryFocusRequester = entryFocusRequester,
        emptyAction = emptyAction,
        emptyIcon = emptyIcon,
        retryHolding = retryHolding,
        trailingAction = trailingAction,
        band = band,
        queryKey = queryKey,
    )
}

/** Results of [items], keyed by id, as Binge's `TvGridResults.Items(list)` is. */
internal fun gridItems(items: List<GridItem>): TvGridResults<GridItem> = TvGridResults.Items(items.size, { items[it].id }, { items[it] })
