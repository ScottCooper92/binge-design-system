package com.binge.designsystem.tv.template

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import com.binge.designsystem.tv.focus.TvStableFocusScroll
import com.binge.designsystem.tv.layout.TvLayoutAnchors
import com.binge.designsystem.tv.layout.tvLayoutAnchor
import com.binge.designsystem.tv.R as TvR

/** Rows of plates drawn while a grid with [TvImmersiveGridInPlace] has no cells: enough to run past the bottom edge. */
internal const val PLATE_ROWS = 3

private const val NO_CELL = -1

/**
 * Draws one cell of an in-place [TvImmersiveGrid], with an [item] or without ([item] null: a plate). It is one composable for
 * both, so the node focus is on is the node the item arrives in. A plate takes focus and OK does nothing.
 */
typealias TvInPlaceCell<T> = @Composable (
    item: T?,
    isFocused: Boolean,
    onFocusChanged: (Boolean) -> Unit,
    onClick: () -> Unit,
    cellModifier: Modifier,
) -> Unit

/** Opts a [TvImmersiveGrid] into one grid for every state, drawing each cell, plate or item, with [cell]. */
class TvImmersiveGridInPlace<T : Any>(
    val cell: TvInPlaceCell<T>,
)

/** The one grid for loading and cells. Cells are keyed by position, so the plate focus is on is the cell the item arrives in. */
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
) {
    val items = content as? TvImmersiveGridContent.Items
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
                    val item = items?.takeIf { index < it.count }?.at(index)
                    // The anchor rides a wrapper, plate or item, so the first cell is the one the skeleton is measured against.
                    Box(
                        modifier = if (index == 0) Modifier.tvLayoutAnchor(TvLayoutAnchors.GRID_FIRST_CELL) else Modifier,
                        propagateMinConstraints = true,
                    ) {
                        inPlace.cell(
                            item,
                            ringIndex == index,
                            { focused -> onCellFocused(index, focused) },
                            { if (item != null) onCellClick(index, item) },
                            if (index == entryIndex) entryModifier else Modifier,
                        )
                    }
                }
            }
        }
    }
}
