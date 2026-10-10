package com.binge.designsystem

import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

/** [paneContentPadding]: only the edge shared with another pane drops to the inner inset. */
class PaneContentPaddingTest {
    @Test
    fun `no inner edge keeps the outer inset on both sides`() {
        val padding = paneContentPadding(innerEdge = null, outer = OUTER, inner = INNER, top = TOP, bottom = BOTTOM)
        assertEquals(OUTER, padding.calculateLeftPadding(LayoutDirection.Ltr))
        assertEquals(OUTER, padding.calculateRightPadding(LayoutDirection.Ltr))
    }

    @Test
    fun `a list pane narrows its end edge only`() {
        val padding = paneContentPadding(innerEdge = PaneEdge.End, outer = OUTER, inner = INNER, top = TOP, bottom = BOTTOM)
        assertEquals(OUTER, padding.calculateLeftPadding(LayoutDirection.Ltr))
        assertEquals(INNER, padding.calculateRightPadding(LayoutDirection.Ltr))
    }

    @Test
    fun `a detail pane narrows its start edge only`() {
        val padding = paneContentPadding(innerEdge = PaneEdge.Start, outer = OUTER, inner = INNER, top = TOP, bottom = BOTTOM)
        assertEquals(INNER, padding.calculateLeftPadding(LayoutDirection.Ltr))
        assertEquals(OUTER, padding.calculateRightPadding(LayoutDirection.Ltr))
    }

    @Test
    fun `a middle pane narrows both edges`() {
        val padding = paneContentPadding(innerEdge = PaneEdge.Both, outer = OUTER, inner = INNER, top = TOP, bottom = BOTTOM)
        assertEquals(INNER, padding.calculateLeftPadding(LayoutDirection.Ltr))
        assertEquals(INNER, padding.calculateRightPadding(LayoutDirection.Ltr))
    }

    @Test
    fun `the narrowed edge mirrors with the layout direction`() {
        // Under RTL the list pane sits on the physical right, so its end edge — the one facing the
        // detail pane — is its physical left.
        val padding = paneContentPadding(innerEdge = PaneEdge.End, outer = OUTER, inner = INNER, top = TOP, bottom = BOTTOM)
        assertEquals(INNER, padding.calculateLeftPadding(LayoutDirection.Rtl))
        assertEquals(OUTER, padding.calculateRightPadding(LayoutDirection.Rtl))
    }

    @Test
    fun `vertical padding passes through untouched`() {
        val padding = paneContentPadding(innerEdge = PaneEdge.Start, outer = OUTER, inner = INNER, top = TOP, bottom = BOTTOM)
        assertEquals(TOP, padding.calculateTopPadding())
        assertEquals(BOTTOM, padding.calculateBottomPadding())
    }

    private companion object {
        val OUTER = 24.dp
        val INNER = 8.dp
        val TOP = 12.dp
        val BOTTOM = 40.dp
    }
}
