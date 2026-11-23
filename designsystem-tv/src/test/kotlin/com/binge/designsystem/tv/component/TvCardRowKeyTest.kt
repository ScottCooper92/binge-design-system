package com.binge.designsystem.tv.component

import android.app.Application
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Text
import com.binge.designsystem.testing.createTakeDownKeyboardComposeRule
import com.binge.designsystem.tv.theme.BingeTvTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private const val TRAILING = "trailing"
private const val CELL_WIDTH_DP = 80

/** An app item keyed like the trailing tile's old plain key lays out beside the tile, where it used to crash as a duplicate key. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class, qualifiers = "w960dp-h540dp-television-xhdpi")
class TvCardRowKeyTest {
    @get:Rule
    val composeTestRule = createTakeDownKeyboardComposeRule()

    @Test
    fun `an item keyed like the old trailing key is not taken for the tile`() {
        composeTestRule.setContent {
            BingeTvTheme {
                TvCardRow(
                    items = listOf("tv-card-row-trailing", "other"),
                    key = { it },
                    cellWidth = CELL_WIDTH_DP.dp,
                    trailingContent = { _, _, cellModifier ->
                        Box(Modifier.testTag(TRAILING).size(CELL_WIDTH_DP.dp).then(cellModifier))
                    },
                ) { item, _, _, cellModifier ->
                    Text(item, cellModifier)
                }
            }
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("tv-card-row-trailing").assertExists()
        composeTestRule.onNodeWithTag(TRAILING).assertExists()
    }
}
