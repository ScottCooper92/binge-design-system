package com.binge.designsystem.tv.template

import android.app.Application
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.isFocused
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.pressKey
import androidx.compose.ui.test.requestFocus
import androidx.compose.ui.unit.dp
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

/**
 * A hub with no hero rests its first row one row gap under the copy band. Focus arriving anchors a row where that row rests, so
 * the layout does not move on the first key.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class, qualifiers = "w960dp-h540dp-television-xhdpi")
class TvImmersiveHubFirstRowStableTest {
    @get:Rule(order = 0)
    val leanback = LeanbackRule()

    @get:Rule(order = 1)
    val composeTestRule = createTakeDownKeyboardComposeRule()

    private fun setHub() {
        composeTestRule.setContent {
            BingeTvTheme(reduceMotion = true) {
                TvImmersiveHub(
                    rows = listOf(
                        TvHubRow(key = "first", title = "First", items = listOf(0, 1, 2)),
                        TvHubRow(key = "second", title = "Second", items = listOf(10, 11, 12)),
                        TvHubRow(key = "third", title = "Third", items = listOf(20, 21, 22)),
                    ),
                    itemId = { it },
                    cardWidth = 96.dp,
                    onItemClick = {},
                    artwork = { Box(Modifier.size(1.dp)) },
                    copy = {},
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
    }

    private fun top(item: Int): Float =
        composeTestRule
            .onNodeWithTag("card-$item")
            .fetchSemanticsNode()
            .boundsInRoot.top

    private fun press(key: Key) {
        composeTestRule.onNode(isFocused()).performKeyInput { pressKey(key) }
        composeTestRule.waitForIdle()
    }

    @Test
    fun `the first row's top is the same at rest and after focus arrives on it`() {
        setHub()
        val atRest = top(0)

        composeTestRule.onNodeWithTag("card-0").requestFocus()
        composeTestRule.waitForIdle()

        assertEquals(atRest, top(0), 0.5f)
    }

    @Test
    fun `a row focused further down is anchored where the first row rests`() {
        setHub()
        val atRest = top(0)
        composeTestRule.onNodeWithTag("card-0").requestFocus()
        composeTestRule.waitForIdle()

        press(Key.DirectionDown)

        assertEquals(atRest, top(10), 0.5f)
        press(Key.DirectionDown)
        assertEquals(atRest, top(20), 0.5f)
    }
}
