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
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

private fun part(i: Int, blurb: String? = null) =
    MediaResult(key = "p$i", kind = MediaResultKind.Poster, imageUrl = null, name = "Part $i", rating = 7f, meta = "20$i", blurb = blurb)

private fun collection(parts: List<MediaResult>, overview: String? = "An overview.") =
    CollectionDetail(
        title = "Saga",
        metaText = "2 movies",
        overview = overview,
        parts = parts,
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
