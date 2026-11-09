package com.binge.designsystem.component

import androidx.compose.ui.test.onNodeWithText
import com.binge.designsystem.testing.TestTheme
import com.binge.designsystem.testing.createTakeDownComposeRule
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** The step handle docks into a bar only inside a docking sheet; anywhere else it draws nothing, so it is safe to place unconditionally. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class, qualifiers = "w320dp-h640dp")
class BingeSheetStepHandleTest {
    @get:Rule
    val rule = createTakeDownComposeRule()

    @Test
    fun `outside a docking sheet it draws nothing`() {
        rule.setContent { TestTheme { BingeSheetStepHandle(title = "Folder name") } }

        rule.onNodeWithText("Folder name").assertDoesNotExist()
    }
}
