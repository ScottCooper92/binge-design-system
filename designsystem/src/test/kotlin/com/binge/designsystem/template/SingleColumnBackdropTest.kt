package com.binge.designsystem.template

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * The image behind a media page's single-column hero: the title's own backdrop, the poster where there is none, and nothing where there
 * is neither. The expanded header gets the backdrop alone, so the poster is never drawn twice.
 */
class SingleColumnBackdropTest {
    @Test
    fun `a backdrop is used when there is one`() {
        assertEquals("backdrop", singleColumnBackdrop(backdropUrl = "backdrop", posterUrl = "poster"))
    }

    @Test
    fun `the poster stands in for a missing backdrop`() {
        assertEquals("poster", singleColumnBackdrop(backdropUrl = null, posterUrl = "poster"))
    }

    @Test
    fun `neither a backdrop nor a poster gives nothing`() {
        assertNull(singleColumnBackdrop(backdropUrl = null, posterUrl = null))
    }
}
