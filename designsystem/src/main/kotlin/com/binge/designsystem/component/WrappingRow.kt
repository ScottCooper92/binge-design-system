package com.binge.designsystem.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.IntrinsicMeasurable
import androidx.compose.ui.layout.IntrinsicMeasureScope
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.MeasurePolicy
import androidx.compose.ui.layout.MeasureResult
import androidx.compose.ui.layout.MeasureScope
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import com.binge.designsystem.R

/**
 * A row that wraps onto further lines, with its children centred on each line: chips, tags and short status text.
 *
 * Use it instead of `FlowRow` wherever a parent sizes itself from `IntrinsicSize.Min`, as [ListRow] does for its
 * card. `FlowRow` answers an intrinsic-height question with fewer lines than it then draws, so the parent comes out
 * too short and clips whatever sits below the wrapped line. This row counts every line it will draw.
 *
 * @param horizontalSpacing the gap between children on a line.
 * @param verticalSpacing the gap between lines.
 */
@Composable
fun WrappingRow(
    modifier: Modifier = Modifier,
    horizontalSpacing: Dp = dimensionResource(R.dimen.padding_xs),
    verticalSpacing: Dp = dimensionResource(R.dimen.padding_xs),
    content: @Composable () -> Unit,
) {
    Layout(
        content = content,
        modifier = modifier,
        measurePolicy = WrappingRowPolicy(horizontalSpacing, verticalSpacing),
    )
}

private class WrappingRowPolicy(
    private val horizontalSpacing: Dp,
    private val verticalSpacing: Dp,
) : MeasurePolicy {
    override fun MeasureScope.measure(measurables: List<Measurable>, constraints: Constraints): MeasureResult {
        val hGap = horizontalSpacing.roundToPx()
        val vGap = verticalSpacing.roundToPx()
        val loose = constraints.copy(minWidth = 0, minHeight = 0)
        val lines = mutableListOf<List<Placeable>>()
        val current = mutableListOf<Placeable>()
        var used = 0
        for (measurable in measurables) {
            val placeable = measurable.measure(loose)
            if (current.isNotEmpty() && used + hGap + placeable.width > constraints.maxWidth) {
                lines += current.toList()
                current.clear()
                used = 0
            }
            used = if (current.isEmpty()) placeable.width else used + hGap + placeable.width
            current += placeable
        }
        if (current.isNotEmpty()) lines += current.toList()
        val lineHeights = lines.map { line -> line.maxOf { it.height } }
        val lineWidths = lines.map { line -> line.sumOf { it.width } + hGap * (line.size - 1) }
        val width = (lineWidths.maxOrNull() ?: 0).coerceIn(constraints.minWidth, constraints.maxWidth)
        val height =
            (lineHeights.sum() + vGap * (lines.size - 1).coerceAtLeast(0))
                .coerceIn(constraints.minHeight, constraints.maxHeight)
        return layout(width, height) {
            var y = 0
            lines.forEachIndexed { index, line ->
                var x = 0
                line.forEach { placeable ->
                    placeable.placeRelative(x, y + (lineHeights[index] - placeable.height) / 2)
                    x += placeable.width + hGap
                }
                y += lineHeights[index] + vGap
            }
        }
    }

    override fun IntrinsicMeasureScope.minIntrinsicWidth(measurables: List<IntrinsicMeasurable>, height: Int): Int =
        measurables.maxOfOrNull { it.minIntrinsicWidth(height) } ?: 0

    override fun IntrinsicMeasureScope.maxIntrinsicWidth(measurables: List<IntrinsicMeasurable>, height: Int): Int {
        val hGap = horizontalSpacing.roundToPx()
        return measurables.sumOf { it.maxIntrinsicWidth(height) } + hGap * (measurables.size - 1).coerceAtLeast(0)
    }

    override fun IntrinsicMeasureScope.minIntrinsicHeight(measurables: List<IntrinsicMeasurable>, width: Int): Int =
        wrappedHeight(measurables, width)

    override fun IntrinsicMeasureScope.maxIntrinsicHeight(measurables: List<IntrinsicMeasurable>, width: Int): Int =
        wrappedHeight(measurables, width)

    /** Lays the children out greedily at their preferred widths, as [measure] does, and sums the line heights. */
    private fun IntrinsicMeasureScope.wrappedHeight(measurables: List<IntrinsicMeasurable>, width: Int): Int {
        val hGap = horizontalSpacing.roundToPx()
        val vGap = verticalSpacing.roundToPx()
        var total = 0
        var lineHeight = 0
        var used = 0
        var onLine = 0
        var lines = 0
        for (measurable in measurables) {
            val childWidth = measurable.maxIntrinsicWidth(Constraints.Infinity).coerceAtMost(width)
            // Line emptiness is a count, as in `measure`, not an inference from the height or width so far: a child of
            // zero height or width still takes a place on its line and charges a gap.
            if (onLine > 0 && used + hGap + childWidth > width) {
                total += lineHeight
                lines++
                lineHeight = 0
                used = 0
                onLine = 0
            }
            used = if (onLine == 0) childWidth else used + hGap + childWidth
            onLine++
            lineHeight = maxOf(lineHeight, measurable.minIntrinsicHeight(childWidth))
        }
        if (onLine > 0) {
            total += lineHeight
            lines++
        }
        return total + vGap * (lines - 1).coerceAtLeast(0)
    }
}
