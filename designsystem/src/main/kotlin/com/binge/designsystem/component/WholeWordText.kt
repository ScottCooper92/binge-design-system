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
 * It is a plain `Layout`, so it can sit under `IntrinsicSize` and in a lazy list. It composes the text three times: wrapping, wrapping
 * with an ellipsis, and on one line. It measures the wrapping one first and keeps it unless its layout broke a word, and then measures the
 * one-line one. When a bounded height cuts the wrapping text, it shows the one with an ellipsis, so the last line ends in `…` and not
 * through its glyphs. [maxLines] caps the wrapping text the same way: past it the last line ends in `…`. The
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
    maxLines: Int = Int.MAX_VALUE,
) {
    val layouts = remember { WholeWordLayouts() }
    Layout(
        content = {
            WholeWordLine(text, style, color, WholeWordMode.Wrapping, maxLines, onLayout = { layouts.wrapping = it })
            WholeWordLine(text, style, color, WholeWordMode.WrappingEllipsis, maxLines, onLayout = { layouts.ellipsized = it })
            WholeWordLine(text, style, color, WholeWordMode.OneLine, maxLines, onLayout = { layouts.oneLine = it })
        },
        modifier =
            modifier.semantics {
                this.text = AnnotatedString(text)
                getTextLayoutResult { results -> layouts.shown?.let { results.add(it) } != null }
            },
    ) { measurables, constraints ->
        val wrapping = measurables[0].measure(constraints)
        val breaksAWord = constraints.hasBoundedWidth && layouts.wrapping?.let { breaksAWord(it, text) } == true
        // A height that cuts the wrapping text would cut its last line through the glyphs, so that case shows the wrapping text with an
        // ellipsis instead. It is not used otherwise: an ellipsis on a long unbroken run truncates it where the clipped one wraps it.
        val cutByHeight =
            (constraints.hasBoundedHeight && (layouts.wrapping?.multiParagraph?.height ?: 0f) > constraints.maxHeight) ||
                layouts.wrapping?.didOverflowHeight == true
        val shown =
            when {
                breaksAWord -> measurables[2].measure(constraints)
                cutByHeight -> measurables[1].measure(constraints)
                else -> wrapping
            }
        layouts.shown =
            when {
                breaksAWord -> layouts.oneLine
                cutByHeight -> layouts.ellipsized
                else -> layouts.wrapping
            }
        layout(shown.width, shown.height) { shown.place(0, 0) }
    }
}

/** The text layouts the two lines of a [WholeWordText] last reported, and which of them showed. Written while measuring. */
private class WholeWordLayouts {
    var ellipsized: TextLayoutResult? = null
    var wrapping: TextLayoutResult? = null
    var oneLine: TextLayoutResult? = null
    var shown: TextLayoutResult? = null
}

/** Which of a [WholeWordText]'s texts a line is: wrapping and clipped, wrapping with an ellipsis, or one line with an ellipsis. */
private enum class WholeWordMode { Wrapping, WrappingEllipsis, OneLine }

@Composable
private fun WholeWordLine(
    text: String,
    style: TextStyle,
    color: Color,
    mode: WholeWordMode,
    maxLines: Int,
    onLayout: (TextLayoutResult) -> Unit,
) {
    Text(
        text = text,
        style = style,
        color = color,
        maxLines = if (mode == WholeWordMode.OneLine) 1 else maxLines,
        // The wrapping text clips: with an ellipsis it would truncate a long unbroken run instead of letting the line breaker wrap
        // it, which is what lets the check below see a word split.
        overflow = if (mode == WholeWordMode.Wrapping) TextOverflow.Clip else TextOverflow.Ellipsis,
        onTextLayout = onLayout,
        modifier = Modifier.clearAndSetSemantics {},
    )
}

/**
 * Whether any line of [layout] ends inside a word: a break that is not at a space, after a hyphen or dash, or beside a character of an
 * unspaced script. A break with such a character on either side is one the line breaker is allowed to make: no Latin word can be split by it.
 */
private fun breaksAWord(layout: TextLayoutResult, text: String): Boolean =
    (0 until layout.lineCount - 1).any { line ->
        val end = layout.getLineEnd(line)
        end in 1 until text.length && isBrokenWord(text.codePointBefore(end), text.codePointAt(end))
    }
