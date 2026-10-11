package com.binge.designsystem.tv.template

import android.app.Application
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.onNodeWithTag
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
private const val HERO_TARGET = "hero-target"
private const val ITEMS_PER_ROW = 3

private val Rows =
    listOf(
        TvHubRow(key = "first", title = "First", items = List(ITEMS_PER_ROW) { it }),
        TvHubRow(key = "second", title = "Second", items = List(ITEMS_PER_ROW) { 10 + it }),
    )

/**
 * The hub's hero gives way to the backdrop once a card takes focus, and comes back when focus returns to it. No frame
 * can show the transition, which depends on where focus is.
 *
 * The backdrop is what the hub composes for the focused item, so the artwork slot says which state it is in: no artwork
 * while the hero shows, one item's artwork once a card is settled. Focus held on a *descendant* of the hero must count
 * as the hero having focus. The hero's observer sits after its `focusGroup`, the reverse of every other site in the
 * module, and it works only because the observer sees its descendants' events; the return-to-hero case pins that order.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class, qualifiers = "w960dp-h540dp-television-xhdpi")
class TvImmersiveHubHeroTest {
    @get:Rule(order = 0)
    val leanback = LeanbackRule()

    @get:Rule(order = 1)
    val composeTestRule = createKeyboardComposeRule()

    private lateinit var focusManager: FocusManager

    @Test
    fun `the hero shows with no backdrop artwork while focus is in it`() {
        setHub()
        enterFromRail()

        composeTestRule.onNodeWithTag(HERO_TARGET).assertIsFocused()
        assertEquals(0, artworkCount())
    }

    @Test
    fun `focus moving from the hero into a row swaps the hero for that card's backdrop`() {
        setHub()
        enterFromRail()
        move(FocusDirection.Down)
        composeTestRule.onNodeWithTag("card-0").assertIsFocused()
        composeTestRule.settle(ANCHOR_COALESCE_WAIT_MILLIS)

        assertEquals(1, artworkCount())
        composeTestRule.onNodeWithTag("art-0").assertExists()
    }

    @Test
    fun `focus returning to the hero from a row brings the hero back and clears the backdrop`() {
        setHub()
        enterFromRail()
        move(FocusDirection.Down)
        composeTestRule.settle(ANCHOR_COALESCE_WAIT_MILLIS)
        assertEquals(1, artworkCount())

        move(FocusDirection.Up)
        composeTestRule.settle(ANCHOR_COALESCE_WAIT_MILLIS)

        composeTestRule.onNodeWithTag(HERO_TARGET).assertIsFocused()
        assertEquals(0, artworkCount())
    }

    private fun artworkCount() =
        composeTestRule
            .onAllNodes(
                androidx.compose.ui.test.SemanticsMatcher("is backdrop artwork") { node ->
                    androidx.compose.ui.semantics.SemanticsProperties.TestTag in node.config &&
                        node.config[androidx.compose.ui.semantics.SemanticsProperties.TestTag].startsWith("art-")
                },
            ).fetchSemanticsNodes()
            .size

    private fun setHub() {
        composeTestRule.setContent {
            BingeTvTheme {
                focusManager = LocalFocusManager.current
                Row {
                    Box(Modifier.size(48.dp).testTag(RAIL).focusable())
                    TvImmersiveHub(
                        rows = Rows,
                        itemId = { it },
                        cardWidth = 96.dp,
                        onItemClick = {},
                        artwork = { item -> Box(Modifier.testTag("art-$item")) },
                        copy = {},
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                        hero = { Box(Modifier.size(width = 320.dp, height = 96.dp).testTag(HERO_TARGET).focusable()) },
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

    private fun enterFromRail() = move(FocusDirection.Right)

    private fun move(direction: FocusDirection) {
        composeTestRule.runOnIdle { focusManager.moveFocus(direction) }
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
