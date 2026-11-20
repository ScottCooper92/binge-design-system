@file:SelfDescribing

package com.binge.designsystem.catalog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import com.binge.designsystem.R
import com.binge.designsystem.component.BingeFastScrollbar
import com.binge.designsystem.component.FastScrollbarBar
import com.binge.designsystem.component.SectionHeader
import com.binge.designsystem.preview.ScreenshotTheme
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

private val Departments = listOf("Directing", "Writing", "Production", "Camera", "Editing", "Sound", "Art", "Costume", "Visual Effects")
private const val PER_DEPARTMENT = 9
private const val COLUMNS = 3
private const val GRID_ITEMS = 9 * (PER_DEPARTMENT + 1)
private const val LIST_ITEMS = 80

/** Where a frame holds the thumb, as a share of the track, and the share of the list a screenful is. */
private const val HELD_FRACTION = 0.35f
private const val GRID_THUMB_SHARE = 0.18f
private const val LIST_THUMB_SHARE = 0.12f

/** The department a grid item belongs to, for the bubble: each department is a header followed by [PER_DEPARTMENT] cells. */
private fun departmentAt(index: Int): String = Departments[(index / (PER_DEPARTMENT + 1)).coerceIn(0, Departments.lastIndex)]

private fun indexAt(fraction: Float, total: Int): Int = (fraction * (total - 1)).roundToInt()

@Composable
private fun SectionedGrid(state: LazyGridState, modifier: Modifier = Modifier) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(COLUMNS),
        state = state,
        modifier = modifier,
        contentPadding = PaddingValues(dimensionResource(R.dimen.padding_m)),
        horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_sm)),
        verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_sm)),
    ) {
        Departments.forEach { department ->
            item(key = "header-$department", span = { GridItemSpan(maxLineSpan) }) { SectionHeader(title = department) }
            items(PER_DEPARTMENT, key = { "$department-$it" }) { index ->
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(dimensionResource(R.dimen.fast_scrollbar_sample_cell_height))
                        .padding(dimensionResource(R.dimen.padding_xxs)),
                    contentAlignment = Alignment.Center,
                ) { Text(text = "${index + 1}", style = MaterialTheme.typography.titleMedium) }
            }
        }
    }
}

/**
 * [BingeFastScrollbar] on a sectioned grid with the thumb held a third of the way down, so the bubble names the department the grid is in. A frame
 * cannot hold a drag, so this draws the held scrollbar directly; in the catalog the thumb drags the grid, and the bubble follows it.
 */
@Composable
fun BingeFastScrollbarSample() {
    ScreenshotTheme {
        val state = rememberLazyGridState(initialFirstVisibleItemIndex = indexAt(HELD_FRACTION, GRID_ITEMS))
        var fraction by remember { mutableFloatStateOf(HELD_FRACTION) }
        var held by remember { mutableStateOf(true) }
        val scope = rememberCoroutineScope()
        Box(Modifier.height(dimensionResource(R.dimen.fast_scrollbar_sample_height))) {
            SectionedGrid(state, Modifier.fillMaxSize())
            FastScrollbarBar(
                fraction = fraction,
                thumbShare = GRID_THUMB_SHARE,
                revealed = true,
                dragging = held,
                bubble = if (held) departmentAt(indexAt(fraction, GRID_ITEMS)) else null,
                label = stringResource(R.string.fast_scrollbar_label),
                stateText = departmentAt(indexAt(fraction, GRID_ITEMS)),
                onDragStart = { held = true },
                onDragTo = {
                    fraction = it
                    scope.launch { state.scrollToItem(indexAt(fraction, GRID_ITEMS)) }
                },
                onDragEnd = { held = false },
                onSetProgress = { fraction = it },
            )
        }
    }
}

@Composable
private fun PlainList(state: LazyListState, modifier: Modifier = Modifier) {
    LazyColumn(state = state, modifier = modifier, contentPadding = PaddingValues(dimensionResource(R.dimen.padding_m))) {
        items(LIST_ITEMS, key = { it }) { index ->
            Text(text = "Item ${index + 1}", modifier = Modifier.padding(vertical = dimensionResource(R.dimen.padding_s)))
        }
    }
}

/** [BingeFastScrollbar] on a plain list with no bubble, the thumb held a third of the way down. Drawn directly, as the grid sample is. */
@Composable
fun BingeFastScrollbarListSample() {
    ScreenshotTheme {
        val state = rememberLazyListState(initialFirstVisibleItemIndex = indexAt(HELD_FRACTION, LIST_ITEMS))
        var fraction by remember { mutableFloatStateOf(HELD_FRACTION) }
        var held by remember { mutableStateOf(true) }
        val scope = rememberCoroutineScope()
        Box(Modifier.height(dimensionResource(R.dimen.fast_scrollbar_sample_height))) {
            PlainList(state, Modifier.fillMaxSize())
            FastScrollbarBar(
                fraction = fraction,
                thumbShare = LIST_THUMB_SHARE,
                revealed = true,
                dragging = held,
                bubble = null,
                label = stringResource(R.string.fast_scrollbar_label),
                stateText = null,
                onDragStart = { held = true },
                onDragTo = {
                    fraction = it
                    scope.launch { state.scrollToItem(indexAt(fraction, LIST_ITEMS)) }
                },
                onDragEnd = { held = false },
                onSetProgress = { fraction = it },
            )
        }
    }
}

/** The public [BingeFastScrollbar] on the sectioned grid, live: scroll to reveal it, drag the thumb to jump, and the bubble names the department. */
@Composable
fun BingeFastScrollbarDemo() {
    ScreenshotTheme {
        val state = rememberLazyGridState()
        Box(Modifier.height(dimensionResource(R.dimen.fast_scrollbar_sample_height))) {
            SectionedGrid(state, Modifier.fillMaxSize())
            BingeFastScrollbar(state = state, bubbleText = ::departmentAt)
        }
    }
}
