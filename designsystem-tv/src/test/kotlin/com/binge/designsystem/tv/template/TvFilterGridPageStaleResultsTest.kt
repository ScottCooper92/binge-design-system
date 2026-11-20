package com.binge.designsystem.tv.template

import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.assertIsNotFocused
import androidx.compose.ui.test.isFocused
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.pressKey
import androidx.navigationevent.NavigationEventDispatcher
import androidx.navigationevent.NavigationEventDispatcherOwner
import androidx.navigationevent.compose.LocalNavigationEventDispatcherOwner
import androidx.paging.LoadState
import androidx.tv.material3.Text
import com.binge.designsystem.testing.createTakeDownKeyboardComposeRule
import com.binge.designsystem.tv.catalog.FilterFixture
import com.binge.designsystem.tv.catalog.SamplePaneCopy
import com.binge.designsystem.tv.component.TvPosterCell
import com.binge.designsystem.tv.focus.LocalTvShellArrival
import com.binge.designsystem.tv.focus.TvShellArrival
import com.binge.designsystem.tv.focus.TvUserMovesHost
import com.binge.designsystem.tv.theme.BingeTvTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private const val STALE = "Couldn't update these results."
private const val TRY_AGAIN = "Try again"

/** Stands in for the top bar's tab, which the shell composes before the page, so it is where focus lands when nothing claims it. */
private const val BAR = "Discover tab"

