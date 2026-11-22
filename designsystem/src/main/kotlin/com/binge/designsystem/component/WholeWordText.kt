package com.binge.designsystem.component

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextOverflow

/**
 * Text that wraps at its spaces and never breaks a word in two. When the longest word of [text] is wider than the room the
 * text is given, it stays on one line and ends in an ellipsis, where [Text] would break the word across lines. Use it for a
 * name or a label that wraps freely at a normal size but may meet a narrow slot at the largest font.
 *
 * It decides while measuring, so one pass is enough, and it composes one [Text] only, so the semantics tree holds the text
 * once. Like any `SubcomposeLayout` it cannot answer an intrinsic size query: do not put it under `IntrinsicSize`.
 */
@Composable
internal fun WholeWordText(
    text: String,
    style: TextStyle,
    color: Color,
    modifier: Modifier = Modifier,
) {
    val measurer = rememberTextMeasurer()
    SubcomposeLayout(modifier) { constraints ->
        val longestWord = text.split(WHITESPACE).maxOf { measurer.measure(it, style).size.width }
        val oneLine = constraints.hasBoundedWidth && longestWord > constraints.maxWidth
        val placeable =
            subcompose(Unit) {
                Text(
                    text = text,
                    style = style,
                    color = color,
                    maxLines = if (oneLine) 1 else Int.MAX_VALUE,
                    overflow = TextOverflow.Ellipsis,
                )
            }.first().measure(constraints)
        layout(placeable.width, placeable.height) { placeable.place(0, 0) }
    }
}

private val WHITESPACE = Regex("\\s+")
