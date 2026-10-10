package com.binge.designsystem.component

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import com.binge.designsystem.testing.TestTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** A drag has no screen-reader equivalent, so the row's Move up and Move down actions are the only way in. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class)
class ReorderableHandleRowTest {
    @get:Rule
    val rule = createComposeRule()

    private val moves = mutableListOf<String>()

    private fun show(onMoveUp: (() -> Unit)?, onMoveDown: (() -> Unit)?) =
        rule.setContent {
            TestTheme {
                ReorderableHandleRow(
                    handleModifier = Modifier,
                    onMoveUp = onMoveUp,
                    onMoveDown = onMoveDown,
                    modifier = Modifier.testTag(ROW),
                ) {
                    Box(Modifier)
                }
            }
        }

    private fun actionLabels(): List<String> =
        rule
            .onNodeWithTag(ROW)
            .fetchSemanticsNode()
            .config
            .getOrNull(SemanticsActions.CustomActions)
            .orEmpty()
            .map { it.label }

    @Test
    fun `a middle row offers both moves`() {
        show(onMoveUp = { moves += "up" }, onMoveDown = { moves += "down" })

        assertEquals(listOf("Move up", "Move down"), actionLabels())
    }

    @Test
    fun `the first row offers no Move up, and the last no Move down`() {
        show(onMoveUp = null, onMoveDown = { moves += "down" })
        assertEquals(listOf("Move down"), actionLabels())
    }

    @Test
    fun `the last row offers no Move down`() {
        show(onMoveUp = { moves += "up" }, onMoveDown = null)

        assertEquals(listOf("Move up"), actionLabels())
    }

    @Test
    fun `each action runs its own callback and reports it handled`() {
        show(onMoveUp = { moves += "up" }, onMoveDown = { moves += "down" })

        val actions = rule.onNodeWithTag(ROW).fetchSemanticsNode().config[SemanticsActions.CustomActions]
        rule.runOnUiThread { assertTrue(actions.first { it.label == "Move down" }.action()) }
        rule.runOnUiThread { assertTrue(actions.first { it.label == "Move up" }.action()) }

        assertEquals(listOf("down", "up"), moves)
    }

    @Test
    fun `the handle does not announce itself as a second control`() {
        show(onMoveUp = { moves += "up" }, onMoveDown = { moves += "down" })

        rule.onNodeWithContentDescription("Drag to reorder").assertDoesNotExist()
    }

    @Test
    fun `the Move actions sit on the label's own node`() {
        rule.setContent {
            TestTheme {
                ReorderableHandleRow(handleModifier = Modifier, onMoveUp = {}, onMoveDown = {}) { Text("Drama") }
            }
        }

        val labels =
            rule
                .onNodeWithText("Drama")
                .fetchSemanticsNode()
                .config
                .getOrNull(SemanticsActions.CustomActions)
                .orEmpty()
                .map { it.label }
        assertEquals(2, labels.size)
    }

    private companion object {
        const val ROW = "row"
    }
}
