package com.binge.designsystem.tv.template

import org.junit.Assert.assertEquals
import org.junit.Test

class TvFilterRangeLogicTest {
    private val steps = listOf("G", "PG", "PG-13", "R").map { TvRangeStep(it, it) }

    private fun pick(
        bound: TvRangeBound,
        from: String?,
        to: String?,
        picked: String?,
    ) = rangeAfterPick(steps, bound, from, to, picked)

    @Test
    fun `a minimum within the range leaves the maximum`() {
        assertEquals("PG" to "R", pick(TvRangeBound.Min, from = null, to = "R", picked = "PG"))
    }

    @Test
    fun `a minimum equal to the maximum keeps it`() {
        assertEquals("PG" to "PG", pick(TvRangeBound.Min, from = null, to = "PG", picked = "PG"))
    }

    @Test
    fun `a minimum above the maximum clears the maximum`() {
        assertEquals("R" to null, pick(TvRangeBound.Min, from = null, to = "PG", picked = "R"))
    }

    @Test
    fun `a maximum below the minimum clears the minimum`() {
        assertEquals(null to "G", pick(TvRangeBound.Max, from = "PG-13", to = null, picked = "G"))
    }

    @Test
    fun `a maximum above the minimum leaves the minimum`() {
        assertEquals("PG" to "R", pick(TvRangeBound.Max, from = "PG", to = null, picked = "R"))
    }

    @Test
    fun `Any clears just the bound being set`() {
        assertEquals(null to "R", pick(TvRangeBound.Min, from = "PG", to = "R", picked = null))
        assertEquals("PG" to null, pick(TvRangeBound.Max, from = "PG", to = "R", picked = null))
    }

    @Test
    fun `setting a bound on an empty range sets only it`() {
        assertEquals("PG" to null, pick(TvRangeBound.Min, from = null, to = null, picked = "PG"))
        assertEquals(null to "PG", pick(TvRangeBound.Max, from = null, to = null, picked = "PG"))
    }

    @Test
    fun `a bound that is not on the scale sorts before every step`() {
        assertEquals("PG" to "R", pick(TvRangeBound.Min, from = "NC-17", to = "R", picked = "PG"))
        assertEquals("NC-17" to "G", pick(TvRangeBound.Max, from = "NC-17", to = null, picked = "G"))
    }

    private val ratings = (1..9).map { TvRangeStep("$it.0", "$it.0") }
    private val byNumber: (String) -> Float = { it.toFloatOrNull() ?: Float.NEGATIVE_INFINITY }

    @Test
    fun `an off-scale rating floor is ordered by its number, so a Maximum below it clears it`() {
        assertEquals(null to "5.0", rangeAfterPick(ratings, TvRangeBound.Max, from = "7.5", to = null, picked = "5.0", rank = byNumber))
    }

    @Test
    fun `an off-scale rating floor stays under a Maximum above it`() {
        assertEquals("7.5" to "8.0", rangeAfterPick(ratings, TvRangeBound.Max, from = "7.5", to = null, picked = "8.0", rank = byNumber))
    }

    @Test
    fun `an off-scale rating ceiling is ordered by its number, so a Minimum above it clears it`() {
        assertEquals("8.0" to null, rangeAfterPick(ratings, TvRangeBound.Min, from = null, to = "7.5", picked = "8.0", rank = byNumber))
    }
}
