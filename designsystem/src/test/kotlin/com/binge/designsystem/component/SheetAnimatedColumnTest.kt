package com.binge.designsystem.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.Snapshot
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.height
import com.binge.designsystem.testing.advanceFrames
import com.binge.designsystem.testing.createTakeDownComposeRule
import com.binge.designsystem.theme.BingeExpressiveTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * A sheet's content animates its own height: a change of height eases on the theme's spatial spring, so a sheet as tall as
 * its content follows it, and under reduced motion it lands at once with nothing in between. The clock is paused so a frame part-way
 * through the change can be read.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class)
class SheetAnimatedColumnTest {
    @get:Rule
    val rule = createTakeDownComposeRule()

    private var contentHeight by mutableStateOf(SHORT)

    private fun show(reduceMotion: Boolean) {
        contentHeight = SHORT
        rule.mainClock.autoAdvance = false
        rule.setContent {
            BingeExpressiveTheme(dynamicColor = false, reduceMotion = reduceMotion) {
                Box(Modifier.testTag(HOST)) { SheetAnimatedColumn { Spacer(Modifier.height(contentHeight)) } }
            }
        }
        rule.mainClock.advanceTimeByFrame()
    }

    /** Changes the content's height. A paused clock does not deliver the state write by itself, so it is applied by hand. */
    private fun changeContentHeightTo(height: Dp) {
        contentHeight = height
        Snapshot.sendApplyNotifications()
    }

    /** The animation starts the frame after the layout change, so a few frames in is part-way through it. */

    private fun hostHeight(): Dp = rule.onNodeWithTag(HOST).getUnclippedBoundsInRoot().height

    @Test
    fun `a taller content is part-way between the heights a frame into the change, then lands`() {
        show(reduceMotion = false)
        assertEquals(SHORT.value, hostHeight().value, 0.5f)

        changeContentHeightTo(TALL)
        rule.advanceFrames(MIDWAY_FRAMES)
        val midway = hostHeight()
        assertTrue("the height should have started to move, was $midway", midway > SHORT)
        assertTrue("the height should not have arrived, was $midway", midway < TALL)

        rule.mainClock.autoAdvance = true
        rule.waitForIdle()
        assertEquals(TALL.value, hostHeight().value, 0.5f)
    }

    @Test
    fun `reduced motion lands on the new height at once`() {
        show(reduceMotion = true)
        assertEquals(SHORT.value, hostHeight().value, 0.5f)

        changeContentHeightTo(TALL)
        rule.mainClock.advanceTimeByFrame()

        assertEquals(TALL.value, hostHeight().value, 0.5f)
    }

    @Test
    fun `a shorter content eases down too`() {
        show(reduceMotion = false)
        changeContentHeightTo(TALL)
        rule.mainClock.autoAdvance = true
        rule.waitForIdle()
        rule.mainClock.autoAdvance = false

        changeContentHeightTo(SHORT)
        rule.advanceFrames(MIDWAY_FRAMES)
        val midway = hostHeight()

        assertTrue("the height should have started to fall, was $midway", midway < TALL)
        assertTrue("the height should not have arrived, was $midway", midway > SHORT)
    }

    private companion object {
        const val HOST = "host"
        const val MIDWAY_FRAMES = 4
        val SHORT = 100.dp
        val TALL = 300.dp
    }
}
