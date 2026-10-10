package com.binge.designsystem.component

import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class)
class MonthYearPickerDialogModifierTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `a modifier passed to the dialog reaches its content`() {
        rule.setContent {
            MonthYearPickerDialog(
                title = "From",
                mode = MonthYearPickerMode.MonthAndYear,
                yearRange = 1990..2030,
                onDismiss = {},
                onConfirm = {},
                modifier = Modifier.testTag(DIALOG_TAG),
                defaultYear = 2030,
            )
        }

        rule.onNodeWithTag(DIALOG_TAG).assertIsDisplayed()
    }

    private companion object {
        const val DIALOG_TAG = "picker-dialog"
    }
}
