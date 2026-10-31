package com.binge.designsystem.tv.component

import android.app.Application
import android.view.KeyEvent
import androidx.activity.ComponentActivity
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.pressKey
import androidx.compose.ui.test.requestFocus
import com.binge.designsystem.testing.createTakeDownKeyboardAndroidComposeRule
import com.binge.designsystem.tv.theme.BingeTvTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * The remote's Back, as the platform delivers it: a KEYCODE_BACK key event through the window, the focused node first and
 * the activity's back handling after. One press on a pushed page must pop it and leave focus on the row that opened it.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class, qualifiers = "w960dp-h540dp-television-xhdpi")
class TvSideSheetStackBackTest {
    @get:Rule
    val composeTestRule = createTakeDownKeyboardAndroidComposeRule<ComponentActivity>()

    private var dismissed = 0

    private fun setSheet() {
        composeTestRule.setContent {
            BingeTvTheme {
                TvSideSheetStack(root = "root", onDismissRequest = { dismissed++ }) { nav ->
                    TvSideSheetTitle(nav.page)
                    (1..ROWS).forEach { index ->
                        val key = "${nav.page}-$index"
                        TvSideSheetRow(
                            label = "${nav.page} $index",
                            onClick = { if (nav.page == "root") nav.push("child", key) },
                            modifier = nav.row(key),
                        )
                    }
                    if (nav.page == "child") nav.arriveAt("child-$ARRIVAL")
                }
            }
        }
        composeTestRule.waitForIdle()
    }

    /**
     * Back as a TV delivers it below API 33: down and up through the window, the focused node first and the activity's own
     * handling after. Returns whether the activity handled the key-up, which it does only when nothing in the views took the
     * key-down first (it starts tracking the press in `onKeyDown`).
     */
    private fun pressBack(): Boolean {
        var handledByActivity = false
        composeTestRule.runOnIdle {
            val decor = composeTestRule.activity.window.decorView
            decor.dispatchKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_BACK))
            handledByActivity = decor.dispatchKeyEvent(KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_BACK))
        }
        composeTestRule.waitForIdle()
        return handledByActivity
    }

    private fun openChild() {
        composeTestRule.onNodeWithText("root $OPENER").requestFocus()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("root $OPENER").performKeyInput { pressKey(Key.DirectionCenter) }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("child $ARRIVAL").assertIsFocused()
    }

    @Test
    fun `one Back pops a pushed page and focus lands on the row that opened it`() {
        setSheet()
        openChild()

        val handledByActivity = pressBack()

        assertTrue("the views took Back's key-down, so the activity never handled the press", handledByActivity)
        composeTestRule.onNodeWithText("root $OPENER").assertIsFocused()
        assertEquals("the sheet must not close", 0, dismissed)
    }

    @Test
    fun `one Back on the root closes the sheet`() {
        setSheet()

        assertTrue(pressBack())

        assertEquals(1, dismissed)
    }

    private companion object {
        const val ROWS = 5
        const val OPENER = 4
        const val ARRIVAL = 3
    }
}
