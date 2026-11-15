package com.binge.designsystem.tv.template

import android.app.Application
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.requestFocus
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Text
import com.binge.designsystem.testing.createTakeDownKeyboardComposeRule
import com.binge.designsystem.tv.focus.tvClickable
import com.binge.designsystem.tv.testing.LeanbackRule
import com.binge.designsystem.tv.theme.BingeTvTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private const val SEE_ALL_LABEL = "See all"
private const val ITEMS = 4

private val PeekLayers: List<@Composable androidx.compose.foundation.layout.BoxScope.() -> Unit> =
    listOf(Color.Red, Color.Green, Color.Blue).map { tone -> { Box(Modifier.fillMaxSize().background(tone)) } }

private val StableRows =
    listOf(
        TvHubRow(key = "first", title = "First", items = List(ITEMS) { it }),
        TvHubRow(key = "second", title = "Second", items = List(ITEMS) { 10 + it }, onSeeAll = {}, seeAllPeek = PeekLayers),
        TvHubRow(key = "third", title = "Third", items = List(ITEMS) { 20 + it }),
    )

/**
 * Stable layouts: moving focus onto the last cell of a row, the See-all card, must not move the row. The row's cards and the card
 * itself keep their tops whether focus is on a title or on See-all.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class, qualifiers = "w960dp-h540dp-television-xhdpi")
class TvImmersiveHubSeeAllStableTest {
    @get:Rule(order = 0)
    val leanback = LeanbackRule()

    @get:Rule(order = 1)
    val composeTestRule = createTakeDownKeyboardComposeRule()

    private lateinit var focusManager: FocusManager

    @Test
    fun `the row's top is the same with focus on a title and on See-all`() {
        composeTestRule.setContent {
            BingeTvTheme {
                focusManager = LocalFocusManager.current
                Row {
                    Box(Modifier.size(48.dp).testTag("rail").focusable())
                    TvImmersiveHub(
                        rows = StableRows,
                        itemId = { it },
                        cardWidth = 96.dp,
                        onItemClick = {},
                        artwork = {},
                        copy = { item ->
                            Text("A title $item")
                            Text("Meta line for $item")
                            Text("A synopsis that fills a couple of lines of the copy band above the rows, as the sample's does.")
                        },
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                        hosting = TvPageHosting.Destination,
                        seeAllLabel = SEE_ALL_LABEL,
                    ) { item, _, onFocusChanged, onClick, cellModifier ->
                        Box(
                            cellModifier
                                .aspectRatio(2f / 3f)
                                .testTag("card-$item")
                                .tvClickable(onFocusChanged = onFocusChanged, onClick = onClick),
                        )
                    }
                }
            }
        }
        composeTestRule.onNodeWithTag("rail").requestFocus()
        composeTestRule.waitForIdle()
        move(FocusDirection.Right)
        move(FocusDirection.Down)
        composeTestRule.mainClock.advanceTimeBy(2_000)
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("card-10").assertIsFocused()
        val titleTop = composeTestRule.onNodeWithTag("card-12").getUnclippedBoundsInRoot().top
        val titleBounds = composeTestRule.onNodeWithTag("card-12").getUnclippedBoundsInRoot()
        val titleHeight = titleBounds.bottom - titleBounds.top

        move(FocusDirection.Right, times = ITEMS)
        composeTestRule.mainClock.advanceTimeBy(2_000)
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText(SEE_ALL_LABEL).assertIsFocused()

        val seeAll = composeTestRule.onNodeWithText(SEE_ALL_LABEL).getUnclippedBoundsInRoot()
        val sameCardAfter = composeTestRule.onNodeWithTag("card-12").getUnclippedBoundsInRoot()
        assertEquals("The row moved when focus reached See-all", titleTop.value, sameCardAfter.top.value, 0.5f)
        assertEquals("See-all is not the height of a title cell", titleHeight.value, (seeAll.bottom - seeAll.top).value, 1f)
    }

    private fun move(direction: FocusDirection, times: Int = 1) {
        repeat(times) { composeTestRule.runOnIdle { focusManager.moveFocus(direction) } }
        composeTestRule.waitForIdle()
    }
}
