package com.binge.designsystem.component

import org.junit.Assert.assertEquals
import org.junit.Test

/** The 0–10 rating to half-star mapping: nearest half star on the 0–5 scale, ties rounding down. */
class RatingToHalvesTest {
    @Test
    fun `a value between two half stars is three and a half stars`() {
        assertEquals(7, ratingToHalves(7.5f))
    }

    @Test
    fun `7_25 rounds to the nearer half star`() {
        assertEquals(7, ratingToHalves(7.25f))
    }

    @Test
    fun `7_75 rounds up to four stars`() {
        assertEquals(8, ratingToHalves(7.75f))
    }

    @Test
    fun `a quarter point rounds to no stars`() {
        assertEquals(0, ratingToHalves(0.25f))
    }

    @Test
    fun `9_9 is five stars`() {
        assertEquals(10, ratingToHalves(9.9f))
    }

    @Test
    fun `whole values draw their own number of half stars`() {
        assertEquals(0, ratingToHalves(0f))
        assertEquals(7, ratingToHalves(7f))
        assertEquals(10, ratingToHalves(10f))
    }

    @Test
    fun `every tie rounds down`() {
        assertEquals(0, ratingToHalves(0.5f))
        assertEquals(1, ratingToHalves(1.5f))
        assertEquals(8, ratingToHalves(8.5f))
        assertEquals(9, ratingToHalves(9.5f))
    }

    @Test
    fun `out of range values clamp`() {
        assertEquals(0, ratingToHalves(-3f))
        assertEquals(10, ratingToHalves(12f))
    }

    @Test
    fun `NaN draws no stars`() {
        assertEquals(0, ratingToHalves(Float.NaN))
    }
}
