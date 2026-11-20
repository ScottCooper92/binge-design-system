package com.binge.designsystem.tv.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.TextStyle
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.binge.designsystem.theme.BingeShapes
import com.binge.designsystem.R as DesR

/**
 * One line of text as a bar of [widthFraction]. The line's height comes from an empty `Text` at [style], so it is what the
 * real text measures rather than a dimen that drifts from the type scale; the bar is inset within it, so stacked lines read as lines.
 *
 * @need draw one text line as a loading plate that keeps the line's height
 * @group tv
 * @sample TvSkeletonLineSample
 */
@Composable
fun TvSkeletonLine(
    style: TextStyle,
    widthFraction: Float,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.surfaceVariant,
) {
    Box(modifier.fillMaxWidth(widthFraction)) {
        Text(text = "", style = style)
        Box(
            Modifier
                .matchParentSize()
                .padding(vertical = dimensionResource(DesR.dimen.padding_xs))
                .clip(BingeShapes.Tag)
                .background(color),
        )
    }
}

/**
 * The meta line as a bar of [widthFraction], over a real [TvExpandingMeta] holding nothing: that line's height is the rating badge's,
 * not a text line's, so the plate takes the line's height from the line itself rather than from a dimen that drifts from it.
 */
@Composable
internal fun TvSkeletonMetaLine(
    widthFraction: Float,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.surfaceVariant,
) {
    Box(modifier) {
        TvExpandingMeta(
            genres = emptyList(),
            trailingFacts = emptyList(),
            rating = null,
            certification = null,
            modifier = Modifier.fillMaxWidth(),
        )
        Box(Modifier.matchParentSize()) {
            Box(
                Modifier
                    .fillMaxWidth(widthFraction)
                    .fillMaxHeight()
                    .padding(vertical = dimensionResource(DesR.dimen.padding_xs))
                    .clip(BingeShapes.Tag)
                    .background(color),
            )
        }
    }
}

/**
 * [lines] lines of text as bars, the last [lastLineFraction] as wide. The block's height is [lines] lines of [style], as the real
 * text clamped to that many reserves, which separate empty `Text`s do not: they measure short of a real line.
 */
@Composable
internal fun TvSkeletonParagraph(
    style: TextStyle,
    lines: Int,
    lastLineFraction: Float,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.surfaceVariant,
) {
    Box(modifier) {
        Text(text = "", style = style, minLines = lines, maxLines = lines, modifier = Modifier.fillMaxWidth())
        Box(Modifier.matchParentSize()) {
            Column(Modifier.fillMaxSize()) {
                repeat(lines) { line ->
                    Box(Modifier.weight(1f).fillMaxWidth()) {
                        Box(
                            Modifier
                                .fillMaxWidth(if (line == lines - 1) lastLineFraction else 1f)
                                .fillMaxHeight()
                                .padding(vertical = dimensionResource(DesR.dimen.padding_xs))
                                .clip(BingeShapes.Tag)
                                .background(color),
                        )
                    }
                }
            }
        }
    }
}
