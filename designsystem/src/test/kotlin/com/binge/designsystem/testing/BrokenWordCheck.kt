package com.binge.designsystem.testing

import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsNode
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.text.TextLayoutResult

/**
 * Fails when a line of text ends in the middle of a word. A line may end at a space or just after a hyphen, but not between two letters
 * or digits: that is a word the layout had no room for, and it should wrap whole or be given room.
 *
 * Private to these tests until the shared broken-word check of the text-clipping fixture lands; then these tests move onto that one.
 */
internal fun ComposeTestRule.assertNoBrokenWords() {
    val found = mutableListOf<String>()
    collectBroken(onRoot(useUnmergedTree = true).fetchSemanticsNode(), found)
    if (found.isNotEmpty()) throw AssertionError("A word is broken across lines:\n" + found.joinToString("\n"))
}

private fun collectBroken(node: SemanticsNode, out: MutableList<String>) {
    val text = node.config.getOrNull(SemanticsProperties.Text)?.joinToString(" ") { it.text }
    if (!text.isNullOrEmpty()) {
        val results = mutableListOf<TextLayoutResult>()
        node.config
            .getOrNull(SemanticsActions.GetTextLayoutResult)
            ?.action
            ?.invoke(results)
        results.firstOrNull()?.let { layout ->
            val shown = layout.layoutInput.text.text
            for (line in 0 until layout.lineCount - 1) {
                val end = layout.getLineEnd(line, visibleEnd = false)
                if (end in 1 until shown.length && shown[end - 1].isLetterOrDigit() && shown[end].isLetterOrDigit()) {
                    out +=
                        "  \"${shown.take(
                            MAX_SHOWN,
                        )}\": line ${line + 1} ends inside a word, between '${shown[end - 1]}' and '${shown[end]}'"
                }
            }
        }
    }
    node.children.forEach { collectBroken(it, out) }
}

private const val MAX_SHOWN = 60
