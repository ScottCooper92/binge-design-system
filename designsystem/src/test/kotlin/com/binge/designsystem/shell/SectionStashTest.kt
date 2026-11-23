package com.binge.designsystem.shell

import androidx.compose.runtime.mutableIntStateOf
import androidx.navigation3.runtime.NavKey
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SectionStashTest {
    private val tabs = listOf("home", "host", "other")

    private fun state(initial: Int) = TabShellState(tabs, mutableIntStateOf(initial), tabs.associateWith { stackOf(it) })

    private val opensBeside: (NavKey) -> Boolean = { (it as Route).name.startsWith("beside") }
    private val isStandIn: (NavKey) -> Boolean = { (it as Route).name == "standin" }

    @Test
    fun `leaving the host tab parks the chain above the app root on the host stack`() {
        val state = state(initial = 2)
        val app = stackOf("root", "beside-1", "beside-2")
        state.settleStash(app, "host", opensBeside, isStandIn)
        assertEquals(listOf("root"), app.names())
        assertEquals(listOf("host", "beside-1", "beside-2"), state.stack("host").names())
    }

    @Test
    fun `a chain that does not open beside the tabs is left where it is`() {
        val state = state(initial = 2)
        val app = stackOf("root", "detail")
        state.settleStash(app, "host", opensBeside, isStandIn)
        assertEquals(listOf("root", "detail"), app.names())
        assertEquals(listOf("host"), state.stack("host").names())
    }

    @Test
    fun `showing the host tab restores the parked chain and empties the stash`() {
        val state = state(initial = 1)
        state.stack("host").replaceAll(listOf(Route("host"), Route("beside-1")))
        val app = stackOf("root")
        state.settleStash(app, "host", opensBeside, isStandIn)
        assertEquals(listOf("root", "beside-1"), app.names())
        assertEquals(listOf("host"), state.stack("host").names())
    }

    @Test
    fun `a stand-in above the root is replaced by the restored chain`() {
        val state = state(initial = 1)
        state.stack("host").replaceAll(listOf(Route("host"), Route("beside-1")))
        val app = stackOf("root", "standin")
        state.settleStash(app, "host", opensBeside, isStandIn)
        assertEquals(listOf("root", "beside-1"), app.names())
    }

    @Test
    fun `a page the user opened meanwhile wins and the stash is dropped`() {
        val state = state(initial = 1)
        state.stack("host").replaceAll(listOf(Route("host"), Route("beside-1")))
        val app = stackOf("root", "detail")
        state.settleStash(app, "host", opensBeside, isStandIn)
        assertEquals(listOf("root", "detail"), app.names())
        assertEquals(listOf("host"), state.stack("host").names())
    }

    @Test
    fun `nothingOpenBeside is true for the root alone or the root with a stand-in`() {
        val isRoot: (NavKey) -> Boolean = { (it as Route).name == "root" }
        assertTrue(nothingOpenBeside(listOf(Route("root")), isRoot, isStandIn))
        assertTrue(nothingOpenBeside(listOf(Route("root"), Route("standin")), isRoot, isStandIn))
        assertFalse(nothingOpenBeside(listOf(Route("root"), Route("detail")), isRoot, isStandIn))
        assertFalse(nothingOpenBeside(listOf(Route("root"), Route("standin"), Route("detail")), isRoot, isStandIn))
        assertFalse(nothingOpenBeside(listOf(Route("other")), isRoot, isStandIn))
    }
}
