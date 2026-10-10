package com.binge.designsystem.component

import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Test

/** The month grid grows with its font size rather than clipping its last row (#379). */
class MonthGridHeightTest {
    private fun height(lineHeight: Int) =
        monthGridHeight(lineHeight = lineHeight.dp, cellMin = 48.dp, cellPadding = 8.dp, gap = 8.dp, floor = 216.dp)

    @Test
    fun `at the default size the grid keeps its design height`() {
        assertEquals(216.dp, height(lineHeight = 24))
    }

    @Test
    fun `a taller line makes every row taller`() {
        // 48 + 16 = 64 per row, four rows and three gaps.
        assertEquals(280.dp, height(lineHeight = 48))
    }

    @Test
    fun `the grid never shrinks below its design height`() {
        assertEquals(216.dp, height(lineHeight = 10))
    }
}
