package com.binge.designsystem.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.Text
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeUp
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
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

/**
 * The fast scrollbar over a lazy grid: it appears while the grid scrolls and goes after a rest, its thumb drags the grid and shows a bubble while
 * held, a tap on the grid under it still lands, a screen reader gets one adjustable control, and the bubble never clips at a large font.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class, qualifiers = "w412dp-h900dp")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class BingeFastScrollbarTest {
    @get:Rule
    val rule = createTakeDownComposeRule()

    private lateinit var state: LazyGridState
    private var clicks = 0

    private fun show(
        items: Int = ITEMS,
        fontScale: Float = 1f,
        section: (Int) -> String? = { "Section ${it / PER_SECTION}" },
    ) {
        rule.setContent {
            val density = LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(density.density, fontScale = fontScale)) {
                TestTheme {
                    state = rememberLazyGridState()
                    Box(Modifier.height(GRID_HEIGHT)) {
                        LazyVerticalGrid(columns = GridCells.Fixed(COLUMNS), state = state, modifier = Modifier.testTag(GRID)) {
                            items(items) { index ->
                                Box(Modifier.fillMaxWidth().height(CELL_HEIGHT).clickable { clicks++ }) { Text("Cell $index") }
                            }
                        }
                        BingeFastScrollbar(state = state, bubbleText = section)
                    }
                }
            }
        }
        rule.waitForIdle()
    }

    /** Puts a finger on the thumb and drags it down in the small steps a finger makes, leaving it held. */
    private fun holdAndDragThumb() {
        rule.onNodeWithTag(FAST_SCROLLBAR_THUMB_TAG).performTouchInput {
            down(center)
            repeat(DRAG_STEPS) {
                moveBy(
                    androidx.compose.ui.geometry
                        .Offset(0f, DRAG_PX / DRAG_STEPS),
                )
            }
        }
    }

    private fun thumbExists() =
        rule
            .onAllNodes(
                androidx.compose.ui.test
                    .hasTestTag(FAST_SCROLLBAR_THUMB_TAG),
            ).fetchSemanticsNodes()
            .isNotEmpty()

    @Test
    fun `a list that fits on screen has no scrollbar`() {
        show(items = 3)
        rule
            .onAllNodes(
                androidx.compose.ui.test
                    .hasContentDescription("Scroll position"),
            ).fetchSemanticsNodes()
            .let { assertTrue(it.isEmpty()) }
    }

    @Test
    fun `the thumb is hidden at rest, shown while the grid scrolls and gone again after a pause`() {
        show()
        assertTrue("the thumb was drawn at rest", !thumbExists())

        rule.mainClock.autoAdvance = false
        rule.onNodeWithTag(GRID).performTouchInput { swipeUp() }
        rule.mainClock.advanceTimeBy(FLING_MS)
        assertTrue("the thumb did not appear while scrolling", thumbExists())

        // Let the grid come to rest, then wait out the delay before the thumb fades.
        rule.mainClock.advanceTimeBy(REST_MS)
        assertTrue("the thumb stayed after a pause", !thumbExists())
    }

    @Test
    fun `dragging the thumb jumps the grid and shows the bubble, which goes when it is let go`() {
        show()
        rule.onNodeWithTag(GRID).performTouchInput { swipeUp() }
        rule.waitForIdle()
        val before = state.firstVisibleItemIndex

        holdAndDragThumb()
        rule.waitForIdle()
        assertTrue("the drag did not move the grid: $before to ${state.firstVisibleItemIndex}", state.firstVisibleItemIndex > before)
        rule.onNodeWithText("Section ${state.firstVisibleItemIndex / PER_SECTION}").assertExists()

        rule.onNodeWithTag(FAST_SCROLLBAR_THUMB_TAG).performTouchInput { up() }
        rule.waitForIdle()
        rule
            .onAllNodes(
                androidx.compose.ui.test
                    .hasText("Section", substring = true),
            ).fetchSemanticsNodes()
            .let { assertTrue(it.isEmpty()) }
    }

    @Test
    fun `a list with no bubble text drags without a bubble`() {
        show(section = { null })
        rule.onNodeWithTag(GRID).performTouchInput { swipeUp() }
        rule.waitForIdle()
        holdAndDragThumb()
        rule.waitForIdle()
        rule
            .onAllNodes(
                androidx.compose.ui.test
                    .hasText("Section", substring = true),
            ).fetchSemanticsNodes()
            .let { assertTrue(it.isEmpty()) }
    }

    @Test
    fun `a screen reader gets one adjustable control that sets the position`() {
        show()
        val node = rule.onNodeWithContentDescription("Scroll position").fetchSemanticsNode()
        val range = node.config[SemanticsProperties.ProgressBarRangeInfo]
        assertEquals(0f, range.current, 0.01f)
        assertTrue(SemanticsActions.SetProgress in node.config)

        rule.onNodeWithContentDescription("Scroll position").performSemanticsAction(SemanticsActions.SetProgress) { it(1f) }
        rule.waitForIdle()
        assertTrue("setting the progress did not scroll the grid: ${state.firstVisibleItemIndex}", state.firstVisibleItemIndex > ITEMS / 2)
    }

    @Test
    fun `the end of the track reaches the last item and the thumb stays there`() {
        show()
        rule.onNodeWithContentDescription("Scroll position").performSemanticsAction(SemanticsActions.SetProgress) { it(1f) }
        rule.waitForIdle()
        val last = state.layoutInfo.visibleItemsInfo.maxOf { it.index }
        assertEquals("the end of the track did not reach the last item", ITEMS - 1, last)
        val node = rule.onNodeWithContentDescription("Scroll position").fetchSemanticsNode()
        assertEquals(1f, node.config[SemanticsProperties.ProgressBarRangeInfo].current, 0.01f)
    }

    @Test
    fun `a tap on the grid under the scrollbar still lands on the grid`() {
        show()
        rule.onNodeWithTag(GRID).performTouchInput { swipeUp() }
        rule.waitForIdle()
        // A drag of the thumb jumps the grid and stops its fling; the thumb stays for a moment after it is let go, so a tap lands with it showing.
        holdAndDragThumb()
        rule.onNodeWithTag(FAST_SCROLLBAR_THUMB_TAG).performTouchInput { up() }
        rule.waitForIdle()
        assertTrue("the thumb was not shown", thumbExists())
        val thumb = rule.onNodeWithTag(FAST_SCROLLBAR_THUMB_TAG).getBoundsInRoot()
        // The end edge of the grid, level with a cell that is not under the thumb.
        val y = if (thumb.top > CELL_HEIGHT) CELL_HEIGHT / 2 else GRID_HEIGHT - CELL_HEIGHT / 2
        rule.onNodeWithTag(GRID).performTouchInput {
            val point = androidx.compose.ui.geometry
                .Offset(width - 4f, y.toPx())
            down(point)
            up()
        }
        assertEquals("the tap under the track did not reach the grid", 1, clicks)
    }

    private fun clipGuard(fontScale: Float) {
        show(fontScale = fontScale, section = { "A department with a very long name that cannot fit" })
        rule.onNodeWithTag(GRID).performTouchInput { swipeUp() }
        rule.waitForIdle()
        holdAndDragThumb()
        rule.waitForIdle()
        val bubble = rule
            .onNode(
                androidx.compose.ui.test
                    .hasText("A department", substring = true),
            ).getBoundsInRoot()
        val thumb = rule.onNodeWithTag(FAST_SCROLLBAR_THUMB_TAG).getBoundsInRoot()
        assertTrue("the bubble starts off screen: ${bubble.left}", bubble.left.value >= 0f)
        assertTrue("the bubble reaches the thumb's strip: ${bubble.right} vs ${thumb.left}", bubble.right <= thumb.left)
        assertTrue("the bubble clipped its text", rule.assertNoTextClipped() > 0)
    }

    @Test
    fun `the bubble does not clip at 1_3`() = clipGuard(1.3f)

    @Test
    fun `the bubble does not clip at 2_0`() = clipGuard(2f)

    private companion object {
        const val GRID = "grid"
        const val COLUMNS = 3
        const val ITEMS = 90
        const val PER_SECTION = 9
        const val DRAG_PX = 300f
        const val DRAG_STEPS = 10
        const val FLING_MS = 100L
        const val FRAME_MS = 16L
        const val REST_MS = 5_000L
        val GRID_HEIGHT = 400.dp
        val CELL_HEIGHT = 96.dp
    }
}
