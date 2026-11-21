package com.binge.designsystem.template

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import com.binge.designsystem.catalog.SearchScreenLoadingSample
import com.binge.designsystem.catalog.SearchScreenReadySample
import com.binge.designsystem.component.MediaResult
import com.binge.designsystem.component.MediaResultKind
import com.binge.designsystem.layout.LayoutAnchors
import com.binge.designsystem.preview.ScreenshotTheme
import com.binge.designsystem.testing.ShadowMeshSpecification
import com.binge.designsystem.testing.assertSkeletonReservesGeometry
import com.binge.designsystem.testing.createTakeDownComposeRule
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * The search template's loading state holds its first result row where the loaded results put it, across the page and at the size it gives
 * it at the default font, and where it puts it at 1.3 and 2. The loading state reserves the type chips' row, which the loaded results bring.
 * Sizes are checked, so the graphics mode is native.
 *
 * At a larger font a loaded row's own text, its name and its status chips, wraps over as many lines as that result's words need, and a plate
 * cannot know how many: at 1.3 the sample's row is 13dp taller than its skeleton and at 2.0 it is 58dp taller. So the large-font cases hold the
 * row's position and start edge, which the words do not decide. The hinted cases give the loading state a hint at the result names
 * (`expectedResultName`) and hold the first row's size at every scale for a row whose name wraps and whose status is one rating chip. A status
 * that wraps (a trailing chip beside a rating, a long genre line) is the app's own and still moves, as the loaded sample's does.
 */
@RunWith(RobolectricTestRunner::class)
@Config(
    sdk = [34],
    application = android.app.Application::class,
    shadows = [ShadowMeshSpecification::class],
    qualifiers = "w411dp-h891dp-xhdpi",
)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class SearchSkeletonGeometryTest {
    @get:Rule
    val rule: ComposeContentTestRule = createTakeDownComposeRule()

    private val anchors = listOf(LayoutAnchors.Collection.FIRST_ITEM).map(LayoutAnchors::section)

    private fun check(checkSize: Boolean, hinted: Boolean = false) {
        rule.assertSkeletonReservesGeometry(anchors, checkStartEdge = true, checkSize = checkSize) { resolved ->
            when {
                resolved && hinted -> HintedSearch(ReadyResults)
                resolved -> SearchScreenReadySample()
                hinted -> HintedSearch(SearchState.Loading)
                else -> SearchScreenLoadingSample()
            }
        }
    }

    @Test
    fun `the skeleton holds the first result`() = check(checkSize = true)

    @Test
    @Config(qualifiers = "w411dp-h891dp-xhdpi", fontScale = 1.3f)
    fun `the skeleton holds its position at a font scale of 1 point 3`() = check(checkSize = false)

    @Test
    @Config(qualifiers = "w411dp-h891dp-xhdpi", fontScale = 2f)
    fun `the skeleton holds its position at a font scale of 2`() = check(checkSize = false)

    @Test
    fun `a hinted skeleton holds the first result at the default font`() = check(checkSize = true, hinted = true)

    @Test
    @Config(qualifiers = "w411dp-h891dp-xhdpi", fontScale = 1.3f)
    fun `a hinted skeleton holds the first result at a font scale of 1 point 3`() = check(checkSize = true, hinted = true)

    @Test
    @Config(qualifiers = "w411dp-h891dp-xhdpi", fontScale = 2f)
    fun `a hinted skeleton holds the first result at a font scale of 2`() = check(checkSize = true, hinted = true)
}

private const val LONG_TITLE = "The Lord of the Rings: The Fellowship of the Ring"

/** A result whose name wraps at a large font, with a subtitle and a rating chip as its status: the words a hint can stand for. */
private val ReadyResults =
    SearchState.Ready(
        results = listOf(MediaResult("r0", MediaResultKind.Poster, null, LONG_TITLE, "Movie · 2001", rating = 8.4f)),
        loaded = 1,
        hasMore = false,
        types = SearchTypes(
            listOf(SearchTypeChip("all", "All"), SearchTypeChip("movies", "Movies"), SearchTypeChip("series", "Series")),
            "all",
        ),
    )

@Composable
private fun HintedSearch(state: SearchState) {
    ScreenshotTheme(modifier = Modifier.fillMaxSize()) {
        SearchScreen(
            title = "Search",
            text = "ring",
            state = state,
            placeholder = "Movies, series and people",
            reserveTypeChipsWhileLoading = true,
            expectedResultName = LONG_TITLE,
            callbacks = SearchCallbacks(onQueryChange = {}, onClear = {}, onOpenResult = {}, onLoadMore = {}, onRetry = {}),
        )
    }
}
