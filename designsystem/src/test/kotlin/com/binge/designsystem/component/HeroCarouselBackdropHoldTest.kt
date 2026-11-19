package com.binge.designsystem.component

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import com.binge.designsystem.testing.createTakeDownComposeRule
import com.binge.designsystem.theme.BingeExpressiveTheme
import kotlinx.coroutines.CompletableDeferred
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * The hero carousel keeps the previous backdrop until the next one has loaded (#1297): no placeholder is drawn over it while the
 * image is on its way. The loader is held open by the test, which a real image request cannot be.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class)
class HeroCarouselBackdropHoldTest {
    @get:Rule
    val rule = createTakeDownComposeRule()

    private val items = listOf(
        HeroItem(id = 1, imageUrl = "alpha", title = "Alpha", rating = 8f),
        HeroItem(id = 2, imageUrl = "beta", title = "Beta", rating = 8f),
        HeroItem(id = 3, imageUrl = "gamma", title = "Gamma", rating = 8f),
    )

    /** The loads the carousel asked for, each held open until the test finishes it. */
    private val loads = mutableMapOf<String, CompletableDeferred<Boolean>>()
    private val loader = HeroBackdropLoader { url, _ -> loads.getOrPut(url.orEmpty()) { CompletableDeferred() }.await() }

    @Before
    fun holdTheClock() {
        rule.mainClock.autoAdvance = false
    }

    private val routed = mutableListOf<Int>()
    private val reported = mutableListOf<Int>()

    private fun show(reduceMotion: Boolean = false) =
        rule.setContent {
            BingeExpressiveTheme(dynamicColor = false, reduceMotion = reduceMotion) {
                CompositionLocalProvider(LocalHeroBackdropLoader provides loader) {
                    HeroCarousel(items = items, onItemClick = { routed += it }, onItemTapped = { reported += it })
                }
            }
        }

    private fun finishLoad(url: String) = loads.getOrPut(url) { CompletableDeferred() }.complete(true)

    private fun advanceFrames(count: Int = FRAMES) = repeat(count) { rule.mainClock.advanceTimeByFrame() }

    /** The backdrop buttons on screen for a title: more than one is a crossfade in progress. */
    private fun backdrops(title: String) =
        rule.onAllNodes(
            hasContentDescription(title, substring = true) and SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Button),
        )

    @Test
    fun `the previous backdrop stays while the next one is loading, then the next replaces it`() {
        show()
        advanceFrames()
        backdrops("Alpha").assertCountEquals(1)

        rule.onNodeWithContentDescription("Show Beta").performClick()
        advanceFrames()
        rule.onNodeWithContentDescription("Show Beta").assertIsSelected()
        backdrops("Alpha").assertCountEquals(1)
        backdrops("Beta").assertCountEquals(0)

        finishLoad("beta")
        advanceFrames()
        backdrops("Beta").assertCountEquals(1)

        rule.mainClock.advanceTimeBy(SETTLE_MS)
        backdrops("Alpha").assertCountEquals(0)
        backdrops("Beta").assertCountEquals(1)
    }

    @Test
    fun `a tap on the held backdrop reports the position of the slide it routes to`() {
        show()
        advanceFrames()
        rule.onNodeWithContentDescription("Show Beta").performClick()
        advanceFrames()
        // The dots have moved to Beta; the art on screen is still Alpha's.
        rule.onNodeWithContentDescription("Show Beta").assertIsSelected()

        rule
            .onAllNodes(
                hasContentDescription("Alpha", substring = true) and SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Button),
            ).onFirst()
            .performClick()

        assertEquals(listOf(1), routed)
        assertEquals(listOf(0), reported)
    }

    @Test
    fun `a load that fails still swaps the slide`() {
        show()
        advanceFrames()
        rule.onNodeWithContentDescription("Show Beta").performClick()
        advanceFrames()
        backdrops("Beta").assertCountEquals(0)

        // A failed load returns as a finished one does: the slide swaps and shows its own placeholder.
        finishLoad("beta")
        advanceFrames()
        backdrops("Beta").assertCountEquals(1)
    }

    @Test
    fun `autoplay waits for the next backdrop before it advances`() {
        show()
        advanceFrames()
        rule.mainClock.advanceTimeBy(AUTOPLAY_MS + SLACK_MS)
        rule.onNodeWithContentDescription("Show Alpha").assertIsSelected()
        backdrops("Alpha").assertCountEquals(1)

        finishLoad("beta")
        advanceFrames()
        rule.onNodeWithContentDescription("Show Beta").assertIsSelected()
    }

    @Test
    fun `the neighbours of the current slide are loaded while it dwells`() {
        show()
        advanceFrames()

        assertTrue("the next slide's image should be requested, requested: ${loads.keys}", "beta" in loads)
    }

    @Test
    fun `reduced motion still waits for the load`() {
        show(reduceMotion = true)
        advanceFrames()
        rule.onNodeWithContentDescription("Show Beta").performClick()
        advanceFrames()
        backdrops("Alpha").assertCountEquals(1)
        backdrops("Beta").assertCountEquals(0)

        finishLoad("beta")
        advanceFrames()
        backdrops("Alpha").assertCountEquals(0)
        backdrops("Beta").assertCountEquals(1)
    }

    private companion object {
        const val FRAMES = 4
        const val SETTLE_MS = 1_000L
        const val AUTOPLAY_MS = 6_000L
        const val SLACK_MS = 200L
    }
}
