package com.binge.designsystem.component

import androidx.activity.OnBackPressedDispatcherOwner
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.material3.Text
import androidx.compose.ui.test.junit4.v2.createComposeRule
import com.binge.designsystem.theme.BingeExpressiveTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private const val PANEL_TEXT = "Filters"

/**
 * Back dismisses the side sheet, through the dialog's own back dispatcher (#394). The scrim tap is pinned in
 * ClickableSemanticsTest.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class, qualifiers = "w840dp-h900dp")
class BingeModalSideSheetDismissTest {
    @get:Rule
    val rule = createComposeRule()

    private var dismissals = 0

    /** The dialog's own back owner, read from inside the sheet, so Back takes the path a real press does. */
    private var back: OnBackPressedDispatcherOwner? = null

    @Test
    fun `Back dismisses the sheet`() {
        show()

        rule.runOnIdle { checkNotNull(back).onBackPressedDispatcher.onBackPressed() }
        rule.waitForIdle()

        assertEquals(1, dismissals)
    }

    private fun show() {
        rule.setContent {
            BingeExpressiveTheme(dynamicColor = false) {
                BingeModalSideSheet(onDismissRequest = { dismissals++ }) {
                    back = LocalOnBackPressedDispatcherOwner.current
                    Text(PANEL_TEXT)
                }
            }
        }
        rule.waitForIdle()
    }
}
