package com.binge.designsystem.component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp

/**
 * A hero's height as last laid out, for a screen that sets [DetailOverlayTopBar] over its own hero. A hero that grows
 * past its floor at a large font scale would otherwise fade the bar early, since the bar fades on the height it is
 * given. Attach [measuredHeroHeight] to the hero's outermost node, and pass [height] as the bar's `heroHeight`.
 */
@Stable
class MeasuredHeroHeight internal constructor() {
    internal var px by mutableIntStateOf(0)

    /** The measured height, or [fallback] before the hero has been laid out. */
    @Composable
    fun height(fallback: Dp): Dp = if (px > 0) with(LocalDensity.current) { px.toDp() } else fallback
}

/** Remembers a [MeasuredHeroHeight] for one hero. */
@Composable
fun rememberMeasuredHeroHeight(): MeasuredHeroHeight = remember { MeasuredHeroHeight() }

/** Reports this node's laid-out height to [measured]. */
fun Modifier.measuredHeroHeight(measured: MeasuredHeroHeight): Modifier = onSizeChanged { measured.px = it.height }
