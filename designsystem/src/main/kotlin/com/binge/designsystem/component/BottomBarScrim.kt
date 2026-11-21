package com.binge.designsystem.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.unit.Dp
import com.binge.designsystem.R

/**
 * The fade that lets content scrolling under a floating bottom element (a comment composer, a floating action bar) dissolve instead of running
 * sharp to the screen's edge. It is [TopBarScrim]'s ramp mirrored, drawn from the same stops: transparent at its top, rising to the same strong
 * stop at the bottom edge, in [scrimColor] (the theme's background by default, so it reads as the page fading out).
 *
 * Call it as the first child of the [Box] the floating element sits in, so it fills that box and the element draws over it. The box is the
 * element and the room below it down to the screen's bottom, **navigation-bar inset included**: put the inset's padding on the element inside
 * the box, not on the box, so the scrim runs to the very edge. [tailHeight] extends the paint area above the box, so the fade starts that far
 * above the element and has room to thin out rather than cutting in at its top edge; the ramp's stops are rescaled onto the taller area.
 *
 * [fraction] scales the whole ramp, 1 by default. A screen whose content only sometimes runs under the element passes 0 while nothing does and
 * 1 once something does, so the scrim does not paint a band over a bare page. At 0 nothing is drawn. The scrim paints past the box's top edge,
 * so it must sit over the content it fades, never in a box that clips.
 *
 * @need fade content that scrolls under a floating bottom element
 * @group bars
 * @sample BottomBarScrimSample
 * @replaces a hand-built bottom gradient behind a floating composer or action bar
 */
@Composable
fun BoxScope.BottomBarScrim(
    modifier: Modifier = Modifier,
    fraction: Float = 1f,
    tailHeight: Dp = dimensionResource(R.dimen.bottom_bar_scrim_tail),
    scrimColor: Color = MaterialTheme.colorScheme.background,
) {
    if (fraction <= 0f) return
    Box(
        modifier = modifier
            .matchParentSize()
            .drawBehind {
                val tailPx = tailHeight.toPx()
                val paintHeight = size.height + tailPx
                val barFraction = size.height / paintHeight
                // The top ramp's stops mirrored: its first stop (strongest) is this one's last.
                val stops = scrimRamp(scrimColor, fraction, barFraction).asReversed().map { (1f - it.first) to it.second }
                drawRect(
                    brush = Brush.verticalGradient(*stops.toTypedArray(), startY = -tailPx, endY = size.height),
                    topLeft = Offset(0f, -tailPx),
                    size = Size(size.width, paintHeight),
                )
            },
    )
}
