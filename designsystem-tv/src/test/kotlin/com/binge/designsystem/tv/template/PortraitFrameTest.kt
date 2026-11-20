package com.binge.designsystem.tv.template

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PortraitFrameTest {
    @Test
    fun `a fraction of 1 fills the page height and sits flush right`() {
        val frame = portraitFrame(pageWidth = 1920, pageHeight = 1080, fraction = 1f)
        assertEquals(1080, frame.height)
        assertEquals(720, frame.width)
        assertEquals(1920 - 720, frame.x)
    }

    @Test
    fun `a fraction above 1 grows past the page height and moves the left edge left`() {
        val whole = portraitFrame(1920, 1080, 1f)
        val grown = portraitFrame(1920, 1080, 1.4f)
        assertTrue(grown.height > 1080)
        assertTrue(grown.x < whole.x)
        assertEquals(1920, grown.x + grown.width)
    }
}
