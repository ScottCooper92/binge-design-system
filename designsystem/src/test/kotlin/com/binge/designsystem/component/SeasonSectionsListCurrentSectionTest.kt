package com.binge.designsystem.component

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.binge.designsystem.testing.TestTheme
import com.binge.designsystem.testing.createTakeDownComposeRule
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class)
class SeasonSectionsListCurrentSectionTest {
    @get:Rule
    val rule = createTakeDownComposeRule()

    private val episodes = List(6) { EpisodeBrowseUi(number = it + 1, name = "Episode ${it + 1}") }
    private val sections = listOf(
        SeasonSectionUi("1", "Season 1", "6 episodes", SeasonSectionState.Loaded(episodes)),
        SeasonSectionUi("2", "Season 2", "6 episodes", SeasonSectionState.Loaded(episodes)),
    )

    @Test
    fun `the section at the top is reported on opening and again when scrolling reaches another`() {
        val reported = mutableListOf<String>()
        lateinit var listState: LazyListState
        rule.setContent {
            TestTheme {
                listState = rememberLazyListState()
                SeasonSectionsList(
                    sections = sections,
                    listState = listState,
                    openAt = "1",
                    onSeasonNeeded = {},
                    onCurrentSectionChanged = { reported += it },
                )
            }
        }
        rule.waitForIdle()
        assertEquals(listOf("1"), reported)
        // The second section's header is the item after the first's header and six rows.
        rule.runOnIdle { runBlocking { listState.scrollToItem(7) } }
        rule.waitForIdle()
        assertEquals(listOf("1", "2"), reported)
    }

    @Test
    fun `an earlier section loading with another row count reports nothing new`() {
        val reported = mutableListOf<String>()
        var current by mutableStateOf(
            listOf(
                SeasonSectionUi("1", "Season 1", "3 episodes", SeasonSectionState.Loading(3)),
                sections[1],
            ),
        )
        rule.setContent {
            TestTheme {
                SeasonSectionsList(
                    sections = current,
                    listState = rememberLazyListState(),
                    openAt = "1",
                    onSeasonNeeded = {},
                    onCurrentSectionChanged = { reported += it },
                )
            }
        }
        rule.waitForIdle()
        rule.runOnIdle { current = sections }
        rule.waitForIdle()
        assertEquals(listOf("1"), reported)
    }
}
