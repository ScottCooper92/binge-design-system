package com.binge.designsystem.tv.template

import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.requestFocus
import androidx.compose.ui.unit.dp
import com.binge.designsystem.testing.createTakeDownKeyboardComposeRule
import com.binge.designsystem.tv.theme.BingeTvTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private const val STRIP = "strip"
private const val SEASON_COUNT = 30

/**
 * Walking the episode strip changes the selected season with focus nowhere near the pills, so the row has to bring the
 * selection into view itself — without taking focus from the strip.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class, qualifiers = "w960dp-h540dp-television-xhdpi")
class TvDetailSeasonPillsScrollTest {
    @get:Rule
    val composeTestRule = createTakeDownKeyboardComposeRule()

    private var selected by mutableStateOf("1")

    @Test
    fun `a selection changed off-screen scrolls its pill into view and leaves focus alone`() {
        val seasons = List(SEASON_COUNT) { TvDetailSeason(number = it + 1, name = "Season ${it + 1}", episodeCount = "8 episodes") }
        composeTestRule.setContent {
            val down = remember { FocusRequester() }
            val entry = remember { FocusRequester() }
            BingeTvTheme {
                Column {
                    Box(Modifier.testTag(STRIP).size(20.dp).focusable())
                    TvDetailSeasonPills(
                        seasons = seasons,
                        selected = selected,
                        onSelect = { selected = it },
                        down = down,
                        entry = entry,
                        onFocused = {},
                    )
                }
            }
        }
        composeTestRule.onNodeWithTag(STRIP).requestFocus()
        composeTestRule.waitForIdle()

        selected = SEASON_COUNT.toString()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Season $SEASON_COUNT").assertIsDisplayed()
        composeTestRule.onNodeWithTag(STRIP).assertIsFocused()
    }
}
