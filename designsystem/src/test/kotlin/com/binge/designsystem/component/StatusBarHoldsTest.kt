package com.binge.designsystem.component

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** The per-window hold count behind [DarkStatusBarEffect]: only the last release restores. */
class StatusBarHoldsTest {
    private val window = Any()

    @Test
    fun `a single hold's release is the last one`() {
        val holds = StatusBarHolds<Any>()
        holds.acquire(window)
        assertTrue(holds.release(window))
        assertEquals(0, holds.count(window))
    }

    @Test
    fun `the outgoing screen's release does not restore while the incoming one holds`() {
        val holds = StatusBarHolds<Any>()
        holds.acquire(window) // outgoing screen
        holds.acquire(window) // incoming screen, composed during the transition
        assertFalse(holds.release(window))
        assertEquals(1, holds.count(window))
        assertTrue(holds.release(window))
    }

    @Test
    fun `holds on one window do not keep another from restoring`() {
        val holds = StatusBarHolds<Any>()
        val other = Any()
        holds.acquire(window)
        holds.acquire(other)
        assertTrue(holds.release(other))
        assertEquals(1, holds.count(window))
    }

    @Test
    fun `a release with no hold outstanding restores and leaves no negative count`() {
        val holds = StatusBarHolds<Any>()
        assertTrue(holds.release(window))
        assertEquals(0, holds.count(window))
        holds.acquire(window)
        assertTrue(holds.release(window))
    }
}
