package com.binge.designsystem.template

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onNodeWithContentDescription
import com.binge.designsystem.R
import com.binge.designsystem.component.MediaResult
import com.binge.designsystem.component.MediaResultKind
import com.binge.designsystem.testing.TestTheme
import com.binge.designsystem.testing.createTakeDownComposeRule
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

private const val LONG = 60

/**
 * The fast scrollbar a template draws over its own grid when it is asked to: none by default, one adjustable control at the end edge when
 * [FastScrollbarOptions] is passed, with the app's bubble text as its state. The control is found by its label, which is in the semantics tree
 * whether or not the thumb is showing.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class, qualifiers = "w412dp-h900dp")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class TemplateFastScrollbarTest {
    @get:Rule
    val rule = createTakeDownComposeRule()

    private var label = ""

    private fun show(content: @Composable () -> Unit) {
        rule.setContent {
            label = stringResource(R.string.fast_scrollbar_label)
            TestTheme { content() }
        }
        rule.waitForIdle()
    }

    private fun pagedGrid(options: FastScrollbarOptions?) =
        show {
            PagedGridScreen(
                title = "Discover",
                state = PagedGridState.Ready(
                    List(LONG) { GridItem(key = "k$it", posterUrl = null, title = "Title $it", rating = 7f) },
                    hasMore = false,
                ),
                callbacks = PagedGridCallbacks(onOpenItem = {}, onLoadMore = {}, onRetry = {}),
                fastScrollbar = options,
            )
        }

    private fun search(options: FastScrollbarOptions?) =
        show {
            SearchScreen(
                title = "Search",
                text = "past",
                state = SearchState.Ready(
                    List(LONG) { MediaResult("r$it", MediaResultKind.Poster, null, "Result $it", "Movie · 2020", rating = 7f) },
                    loaded = LONG,
                    hasMore = false,
                ),
                callbacks = SearchCallbacks(
                    onQueryChange = {},
                    onClear = {},
                    onOpenResult = {},
                    onLoadMore = {},
                    onRetry = {},
                ),
                placeholder = "Search here",
                fastScrollbar = options,
            )
        }

    private fun assertAbsent() = rule.onAllNodesWithContentDescription(label).assertCountEquals(0)

    private fun assertPresentWithBubble(text: String) {
        rule.onAllNodesWithContentDescription(label).assertCountEquals(1)
        val state = rule.onNodeWithContentDescription(label).fetchSemanticsNode().config[SemanticsProperties.StateDescription]
        assertEquals(text, state)
    }

    @Test
    fun `a paged grid draws no scrollbar unless asked`() {
        pagedGrid(null)
        assertAbsent()
    }

    @Test
    fun `a paged grid draws the scrollbar when asked, with the app's bubble text`() {
        pagedGrid(FastScrollbarOptions(bubbleText = { "From $it" }))
        assertPresentWithBubble("From 0")
    }

    @Test
    fun `search draws no scrollbar unless asked`() {
        search(null)
        assertAbsent()
    }

    @Test
    fun `search draws the scrollbar when asked, with the app's bubble text`() {
        search(FastScrollbarOptions(bubbleText = { "From $it" }))
        assertPresentWithBubble("From 0")
    }

    @Test
    fun `a scrollbar with no bubble text draws no state`() {
        pagedGrid(FastScrollbarOptions())
        rule.onAllNodesWithContentDescription(label).assertCountEquals(1)
    }
}
