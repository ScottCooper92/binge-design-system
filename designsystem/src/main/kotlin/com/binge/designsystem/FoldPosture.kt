package com.binge.designsystem

import android.graphics.Rect
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.window.layout.FoldingFeature
import androidx.window.layout.WindowInfoTracker

/**
 * A vertical fold crossing the window, measured from the window's left edge.
 *
 * "Vertical" is the hinge's own axis, so this is the posture that splits a window into a left and a
 * right half — the one a two-pane layout can align to. The tabletop posture folds the other way and
 * is [HorizontalHinge].
 */
data class VerticalHinge(
    val start: Dp,
    val width: Dp,
) {
    val end: Dp get() = start + width
}

/**
 * A horizontal fold crossing the window, measured from the window's top edge.
 *
 * This is the tabletop posture: the device stands half-opened like a laptop, so the window is divided
 * into an upright top half and a flat bottom half. Unlike [VerticalHinge] nothing here aligns a pane
 * boundary — the crease is a region to *stay out of*, because a control drawn across it is bent over
 * the hinge and half of it faces away from the user.
 */
data class HorizontalHinge(
    val top: Dp,
    val height: Dp,
) {
    val bottom: Dp get() = top + height
}

/** How a two-pane [androidx.compose.foundation.layout.Row] should divide itself. */
data class TwoPaneSplit(
    val leadingWidth: Dp,
    val gap: Dp,
)

/**
 * The window's separating vertical fold, or null when it has none.
 *
 * Null is the answer for every phone, tablet and flat-open foldable, which is why the split below
 * treats it as "keep doing exactly what you did before".
 *
 * Bounds arrive in **window** coordinates, so a caller whose row is inset from the window edge has to
 * say so — [rememberTwoPaneSplit] takes that inset rather than assuming.
 */
@Composable
fun rememberVerticalHinge(): VerticalHinge? =
    rememberSeparatingFold(FoldingFeature.Orientation.VERTICAL) { bounds ->
        VerticalHinge(start = bounds.left.toDp(), width = bounds.width().toDp())
    }

/**
 * The window's separating horizontal fold, or null when it has none.
 *
 * Null for every ordinary window, same as [rememberVerticalHinge], and for the same reason: a caller
 * that gets null must keep the layout it already had.
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
 * property that answers "is the window really in two pieces", which is what both callers are asking.
 *
 * [toHinge] reads `bounds` in **window** pixels; it runs inside [Density] so each caller converts on
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
 * The split a two-pane row should use, aligned to the hinge when the window has one.
 *
 * [contentInset] is the padding the row applies to itself. The hinge is reported against the window,
 * so a row that insets its content sits in a different coordinate space; passing this wrong moves
 * the boundary off the hinge by that much rather than failing, which is why call sites pass their
 * own padding rather than a literal. Only the horizontal edges are read — the vertical ones are
 * accepted so a caller can hand over a `Scaffold`'s own `PaddingValues` unaltered.
 *
 * It is [PaddingValues] rather than a single `Dp` because a row's two horizontal insets are not
 * necessarily equal: `Scaffold` derives its padding from `safeDrawing`, and a display cutout or
 * gesture handle inset lands on one edge only in landscape (#2280). A symmetric parameter cannot
 * express that, and silently reading one edge for both is how a hinge alignment drifts.
 */
@Composable
fun rememberTwoPaneSplit(
    defaultLeadingWidth: Dp,
    defaultGap: Dp,
    contentInset: PaddingValues = PaddingValues(dimensionResource(R.dimen.zero)),
): TwoPaneSplit {
    val layoutDirection = LocalLayoutDirection.current
    val windowWidth = with(LocalDensity.current) {
        LocalWindowInfo.current.containerSize.width
            .toDp()
    }
    return twoPaneSplitInWindow(
        windowWidth = windowWidth,
        startInset = contentInset.calculateStartPadding(layoutDirection),
        endInset = contentInset.calculateEndPadding(layoutDirection),
        defaultLeadingWidth = defaultLeadingWidth,
        defaultGap = defaultGap,
        minPaneWidth = dimensionResource(R.dimen.two_pane_min_width),
        hinge = rememberVerticalHinge(),
        layoutDirection = layoutDirection,
    )
}

