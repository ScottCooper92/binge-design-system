package com.binge.designsystem.tv.component

/** Rows of plates a grid draws while it has no cells yet: enough to run past the bottom edge, the way a resolved grid does. */
internal const val TV_GRID_PLATE_ROWS = 3

/** The focused cell of a plate-and-item grid when none is. */
internal const val TV_GRID_NO_CELL = -1

/** How many plates fill a loading grid of [columns] columns. */
internal fun tvGridPlateCells(columns: Int): Int = columns * TV_GRID_PLATE_ROWS

/**
 * How many cells an in-place grid composes: [base] (its items, or its plates while it loads), but never fewer than reach the
 * focused cell, so a cell focus is on stays composed, as a plate, when the results end before it. Cells are keyed by position,
 * so the plate focus is on is the node the item arrives in.
 *
 * Shared by [TvPosterGrid] and the template's `TvImmersiveGridInPlace`, which differ in who owns the cell and the scroll state
 * (see their KDoc) but not in this rule.
 */
internal fun tvGridCellsDrawn(base: Int, focusedIndex: Int): Int = maxOf(base, focusedIndex + 1)
