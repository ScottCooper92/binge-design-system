package com.binge.designsystem

import androidx.compose.runtime.MonotonicFrameClock
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

private const val TITLE_HEIGHT_PX = 120

/** One frame every 16ms, so an animation driven by `withFrameNanos` runs to completion in the test. */
private class ImmediateFrameClock : MonotonicFrameClock {
    private var nanos = 0L

    override suspend fun <R> withFrameNanos(onFrame: (frameTimeNanos: Long) -> R): R {
        nanos += 16_000_000L
        return onFrame(nanos)
    }
}

/**
 * Covers the collapse arithmetic, and the reason [CollapsingTitleState.expand] exists at all: the
 * collapse moves only through [CollapsingTitleState.consume], which the nested-scroll connection
 * drives from the user's drags. A programmatic `animateScrollToItem(0)` dispatches no nested scroll,
 * so without `expand` a scroll-to-top leaves the title hidden over a list already at the top — and
 * anything reading the collapse to decide "am I at the top" reads a value that never comes back.
 */
class CollapsingTitleStateTest {
    @Test
    fun `consume collapses the title and stops at its height`() {
        val state = CollapsingTitleState().apply { heightPx = TITLE_HEIGHT_PX }

        state.consume(-40f)
        assertEquals(40f, state.collapsedPx)

        state.consume(-500f)
        assertEquals(TITLE_HEIGHT_PX.toFloat(), state.collapsedPx)
    }

    @Test
    fun `consume reopens the title and stops fully open`() {
        val state = CollapsingTitleState().apply { heightPx = TITLE_HEIGHT_PX }
        state.consume(-TITLE_HEIGHT_PX.toFloat())

        state.consume(500f)
        assertEquals(0f, state.collapsedPx)
    }

    @Test
    fun `expand reopens a title the user scrolled away`() =
        runTest {
            val state = CollapsingTitleState().apply { heightPx = TITLE_HEIGHT_PX }
            state.consume(-TITLE_HEIGHT_PX.toFloat())
            assertEquals(TITLE_HEIGHT_PX.toFloat(), state.collapsedPx)

            withContext(ImmediateFrameClock()) { state.expand() }

            assertEquals(0f, state.collapsedPx)
        }
}
