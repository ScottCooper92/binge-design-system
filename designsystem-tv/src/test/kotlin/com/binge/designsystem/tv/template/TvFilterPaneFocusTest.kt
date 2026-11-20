package com.binge.designsystem.tv.template

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isFocused
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.pressKey
import androidx.compose.ui.test.requestFocus
import androidx.navigationevent.DirectNavigationEventInput
import androidx.navigationevent.NavigationEvent
import androidx.navigationevent.NavigationEventDispatcher
import androidx.navigationevent.NavigationEventDispatcherOwner
import androidx.navigationevent.compose.LocalNavigationEventDispatcherOwner
import com.binge.designsystem.testing.createTakeDownKeyboardComposeRule
import com.binge.designsystem.tv.catalog.FilterFixture
import com.binge.designsystem.tv.catalog.LABEL_CERT
import com.binge.designsystem.tv.catalog.LABEL_COMPANIES
import com.binge.designsystem.tv.catalog.LABEL_CONTENT
import com.binge.designsystem.tv.catalog.LABEL_GENRES
import com.binge.designsystem.tv.catalog.LABEL_MATCH_ALL
import com.binge.designsystem.tv.catalog.LABEL_MAXIMUM
import com.binge.designsystem.tv.catalog.LABEL_MINIMUM
import com.binge.designsystem.tv.catalog.LABEL_PROVIDERS
import com.binge.designsystem.tv.catalog.LABEL_RATING
import com.binge.designsystem.tv.catalog.LABEL_SORT
import com.binge.designsystem.tv.catalog.SampleFilterPane
import com.binge.designsystem.tv.catalog.ThreeGenres
import com.binge.designsystem.tv.testing.LeanbackRule
import com.binge.designsystem.tv.theme.BingeTvTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * The filter pane's D-pad paths, which a baseline cannot show because focus is a parameter there: Back leaving a category in one
 * press, ← going back to the list, focus surviving the services row's swap and Clear, the scale's bounds, and focus on a node on
 * every frame as the pane swaps views. The pane is driven directly, over a state that applies each pick as the host app's view
 * model would.
 *
 * `television` qualifier: a 2D focus search on Robolectric's default screen resolves nothing like a TV panel's geometry.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], qualifiers = "w960dp-h540dp-television-xhdpi", application = android.app.Application::class)
class TvFilterPaneFocusTest {
    @get:Rule
    val composeTestRule = createTakeDownKeyboardComposeRule()

    @get:Rule
    val leanback = LeanbackRule()

    private val dispatcher = NavigationEventDispatcher()
    private val backInput = DirectNavigationEventInput().also { dispatcher.addInput(it) }
    private var state by mutableStateOf(FilterFixture())
    private var watchProviderOpens = 0

    @Test
    fun `Back from the values leaves the category in one press, onto the category`() {
        setPane()
        open(LABEL_GENRES)
        text("Action").assertIsFocused()

        pressBack()

        text(LABEL_GENRES).assertIsFocused()
        composeTestRule.onAllNodesWithText("Action").assertCountEquals(0)
    }

    @Test
    fun `Back from the services values leaves the category in one press`() {
        setPane()
        open(LABEL_PROVIDERS)
        text("Netflix").assertIsFocused()

        pressBack()

        text(LABEL_PROVIDERS).assertIsFocused()
        composeTestRule.onAllNodesWithText("Netflix").assertCountEquals(0)
    }

    @Test
    fun `Back from a value in a category with nothing pinned leaves it in one press`() {
        setPane()
        open(LABEL_SORT)

        pressBack()

        text(LABEL_SORT).assertIsFocused()
        composeTestRule.onAllNodesWithText("Popularity").assertCountEquals(0)
    }

    @Test
    fun `Left from the values goes back to the list on the category it came from`() {
        setPane()
        open(LABEL_GENRES)
        text("Action").assertIsFocused()

        press(Key.DirectionLeft)

        text(LABEL_GENRES).assertIsFocused()
        composeTestRule.onAllNodesWithText("Action").assertCountEquals(0)
    }

    @Test
    fun `focus stays on the services row as it swaps between using the saved services, their count and back`() {
        state = FilterFixture(savedProviders = 2)
        setPane()
        open(LABEL_PROVIDERS)
        press(Key.DirectionUp)
        text("Use my 2 services").assertIsFocused()

        press(Key.DirectionCenter)
        text("2 selected").assertIsFocused()

        press(Key.DirectionCenter)
        text("Use my 2 services").assertIsFocused()
    }

