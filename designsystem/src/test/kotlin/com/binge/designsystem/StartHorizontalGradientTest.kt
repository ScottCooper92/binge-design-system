package com.binge.designsystem

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.LayoutDirection
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

/**
 * Covers the mirroring behind [startHorizontalGradient]. The composable wrapper only reads
 * `LocalLayoutDirection` and hands off to the function under test here, so the direction the side
 * scrims actually ramp in is decided entirely by these assertions.
 *
 * The expectations are written as the [Brush.horizontalGradient] a reader would have hand-written
 * for that direction, so a wrong mapping shows up as the brush it produced rather than as a
 * rearranged list of floats.
 */
class StartHorizontalGradientTest {
    @Test
    fun `LTR leaves the stops alone`() {
        assertEquals(
            Brush.horizontalGradient(0f to DENSE, HOLD_STOP to MID, 1f to Color.Transparent),
            startHorizontalGradient(LayoutDirection.Ltr, 0f to DENSE, HOLD_STOP to MID, 1f to Color.Transparent),
        )
    }

    @Test
    fun `RTL puts the dense end at the right edge`() {
        assertEquals(
            Brush.horizontalGradient(0f to Color.Transparent, 1f - HOLD_STOP to MID, 1f to DENSE),
            startHorizontalGradient(LayoutDirection.Rtl, 0f to DENSE, HOLD_STOP to MID, 1f to Color.Transparent),
        )
    }

    /**
     * The mirror is its own inverse, which is the property that makes a call site's stop list
     * readable as written: whatever the direction, the same ramp is being described.
     */
    @Test
    fun `mirroring twice is the original`() {
        assertEquals(
            startHorizontalGradient(LayoutDirection.Ltr, 0f to DENSE, HOLD_STOP to MID, 1f to Color.Transparent),
            startHorizontalGradient(
                LayoutDirection.Rtl,
                0f to Color.Transparent,
                1f - HOLD_STOP to MID,
                1f to DENSE,
            ),
        )
    }

    /** A ramp that does not clear by its final stop still mirrors about the same midpoint. */
    @Test
    fun `an interior clear stop mirrors`() {
        assertEquals(
            Brush.horizontalGradient(1f - CLEAR_STOP to Color.Transparent, 1f - HOLD_STOP to MID, 1f to DENSE),
            startHorizontalGradient(
                LayoutDirection.Rtl,
                0f to DENSE,
                HOLD_STOP to MID,
                CLEAR_STOP to Color.Transparent,
            ),
        )
    }

    private companion object {
        val DENSE = Color.Black
        val MID = Color.DarkGray
        const val HOLD_STOP = 0.85f
        const val CLEAR_STOP = 0.7f
    }
}
