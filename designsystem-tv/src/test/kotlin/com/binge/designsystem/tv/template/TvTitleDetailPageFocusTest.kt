package com.binge.designsystem.tv.template

import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isFocusable
import androidx.compose.ui.test.isFocused
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.pressKey
import androidx.compose.ui.unit.dp
import androidx.navigationevent.DirectNavigationEventInput
import androidx.navigationevent.NavigationEvent
import androidx.navigationevent.NavigationEventDispatcher
import androidx.navigationevent.NavigationEventDispatcherOwner
import androidx.navigationevent.OnBackCompletedFallback
import androidx.navigationevent.compose.LocalNavigationEventDispatcherOwner
import com.binge.designsystem.testing.createTakeDownKeyboardComposeRule
import com.binge.designsystem.tv.testing.LeanbackRule
import com.binge.designsystem.tv.theme.BingeTvTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private const val ACTIONS = "actions"
private const val DETAILS = "Details"
private const val CAST = "Cast"
private const val VIDEOS = "Videos"
private val TABS = listOf(DETAILS, CAST, VIDEOS)

/**
 * The detail page's expand and fold, driven with real keys: ↓ from the actions reaches the tabs, ↓ or OK on a tab expands the page
 * onto it with the tabs moved to the top, ←/→ across the tabs swap the content, and ↑ from the top tabs or Back folds it again
 * with focus back on the tab. Back goes through a navigation-event dispatcher, the one `BackHandler` registers with here.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class, qualifiers = "w960dp-h540dp-television-xhdpi")
class TvTitleDetailPageFocusTest {
    @get:Rule
    val composeTestRule = createTakeDownKeyboardComposeRule()

    @get:Rule
    val leanback = LeanbackRule()

    private var systemBacks = 0
    private val shown = mutableListOf<String>()
    private val dispatcher = NavigationEventDispatcher(OnBackCompletedFallback { systemBacks++ })
    private val backInput = DirectNavigationEventInput().also { dispatcher.addInput(it) }

    @Test
    fun `arrival lands on the actions and down reaches the first tab`() {
        setPage()
        composeTestRule.onNodeWithTag(ACTIONS).assertIsFocused()
        press(Key.DirectionDown)
        composeTestRule.onNode(hasText(DETAILS) and isFocused()).assertExists()
    }

    @Test
    fun `down on a tab expands the page onto it and shows that tab's content`() {
        setPage()
        press(Key.DirectionDown)
        assertContentShown(null)

        press(Key.DirectionDown)

        assertContentShown(DETAILS)
        composeTestRule.onNode(hasText(DETAILS) and isFocused()).assertExists()
    }

    @Test
    fun `OK on a tab expands onto that tab`() {
        setPage()
        press(Key.DirectionDown)
        press(Key.DirectionRight)
        press(Key.DirectionCenter)

        assertContentShown(CAST)
    }

    @Test
    fun `left and right across the tabs swap the open content`() {
        setPage()
        press(Key.DirectionDown)
        press(Key.DirectionDown)
        press(Key.DirectionRight)
        assertContentShown(CAST)
        press(Key.DirectionRight)
        assertContentShown(VIDEOS)
        press(Key.DirectionLeft)
        assertContentShown(CAST)
    }

    @Test
    fun `up from the top tabs folds the page and keeps focus on the tab`() {
        setPage()
        press(Key.DirectionDown)
        press(Key.DirectionDown)
        assertContentShown(DETAILS)

        press(Key.DirectionUp)

        assertContentShown(null)
        composeTestRule.onNode(hasText(DETAILS) and isFocused()).assertExists()
    }

    @Test
    fun `back folds an expanded page, and with it folded is left to the system`() {
        setPage()
        press(Key.DirectionDown)
        press(Key.DirectionDown)
        press(Key.DirectionRight)

        pressBack()

        assertContentShown(null)
        composeTestRule.onNode(hasText(CAST) and isFocused()).assertExists()
        assertEquals(0, systemBacks)
        pressBack()
        assertEquals(1, systemBacks)
    }

    @Test
    fun `down from the top tabs reaches the open tab's content`() {
        setPage()
        press(Key.DirectionDown)
        press(Key.DirectionDown)

        press(Key.DirectionDown)

        composeTestRule.onNodeWithTag("content-$DETAILS").assertIsFocused()
    }

    @Test
    fun `a tab is reported once the page is expanded onto it, and not while resting`() {
        setPage()
        press(Key.DirectionDown)
        assertEquals(emptyList<String>(), shown)

        press(Key.DirectionDown)
        assertEquals(listOf(DETAILS), shown)

        press(Key.DirectionRight)
        assertEquals(listOf(DETAILS, CAST), shown)
    }

    private fun assertContentShown(tab: String?) {
        TABS.forEach { name ->
            val nodes = composeTestRule
                .onAllNodes(
                    androidx.compose.ui.test
                        .hasTestTag("content-$name"),
                ).fetchSemanticsNodes()
            assertEquals("content of $name", name == tab, nodes.isNotEmpty())
        }
    }

    private fun press(key: Key) {
        composeTestRule.onNode(isFocusable() and isFocused()).performKeyInput { pressKey(key) }
        composeTestRule.waitForIdle()
    }

    private fun pressBack() {
        composeTestRule.runOnUiThread {
            backInput.backStarted(NavigationEvent(touchX = 0f, touchY = 0f, progress = 0f, swipeEdge = NavigationEvent.EDGE_NONE))
            backInput.backCompleted()
        }
        composeTestRule.waitForIdle()
    }

    @Test
    fun `a page with no tabs composes and rests on the actions`() {
        setPage(tabNames = emptyList())
        composeTestRule.onNodeWithTag(ACTIONS).assertIsFocused()
    }

    @Test
    fun `a tab's label changing under the same key shows the new label`() {
        var suffix by mutableStateOf("")
        setPage(label = { it + suffix })
        suffix = "!"
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText(TABS.first() + "!").assertExists()
    }

    private fun setPage(tabNames: List<String> = TABS, label: (String) -> String = { it }) {
        val owner = object : NavigationEventDispatcherOwner {
            override val navigationEventDispatcher: NavigationEventDispatcher = dispatcher
        }
        composeTestRule.setContent {
            CompositionLocalProvider(LocalNavigationEventDispatcherOwner provides owner) {
                BingeTvTheme {
                    val actionsFocus = androidx.compose.runtime.remember { FocusRequester() }
                    TvTitleDetailPage(
                        title = TITLE,
                        tabs = tabNames.map { name ->
                            TvDetailTab(name, label(name)) { Box(Modifier.size(CONTENT_SIZE).testTag("content-$name").focusable()) }
                        },
                        actionsFocus = actionsFocus,
                        hasActions = true,
                        onTabShown = { shown += it },
                    ) {
                        Box(
                            Modifier
                                .size(CONTENT_SIZE)
                                .testTag(ACTIONS)
                                .focusRequester(actionsFocus)
                                .focusable(),
                        )
                    }
                }
            }
        }
        composeTestRule.waitForIdle()
    }

    private companion object {
        val CONTENT_SIZE = 40.dp
        val TITLE = TvDetailTitle(
            title = "The Long Way Home",
            logoUrl = null,
            backdropUrl = null,
            overview = "A washed-up bounty hunter takes one last job.",
            genres = listOf("Drama"),
            facts = listOf("2024"),
            rating = 8.1f,
            certification = "12A",
        )
    }
}
