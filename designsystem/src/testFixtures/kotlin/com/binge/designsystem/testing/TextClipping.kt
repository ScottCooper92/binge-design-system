package com.binge.designsystem.testing

import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsNode
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.unit.IntSize

/**
 * Fails when text is laid out larger than the part of it a parent lets through.
 *
 * It is how a test catches a row that is too short for its content, such as a card sized from an intrinsic height
 * that undercounts a wrapped line: the line below is squeezed to nothing or clipped away. For each node under [root]
 * with text, the size its text needs (the lines the layout drew, as `TextLayoutResult` reports them) is compared
 * with the node's visible bounds: its own box clipped to the bounds of every ancestor, whether or not that ancestor has a
 * clip modifier. So text that overflows a parent which does not clip, and is still drawn, is reported too. That errs on
 * the strict side, which suits a check for a layout that is too small for its content. The height is the larger
 * of the two, so a text squeezed to nothing by a parent that ran out of room is caught as well as one clipped away. A
 * shortfall of more than [tolerancePx] in width or height fails the test, naming the text and the shortfall.
 *
 * Five further faults fail the same way, each found from the text's own layout and not from its parents:
 * - the text needs more height than its node has, by more than [tolerancePx], so a line is cut off;
 * - a one-line text needs more width than its node has, by more than [tolerancePx], and is not ellipsized, so the end of the line is cut off;
 * - a line is ellipsized down to the ellipsis alone, with none of its text left, which tells the reader nothing;
 * - a word is broken across two lines: a line other than the last ends between two characters that are neither space nor a
 *   place a break is welcome (after a hyphen, a slash or a dash, or between two CJK characters, which have no spaces);
 * - the text's box reaches past the left or right edge of the window. The window is [windowWidthPx] wide from the left edge
 *   of the root, or, when it is null, the edges of [root] itself, so a subtree that does not start at the window's left edge is
 *   held to its own bounds;
 * - the text's box is larger than its parent's by more than [tolerancePx] on either axis, unless the parent scrolls.
 *
 * It does not use `TextLayoutResult.hasVisualOverflow`. That is true for every `maxLines` with an ellipsis, which
 * is deliberate and not a bug. The layout of an ellipsized text holds only the lines it shows, with the ellipsis
 * inside the width, so the text fits its box and passes here.
 *
 * Skipped, so that what is not on screen never fails:
 * - text inside a scroll container that is not wholly in view: a clipped item there is simply scrolled out, and only
 *   its edge is cut by the container. A text that is wholly inside the container's viewport is still checked, because
 *   anything cutting it is then something other than scrolling;
 * - text whose own position lies outside the root;
 * - empty text, which has no glyph for a parent to cut. A bar that is given no title draws one, and it is no bigger than
 *   the nothing it holds.
 *
 * [allowBrokenWords] switches the broken-word check off for a caller whose text has no good place to break, a URL for one, and
 * says why where it does; each such caller names the issue that tracks it.
 *
 * The unmerged semantics tree is searched, so a merged parent does not hide the text inside it.
 *
 * Returns how many text nodes it checked, so a test can tell a pass from a pass that skipped everything because the
 * content was in a scroll container or off screen.
 */
fun ComposeTestRule.assertNoTextClipped(
    root: SemanticsNodeInteraction = onRoot(useUnmergedTree = true),
    tolerancePx: Int = 1,
    windowWidthPx: Int? = null,
    allowBrokenWords: Boolean = false,
): Int {
    val rootNode = root.fetchSemanticsNode()
    val viewport = rootNode.boundsInRoot
    val windowLeft = if (windowWidthPx == null) viewport.left else 0f
    val windowRight = windowWidthPx?.toFloat() ?: viewport.right
    val clipped = mutableListOf<ClippedText>()
    val checked = intArrayOf(0)
    collectClipped(rootNode, viewport, null, tolerancePx, windowLeft, windowRight, allowBrokenWords, clipped, checked)
    if (clipped.isNotEmpty()) {
        throw AssertionError(
            "Text does not fit (sizes in px):\n" +
                clipped.joinToString("\n") { it.describe() },
        )
    }
    return checked[0]
}

