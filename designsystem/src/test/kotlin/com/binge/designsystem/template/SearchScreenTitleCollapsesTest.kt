package com.binge.designsystem.template

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeUp
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.height
import com.binge.designsystem.component.MediaResult
import com.binge.designsystem.component.MediaResultKind
import com.binge.designsystem.testing.TestTheme
import com.binge.designsystem.testing.createTakeDownComposeRule
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

private const val RESULTS = 60
private const val TITLE = "Search"

/**
 * With `titleCollapses` the Search heading above the field scrolls away entirely as the results scroll, as Binge's does, instead of
 * collapsing into a top bar: the field stays, and nothing is left of the heading.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class, qualifiers = "w412dp-h900dp")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class SearchScreenTitleCollapsesTest {
    @get:Rule
    val rule = createTakeDownComposeRule()

    private fun show(titleCollapses: Boolean) {
        rule.setContent {
            TestTheme {
                SearchScreen(
                    title = TITLE,
                    text = "past",
                    state =
                        SearchState.Ready(
                            List(RESULTS) { MediaResult("r$it", MediaResultKind.Poster, null, "Result $it", "Movie · 2020", rating = 7f) },
                            loaded = RESULTS,
                            hasMore = false,
                        ),
                    callbacks = SearchCallbacks(onQueryChange = {}, onClear = {}, onOpenResult = {}, onLoadMore = {}, onRetry = {}),
                    placeholder = "Find a title",
                    titleCollapses = titleCollapses,
                )
            }
        }
        rule.waitForIdle()
    }

    private fun titleHeight(): Dp = rule.onNodeWithText(TITLE).getUnclippedBoundsInRoot().height

    private fun scrollResults() {
        repeat(3) { rule.onRoot().performTouchInput { swipeUp() } }
        rule.waitForIdle()
    }

    @Test
    fun `the heading is gone once the results have scrolled`() {
        show(titleCollapses = true)
        rule.onNodeWithText(TITLE).assertIsDisplayed()

        scrollResults()

        rule.onNodeWithText(TITLE).assertIsNotDisplayed()
    }

    @Test
    fun `without it the title stays in the bar as the results scroll`() {
        show(titleCollapses = false)

        scrollResults()

        assertTrue("the bar keeps its title", titleHeight() > Dp.Hairline)
    }
}
