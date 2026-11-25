package com.binge.designsystem.shell

import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import com.binge.designsystem.testing.createTakeDownComposeRule
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class)
class GateEffectsTest {
    @get:Rule
    val rule = createTakeDownComposeRule()

    private val tabs = listOf("home", "search")

    private fun state(initial: Int = 0) = TabShellState(tabs, mutableIntStateOf(initial), tabs.associateWith { stackOf(it, "$it-a") })

    @Test
    fun `an unresolved gate leaves the shell alone`() {
        val state = state()
        val resolved = mutableStateOf<Boolean?>(null)
        rule.setContent { KeepPlaceOnGateChange(resolved.value, state, offTab = "search") }
        rule.waitForIdle()
        assertEquals("home", state.selected)
        assertEquals(2, state.stack("home").size)
    }

    @Test
    fun `a first value of false shows the off tab and pops nothing`() {
        val state = state()
        val resolved = mutableStateOf<Boolean?>(null)
        rule.setContent { KeepPlaceOnGateChange(resolved.value, state, offTab = "search") }
        resolved.value = false
        rule.waitForIdle()
        assertEquals("search", state.selected)
        assertEquals(2, state.stack("home").size)
    }

    @Test
    fun `a first value of true leaves the user's place`() {
        val state = state(initial = 1)
        val resolved = mutableStateOf<Boolean?>(null)
        rule.setContent { KeepPlaceOnGateChange(resolved.value, state, offTab = "home") }
        resolved.value = true
        rule.waitForIdle()
        assertEquals("search", state.selected)
        assertEquals(2, state.stack("search").size)
    }

    @Test
    fun `a change after the first value takes every tab to its root and shows the off tab`() {
        val state = state(initial = 1)
        val resolved = mutableStateOf<Boolean?>(null)
        rule.setContent { KeepPlaceOnGateChange(resolved.value, state, offTab = "home") }
        resolved.value = true
        rule.waitForIdle()
        resolved.value = false
        rule.waitForIdle()
        assertEquals("home", state.selected)
        tabs.forEach { assertEquals(1, state.stack(it).size) }
    }

    @Test
    fun `a null session leaves a restored stack alone and false restarts`() {
        val state = state(initial = 1)
        val app = stackOf("root", "detail")
        val connected = mutableStateOf<Boolean?>(null)
        rule.setContent { RestartOnSessionEnd(connected.value, state, app) }
        rule.waitForIdle()
        assertEquals(listOf("root", "detail"), app.names())
        connected.value = true
        rule.waitForIdle()
        assertEquals(listOf("root", "detail"), app.names())
        connected.value = false
        rule.waitForIdle()
        assertEquals(listOf("root"), app.names())
        assertEquals("home", state.selected)
    }
}
