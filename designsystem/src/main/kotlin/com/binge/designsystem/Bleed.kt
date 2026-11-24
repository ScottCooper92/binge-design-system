package com.binge.designsystem

import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.layout
import androidx.compose.ui.unit.Dp

/**
 * Lays the content out [by] wider than the space it is given on each side, so it runs under the gutter of the column around it: a
 * scrolling tab or chip row that should reach the screen's edges inside a padded column. The node still reports the width it was
 * given, so nothing beside it moves. Pair it with an edge padding of the same size and the first item still lines up with the column.
 * The content is expected to fill that width: it is measured from the left edge of the bled area, not centred. Under an unbounded
 * width, such as inside a horizontal scroll, there is no gutter to run under and the content is laid out as it was.
 * A [by] of zero or less leaves the modifier as it was.
 */
fun Modifier.bleedHorizontally(by: Dp): Modifier =
    if (by.value <= 0f) {
        this
    } else {
        layout { measurable, constraints ->
            if (!constraints.hasBoundedWidth) {
                val unchanged = measurable.measure(constraints)
                return@layout layout(unchanged.width, unchanged.height) { unchanged.place(0, 0) }
            }
            val extra = by.roundToPx()
            val placeable = measurable.measure(constraints.copy(minWidth = 0, maxWidth = constraints.maxWidth + 2 * extra))
            layout(constraints.maxWidth, placeable.height) { placeable.place(-extra, 0) }
        }
    }
