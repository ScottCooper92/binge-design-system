package com.binge.designsystem.component

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.binge.designsystem.testing.TestTheme
import com.binge.designsystem.testing.createTakeDownComposeRule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** A [BingeOverflowMenu] sorts destructive entries last, closes on a tap, and takes no tap on a disabled entry. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class)
class BingeOverflowMenuTest {
    @get:Rule
    val rule = createTakeDownComposeRule()

    @Test
    fun `a destructive entry is drawn after the others whatever its place`() {
        rule.setContent {
            TestTheme { BingeOverflowMenu(true, {}, listOf(item("Delete", destructive = true), item("Edit"))) }
        }

        val delete = rule.onNodeWithText("Delete").getUnclippedBoundsInRoot()
        val edit = rule.onNodeWithText("Edit").getUnclippedBoundsInRoot()
        assertTrue(delete.top > edit.top)
    }

    @Test
    fun `a tap runs the action and asks to close`() {
        var clicks = 0
        var dismissed = 0
        rule.setContent {
            TestTheme { BingeOverflowMenu(true, { dismissed++ }, listOf(item("Edit", onClick = { clicks++ }))) }
        }

        rule.onNodeWithText("Edit").performClick()

        assertEquals(1, clicks)
        assertEquals(1, dismissed)
    }

    @Test
    fun `a disabled entry is not enabled and does not click`() {
        var clicks = 0
        rule.setContent {
            TestTheme { BingeOverflowMenu(true, {}, listOf(item("Edit", enabled = false, onClick = { clicks++ }))) }
        }

        rule.onNodeWithText("Edit").assertIsNotEnabled().performClick()

        assertEquals(0, clicks)
    }

    private fun item(
        label: String,
        destructive: Boolean = false,
        enabled: Boolean = true,
        onClick: () -> Unit = {},
    ) = OverflowItem(label, if (destructive) Icons.Filled.Delete else Icons.Filled.Edit, onClick, destructive, enabled)
}
