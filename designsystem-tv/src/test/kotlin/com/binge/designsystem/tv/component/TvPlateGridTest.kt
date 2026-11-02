package com.binge.designsystem.tv.component

import org.junit.Assert.assertEquals
import org.junit.Test

/** The cell-count rule the two in-place TV grids share. */
class TvPlateGridTest {
    @Test
    fun `a loading grid is three rows of plates`() {
        assertEquals(12, tvGridPlateCells(columns = 4))
        assertEquals(15, tvGridPlateCells(columns = 5))
    }

    @Test
    fun `with no focused cell the grid draws exactly its base cells`() {
        assertEquals(12, tvGridCellsDrawn(base = 12, focusedIndex = TV_GRID_NO_CELL))
    }

    @Test
    fun `a focus inside the base changes nothing`() {
        assertEquals(12, tvGridCellsDrawn(base = 12, focusedIndex = 11))
    }

    @Test
    fun `a focus past the base keeps its cell composed`() {
        assertEquals(9, tvGridCellsDrawn(base = 5, focusedIndex = 8))
    }

    @Test
    fun `an empty base with no focus draws nothing`() {
        assertEquals(0, tvGridCellsDrawn(base = 0, focusedIndex = TV_GRID_NO_CELL))
    }
}
