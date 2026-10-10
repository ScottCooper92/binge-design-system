package com.binge.designsystem.component

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import androidx.compose.ui.test.swipeRight
import com.binge.designsystem.theme.BingeExpressiveTheme
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class)
class HeroCarouselMotionTest {
    @get:Rule
    val rule = createComposeRule()

    private val items = listOf(
        HeroItem(id = 1, imageUrl = null, title = "Alpha", rating = 8f),
        HeroItem(id = 2, imageUrl = null, title = "Beta", rating = 8f),
        HeroItem(id = 3, imageUrl = null, title = "Gamma", rating = 8f),
    )

    @Before
    fun holdTheClock() {
        rule.mainClock.autoAdvance = false
    }

    private fun show(reduceMotion: Boolean = false) =
        rule.setContent {
            BingeExpressiveTheme(dynamicColor = false, reduceMotion = reduceMotion) {
                HeroCarousel(items = items, onItemClick = {})
            }
        }

    /**
     * A swipe is a stream of events the drag detector must see one at a time, which needs the clock
     * running. It lasts well under the autoplay interval, so autoplay cannot move the slide meanwhile.
     */
    private fun showWithClockRunning() {
        rule.mainClock.autoAdvance = true
        show()
    }

    private fun assertSlide(title: String) {
        rule.onNodeWithContentDescription("Show $title").assertIsSelected()
    }

    private fun advance(millis: Long) {
        rule.mainClock.advanceTimeBy(millis)
    }

    @Test
    fun `autoplay moves to the next slide after the interval`() {
        show()
        assertSlide("Alpha")

        advance(AUTOPLAY_MS + SLACK_MS)

        assertSlide("Beta")
    }

    @Test
    fun `every slide gets a full interval before autoplay moves on`() {
        show()
        advance(AUTOPLAY_MS + SLACK_MS)
        assertSlide("Beta")

        advance(AUTOPLAY_MS - SLACK_MS)
        assertSlide("Beta")

        advance(2 * SLACK_MS)
        assertSlide("Gamma")
    }

    @Test
    fun `autoplay holds on the current slide under reduce motion`() {
        show(reduceMotion = true)

        advance(3 * AUTOPLAY_MS)

        assertSlide("Alpha")
    }

    @Test
    fun `autoplay waits while a drag is held and resumes when it ends`() {
        show()
        rule.onRoot().performTouchInput {
            down(center)
            moveBy(Offset(-SHORT_DRAG_PX, 0f))
        }
        rule.mainClock.advanceTimeByFrame()

        advance(2 * AUTOPLAY_MS)
        assertSlide("Alpha")

        rule.onRoot().performTouchInput { up() }
        rule.mainClock.advanceTimeByFrame()
        assertSlide("Alpha")

        advance(AUTOPLAY_MS + SLACK_MS)
        assertSlide("Beta")
    }

    @Test
    fun `a second swipe pages from the slide the first one reached`() {
        showWithClockRunning()

        rule.onRoot().performTouchInput { swipeLeft() }
        rule.waitForIdle()
        assertSlide("Beta")

        rule.onRoot().performTouchInput { swipeLeft() }
        rule.waitForIdle()
        assertSlide("Gamma")
    }

    @Test
    fun `swiping past the last slide wraps to the first, and back from the first wraps to the last`() {
        showWithClockRunning()

        rule.onRoot().performTouchInput { swipeRight() }
        rule.waitForIdle()
        assertSlide("Gamma")

        rule.onRoot().performTouchInput { swipeLeft() }
        rule.waitForIdle()
        assertSlide("Alpha")
    }

    private companion object {
        const val AUTOPLAY_MS = 6_000L
        const val SLACK_MS = 100L
        const val SHORT_DRAG_PX = 20f
    }
}