private data class ClippedText(
    val text: String,
    val shortfallWidth: Int,
    val shortfallHeight: Int,
    val ownWidth: Int,
    val ownHeight: Int,
    val visibleWidth: Int,
    val visibleHeight: Int,
    val fault: String? = null,
) {
    fun describe(): String =
        if (fault != null) {
            "  \"$text\": $fault"
        } else {
            "  \"$text\": ${ownWidth}x$ownHeight, visible ${visibleWidth}x$visibleHeight" +
                " (short ${shortfallWidth}px wide, ${shortfallHeight}px high)"
        }
}

private fun collectClipped(
    node: SemanticsNode,
    viewport: Rect,
    scrollViewport: Rect?,
    tolerancePx: Int,
    windowLeft: Float,
    windowRight: Float,
    allowBrokenWords: Boolean,
    out: MutableList<ClippedText>,
    checked: IntArray,
) {
    val config = node.config
    val scrolls =
        config.contains(SemanticsProperties.VerticalScrollAxisRange) ||
            config.contains(SemanticsProperties.HorizontalScrollAxisRange)
    val inScroll = if (scrolls) node.boundsInRoot else scrollViewport
    val text = config.getOrNull(SemanticsProperties.Text)?.joinToString(" ") { it.text }
    if (!text.isNullOrEmpty()) {
        val bounds = node.boundsInRoot
        val needed = node.neededTextSize()
        if (needed != null && node.isInside(viewport) && node.isFullyInside(inScroll, tolerancePx)) {
            checked[0]++
            val visibleWidth = bounds.width.toInt()
            val visibleHeight = bounds.height.toInt()
            // Width is the node's own box against what the parents let through. The text's own line width is not used,
            // because an ellipsized line reports its full unellipsized width.
            val shortWidth = node.size.width - visibleWidth
            val shortHeight = maxOf(needed.height, node.size.height) - visibleHeight
            if (shortWidth > tolerancePx || shortHeight > tolerancePx) {
                out +=
                    ClippedText(
                        text = text.take(MAX_TEXT_IN_MESSAGE),
                        shortfallWidth = maxOf(shortWidth, 0),
                        shortfallHeight = maxOf(shortHeight, 0),
                        ownWidth = node.size.width,
                        ownHeight = maxOf(needed.height, node.size.height),
                        visibleWidth = visibleWidth,
                        visibleHeight = visibleHeight,
                    )
            }
        }
    }
    if (!text.isNullOrEmpty() && node.isInside(viewport) && node.isFullyInside(inScroll, tolerancePx)) {
        node.layoutFaults(windowLeft, windowRight, tolerancePx, allowBrokenWords).forEach { fault ->
            out +=
                ClippedText(
                    text.take(MAX_TEXT_IN_MESSAGE),
                    0,
                    0,
                    node.size.width,
                    node.size.height,
                    node.size.width,
                    node.size.height,
                    fault,
                )
        }
    }
    node.children.forEach { collectClipped(it, viewport, inScroll, tolerancePx, windowLeft, windowRight, allowBrokenWords, out, checked) }
}

