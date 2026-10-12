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
 * with the node's visible bounds, which are the node's own size after every ancestor's clip. The height is the larger
 * of the two, so a text squeezed to nothing by a parent that ran out of room is caught as well as one clipped away. A
 * shortfall of more than [tolerancePx] in width or height fails the test, naming the text and the shortfall.
 *
 * It does not use `TextLayoutResult.hasVisualOverflow`. That is true for every `maxLines` with an ellipsis, which
 * is deliberate and not a bug. The layout of an ellipsized text holds only the lines it shows, with the ellipsis
 * inside the width, so the text fits its box and passes here.
 *
 * Skipped, so that what is not on screen never fails:
 * - text inside a scroll container, where a clipped item is simply scrolled out of view;
 * - text with no bounds, or whose bounds lie wholly outside the root.
 *
 * The unmerged semantics tree is searched, so a merged parent does not hide the text inside it.
 */
fun ComposeTestRule.assertNoTextClipped(root: SemanticsNodeInteraction = onRoot(useUnmergedTree = true), tolerancePx: Int = 1) {
    val rootNode = root.fetchSemanticsNode()
    val viewport = rootNode.boundsInRoot
    val clipped = mutableListOf<ClippedText>()
    collectClipped(rootNode, viewport, tolerancePx, clipped)
    if (clipped.isNotEmpty()) {
        throw AssertionError(
            "Text is clipped by a parent (own size vs. visible size, in px):\n" +
                clipped.joinToString("\n") { it.describe() },
        )
    }
}

private data class ClippedText(
    val text: String,
    val shortfallWidth: Int,
    val shortfallHeight: Int,
    val ownWidth: Int,
    val ownHeight: Int,
    val visibleWidth: Int,
    val visibleHeight: Int,
) {
    fun describe(): String =
        "  \"$text\": ${ownWidth}x$ownHeight, visible ${visibleWidth}x$visibleHeight" +
            " (short ${shortfallWidth}px wide, ${shortfallHeight}px high)"
}

private fun collectClipped(
    node: SemanticsNode,
    viewport: Rect,
    tolerancePx: Int,
    out: MutableList<ClippedText>,
) {
    val config = node.config
    if (config.contains(SemanticsProperties.VerticalScrollAxisRange) ||
        config.contains(SemanticsProperties.HorizontalScrollAxisRange)
    ) {
        return
    }
    val text = config.getOrNull(SemanticsProperties.Text)?.joinToString(" ") { it.text }
    if (text != null) {
        val bounds = node.boundsInRoot
        val needed = node.neededTextSize()
        if (needed != null && node.isInside(viewport)) {
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
    node.children.forEach { collectClipped(it, viewport, tolerancePx, out) }
}

/**
 * Whether the node's own, unclipped position lies in the [viewport]. The clipped bounds cannot say, because a text
 * squeezed to nothing has empty bounds wherever it sits.
 */
private fun SemanticsNode.isInside(viewport: Rect): Boolean {
    val at = positionInRoot
    return at.x < viewport.right && at.y < viewport.bottom && at.x + size.width >= viewport.left && at.y + size.height >= viewport.top
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
