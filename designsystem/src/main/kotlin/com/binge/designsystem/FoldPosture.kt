package com.binge.designsystem

import android.graphics.Rect
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.window.layout.FoldingFeature
import androidx.window.layout.WindowInfoTracker

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
fun rememberFoldSafeBottomHeight(): Dp? {
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
fun foldSafeBottomHeight(
    windowHeight: Dp,
    hinge: HorizontalHinge?,
    minHeight: Dp,
): Dp? {
    if (hinge == null) return null
    val belowFold = windowHeight - hinge.bottom
    return if (belowFold < minHeight) null else belowFold
}