/** What is wrong with how the node's text sits in its own box, apart from what a parent clips: see [assertNoTextClipped]. */
private fun SemanticsNode.layoutFaults(
    windowLeft: Float,
    windowRight: Float,
    tolerancePx: Int,
    allowBrokenWords: Boolean,
): List<String> {
    val faults = mutableListOf<String>()
    val results = mutableListOf<TextLayoutResult>()
    config.getOrNull(SemanticsActions.GetTextLayoutResult)?.action?.invoke(results)
    val layout = results.firstOrNull()
    val paragraph = layout?.multiParagraph
    if (paragraph != null) {
        val needed = kotlin.math.ceil(paragraph.height).toInt()
        if (needed > size.height + tolerancePx) faults += "needs ${needed}px of height, its box is ${size.height}px"
        if (paragraph.lineCount == 1 && paragraph.maxIntrinsicWidth > size.width + tolerancePx && !paragraph.isLineEllipsized(0)) {
            faults += "needs ${kotlin.math.ceil(paragraph.maxIntrinsicWidth).toInt()}px of width on one line, its box is ${size.width}px"
        }
        for (line in 0 until paragraph.lineCount) {
            if (paragraph.isLineEllipsized(line) && paragraph.getLineStart(line) == paragraph.getLineEnd(line, visibleEnd = true)) {
                faults += "draws only an ellipsis on line ${line + 1}, its box is ${size.width}px wide"
            }
        }
        val text = layout.layoutInput.text.text
        for (line in 0 until if (allowBrokenWords) 0 else paragraph.lineCount - 1) {
            val end = paragraph.getLineEnd(line, visibleEnd = false)
            if (end in 1 until text.length && isBrokenWord(text.codePointBefore(end), text.codePointAt(end))) {
                faults += "breaks a word across lines after \"${text.substring(maxOf(0, end - WORD_CONTEXT), end)}\""
            }
        }
    }
    val left = positionInRoot.x
    val right = left + size.width
    if (left < windowLeft - tolerancePx || right > windowRight + tolerancePx) {
        faults += "reaches past the window: ${left.toInt()}..${right.toInt()} in a window ${windowLeft.toInt()}..${windowRight.toInt()}"
    }
    val parentNode = parent
    val parentScrolls =
        parentNode?.config?.let {
            it.contains(SemanticsProperties.VerticalScrollAxisRange) || it.contains(SemanticsProperties.HorizontalScrollAxisRange)
        } ?: true
    if (parentNode != null &&
        !parentScrolls &&
        (size.width > parentNode.size.width + tolerancePx || size.height > parentNode.size.height + tolerancePx)
    ) {
        faults += "is ${size.width}x${size.height}, larger than its parent, ${parentNode.size.width}x${parentNode.size.height}"
    }
    return faults
}

/**
 * Whether a line that ends between the code points [before] and [after] has split a word: neither is a space, the break is not after a
 * hyphen or dash, and neither side is a character of a script written without spaces (CJK, Thai, Lao, Khmer, Myanmar), beside which the
 * line breaker may break and which no Latin word can be split by. It is the rule `WholeWordText` uses to decide when to stop wrapping,
 * so what that component wraps passes here. Public so a test can pin the rule on its own, without a line breaker deciding where the
 * line ends.
 */
fun isBrokenWord(before: Int, after: Int): Boolean =
    com.binge.designsystem.component
        .isBrokenWord(before, after)

/** [isBrokenWord] for two `Char`s. */
fun isBrokenWord(before: Char, after: Char): Boolean = isBrokenWord(before.code, after.code)

private const val WORD_CONTEXT = 12

/**
 * Whether the node's own, unclipped position lies in the [viewport]. The clipped bounds cannot say, because a text
 * squeezed to nothing has empty bounds wherever it sits.
 */
private fun SemanticsNode.isInside(viewport: Rect): Boolean {
    val at = positionInRoot
    return at.x < viewport.right && at.y < viewport.bottom && at.x + size.width >= viewport.left && at.y + size.height >= viewport.top
}

/** Whether the node's own, unclipped box lies wholly inside [container], or there is no container. */
private fun SemanticsNode.isFullyInside(container: Rect?, tolerancePx: Int): Boolean {
    if (container == null) return true
    val at = positionInRoot
    return at.x >= container.left - tolerancePx &&
        at.y >= container.top - tolerancePx &&
        at.x + size.width <= container.right + tolerancePx &&
        at.y + size.height <= container.bottom + tolerancePx
}

/** The size the node's text needs: the height of the lines the layout drew, and the width of its widest line. */
private fun SemanticsNode.neededTextSize(): IntSize? {
    val results = mutableListOf<TextLayoutResult>()
    config.getOrNull(SemanticsActions.GetTextLayoutResult)?.action?.invoke(results)
    val paragraph = results.firstOrNull()?.multiParagraph ?: return null
    val widest = (0 until paragraph.lineCount).maxOfOrNull { paragraph.getLineRight(it) - paragraph.getLineLeft(it) } ?: 0f
    return IntSize(kotlin.math.ceil(widest).toInt(), kotlin.math.ceil(paragraph.height).toInt())
}

private const val MAX_TEXT_IN_MESSAGE = 40
