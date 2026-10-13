package com.binge.designsystem.tv.template

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll

/**
 * [immersiveClipTopPx] is the whole of the "hide the row above" logic, extracted from the draw phase precisely
 * so it can be pinned here rather than only in a rendered frame. What it decides is invisible to a screenshot —
 * a static baseline captures the *rested* surface, where the answer is always 0, and never the drifted or
 * mid-reveal frames this discriminates.
 *
 * The caller ([listTopClip] / [gridTopClip]) resolves which visible items are the focused target and hands their
 * top offsets here; this maps them to the y to clip above. The three cases below are the three the modifier
 * behaves differently on, and the grid's min-of-row is the robustness the two-writers drift needs.
 */
class TvImmersiveClipTest {
    /**
     * The rested case, and the reason the clip is inert at rest: the focused row sits at the viewport top, so its
     * offset is 0 and [immersiveTopClip] draws everything. This is what keeps the screenshot baselines unchanged.
     */
    @Test
    fun `a row at the viewport top clips nothing`() {
        assertEquals(0f, immersiveClipTopPx(listOf(0)))
    }

    /**
     * The case that matters: a stray `bringIntoView` has drifted the focused row down, so its measured top is
     * the drift, and the clip follows it there — painting the band the drift uncovered as backdrop rather than
     * the row above.
     */
    @Test
    fun `a drifted row clips to its measured top`() {
        assertEquals(37f, immersiveClipTopPx(listOf(37)))
    }

    /**
     * The first frame of a reveal: the newly focused row is not laid out yet, so nothing here is the target and
     * the clip must **hide everything** rather than fall back to showing the row above. Positive infinity is the
     * modifier's hide-all sentinel (it coerces the clip to the full height).
     */
    @Test
    fun `an unmeasured target hides everything`() {
        assertEquals(Float.POSITIVE_INFINITY, immersiveClipTopPx(emptyList()))
    }

    /**
     * A row straddling the viewport top — part of it scrolled above the clip line — clips nothing rather than a
     * negative distance. Over-clipping would eat the row itself; the coerce keeps the clip subtractive only.
     */
    @Test
    fun `a row scrolled partly above the top clips nothing`() {
        assertAll(
            { assertEquals(0f, immersiveClipTopPx(listOf(-5)), "wholly above") },
            { assertEquals(0f, immersiveClipTopPx(listOf(-3, 10)), "straddling") },
        )
    }

    /**
     * The grid feeds every cell of the focused row, which share a top in principle but not always in the same
     * frame — the lead cell can be a pixel behind a sibling mid-scroll. Taking the minimum anchors to the row's
     * true top rather than to whichever cell measured highest, so the clip never leaves a sliver of the row above.
     */
    @Test
    fun `a grid row clips to the highest cell in it`() {
        assertEquals(30f, immersiveClipTopPx(listOf(31, 30, 31)))
    }
}
