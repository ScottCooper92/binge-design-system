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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.isFocused
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.pressKey
import androidx.compose.ui.test.requestFocus
import androidx.compose.ui.unit.dp
import com.binge.designsystem.testing.createKeyboardComposeRule
import com.binge.designsystem.tv.focus.tvClickable
import com.binge.designsystem.tv.testing.LeanbackRule
import com.binge.designsystem.tv.theme.BingeTvTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private const val RAIL = "rail"
private const val COLUMNS = 3
private const val COUNT = 9
private const val FEW = 2

/**
 * The in-place grid's contract: whatever the grid is showing, the cell focus is on is the cell the next state fills, so a
 * plate that has focus keeps it when the items arrive. A message is not drawn in place: it takes the pane.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class, qualifiers = "w960dp-h540dp-television-xhdpi")
class TvImmersiveGridInPlaceTest {
    @get:Rule(order = 0)
    val leanback = LeanbackRule()

    @get:Rule(order = 1)
    val composeTestRule = createKeyboardComposeRule()

    private var content by mutableStateOf<TvImmersiveGridContent<Int>>(TvImmersiveGridContent.Loading)
    private var clicks = 0
    private var leftGrid = 0

    @Test
    fun `focus on a loading plate is on the same node when the items arrive`() {
        setGrid()
        enterFromRail()
        val plate = focusedId()

        content = TvImmersiveGridContent.Items(count = COUNT, key = { it }, at = { it })
        composeTestRule.waitForIdle()

        assertEquals(plate, focusedId())
        composeTestRule.onNodeWithTag("cell-0").assertExists()
    }

    @Test
    fun `an end press on a loading plate moves along the row`() {
        setGrid(onNavigateToEnd = { leftGrid++ })
        enterFromRail()
        val first = focusedId()

        press(Key.DirectionRight)

        assertEquals(0, leftGrid)
        assertTrue(first != focusedId())
    }

    @Test
    fun `a plate focused when its item arrives is where the grid re-enters`() {
        setGrid()
        enterFromRail()
        press(Key.DirectionRight)
        val plate = focusedId()

        content = TvImmersiveGridContent.Items(count = COUNT, key = { it }, at = { it })
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag(RAIL).requestFocus()
        composeTestRule.waitForIdle()
        press(Key.DirectionRight)

        assertEquals(plate, focusedId())
    }

    @Test
    fun `items arriving under the plate focus is on keep focus in the grid`() {
        setGrid()
        enterFromRail()
        press(Key.DirectionDown)
        press(Key.DirectionRight)
        press(Key.DirectionRight)

        content = TvImmersiveGridContent.Items(count = FEW, key = { it }, at = { it })
        composeTestRule.waitForIdle()

        assertTrue(composeTestRule.onAllNodes(isFocused()).fetchSemanticsNodes().isNotEmpty())
    }

    @Test
    fun `a message takes the pane instead of the cells`() {
        content = TvImmersiveGridContent.Items(count = COUNT, key = { it }, at = { it })
        setGrid()
        composeTestRule.onNodeWithTag("cell-0").assertExists()

        content = TvImmersiveGridContent.Message()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("cell-0").assertDoesNotExist()
        composeTestRule.onNodeWithTag("message").assertExists()
    }

    @Test
    fun `a plate does nothing on OK while loading`() {
        setGrid()
        enterFromRail()

        press(Key.DirectionCenter)

        assertEquals(0, clicks)
        assertTrue(composeTestRule.onAllNodes(isFocused()).fetchSemanticsNodes().isNotEmpty())
    }

    private fun focusedId() = composeTestRule.onNode(isFocused()).fetchSemanticsNode().id

    private fun press(key: Key) {
        composeTestRule.onNode(isFocused()).performKeyInput { pressKey(key) }
        composeTestRule.waitForIdle()
    }

    private fun enterFromRail() {
        composeTestRule.onNodeWithTag(RAIL).requestFocus()
        composeTestRule.waitForIdle()
        press(Key.DirectionRight)
    }

    private fun setGrid(onNavigateToEnd: (() -> Unit)? = null) {
        composeTestRule.setContent {
            BingeTvTheme {
                Row {
                    Box(Modifier.size(48.dp).testTag(RAIL).focusable())
                    TvImmersiveGrid(
                        heading = "Heading",
                        content = content,
                        onItemClick = { clicks++ },
                        artwork = {},
                        copy = {},
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                        columns = COLUMNS,
                        hosting = TvPageHosting.Destination,
                        onNavigateToEnd = onNavigateToEnd,
                        message = { Box(Modifier.size(48.dp).testTag("message")) },
                        inPlace = TvImmersiveGridInPlace<Int>(
                            cell = { item, _, onFocusChanged, onClick, cellModifier ->
                                Box(
                                    cellModifier
                                        .height(24.dp)
                                        .testTag(if (item == null) "plate" else "cell-$item")
                                        .tvClickable(onFocusChanged = onFocusChanged, onClick = onClick),
                                )
                            },
                        ),
                    ) { item, _, onFocusChanged, onClick, cellModifier ->
                        Box(
                            cellModifier
                                .height(24.dp)
                                .testTag("cell-$item")
                                .tvClickable(onFocusChanged = onFocusChanged, onClick = onClick),
                        )
                    }
                }
            }
        }
        composeTestRule.waitForIdle()
    }
}
