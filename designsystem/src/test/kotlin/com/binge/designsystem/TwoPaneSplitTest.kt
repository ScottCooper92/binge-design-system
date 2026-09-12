package com.binge.designsystem

import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

/**
 * The hinge-aware two-pane split (#819).
 *
 * The first test is the load-bearing one: every phone, tablet and flat-open foldable reports no
 * separating fold, so if that case ever stopped returning the defaults this feature would silently
 * relayout every two-pane screen in the app on hardware that has no hinge at all.
 */
class TwoPaneSplitTest {
    @Test
    fun `no hinge keeps the default split`() {
        val split = split(hinge = null)

        assertEquals(TwoPaneSplit(DEFAULT_LEADING, DEFAULT_GAP), split)
    }

    @Test
    fun `a separating hinge moves the boundary onto it`() {
        val split = split(hinge = VerticalHinge(start = 500.dp, width = 24.dp))

        assertEquals(TwoPaneSplit(leadingWidth = 500.dp, gap = 24.dp), split)
    }

    @Test
    fun `a hinge narrower than the default gap does not shrink it`() {
        val split = split(hinge = VerticalHinge(start = 500.dp, width = 4.dp))

        assertEquals(DEFAULT_GAP, split.gap)
    }

    @Test
    fun `a hinge that would leave the leading pane a sliver is ignored`() {
        val split = split(hinge = VerticalHinge(start = 120.dp, width = 24.dp))

        assertEquals(TwoPaneSplit(DEFAULT_LEADING, DEFAULT_GAP), split)
    }

    @Test
    fun `a hinge that would leave the trailing pane a sliver is ignored`() {
        val split = split(hinge = VerticalHinge(start = 900.dp, width = 24.dp))

        assertEquals(TwoPaneSplit(DEFAULT_LEADING, DEFAULT_GAP), split)
    }

    @Test
    fun `a hinge exactly on the floor is honoured`() {
        val split = split(hinge = VerticalHinge(start = MIN_PANE, width = 24.dp))

        assertEquals(MIN_PANE, split.leadingWidth)
    }

    @Test
    fun `under RTL the leading width is measured from the physical right`() {
        val split =
            split(hinge = VerticalHinge(start = 500.dp, width = 24.dp), layoutDirection = LayoutDirection.Rtl)

        // The row's first child now starts at the physical right, so its width has to reach back to
        // the far edge of the crease, not the near one — availableWidth - hinge.end, not hinge.start.
        assertEquals(TwoPaneSplit(leadingWidth = AVAILABLE - 524.dp, gap = 24.dp), split)
    }

    @Test
    fun `an RTL hinge that would leave the physical-right pane a sliver is ignored`() {
        val split =
            split(hinge = VerticalHinge(start = 900.dp, width = 24.dp), layoutDirection = LayoutDirection.Rtl)

        assertEquals(TwoPaneSplit(DEFAULT_LEADING, DEFAULT_GAP), split)
    }

    @Test
    fun `an RTL hinge that would leave the physical-left pane a sliver is ignored`() {
        val split =
            split(hinge = VerticalHinge(start = 120.dp, width = 24.dp), layoutDirection = LayoutDirection.Rtl)

        assertEquals(TwoPaneSplit(DEFAULT_LEADING, DEFAULT_GAP), split)
    }

    private fun split(hinge: VerticalHinge?, layoutDirection: LayoutDirection = LayoutDirection.Ltr) =
        twoPaneSplit(
            availableWidth = AVAILABLE,
            defaultLeadingWidth = DEFAULT_LEADING,
            defaultGap = DEFAULT_GAP,
            minPaneWidth = MIN_PANE,
            hinge = hinge,
            layoutDirection = layoutDirection,
        )
}

/** A 7.6" fold unfolded in landscape, roughly: ~1080dp of width with the crease near the middle. */
private val AVAILABLE = 1080.dp
private val DEFAULT_LEADING = 380.dp
private val DEFAULT_GAP = 16.dp
private val MIN_PANE = 280.dp
