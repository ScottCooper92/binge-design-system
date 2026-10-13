package com.binge.designsystem.tv.template

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * How far into a focus-driven surface a visit got.
 *
 * Deepest and never current is the whole contract: a user who walks to the bottom row and back up has
 * still been to the bottom, and a tracker that followed live focus would report the visit as one that
 * never left the top.
 */
class TvVisitDepthTest {
    @Test
    fun `a surface focus never entered has no depth`() {
        assertNull(TvVisitDepth().deepest)
    }

    /** Not zero: focus never arriving is different from arriving and moving nothing. */
    @Test
    fun `arriving at the first row is a depth of zero, not nothing`() {
        val depth = TvVisitDepth()

        depth.reach(0)

        assertEquals(0, depth.deepest)
    }

    @Test
    fun `walking back up keeps the deepest row reached`() {
        val depth = TvVisitDepth()

        depth.reach(0)
        depth.reach(3)
        depth.reach(1)

        assertEquals(3, depth.deepest)
    }

    /** A key the caller could not place in its list — it must not become a depth of its own. */
    @Test
    fun `a row that is not in the list is ignored`() {
        val depth = TvVisitDepth()

        depth.reach(2)
        depth.reach(null)

        assertEquals(2, depth.deepest)
    }
}
