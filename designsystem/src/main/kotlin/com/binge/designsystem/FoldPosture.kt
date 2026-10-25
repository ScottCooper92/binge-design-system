package com.binge.designsystem

import android.graphics.Rect
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.layout
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.constrainHeight
import androidx.compose.ui.unit.constrainWidth
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.offset
import androidx.window.layout.FoldingFeature
import androidx.window.layout.WindowInfoTracker
import kotlin.math.abs

/**
 * A horizontal fold crossing the window, measured from the window's top edge.
 *
 * This is the tabletop posture: the device stands half-opened like a laptop, so the window is divided
 * into an upright top half and a flat bottom half. The crease is a region to *stay out of*, because a control drawn across it is bent over
 * the hinge and half of it faces away from the user.
 */
data class HorizontalHinge(
    val top: Dp,
    val height: Dp,
) {
    val bottom: Dp get() = top + height
}

/**
 * The window's separating horizontal fold, or null when it has none.
 *
 * Null for every phone, tablet and flat-open foldable: a caller that gets null must keep the layout it
 * already had.
 */
@Composable
fun rememberHorizontalHinge(): HorizontalHinge? =
    rememberSeparatingFold(FoldingFeature.Orientation.HORIZONTAL) { bounds ->
        HorizontalHinge(top = bounds.top.toDp(), height = bounds.height().toDp())
    }

/**
 * The first separating fold on [orientation], measured by [toHinge], or null when the window has none.
 *
 * **Separating, not `HALF_OPENED`.** The state and the question differ: a dual-screen device reports
 * `FLAT` across a physical seam that occludes content just as a half-opened crease does, while a
 * foldable opened flat still reports a `FoldingFeature` that divides nothing. `isSeparating` is the
 * property that answers "is the window really in two pieces", which is what the caller is asking.
 *
 * [toHinge] reads `bounds` in **window** pixels; it runs inside [Density] so the caller converts on
 * the axis it cares about.
 */
@Composable
private fun <T> rememberSeparatingFold(orientation: FoldingFeature.Orientation, toHinge: Density.(Rect) -> T): T? {
    val context = LocalContext.current
    val density = LocalDensity.current
    val hinge by produceState<T?>(initialValue = null, context, density, orientation) {
        WindowInfoTracker
            .getOrCreate(context)
            .windowLayoutInfo(context)
            .collect { info ->
                value =
                    info.displayFeatures
                        .filterIsInstance<FoldingFeature>()
                        .firstOrNull { it.isSeparating && it.orientation == orientation }
                        ?.let { fold -> density.toHinge(fold.bounds) }
            }
    }
    return hinge
}

/**
 * The height cap a surface anchored to the window's bottom edge should apply in the tabletop posture,
 * or null when it should keep whatever height it already had.
 *
 * Reads the window from [LocalWindowInfo], so the value is in the same coordinate space the fold is
 * reported in. Call it from the surface's *host*, not from inside a dialog window — a dialog composes
 * against its own window and would measure the wrong thing.
 */
@Composable
internal fun rememberFoldSafeBottomHeight(): Dp? {
    val windowHeight = with(LocalDensity.current) {
        LocalWindowInfo.current.containerSize.height
            .toDp()
    }
    return foldSafeBottomHeight(
        windowHeight = windowHeight,
        hinge = rememberHorizontalHinge(),
        minHeight = dimensionResource(R.dimen.fold_safe_bottom_min_height),
    )
}

/**
 * How tall a bottom-anchored surface may be without reaching across [hinge], or null for "no cap".
 *
 * This is a **maximum**, not a height: a sheet that already fits below the crease is untouched, and
 * every window without a separating horizontal fold gets null, meaning "keep doing what you did before".
 *
 * A crease close to the bottom edge is refused rather than honoured: squeezing a sheet into [minHeight]
 * of screen is a worse answer than letting it cross a fold it was never going to clear.
 */
internal fun foldSafeBottomHeight(
    windowHeight: Dp,
    hinge: HorizontalHinge?,
    minHeight: Dp,
): Dp? {
    if (hinge == null) return null
    val belowFold = windowHeight - hinge.bottom
    return if (belowFold < minHeight) null else belowFold
}

