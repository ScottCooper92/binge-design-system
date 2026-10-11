package com.binge.designsystem.tv.template

import android.app.Application
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.assertIsNotFocused
import androidx.compose.ui.test.isFocused
import androidx.compose.ui.test.onNodeWithTag
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
private const val COLUMNS = 3
private const val COUNT = 9
private const val REMEMBERED = 4

/** Long enough for an overlay's arrival effect to have offered focus several frames over. */
private const val ARRIVAL_WAIT_MILLIS = 500L

/**
 * [TvImmersiveGrid]'s focus memory, which no frame can show (#395). Focus that leaves the grid comes back on the
 * cell it left. As an overlay the grid claims focus only once its remembered cell is loaded: a paged source can
 * report a count before that cell's page arrives, and offering focus to a cell not yet composed would time out.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class, qualifiers = "w960dp-h540dp-television-xhdpi")
class TvImmersiveGridFocusTest {
    @get:Rule(order = 0)
    val leanback = LeanbackRule()

    @get:Rule(order = 1)
    val composeTestRule = createKeyboardComposeRule()

    private lateinit var focusManager: FocusManager

    /** Cells below this index are loaded; the rest read as null, as a page still in flight does. */
    private var loaded by mutableIntStateOf(COUNT)

    @Test
    fun `re-entry from the rail lands on the cell last focused`() {
        setGrid(TvPageHosting.RailDestination)
        composeTestRule.onNodeWithTag(RAIL).requestFocus()
        composeTestRule.waitForIdle()

        move(FocusDirection.Right)
        composeTestRule.onNodeWithTag("cell-0").assertIsFocused()
        move(FocusDirection.Right)
        move(FocusDirection.Down)
        composeTestRule.onNodeWithTag("cell-$REMEMBERED").assertIsFocused()

        composeTestRule.onNodeWithTag(RAIL).requestFocus()
        composeTestRule.waitForIdle()
        move(FocusDirection.Right)

        composeTestRule.onNodeWithTag("cell-$REMEMBERED").assertIsFocused()
    }

    @Test
    fun `an overlay claims focus on its remembered cell once that cell is loaded`() {
        loaded = REMEMBERED
        setGrid(TvPageHosting.Overlay, initialFocusedIndex = REMEMBERED)
        composeTestRule.settle(ARRIVAL_WAIT_MILLIS)

        // The cells before the remembered one are composed, but focus waits for the one it is meant for.
        composeTestRule.onNodeWithTag("cell-0").assertIsNotFocused()
        composeTestRule.onNodeWithTag(RAIL).assertIsNotFocused()
        assertEquals(0, composeTestRule.onAllNodes(isFocused()).fetchSemanticsNodes().size)

        loaded = COUNT
        composeTestRule.settle(ARRIVAL_WAIT_MILLIS)

        composeTestRule.onNodeWithTag("cell-$REMEMBERED").assertIsFocused()
    }

    @Test
    fun `an overlay with no cells loaded claims focus on the first once it arrives`() {
        loaded = 0
        setGrid(TvPageHosting.Overlay)
        composeTestRule.settle(ARRIVAL_WAIT_MILLIS)
        assertEquals(0, composeTestRule.onAllNodes(isFocused()).fetchSemanticsNodes().size)

        loaded = COUNT
        composeTestRule.settle(ARRIVAL_WAIT_MILLIS)

        composeTestRule.onNodeWithTag("cell-0").assertIsFocused()
    }

    private fun setGrid(hosting: TvPageHosting, initialFocusedIndex: Int? = null) {
        composeTestRule.setContent {
            BingeTvTheme {
                focusManager = LocalFocusManager.current
                Row {
                    Box(Modifier.size(48.dp).testTag(RAIL).focusable())
                    TvImmersiveGrid(
                        heading = "Heading",
                        count = COUNT,
                        itemAt = { index -> index.takeIf { it < loaded } },
                        itemKey = { it },
                        artwork = {},
                        copy = {},
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                        columns = COLUMNS,
                        hosting = hosting,
                        initialFocusedIndex = initialFocusedIndex,
                    ) { item, _, onFocusChanged, cellModifier ->
                        Box(
                            cellModifier
                                // Short cells, so every row is composed inside the viewport.
                                .height(24.dp)
                                .testTag("cell-$item")
                                .tvClickable(onFocusChanged = onFocusChanged, onClick = {}),
                        )
                    }
                }
            }
        }
    }

    private fun move(direction: FocusDirection) {
        composeTestRule.runOnIdle { focusManager.moveFocus(direction) }
        composeTestRule.waitForIdle()
    }
}
