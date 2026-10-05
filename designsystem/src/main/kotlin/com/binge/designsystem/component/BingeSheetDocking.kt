package com.binge.designsystem.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.util.lerp
import com.binge.designsystem.R
import com.binge.designsystem.theme.BingeShapes
import kotlin.math.roundToInt

/**
 * A docking [BingeBottomSheet]'s live geometry, read by its content through [LocalBingeSheetDock].
 *
 * [fraction] runs 0 → 1 over the last stretch of travel before the sheet's top edge reaches the bottom of the
 * status bar. It is measured from where the sheet actually is, mid-drag included, not from its settled value.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Stable
class BingeSheetDock internal constructor(
    private val sheetState: SheetState,
    private val statusBarBottom: () -> Int,
    private val travelPx: Float,
    /** Hides the sheet, then dismisses it: what a docked bar's close does. */
    val close: () -> Unit,
) {
    /** How docked the sheet is: 0 while it floats clear of the status bar, 1 once its top edge is under it. */
    val fraction: Float
        get() = sheetTopOrNull()?.let { dockFraction(it, statusBarBottom().toFloat(), travelPx) } ?: 0f

    // requireOffset throws until the sheet has been measured and given its anchors.
    private fun sheetTopOrNull(): Float? = runCatching { sheetState.requireOffset() }.getOrNull()?.takeUnless { it.isNaN() }
}

/** The dock of the [BingeBottomSheet] this content sits in, or null outside a docking sheet. */
val LocalBingeSheetDock = staticCompositionLocalOf<BingeSheetDock?> { null }

/**
 * How docked a sheet whose top edge sits at [sheetTop] is: 0 while it is at least [travel] below [statusBarBottom],
 * rising to 1 as it reaches it.
 */
internal fun dockFraction(
    sheetTop: Float,
    statusBarBottom: Float,
    travel: Float,
): Float = ((statusBarBottom + travel - sheetTop) / travel).coerceIn(0f, 1f)

/**
 * How far to lift a footer whose laid-out bottom edge is at [naturalBottom] so it sits on top of the navigation bar.
 * A footer already above the bar stays put.
 */
internal fun pinnedFooterLift(
    naturalBottom: Float,
    windowHeight: Int,
    navigationBarHeight: Int,
): Float = (naturalBottom - (windowHeight - navigationBarHeight)).coerceAtLeast(0f)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun rememberBingeSheetDock(sheetState: SheetState, close: () -> Unit): BingeSheetDock {
    val density = LocalDensity.current
    val statusBars = WindowInsets.statusBars
    val travelPx = with(density) { dimensionResource(R.dimen.sheet_dock_travel).toPx() }
    val latestClose by rememberUpdatedState(close)
    return remember(sheetState, statusBars, travelPx, density) {
        BingeSheetDock(sheetState, { statusBars.getTop(density) }, travelPx, { latestClose() })
    }
}

/** A docking sheet's outline: [BingeShapes.HeroTop]'s top corners, squared off as [fraction] reaches 1. */
internal data class DockingSheetShape(
    private val fraction: Float,
) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density,
    ): Outline {
        val corner = CornerRadius(BingeShapes.HeroTop.topStart.toPx(size, density) * (1f - fraction))
        return Outline.Rounded(
            RoundRect(
                left = 0f,
                top = 0f,
                right = size.width,
                bottom = size.height,
                topLeftCornerRadius = corner,
                topRightCornerRadius = corner,
            ),
        )
    }
}

/**
 * The top of a docking sheet: the drag handle over [header] while the sheet floats, crossfading to [dockedTopBar]
 * as it docks under the status bar. The region's height follows the crossfade, so the content below moves into the
 * space the header gives back. Outside a docking sheet (a side sheet, say) it is [header] alone. [dockedTopBar] is
 * handed the sheet's close.
 */
@Composable
fun BingeSheetDockingHeader(
    header: @Composable () -> Unit,
    dockedTopBar: @Composable (onClose: () -> Unit) -> Unit,
    modifier: Modifier = Modifier,
) {
    val dock = LocalBingeSheetDock.current
    if (dock == null) {
        Box(modifier) { header() }
        return
    }
    DockingHeaderLayout(
        fraction = { dock.fraction },
        header = header,
        dockedTopBar = { dockedTopBar(dock.close) },
        modifier = modifier,
    )
}

/**
 * [BingeSheetDockingHeader]'s crossfade at a given [fraction], with no sheet behind it, so a frame can render any
 * point of the dock. [fraction] is read during layout, so a drag relayouts the header without recomposing it.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun DockingHeaderLayout(
    fraction: () -> Float,
    header: @Composable () -> Unit,
    dockedTopBar: @Composable () -> Unit,
    modifier: Modifier = Modifier,
) {
    Layout(
        contents =
            listOf(
                {
                    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                        BottomSheetDefaults.DragHandle()
                        header()
                    }
                },
                dockedTopBar,
            ),
        modifier = modifier.fillMaxWidth().clipToBounds(),
    ) { (floatingMeasurables, dockedMeasurables), constraints ->
        val docking = fraction()
        val loose = constraints.copy(minHeight = 0)
        val floating = floatingMeasurables.map { it.measure(loose) }
        val docked = dockedMeasurables.map { it.measure(loose) }
        val floatingHeight = floating.maxOfOrNull { it.height } ?: 0
        val dockedHeight = docked.maxOfOrNull { it.height } ?: 0
        val height = lerp(floatingHeight.toFloat(), dockedHeight.toFloat(), docking).roundToInt()
        layout(constraints.maxWidth, height) {
            if (docking < 1f) floating.forEach { it.placeWithLayer(0, 0) { alpha = 1f - docking } }
            if (docking > 0f) docked.forEach { it.placeWithLayer(0, 0) { alpha = docking } }
        }
    }
}

/**
 * Keeps a docking sheet's footer on the window's bottom edge, above the navigation bar, whatever the sheet's height.
 * The sheet lays its content out at full height and slides it, so a partially expanded sheet would otherwise carry
 * the footer below the screen. The lift is measured from the footer's own position in the window and applied in its
 * layer, so a drag moves it without a relayout. The strip under it, down to the window edge, is painted in [backing]
 * so content sliding beneath does not show through. Outside a docking sheet ([dock] null) it does nothing.
 */
@Composable
fun Modifier.bingeSheetPinnedFooter(dock: BingeSheetDock?, backing: Color): Modifier {
    if (dock == null) return this
    val windowHeight = LocalWindowInfo.current.containerSize.height
    val navigationBarHeight = WindowInsets.navigationBars.getBottom(LocalDensity.current)
    var naturalBottom by remember { mutableFloatStateOf(Float.NaN) }
    return onGloballyPositioned { naturalBottom = it.positionInWindow().y + it.size.height }
        .graphicsLayer {
            translationY = if (naturalBottom.isNaN()) 0f else -pinnedFooterLift(naturalBottom, windowHeight, navigationBarHeight)
        }.drawBehind { drawRect(backing, size = Size(size.width, size.height + navigationBarHeight)) }
}
