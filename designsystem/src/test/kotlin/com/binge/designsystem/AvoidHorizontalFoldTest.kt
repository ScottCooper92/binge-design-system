package com.binge.designsystem

import android.graphics.Rect
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.height
import androidx.window.layout.FoldingFeature
import androidx.window.testing.layout.TestWindowLayoutInfo
import androidx.window.testing.layout.WindowLayoutInfoPublisherRule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import androidx.window.testing.layout.FoldingFeature as posedFold

/**
 * That [avoidHorizontalFold] really moves a row off a posed tabletop crease, and leaves it alone without one.
 *
 * The arithmetic is [FoldClearancePaddingTest]'s; what needs rendering is the wiring, the node's window
 * position reaching the padding. It proves the modifier against the test fold, not hardware.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class, qualifiers = "w800dp-h1000dp-xhdpi")
class AvoidHorizontalFoldTest {
    private val publisher = WindowLayoutInfoPublisherRule()
    private val compose = createComposeRule()
    private val scroll = ScrollState(initial = 0)

    @get:Rule
    val rules: RuleChain = RuleChain.outerRule(publisher).around(compose)

    @Test
    fun `a row across the crease moves below it and returns when the fold goes`() {
        compose.setContent {
            Box(
                Modifier
                    .offset(y = ROW_TOP)
                    .fillMaxWidth()
                    .avoidHorizontalFold()
                    .height(ROW_HEIGHT)
                    .testTag(ROW),
            )
        }
        compose.waitForIdle()
        assertEquals("no fold, so the row must not move", ROW_TOP, rowTop())

        publisher.overrideWindowLayoutInfo(TestWindowLayoutInfo(listOf(tabletopFold())))
        compose.waitForIdle()

        assertTrue("the row still crosses the crease at ${rowTop()}", rowTop() >= CREASE_BOTTOM)

        publisher.overrideWindowLayoutInfo(TestWindowLayoutInfo(emptyList()))
        compose.waitForIdle()

        assertEquals("the row stayed pushed down after the fold went", ROW_TOP, rowTop())
    }

    @Test
    fun `a row with a fixed outer height settles below the crease instead of oscillating`() {
        compose.setContent {
            Box(
                Modifier
                    .offset(y = FIXED_ROW_TOP)
                    .fillMaxWidth()
                    .height(FIXED_ROW_HEIGHT)
                    .avoidHorizontalFold()
                    .testTag(ROW),
            )
        }
        publisher.overrideWindowLayoutInfo(TestWindowLayoutInfo(listOf(tabletopFold())))
        compose.waitForIdle()
        val settled = rowTop()
        assertTrue("the row still crosses the crease at $settled", settled >= CREASE_BOTTOM)

        repeat(SETTLE_PASSES) {
            compose.mainClock.advanceTimeByFrame()
            compose.waitForIdle()
        }

        assertEquals("the row moved again after it had settled", settled, rowTop())
    }

    @Test
    fun `a row scrolled up across the crease is held below it, then lets go once clear`() {
        compose.setContent {
            Column(Modifier.fillMaxSize().verticalScroll(scroll)) {
                Spacer(Modifier.height(SCROLL_ROW_TOP))
                Box(
                    Modifier
                        .fillMaxWidth()
                        .avoidHorizontalFold()
                        .height(ROW_HEIGHT)
                        .testTag(ROW),
                )
                Spacer(Modifier.height(SCROLL_TAIL))
            }
        }
        publisher.overrideWindowLayoutInfo(TestWindowLayoutInfo(listOf(tabletopFold())))
        compose.waitForIdle()
        assertEquals("the row starts below the crease, so it must not move", SCROLL_ROW_TOP, rowTop())

        var scrolled = 0.dp
        repeat(SCROLL_STEPS) {
            scrollBy(SCROLL_STEP)
            scrolled += SCROLL_STEP
            val unpaddedTop = SCROLL_ROW_TOP - scrolled
            val crossesCrease = unpaddedTop < CREASE_BOTTOM && unpaddedTop + ROW_HEIGHT > CREASE_TOP
            val expected = if (crossesCrease) CREASE_BOTTOM else unpaddedTop
            assertEquals("after scrolling $scrolled", expected, rowTop())
        }
        assertEquals("the row never let go of the crease", SCROLL_ROW_TOP - scrolled, rowTop())

        repeat(SCROLL_STEPS) { scrollBy(-SCROLL_STEP) }
        assertEquals("the row did not return after scrolling back", SCROLL_ROW_TOP, rowTop())
    }

    @Test
    fun `a bottom-anchored row across the crease settles instead of growing`() {
        compose.setContent {
            Box(Modifier.fillMaxWidth().height(BOTTOM_PARENT_HEIGHT)) {
                // Two nodes, because two test tags on one node merge into one.
                Box(
                    Modifier
                        .align(Alignment.BottomCenter)
                        .testTag(OUTER)
                        .avoidHorizontalFold(),
                ) {
                    Box(Modifier.fillMaxWidth().height(ROW_HEIGHT).testTag(ROW))
                }
            }
        }
        publisher.overrideWindowLayoutInfo(TestWindowLayoutInfo(listOf(tabletopFold())))
        compose.waitForIdle()
        repeat(SETTLE_PASSES) {
            compose.mainClock.advanceTimeByFrame()
            compose.waitForIdle()
        }

        // Top padding grows a bottom-aligned node upward without moving its content, so none is applied.
        assertEquals("the node grew from its own padding", ROW_HEIGHT, outerHeight())
        assertEquals("the row moved off its bottom anchor", BOTTOM_PARENT_HEIGHT - ROW_HEIGHT, rowTop())
    }

    private fun scrollBy(distance: Dp) {
        compose.runOnIdle { scroll.dispatchRawDelta(with(compose.density) { distance.toPx() }) }
        compose.waitForIdle()
    }

    private fun rowTop() = compose.onNodeWithTag(ROW).getUnclippedBoundsInRoot().top

    private fun outerHeight() = compose.onNodeWithTag(OUTER).getUnclippedBoundsInRoot().height

    private fun tabletopFold(): FoldingFeature =
        posedFold(
            windowBounds = Rect(0, 0, WINDOW_WIDTH_PX, WINDOW_HEIGHT_PX),
            center = CREASE_CENTRE_PX,
            size = CREASE_SIZE_PX,
            state = FoldingFeature.State.HALF_OPENED,
            orientation = FoldingFeature.Orientation.HORIZONTAL,
        )

    private companion object {
        const val ROW = "row"
        const val OUTER = "outer"
        const val WINDOW_WIDTH_PX = 1600
        const val WINDOW_HEIGHT_PX = 2000
        const val CREASE_CENTRE_PX = 1000
        const val CREASE_SIZE_PX = 40

        val CREASE_TOP = 490.dp
        val CREASE_BOTTOM = 510.dp
        val ROW_TOP = 470.dp
        val ROW_HEIGHT = 80.dp
        val FIXED_ROW_TOP = 485.dp
        val FIXED_ROW_HEIGHT = 30.dp
        const val SETTLE_PASSES = 5

        val SCROLL_ROW_TOP = 600.dp
        val SCROLL_TAIL = 2000.dp
        val SCROLL_STEP = 10.dp
        const val SCROLL_STEPS = 35

        // Its bottom-aligned row spans 460dp to 540dp, across the crease.
        val BOTTOM_PARENT_HEIGHT = 540.dp
    }
}
