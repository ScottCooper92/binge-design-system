package com.binge.designsystem.tv.template

import android.app.Application
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.assertIsNotFocused
import androidx.compose.ui.test.isFocused
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.pressKey
import androidx.compose.ui.test.requestFocus
import androidx.compose.ui.unit.dp
import com.binge.designsystem.testing.createKeyboardComposeRule
import com.binge.designsystem.tv.focus.tvClickable
import com.binge.designsystem.tv.testing.LeanbackRule
import com.binge.designsystem.tv.testing.settle
import com.binge.designsystem.tv.theme.BingeTvTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private const val RAIL = "rail"
private const val RETRY = "retry"
private const val COLUMNS = 3
private const val COUNT = 9
private const val REMEMBERED = 4
private const val END_OF_FIRST_ROW = 2
private const val SETTLE_MILLIS = 500L

private fun cells(firstId: Int): TvImmersiveGridContent<Int> =
    TvImmersiveGridContent.Items(count = COUNT, key = { firstId + it }, at = { firstId + it })

/**
 * [TvImmersiveGrid]'s states and callbacks, which no frame can show. The focus memory survives a reload of the same
 * cells and retires when the query or the cell under it changes. An end press leaves only from the end edge. A
 * swap from cells to a message keeps focus in the body.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class, qualifiers = "w960dp-h540dp-television-xhdpi")
class TvImmersiveGridStateTest {
    @get:Rule(order = 0)
    val leanback = LeanbackRule()

    @get:Rule(order = 1)
    val composeTestRule = createKeyboardComposeRule()

    private lateinit var focusManager: FocusManager
    private var content by mutableStateOf(cells(firstId = 0))
    private var queryKey by mutableStateOf<Any?>("first")
    private var attached by mutableStateOf(true)
    private val tapped = mutableListOf<Int>()
    private val deepest = mutableListOf<Int?>()
    private var endPresses = 0

    @Test
    fun `a reload of the same cells keeps the memory`() {
        setGrid()
        focusRemembered()

        content = TvImmersiveGridContent.Loading
        composeTestRule.waitForIdle()
        content = cells(firstId = 0)
        reenterFromRail()

        composeTestRule.onNodeWithTag("cell-$REMEMBERED").assertIsFocused()
    }

    @Test
    fun `a new query resets the memory to the first cell`() {
        setGrid()
        focusRemembered()

        queryKey = "second"
        reenterFromRail()

        composeTestRule.onNodeWithTag("cell-0").assertIsFocused()
    }

    @Test
    fun `a different cell under the remembered index retires the memory`() {
        setGrid()
        focusRemembered()

        content = cells(firstId = 100)
        reenterFromRail()

        composeTestRule.onNodeWithTag("cell-100").assertIsFocused()
    }

    @Test
    fun `fewer cells under the same query do not read past the end`() {
        setGrid()
        focusRemembered()

        val shrunk = 2
        content =
            TvImmersiveGridContent.Items(
                count = shrunk,
                key = { it },
                at = { index ->
                    check(index in 0 until shrunk) { "read $index past the end" }
                    index
                },
            )
        composeTestRule.settle(SETTLE_MILLIS)

        composeTestRule.onNodeWithTag("cell-0").assertExists()
    }

    @Test
    fun `an end press leaves from the end edge only`() {
        setGrid()
        enterFromRail()
        composeTestRule.onNodeWithTag("cell-0").performKeyInput { pressKey(Key.DirectionRight) }
        composeTestRule.waitForIdle()
        assertEquals(0, endPresses)

        composeTestRule.onNodeWithTag("cell-1").assertIsFocused().performKeyInput { pressKey(Key.DirectionRight) }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("cell-$END_OF_FIRST_ROW").assertIsFocused().performKeyInput { pressKey(Key.DirectionRight) }
        composeTestRule.waitForIdle()

        assertEquals(1, endPresses)
    }

    @Test
    fun `a click reports its index, and leaving reports the deepest cell`() {
        setGrid()
        focusRemembered()
        composeTestRule.onNodeWithTag("cell-$REMEMBERED").performClick()
        move(FocusDirection.Up)

        attached = false
        composeTestRule.waitForIdle()

        assertEquals(listOf(REMEMBERED), tapped)
        assertEquals(listOf<Int?>(REMEMBERED), deepest)
    }

    @Test
    fun `swapping cells for a message keeps focus in the body`() {
        setGrid()
        focusRemembered()

        content = TvImmersiveGridContent.Message()
        composeTestRule.settle(SETTLE_MILLIS)

        composeTestRule.onNodeWithTag(RETRY).assertIsFocused()
    }

    /**
     * The way back: a retry that works swaps the message for cells while focus is on the message's button. The cells compose
     * during measure, so the one request waits for the entry cell to be placed; with that request gone, focus falls to
     * the rail instead of staying in the body.
     */
    @Test
    fun `swapping a message back for cells keeps focus in the body`() {
        setGrid()
        focusRemembered()
        content = TvImmersiveGridContent.Message()
        composeTestRule.settle(SETTLE_MILLIS)
        composeTestRule.onNodeWithTag(RETRY).assertIsFocused()

        content = cells(firstId = 0)
        composeTestRule.settle(SETTLE_MILLIS)

        composeTestRule.onNodeWithTag(RETRY).assertDoesNotExist()
        composeTestRule.onNodeWithTag(RAIL).assertIsNotFocused()
        composeTestRule.onNode(isFocused() and aCell).assertExists()
    }

    private val aCell =
        SemanticsMatcher("a grid cell") { it.config.getOrNull(SemanticsProperties.TestTag)?.startsWith("cell-") == true }

    private fun setGrid() {
        composeTestRule.setContent {
            BingeTvTheme {
                focusManager = LocalFocusManager.current
                val entry = remember { FocusRequester() }
                Row {
                    Box(Modifier.size(48.dp).testTag(RAIL).focusable())
                    if (attached) {
                        TvImmersiveGrid(
                            heading = "Heading",
                            content = content,
                            onItemClick = {},
                            artwork = {},
                            copy = {},
                            modifier = Modifier.weight(1f).fillMaxHeight(),
                            columns = COLUMNS,
                            hosting = TvPageHosting.Destination,
                            onItemTapped = { tapped += it },
                            onVisitEnded = { deepest += it },
                            onNavigateToEnd = { endPresses++ },
                            entryFocusRequester = entry,
                            queryKey = queryKey,
                            message = {
                                Box(
                                    Modifier
                                        .size(48.dp)
                                        .testTag(RETRY)
                                        .focusRequester(entry)
                                        .focusable(),
                                )
                            },
                        ) { item, _, onFocusChanged, onClick, cellModifier ->
                            Box(
                                cellModifier
                                    // Short cells, so every row is composed inside the viewport.
                                    .height(24.dp)
                                    .testTag("cell-$item")
                                    .tvClickable(onFocusChanged = onFocusChanged, onClick = onClick),
                            )
                        }
                    }
                }
            }
        }
        composeTestRule.onNodeWithTag(RAIL).requestFocus()
        composeTestRule.waitForIdle()
    }

    private fun focusRemembered() {
        enterFromRail()
        move(FocusDirection.Right)
        move(FocusDirection.Down)
        composeTestRule.onNodeWithTag("cell-$REMEMBERED").assertIsFocused()
    }

    private fun reenterFromRail() {
        composeTestRule.onNodeWithTag(RAIL).requestFocus()
        composeTestRule.settle(SETTLE_MILLIS)
        enterFromRail()
    }

    private fun enterFromRail() = move(FocusDirection.Right)

    private fun move(direction: FocusDirection) {
        composeTestRule.runOnIdle { focusManager.moveFocus(direction) }
        composeTestRule.waitForIdle()
    }
}
