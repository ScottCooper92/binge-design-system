package com.binge.designsystem.template

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.Density
import com.binge.designsystem.component.MediaResult
import com.binge.designsystem.component.MediaResultKind
import com.binge.designsystem.layout.LayoutAnchors
import com.binge.designsystem.testing.ShadowMeshSpecification
import com.binge.designsystem.testing.TestTheme
import com.binge.designsystem.testing.assertNoTextClipped
import com.binge.designsystem.testing.createTakeDownComposeRule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

private fun part(i: Int, blurb: String? = null) =
    MediaResult(key = "p$i", kind = MediaResultKind.Poster, imageUrl = null, name = "Part $i", rating = 7f, meta = "20$i", blurb = blurb)

private fun collection(
    parts: List<MediaResult>,
    overview: String? = "An overview.",
    partsHeading: String? = null,
) = CollectionDetail(
    title = "Saga",
    metaText = "2 movies",
    overview = overview,
    parts = parts,
    partsHeading = partsHeading,
    emptyTitle = "Nothing here",
    emptyMessage = "No titles.",
)

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class, shadows = [ShadowMeshSpecification::class], qualifiers = "w412dp-h900dp")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class CollectionDetailScreenTest {
    @get:Rule
    val rule = createTakeDownComposeRule()

    private fun show(
        state: CollectionDetailState,
        callbacks: CollectionDetailCallbacks = CollectionDetailCallbacks(onBack = {}, onRetry = {}, onOpenPart = {}),
        fontScale: Float = 1f,
    ) {
        rule.setContent {
            val density = LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(density.density, fontScale = fontScale)) {
                TestTheme { CollectionDetailScreen(state = state, callbacks = callbacks, partTrailing = { null }) }
            }
        }
        rule.waitForIdle()
    }

    @Test
    fun `parts and the overview are drawn, and a tap hands back the part's key`() {
        var opened: String? = null
        show(
            CollectionDetailState.Content(collection(listOf(part(1), part(2)))),
            CollectionDetailCallbacks(onBack = {}, onRetry = {}, onOpenPart = { opened = it }),
        )
        rule.onNodeWithText("An overview.").assertIsDisplayed()
        rule.onNodeWithText("Part 2").performClick()
        assertEquals("p2", opened)
    }

    @Test
    fun `a part's blurb is drawn, and one without draws none`() {
        show(CollectionDetailState.Content(collection(listOf(part(1, blurb = "What happens next."), part(2)))))
        rule.onNodeWithText("What happens next.").assertIsDisplayed()
        rule.onAllNodesWithText("What happens next.").assertCountEquals(1)
    }

    @Test
    fun `no parts shows the empty message and no overview`() {
        show(CollectionDetailState.Content(collection(emptyList())))
        rule.onNodeWithText("No titles.").assertIsDisplayed()
        rule.onAllNodesWithText("An overview.").assertCountEquals(0)
    }

    @Test
    fun `a failed page retries the page`() {
        var retried = false
        show(
            CollectionDetailState.Failed(),
            CollectionDetailCallbacks(onBack = {}, onRetry = { retried = true }, onOpenPart = {}),
        )
        rule.onNodeWithText("Try again").performClick()
        assertEquals(true, retried)
    }

    @Test
    @Config(qualifiers = "w1000dp-h800dp")
    fun `the loading rows stand where the loaded parts do on a wide window`() = loadingRowsStandWhereLoadedPartsDo()

    @Test
    fun `the loading rows stand where the loaded parts do`() = loadingRowsStandWhereLoadedPartsDo()

    @Test
    fun `the loading rows are as tall as parts with a three line blurb at 1x font`() = loadingRowsMatchLoadedPartsWithBlurbs(1f)

    @Test
    fun `the loading rows are as tall as parts with a three line blurb at 2x font`() = loadingRowsMatchLoadedPartsWithBlurbs(2f)

    private fun loadingRowsStandWhereLoadedPartsDo() {
        var state by mutableStateOf<CollectionDetailState>(CollectionDetailState.Loading(hasOverview = false, parts = 2))
        rule.setContent {
            TestTheme {
                CollectionDetailScreen(
                    state = state,
                    callbacks = CollectionDetailCallbacks(onBack = {}, onRetry = {}, onOpenPart = {}),
                )
            }
        }
        rule.waitForIdle()
        val anchor = hasTestTag(LayoutAnchors.section(LayoutAnchors.Collection.FIRST_ITEM))
        val loading = rule.onNode(anchor).fetchSemanticsNode().boundsInRoot
        state = CollectionDetailState.Content(collection(listOf(part(1), part(2)), overview = null))
        rule.waitForIdle()
        val loaded = rule.onNode(anchor).fetchSemanticsNode().boundsInRoot
        assertEquals(loaded.left, loading.left, 0.5f)
        assertEquals(loaded.top, loading.top, 0.5f)
        assertEquals(loaded.width, loading.width, 0.5f)
    }

    /** The first two rows keep their position and their height across the swap, so nothing under them shifts when the parts arrive. */
    private fun loadingRowsMatchLoadedPartsWithBlurbs(fontScale: Float) {
        val blurb = "A long blurb that runs over the three lines a part row gives it, so the row is as tall as a row ever is. " +
            "It keeps going: the story picks up after the last film, and the people who lived through it come back."
        var state by mutableStateOf<CollectionDetailState>(CollectionDetailState.Loading(hasOverview = false, parts = 2))
        rule.setContent {
            val density = LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(density.density, fontScale = fontScale)) {
                TestTheme {
                    CollectionDetailScreen(
                        state = state,
                        callbacks = CollectionDetailCallbacks(onBack = {}, onRetry = {}, onOpenPart = {}),
                        partTrailing = { null },
                    )
                }
            }
        }
        rule.waitForIdle()
        val first = hasTestTag(LayoutAnchors.section(LayoutAnchors.Collection.FIRST_ITEM))
        val second = hasTestTag(LayoutAnchors.section(LayoutAnchors.Collection.SECOND_ITEM))
        val loading = listOf(first, second).map { rule.onNode(it).fetchSemanticsNode().boundsInRoot }
        state = CollectionDetailState.Content(
            collection(listOf(part(1, blurb = blurb), part(2, blurb = blurb)), overview = null),
        )
        rule.waitForIdle()
        val loaded = listOf(first, second).map { rule.onNode(it).fetchSemanticsNode().boundsInRoot }
        loaded.zip(loading).forEachIndexed { index, (shown, plate) ->
            assertEquals("row $index top", shown.top, plate.top, 0.5f)
            assertEquals("row $index height", shown.height, plate.height, 0.5f)
        }
    }

    private fun gapUnderOverview(partsHeading: String?): Float {
        show(CollectionDetailState.Content(collection(listOf(part(1), part(2)), partsHeading = partsHeading)))
        val overview = rule.onNode(hasTestTag(LayoutAnchors.section(LayoutAnchors.Detail.OVERVIEW))).fetchSemanticsNode().boundsInRoot
        val first = rule.onNode(hasTestTag(LayoutAnchors.section(LayoutAnchors.Collection.FIRST_ITEM))).fetchSemanticsNode().boundsInRoot
        return first.top - overview.bottom
    }

    @Test
    fun `with no parts heading the first part keeps a gap under the overview`() {
        // padding_m is 16dp and a phone's density here is 1px a dp.
        assertEquals(16f, gapUnderOverview(partsHeading = null), 0.5f)
    }

    @Test
    fun `a parts heading keeps the gap it always supplied`() {
        assertTrue("the heading's room is more than the bare gap", gapUnderOverview(partsHeading = "Movies") > 16f)
    }

    private fun assertUnclippedAt(scale: Float) {
        show(CollectionDetailState.Content(collection(listOf(part(1, blurb = "What happens next."), part(2)))), fontScale = scale)
        rule.assertNoTextClipped()
    }

    @Test
    fun `nothing is clipped at 1_3x font`() = assertUnclippedAt(1.3f)

    @Test
    fun `nothing is clipped at 2x font`() = assertUnclippedAt(2f)

    @Test
    fun `a failed page draws its secondary action and a tap reports it`() {
        var allowed = 0
        show(
            CollectionDetailState.Failed(secondary = ScreenAction("Allow", { allowed++ })),
            CollectionDetailCallbacks(onBack = {}, onRetry = {}, onOpenPart = {}),
        )
        rule.onNodeWithText("Try again").assertExists()
        rule.onNodeWithText("Allow").performClick()
        assertEquals(1, allowed)
    }
}
