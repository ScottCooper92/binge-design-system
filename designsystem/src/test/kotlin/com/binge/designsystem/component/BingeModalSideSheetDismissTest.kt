package com.binge.designsystem.component

import androidx.activity.OnBackPressedDispatcherOwner
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.material3.Text
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.click
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import com.binge.designsystem.R
import com.binge.designsystem.theme.BingeExpressiveTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

private const val PANEL_TEXT = "Filters"

/** Near the start edge, well clear of the panel, which sits at the end edge. */
private const val SCRIM_TOUCH_FRACTION = 0.1f

/**
 * Back and a touch on the scrim dismiss the side sheet, and a tap on the panel does not (#394). The window is wide
 * enough that the scrim shows beside the panel, so the scrim can be touched rather than reached by its semantics.
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

    @Test
    fun `a touch on the scrim beside the panel dismisses the sheet`() {
        show()

        rule
            .onNode(
                hasContentDescription(RuntimeEnvironment.getApplication().getString(R.string.cd_close_sheet)) and hasClickAction(),
            ).performTouchInput {
                click(Offset(width * SCRIM_TOUCH_FRACTION, centerY))
            }
        rule.waitForIdle()

        assertEquals(1, dismissals)
    }

    @Test
    fun `a tap on the panel does not fall through to the scrim`() {
        show()

        rule.onNodeWithText(PANEL_TEXT).performClick()
        rule.waitForIdle()

        assertEquals(0, dismissals)
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
