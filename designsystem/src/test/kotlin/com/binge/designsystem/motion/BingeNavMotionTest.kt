package com.binge.designsystem.motion

import androidx.compose.animation.EnterTransition
import androidx.compose.ui.unit.LayoutDirection
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class BingeNavMotionTest {
    @Test
    fun `the constants are the tuned values`() {
        assertEquals(5, NAV_FORWARD_ENTER_OFFSET_FRACTION)
        assertEquals(10, NAV_FORWARD_EXIT_OFFSET_FRACTION)
        assertEquals(280, NAV_FORWARD_DURATION_MS)
        assertEquals(90, NAV_POP_DURATION_MS)
        assertEquals(0.9f, NAV_POP_SCALE, 0f)
        assertEquals(0.9f, NAV_PREDICTIVE_POP_ALPHA, 0f)
        assertEquals(0.96f, NAV_TAB_PREDICTIVE_POP_SCALE, 0f)
    }

    @Test
    fun `every pop enters with nothing and draws the popped screen above`() {
        listOf(
            bingeDetailPop(reduceMotion = false),
            bingeDetailPredictivePop(reduceMotion = false),
            bingeTabPredictivePop(reduceMotion = false),
            bingeDetailPop(reduceMotion = true),
        ).forEach {
            assertEquals(EnterTransition.None, it.targetContentEnter)
            assertEquals(-1f, it.targetContentZIndex, 0f)
        }
    }

    @Test
    fun `a forward push enters and exits, in both layout directions`() {
        listOf(LayoutDirection.Ltr, LayoutDirection.Rtl).forEach { direction ->
            val forward = bingeDetailForward(reduceMotion = false, layoutDirection = direction)
            assertNotEquals(EnterTransition.None, forward.targetContentEnter)
            assertEquals(0f, forward.targetContentZIndex, 0f)
        }
    }

    @Test
    fun `reduce motion swaps the slide for a crossfade, so the exit differs from the moving one`() {
        val moving = bingeDetailForward(reduceMotion = false)
        val reduced = bingeDetailForward(reduceMotion = true)

        assertNotEquals(moving.targetContentEnter, reduced.targetContentEnter)
        assertNotEquals(moving.initialContentExit, reduced.initialContentExit)
        assertNotEquals(bingeDetailPop(false).initialContentExit, bingeDetailPop(true).initialContentExit)
    }

    @Test
    fun `the predictive pops differ from the plain pop and from each other`() {
        val pop = bingeDetailPop(false).initialContentExit
        val predictive = bingeDetailPredictivePop(false).initialContentExit
        val tab = bingeTabPredictivePop(false).initialContentExit

        assertNotEquals(pop, predictive)
        assertNotEquals(predictive, tab)
    }
}
