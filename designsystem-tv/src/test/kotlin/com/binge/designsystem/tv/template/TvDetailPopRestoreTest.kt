package com.binge.designsystem.tv.template

import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.isFocused
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.pressKey
import androidx.compose.ui.unit.dp
import androidx.navigationevent.NavigationEventDispatcher
import androidx.navigationevent.NavigationEventDispatcherOwner
import androidx.navigationevent.compose.LocalNavigationEventDispatcherOwner
import androidx.tv.material3.Text
import com.binge.designsystem.testing.createTakeDownKeyboardComposeRule
import com.binge.designsystem.tv.testing.LeanbackRule
import com.binge.designsystem.tv.testing.MainThreadImageLoaderRule
import com.binge.designsystem.tv.theme.BingeTvTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private const val CAST = "Cast"
private val CAST_MEMBERS = listOf("Ada", "Ben", "Cleo", "Dev")

/**
 * A detail page rebuilt under a page opened from it (a cast member's, the image viewer) comes back expanded on the tab it was
 * left on, with focus on the card the user had stepped to rather than the first. A saveable state holder stands in for
 * the nav entry decorator: hiding the page disposes it, showing it again composes it afresh from the saved state.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class, qualifiers = "w960dp-h540dp-television-xhdpi")
class TvDetailPopRestoreTest {
    @get:Rule
    val composeTestRule = createTakeDownKeyboardComposeRule()

    /** Coil writes painter state off the composition thread, which can kill this run. */
    @get:Rule
    val imageLoaderRule = MainThreadImageLoaderRule()

    @get:Rule
    val leanback = LeanbackRule()

    private val dispatcher = NavigationEventDispatcher()
    private var shown by mutableStateOf(true)
    private val opened = mutableListOf<Int>()

    @Test
    fun `back from a cast member past the first lands on that member`() {
        setPage()
        repeat(DOWNS_TO_CONTENT) { press(Key.DirectionDown) }
        assertSlotFocusedOn("Ada")
        press(Key.DirectionRight)
        press(Key.DirectionRight)
        assertSlotFocusedOn("Cleo")
        press(Key.DirectionCenter)
        assertEquals(listOf(2), opened)

        shown = false
        composeTestRule.waitForIdle()
        shown = true
        composeTestRule.waitForIdle()

        assertSlotFocusedOn("Cleo")
        press(Key.DirectionCenter)
        assertEquals(listOf(2, 2), opened)
    }

    /** Expanded onto the tab (the resting actions are gone), the slot captions the card stepped to, and something holds focus. */
    private fun assertSlotFocusedOn(name: String) {
        composeTestRule.onNode(isFocused()).assertExists()
        composeTestRule.onNodeWithTag(ACTIONS).assertDoesNotExist()
        composeTestRule.onNodeWithText(name).assertExists()
        (CAST_MEMBERS - name).forEach { other -> composeTestRule.onNodeWithText(other).assertDoesNotExist() }
    }

    private fun press(key: Key) {
        composeTestRule.onNode(isFocused()).performKeyInput { pressKey(key) }
        composeTestRule.waitForIdle()
    }

    private fun setPage() {
        val owner = object : NavigationEventDispatcherOwner {
            override val navigationEventDispatcher: NavigationEventDispatcher = dispatcher
        }
        composeTestRule.setContent {
            CompositionLocalProvider(LocalNavigationEventDispatcherOwner provides owner) {
                BingeTvTheme(reduceMotion = true) {
                    val holder = rememberSaveableStateHolder()
                    if (shown) {
                        holder.SaveableStateProvider(PAGE) {
                            val actionsFocus = remember { FocusRequester() }
                            TvTitleDetailPage(
                                title = TITLE,
                                tabs = listOf(
                                    TvDetailTab(CAST, CAST) {
                                        TvDetailSlotCarousel(
                                            items = CAST_MEMBERS,
                                            key = { it },
                                            aspect = 1f,
                                            slotHeight = SLOT,
                                            stripHeight = SLOT,
                                            onClick = { opened += it },
                                            image = {},
                                            caption = { Text(it) },
                                        )
                                    },
                                ),
                                actionsFocus = actionsFocus,
                                hasActions = true,
                            ) {
                                Box(
                                    Modifier
                                        .size(SLOT)
                                        .testTag(ACTIONS)
                                        .focusRequester(actionsFocus)
                                        .focusable(),
                                )
                            }
                        }
                    }
                }
            }
        }
        composeTestRule.waitForIdle()
    }

    private companion object {
        const val PAGE = "page"
        const val ACTIONS = "actions"

        /** Actions to the tabs, the tab to expanding the page onto it, and the open tab to its content. */
        const val DOWNS_TO_CONTENT = 3
        val SLOT = 80.dp
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
