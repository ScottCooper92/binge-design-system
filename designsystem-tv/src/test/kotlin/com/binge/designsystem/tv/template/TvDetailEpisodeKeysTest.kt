package com.binge.designsystem.tv.template

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
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

/**
 * Episodes and seasons are identified by text the app chooses, not by numbers: a season id that is not a number, a specials season that is only
 * a name, and an episode keyed like the strip's own tail item all work, and a call that still passes numbers keeps compiling and means the same.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class, qualifiers = "w960dp-h540dp-television-xhdpi")
class TvDetailEpisodeKeysTest {
    @get:Rule
    val composeTestRule = createTakeDownKeyboardComposeRule()

    @get:Rule
    val imageLoaderRule = MainThreadImageLoaderRule()

    @get:Rule
    val leanback = LeanbackRule()

    private fun episode(
        key: String,
        season: String,
        number: Int,
    ) = TvDetailEpisode(
        key = key,
        seasonKey = season,
        episodeNumber = number,
        stillUrl = null,
        badge = "b$key",
        title = "Title $key",
        overview = "",
    )

    @Test
    fun `numbers still build an episode and a season, as their text`() {
        val episode = TvDetailEpisode(
            key = 205,
            seasonNumber = 2,
            episodeNumber = 5,
            stillUrl = null,
            badge = "5",
            title = "Five",
            overview = "",
        )
        val season = TvDetailSeason(number = 3, name = "Season 3", episodeCount = "4 episodes")

        assertEquals("205", episode.key)
        assertEquals("2", episode.seasonKey)
        assertEquals("3", season.key)
    }

    @Test
    fun `text keys, a specials season with no number, and the old tail key as an episode key all render`() {
        val seasons =
            listOf(
                TvDetailSeason(key = "specials", name = "Specials", episodeCount = "1 episode"),
                TvDetailSeason(key = "season-uuid-1", name = "Season 1", episodeCount = "2 episodes"),
            )
        val episodes =
            listOf(
                episode("sp-1", "specials", 1),
                episode("ep-1", "season-uuid-1", 1),
                // The strip's tail item used to be keyed by this exact text, beside the episodes' keys in one lazy row.
                episode("tv-episode-row-tail", "season-uuid-1", 2),
            )
        var current by mutableStateOf("season-uuid-1")
        composeTestRule.setContent {
            BingeTvTheme {
                TvDetailEpisodes(
                    title = TITLE,
                    seasons = seasons,
                    currentSeason = current,
                    onSeasonChange = { current = it },
                    episodes = episodes,
                    onEpisodeClick = {},
                    initialEpisodeKey = "ep-1",
                )
            }
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Specials").assertIsDisplayed()
        composeTestRule.onNodeWithText("Season 1").assertIsDisplayed()
        composeTestRule.onNodeWithText("btv-episode-row-tail").assertIsDisplayed()
    }

    private companion object {
        val TITLE = TvDetailTitle(
            title = "The Long Way Home",
            logoUrl = null,
            backdropUrl = null,
            overview = "",
            genres = emptyList(),
            facts = emptyList(),
            rating = null,
            certification = null,
        )
    }
}
