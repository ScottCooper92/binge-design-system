package com.binge.designsystem.component

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.binge.designsystem.testing.createTakeDownComposeRule
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import androidx.compose.foundation.lazy.grid.items as gridItems

private const val MOVIES = "movies"
private const val SEARCH = "search"

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class)
class TabReselectTest {
    @get:Rule
    val rule = createTakeDownComposeRule()

    private val signal = MutableSharedFlow<Any>(extraBufferCapacity = 1)

    private fun emit(tab: Any) {
        runBlocking { signal.emit(tab) }
        rule.waitForIdle()
    }

    @Test
    fun `an action runs for its own tab's reselect and not for another's`() {
        var runs = 0
        rule.setContent {
            CompositionLocalProvider(LocalTabReselectSignal provides signal) { OnTabReselect(MOVIES) { runs++ } }
        }

        emit(SEARCH)
        assertEquals(0, runs)

        emit(MOVIES)
        assertEquals(1, runs)

        emit(MOVIES)
        assertEquals(2, runs)
    }

    @Test
    fun `with no signal provided nothing runs and nothing fails`() {
        var runs = 0
        rule.setContent { OnTabReselect(MOVIES) { runs++ } }
        rule.waitForIdle()

        assertEquals(0, runs)
    }

    @Test
    fun `the action that is current when the tab is tapped is the one that runs`() {
        val state = mutableIntStateOf(1)
        var seen = 0
        rule.setContent {
            CompositionLocalProvider(LocalTabReselectSignal provides signal) {
                val now = state.intValue
                OnTabReselect(MOVIES) { seen = now }
            }
        }
        state.intValue = 2
        rule.waitForIdle()

        emit(MOVIES)

        assertEquals(2, seen)
    }

    @Test
    fun `a scrolled list goes back to the top on its tab's reselect only`() {
        lateinit var list: LazyListState
        rule.setContent {
            list = rememberLazyListState()
            CompositionLocalProvider(LocalTabReselectSignal provides signal) {
                ScrollToTopOnTabReselect(MOVIES, list)
                LazyColumn(Modifier, list) { items((0 until 60).toList()) { Text("row $it", Modifier.height(48.dp)) } }
            }
        }
        rule.runOnIdle { runBlocking { list.scrollToItem(30) } }
        rule.waitForIdle()
        assertEquals(30, list.firstVisibleItemIndex)

        emit(SEARCH)
        assertEquals("another tab's reselect leaves it", 30, list.firstVisibleItemIndex)

        emit(MOVIES)
        rule.mainClock.advanceTimeBy(2_000)
        rule.waitForIdle()
        assertEquals(0, list.firstVisibleItemIndex)
    }

    @Test
    fun `a scrolled grid goes back to the top on its tab's reselect`() {
        lateinit var grid: LazyGridState
        rule.setContent {
            grid = rememberLazyGridState()
            CompositionLocalProvider(LocalTabReselectSignal provides signal) {
                ScrollToTopOnTabReselect(SEARCH, grid)
                LazyVerticalGrid(
                    GridCells.Fixed(2),
                    Modifier,
                    grid,
                ) { gridItems((0 until 80).toList()) { Text("cell $it", Modifier.height(48.dp)) } }
            }
        }
        rule.runOnIdle { runBlocking { grid.scrollToItem(40) } }
        rule.waitForIdle()
        assertEquals(40, grid.firstVisibleItemIndex)

        emit(SEARCH)
        rule.mainClock.advanceTimeBy(2_000)
        rule.waitForIdle()

        assertEquals(0, grid.firstVisibleItemIndex)
    }
}
