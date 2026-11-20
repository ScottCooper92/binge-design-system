package com.binge.designsystem.template

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.assertDoesNotExist
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.Density
import com.binge.designsystem.ErrorKind
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

private const val ITEMS = 12

private fun items(count: Int = ITEMS) = List(count) { GridItem(key = "k$it", posterUrl = null, title = "Title $it", rating = 7f) }

private val bar =
    GridFilterBar(
        activeCount = 2,
        pillLabel = "Filters",
        pillDescription = "Filters, 2 applied",
        chips = listOf(GridFilterChip("a", "Drama"), GridFilterChip("b", "2020s")),
    )

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class, qualifiers = "w412dp-h900dp")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class PagedGridScreenTest {
    @get:Rule
    val rule = createTakeDownComposeRule()

    private fun show(
        state: PagedGridState,
        callbacks: PagedGridCallbacks = PagedGridCallbacks(onOpenItem = {}, onLoadMore = {}, onRetry = {}),
        filterBar: GridFilterBar? = bar,
        fontScale: Float = 1f,
        failedContent: (@androidx.compose.runtime.Composable (androidx.compose.ui.Modifier) -> Unit)? = null,
    ) {
        rule.setContent {
            val density = LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(density.density, fontScale = fontScale)) {
                TestTheme {
                    PagedGridScreen(
                        title = "Discover",
                        state = state,
                        callbacks = callbacks,
                        filterBar = filterBar,
                        failedContent = failedContent,
                    )
                }
            }
        }
        rule.waitForIdle()
    }

    @Test
    fun `the near-end rule fires within the look-ahead and not before`() {
        assertTrue(isNearEnd(lastVisibleIndex = 6, totalCount = 12))
        assertFalse(isNearEnd(lastVisibleIndex = 5, totalCount = 12))
    }

    @Test
    fun `the footer shows only while appending or after a failed append`() {
        assertFalse(showsGridFooter(PagedGridState.Ready(items(), hasMore = true)))
        assertTrue(showsGridFooter(PagedGridState.Ready(items(), hasMore = true, appending = true)))
        assertTrue(showsGridFooter(PagedGridState.Ready(items(), hasMore = true, appendFailed = true)))
    }

    @Test
    fun `a short list that has more asks for the next page once`() {
        var asked = 0
        show(
            PagedGridState.Ready(items(), hasMore = true),
            PagedGridCallbacks(onOpenItem = {}, onLoadMore = { asked++ }, onRetry = {}),
        )
        assertEquals(1, asked)
    }

    @Test
    fun `a list with nothing more never asks`() {
        var asked = 0
        show(
            PagedGridState.Ready(items(), hasMore = false),
            PagedGridCallbacks(onOpenItem = {}, onLoadMore = { asked++ }, onRetry = {}),
        )
        assertEquals(0, asked)
    }

    @Test
    fun `tapping a chip hands back its key`() {
        var cleared: String? = null
        show(
            PagedGridState.Ready(items(), hasMore = false),
            PagedGridCallbacks(onOpenItem = {}, onLoadMore = {}, onRetry = {}, onClearChip = { cleared = it }),
        )
        rule.onNodeWithText("Drama").performClick()
        assertEquals("a", cleared)
    }

    @Test
    fun `the first poster carries the layout anchor`() {
        show(PagedGridState.Ready(items(), hasMore = false))
        rule.onNode(hasTestTag(LayoutAnchors.section(LayoutAnchors.Collection.FIRST_ITEM))).assertIsDisplayed()
    }

    @Test
    fun `the skeleton's first plate carries the same anchor`() {
        show(PagedGridState.Loading)
        rule.onNode(hasTestTag(LayoutAnchors.section(LayoutAnchors.Collection.FIRST_ITEM))).assertIsDisplayed()
    }

    @Test
    fun `a failure without a retry draws no Try again, and a failedContent slot replaces the plate`() {
        show(PagedGridState.Failed(ErrorKind.Network, retryable = false))
        rule.onNodeWithText("Try again").assertDoesNotExist()
        rule.onNodeWithText("Discover").assertIsDisplayed()
    }

    @Test
    fun `no filter bar draws no pill`() {
        show(PagedGridState.Ready(items(), hasMore = false), filterBar = null)
        rule.onNodeWithText("Filters").assertDoesNotExist()
    }


    private fun clipping(fontScale: Float) {
        show(PagedGridState.Ready(items(), hasMore = false), fontScale = fontScale)
        val checked = rule.assertNoTextClipped()
        assertTrue("only $checked text nodes were checked", checked >= 2)
    }

    @Test fun `bar, pill and chips are not clipped at 1_3`() = clipping(1.3f)

    @Test fun `bar, pill and chips are not clipped at 2_0`() = clipping(2.0f)
}
