package com.binge.designsystem.tv.template

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithContentDescription
import com.binge.designsystem.testing.createTakeDownComposeRule
import com.binge.designsystem.tv.testing.MainThreadImageLoaderRule
import com.binge.designsystem.tv.theme.BingeTvTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** The marks an episode and a season can carry: each draws nothing unless set, and the ones that need words announce them in the phone's own. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class, qualifiers = "w960dp-h540dp-television-xhdpi")
class TvDetailEpisodeMarksTest {
    @get:Rule
    val composeTestRule = createTakeDownComposeRule()

    @get:Rule
    val imageLoaderRule = MainThreadImageLoaderRule()

    private fun episode(watched: Boolean = false, progress: Float? = null) =
        TvDetailEpisode(
            key = "e",
            seasonKey = "s",
            episodeNumber = 1,
            stillUrl = null,
            badge = "1",
            title = "One",
            overview = "",
            watched = watched,
            progress = progress,
        )

    @Test
    fun `no mark is set by default, on an episode or a season`() {
        val episode = episode()
        val season = TvDetailSeason(key = "s", name = "Season 1", episodeCount = "8 episodes")

        assertFalse(episode.watched)
        assertNull(episode.progress)
        assertFalse(season.watched)
        assertEquals(0, season.unwatchedCount)
    }

    @Test
    fun `the number constructors take the marks too`() {
        val episode = TvDetailEpisode(
            key = 1,
            seasonNumber = 1,
            episodeNumber = 1,
            stillUrl = null,
            badge = "1",
            title = "One",
            overview = "",
            watched = true,
            progress = 0.5f,
        )
        val season = TvDetailSeason(number = 1, name = "Season 1", episodeCount = "8 episodes", unwatchedCount = 3)

        assertEquals(true, episode.watched)
        assertEquals(0.5f, episode.progress)
        assertEquals(3, season.unwatchedCount)
    }

    @Test
    fun `a watched episode's still says so, and an unmarked one says nothing`() {
        composeTestRule.setContent { BingeTvTheme { TvDetailEpisodeCard(episode(watched = true), isFocused = false) } }
        composeTestRule.onNodeWithContentDescription("Watched").assertIsDisplayed()
    }

    @Test
    fun `an unmarked episode has no watched mark`() {
        composeTestRule.setContent { BingeTvTheme { TvDetailEpisodeCard(episode(), isFocused = false) } }
        composeTestRule.onNodeWithContentDescription("Watched").assertDoesNotExist()
    }

    @Test
    fun `a watched season's pill says watched, and a season with episodes left says how many`() {
        composeTestRule.setContent {
            BingeTvTheme {
                TvDetailSeasonPill(TvDetailSeason("a", "Season 1", "8 episodes", watched = true), selected = false, isFocused = false)
            }
        }
        composeTestRule.onNodeWithContentDescription("Watched").assertIsDisplayed()
    }

    @Test
    fun `a season with episodes left announces how many`() {
        composeTestRule.setContent {
            BingeTvTheme {
                TvDetailSeasonPill(TvDetailSeason("a", "Season 2", "8 episodes", unwatchedCount = 3), selected = false, isFocused = false)
            }
        }
        composeTestRule.onNodeWithContentDescription("3 unwatched").assertIsDisplayed()
    }
}
