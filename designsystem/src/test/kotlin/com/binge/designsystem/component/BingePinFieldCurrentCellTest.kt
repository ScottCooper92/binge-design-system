package com.binge.designsystem.component

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/** Which box a focused PIN field outlines as the one being typed (#394): it tracks the digits entered. */
class BingePinFieldCurrentCellTest {
    @Test
    fun `the first box is current on an empty focused field`() {
        assertEquals(0, currentPinCell(focused = true, enabled = true, filled = 0, length = PIN_LENGTH))
    }

    @Test
    fun `the current box follows the digits entered`() {
        assertEquals(
            listOf(0, 1, 2, 3),
            (0 until PIN_LENGTH).map { currentPinCell(focused = true, enabled = true, filled = it, length = PIN_LENGTH) },
        )
    }

    @Test
    fun `a full field keeps the last box current`() {
        assertEquals(PIN_LENGTH - 1, currentPinCell(focused = true, enabled = true, filled = PIN_LENGTH, length = PIN_LENGTH))
    }

    @Test
    fun `no box is current while the field is unfocused or disabled`() {
        assertNull(currentPinCell(focused = false, enabled = true, filled = 2, length = PIN_LENGTH))
        assertNull(currentPinCell(focused = true, enabled = false, filled = 2, length = PIN_LENGTH))
    }

    private companion object {
        const val PIN_LENGTH = 4
    }
}
