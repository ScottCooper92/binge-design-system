package com.binge.designsystem.tv.template

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import com.binge.designsystem.tv.focus.TvStableFocusScroll
import com.binge.designsystem.tv.layout.TvLayoutAnchors
import com.binge.designsystem.tv.layout.tvLayoutAnchor
import com.binge.designsystem.tv.R as TvR

/** Rows of plates drawn while a grid with [TvImmersiveGridInPlace] has no cells: enough to run past the bottom edge. */
internal const val PLATE_ROWS = 3

private const val NO_CELL = -1

/** What the entry plate of an in-place grid does in the message state: [label] drawn in it, [onClick] on OK. */
class TvImmersiveGridAction(
    val label: String,
    val onClick: () -> Unit,
)

/**
 * Draws one cell of an in-place [TvImmersiveGrid], with an [item] or without ([item] null: a plate). It is one composable for
 * both, so the node focus is on is the node the item arrives in. A plate takes focus and OK does nothing; the entry plate of
 * a message gets its [label] and OK runs the action.
 */
typealias TvInPlaceCell<T> = @Composable (
    item: T?,
    isFocused: Boolean,
    onFocusChanged: (Boolean) -> Unit,
    onClick: () -> Unit,
    cellModifier: Modifier,
    label: String?,
) -> Unit

/** Opts a [TvImmersiveGrid] into one grid for every state, drawing each cell, plate or item, with [cell]. */
class TvImmersiveGridInPlace<T : Any>(
    val cell: TvInPlaceCell<T>,
    val messageAction: TvImmersiveGridAction? = null,
)

/**
 * The one grid for loading, message and cells. Cells are keyed by position, so the plate focus is on is the cell the item
 * arrives in; in the message state the entry plate stays, the other cells draw nothing, and the message sits beside it, never over it.
 */
@Composable
internal fun <T : Any> TvImmersiveGridBody(
    content: TvImmersiveGridContent<T>,
    inPlace: TvImmersiveGridInPlace<T>,
    columns: Int,
    gridState: LazyGridState,
    cellPadding: PaddingValues,
    gridModifier: Modifier,
    entryIndex: Int,
    entryModifier: Modifier,
    ringIndex: Int?,
    onCellFocused: (index: Int, focused: Boolean) -> Unit,
    onCellClick: (index: Int, item: T) -> Unit,
    messageEndInset: Dp,
    message: @Composable BoxScope.() -> Unit,
) {
    val items = content as? TvImmersiveGridContent.Items
    val isMessage = content is TvImmersiveGridContent.Message
    Box(Modifier.fillMaxSize()) {
        TvStableFocusScroll {
            LazyVerticalGrid(
                columns = GridCells.Fixed(columns),
                state = gridState,
                modifier = gridModifier,
                contentPadding = cellPadding,
                verticalArrangement = Arrangement.spacedBy(dimensionResource(TvR.dimen.tv_immersive_grid_row_gap)),
                horizontalArrangement = Arrangement.spacedBy(dimensionResource(TvR.dimen.tv_media_row_card_gap)),
            ) {
                // Focus keeps its cell composed, even past the plate rows or fewer items than were focused: a cell past the
                // items is a plate until focus leaves it.
                val focused = ringIndex ?: NO_CELL
                val drawn = maxOf(items?.count ?: maxOf(columns * PLATE_ROWS, entryIndex + 1), focused + 1)
                items(count = drawn, key = { it }) { index ->
                    if (isMessage && index != entryIndex) {
                        Spacer(Modifier.fillMaxWidth())
                    } else {
                        val item = items?.takeIf { index < it.count }?.at(index)
                        val isEntryMessage = isMessage && index == entryIndex
                        // The anchor rides a wrapper, plate or item, so the first cell is the one the skeleton is measured against.
                        Box(
                            modifier = if (index == 0) Modifier.tvLayoutAnchor(TvLayoutAnchors.GRID_FIRST_CELL) else Modifier,
                            propagateMinConstraints = true,
                        ) {
                            inPlace.cell(
                                item,
                                ringIndex == index,
                                { focused -> onCellFocused(index, focused) },
                                {
                                    if (item != null) {
                                        onCellClick(index, item)
                                    } else if (isEntryMessage) {
                                        inPlace.messageAction?.onClick?.invoke()
                                    }
                                },
                                if (index == entryIndex) entryModifier else Modifier,
                                inPlace.messageAction?.label.takeIf { isEntryMessage },
                            )
                        }
                    }
                }
            }
        }
        if (isMessage) TvImmersiveGridMessage(gridState, entryIndex, columns, cellPadding, messageEndInset, message)
    }
}

/**
 * The message beside the entry plate, top-aligned with it and placed from the plate's own laid-out cell so it follows wherever
 * focus is and however far the grid has scrolled. It runs from the plate's end edge to the grid's end; a plate in the last column
 * has nothing to its right, so the message sits on its start side, end-aligned against the plate. Beside rather than under, because a
 * poster-sized cell leaves no room below it. Read in the layout phase, after the grid has measured, so the first frame has the
 * plate's cell. Item offsets exclude the grid's content padding, so it is added back; placement is left-to-right only.
 */
@Composable
private fun TvImmersiveGridMessage(
    state: LazyGridState,
    entryIndex: Int,
    columns: Int,
    cellPadding: PaddingValues,
    endInset: Dp,
    message: @Composable BoxScope.() -> Unit,
) {
    val direction = LocalLayoutDirection.current
    val columnGap = dimensionResource(TvR.dimen.tv_media_row_card_gap)
    val startPadding = cellPadding.calculateStartPadding(direction)
    val topPadding = cellPadding.calculateTopPadding()
    val endPadding = cellPadding.calculateEndPadding(direction)
    Box(
        Modifier.fillMaxSize().layout { measurable, constraints ->
            val plate = state.layoutInfo.visibleItemsInfo.firstOrNull { it.index == entryIndex }
            if (plate == null) {
                layout(constraints.maxWidth, constraints.maxHeight) {}
            } else {
                val gap = columnGap.roundToPx()
                val plateStart = startPadding.roundToPx() + plate.offset.x
                val gridStart = startPadding.roundToPx()
                val gridEnd = constraints.maxWidth - endPadding.roundToPx() - endInset.roundToPx()
                val isLastColumn = entryIndex % columns == columns - 1
                val startX = if (isLastColumn) gridStart else plateStart + plate.size.width + gap
                val endX = if (isLastColumn) plateStart - gap else gridEnd
                val startY = topPadding.roundToPx() + plate.offset.y
                val room = (endX - startX).coerceAtLeast(0)
                val height = (constraints.maxHeight - startY).coerceAtLeast(0)
                val width = if (isLastColumn) measurable.maxIntrinsicWidth(height).coerceIn(0, room) else room
                val placeable = measurable.measure(Constraints(maxWidth = width, maxHeight = height))
                val placeX = if (isLastColumn) endX - placeable.width else startX
                layout(constraints.maxWidth, constraints.maxHeight) { placeable.place(placeX, startY) }
            }
        },
    ) { Box(contentAlignment = Alignment.TopStart, content = message) }
}