/**
 * A refresh that failed with the earlier query's results still showing: the line says so, in place above the grid, and the grid and
 * its focus stay where they were. The results are driven the way a paged source drives them: the refresh state and a stale flag
 * that follows it.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], qualifiers = "w960dp-h540dp-television-xhdpi", application = android.app.Application::class)
class TvFilterGridPageStaleResultsTest {
    @get:Rule
    val composeTestRule = createTakeDownKeyboardComposeRule()

    private val dispatcher = NavigationEventDispatcher()
    private var refresh: LoadState by mutableStateOf(LoadState.Error(RuntimeException("offline")))
    private val ids = List(8) { 900 + it }
    private var retries = 0
    private var reloading by mutableStateOf(false)
    private val opened = mutableListOf<Int>()

    @Test
    fun `the earlier results stay, with the line above them`() {
        setScreen()

        composeTestRule.onNodeWithText(STALE).assertExists()
        composeTestRule.onNodeWithText("Film 0").assertExists()
    }

    @Test
    fun `no line when the refresh succeeded`() {
        refresh = LoadState.NotLoading(endOfPaginationReached = false)
        setScreen()

        composeTestRule.onNodeWithText(STALE).assertDoesNotExist()
    }

    @Test
    fun `entering the grid from the pane lands on a cell, not on Try again`() {
        setScreen()

        repeat(2) { press(Key.DirectionDown) }
        press(Key.DirectionRight)

        composeTestRule.onNodeWithText(TRY_AGAIN).assertIsNotFocused()
        press(Key.DirectionCenter)
        assertEquals(listOf(900), opened)
    }

    @Test
    fun `up from the first row reaches Try again from any column`() {
        setScreen()

        repeat(2) { press(Key.DirectionDown) }
        repeat(3) { press(Key.DirectionRight) }
        press(Key.DirectionRight)
        press(Key.DirectionUp)

        composeTestRule.onNodeWithText(TRY_AGAIN).assertIsFocused()
    }

    @Test
    fun `Try again retries once, the line stays while it loads, and focus lands on the grid when it succeeds`() {
        setScreen()
        focusTryAgain()

        press(Key.DirectionCenter)
        press(Key.DirectionCenter)
        assertEquals(1, retries)
        composeTestRule.onNodeWithText(STALE).assertExists()

        refresh = LoadState.NotLoading(endOfPaginationReached = false)
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText(STALE).assertDoesNotExist()
        composeTestRule.onNode(isFocused()).assertExists()
        press(Key.DirectionCenter)
        assertEquals(1, opened.size)
    }

    @Test
    fun `a stale retry that succeeds puts focus on the first result, not on the pane or the bar`() {
        setScreen(withBar = true)
        focusTryAgain()
        press(Key.DirectionCenter)

        refresh = LoadState.NotLoading(endOfPaginationReached = false)
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText(STALE).assertDoesNotExist()
        composeTestRule.onNodeWithText("Film 0").assertIsFocused()
        composeTestRule.onNodeWithText(BAR).assertIsNotFocused()
    }

    @Test
    fun `a move the user makes while the stale retry loads is not overridden when it succeeds`() {
        setScreen(withBar = true)
        focusTryAgain()
        press(Key.DirectionCenter)
        refresh = LoadState.Loading
        composeTestRule.waitForIdle()

        press(Key.DirectionUp)
        composeTestRule.onNodeWithText(BAR).assertIsFocused()
        refresh = LoadState.NotLoading(endOfPaginationReached = false)
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText(STALE).assertDoesNotExist()
        composeTestRule.onNodeWithText(BAR).assertIsFocused()
    }

    @Test
    fun `a retry that fails again keeps the line, and Try again keeps focus`() {
        setScreen()
        focusTryAgain()
        press(Key.DirectionCenter)

        refresh = LoadState.Error(RuntimeException("still offline"))
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText(STALE).assertExists()
        composeTestRule.onNodeWithText(TRY_AGAIN).assertIsFocused()
    }

    /** Re-selecting the tab reloads the screen through its loading shell, not through Try again; the line must not outlive that. */
    @Test
    fun `the line is gone after a reload through the loading shell that succeeds`() {
        setScreen()
        composeTestRule.onNodeWithText(STALE).assertExists()

        reloading = true
        composeTestRule.waitForIdle()
        refresh = LoadState.NotLoading(endOfPaginationReached = false)
        reloading = false
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText(STALE).assertDoesNotExist()
        composeTestRule.onNodeWithText("Film 0").assertExists()
    }

    /** Into the grid from the filter pane, then ↑ from the first row onto the line's button. */
    private fun focusTryAgain() {
        repeat(2) { press(Key.DirectionDown) }
        press(Key.DirectionRight)
        press(Key.DirectionUp)
        composeTestRule.onNodeWithText(TRY_AGAIN).assertIsFocused()
    }

    private fun press(key: Key) {
        runCatching { composeTestRule.onNode(isFocused()).performKeyInput { pressKey(key) } }
        composeTestRule.waitForIdle()
    }

    private fun setScreen(withBar: Boolean = false) {
        val owner = object : NavigationEventDispatcherOwner {
            override val navigationEventDispatcher: NavigationEventDispatcher = dispatcher
        }
        composeTestRule.setContent {
            CompositionLocalProvider(
                LocalNavigationEventDispatcherOwner provides owner,
                LocalTvShellArrival provides TvShellArrival().also { it.arm() },
            ) {
                BingeTvTheme(reduceMotion = true) {
                    TvUserMovesHost {
                        Column {
                            if (withBar) Text(BAR, modifier = Modifier.focusable())
                            run {
                                TvFilterGridPage(
                                    categories = FilterFixture().categories(),
                                    paneCopy = SamplePaneCopy,
                                    gridCopy = TvFilterGridCopy(empty = "Nothing here", stale = STALE, retry = TRY_AGAIN),
                                    filterCount = 0,
                                    results = TvGridResults.Items(
                                        count = ids.size,
                                        key = { it },
                                        at = { ids.getOrNull(it) },
                                        stale = refresh is LoadState.Error,
                                    ),
                                    cell = { _, id -> TvPosterCell(null, "Film ${id - 900}", onClick = { opened += id }) },
                                    listKey = null,
                                    onClearAll = {},
                                    onToggle = { _, _ -> },
                                    onClearCategory = {},
                                    onRetry = {
                                        retries++
                                        refresh = LoadState.Loading
                                    },
                                    refreshing = refresh is LoadState.Loading,
                                    loadingShell = reloading,
                                )
                            }
                        }
                    }
                }
            }
        }
        composeTestRule.waitForIdle()
    }
}
