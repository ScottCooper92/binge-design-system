package com.binge.designsystem.template

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.Density
import com.binge.designsystem.ErrorKind
import com.binge.designsystem.component.MediaResult
import com.binge.designsystem.component.MediaResultKind
import com.binge.designsystem.layout.LayoutAnchors
import com.binge.designsystem.testing.TestTheme
import com.binge.designsystem.testing.assertNoTextClipped
import com.binge.designsystem.testing.createTakeDownComposeRule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

private fun results(count: Int) =
    List(count) { MediaResult("k$it", MediaResultKind.Poster, null, "Result $it", "Movie · 2020", rating = 7f, meta = "Drama") }

private val types =
    SearchTypes(
        listOf(SearchTypeChip("all", "All"), SearchTypeChip("movies", "Movies"), SearchTypeChip("people", "People")),
        selectedKey = "all",
    )

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class, qualifiers = "w412dp-h900dp")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class SearchScreenTest {
    @get:Rule
    val rule = createTakeDownComposeRule()

    private fun callbacks(
        onLoadMore: () -> Unit = {},
        onPickRecent: (String) -> Unit = {},
        onOpenResult: (String) -> Unit = {},
        onType: (String) -> Unit = {},
    ) = SearchCallbacks(
        onQueryChange = {},
        onClear = {},
        onOpenResult = onOpenResult,
        onLoadMore = onLoadMore,
        onRetry = {},
        onType = onType,
        onPickRecent = onPickRecent,
    )

    private fun show(
        state: SearchState,
        callbacks: SearchCallbacks = callbacks(),
        fontScale: Float = 1f,
        failedContent: (@Composable (Modifier) -> Unit)? = null,
        idleDefaults: Boolean = false,
    ) {
        rule.setContent {
            val density = LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(density.density, fontScale = fontScale)) {
                TestTheme {
                    if (idleDefaults) {
                        SearchScreen(title = "Search", text = "", state = state, callbacks = callbacks, placeholder = "Search here")
                    } else {
                        SearchScreen(
                            title = "Search",
                            text = "past",
                            state = state,
                            callbacks = callbacks,
                            placeholder = "Search here",
                            idleTitle = "Idle title",
                            idleMessage = "Idle message",
                            failedContent = failedContent,
                        )
                    }
                }
            }
        }
        rule.waitForIdle()
    }

    @Test
    fun `the near-end rule fires within three of the end`() {
        assertTrue(isNearEnd(lastVisibleIndex = 7, totalCount = 10, ahead = SEARCH_LOAD_MORE_AHEAD))
        assertFalse(isNearEnd(lastVisibleIndex = 6, totalCount = 10, ahead = SEARCH_LOAD_MORE_AHEAD))
    }

    @Test
    fun `type chips show in Ready and NoResults only`() {
        show(SearchState.Ready(results(2), loaded = 2, hasMore = false, types = types))
        rule.onNodeWithText("Movies").assertIsDisplayed()
    }

    @Test
    fun `idle shows no type chips and does show a recent that can be picked`() {
        var picked: String? = null
        show(SearchState.Idle(listOf("severance")), callbacks(onPickRecent = { picked = it }))
        rule.onNodeWithText("Movies").assertDoesNotExist()
        rule.onNodeWithText("severance").performClick()
        assertEquals("severance", picked)
    }

    @Test
    fun `idle with nothing to offer shows the plate, and extras keep it away`() {
        show(SearchState.Idle(emptyList()))
        rule.onNodeWithText("Idle title").assertIsDisplayed()
    }

    @Test
    fun `a settled empty type shows its plate`() {
        show(SearchState.Ready(emptyList(), loaded = 0, hasMore = false, emptyTitle = "No people", emptyMessage = "None here"))
        rule.onNodeWithText("No people").assertIsDisplayed()
    }

    @Test
    fun `an empty type that has more still asks for the next page`() {
        var asked = 0
        show(SearchState.Ready(emptyList(), loaded = 0, hasMore = true, emptyTitle = "No people"), callbacks(onLoadMore = { asked++ }))
        rule.onNodeWithText("No people").assertDoesNotExist()
        assertTrue("expected a load-more request, got $asked", asked >= 1)
    }

    @Test
    fun `tapping a result hands back its key and a chip its key`() {
        var opened: String? = null
        var type: String? = null
        show(
            SearchState.Ready(results(3), loaded = 3, hasMore = false, types = types),
            callbacks(onOpenResult = { opened = it }, onType = { type = it }),
        )
        rule.onNodeWithText("Result 1").performClick()
        rule.onNodeWithText("People").performClick()
        assertEquals("k1", opened)
        assertEquals("people", type)
    }

    @Test
    fun `the default idle plate says what to do`() {
        show(SearchState.Idle(emptyList()), idleDefaults = true)
        rule.onNodeWithText("Type a title or a name.").assertIsDisplayed()
    }

    @Test
    fun `loading draws the first plate with its anchor and no results`() {
        show(SearchState.Loading)
        rule.onNode(hasTestTag(LayoutAnchors.section(LayoutAnchors.Collection.FIRST_ITEM))).assertIsDisplayed()
    }

    @Test
    fun `a failure that cannot be retried draws no Try again`() {
        show(SearchState.Failed(ErrorKind.Network, retryable = false))
        rule.onNodeWithText("Try again").assertDoesNotExist()
    }

    private fun clipping(fontScale: Float) {
        show(SearchState.Ready(results(3), loaded = 3, hasMore = false, types = types), fontScale = fontScale)
        val checked = rule.assertNoTextClipped()
        assertTrue("only $checked text nodes were checked", checked >= 4)
    }

    @Test fun `field, chips and rows are not clipped at 1_3`() = clipping(1.3f)

    @Test fun `field, chips and rows are not clipped at 2_0`() = clipping(2.0f)

    @Test
    fun `a failed page draws its secondary action and a tap reports it`() {
        var allowed = 0
        show(SearchState.Failed(ErrorKind.Network, secondary = ScreenAction("Allow", { allowed++ })))
        rule.onNodeWithText("Try again").assertExists()
        rule.onNodeWithText("Allow").performClick()
        assertEquals(1, allowed)
    }
}
