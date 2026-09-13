package com.binge.designsystem.tv.focus

import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.unit.LayoutDirection
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Test

/**
 * The mirror itself, at the one place it is defined.
 *
 * A consumer's surfaces prove they honour it, which is the evidence that matters; this pins the table
 * those surfaces read so a regression names the helper rather than arriving as two unrelated focus
 * failures. The `@Composable` wrappers only read `LocalLayoutDirection` and delegate here, the same
 * split `StartHorizontalGradientTest` tests behind.
 */
class TvDirectionTest {
    @Test
    fun `the start-edge key is left under Ltr and right under Rtl`() {
        assertEquals(Key.DirectionLeft, tvStartDirectionKey(LayoutDirection.Ltr))
        assertEquals(Key.DirectionRight, tvStartDirectionKey(LayoutDirection.Rtl))
    }

    @Test
    fun `the end-edge key is right under Ltr and left under Rtl`() {
        assertEquals(Key.DirectionRight, tvEndDirectionKey(LayoutDirection.Ltr))
        assertEquals(Key.DirectionLeft, tvEndDirectionKey(LayoutDirection.Rtl))
    }

    /**
     * The pair a `moveFocus` is run with: an end-edge trigger presses the end key *and* searches focus that
     * way, so a key mirrored without its search would move focus back the way it came.
     */
    @Test
    fun `the end-edge focus search mirrors alongside its key`() {
        assertEquals(FocusDirection.Right, tvEndFocusDirection(LayoutDirection.Ltr))
        assertEquals(FocusDirection.Left, tvEndFocusDirection(LayoutDirection.Rtl))
    }

    /** Start and end must never resolve to the same key, in either direction — the mirror is a swap, not a shift. */
    @Test
    fun `start and end are opposites in both directions`() {
        LayoutDirection.entries.forEach { direction ->
            assertNotEquals(
                tvStartDirectionKey(direction),
                tvEndDirectionKey(direction),
                "start and end must be distinct keys under $direction",
            )
        }
    }
}
