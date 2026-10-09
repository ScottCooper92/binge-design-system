package com.binge.designsystem.component

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ChoiceSectionsTest {
    private val choices = listOf("Albania", "Algeria", "Brazil", "Canada", "Chile").map { BingeChoice(it, it) }

    @Test
    fun `the current choice leads, then the suggestions it is not, then every choice by letter`() {
        val sections = choiceSections(choices, selected = "Brazil", suggested = listOf("Canada", "Brazil", "Mars"))
        assertEquals(listOf("Brazil"), sections.current.map { it.value })
        assertEquals("the current pick and an unknown value are not suggested again", listOf("Canada"), sections.suggested.map { it.value })
        assertEquals(listOf('A', 'B', 'C'), sections.byLetter.map { it.first })
    }

    /** Current (header and row), Suggested (header and row), then A's header at 4, B's at 7, C's at 9. */
    @Test
    fun `a letter's header index counts every header and row above it`() {
        val sections = choiceSections(choices, selected = "Brazil", suggested = listOf("Canada"))
        assertEquals(4, sections.indexOf('A'))
        assertEquals(7, sections.indexOf('B'))
        assertEquals(9, sections.indexOf('C'))
        assertNull(sections.indexOf('Z'))
    }

    @Test
    fun `an accented first letter sits with its base letter and a non-Latin one goes under hash`() {
        val labels = listOf("Égypte", "Équateur", "Espagne", "États-Unis", "Österreich", "Zambie", "日本")
        val sections = choiceSections(labels.map { BingeChoice(it, it) }, selected = null, suggested = emptyList())
        assertEquals(listOf('#', 'E', 'O', 'Z'), sections.byLetter.map { it.first })
        val eRows = sections.byLetter.first { it.first == 'E' }.second
        assertEquals(4, eRows.size)
    }

    @Test
    fun `the rail follows the item at the top of the list`() {
        val sections = choiceSections(choices, selected = "Brazil", suggested = listOf("Canada"))
        assertNull("still on Current and Suggested", sections.letterAt(3))
        assertEquals('A', sections.letterAt(4))
        assertEquals('A', sections.letterAt(6))
        assertEquals('B', sections.letterAt(7))
        assertEquals('C', sections.letterAt(10))
    }

    @Test
    fun `several chosen values lead together, in list order, and are not suggested again`() {
        val sections = choiceSections(choices, current = listOf("Canada", "Albania"), suggested = listOf("Canada", "Chile"))
        assertEquals(listOf("Canada", "Albania"), sections.current.map { it.value })
        assertEquals(listOf("Chile"), sections.suggested.map { it.value })
    }
}