    @Test
    fun `Clear in a multi-select with a switch moves focus to the switch`() {
        state = FilterFixture(genres = ThreeGenres)
        setPane()
        open(LABEL_GENRES)
        press(Key.DirectionUp)
        press(Key.DirectionUp)
        text("3 selected").assertIsFocused()

        press(Key.DirectionCenter)

        text(LABEL_MATCH_ALL).assertIsFocused()
        text("None selected")
    }

    @Test
    fun `the header's Clear moves focus to the first category and empties the filters`() {
        state = FilterFixture(genres = ThreeGenres)
        setPane()
        focusPane()
        press(Key.DirectionUp)
        text("1 filter").assertIsFocused()

        press(Key.DirectionCenter)

        text(LABEL_CONTENT).assertIsFocused()
        text("No filters")
        assertEquals(emptyMap<String, Any>(), state.genres)
    }

    @Test
    fun `OK on the services row with none saved opens Watch services, not a filter`() {
        setPane()
        open(LABEL_PROVIDERS)
        press(Key.DirectionUp)
        text("Add your services").assertIsFocused()

        press(Key.DirectionCenter)

        assertEquals(1, watchProviderOpens)
        assertEquals(emptySet<String>(), state.providers)
    }

    @Test
    fun `Back from the scale leaves the category in one press`() {
        setPane()
        open(LABEL_RATING)
        composeTestRule.onNode(isFocused() and hasText("Any")).assertExists()

        pressBack()

        text(LABEL_RATING).assertIsFocused()
        composeTestRule.onAllNodesWithText(LABEL_MINIMUM).assertCountEquals(0)
    }

    @Test
    fun `Back from the scale leaves in one press while the Maximum is the bound being set`() {
        setPane()
        open(LABEL_RATING)
        press(Key.DirectionUp)
        press(Key.DirectionRight)
        text(LABEL_MAXIMUM).assertIsFocused()
        press(Key.DirectionCenter)
        press(Key.DirectionDown)

        pressBack()

        text(LABEL_RATING).assertIsFocused()
        composeTestRule.onAllNodesWithText(LABEL_MAXIMUM).assertCountEquals(0)
    }

    @Test
    fun `Left from the scale goes back to the list`() {
        setPane()
        open(LABEL_RATING)

        press(Key.DirectionLeft)

        text(LABEL_RATING).assertIsFocused()
        composeTestRule.onAllNodesWithText(LABEL_MINIMUM).assertCountEquals(0)
    }

    @Test
    fun `a pick sets the bound being set, Minimum first`() {
        setPane()
        open(LABEL_RATING)
        repeat(3) { press(Key.DirectionDown) }

        press(Key.DirectionCenter)

        assertEquals(3f, state.ratingFrom)
        assertEquals(null, state.ratingTo)
    }

    @Test
    fun `a Maximum below the Minimum clears the Minimum`() {
        state = FilterFixture(ratingFrom = 7f)
        setPane()
        open(LABEL_RATING)
        moveUpTo(LABEL_MINIMUM)
        press(Key.DirectionRight)
        press(Key.DirectionCenter)
        press(Key.DirectionDown)
        repeat(3) { press(Key.DirectionDown) }

        press(Key.DirectionCenter)

        assertEquals(null, state.ratingFrom)
        assertEquals(3f, state.ratingTo)
    }

    @Test
    fun `content rating is a range over the region's certifications`() {
        setPane()
        open(LABEL_CERT)
        press(Key.DirectionDown)

        press(Key.DirectionCenter)

        assertEquals("G", state.certGte)
        assertEquals(null, state.certLte)
    }

    @Test
    fun `a Maximum picked below an off-scale floor clears the floor`() {
        state = FilterFixture(ratingFrom = 7.5f)
        setPane()
        open(LABEL_RATING)
        press(Key.DirectionUp)
        press(Key.DirectionRight)
        press(Key.DirectionCenter)
        press(Key.DirectionDown)
        repeat(5) { press(Key.DirectionDown) }

        press(Key.DirectionCenter)

        assertEquals(null, state.ratingFrom)
        assertEquals(5f, state.ratingTo)
    }

    @Test
    fun `a rating floor on no step shows its value and still enters the scale on Any`() {
        state = FilterFixture(ratingFrom = 7.5f)
        setPane()
        open(LABEL_RATING)

        text("7.5").assertExists()
        composeTestRule.onNode(isFocused() and hasText("Any")).assertExists()
    }

