package com.binge.designsystem.tv.template

import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.assertIsNotFocused
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isFocused
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.pressKey
import androidx.compose.ui.test.requestFocus
import androidx.compose.ui.unit.dp
import com.binge.designsystem.ErrorKind
import com.binge.designsystem.testing.createTakeDownKeyboardComposeRule
import com.binge.designsystem.tv.testing.LeanbackRule
import com.binge.designsystem.tv.theme.BingeTvTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private const val BAR = "bar"
private const val STEPPED_FRAMES = 6

/**
 * The see-all grid loads in place: focus on a loading plate is on the same node when the titles arrive. A failure or an empty
 * result collapses it to a message plate and moves focus to its button (or to the plate, with no action).
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class, qualifiers = "w960dp-h540dp-television-xhdpi")
class TvGridPageInPlaceFocusTest {
    @get:Rule
    val composeTestRule = createTakeDownKeyboardComposeRule()

    @get:Rule
    val leanback = LeanbackRule()

    private var results by mutableStateOf<TvGridResults<GridItem>>(TvGridResults.Loading)
    private val entry = FocusRequester()
    private var retries = 0
    private var cleared = 0
    private var backs = 0

    @Test
    fun `focus on a loading plate is on the same node when the titles arrive`() {
        setPane()
        enterFromBar()
        val plate = focusedId()

        results = gridItems(ITEMS)
        composeTestRule.waitForIdle()

        assertEquals(plate, focusedId())
    }

    @Test
    fun `a failure collapses the grid and focus moves to Try again, and OK retries`() {
        results = gridItems(ITEMS)
        setPane()
        enterFromBar()

        results = TvGridResults.Error(ErrorKind.Network)
        composeTestRule.waitForIdle()

        composeTestRule.onNode(hasText("Try again")).assertIsFocused()
        press(Key.DirectionCenter)
        assertEquals(1, retries)
    }

    @Test
    fun `retrying from the collapsed plate puts focus back in the grid`() {
        results = TvGridResults.Error(ErrorKind.Network)
        setPane()
        composeTestRule.onNode(hasText("Try again")).assertIsFocused()

        press(Key.DirectionCenter)
        results = TvGridResults.Loading
        composeTestRule.waitForIdle()

        composeTestRule.onNode(hasText("Try again")).assertDoesNotExist()
        composeTestRule.onNode(isFocused()).assertExists()
    }

    /**
     * The device's order: Try again takes its message, and the focus on it, away, Compose puts focus on whatever is nearest
     * (here the bar), and the retry's titles arrive after. The grid takes focus when they do. The test makes the
     * reassignment, as Robolectric does not always.
     */
    @Test
    fun `titles that arrive after focus fell elsewhere take it back`() {
        results = TvGridResults.Error(ErrorKind.Network)
        setPane()
        press(Key.DirectionCenter)
        results = TvGridResults.Loading
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag(BAR).requestFocus()
        composeTestRule.waitForIdle()

        results = gridItems(ITEMS)
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag(BAR).assertIsNotFocused()
        composeTestRule.onNode(isFocused()).assertTextContains("7.5")
    }

    /**
     * Try again takes its message away as the retry starts, and the titles lay out a frame after they arrive. A held node keeps
     * focus meanwhile, so some node holds it on every frame and it is never the bar.
     */
    @Test
    fun `focus goes from Try again to the first title without passing the bar`() {
        results = TvGridResults.Error(ErrorKind.Network)
        setPane()
        press(Key.DirectionCenter)
        results = TvGridResults.Loading
        composeTestRule.waitForIdle()

        composeTestRule.mainClock.autoAdvance = false
        results = gridItems(ITEMS)
        repeat(STEPPED_FRAMES) {
            composeTestRule.mainClock.advanceTimeByFrame()
            composeTestRule.onAllNodes(isFocused()).assertCountEquals(1)
            composeTestRule.onNodeWithTag(BAR).assertIsNotFocused()
        }
        composeTestRule.mainClock.autoAdvance = true
        composeTestRule.waitForIdle()

        composeTestRule.onNode(isFocused()).assertTextContains("7.5")
    }

    @Test
    fun `a retry that goes straight from Error to titles lands on the first title`() {
        results = TvGridResults.Error(ErrorKind.Network)
        setPane()
        press(Key.DirectionCenter)
        results = gridItems(ITEMS)
        composeTestRule.waitForIdle()

        composeTestRule.onNode(isFocused()).assertTextContains("7.5")
    }

    @Test
    fun `an empty result with an action focuses the action`() {
        results = TvGridResults.Empty
        setPane(emptyAction = TvGridAction("Clear filters") { cleared++ })

        composeTestRule.onNode(hasText("Clear filters")).assertIsFocused()
        press(Key.DirectionCenter)
        assertEquals(1, cleared)
    }

    @Test
    fun `an empty result with no action focuses the plate itself`() {
        results = TvGridResults.Empty
        setPane()

        val plate = composeTestRule
            .onNode(isFocused())
            .assertTextContains("No films here right now.")
            .fetchSemanticsNode()
            .boundsInRoot
        val pane = composeTestRule.onRoot().fetchSemanticsNode().boundsInRoot
        assertTrue(
            "the focus target is the plate, not the pane: $plate in $pane",
            plate.width < pane.width / 2 && plate.height < pane.height / 2,
        )
    }

    @Test
    fun `the entry requester reaches the collapsed button, as a filter overlay closing restores it`() {
        results = TvGridResults.Error(ErrorKind.Network)
        setPane()
        composeTestRule.onNodeWithTag(BAR).requestFocus()
        composeTestRule.waitForIdle()

        composeTestRule.runOnIdle { entry.requestFocus() }
        composeTestRule.waitForIdle()

        composeTestRule.onNode(hasText("Try again")).assertIsFocused()
    }

    @Test
    fun `the entry requester reaches the collapsed plate when there is no action`() {
        results = TvGridResults.Empty
        setPane()
        composeTestRule.onNodeWithTag(BAR).requestFocus()
        composeTestRule.waitForIdle()

        composeTestRule.runOnIdle { entry.requestFocus() }
        composeTestRule.waitForIdle()

        composeTestRule.onNode(isFocused()).assertTextContains("No films here right now.")
    }

    @Test
    fun `Back from the collapsed plate reaches the host`() {
        results = TvGridResults.Error(ErrorKind.Network)
        setPane()

        press(Key.Back)

        assertEquals(1, backs)
    }

    private fun focusedId() = composeTestRule.onNode(isFocused()).fetchSemanticsNode().id

    private fun press(key: Key) {
        composeTestRule.onNode(isFocused()).performKeyInput { pressKey(key) }
        composeTestRule.waitForIdle()
    }

    private fun enterFromBar() {
        composeTestRule.onNodeWithTag(BAR).requestFocus()
        composeTestRule.waitForIdle()
        press(Key.DirectionDown)
    }

    private fun setPane(emptyAction: TvGridAction? = null) {
        composeTestRule.setContent {
            BingeTvTheme(reduceMotion = true) {
                Column(Modifier.onPreviewKeyEvent { e -> (e.key == Key.Back && e.type == KeyEventType.KeyUp).also { if (it) backs++ } }) {
                    androidx.compose.foundation.layout
                        .Box(Modifier.testTag(BAR).size(BAR_SIZE).focusable())
                    TestGridPage(
                        heading = "Horror",
                        results = results,
                        onItemClick = {},
                        onRetry = { retries++ },
                        emptyMessage = "No films here right now.",
                        emptyAction = emptyAction,
                        entryFocusRequester = entry,
                    )
                }
            }
        }
        composeTestRule.waitForIdle()
    }

    private companion object {
        val BAR_SIZE = 20.dp
        val ITEMS = List(12) { GridItem(id = it + 1, title = "Film $it") }
    }
}
