package com.binge.designsystem.refresh

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.async
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.io.IOException

class RefreshTrackerTest {
    private val tracker = RefreshTracker()

    @Test
    fun `it is not refreshing until something is tracked`() {
        assertFalse(tracker.isRefreshing.value)
    }

    @Test
    fun `it is refreshing for the length of the block, and hands back its result`() =
        runTest {
            val gate = CompletableDeferred<Unit>()
            val result = async { tracker.track { gate.await().let { "reloaded" } } }
            testScheduler.runCurrent()

            assertTrue(tracker.isRefreshing.value)
            gate.complete(Unit)

            assertEquals("reloaded", result.await())
            assertFalse(tracker.isRefreshing.value)
        }

    @Test
    fun `a failed reload ends the refresh and rethrows the very same exception`() =
        runTest {
            val failure = IOException("down")

            val thrown = runCatching { tracker.track<Unit> { throw failure } }.exceptionOrNull()

            assertSame(failure, thrown)
            assertFalse(tracker.isRefreshing.value)
        }

    @Test
    fun `a cancelled reload ends the refresh`() =
        runTest {
            val gate = CompletableDeferred<Unit>()
            val job = launch { tracker.track { gate.await() } }
            testScheduler.runCurrent()
            assertTrue(tracker.isRefreshing.value)

            job.cancelAndJoin()

            assertFalse(tracker.isRefreshing.value)
        }

    @Test
    fun `overlapping reloads keep it refreshing until the last one ends`() =
        runTest {
            val first = CompletableDeferred<Unit>()
            val second = CompletableDeferred<Unit>()
            val a = launch { tracker.track { first.await() } }
            val b = launch { tracker.track { second.await() } }
            testScheduler.runCurrent()

            first.complete(Unit)
            a.join()
            assertTrue(tracker.isRefreshing.value, "the second reload is still running")

            second.complete(Unit)
            b.join()
            assertFalse(tracker.isRefreshing.value)
        }
}
