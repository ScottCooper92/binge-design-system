package com.binge.designsystem.tv.template

import android.app.Application
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.dp
import com.binge.designsystem.testing.createTakeDownKeyboardComposeRule
import com.binge.designsystem.tv.focus.tvClickable
import com.binge.designsystem.tv.testing.LeanbackRule
import com.binge.designsystem.tv.theme.BingeTvTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * The hub's hero is a lazy item with a key of its own. An app's row keys are text too, so a row keyed like the old plain
 * hero key must sit beside the hero and not crash the list as a duplicate.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class, qualifiers = "w960dp-h540dp-television-xhdpi")
class TvImmersiveHubKeyTest {
    @get:Rule(order = 0)
    val leanback = LeanbackRule()

    @get:Rule(order = 1)
    val composeTestRule = createTakeDownKeyboardComposeRule()

    @Test
    fun `a row keyed like the old hero key renders beside the hero without crashing`() {
        composeTestRule.setContent {
            BingeTvTheme {
                TvImmersiveHub(
                    rows = listOf(TvHubRow(key = "tv-immersive-hero", title = "Row", items = listOf(0, 1))),
                    itemId = { it },
                    cardWidth = 96.dp,
                    onItemClick = {},
                    artwork = { Box(Modifier.size(1.dp)) },
                    copy = {},
                    hero = { Box(Modifier.size(width = 320.dp, height = 96.dp).testTag("hero")) },
                    hosting = TvPageHosting.Destination,
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

        composeTestRule.onNodeWithTag("hero").assertExists()
        composeTestRule.onNodeWithTag("card-0").assertExists()
    }
}
