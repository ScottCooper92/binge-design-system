package com.binge.designsystem.component

import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.binge.designsystem.testing.createTakeDownComposeRule
import com.binge.designsystem.theme.BingeExpressiveTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** [ReorderableSwitchRow] gives the tap to the row or to the switch, and keeps the handle row's Move actions. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class)
class ReorderableSwitchRowTest {
    @get:Rule
    val rule = createTakeDownComposeRule()

    private fun show(
        onClick: (() -> Unit)?,
        onToggle: (Boolean) -> Unit,
        onMoveUp: (() -> Unit)? = null,
    ) {
        rule.setContent {
            BingeExpressiveTheme(dynamicColor = false) {
                ReorderableSwitchRow(
                    label = "Upcoming",
                    checked = false,
                    onCheckedChange = onToggle,
                    handleModifier = Modifier,
                    onMoveUp = onMoveUp,
                    onMoveDown = {},
                    onClick = onClick,
                )
            }
        }
    }

    @Test
    fun `without onClick a tap on the row toggles`() {
        val toggled = mutableListOf<Boolean>()
        show(onClick = null, onToggle = { toggled += it })

        rule.onNodeWithText("Upcoming").performClick()

        assertEquals(listOf(true), toggled)
    }

    @Test
    fun `with onClick a tap on the row opens it and does not toggle`() {
        var opened = 0
        val toggled = mutableListOf<Boolean>()
        show(onClick = { opened++ }, onToggle = { toggled += it })

        rule.onNodeWithText("Upcoming").performClick()

        assertEquals(1, opened)
        assertEquals(emptyList<Boolean>(), toggled)
    }

    @Test
    fun `the move actions follow the callbacks`() {
        show(onClick = null, onToggle = {}, onMoveUp = null)

        rule.onNodeWithText("Upcoming").assert(
            SemanticsMatcher("only Move down is offered") {
                val actions = it.config.getOrNull(SemanticsActions.CustomActions)
                assertNotNull(actions)
                actions?.size == 1
            },
        )
    }
}
