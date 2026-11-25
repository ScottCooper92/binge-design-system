package com.binge.designsystem.component

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.unit.Velocity
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

/** One drag, one job: a drag that scrolled the sheet's content never moves the sheet, and one that began at the edge does. */
class SheetScrollHandoffTest {
    private val handoff = SheetScrollHandoff()
    private val drag = NestedScrollSource.UserInput

    @Test
    fun `a drag that scrolled the content keeps its leftover from the sheet`() {
        handoff.onPostScroll(consumed = Offset(0f, 40f), available = Offset.Zero, source = drag)

        val passed = handoff.onPostScroll(consumed = Offset(0f, 10f), available = Offset(0f, 300f), source = drag)

        assertEquals(Offset(0f, 300f), passed)
    }

    @Test
    fun `and its fling`() =
        runTest {
            handoff.onPostScroll(consumed = Offset(0f, 40f), available = Offset.Zero, source = drag)

            handoff.onPreFling(Velocity(0f, 2_000f))

            assertEquals(Velocity(0f, 2_000f), handoff.onPostFling(Velocity.Zero, Velocity(0f, 2_000f)))
        }

    @Test
    fun `a drag that began with the content at its top moves the sheet`() =
        runTest {
            val passed = handoff.onPostScroll(consumed = Offset.Zero, available = Offset(0f, 300f), source = drag)

            assertEquals(Offset.Zero, passed)
            handoff.onPreFling(Velocity(0f, 2_000f))
            assertEquals(Velocity.Zero, handoff.onPostFling(Velocity.Zero, Velocity(0f, 2_000f)))
        }

    @Test
    fun `each drag decides afresh once the last one has ended`() =
        runTest {
            handoff.onPostScroll(consumed = Offset(0f, 40f), available = Offset.Zero, source = drag)
            handoff.onPreFling(Velocity.Zero)

            val passed = handoff.onPostScroll(consumed = Offset.Zero, available = Offset(0f, 300f), source = drag)

            assertEquals(Offset.Zero, passed)
        }

    @Test
    fun `a programmatic scroll is never held back`() {
        val passed = handoff.onPostScroll(consumed = Offset(0f, 40f), available = Offset(0f, 300f), source = NestedScrollSource.SideEffect)

        assertEquals(Offset.Zero, passed)
    }
}
