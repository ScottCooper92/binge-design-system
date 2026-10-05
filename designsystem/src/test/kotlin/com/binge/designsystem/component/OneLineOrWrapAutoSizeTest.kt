package com.binge.designsystem.component

import androidx.compose.ui.unit.sp
import org.junit.Assert.assertEquals
import org.junit.Test

/** How [OneLineOrWrapAutoSize] steps a title down to fit one line, or gives up and wraps at full size. */
class OneLineOrWrapAutoSizeTest {
    @Test
    fun `a title that fits at full size keeps it`() {
        val tried = mutableListOf<Float>()
        val size = oneLineOrWrapSize(max = 36f, min = 28f, step = 2f) {
            tried += it
            true
        }
        assertEquals(36f, size)
        assertEquals(listOf(36f), tried)
    }

    @Test
    fun `a title steps down until it fits on one line`() {
        val tried = mutableListOf<Float>()
        val size = oneLineOrWrapSize(max = 36f, min = 28f, step = 2f) {
            tried += it
            it <= 31f
        }
        assertEquals(30f, size)
        assertEquals(listOf(36f, 34f, 32f, 30f), tried)
    }

    @Test
    fun `the minimum is still tried`() {
        assertEquals(28f, oneLineOrWrapSize(max = 36f, min = 28f, step = 2f) { it == 28f })
    }

    @Test
    fun `a title that never fits wraps at full size`() {
        val tried = mutableListOf<Float>()
        val size = oneLineOrWrapSize(max = 36f, min = 28f, step = 2f) {
            tried += it
            false
        }
        assertEquals(36f, size)
        assertEquals(listOf(36f, 34f, 32f, 30f, 28f), tried)
    }

    @Test
    fun `a step that overshoots the minimum stops above it`() {
        val tried = mutableListOf<Float>()
        oneLineOrWrapSize(max = 45f, min = 34f, step = 2f) {
            tried += it
            false
        }
        assertEquals(listOf(45f, 43f, 41f, 39f, 37f, 35f), tried)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `a step of zero is refused`() {
        OneLineOrWrapAutoSize(max = 36.sp, min = 28.sp, step = 0.sp)
    }
}
