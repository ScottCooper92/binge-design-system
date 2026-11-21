package com.binge.designsystem.tv.component

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.DpRect
import com.binge.designsystem.testing.createTakeDownKeyboardComposeRule
import com.binge.designsystem.tv.template.TvDetailEpisode
import com.binge.designsystem.tv.template.TvDetailEpisodeCard
import com.binge.designsystem.tv.theme.BingeTvTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.math.sqrt

/**
 * The progress bar on a poster and on an episode card sits where it does whether or not the card is focused, and the focus ring never covers it.
 *
 * The ring is drawn over the card's own outer edge, `tv_focus_ring_width` wide, and follows the card's rounded corner. So the bar has to start
 * clear of that band on its start, end and bottom, and clear of the corner's curve where the bar's own corners are. The focused state is the
 * ring drawn, and the bar's bounds are compared across it.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], qualifiers = "w960dp-h540dp-television-xhdpi", application = android.app.Application::class)
class TvProgressBarRingTest {
    @get:Rule
    val composeTestRule = createTakeDownKeyboardComposeRule()

    /** No image loads, so a poster draws its title and the run stays on the test thread. */
    @get:Rule
    val imageLoaderRule: TestRule = NoImagesRule()

    private val entry = FocusRequester()

    private fun DpRect.wide() = right.value - left.value

    private fun bar(): DpRect = composeTestRule.onNodeWithTag(TV_PROGRESS_BAR_TAG, useUnmergedTree = true).getBoundsInRoot()

    private fun card(tag: String): DpRect = composeTestRule.onNodeWithTag(tag).getBoundsInRoot()

    /** The bar's corners against the ring's inner edge: a corner of the bar must lie inside the card's inner rounded rectangle. */
    private fun assertClearOfRing(
        bar: DpRect,
        card: DpRect,
        cornerRadius: Float,
        label: String,
    ) {
        val ring = RING_WIDTH
        assertTrue("$label: the bar starts inside the ring on the start edge", bar.left.value >= card.left.value + ring)
        assertTrue("$label: the bar ends inside the ring on the end edge", bar.right.value <= card.right.value - ring)
        assertTrue("$label: the bar ends above the ring on the bottom edge", bar.bottom.value <= card.bottom.value - ring)
        // The inner edge's corner is a circle of radius (cornerRadius - ring) centred (cornerRadius) from each outer edge.
        val centreX = card.left.value + cornerRadius
        val centreY = card.bottom.value - cornerRadius
        val dx = centreX - bar.left.value
        val dy = bar.bottom.value - centreY
        if (dx > 0f && dy > 0f) {
            val reach = sqrt(dx * dx + dy * dy)
            assertTrue(
                "$label: the bar's bottom-start corner is $reach from the corner's centre, past the ring's inner radius",
                reach <= cornerRadius - ring,
            )
        }
    }

    @Test
    fun `a poster's progress bar is where it was when the card is focused, and clear of the ring`() {
        composeTestRule.setContent {
            BingeTvTheme {
                TvPosterGrid(
                    count = 1,
                    posterAt = { TvPosterCell(imageUrl = null, title = "Film", onClick = {}, progress = 0.5f) },
                    loading = false,
                    entry = remember { entry },
                    entryIndex = 0,
                    listKey = Unit,
                    onCellFocused = {},
                    seededIndex = seededState.value,
                    cellModifier = { Modifier.testTag(CARD) },
                )
            }
        }
        composeTestRule.waitForIdle()
        val resting = bar()
        val restingCard = card(CARD)

        seededState.value = 0
        composeTestRule.waitForIdle()

        assertEquals("the bar's bounds with the ring drawn", resting, bar())
        assertClearOfRing(bar(), card(CARD), POSTER_CORNER, "poster")
        assertClearOfRing(resting, restingCard, POSTER_CORNER, "poster at rest")
    }

    @Test
    fun `an episode card's progress bar is where it was when the card is focused, and clear of the ring`() {
        composeTestRule.setContent {
            BingeTvTheme {
                Box {
                    TvDetailEpisodeCard(
                        episode = episode(),
                        isFocused = focusedState.value,
                        modifier = Modifier.testTag(CARD),
                    )
                }
            }
        }
        composeTestRule.waitForIdle()
        val resting = bar()

        focusedState.value = true
        composeTestRule.waitForIdle()

        assertEquals("the bar's bounds with the ring drawn", resting, bar())
        assertClearOfRing(bar(), card(CARD), EPISODE_CORNER, "episode")
        assertTrue(
            "the bar is drawn",
            composeTestRule
                .onNode(hasTestTag(TV_PROGRESS_BAR_TAG))
                .fetchSemanticsNode()
                .layoutInfo.isPlaced,
        )
    }

    private val seededState = mutableStateOf<Int?>(null)
    private val focusedState = mutableStateOf(false)

    private fun episode() =
        TvDetailEpisode(
            key = "e",
            seasonKey = "s",
            episodeNumber = 1,
            stillUrl = null,
            badge = "1",
            title = "One",
            overview = "",
            progress = 0.5f,
        )

    private companion object {
        const val CARD = "card"
        const val RING_WIDTH = 3f

        /** `BingeShapes.MediaCard`'s corner. */
        const val POSTER_CORNER = 20f

        /** The tv-material medium shape's corner, which the episode card is clipped to. */
        const val EPISODE_CORNER = 12f
    }
}
