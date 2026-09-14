package com.binge.designsystem

import androidx.compose.ui.unit.dp
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

/**
 * The tabletop height cap.
 *
 * The first test is the load-bearing one, for the same reason it is in [TwoPaneSplitTest]: every
 * phone, tablet and flat-open foldable reports no separating horizontal fold, so if null ever stopped
 * meaning "no cap" every bottom sheet in the app would change height on hardware with no hinge in it.
 */
class FoldSafeBottomHeightTest {
    @Test
    fun `no hinge means no cap`() {
        assertNull(foldSafeBottomHeight(WINDOW_HEIGHT, hinge = null, minHeight = MIN_HEIGHT))
    }

    @Test
    fun `a tabletop fold caps at the space below the crease`() {
        val cap = foldSafeBottomHeight(WINDOW_HEIGHT, HorizontalHinge(top = 490.dp, height = 20.dp), MIN_HEIGHT)

        assertEquals(490.dp, cap)
    }

    @Test
    fun `the cap clears the crease itself, not just its top edge`() {
        val thick = foldSafeBottomHeight(WINDOW_HEIGHT, HorizontalHinge(top = 400.dp, height = 100.dp), MIN_HEIGHT)
        val thin = foldSafeBottomHeight(WINDOW_HEIGHT, HorizontalHinge(top = 400.dp, height = 0.dp), MIN_HEIGHT)

        assertEquals(500.dp, thick)
        assertEquals(600.dp, thin)
    }

    @Test
    fun `a crease too close to the bottom edge is ignored`() {
        val cap = foldSafeBottomHeight(WINDOW_HEIGHT, HorizontalHinge(top = 880.dp, height = 20.dp), MIN_HEIGHT)

        assertNull(cap)
    }

    @Test
    fun `exactly the minimum below the crease still caps`() {
        val cap = foldSafeBottomHeight(WINDOW_HEIGHT, HorizontalHinge(top = 780.dp, height = 20.dp), MIN_HEIGHT)

        assertEquals(MIN_HEIGHT, cap)
    }

    private companion object {
        val WINDOW_HEIGHT = 1000.dp
        val MIN_HEIGHT = 200.dp
    }
}
