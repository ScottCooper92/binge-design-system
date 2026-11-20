package com.binge.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.unit.Density
import com.binge.designsystem.R
import com.binge.designsystem.theme.BingeShapes
import com.binge.designsystem.theme.labelSmallEmphasis

/** The largest count shown whole; above it the badge reads "99+". */
private const val MAX_SHOWN_COUNT = 99

/**
 * A number in a circle, for a count that sits on another control: the filters applied, the items waiting. The circle is
 * [R.dimen.icon_size_l] across, and a count of three digits or more grows it into a pill rather than clipping the number.
 *
 * The number is drawn at a font scale of 1, so it does not grow with the system font: a badge that did would outgrow the
 * control it sits on (an earlier copy clipped at 2x). That is a deliberate exception to scaling text, and it holds because the badge
 * is a mark on a control, not copy: the control it sits on carries the count in its own accessibility description, so the badge adds
 * nothing for a screen reader to read twice. A count above 99 reads "99+".
 *
 * @param count the number to show.
 * @param containerColor the circle's fill.
 * @param contentColor the number's colour. Pass one with the contrast the caller needs against [containerColor].
 *
 * @need put a small count on a control
 * @group cards
 * @replaces a hand-built circle with a number in it
 */
@Composable
fun CountBadge(
    count: Int,
    containerColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current
    val size = dimensionResource(R.dimen.icon_size_l)
    CompositionLocalProvider(LocalDensity provides Density(density.density, fontScale = 1f)) {
        Box(
            modifier = modifier
                .defaultMinSize(minWidth = size, minHeight = size)
                .clip(BingeShapes.Pill)
                .background(containerColor)
                .padding(horizontal = dimensionResource(R.dimen.padding_xs)),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = if (count > MAX_SHOWN_COUNT) "$MAX_SHOWN_COUNT+" else count.toString(),
                style = MaterialTheme.typography.labelSmallEmphasis,
                color = contentColor,
                maxLines = 1,
                softWrap = false,
            )
        }
    }
}
