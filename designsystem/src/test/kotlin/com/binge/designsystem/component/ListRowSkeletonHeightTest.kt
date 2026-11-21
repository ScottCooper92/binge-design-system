package com.binge.designsystem.component

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * The height a row skeleton takes from its font-scale points: the point's own height at a scale it names, a straight line between
 * two points, the nearest point's height beyond the ends, and the plain height when there are none.
 */
class ListRowSkeletonHeightTest {
    private val points =
        listOf(
            FontScaleHeight(1f, 152.dp),
            FontScaleHeight(1.3f, 181.dp),
            FontScaleHeight(2f, 307.dp),
        )

    private fun at(fontScale: Float, of: List<FontScaleHeight> = points): Dp = resolveRowHeight(of, fontScale, FALLBACK)

    @Test
    fun `a scale a point names gets that point's height`() {
        assertEquals(152f, at(1f).value, TOLERANCE)
        assertEquals(181f, at(1.3f).value, TOLERANCE)
        assertEquals(307f, at(2f).value, TOLERANCE)
    }

    @Test
    fun `between two points the height is a straight line`() {
        assertEquals(166.5f, at(1.15f).value, TOLERANCE)
        assertEquals(244f, at(1.65f).value, TOLERANCE)
    }

    @Test
    fun `beyond the first and last point it holds the nearest height`() {
        assertEquals(152f, at(0.85f).value, TOLERANCE)
        assertEquals(307f, at(3f).value, TOLERANCE)
    }

    @Test
    fun `points given out of order are sorted`() {
        assertEquals(181f, at(1.3f, points.reversed()).value, TOLERANCE)
        assertEquals(166.5f, at(1.15f, points.reversed()).value, TOLERANCE)
    }

    @Test
    fun `with no points it is the plain height`() {
        assertEquals(FALLBACK.value, at(1.5f, emptyList()).value, TOLERANCE)
    }

    @Test
    fun `one point holds its height at every scale`() {
        val one = listOf(FontScaleHeight(1.3f, 90.dp))
        assertEquals(90f, at(1f, one).value, TOLERANCE)
        assertEquals(90f, at(2f, one).value, TOLERANCE)
    }

    private companion object {
        val FALLBACK = 40.dp
        const val TOLERANCE = 0.01f
    }
}
