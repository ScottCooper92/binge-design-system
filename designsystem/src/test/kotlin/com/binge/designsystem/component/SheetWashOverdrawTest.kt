package com.binge.designsystem.component

import org.junit.Assert.assertEquals
import org.junit.Test

class SheetWashOverdrawTest {
    @Test
    fun `an expanded sheet with a handle washes up to its top edge`() {
        // The content rests below the inset and the 48 handle: 198.
        assertEquals(198f, sheetWashOverdraw(handle = 48f, topInset = 150f, containerTop = 198f), 0f)
    }

    @Test
    fun `a sheet dragged down gives the inset back as its content comes away from the top`() {
        assertEquals(148f, sheetWashOverdraw(handle = 48f, topInset = 150f, containerTop = 248f), 0f)
    }

    @Test
    fun `a status bar shorter than the handle still reaches the top edge when expanded`() {
        assertEquals(48f + 40f, sheetWashOverdraw(handle = 48f, topInset = 40f, containerTop = 88f), 0f)
    }

    @Test
    fun `a docking sheet, whose content rests at the inset, still reaches its top edge`() {
        assertEquals(198f, sheetWashOverdraw(handle = 48f, topInset = 150f, containerTop = 150f), 0f)
    }

    @Test
    fun `a part-way sheet washes only over its handle`() {
        assertEquals(48f, sheetWashOverdraw(handle = 48f, topInset = 150f, containerTop = 900f), 0f)
    }

    @Test
    fun `a sheet with no top inset or no handle overdraw reaches nothing extra`() {
        assertEquals(0f, sheetWashOverdraw(handle = 0f, topInset = 0f, containerTop = 0f), 0f)
    }
}