/**
 * Keeps this node's content from straddling a tabletop crease: when the content would overlap the fold it
 * starts below it instead, by adding top padding to the node.
 *
 * For a compact control row or a short block of text, not a long scrolling column. A window with no
 * separating horizontal fold gets this modifier back unchanged, so every phone, tablet and flat-open
 * foldable is unaffected. A fixed size applied outside this modifier squeezes the content instead of
 * moving it, so size the content inside it. Apply it before any padding or background that should move with the content.
 * Position is read in window pixels, the space the fold is reported in, so a node inside a dialog window
 * would measure the wrong thing.
 *
 * The straddle test reads the node's unpadded geometry, never its own padded output, so the padding cannot
 * feed back into itself. A node in a scrolling column is held below the crease only while its unpadded
 * place still crosses it, and lets go once it scrolls clear. Top padding moves content only when the node's
 * place in its parent does not depend on its own height. A node aligned to its parent's bottom or centre
 * grows upward instead of moving, so the modifier sees that and leaves it unpadded.
 */
@Composable
fun Modifier.avoidHorizontalFold(): Modifier {
    val hinge = rememberHorizontalHinge() ?: return this
    val density = LocalDensity.current
    val clearance = remember(hinge, density) { FoldClearance(hinge, density) }
    return this
        // Before the layout below, so these are the outer coordinates: their top is the unpadded top.
        .onGloballyPositioned(clearance::onPositioned)
        .layout { measurable, constraints ->
            val clearancePx = clearance.clearancePx
            val placeable = measurable.measure(constraints.offset(vertical = -clearancePx))
            clearance.onMeasured(
                clearancePx = clearancePx,
                // The height the node would have with no padding. A fixed outer height keeps its full size
                // here, although the content inside it shrinks by the padding.
                unpaddedHeightPx = constraints.constrainHeight(placeable.height),
            )
            layout(constraints.constrainWidth(placeable.width), constraints.constrainHeight(placeable.height + clearancePx)) {
                placeable.place(0, clearancePx)
            }
        }
}

/**
 * The padding [avoidHorizontalFold] applies, and what it learned about the node from the last layout.
 *
 * Only [clearancePx] is snapshot state. The rest is written during measure and read after placement,
 * so it never invalidates a layout on its own.
 */
private class FoldClearance(
    private val hinge: HorizontalHinge,
    private val density: Density,
) {
    var clearancePx by mutableIntStateOf(0)
        private set

    private var measuredClearancePx = 0
    private var unpaddedHeightPx = 0
    private var positionedClearancePx: Int? = null
    private var positionedParentTop = 0f
    private var growsUpward = false

    fun onMeasured(clearancePx: Int, unpaddedHeightPx: Int) {
        measuredClearancePx = clearancePx
        this.unpaddedHeightPx = unpaddedHeightPx
    }

    fun onPositioned(outer: LayoutCoordinates) {
        val parentTop = outer.positionInParent().y
        val previousClearancePx = positionedClearancePx
        if (previousClearancePx != null) {
            val parentMoved = abs(parentTop - positionedParentTop) >= MOVE_THRESHOLD_PX
            val clearanceChanged = measuredClearancePx != previousClearancePx
            // A node that moves in its parent when only its own padding changed is placed by its own height,
            // so padding cannot move its content. Movement with no padding change is the layout around it
            // changing, so the node is judged afresh.
            if (clearanceChanged && parentMoved) {
                growsUpward = true
            } else if (parentMoved) {
                growsUpward = false
            }
        }
        positionedClearancePx = measuredClearancePx
        positionedParentTop = parentTop

        clearancePx =
            if (growsUpward) {
                0
            } else {
                with(density) {
                    foldClearancePadding(
                        top = outer.positionInWindow().y.toDp(),
                        height = unpaddedHeightPx.toDp(),
                        hinge = hinge,
                    ).roundToPx()
                }
            }
    }

    private companion object {
        const val MOVE_THRESHOLD_PX = 0.5f
    }
}

/**
 * The top padding that moves content of [height], whose unpadded top is [top], from across [hinge] to just
 * below it. Zero when there is no hinge or the content already sits wholly above or below the crease.
 */
internal fun foldClearancePadding(
    top: Dp,
    height: Dp,
    hinge: HorizontalHinge?,
): Dp {
    if (hinge == null) return 0.dp
    val bottom = top + height
    val straddles = top < hinge.bottom && bottom > hinge.top
    return if (straddles) hinge.bottom - top else 0.dp
}
