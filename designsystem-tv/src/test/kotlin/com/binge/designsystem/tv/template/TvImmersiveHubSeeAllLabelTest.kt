package com.binge.designsystem.tv.template

import android.app.Application
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.requestFocus
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Text
import com.binge.designsystem.testing.createTakeDownKeyboardComposeRule
import com.binge.designsystem.tv.focus.tvClickable
import com.binge.designsystem.tv.testing.LeanbackRule
import com.binge.designsystem.tv.theme.BingeTvTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private const val ITEMS = 3
private const val OWN_LABEL = "All 24"
private const val HUB_LABEL = "See all"

/**
 * A [TvHubRow]'s See-all tile says its own `seeAllLabel`, else the hub's, and a row with neither has no tile.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class, qualifiers = "w960dp-h540dp-television-xhdpi")
class TvImmersiveHubSeeAllLabelTest {
    @get:Rule(order = 0)
    val leanback = LeanbackRule()

    @get:Rule(order = 1)
    val composeTestRule = createTakeDownKeyboardComposeRule()

    private fun show(hubLabel: String?, vararg rows: TvHubRow<Int>) {
        composeTestRule.setContent {
            BingeTvTheme {
                TvImmersiveHub(
                    rows = rows.toList(),
                    itemId = { it },
                    cardWidth = 96.dp,
                    onItemClick = {},
                    artwork = {},
                    copy = { item -> Text("A title $item") },
                    modifier = Modifier.fillMaxHeight(),
                    hosting = TvPageHosting.Destination,
                    seeAllLabel = hubLabel,
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
        composeTestRule.waitForIdle()
    }

    private fun row(
        key: String,
        firstId: Int,
        seeAllLabel: String? = null,
        withSeeAll: Boolean = true,
    ) = TvHubRow(
        key = key,
        title = key,
        items = List(ITEMS) { firstId + it },
        onSeeAll = if (withSeeAll) ({}) else null,
        seeAllLabel = seeAllLabel,
    )

    @Test
    fun `a row's own label is its tile's text, and a row without one says the hub's`() {
        show(HUB_LABEL, row("first", 0, seeAllLabel = OWN_LABEL))
        composeTestRule.onNodeWithText(OWN_LABEL).assertExists()
        composeTestRule.onAllNodesWithText(HUB_LABEL).assertCountEquals(0)
    }

    @Test
    fun `a row with no label of its own falls back to the hub's label`() {
        show(HUB_LABEL, row("first", 0))
        composeTestRule.onNodeWithText(HUB_LABEL).assertExists()
    }

    @Test
    fun `a row's own label gives it a tile though the hub has no label`() {
        show(null, row("first", 0, seeAllLabel = OWN_LABEL))
        composeTestRule.onNodeWithText(OWN_LABEL).assertExists()
    }

    @Test
    fun `a row with no label from either has no tile`() {
        show(null, row("first", 0))
        composeTestRule.onAllNodesWithText(HUB_LABEL).assertCountEquals(0)
        composeTestRule.onAllNodesWithText(OWN_LABEL).assertCountEquals(0)
    }

    @Test
    fun `a row without a See-all action has no tile whatever its label`() {
        show(HUB_LABEL, row("first", 0, seeAllLabel = OWN_LABEL, withSeeAll = false))
        composeTestRule.onAllNodesWithText(OWN_LABEL).assertCountEquals(0)
        composeTestRule.onAllNodesWithText(HUB_LABEL).assertCountEquals(0)
    }

    @Test
    fun `the tile with its own label takes focus like any See-all tile`() {
        show(HUB_LABEL, row("first", 0, seeAllLabel = OWN_LABEL))
        composeTestRule.onNodeWithTag("card-0").requestFocus()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText(OWN_LABEL).requestFocus()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText(OWN_LABEL).assertIsFocused()
    }
}
