package com.binge.designsystem.component

import androidx.compose.ui.unit.IntSize
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Which hero slides draw a placeholder while their image loads (#1300). A slide the carousel waited for arrives loaded and draws
 * none, so it never flashes a plate over the previous backdrop; one it never waited for (the first slide, or a refreshed list's
 * new image) does. Coil's own loading state is not observable in a unit test, so the rule is pinned here.
 */
class HeroBackdropReadinessTest {
    private val readiness = HeroBackdropReadiness()
    private val size = IntSize(1, 1)

    @Test
    fun `an image nobody waited for needs a placeholder`() {
        assertTrue(readiness.needsPlaceholder("alpha"))
    }

    @Test
    fun `an image the carousel waited for does not`() {
        runBlocking { HeroBackdropLoader { _, _ -> true }.loadAndMark(readiness, "alpha", size) }

        assertFalse(readiness.needsPlaceholder("alpha"))
    }

    @Test
    fun `a refreshed list's new image for the same slide needs one again`() {
        runBlocking { HeroBackdropLoader { _, _ -> true }.loadAndMark(readiness, "alpha", size) }

        assertTrue(readiness.needsPlaceholder("alpha-refreshed"))
    }

    @Test
    fun `a missing image always needs one, and is never recorded`() {
        runBlocking { HeroBackdropLoader { _, _ -> true }.loadAndMark(readiness, null, size) }

        assertTrue(readiness.needsPlaceholder(null))
        assertTrue(readiness.needsPlaceholder(""))
    }

    @Test
    fun `an image that failed to load still needs one`() {
        runBlocking { HeroBackdropLoader { _, _ -> false }.loadAndMark(readiness, "alpha", size) }

        assertTrue(readiness.needsPlaceholder("alpha"))
    }
}
