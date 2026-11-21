package com.binge.designsystem.tv.component

import androidx.compose.runtime.remember
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.isFocusable
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import com.binge.designsystem.testing.createTakeDownKeyboardComposeRule
import com.binge.designsystem.tv.testing.LeanbackRule
import com.binge.designsystem.tv.testing.MainThreadImageLoaderRule
import com.binge.designsystem.tv.theme.BingeTvTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** A poster cell's optional caption and footprint: a subtitle adds a line under the art, and a square cell is square. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], qualifiers = "w960dp-h540dp-television-xhdpi", application = android.app.Application::class)
class TvPosterGridCaptionTest {
    @get:Rule(order = 0)
    val leanback = LeanbackRule()

    @get:Rule(order = 1)
    val imageLoaderRule = MainThreadImageLoaderRule()

    @get:Rule(order = 2)
    val composeTestRule = createTakeDownKeyboardComposeRule()

    private fun setGrid(cells: List<TvPosterCell>) {
        composeTestRule.setContent {
            BingeTvTheme {
                TvPosterGrid(
                    count = cells.size,
                    posterAt = cells::getOrNull,
                    loading = false,
                    entry = remember { FocusRequester() },
                    entryIndex = 0,
                    listKey = Unit,
                    onCellFocused = {},
                )
            }
        }
        composeTestRule.waitForIdle()
    }

    private fun cell(
        title: String,
        subtitle: String? = null,
        shape: TvPosterShape = TvPosterShape.Poster,
    ) = TvPosterCell(imageUrl = null, title = title, onClick = {}, subtitle = subtitle, shape = shape)

    @Test
    fun `a subtitle is drawn under the poster`() {
        setGrid(listOf(cell("Heat", subtitle = "Crime · 1995")))

        composeTestRule.onNodeWithText("Crime · 1995").assertIsDisplayed()
    }

    @Test
    fun `a captioned cell without art shows its title once`() {
        setGrid(listOf(cell("Heat", subtitle = "Crime · 1995")))

        composeTestRule.onAllNodesWithText("Heat").assertCountEquals(1)
    }

    @Test
    fun `a cell without a subtitle draws no caption`() {
        setGrid(listOf(cell("Heat")))

        composeTestRule.onAllNodesWithText("Crime · 1995").assertCountEquals(0)
    }

    @Test
    fun `a square cell is as tall as it is wide, and a poster is taller`() {
        setGrid(listOf(cell("Poster"), cell("Logo", shape = TvPosterShape.Square)))

        val bounds = composeTestRule.onAllNodes(isFocusable()).fetchSemanticsNodes().map { it.boundsInRoot }

        assertEquals(bounds[1].width, bounds[1].height, 0.5f)
        assertEquals(bounds[0].width * 3 / 2, bounds[0].height, 1f)
    }
}
