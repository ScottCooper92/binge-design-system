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
 * It does not use `TextLayoutResult.hasVisualOverflow`. That is true for every `maxLines` with an ellipsis, which
 * is deliberate and not a bug. The layout of an ellipsized text holds only the lines it shows, with the ellipsis
 * inside the width, so the text fits its box and passes here.
 *
 * Skipped, so that what is not on screen never fails:
 * - text inside a scroll container that is not wholly in view: a clipped item there is simply scrolled out, and only
 *   its edge is cut by the container. A text that is wholly inside the container's viewport is still checked, because
 *   anything cutting it is then something other than scrolling;
 * - text whose own position lies outside the root.
 *
 * The unmerged semantics tree is searched, so a merged parent does not hide the text inside it.
 *
 * Returns how many text nodes it checked, so a test can tell a pass from a pass that skipped everything because the
 * content was in a scroll container or off screen.
 */
fun ComposeTestRule.assertNoTextClipped(root: SemanticsNodeInteraction = onRoot(useUnmergedTree = true), tolerancePx: Int = 1): Int {
    val rootNode = root.fetchSemanticsNode()
    val viewport = rootNode.boundsInRoot
    val clipped = mutableListOf<ClippedText>()
    val checked = intArrayOf(0)
    collectClipped(rootNode, viewport, null, tolerancePx, clipped, checked)
    if (clipped.isNotEmpty()) {
        throw AssertionError(
            "Text is clipped by a parent (own size vs. visible size, in px):\n" +
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
) {
    fun describe(): String =
        "  \"$text\": ${ownWidth}x$ownHeight, visible ${visibleWidth}x$visibleHeight" +
            " (short ${shortfallWidth}px wide, ${shortfallHeight}px high)"
}

private fun collectClipped(
    node: SemanticsNode,
    viewport: Rect,
    scrollViewport: Rect?,
    tolerancePx: Int,
    out: MutableList<ClippedText>,
    checked: IntArray,
) {
    val config = node.config
    val scrolls =
        config.contains(SemanticsProperties.VerticalScrollAxisRange) ||
            config.contains(SemanticsProperties.HorizontalScrollAxisRange)
    val inScroll = if (scrolls) node.boundsInRoot else scrollViewport
    val text = config.getOrNull(SemanticsProperties.Text)?.joinToString(" ") { it.text }
    if (text != null) {
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
    node.children.forEach { collectClipped(it, viewport, inScroll, tolerancePx, out, checked) }
}

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
