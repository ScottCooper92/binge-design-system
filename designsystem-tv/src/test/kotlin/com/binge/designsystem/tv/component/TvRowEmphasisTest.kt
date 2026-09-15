package com.binge.designsystem.tv.component

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll

/**
 * [tvRowEmphasis] is the whole of #1331 that is not a colour: the two-input table that used to be one boolean.
 *
 * Four rows, and the two that matter are the pair that differ only in [tvRowEmphasis]' second argument — the
 * described row *with* and *without* focus in its column. Before the third state existed both were the full
 * amber fill, which is the bug, so a regression that dropped `columnHasFocus` back to being ignored would show
 * up as those two agreeing again. The colours themselves are `@Composable` token reads and belong to the
 * screenshot baselines, not here.
 */
class TvRowEmphasisTest {
    @Test
    fun `described row with focus in the column is focused`() {
        assertEquals(
            TvRowEmphasis.Focused,
            tvRowEmphasis(isDescribedRow = true, columnHasFocus = true),
        )
    }

    @Test
    fun `described row with focus elsewhere is current`() {
        assertEquals(
            TvRowEmphasis.Current,
            tvRowEmphasis(isDescribedRow = true, columnHasFocus = false),
        )
    }

    @Test
    fun `any other row rests whatever the column is doing`() {
        assertAll(
            { assertEquals(TvRowEmphasis.Resting, tvRowEmphasis(isDescribedRow = false, columnHasFocus = true)) },
            { assertEquals(TvRowEmphasis.Resting, tvRowEmphasis(isDescribedRow = false, columnHasFocus = false)) },
        )
    }

    @Test
    fun `focused and current differ, which is the bug this replaced`() {
        assertEquals(
            2,
            setOf(
                tvRowEmphasis(isDescribedRow = true, columnHasFocus = true),
                tvRowEmphasis(isDescribedRow = true, columnHasFocus = false),
            ).size,
        )
    }
}
