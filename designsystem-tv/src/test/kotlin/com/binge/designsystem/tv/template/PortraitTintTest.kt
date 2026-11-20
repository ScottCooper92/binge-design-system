package com.binge.designsystem.tv.template

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

private const val LUMINANCE_SLACK = 0.002f
private const val SATURATION_SLACK = 0.03f
private const val MAX_LUMINANCE = 0.04f
private const val MAX_SATURATION = 0.5f

class PortraitTintTest {
    private fun assertWithinBounds(color: Color) {
        assertTrue("luminance ${luminanceOf(color)}", luminanceOf(color) <= MAX_LUMINANCE + LUMINANCE_SLACK)
        assertTrue("saturation ${saturationOf(color)}", saturationOf(color) <= MAX_SATURATION + SATURATION_SLACK)
    }

    @Test
    fun `white is darkened to a dark grey`() {
        val tint = clampTint(Color.White)
        assertWithinBounds(tint)
        assertTrue("the tint is not simply black", tint.red > 0f)
    }

    @Test
    fun `a vivid colour is darkened and de-saturated but keeps its hue`() {
        val tint = clampTint(Color(1f, 0.1f, 0.1f))
        assertWithinBounds(tint)
        assertTrue("still red-led: $tint", tint.red > tint.green && tint.red > tint.blue)
    }

    @Test
    fun `a colour already inside the bounds is unchanged`() {
        val dark = Color(0.10f, 0.08f, 0.07f)
        val tint = clampTint(dark)
        assertEquals(dark.red, tint.red, 0.01f)
        assertEquals(dark.green, tint.green, 0.01f)
        assertEquals(dark.blue, tint.blue, 0.01f)
    }

    @Test
    fun `black stays black`() {
        assertEquals(Color.Black.toArgb(), clampTint(Color.Black).toArgb())
    }

    @Test
    fun `an edge of one colour gives that colour's clamped tint`() {
        val warm = 0xFFC8A878.toInt()
        val tint = edgeTint(width = 600, height = 900) { _, _ -> warm }
        val clamped = checkNotNull(tint)
        assertWithinBounds(clamped)
        assertTrue("a warm edge gives a warm tint: $tint", clamped.red > clamped.blue)
    }

    @Test
    fun `only the left edge of the lower half is read`() {
        val tint = edgeTint(width = 600, height = 900) { x, y -> if (x < 36 && y >= 450) 0xFF000000.toInt() else 0xFFFFFFFF.toInt() }
        assertEquals(Color.Black.toArgb(), tint?.toArgb())
    }

    @Test
    fun `an empty image gives no tint`() {
        assertNull(edgeTint(width = 0, height = 900) { _, _ -> 0 })
        assertNull(edgeTint(width = 600, height = 0) { _, _ -> 0 })
    }
}
