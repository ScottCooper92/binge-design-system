package com.binge.designsystem.tv.nav

import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tv
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.assertIsNotFocused
import androidx.compose.ui.test.isFocused
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.pressKey
import androidx.compose.ui.test.requestFocus
import androidx.compose.ui.unit.dp
import androidx.navigationevent.DirectNavigationEventInput
import androidx.navigationevent.NavigationEvent
import androidx.navigationevent.NavigationEventDispatcher
import androidx.navigationevent.NavigationEventDispatcherOwner
import androidx.navigationevent.OnBackCompletedFallback
import androidx.navigationevent.compose.LocalNavigationEventDispatcherOwner
import com.binge.designsystem.testing.createTakeDownKeyboardComposeRule
import com.binge.designsystem.tv.focus.tvShellArrivalTarget
import com.binge.designsystem.tv.theme.BingeTvTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private const val BASE = "base"
private const val LATE = "late"
private const val MOVIES_TAB = "Movies"
private const val SHOWS_TAB = "TV shows"
private val LATE_SIZE = 40.dp

/**
 * The shell's arrival through the real scaffold: a tab switch arms it, a destination whose first focusable attaches later
 * (its data lands after the switch) takes focus, and a direction key pressed in between stops it.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class, qualifiers = "w960dp-h540dp-television-xhdpi")
class TvShellArrivalScaffoldTest {
    @get:Rule
    val composeTestRule = createTakeDownKeyboardComposeRule()

    private val selected = mutableStateOf<Any>(KEY_MOVIES)
    private val lateContent = mutableStateOf(false)
    private val dispatcher = NavigationEventDispatcher(OnBackCompletedFallback {})
    private val backInput = DirectNavigationEventInput().also { dispatcher.addInput(it) }

    @Test
    fun `a first focusable that attaches after a tab switch takes focus`() {
        switchToShows()

        composeTestRule.runOnUiThread { lateContent.value = true }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag(LATE).assertIsFocused()
    }

    @Test
    fun `a direction key pressed after the switch stops the arrival`() {
        switchToShows()
        press(Key.DirectionRight)

        composeTestRule.runOnUiThread { lateContent.value = true }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag(LATE).assertIsNotFocused()
    }

    @Test
    fun `back on a tab off home goes home and keeps focus on the home tab`() {
        switchToShows()
        pressBack()

        composeTestRule.runOnUiThread { lateContent.value = true }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithContentDescription(MOVIES_TAB).assertIsFocused()
        composeTestRule.onNodeWithTag(LATE).assertIsNotFocused()
    }

    private fun pressBack() {
        composeTestRule.runOnUiThread {
            backInput.backStarted(NavigationEvent(touchX = 0f, touchY = 0f, progress = 0f, swipeEdge = NavigationEvent.EDGE_NONE))
            backInput.backCompleted()
        }
        composeTestRule.waitForIdle()
    }

    private fun switchToShows() {
        setBar()
        composeTestRule.onNodeWithContentDescription(MOVIES_TAB).requestFocus()
        composeTestRule.waitForIdle()
        press(Key.DirectionRight)
        composeTestRule.onNodeWithContentDescription(SHOWS_TAB).assertIsFocused()
        press(Key.DirectionCenter)
    }

    private fun press(key: Key) {
        composeTestRule.onNode(isFocused()).performKeyInput { pressKey(key) }
        composeTestRule.waitForIdle()
    }

    private fun setBar() {
        val owner = object : NavigationEventDispatcherOwner {
            override val navigationEventDispatcher: NavigationEventDispatcher = dispatcher
        }
        composeTestRule.setContent {
            CompositionLocalProvider(LocalNavigationEventDispatcherOwner provides owner) {
                BingeTvTheme {
                    val content = remember { FocusRequester() }
                    val late = remember { FocusRequester() }
                    var selectedKey by selected
                    TvTopBarScaffold(
                        slots = TvTopBarSlots(
                            header = TvNavItem(KEY_ACCOUNT, "Account", Icons.Filled.Person),
                            items = listOf(
                                TvNavItem(KEY_SEARCH, "Search", Icons.Filled.Search),
                                TvNavItem(KEY_MOVIES, MOVIES_TAB, Icons.Filled.Movie),
                                TvNavItem(KEY_TV, SHOWS_TAB, Icons.Filled.Tv),
                            ),
                            footer = TvNavItem(KEY_SETTINGS, "Settings", Icons.Filled.Settings),
                        ),
                        selectedKey = selectedKey,
                        homeKey = KEY_MOVIES,
                        onSelect = { selectedKey = it },
                        contentDepth = 1,
                        onGoHome = { selectedKey = KEY_MOVIES },
                        overlayOpen = false,
                        contentFocusRequester = content,
                        iconOnlyKeys = setOf(KEY_SEARCH),
                    ) {
                        Box(
                            Modifier
                                .fillMaxSize()
                                .testTag(BASE)
                                .focusRequester(content)
                                .focusable(),
                        )
                        if (lateContent.value) {
                            Box(
                                Modifier
                                    .size(LATE_SIZE)
                                    .testTag(LATE)
                                    .tvShellArrivalTarget(late)
                                    .focusable(),
                            )
                        }
                    }
                }
            }
        }
        composeTestRule.waitForIdle()
    }
}
