package com.binge.designsystem.component

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints

/**
 * Text that wraps where the line breaker puts its breaks (at spaces, after a hyphen, between CJK characters) and never breaks
 * a word in two. When laying [text] out in the room it is given would split a word, it stays on one line and ends in an ellipsis, where [Text] would break the word across lines. Use it for a
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
        val oneLine =
            constraints.hasBoundedWidth &&
                breaksAWord(measurer.measure(text, style, constraints = Constraints(maxWidth = constraints.maxWidth)), text)
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

/** Whether any line of [layout] ends inside a word: a break that is not at a space, after a hyphen or dash, or between CJK characters. */
private fun breaksAWord(layout: TextLayoutResult, text: String): Boolean =
    (0 until layout.lineCount - 1).any { line ->
        val end = layout.getLineEnd(line)
        end in 1 until text.length && isMidWord(text[end - 1], text[end])
    }

private fun isMidWord(before: Char, after: Char): Boolean =
    !before.isWhitespace() && !after.isWhitespace() && before !in BREAK_AFTER && !(before.isCjk() && after.isCjk())

private fun Char.isCjk(): Boolean =
    Character.UnicodeScript.of(code) in
        setOf(
            Character.UnicodeScript.HAN,
            Character.UnicodeScript.HIRAGANA,
            Character.UnicodeScript.KATAKANA,
            Character.UnicodeScript.HANGUL,
        )

private val BREAK_AFTER = setOf('-', '/', '\u2013', '\u2014', '\u2010')
