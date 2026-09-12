package com.binge.designsystem

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

private val WINDOW = 1000.dp
private val DEFAULT_LEADING = 300.dp
private val DEFAULT_GAP = 16.dp
private val MIN_PANE = 200.dp

/**
 * Translating a window-reported hinge into an inset row's own coordinate space (#2280).
 *
 * [TwoPaneSplitTest] covers where the boundary goes once both are in the same space; this covers
 * getting them into the same space, which is where the two edges of an asymmetric inset can be
 * confused for each other.
 */
class TwoPaneSplitInWindowTest {
    @Test
    fun `no inset leaves the hinge where the window reported it`() {
        val split = split(startInset = 0.dp, endInset = 0.dp, hinge = VerticalHinge(500.dp, 24.dp))

        assertEquals(500.dp, split.leadingWidth)
    }

    @Test
    fun `a symmetric inset shifts the boundary by one edge, not two`() {
        val split = split(startInset = 24.dp, endInset = 24.dp, hinge = VerticalHinge(500.dp, 24.dp))

        // The row starts 24dp in from the window's left edge, so the crease is 24dp nearer in the
        // row's own space. Subtracting the full horizontal padding would double-count it.
        assertEquals(476.dp, split.leadingWidth)
    }

    /**
     * The regression this file exists for. `Scaffold` derives its padding from `safeDrawing`, and in
     * landscape a display cutout or gesture handle lands on one edge only — so the row's start and
     * end insets differ, and only the physical-left one shifts the crease.
     */
    @Test
    fun `an asymmetric inset shifts the boundary by the physical left edge only`() {
        val split = split(startInset = 48.dp, endInset = 0.dp, hinge = VerticalHinge(500.dp, 24.dp))

        assertEquals(452.dp, split.leadingWidth)
    }

    @Test
    fun `an inset on the trailing edge alone does not move the boundary`() {
        val split = split(startInset = 0.dp, endInset = 48.dp, hinge = VerticalHinge(500.dp, 24.dp))

        assertEquals(500.dp, split.leadingWidth)
    }

    /**
     * Under RTL the row's start edge is the physical right, so it is [endInset] that sits against the
     * window's left edge and shifts the crease. Reading [startInset] here — the whole bug — would be
     * wrong by the difference between the two.
     */
    @Test
    fun `under RTL the physical left inset is the end one`() {
        val split =
            split(
                startInset = 0.dp,
                endInset = 48.dp,
                hinge = VerticalHinge(500.dp, 24.dp),
                layoutDirection = LayoutDirection.Rtl,
            )

        // Row width is 1000 - 0 - 48 = 952; the crease sits at 500 - 48 = 452 in row space, and the
        // leading pane reaches back from the physical right: 952 - (452 + 24).
        assertEquals(476.dp, split.leadingWidth)
    }

    @Test
    fun `the inset narrows the available width, so a hinge near the far edge is ignored`() {
        val split = split(startInset = 0.dp, endInset = 340.dp, hinge = VerticalHinge(500.dp, 24.dp))

        // 1000 - 340 leaves a 660dp row, so a crease at 500 gives the trailing pane 136dp — under the
        // floor, and the defaults win. Without the inset in availableWidth this would read as 476dp
        // of trailing pane and be honoured.
        assertEquals(TwoPaneSplit(DEFAULT_LEADING, DEFAULT_GAP), split)
    }

    @Test
    fun `no hinge keeps the defaults whatever the insets`() {
        val split = split(startInset = 48.dp, endInset = 12.dp, hinge = null)

        assertEquals(TwoPaneSplit(DEFAULT_LEADING, DEFAULT_GAP), split)
    }

    private fun split(
        startInset: Dp,
        endInset: Dp,
        hinge: VerticalHinge?,
        layoutDirection: LayoutDirection = LayoutDirection.Ltr,
    ) = twoPaneSplitInWindow(
        windowWidth = WINDOW,
        startInset = startInset,
        endInset = endInset,
        defaultLeadingWidth = DEFAULT_LEADING,
        defaultGap = DEFAULT_GAP,
        minPaneWidth = MIN_PANE,
        hinge = hinge,
        layoutDirection = layoutDirection,
    )
}
