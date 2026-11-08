package com.binge.designsystem.component

import org.junit.Assert.assertEquals
import org.junit.Test

class SheetWashOverdrawTest {
    @Test
    fun `an expanded sheet washes up through the whole top inset as well as over the handle`() {
        assertEquals(48f + 150f, sheetWashOverdraw(handle = 48f, topInset = 150f, containerTop = 150f), 0f)
    }

    @Test
    fun `a sheet dragged down gives the inset back as its content comes away from the top`() {
        assertEquals(48f + 100f, sheetWashOverdraw(handle = 48f, topInset = 150f, containerTop = 200f), 0f)
    }

    @Test
    fun `a part-way sheet washes only over its handle`() {
        assertEquals(48f, sheetWashOverdraw(handle = 48f, topInset = 150f, containerTop = 900f), 0f)
    }

    @Test
    fun `a sheet with no top inset or no handle overdraw reaches nothing extra`() {
        assertEquals(0f, sheetWashOverdraw(handle = 0f, topInset = 0f, containerTop = 0f), 0f)
    }

    @Test
    fun `content above the inset cannot overdraw more than the inset`() {
        assertEquals(48f + 150f, sheetWashOverdraw(handle = 48f, topInset = 150f, containerTop = 0f), 0f)
    }
}
