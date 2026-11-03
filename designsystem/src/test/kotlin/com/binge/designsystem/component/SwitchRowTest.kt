package com.binge.designsystem.component

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.assertHeightIsAtLeast
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import com.binge.designsystem.testing.createTakeDownComposeRule
import com.binge.designsystem.theme.BingeExpressiveTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** The whole row is the target: switch role, at least 48dp tall, one tap toggles it, and a disabled row ignores taps. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class)
class SwitchRowTest {
    @get:Rule
    val rule = createTakeDownComposeRule()

    private var checked by mutableStateOf(false)

    private fun show(enabled: Boolean = true) {
        rule.setContent {
            BingeExpressiveTheme(dynamicColor = false) {
                SwitchRow(label = "Notify me", checked = checked, onCheckedChange = { checked = it }, enabled = enabled)
            }
        }
    }

    @Test
    fun `the row is a switch and at least the minimum touch target tall`() {
        show()

        val node = rule.onNodeWithText("Notify me")
        node.assertHeightIsAtLeast(48.dp)
        assertEquals(Role.Switch, node.fetchSemanticsNode().config.getOrNull(SemanticsProperties.Role))
    }

    @Test
    fun `a tap on the label toggles it`() {
        show()

        rule.onNodeWithText("Notify me").performClick()

        assertEquals(true, checked)
    }

    @Test
    fun `a disabled row ignores a tap`() {
        show(enabled = false)

        rule.onNodeWithText("Notify me").performClick()

        assertEquals(false, checked)
    }
}
