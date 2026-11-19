package com.binge.designsystem.motion

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
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
        assertEquals(0.96f, NAV_TAB_PREDICTIVE_POP_ALPHA, 0f)
        assertEquals(700, NAV_TAB_SWITCH_DURATION_MS)
    }

    @Test
    fun `a tab switch cross-fades, the new tab enters and the old one exits, and neither draws above`() {
        val switch = bingeTabSwitch()

        assertNotEquals(EnterTransition.None, switch.targetContentEnter)
        assertNotEquals(ExitTransition.None, switch.initialContentExit)
        assertEquals(0f, switch.targetContentZIndex, 0f)
    }

    @Test
    fun `a tab switch is a fade and not a pop or a push, so it differs from each`() {
        val switch = bingeTabSwitch()

        assertNotEquals(bingeDetailForward(reduceMotion = false).targetContentEnter, switch.targetContentEnter)
        assertNotEquals(bingeDetailForward(reduceMotion = true).targetContentEnter, switch.targetContentEnter)
        assertNotEquals(bingeDetailPop(reduceMotion = false).initialContentExit, switch.initialContentExit)
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
