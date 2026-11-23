package com.binge.designsystem.tv.template

import androidx.compose.ui.test.onNodeWithText
import com.binge.designsystem.component.HeroItem
import com.binge.designsystem.testing.createTakeDownKeyboardComposeRule
import com.binge.designsystem.tv.testing.LeanbackRule
import com.binge.designsystem.tv.testing.MainThreadImageLoaderRule
import com.binge.designsystem.tv.theme.BingeTvTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** An app row keyed like the hero's old plain key lays out beside the hero, where it used to crash the hub as a duplicate key. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class, qualifiers = "w960dp-h540dp-television-xhdpi")
class TvExpandingRowsHubHeroKeyTest {
    @get:Rule
    val composeTestRule = createTakeDownKeyboardComposeRule()

    /** Coil writes painter state off the composition thread, which can kill this run. */
    @get:Rule
    val imageLoaderRule = MainThreadImageLoaderRule()

    @get:Rule
    val leanback = LeanbackRule()

    @Test
    fun `a row keyed hero lays out beside the hero`() {
        val row =
            TvHubRow(
                key = "hero",
                title = "Row keyed hero",
                items = listOf(TvHubItem(key = "movie_1", posterUrl = null, title = "Title 1", rating = 7.5f, year = "2024")),
            )
        composeTestRule.setContent {
            BingeTvTheme {
                TvExpandingRowsHub(
                    rows = listOf(row),
                    onItemClick = {},
                    heroItems = listOf(HeroItem(id = 9, imageUrl = null, title = "The hero", rating = 7f)),
                )
            }
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Row keyed hero").assertExists()
    }
}
