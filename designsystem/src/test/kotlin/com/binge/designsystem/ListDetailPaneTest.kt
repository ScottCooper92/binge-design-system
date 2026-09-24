package com.binge.designsystem

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

/**
 * [paneShowsBack]'s derivation from stack depth rather than a per-screen marker (#2706, ported from
 * the reference companion's `SectionPanesTest`).
 */
class ListDetailPaneTest {
    @Test
    fun `hidden only when the list pane is beside it and nothing is stacked above the one entry`() {
        assertEquals(false, paneShowsBack(hubBeside = true, paneDepth = 1))
    }

    @Test
    fun `shown once a second entry stacks above the one pushed from the list`() {
        assertEquals(true, paneShowsBack(hubBeside = true, paneDepth = 2))
    }

    @Test
    fun `shown in single pane regardless of depth`() {
        assertEquals(true, paneShowsBack(hubBeside = false, paneDepth = 1))
        assertEquals(true, paneShowsBack(hubBeside = false, paneDepth = 4))
    }

    @Test
    fun `a screen nested under another detail screen keeps its own Back without opting in`() {
        // The case the old LocalIsSinglePaneNav-only rule got wrong: a screen pushed from within
        // another detail screen (paneDepth 2+) rather than from the list pane directly, which used to
        // need its own unconditional-onBack special case to avoid hiding its Back beside the list pane.
        assertEquals(true, paneShowsBack(hubBeside = true, paneDepth = 2))
        assertEquals(true, paneShowsBack(hubBeside = true, paneDepth = 3))
    }
}
