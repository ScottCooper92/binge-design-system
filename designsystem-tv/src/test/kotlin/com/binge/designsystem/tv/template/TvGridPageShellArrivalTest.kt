package com.binge.designsystem.tv.template

import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsNotFocused
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.isFocused
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.pressKey
import androidx.compose.ui.unit.dp
import com.binge.designsystem.ErrorKind
import com.binge.designsystem.testing.createTakeDownKeyboardComposeRule
import com.binge.designsystem.tv.focus.LocalTvShellArrival
import com.binge.designsystem.tv.focus.TvShellArrival
import com.binge.designsystem.tv.testing.LeanbackRule
import com.binge.designsystem.tv.theme.BingeTvTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private val ITEMS = List(3) { GridItem(id = 100 + it, title = "Title $it") }

/** A pushed grid page (Library, gallery, collection, people, watch providers) is the arrival's target: the pane claims it. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class, qualifiers = "w960dp-h540dp-television-xhdpi")
class TvGridPageShellArrivalTest {
    @get:Rule
    val composeTestRule = createTakeDownKeyboardComposeRule()

    @get:Rule
    val leanback = LeanbackRule()

    private val traces = mutableListOf<String>()
    private val arrival = TvShellArrival(onTrace = { traces += it })
    private var shown by mutableStateOf(false)
    private var results by mutableStateOf<TvGridResults<GridItem>>(gridItems(ITEMS))
    private var withTrailingAction = false

    private fun setContent() {
        composeTestRule.setContent {
            BingeTvTheme(reduceMotion = true) {
                CompositionLocalProvider(LocalTvShellArrival provides arrival) {
                    if (shown) {
                        TestGridPage(
                            heading = "Library",
                            results = results,
                            onItemClick = {},
                            onRetry = { results = gridItems(ITEMS) },
                            emptyMessage = "Nothing here",
                            trailingAction = if (withTrailingAction) {
                                { Box(Modifier.size(FILTERS_SIZE).testTag(FILTERS).focusable()) }
                            } else {
                                null
                            },
                        )
                    }
                }
            }
        }
        composeTestRule.waitForIdle()
    }

    @Test
    fun `an armed arrival is claimed by the grid pane, which takes focus`() {
        setContent()
        arrival.arm()
        shown = true
        composeTestRule.waitForIdle()

        assertEquals(listOf("arm", "claimed by a target"), traces)
        assertTrue(composeTestRule.onAllNodes(isFocused()).fetchSemanticsNodes().isNotEmpty())
    }

    @Test
    fun `with nothing armed the pane claims nothing`() {
        setContent()
        shown = true
        composeTestRule.waitForIdle()

        assertEquals(listOf("target reached with no arrival pending"), traces)
    }

    @Test
    fun `a collapsed empty grid still claims, so the plate takes focus`() {
        results = TvGridResults.Empty
        setContent()
        arrival.arm()
        shown = true
        composeTestRule.waitForIdle()

        assertEquals(listOf("arm", "claimed by a target"), traces)
        assertTrue(composeTestRule.onAllNodes(isFocused()).fetchSemanticsNodes().isNotEmpty())
    }

    @Test
    fun `an arrival armed while the grid loads waits for it, then lands on the first cell`() {
        withTrailingAction = true
        results = TvGridResults.Loading
        setContent()
        arrival.arm()
        shown = true
        composeTestRule.waitForIdle()
        results = gridItems(ITEMS)
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag(FILTERS).assertIsNotFocused()
        composeTestRule.onNode(isFocused()).assert(hasContentDescription("Title 0"))
    }

    /** The claim is made on the outer box but the request goes to the grid's own entry requester, so a focusable band ahead of the body must not take it. */
    @Test
    fun `with a focusable trailing action the claim lands on the first cell, not on the action`() {
        withTrailingAction = true
        setContent()
        arrival.arm()
        shown = true
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag(FILTERS).assertIsNotFocused()
        composeTestRule.onNode(isFocused()).assert(hasContentDescription("Title 0"))
    }

    @Test
    fun `after a retry from an error the first cell takes focus, not the trailing action`() {
        withTrailingAction = true
        results = TvGridResults.Error(ErrorKind.Network)
        setContent()
        arrival.arm()
        shown = true
        composeTestRule.waitForIdle()
        composeTestRule.onNode(isFocused()).performKeyInput { pressKey(Key.DirectionCenter) }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag(FILTERS).assertIsNotFocused()
        composeTestRule.onNode(isFocused()).assert(hasContentDescription("Title 0"))
    }
}

private const val FILTERS = "filters"
private val FILTERS_SIZE = 48.dp
