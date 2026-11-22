package com.binge.designsystem.component

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.getTextLayoutResult
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.text
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow

/**
 * Text that wraps where the line breaker puts its breaks (at spaces, after a hyphen, between CJK characters) and never breaks
 * a word in two. When laying [text] out in the room it is given would split a word, it stays on one line and ends in an
 * ellipsis, where [Text] would break the word across lines. Use it for a name or a label that wraps freely at a normal size but
 * may meet a narrow slot at the largest font.
 *
 * It is a plain `Layout`, so it can sit under `IntrinsicSize` and in a lazy list. It composes the text twice, wrapping and on one
 * line, measures the wrapping one first and keeps it unless its layout broke a word, and then measures the one-line one. The
 * unused text is never placed and carries no semantics, so the semantics tree holds the text once, on this node, with the layout
 * of the one that shows. An intrinsic size is that of the wrapping text.
 *
 * @need show a name or label that wraps at its spaces but never breaks a word in two
 * @group rows
 * @replaces a Text that breaks a long word across lines at a large font
 */
@Composable
fun WholeWordText(
    text: String,
    style: TextStyle,
    color: Color,
    modifier: Modifier = Modifier,
) {
    val layouts = remember { WholeWordLayouts() }
    Layout(
        content = {
            WholeWordLine(text, style, color, oneLine = false, onLayout = { layouts.wrapping = it })
            WholeWordLine(text, style, color, oneLine = true, onLayout = { layouts.oneLine = it })
        },
        modifier =
            modifier.semantics {
                this.text = AnnotatedString(text)
                getTextLayoutResult { results -> layouts.shown?.let { results.add(it) } != null }
            },
    ) { measurables, constraints ->
        val wrapping = measurables[0].measure(constraints)
        val breaksAWord = constraints.hasBoundedWidth && layouts.wrapping?.let { breaksAWord(it, text) } == true
        val shown = if (breaksAWord) measurables[1].measure(constraints) else wrapping
        layouts.shown = if (breaksAWord) layouts.oneLine else layouts.wrapping
        layout(shown.width, shown.height) { shown.place(0, 0) }
    }
}

/** The text layouts the two lines of a [WholeWordText] last reported, and which of them showed. Written while measuring. */
private class WholeWordLayouts {
    var wrapping: TextLayoutResult? = null
    var oneLine: TextLayoutResult? = null
    var shown: TextLayoutResult? = null
}

@Composable
private fun WholeWordLine(
    text: String,
    style: TextStyle,
    color: Color,
    oneLine: Boolean,
    onLayout: (TextLayoutResult) -> Unit,
) {
    Text(
        text = text,
        style = style,
        color = color,
        maxLines = if (oneLine) 1 else Int.MAX_VALUE,
        overflow = TextOverflow.Ellipsis,
        onTextLayout = onLayout,
        modifier = Modifier.clearAndSetSemantics {},
    )
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
