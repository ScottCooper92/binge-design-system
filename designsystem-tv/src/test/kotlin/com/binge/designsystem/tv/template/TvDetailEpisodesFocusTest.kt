package com.binge.designsystem.tv.template

import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.isFocusable
import androidx.compose.ui.test.isFocused
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.pressKey
import androidx.compose.ui.test.requestFocus
import androidx.compose.ui.unit.dp
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

private const val ENTRY = "entry"

/**
 * The Episodes tab's focus: the strip runs on across seasons and the season pill follows the episode, choosing a pill jumps the strip,
 * and coming back to the tab lands where focus left it. A card's badge reads "F<season>-<episode>" only while it is focused, and a
 * pill shows its episode count ("c<season>") only while it is the selected one, so both are observable as text.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class, qualifiers = "w960dp-h540dp-television-xhdpi")
class TvDetailEpisodesFocusTest {
    @get:Rule
    val composeTestRule = createTakeDownKeyboardComposeRule()

    /** Coil writes painter state off the composition thread, which can kill this run (#2350, #2367). */
    @get:Rule
    val imageLoaderRule = MainThreadImageLoaderRule()

    @get:Rule
    val leanback = LeanbackRule()

    private val opened = mutableListOf<Pair<Int, Int>>()

    @Test
    fun `entering the tab lands on the selected season's pill`() {
        setTab()
        enter()
        composeTestRule.onNodeWithText("Season 1").assertIsFocused()
    }

    @Test
    fun `down from the pills lands on the season's first episode`() {
        setTab()
        enter()
        press(Key.DirectionDown)
        composeTestRule.onNodeWithText("F1-1").assertIsFocused()
    }

    @Test
    fun `the strip runs across the seasons and the pill follows the episode`() {
        setTab()
        enter()
        press(Key.DirectionDown)
        press(Key.DirectionRight)
        press(Key.DirectionRight)
        composeTestRule.onNodeWithText("F1-3").assertIsFocused()
        composeTestRule.onNodeWithText("2 episodes").assertDoesNotExist()
        press(Key.DirectionRight)
        composeTestRule.onNodeWithText("F2-1").assertIsFocused()
        composeTestRule.onNodeWithText("2 episodes").assertExists()
        press(Key.DirectionLeft)
        composeTestRule.onNodeWithText("F1-3").assertIsFocused()
        composeTestRule.onNodeWithText("3 episodes").assertExists()
    }

    @Test
    fun `walking onto another pill selects it and down jumps to that season's first episode`() {
        setTab()
        enter()
        press(Key.DirectionRight)
        composeTestRule.onNodeWithText("Season 2").assertIsFocused()
        composeTestRule.onNodeWithText("2 episodes").assertExists()
        press(Key.DirectionDown)
        composeTestRule.onNodeWithText("F2-1").assertIsFocused()
    }

    @Test
    fun `coming back to the tab lands on the episode it was left on`() {
        setTab()
        enter()
        press(Key.DirectionDown)
        press(Key.DirectionRight)
        leaveAndReturn()
        composeTestRule.onNodeWithText("F1-2").assertIsFocused()
    }

    @Test
    fun `coming back after moving up to the pills lands on the pills`() {
        setTab()
        enter()
        press(Key.DirectionDown)
        press(Key.DirectionRight)
        press(Key.DirectionUp)
        leaveAndReturn()
        composeTestRule.onNodeWithText("Season 1").assertIsFocused()
    }

    @Test
    fun `a tab opened on an episode lands on it when entered from the pills`() {
        setTab(initialEpisodeKey = 102)
        enter()
        press(Key.DirectionDown)
        composeTestRule.onNodeWithText("F1-2").assertIsFocused()
    }

    /**
     * The return from an episode's page re-creates the strip from saved state, before its seasons are back. Left on index 6 of
     * seasons 1 to 3, and refilled with only 2 and 3, index 6 is the tail spacer: a restored index would show nothing.
     */
    @Test
    fun `a return that refills with fewer episodes lands on the episode it left, placed by its key`() {
        var episodes by mutableStateOf(EPISODES + SEASON_3)
        var current by mutableIntStateOf(1)
        val restoration = StateRestorationTester(composeTestRule)
        restoration.setContent {
            BingeTvTheme {
                Column {
                    Box(Modifier.testTag(ENTRY).size(ENTRY_SIZE).focusable())
                    TvDetailEpisodes(
                        title = TITLE,
                        seasons = SEASONS + SEASON_3_PILL,
                        currentSeason = current,
                        onSeasonChange = { current = it },
                        episodes = episodes,
                        onEpisodeClick = {},
                    )
                }
            }
        }
        composeTestRule.waitForIdle()
        enter()
        press(Key.DirectionDown)
        repeat(SEASON_3_SECOND_INDEX) { press(Key.DirectionRight) }
        composeTestRule.onNodeWithText("F3-2").assertIsFocused()

        episodes = emptyList()
        restoration.emulateSavedInstanceStateRestore()
        composeTestRule.waitForIdle()
        episodes = EPISODES.filter { it.seasonNumber == 2 } + SEASON_3
        composeTestRule.waitForIdle()
        // The episode's resting badge: on screen only if the strip came back on it, not on the spacer past the end.
        composeTestRule.onNodeWithText("2").assertIsDisplayed()
        leaveAndReturn()

        composeTestRule.onNodeWithText("F3-2").assertIsFocused().assertIsDisplayed()
    }

    @Test
    fun `OK on an episode opens it with its season and number`() {
        setTab()
        enter()
        press(Key.DirectionDown)
        press(Key.DirectionRight)
        press(Key.DirectionRight)
        press(Key.DirectionRight)
        press(Key.DirectionCenter)
        assertEquals(listOf(2 to 1), opened)
    }

    private fun setTab(initialEpisodeKey: Int? = null) {
        composeTestRule.setContent {
            BingeTvTheme {
                var current by remember { mutableIntStateOf(1) }
                Column {
                    Box(Modifier.testTag(ENTRY).size(ENTRY_SIZE).focusable())
                    TvDetailEpisodes(
                        title = TITLE,
                        seasons = SEASONS,
                        currentSeason = current,
                        onSeasonChange = { current = it },
                        episodes = EPISODES,
                        onEpisodeClick = { opened += it.seasonNumber to it.episodeNumber },
                        initialEpisodeKey = initialEpisodeKey,
                    )
                }
            }
        }
        composeTestRule.waitForIdle()
    }

    private fun enter() {
        composeTestRule.onNodeWithTag(ENTRY).requestFocus()
        composeTestRule.waitForIdle()
        press(Key.DirectionDown)
    }

    private fun leaveAndReturn() {
        composeTestRule.onNodeWithTag(ENTRY).requestFocus()
        composeTestRule.waitForIdle()
        press(Key.DirectionDown)
    }

    private fun press(key: Key) {
        composeTestRule.onNode(isFocusable() and isFocused()).performKeyInput { pressKey(key) }
        composeTestRule.waitForIdle()
    }

    private companion object {
        val ENTRY_SIZE = 20.dp
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
        val SEASONS = listOf(
            TvDetailSeason(number = 1, name = "Season 1", episodeCount = "3 episodes"),
            TvDetailSeason(number = 2, name = "Season 2", episodeCount = "2 episodes"),
        )
        val EPISODES = listOf(1 to 3, 2 to 2).flatMap { (season, count) -> episodes(season, count) }
        val SEASON_3 = episodes(season = 3, count = 4)
        val SEASON_3_PILL = TvDetailSeason(number = 3, name = "Season 3", episodeCount = "4 episodes")

        /** Season 3's second episode, from the first of season 1: past the five of seasons 1 and 2, then one more. */
        const val SEASON_3_SECOND_INDEX = 6

        fun episodes(season: Int, count: Int) =
            (1..count).map { number ->
                TvDetailEpisode(
                    key = season * 100 + number,
                    seasonNumber = season,
                    episodeNumber = number,
                    stillUrl = null,
                    badge = number.toString(),
                    focusedBadge = "F$season-$number",
                    title = "Episode $season-$number",
                    overview = "",
                )
            }
    }
}