/**
 * [twoPaneSplit] in window coordinates: translates a window-reported [hinge] into the row's own
 * space before asking where the boundary goes.
 *
 * The translation subtracts the row's **physical left** inset, which is [startInset] under
 * [LayoutDirection.Ltr] and [endInset] under [LayoutDirection.Rtl] — `hinge.start` is measured from
 * the window's physical left edge and does not mirror with the layout direction, so subtracting the
 * *start* inset would be wrong by the difference between the two whenever they differ.
 *
 * Separate from [rememberTwoPaneSplit] because that one can only be exercised from a Compose test,
 * and this arithmetic is where the coordinate spaces get confused.
 */
internal fun twoPaneSplitInWindow(
    windowWidth: Dp,
    startInset: Dp,
    endInset: Dp,
    defaultLeadingWidth: Dp,
    defaultGap: Dp,
    minPaneWidth: Dp,
    hinge: VerticalHinge?,
    layoutDirection: LayoutDirection,
): TwoPaneSplit {
    val leftInset = if (layoutDirection == LayoutDirection.Ltr) startInset else endInset
    return twoPaneSplit(
        availableWidth = windowWidth - startInset - endInset,
        defaultLeadingWidth = defaultLeadingWidth,
        defaultGap = defaultGap,
        minPaneWidth = minPaneWidth,
        hinge = hinge?.let { VerticalHinge(it.start - leftInset, it.width) },
        layoutDirection = layoutDirection,
    )
}

/**
 * Where to put the pane boundary, given the row's own width and an optional [hinge].
 *
 * Two things it guarantees, and they are what the tests pin. **No hinge means the defaults, exactly**
 * — every non-foldable window keeps the layout it had. And a hinge that would leave either side under
 * [minPaneWidth] is ignored: a fold near the window edge is real, but honouring it hands one pane a
 * sliver, which is worse than a boundary a few dp off the crease.
 *
 * The gap is at least the hinge's own width, so the occluded strip falls *between* the panes and no
 * control is bisected by it.
 *
 * [hinge] is always reported from the window's *physical* left edge, but the `Row` this feeds places
 * its first child at the physical left only under [LayoutDirection.Ltr] — under
 * [LayoutDirection.Rtl] that child is placed from the physical *right* instead, same as any other
 * `Row` start-to-end child. [leadingWidth][TwoPaneSplit.leadingWidth] is a distance from the row's
 * start edge, not from the physical left, so it has to be measured from whichever physical edge is
 * the row's start for the given direction — otherwise the boundary is mirrored to the wrong side of
 * the crease on an RTL locale.
 */
fun twoPaneSplit(
    availableWidth: Dp,
    defaultLeadingWidth: Dp,
    defaultGap: Dp,
    minPaneWidth: Dp,
    hinge: VerticalHinge?,
    layoutDirection: LayoutDirection = LayoutDirection.Ltr,
): TwoPaneSplit {
    val default = TwoPaneSplit(defaultLeadingWidth, defaultGap)
    if (hinge == null) return default
    val leadingDistance =
        when (layoutDirection) {
            LayoutDirection.Ltr -> hinge.start
            LayoutDirection.Rtl -> availableWidth - hinge.end
        }
    val trailingDistance =
        when (layoutDirection) {
            LayoutDirection.Ltr -> availableWidth - hinge.end
            LayoutDirection.Rtl -> hinge.start
        }
    if (leadingDistance < minPaneWidth || trailingDistance < minPaneWidth) return default
    return TwoPaneSplit(leadingWidth = leadingDistance, gap = maxOf(hinge.width, defaultGap))
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
 * every window without a separating horizontal fold gets null, which is the same "keep doing what you
 * did before" contract [twoPaneSplit] gives the other axis.
 *
 * A crease close to the bottom edge is refused rather than honoured, for the reason [twoPaneSplit]
 * refuses a sliver pane: squeezing a sheet into [minHeight] of screen is a worse answer than letting
 * it cross a fold it was never going to clear.
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
