package com.binge.designsystem.shell

import androidx.compose.runtime.mutableIntStateOf
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class TabShellStateTest {
    private val tabs = listOf("home", "search", "settings")

    private fun state(initial: Int = 0) =
        TabShellState(
            tabs = tabs,
            selectedIndex = mutableIntStateOf(initial),
            stacks = tabs.associateWith { stackOf(it, "$it-a", "$it-b") },
        )

    @Test
    fun `show selects a tab and pops nothing`() {
        val state = state()
        state.show("search")
        assertEquals("search", state.selected)
        assertEquals(3, state.stack("search").size)
        assertEquals(3, state.stack("home").size)
    }

    @Test
    fun `back returns to the start tab, then reports it is already there`() {
        val state = state()
        state.show("settings")
        assertTrue(state.back())
        assertEquals("home", state.selected)
        assertFalse(state.back())
    }

    @Test
    fun `selecting another tab shows it and is not a reselect`() {
        val state = state()
        assertFalse(state.select("search"))
        assertEquals("search", state.selected)
        assertEquals(3, state.stack("home").size)
    }

    @Test
    fun `selecting the showing tab pops it to its root and is a reselect`() {
        val state = state()
        state.show("search")
        assertTrue(state.select("search"))
        assertEquals(listOf("search"), state.stack("search").names())
        assertEquals(3, state.stack("home").size)
    }

    @Test
    fun `settleOn takes every tab to its root and shows the tab`() {
        val state = state()
        state.settleOn("settings")
        assertEquals("settings", state.selected)
        tabs.forEach { assertEquals(listOf(it), state.stack(it).names()) }
    }

    @Test
    fun `restart takes the app stack and every tab to its root and shows the start tab`() {
        val state = state()
        val app = stackOf("root", "detail")
        state.show("settings")
        state.restart(app)
        assertEquals("home", state.selected)
        assertEquals(listOf("root"), app.names())
        tabs.forEach { assertEquals(listOf(it), state.stack(it).names()) }
    }

    @Test
    fun `a tab without a stack is refused`() {
        assertThrows(IllegalArgumentException::class.java) {
            TabShellState(tabs, mutableIntStateOf(0), mapOf("home" to stackOf("home")))
        }
    }
}
