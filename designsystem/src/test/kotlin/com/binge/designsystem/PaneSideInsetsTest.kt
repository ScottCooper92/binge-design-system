package com.binge.designsystem

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.exclude
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

/** [paneSideInsets]: a pane keeps the window's side insets on its outer edges and drops the inner one. */
class PaneSideInsetsTest {
    @Test
    fun `alone in the window both sides are kept`() {
        val insets = paneSideInsets(WINDOW, innerEdge = null)
        assertEquals(LEFT, insets.getLeft(DENSITY, LayoutDirection.Ltr))
        assertEquals(RIGHT, insets.getRight(DENSITY, LayoutDirection.Ltr))
    }

    @Test
    fun `a detail pane drops its start side`() {
        val insets = paneSideInsets(WINDOW, innerEdge = PaneEdge.Start)
        assertEquals(0, insets.getLeft(DENSITY, LayoutDirection.Ltr))
        assertEquals(RIGHT, insets.getRight(DENSITY, LayoutDirection.Ltr))
    }

    @Test
    fun `a list pane drops its end side`() {
        val insets = paneSideInsets(WINDOW, innerEdge = PaneEdge.End)
        assertEquals(LEFT, insets.getLeft(DENSITY, LayoutDirection.Ltr))
        assertEquals(0, insets.getRight(DENSITY, LayoutDirection.Ltr))
    }

    @Test
    fun `a middle pane keeps neither side`() {
        val insets = paneSideInsets(WINDOW, innerEdge = PaneEdge.Both)
        assertEquals(0, insets.getLeft(DENSITY, LayoutDirection.Ltr))
        assertEquals(0, insets.getRight(DENSITY, LayoutDirection.Ltr))
    }

    @Test
    fun `the dropped side mirrors with the layout direction`() {
        // Under RTL the detail pane sits on the physical left, so its start edge, the one facing the
        // list pane, is its physical right.
        val insets = paneSideInsets(WINDOW, innerEdge = PaneEdge.Start)
        assertEquals(LEFT, insets.getLeft(DENSITY, LayoutDirection.Rtl))
        assertEquals(0, insets.getRight(DENSITY, LayoutDirection.Rtl))
    }

    @Test
    fun `top and bottom are never included`() {
        val insets = paneSideInsets(WINDOW, innerEdge = null)
        assertEquals(0, insets.getTop(DENSITY))
        assertEquals(0, insets.getBottom(DENSITY))
    }

    @Test
    fun `insets an ancestor already consumed are not reserved again`() {
        val insets = paneSideInsets(WINDOW, innerEdge = null).exclude(WindowInsets(left = LEFT))
        assertEquals(0, insets.getLeft(DENSITY, LayoutDirection.Ltr))
        assertEquals(RIGHT, insets.getRight(DENSITY, LayoutDirection.Ltr))
    }

    private companion object {
        const val LEFT = 161
        const val RIGHT = 40
        val WINDOW = WindowInsets(left = LEFT, top = 90, right = RIGHT, bottom = 60)
        val DENSITY = Density(1f)
    }
}
