package com.binge.designsystem

import androidx.compose.ui.unit.dp
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

/**
 * The padding that moves content off a tabletop crease.
 *
 * The first test is the load-bearing one: every window without a separating horizontal fold reports no
 * hinge, so if null ever stopped meaning "no padding" every control row using the modifier would move on
 * hardware with no hinge in it.
 */
class FoldClearancePaddingTest {
    private val hinge = HorizontalHinge(top = 490.dp, height = 20.dp)

    @Test
    fun `no hinge means no padding`() {
        assertEquals(0.dp, foldClearancePadding(top = 480.dp, height = 100.dp, hinge = null))
    }

    @Test
    fun `content wholly above the crease is left alone`() {
        assertEquals(0.dp, foldClearancePadding(top = 400.dp, height = 90.dp, hinge = hinge))
    }

    @Test
    fun `content wholly below the crease is left alone`() {
        assertEquals(0.dp, foldClearancePadding(top = 510.dp, height = 90.dp, hinge = hinge))
    }

    @Test
    fun `content crossing the crease starts below it`() {
        assertEquals(30.dp, foldClearancePadding(top = 480.dp, height = 100.dp, hinge = hinge))
    }

    @Test
    fun `content that starts inside the crease starts below it`() {
        assertEquals(5.dp, foldClearancePadding(top = 505.dp, height = 40.dp, hinge = hinge))
    }
}
