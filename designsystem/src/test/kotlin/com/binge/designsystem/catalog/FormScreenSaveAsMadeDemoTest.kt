package com.binge.designsystem.catalog

import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.binge.designsystem.testing.createTakeDownComposeRule
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** The save-as-made demo's switches are the shared [com.binge.designsystem.component.SwitchRow]: the whole row toggles, as one switch node. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class, qualifiers = "w400dp-h800dp")
class FormScreenSaveAsMadeDemoTest {
    @get:Rule
    val rule = createTakeDownComposeRule()

    @Test
    fun `a tap on a switch's label flips it, and the label and switch are one switch node`() {
        rule.setContent { FormScreenSaveAsMadeDemo() }
        rule.waitForIdle()

        val row = rule.onNodeWithText("Sync in the background")
        val before = row.fetchSemanticsNode().config
        assertEquals(Role.Switch, before.getOrNull(SemanticsProperties.Role))
        assertEquals(ToggleableState.Off, before.getOrNull(SemanticsProperties.ToggleableState))

        row.performClick()
        rule.waitForIdle()

        assertEquals(
            ToggleableState.On,
            rule
                .onNodeWithText("Sync in the background")
                .fetchSemanticsNode()
                .config
                .getOrNull(SemanticsProperties.ToggleableState),
        )
    }
}
