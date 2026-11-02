package com.binge.designsystem.component

import androidx.activity.OnBackPressedDispatcherOwner
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.unit.dp
import androidx.core.graphics.Insets
import androidx.core.view.WindowInsetsCompat
import com.binge.designsystem.testing.TestTheme
import com.binge.designsystem.testing.WithWindowInsets
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowDialog

private const val KEYBOARD_HEIGHT = 300

/**
 * Back with the soft keyboard up hides the keyboard and leaves the sheet open, so a draft survives the first Back; with it
 * hidden, Back dismisses as it always has (#1019). Robolectric fakes the keyboard's insets, not the keyboard itself, so this
 * pins the sheet's own handling; the platform's side of it is a device check.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class)
class BingeSheetImeBackTest {
    @get:Rule
    val rule = createComposeRule()

    private var dismissed = 0
    private var fieldFocused = false
    private var back: OnBackPressedDispatcherOwner? = null

    @Composable
    private fun Draft(keyboardUp: Boolean) {
        val requester = remember { FocusRequester() }
        WithWindowInsets({
            WindowInsetsCompat
                .Builder()
                .setInsets(WindowInsetsCompat.Type.ime(), Insets.of(0, 0, 0, if (keyboardUp) KEYBOARD_HEIGHT else 0))
                .setVisible(WindowInsetsCompat.Type.ime(), keyboardUp)
                .build()
        }) {
            back = LocalOnBackPressedDispatcherOwner.current
            BasicTextField(
                value = "draft",
                onValueChange = {},
                modifier =
                    Modifier
                        .size(48.dp)
                        .focusRequester(requester)
                        .onFocusChanged { fieldFocused = it.isFocused },
            )
            LaunchedEffect(Unit) { requester.requestFocus() }
        }
    }

    private fun showBottomSheet(keyboardUp: Boolean) {
        dismissed = 0
        rule.setContent { TestTheme { BingeBottomSheet(onDismissRequest = { dismissed++ }) { Draft(keyboardUp) } } }
        rule.waitForIdle()
        assertTrue("The draft field never took focus, so the test cannot show it losing it", fieldFocused)
    }

    private fun showSideSheet(keyboardUp: Boolean) {
        dismissed = 0
        rule.setContent { TestTheme { BingeModalSideSheet(onDismissRequest = { dismissed++ }) { Draft(keyboardUp) } } }
        rule.waitForIdle()
        assertTrue("The draft field never took focus, so the test cannot show it losing it", fieldFocused)
    }

    private fun pressBottomSheetBack() {
        val dialog = checkNotNull(ShadowDialog.getLatestDialog()) { "No sheet window was shown" }
        rule.runOnUiThread {
            @Suppress("DEPRECATION")
            dialog.onBackPressed()
        }
        rule.waitForIdle()
    }

    private fun pressSideSheetBack() {
        rule.runOnIdle { checkNotNull(back).onBackPressedDispatcher.onBackPressed() }
        rule.waitForIdle()
    }

    @Test
    fun `Back with the keyboard up on a bottom sheet hides it and keeps the sheet`() {
        showBottomSheet(keyboardUp = true)
        pressBottomSheetBack()
        assertEquals(0, dismissed)
        assertFalse(fieldFocused)
    }

    @Test
    fun `Back with the keyboard hidden on a bottom sheet dismisses it`() {
        showBottomSheet(keyboardUp = false)
        pressBottomSheetBack()
        assertEquals(1, dismissed)
    }

    @Test
    fun `Back with the keyboard up on a side sheet hides it and keeps the sheet`() {
        showSideSheet(keyboardUp = true)
        pressSideSheetBack()
        assertEquals(0, dismissed)
        assertFalse(fieldFocused)
    }

    @Test
    fun `Back with the keyboard hidden on a side sheet dismisses it`() {
        showSideSheet(keyboardUp = false)
        pressSideSheetBack()
        assertEquals(1, dismissed)
    }
}
