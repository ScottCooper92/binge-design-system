package com.binge.designsystem.tv.component

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.layout
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.unit.Dp
import com.binge.designsystem.R as DesR
import com.binge.designsystem.tv.R as TvR

/** Whether the [TvSideSheetPanel] around the rows runs to the screen's bottom edge, so the rows carry its overscan. */
internal val LocalTvSideSheetToBottomEdge = staticCompositionLocalOf { false }

/**
 * A sheet's rows: `padding_xs` apart, in a column that scrolls when they outgrow the panel, under the title and above
 * anything the sheet pins below them. On a panel that runs to the bottom edge the bottom overscan sits inside the scroll, so
 * the last row can scroll clear of the edge. Every sheet's rows go through it, so every sheet spaces them alike; a card the
 * sheet shows among them keeps its own spacing inside.
 *
 * A control that takes the focus ring, such as a text field or a stepper, draws it outside its bounds, and the scroll clips
 * to its viewport. So the viewport reaches a ring's width past the column's top and bottom, into the gaps around it, and the
 * rows sit that far inside the scroll: the first row's ring shows at the top, and the last row's once scrolled to the end.
 * The rows rest where they would without it. On a panel to the bottom edge the overscan inside the scroll already clears the
 * last ring, and the viewport stops at the screen's edge.
 */
@Composable
fun ColumnScope.TvSideSheetRows(
    modifier: Modifier = Modifier,
    scrollState: ScrollState = rememberScrollState(),
    content: @Composable ColumnScope.() -> Unit,
) {
    val toBottomEdge = LocalTvSideSheetToBottomEdge.current
    val ring = dimensionResource(TvR.dimen.tv_focus_ring_offset) + dimensionResource(TvR.dimen.tv_focus_ring_width)
    Column(
        modifier =
            modifier
                .weight(1f, fill = false)
                .fillMaxWidth()
                .outsetVertically(ring, atBottom = !toBottomEdge)
                .verticalScroll(scrollState)
                .padding(top = ring, bottom = if (toBottomEdge) dimensionResource(TvR.dimen.tv_overscan_vertical) else ring),
        verticalArrangement = Arrangement.spacedBy(dimensionResource(DesR.dimen.padding_xs)),
        content = content,
    )
}

/**
 * Lays the node out [outset] taller at the top, and at the bottom too when [atBottom], than the space it reports, so it draws
 * into the space around it.
 */
private fun Modifier.outsetVertically(outset: Dp, atBottom: Boolean): Modifier =
    layout { measurable, constraints ->
        val topPx = outset.roundToPx()
        val extra = if (atBottom) topPx * 2 else topPx
        val inner =
            if (constraints.hasBoundedHeight) {
                constraints.copy(minHeight = constraints.minHeight + extra, maxHeight = constraints.maxHeight + extra)
            } else {
                constraints.copy(minHeight = constraints.minHeight + extra)
            }
        val placeable = measurable.measure(inner)
        layout(placeable.width, (placeable.height - extra).coerceAtLeast(0)) { placeable.place(0, -topPx) }
    }
