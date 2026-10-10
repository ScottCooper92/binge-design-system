package com.binge.designsystem.tv.template

import android.app.Application
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
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
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private const val RAIL = "rail"
private const val SEE_ALL = "See all"
private const val ITEMS_PER_ROW = 3
private const val NEW_CARD = 99

private val Rows =
    listOf(
        TvHubRow(key = "first", title = "First", items = List(ITEMS_PER_ROW) { it }),
        TvHubRow(key = "second", title = "Second", items = List(ITEMS_PER_ROW) { 10 + it }, onSeeAll = {}),
    )

/**
 * Focus that leaves the hub for the rail comes back where it left: on the card last focused in the row, or on
 * the row's see-all tile when that held focus. #308: no frame can show it, and the catalog hosts no rail.
 * The row keeps a slot of its own, so the cases a card arrives in pin the hub's own memory, by id (#311).
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class, qualifiers = "w960dp-h540dp-television-xhdpi")
class TvImmersiveHubReentryTest {
    @get:Rule(order = 0)
    val leanback = LeanbackRule()

    @get:Rule(order = 1)
    val composeTestRule = createKeyboardComposeRule()

    private lateinit var focusManager: FocusManager

    private var rows by mutableStateOf(Rows)

    private var itemId: (Int) -> Any = { it }

    @Test
    fun `re-entry from the rail lands on the card last focused`() {
        setHub()
        enterFromRail()
        composeTestRule.onNodeWithTag("card-0").assertIsFocused()

        move(FocusDirection.Right, times = 2)
        composeTestRule.onNodeWithTag("card-2").assertIsFocused()

        composeTestRule.onNodeWithTag(RAIL).requestFocus()
        enterFromRail()
        composeTestRule.onNodeWithTag("card-2").assertIsFocused()
    }

    @Test
    fun `re-entry from the rail lands on the see-all tile when it held focus`() {
        setHub()
        enterFromRail()
        move(FocusDirection.Down)
        composeTestRule.onNodeWithTag("card-10").assertIsFocused()

        move(FocusDirection.Right, times = ITEMS_PER_ROW)
        composeTestRule.onNodeWithText(SEE_ALL).assertIsFocused()

        composeTestRule.onNodeWithTag(RAIL).requestFocus()
        enterFromRail()
        composeTestRule.onNodeWithText(SEE_ALL).assertIsFocused()
    }

    @Test
    fun `re-entry finds the card last focused by its id when a card arrives before it`() {
        setHub()
        enterFromRail()
        move(FocusDirection.Right, times = 2)
        composeTestRule.onNodeWithTag("card-2").assertIsFocused()

        composeTestRule.onNodeWithTag(RAIL).requestFocus()
        // The row remembers a slot, and card-2 is now one slot further on: only the hub's memory knows the card.
        rows = listOf(Rows[0].copy(items = listOf(NEW_CARD) + Rows[0].items), Rows[1])
        composeTestRule.settle(ANCHOR_COALESCE_WAIT_MILLIS)
        enterFromRail()

        composeTestRule.onNodeWithTag("card-2").assertIsFocused()
    }

    @Test
    fun `re-entry lands on the see-all tile when a card arrives in its row`() {
        setHub()
        enterFromRail()
        move(FocusDirection.Down)
        move(FocusDirection.Right, times = ITEMS_PER_ROW)
        composeTestRule.onNodeWithText(SEE_ALL).assertIsFocused()

        composeTestRule.onNodeWithTag(RAIL).requestFocus()
        // The tile's old slot now holds a card, so the row alone would bring focus back to that card.
        rows = listOf(Rows[0], Rows[1].copy(items = Rows[1].items + NEW_CARD))
        composeTestRule.settle(ANCHOR_COALESCE_WAIT_MILLIS)
        enterFromRail()

        composeTestRule.onNodeWithText(SEE_ALL).assertIsFocused()
    }

    @Test
    fun `string ids keep the card last focused when a card arrives before it`() {
        itemId = { "item-$it" }
        `re-entry finds the card last focused by its id when a card arrives before it`()
    }

    @Test
    fun `string ids keep the see-all tile apart from the cards`() {
        itemId = { "item-$it" }
        `re-entry lands on the see-all tile when a card arrives in its row`()
    }

    private fun setHub() {
        composeTestRule.setContent {
            BingeTvTheme {
                focusManager = LocalFocusManager.current
                Row {
                    Box(Modifier.size(48.dp).testTag(RAIL).focusable())
                    TvImmersiveHub(
                        rows = rows,
                        itemId = itemId,
                        cardWidth = 96.dp,
                        onItemClick = {},
                        artwork = {},
                        copy = {},
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                        hosting = TvPageHosting.RailDestination,
                        seeAllLabel = SEE_ALL,
                    ) { item, _, onFocusChanged, onClick, cellModifier ->
                        Card(item, onFocusChanged, onClick, cellModifier)
                    }
                }
            }
        }
        composeTestRule.onNodeWithTag(RAIL).requestFocus()
        composeTestRule.waitForIdle()
    }

    private fun enterFromRail() = move(FocusDirection.Right)

    private fun move(direction: FocusDirection, times: Int = 1) {
        repeat(times) { composeTestRule.runOnIdle { focusManager.moveFocus(direction) } }
        composeTestRule.waitForIdle()
    }
}

@Composable
private fun Card(
    item: Int,
    onFocusChanged: (Boolean) -> Unit,
    onClick: () -> Unit,
    modifier: Modifier,
) {
    Box(modifier.aspectRatio(2f / 3f).testTag("card-$item").tvClickable(onFocusChanged = onFocusChanged, onClick = onClick))
}
