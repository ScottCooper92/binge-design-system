package com.binge.designsystem.tv.component

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The See-all card's fan: every layer behind the front one shows at least a quarter of the cell's width of its own image, and the
 * whole fan stays inside the cell, so a portrait or a face reads as one and the clip never cuts the fan flat.
 */
class TvSeeAllCardFanTest {
    private val quarter = 0.25f

    @Test
    fun `each layer behind the front one shows at least a quarter of the card's width`() {
        for (count in 2..3) {
            val visible = seeAllFanVisibleShares(count)
            assertEquals(count, visible.size)
            visible.drop(1).forEachIndexed { index, share ->
                assertTrue("layer ${index + 1} of $count shows only $share of the card's width", share >= quarter)
            }
        }
    }

    @Test
    fun `the front layer shows its whole width`() {
        for (count in 1..3) {
            assertEquals(PEEK_WIDTH_SHARE, seeAllFanVisibleShares(count).first(), 0.0001f)
        }
    }

    @Test
    fun `every layer stays inside the card's width`() {
        for (count in 1..3) {
            seeAllFanOffsets(count).forEach { offset ->
                val left = 0.5f + offset - PEEK_WIDTH_SHARE / 2
                val right = 0.5f + offset + PEEK_WIDTH_SHARE / 2
                assertTrue("a layer reaches $left", left >= 0f)
                assertTrue("a layer reaches $right", right <= 1f)
            }
        }
    }

    @Test
    fun `a single layer is centred and no layer means no fan`() {
        assertEquals(listOf(0f), seeAllFanOffsets(1))
        assertTrue(seeAllFanOffsets(0).isEmpty())
        assertTrue(seeAllFanVisibleShares(0).isEmpty())
    }

    @Test
    fun `layers beyond three are not fanned`() {
        assertEquals(seeAllFanOffsets(3), seeAllFanOffsets(7))
    }
}
