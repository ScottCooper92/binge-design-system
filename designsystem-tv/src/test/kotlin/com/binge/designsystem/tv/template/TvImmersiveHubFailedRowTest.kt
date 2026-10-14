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
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.pressKey
import androidx.compose.ui.test.requestFocus
import androidx.compose.ui.unit.dp
import com.binge.designsystem.testing.createKeyboardComposeRule
import com.binge.designsystem.tv.focus.tvClickable
import com.binge.designsystem.tv.testing.ANCHOR_COALESCE_WAIT_MILLIS
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
private const val HERO = "hero-target"
private const val RETRY = "Retry"
private const val MESSAGE = "These could not be loaded."
private const val ITEMS_PER_ROW = 3

private fun row(key: String, firstId: Int) = TvHubRow(key = key, title = key, items = List(ITEMS_PER_ROW) { firstId + it })

/**
 * A row whose items could not be read keeps its slot and says so: its retry is the row's one focus stop, so ↓ and ↑ land on
 * it like any row, entry from the rail lands on it when it is first, and OK retries. A failure on a row with items is ignored.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class, qualifiers = "w960dp-h540dp-television-xhdpi")
class TvImmersiveHubFailedRowTest {
    @get:Rule(order = 0)
    val leanback = LeanbackRule()

    @get:Rule(order = 1)
    val composeTestRule = createKeyboardComposeRule()

    private lateinit var focusManager: FocusManager
    private var retries = 0

    private fun failed(key: String) =
        TvHubRow<Int>(key = key, title = key, items = emptyList(), failure = TvHubRowFailure(MESSAGE, RETRY) { retries++ })

    @Test
    fun `the failed row says why in its slot, and the D-pad stops on its retry`() {
        setHub(listOf(row("above", 0), failed("middle"), row("below", 20)))
        composeTestRule.onNodeWithText(MESSAGE).assertExists()

        move(FocusDirection.Right)
        composeTestRule.onNodeWithTag("card-0").assertIsFocused()
        move(FocusDirection.Down)
        composeTestRule.onNodeWithText(RETRY).assertIsFocused()
        move(FocusDirection.Down)
        composeTestRule.onNodeWithTag("card-20").assertIsFocused()
        move(FocusDirection.Up)
        composeTestRule.onNodeWithText(RETRY).assertIsFocused()
    }

    @Test
    fun `entry from the rail lands on the retry of a failed first row, and OK retries`() {
        setHub(listOf(failed("first"), row("below", 20)))

        move(FocusDirection.Right)
        composeTestRule.onNodeWithText(RETRY).assertIsFocused()
        composeTestRule.onRoot().performKeyInput { pressKey(Key.Enter) }
        composeTestRule.waitForIdle()

        assertEquals(1, retries)
    }

    @Test
    fun `a failure on a row that has items is ignored`() {
        setHub(listOf(failed("loaded").copy(items = listOf(10)), row("below", 20)))

        composeTestRule.onNodeWithTag("card-10").assertExists()
        composeTestRule.onNodeWithText(MESSAGE).assertDoesNotExist()
    }

    @Test
    fun `in a hub with a hero, focus on a failed row keeps the list there and the hero away`() {
        setHub(listOf(row("first", 0), row("second", 10), row("third", 20), failed("fourth")), hero = true)

        move(FocusDirection.Right)
        repeat(4) { move(FocusDirection.Down) }
        composeTestRule.settle(ANCHOR_COALESCE_WAIT_MILLIS)

        composeTestRule.onNodeWithText(RETRY).assertIsFocused()
        composeTestRule.onNodeWithText(RETRY).assertIsDisplayed()
        assertEquals(1, artworkCount())
        composeTestRule.onNodeWithTag(HERO).assertIsNotDisplayed()
    }

    @Test
    fun `when the retry brings items in, focus is not left on a dead node`() {
        var rows by mutableStateOf(listOf(failed("first"), row("below", 20)))
        setHub(rows = { rows })

        move(FocusDirection.Right)
        composeTestRule.onNodeWithText(RETRY).assertIsFocused()
        composeTestRule.runOnIdle { rows = listOf(row("first", 0), row("below", 20)) }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText(RETRY).assertDoesNotExist()
        move(FocusDirection.Down)
        composeTestRule.onNode(SemanticsMatcher.expectValue(SemanticsProperties.Focused, true)).assertExists()
    }

    private fun artworkCount() =
        composeTestRule
            .onAllNodes(
                SemanticsMatcher("is backdrop artwork") { node ->
                    SemanticsProperties.TestTag in node.config && node.config[SemanticsProperties.TestTag].startsWith("art-")
                },
            ).fetchSemanticsNodes()
            .size

    private fun setHub(rows: List<TvHubRow<Int>>, hero: Boolean = false) = setHub(rows = { rows }, hero = hero)

    private fun setHub(rows: () -> List<TvHubRow<Int>>, hero: Boolean = false) {
        composeTestRule.setContent {
            BingeTvTheme {
                focusManager = LocalFocusManager.current
                Row {
                    Box(Modifier.size(48.dp).testTag(RAIL).focusable())
                    TvImmersiveHub(
                        rows = rows(),
                        itemId = { it },
                        cardWidth = CARD_WIDTH,
                        onItemClick = {},
                        artwork = { item -> Box(Modifier.testTag("art-$item")) },
                        copy = {},
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                        hero = if (hero) ({ Box(Modifier.size(width = 320.dp, height = 96.dp).testTag(HERO).focusable()) }) else null,
                        hosting = TvPageHosting.RailDestination,
                    ) { item, _, onFocusChanged, onClick, cellModifier ->
                        Card(item, onFocusChanged, onClick, cellModifier)
                    }
                }
            }
        }
        composeTestRule.onNodeWithTag(RAIL).requestFocus()
        composeTestRule.waitForIdle()
    }

    private fun move(direction: FocusDirection) {
        composeTestRule.runOnIdle { focusManager.moveFocus(direction) }
        composeTestRule.waitForIdle()
    }
}

/** Narrow enough that three rows fit the viewport below the copy band. */
private val CARD_WIDTH = 48.dp

@Composable
private fun Card(
    item: Int,
    onFocusChanged: (Boolean) -> Unit,
    onClick: () -> Unit,
    modifier: Modifier,
) {
    Box(modifier.aspectRatio(2f / 3f).testTag("card-$item").tvClickable(onFocusChanged = onFocusChanged, onClick = onClick))
}
