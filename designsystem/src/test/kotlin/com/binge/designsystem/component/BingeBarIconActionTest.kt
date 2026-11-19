package com.binge.designsystem.component

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import com.binge.designsystem.testing.TestTheme
import com.binge.designsystem.testing.createTakeDownComposeRule
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** A [BingeBarIconAction] is named by its content description, clicks when enabled, and does nothing when disabled. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class)
class BingeBarIconActionTest {
    @get:Rule
    val rule = createTakeDownComposeRule()

    @Test
    fun `an enabled action is clickable by its description`() {
        var clicks = 0
        rule.setContent {
            TestTheme { BingeBarIconAction(Icons.Filled.Search, "Search", onClick = { clicks++ }) }
        }

        rule.onNodeWithContentDescription("Search").assertIsEnabled().performClick()

        assertEquals(1, clicks)
    }

    @Test
    fun `a disabled action does not click`() {
        var clicks = 0
        rule.setContent {
            TestTheme { BingeBarIconAction(Icons.Filled.Search, "Search", onClick = { clicks++ }, enabled = false) }
        }

        rule.onNodeWithContentDescription("Search").assertIsNotEnabled().performClick()

        assertEquals(0, clicks)
    }
}
