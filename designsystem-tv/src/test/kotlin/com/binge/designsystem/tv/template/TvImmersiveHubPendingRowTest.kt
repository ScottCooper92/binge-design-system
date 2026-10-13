package com.binge.designsystem.tv.template

import android.app.Application
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.requestFocus
import androidx.compose.ui.unit.dp
import com.binge.designsystem.testing.createKeyboardComposeRule
import com.binge.designsystem.tv.focus.tvClickable
import com.binge.designsystem.tv.testing.ANCHOR_COALESCE_WAIT_MILLIS
import com.binge.designsystem.tv.testing.LeanbackRule
import com.binge.designsystem.tv.testing.settle
import com.binge.designsystem.tv.theme.BingeTvTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.math.abs

private const val RAIL = "rail"
private const val PENDING = "pending-row"
private const val ITEMS_PER_ROW = 3
private const val HELD_SLOT_SHIFT_FRACTION = 0.25f

private fun row(key: String, firstId: Int) = TvHubRow(key = key, title = key, items = List(ITEMS_PER_ROW) { firstId + it })

/**
 * A row still loading holds the slot it will fill: [TvImmersiveHub]'s `pendingRow` stands in for it, takes no focus,
 * and the row below it does not move when it resolves. A row arriving where none was held would move the list under a
 * user navigating it by D-pad. ↓ and ↑ cross the pending row to the real rows on either side, and it never claims
 * the hub's focus memory.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class, qualifiers = "w960dp-h540dp-television-xhdpi")
class TvImmersiveHubPendingRowTest {
    @get:Rule(order = 0)
    val leanback = LeanbackRule()

    @get:Rule(order = 1)
    val composeTestRule = createKeyboardComposeRule()

    private lateinit var focusManager: FocusManager

    private var rows by mutableStateOf(
        listOf(row("above", 0), TvHubRow<Int>(key = "middle", title = "middle", items = emptyList(), isLoading = true), row("below", 20)),
    )

    @Test
    fun `the pending row holds a slot and takes no focus`() {
        setHub()
        composeTestRule.onNodeWithTag(PENDING).assertExists()

        enterFromRail()
        composeTestRule.onNodeWithTag("card-0").assertIsFocused()
        move(FocusDirection.Down)
        composeTestRule.onNodeWithTag("card-20").assertIsFocused()
        move(FocusDirection.Up)
        composeTestRule.onNodeWithTag("card-0").assertIsFocused()
    }

    @Test
    fun `an empty row that is not loading is skipped`() {
        rows = listOf(rows[0], rows[1].copy(isLoading = false), rows[2])
        setHub()

        composeTestRule.onNodeWithTag(PENDING).assertDoesNotExist()
    }

    @Test
    fun `resolving fills the held slot rather than moving the row below`() {
        setHub()
        val before = composeTestRule.onNodeWithText("below").getUnclippedBoundsInRoot().top

        rows = listOf(rows[0], row("middle", 10), rows[2])
        composeTestRule.settle(ANCHOR_COALESCE_WAIT_MILLIS)

        val after = composeTestRule.onNodeWithText("below").getUnclippedBoundsInRoot().top
        // A row arriving where none was held moves the one below by a whole row, so a quarter of one tells the two apart.
        val rowPitch = composeTestRule.onNodeWithTag("card-0").getUnclippedBoundsInRoot().bottom -
            composeTestRule.onNodeWithText("above").getUnclippedBoundsInRoot().top
        assertTrue(
            "the row below moved by ${after - before} as the pending row resolved",
            abs((after - before).value) < rowPitch.value * HELD_SLOT_SHIFT_FRACTION,
        )
        assertEquals(1, composeTestRule.onAllNodesWithTagCount("card-10"))
    }

    private fun setHub() {
        composeTestRule.setContent {
            BingeTvTheme {
                focusManager = LocalFocusManager.current
                Row {
                    Box(Modifier.size(48.dp).testTag(RAIL).focusable())
                    TvImmersiveHub(
                        rows = rows,
                        itemId = { it },
                        cardWidth = CARD_WIDTH,
                        onItemClick = {},
                        artwork = {},
                        copy = {},
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                        hosting = TvPageHosting.RailDestination,
                        // As tall as a loaded row here, as the default skeleton is as tall as a poster row.
                        pendingRow = { rowModifier ->
                            Box(rowModifier.fillMaxWidth().height(ROW_HEIGHT)) { Box(Modifier.fillMaxSize().testTag(PENDING)) }
                        },
                    ) { item, _, onFocusChanged, onClick, cellModifier ->
                        Card(item, onFocusChanged, onClick, cellModifier)
                    }
                }
            }
        }
        composeTestRule.onNodeWithTag(RAIL).requestFocus()
        composeTestRule.waitForIdle()
    }

    private fun androidx.compose.ui.test.junit4.ComposeTestRule.onAllNodesWithTagCount(tag: String): Int =
        onAllNodes(
            androidx.compose.ui.test
                .hasTestTag(tag),
        ).fetchSemanticsNodes().size

    private fun enterFromRail() = move(FocusDirection.Right)

    private fun move(direction: FocusDirection) {
        composeTestRule.runOnIdle { focusManager.moveFocus(direction) }
        composeTestRule.waitForIdle()
    }
}

/** Narrow enough that three rows fit the viewport below the copy band, so the row under the pending one is laid out. */
private val CARD_WIDTH = 48.dp

/** A loaded row's height in this fixture: its title, the header gap and a 48dp-wide poster. */
private val ROW_HEIGHT = 100.dp

@Composable
private fun Card(
    item: Int,
    onFocusChanged: (Boolean) -> Unit,
    onClick: () -> Unit,
    modifier: Modifier,
) {
    Box(modifier.aspectRatio(2f / 3f).testTag("card-$item").tvClickable(onFocusChanged = onFocusChanged, onClick = onClick))
}
