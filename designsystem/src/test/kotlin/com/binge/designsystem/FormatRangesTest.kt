package com.binge.designsystem

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class FormatRangesTest {
    @Test
    fun `empty input gives an empty string`() = assertEquals("", emptyList<Int>().formatRanges())

    @Test
    fun `a single number stays as it is`() = assertEquals("4", listOf(4).formatRanges())

    @Test
    fun `two consecutive numbers do not collapse`() = assertEquals("5, 6", listOf(5, 6).formatRanges())

    @Test
    fun `exactly three consecutive numbers collapse`() = assertEquals("1–3", listOf(1, 2, 3).formatRanges())

    @Test
    fun `a long run collapses`() = assertEquals("1–8", (1..8).toList().formatRanges())

    @Test
    fun `a mixed list collapses only its runs`() = assertEquals("1–3, 5, 6", listOf(1, 2, 3, 5, 6).formatRanges())

    @Test
    fun `a run at the end collapses`() = assertEquals("1, 3–5", listOf(1, 3, 4, 5).formatRanges())

    @Test
    fun `unsorted and duplicated input is normalised`() = assertEquals("1–3, 7", listOf(3, 7, 1, 2, 2, 7, 1).formatRanges())

    @Test
    fun `a run through zero collapses`() = assertEquals("0–2", listOf(0, 1, 2).formatRanges())

    @Test
    fun `a set works as well as a list`() = assertEquals("2–4", setOf(4, 3, 2).formatRanges())

    @Test
    fun `the separator joins runs and the numbers inside a short run alike`() =
        assertEquals("1–3\u060C 5\u060C 6", listOf(1, 2, 3, 5, 6).formatRanges(separator = "\u060C "))
}
