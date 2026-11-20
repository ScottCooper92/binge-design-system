package com.binge.designsystem.tv.template

import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isFocusable
import androidx.compose.ui.test.isFocused
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.pressKey
import androidx.compose.ui.unit.dp
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
 * The detail page while its title loads: focus is on the actions' primary from the first frame and stays on that node when the
 * title arrives, the tabs are there and focusable, and OK and ↓ on them do nothing until the title is in. Nothing offers focus again.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class, qualifiers = "w960dp-h540dp-television-xhdpi")
class TvDetailPageLoadingFocusTest {
    @get:Rule
    val composeTestRule = createTakeDownKeyboardComposeRule()

    @get:Rule
    val leanback = LeanbackRule()

    private val loaded = mutableStateOf(false)
    private val shown = mutableListOf<String>()

    @Test
    fun `a loading page has focus on the actions from its first frame, and on every frame after`() {
        composeTestRule.mainClock.autoAdvance = false
        setPage(settle = false)

        repeat(FRAMES) {
            composeTestRule.mainClock.advanceTimeByFrame()
            composeTestRule.onNodeWithTag(ACTIONS).assertIsFocused()
        }
        loaded.value = true
        repeat(FRAMES) {
            composeTestRule.mainClock.advanceTimeByFrame()
            composeTestRule.onNodeWithTag(ACTIONS).assertIsFocused()
        }
    }

    @Test
    fun `focus on the actions is the same node before and after the title arrives`() {
        setPage()
        val node = focusedId()

        loaded.value = true
        composeTestRule.waitForIdle()

        assertEquals(node, focusedId())
    }

    @Test
    fun `OK and down on a loading tab do nothing`() {
        setPage()
        press(Key.DirectionDown)
        composeTestRule.onNode(hasText(DETAILS) and isFocused()).assertExists()
        val tab = focusedId()

        press(Key.DirectionCenter)
        press(Key.DirectionDown)

        assertEquals(tab, focusedId())
        assertEquals(emptyList<String>(), shown)
        assertEquals(0, composeTestRule.onAllNodes(hasTestTag("content-$DETAILS")).fetchSemanticsNodes().size)
    }

    @Test
    fun `a tab the user moved to stays the focused node when the title arrives`() {
        setPage()
        press(Key.DirectionDown)
        press(Key.DirectionRight)
        val tab = focusedId()
        composeTestRule.onNode(hasText(CAST) and isFocused()).assertExists()

        loaded.value = true
        composeTestRule.waitForIdle()

        assertEquals(tab, focusedId())
        composeTestRule.onNode(hasText(CAST) and isFocused()).assertExists()
    }

    @Test
    fun `a tab the user is on that the loaded page drops leaves focus on the page`() {
        setPage(loadedTabs = listOf(DETAILS, VIDEOS))
        press(Key.DirectionDown)
        press(Key.DirectionRight)
        composeTestRule.onNode(hasText(CAST) and isFocused()).assertExists()

        loaded.value = true
        composeTestRule.waitForIdle()

        composeTestRule.onAllNodes(isFocused()).assertCountEquals(1)
    }

    @Test
    fun `a page with no actions enters on its tabs`() {
        loaded.value = true
        setPage(hasActions = false)

        composeTestRule.onNode(hasText(DETAILS) and isFocused()).assertExists()
    }

    @Test
    fun `an action row that ends up empty while the user is on its placeholder hands focus to the tabs`() {
        setPage(actionsWhenLoaded = false)
        composeTestRule.onNodeWithTag(ACTIONS).assertIsFocused()

        loaded.value = true
        composeTestRule.waitForIdle()

        composeTestRule.onNode(isFocused() and hasText(DETAILS)).assertExists()
    }

    @Test
    fun `once loaded a tab expands the page as before`() {
        setPage()
        loaded.value = true
        composeTestRule.waitForIdle()

        press(Key.DirectionDown)
        press(Key.DirectionDown)

        assertEquals(listOf(DETAILS), shown)
        composeTestRule.onNodeWithTag("content-$DETAILS").assertExists()
    }

    @Test
    fun `a loaded page forced back to loading keeps focus on the actions`() {
        loaded.value = true
        setPage()
        composeTestRule.onNodeWithTag(ACTIONS).assertIsFocused()
        val node = focusedId()

        loaded.value = false
        composeTestRule.waitForIdle()

        assertEquals(node, focusedId())
    }

    @Test
    fun `a tab the user was on keeps its focus when the page returns to loading`() {
        loaded.value = true
        setPage()
        press(Key.DirectionDown)
        press(Key.DirectionRight)
        val tab = focusedId()

        loaded.value = false
        composeTestRule.waitForIdle()
        assertEquals(tab, focusedId())

        loaded.value = true
        composeTestRule.waitForIdle()
        assertEquals(tab, focusedId())
        composeTestRule.onNode(hasText(CAST) and isFocused()).assertExists()
    }

    @Test
    fun `folding an expanded page leaves focus on the tab it was on, every frame of the way`() {
        loaded.value = true
        setPage()
        press(Key.DirectionDown)
        press(Key.DirectionDown)
        composeTestRule.onNode(hasText(DETAILS) and isFocused()).assertExists()
        composeTestRule.onNodeWithTag("content-$DETAILS").assertExists()
        composeTestRule.mainClock.autoAdvance = false

        composeTestRule.onNode(isFocusable() and isFocused()).performKeyInput { pressKey(Key.DirectionUp) }
        repeat(FRAMES) {
            composeTestRule.mainClock.advanceTimeByFrame()
            composeTestRule.onNode(hasText(DETAILS) and isFocused()).assertExists()
        }
        assertEquals(0, composeTestRule.onAllNodes(hasTestTag("content-$DETAILS")).fetchSemanticsNodes().size)
    }

    private fun focusedId(): Int = composeTestRule.onNode(isFocusable() and isFocused()).fetchSemanticsNode().id

    private fun press(key: Key) {
        composeTestRule.onNode(isFocusable() and isFocused()).performKeyInput { pressKey(key) }
        composeTestRule.waitForIdle()
    }

    private fun setPage(
        hasActions: Boolean = true,
        settle: Boolean = true,
        actionsWhenLoaded: Boolean = true,
        loadedTabs: List<String> = TABS,
    ) {
        composeTestRule.setContent {
            BingeTvTheme {
                val actionsFocus = remember { FocusRequester() }
                val isLoaded = loaded.value
                TvTitleDetailPage(
                    title = if (isLoaded) TITLE else TV_DETAIL_LOADING_TITLE,
                    tabs = if (isLoaded) {
                        loadedTabs.map { name ->
                            TvDetailTab(name, name) { Box(Modifier.size(CONTENT_SIZE).testTag("content-$name").focusable()) }
                        }
                    } else {
                        tvDetailLoadingTabs(*TABS.map { it to it }.toTypedArray())
                    },
                    actionsFocus = actionsFocus,
                    hasActions = isLoaded && hasActions,
                    onTabShown = { shown += it },
                    loading = TvDetailLoading.takeIf { !isLoaded },
                ) {
                    if (!isLoaded || actionsWhenLoaded) {
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
        if (settle) composeTestRule.waitForIdle()
    }

    private companion object {
        const val FRAMES = 30
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
