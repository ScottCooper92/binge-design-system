package com.binge.designsystem.tv.template

import android.content.Context
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.assertIsNotFocused
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isFocused
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.pressKey
import androidx.compose.ui.unit.dp
import androidx.test.core.app.ApplicationProvider
import com.binge.designsystem.ErrorKind
import com.binge.designsystem.testing.createTakeDownKeyboardComposeRule
import com.binge.designsystem.tv.component.TvPosterCell
import com.binge.designsystem.tv.theme.BingeTvTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private const val BAR = "bar"
private const val TRY_AGAIN = "Try again"
private const val STEPPED_FRAMES = 6

/**
 * **A failed query hands focus from Try again to the first title without passing the tab bar.** Pressing Try again removes it as
 * the retry starts, and the titles lay out a frame after they arrive; a held node keeps focus between.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class, qualifiers = "w960dp-h540dp-television-xhdpi")
class TvFilterResultsRetryFocusTest {
    @get:Rule
    val composeTestRule = createTakeDownKeyboardComposeRule()

    private var results: TvGridResults<Int> by mutableStateOf(TvGridResults.Error(ErrorKind.Generic))
    private var retries = 0

    @Test
    fun `Try again hands focus to the first title, a frame at a time, without passing the bar`() {
        setGrid()
        composeTestRule.onNode(hasText(TRY_AGAIN)).assertIsFocused()
        press(Key.DirectionCenter)
        assertEquals(1, retries)
        results = TvGridResults.Loading
        composeTestRule.waitForIdle()

        composeTestRule.mainClock.autoAdvance = false
        results = TvGridResults.Items(count = TITLES, key = { it }, at = { it })
        repeat(STEPPED_FRAMES) {
            composeTestRule.mainClock.advanceTimeByFrame()
            composeTestRule.onAllNodes(isFocused()).assertCountEquals(1)
            composeTestRule.onNodeWithTag(BAR).assertIsNotFocused()
        }
        composeTestRule.mainClock.autoAdvance = true
        composeTestRule.waitForIdle()

        composeTestRule.onAllNodes(isFocused()).assertCountEquals(1)
        composeTestRule.onNode(hasText(TRY_AGAIN)).assertDoesNotExist()
        composeTestRule.onNode(isFocused() and (hasText("Film 0") or hasContentDescription("Film 0"))).assertExists()
    }

    @Test
    fun `a retry that fails again puts focus back on Try again`() {
        setGrid()
        press(Key.DirectionCenter)
        results = TvGridResults.Loading
        composeTestRule.waitForIdle()

        results = TvGridResults.Error(ErrorKind.Generic)
        composeTestRule.waitForIdle()

        composeTestRule.onNode(hasText(TRY_AGAIN)).assertIsFocused()
    }

    @Test
    fun `a failure reads with the wording of its kind`() {
        val resources = ApplicationProvider.getApplicationContext<Context>().resources
        results = TvGridResults.Error(ErrorKind.Network)
        setGrid()
        composeTestRule.onNode(hasText(resources.getString(ErrorKind.Network.messageRes))).assertExists()
        composeTestRule.onNode(hasText(resources.getString(ErrorKind.Generic.messageRes))).assertDoesNotExist()

        results = TvGridResults.Error(ErrorKind.Generic)
        composeTestRule.waitForIdle()

        composeTestRule.onNode(hasText(resources.getString(ErrorKind.Generic.messageRes))).assertExists()
        composeTestRule.onNode(hasText(resources.getString(ErrorKind.Network.messageRes))).assertDoesNotExist()
    }

    private fun press(key: Key) {
        composeTestRule.onNode(isFocused()).performKeyInput { pressKey(key) }
        composeTestRule.waitForIdle()
    }

    private fun setGrid() {
        composeTestRule.setContent {
            BingeTvTheme(reduceMotion = true) {
                val entry = remember { FocusRequester() }
                Column {
                    Box(Modifier.testTag(BAR).size(20.dp).focusable())
                    TvFilterResults(
                        results = results,
                        refreshing = false,
                        cell = { _, id -> TvPosterCell(null, "Film $id", onClick = {}) },
                        copy = TvFilterGridCopy(empty = "Nothing here", stale = "Stale", retry = TRY_AGAIN),
                        listKey = null,
                        entry = entry,
                        entryIndex = 0,
                        onCellFocused = {},
                        onRetry = { retries++ },
                        onRetryStale = {},
                    )
                }
            }
        }
        composeTestRule.waitForIdle()
    }

    private companion object {
        const val TITLES = 12
    }
}
