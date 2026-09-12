package com.binge.designsystem

import androidx.compose.animation.core.animate
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.layout.layout

/**
 * How far a screen's title has been scrolled away, and the range it may travel.
 *
 * For a header whose title should give way to content while the controls beneath it — a search field, a
 * filter rail — stay put. Pair [collapsingTitle] on the title with [nestedScrollConnection] on an
 * ancestor of the scrolling content.
 *
 * [heightPx] is written from the layout pass rather than a measurement side-effect, so it is always the
 * title's true pre-collapse height, including at a large font scale where a derived value would be wrong.
 * Nothing reads it during composition, so writing it there cannot loop.
 */
@Stable
class CollapsingTitleState {
    var heightPx by mutableIntStateOf(0)
    var collapsedPx by mutableFloatStateOf(0f)
        private set

    /** Applies [deltaY] of scroll to the collapse, returning the vertical scroll it consumed. */
    fun consume(deltaY: Float): Float {
        val next = (collapsedPx - deltaY).coerceIn(0f, heightPx.toFloat())
        val applied = next - collapsedPx
        collapsedPx = next
        return -applied
    }

    /**
     * Animates the title back open.
     *
     * The collapse only moves through [consume], which the nested-scroll connection drives from the
     * user's drags — a programmatic `animateScrollToItem(0)` does not dispatch nested scroll to its
     * ancestors. So a scroll-to-top leaves the collapse exactly where the last drag left it, and the
     * title stays hidden over a list that is already at the top. Whatever sends the content back to
     * the top has to send the title with it.
     */
    suspend fun expand() {
        animate(initialValue = collapsedPx, targetValue = 0f) { value, _ -> collapsedPx = value }
    }
}

/**
 * Collapse driven by the content's scroll: the title gives way on the way down, and comes back only once
 * the list has nothing left to give (`onPostScroll`) rather than on any upward flick — otherwise it
 * reappears over content the reader is still on.
 */
fun CollapsingTitleState.nestedScrollConnection(): NestedScrollConnection =
    object : NestedScrollConnection {
        override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset =
            if (available.y < 0) Offset(0f, consume(available.y)) else Offset.Zero

        override fun onPostScroll(
            consumed: Offset,
            available: Offset,
            source: NestedScrollSource,
        ): Offset = if (available.y > 0) Offset(0f, consume(available.y)) else Offset.Zero
    }

/**
 * Shrinks and slides the title by [state]'s collapse, clipped so it slides under whatever sits above it.
 *
 * Shrinking as well as sliding is the point: reporting a smaller height is what draws the controls below
 * it upward, rather than leaving a gap where the title was.
 */
fun Modifier.collapsingTitle(state: CollapsingTitleState): Modifier =
    clipToBounds().layout { measurable, constraints ->
        val placeable = measurable.measure(constraints)
        state.heightPx = placeable.height
        val collapsed = state.collapsedPx.toInt()
        layout(placeable.width, (placeable.height - collapsed).coerceAtLeast(0)) {
            placeable.place(0, -collapsed)
        }
    }