    @Test
    fun `clearing a seeded company, which removes its category, leaves its view for a category row`() {
        state = FilterFixture(companies = setOf(COMPANY))
        setPane()
        open(LABEL_COMPANIES)
        press(Key.DirectionUp)
        text("1 selected").assertIsFocused()

        press(Key.DirectionCenter)

        composeTestRule.onAllNodesWithText(LABEL_COMPANIES).assertCountEquals(0)
        text(LABEL_CONTENT).assertIsFocused()
    }

    /** The category's rows leave in the same pass that the values compose, so a node must hold focus on every frame between. */
    @Test
    fun `opening a category keeps focus on a node on every frame`() {
        setPane()
        moveToCategory(LABEL_CONTENT)

        composeTestRule.mainClock.autoAdvance = false
        composeTestRule.onNode(isFocused()).performKeyInput { pressKey(Key.DirectionCenter) }
        repeat(STEPPED_FRAMES) {
            composeTestRule.mainClock.advanceTimeByFrame()
            composeTestRule.onAllNodes(isFocused()).assertCountEquals(1)
        }
        composeTestRule.mainClock.autoAdvance = true
        composeTestRule.waitForIdle()

        composeTestRule.onAllNodes(isFocused()).assertCountEquals(1)
    }

    @Test
    fun `Back from a category's values keeps focus on a node on every frame, and ends on the category`() {
        setPane()
        open(LABEL_CONTENT)

        composeTestRule.mainClock.autoAdvance = false
        composeTestRule.runOnUiThread {
            backInput.backStarted(NavigationEvent(touchX = 0f, touchY = 0f, progress = 0f, swipeEdge = NavigationEvent.EDGE_NONE))
            backInput.backCompleted()
        }
        repeat(STEPPED_FRAMES) {
            composeTestRule.mainClock.advanceTimeByFrame()
            composeTestRule.onAllNodes(isFocused()).assertCountEquals(1)
        }
        composeTestRule.mainClock.autoAdvance = true
        composeTestRule.waitForIdle()

        text(LABEL_CONTENT).assertIsFocused()
    }

    /** Walks Up from the scale to the tab named [label], as the D-pad does, since Back no longer steps there. */
    private fun moveUpTo(label: String) {
        repeat(MAX_CATEGORIES) {
            if (composeTestRule.onAllNodes(isFocused() and hasText(label)).fetchSemanticsNodes().isNotEmpty()) return
            press(Key.DirectionUp)
        }
        text(label).assertIsFocused()
    }

    /** Enters the pane, which lands on its entry (the first category) however the request was aimed. */
    private fun focusPane() {
        text(LABEL_CONTENT).requestFocus()
        composeTestRule.waitForIdle()
    }

    /** Walks Down the category list to [label], as the D-pad does, since a request into the pane is routed to its entry. */
    private fun moveToCategory(label: String) {
        focusPane()
        repeat(MAX_CATEGORIES) {
            if (composeTestRule.onAllNodes(isFocused() and hasText(label)).fetchSemanticsNodes().isNotEmpty()) return
            press(Key.DirectionDown)
        }
    }

    /** Opens [category]'s values: walks to it and presses OK. */
    private fun open(category: String) {
        moveToCategory(category)
        press(Key.DirectionCenter)
    }

    private fun text(label: String): SemanticsNodeInteraction = composeTestRule.onNodeWithText(label)

    private fun press(key: Key) {
        composeTestRule.onNode(isFocused()).performKeyInput { pressKey(key) }
        composeTestRule.waitForIdle()
    }

    private fun pressBack() {
        composeTestRule.runOnUiThread {
            backInput.backStarted(NavigationEvent(touchX = 0f, touchY = 0f, progress = 0f, swipeEdge = NavigationEvent.EDGE_NONE))
            backInput.backCompleted()
        }
        composeTestRule.waitForIdle()
    }

    private fun setPane() {
        state = state.copy(savedProviders = state.savedProviders)
        val owner = object : NavigationEventDispatcherOwner {
            override val navigationEventDispatcher: NavigationEventDispatcher = dispatcher
        }
        val entry = FocusRequester()
        composeTestRule.setContent {
            CompositionLocalProvider(LocalNavigationEventDispatcherOwner provides owner) {
                BingeTvTheme {
                    SampleFilterPane(
                        state = state,
                        onState = { state = it },
                        entry = entry,
                        onOpenServices = { watchProviderOpens++ },
                    )
                }
            }
        }
        composeTestRule.waitForIdle()
    }

    private companion object {
        const val MAX_CATEGORIES = 12
        const val STEPPED_FRAMES = 6
        const val COMPANY = "7"
    }
}
