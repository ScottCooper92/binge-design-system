package com.binge.designsystem.component

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
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
}
