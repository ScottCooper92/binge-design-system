package com.binge.designsystem.tv.nav

import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tv
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.assertIsNotFocused
import androidx.compose.ui.test.isFocused
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.pressKey
import androidx.navigationevent.DirectNavigationEventInput
import androidx.navigationevent.NavigationEvent
import androidx.navigationevent.NavigationEventDispatcher
import androidx.navigationevent.NavigationEventDispatcherOwner
import androidx.navigationevent.OnBackCompletedFallback
import androidx.navigationevent.compose.LocalNavigationEventDispatcherOwner
import com.binge.designsystem.testing.createTakeDownKeyboardComposeRule
import com.binge.designsystem.tv.focus.TvArrivalFocusEffect
import com.binge.designsystem.tv.focus.rememberTvArrivalFocus
import com.binge.designsystem.tv.focus.tvArrivalTarget
import com.binge.designsystem.tv.theme.BingeTvTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private const val ARRIVAL = "arrival"
private const val SETTLE_FRAMES = 10

/**
 * **The shell scaffold feeds the arrival effect the user's own moves, so a late arrival never pulls focus off the bar.**
 *
 * A pane whose data gate opens after the user has already pressed a direction toward the bar must not take focus back.
 * Without the scaffold providing `LocalTvUserMoves` nothing is ever noted, and the arrival would claim.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class, qualifiers = "w960dp-h540dp-television-xhdpi")
class TvUserMovesScaffoldTest {
    @get:Rule
    val composeTestRule = createTakeDownKeyboardComposeRule()

    private val gateOpen = mutableStateOf(false)
    private val dispatcher = NavigationEventDispatcher(OnBackCompletedFallback {})
    private val backInput = DirectNavigationEventInput().also { dispatcher.addInput(it) }

    @Test
    fun `a direction press before the gate opens cancels the pane's arrival`() {
        setBar()
        composeTestRule.mainClock.autoAdvance = false
        repeat(SETTLE_FRAMES) { composeTestRule.mainClock.advanceTimeByFrame() }
        composeTestRule.onNode(isFocused()).performKeyInput { pressKey(Key.DirectionUp) }

        gateOpen.value = true
        repeat(SETTLE_FRAMES) { composeTestRule.mainClock.advanceTimeByFrame() }

        composeTestRule.onNodeWithTag(ARRIVAL).assertIsNotFocused()
    }

    @Test
    fun `with no press the gate opening claims the pane's arrival`() {
        setBar()
        composeTestRule.mainClock.autoAdvance = false
        repeat(SETTLE_FRAMES) { composeTestRule.mainClock.advanceTimeByFrame() }

        gateOpen.value = true
        repeat(SETTLE_FRAMES) { composeTestRule.mainClock.advanceTimeByFrame() }

        composeTestRule.onNodeWithTag(ARRIVAL).assertIsFocused()
    }

    @Test
    fun `back to the bar before the gate opens cancels the pane's arrival`() {
        setBar()
        composeTestRule.mainClock.autoAdvance = false
        repeat(SETTLE_FRAMES) { composeTestRule.mainClock.advanceTimeByFrame() }
        composeTestRule.runOnUiThread {
            backInput.backStarted(NavigationEvent(touchX = 0f, touchY = 0f, progress = 0f, swipeEdge = NavigationEvent.EDGE_NONE))
            backInput.backCompleted()
        }
        repeat(SETTLE_FRAMES) { composeTestRule.mainClock.advanceTimeByFrame() }
        composeTestRule.onNodeWithTag(ARRIVAL).assertIsNotFocused()

        gateOpen.value = true
        repeat(SETTLE_FRAMES) { composeTestRule.mainClock.advanceTimeByFrame() }

        composeTestRule.onNodeWithTag(ARRIVAL).assertIsNotFocused()
    }

    private fun setBar() {
        val owner = object : NavigationEventDispatcherOwner {
            override val navigationEventDispatcher: NavigationEventDispatcher = dispatcher
        }
        composeTestRule.setContent {
            CompositionLocalProvider(LocalNavigationEventDispatcherOwner provides owner) {
                BingeTvTheme {
                    val content = remember { FocusRequester() }
                    TvTopBarScaffold(
                        slots = TvTopBarSlots(
                            header = TvNavItem("account", "Account", Icons.Filled.Person),
                            items = listOf(
                                TvNavItem("search", "Search", Icons.Filled.Search),
                                TvNavItem("movies", "Movies", Icons.Filled.Movie),
                                TvNavItem("tv", "TV shows", Icons.Filled.Tv),
                            ),
                            footer = TvNavItem("settings", "Settings", Icons.Filled.Settings),
                        ),
                        selectedKey = "movies",
                        homeKey = "movies",
                        onSelect = {},
                        contentDepth = 1,
                        onGoHome = {},
                        overlayOpen = false,
                        contentFocusRequester = content,
                        iconOnlyKeys = setOf(KEY_SEARCH),
                    ) {
                        val arrival = rememberTvArrivalFocus()
                        TvArrivalFocusEffect(arrival, enabled = gateOpen.value)
                        Box(
                            Modifier
                                .fillMaxSize()
                                .testTag(ARRIVAL)
                                .shellArrivalTarget()
                                .tvArrivalTarget(arrival)
                                .focusable(),
                        )
                    }
                }
            }
        }
        composeTestRule.waitForIdle()
    }
}
