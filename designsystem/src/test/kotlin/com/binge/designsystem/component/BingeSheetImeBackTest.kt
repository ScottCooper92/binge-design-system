package com.binge.designsystem.component

import androidx.activity.OnBackPressedDispatcherOwner
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.unit.dp
import androidx.core.graphics.Insets
import androidx.core.view.WindowInsetsCompat
import com.binge.designsystem.testing.TestTheme
import com.binge.designsystem.testing.WithWindowInsets
import com.binge.designsystem.testing.createTakeDownComposeRule
import org.junit.Assert.assertEquals
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
 * hidden, Back dismisses as it always has (#1019). Back hides the keyboard and does not clear focus: clearing it handed focus
 * to the sheet's drag handle, which drew a focus box around it (#1024). Robolectric fakes the keyboard's insets, not the keyboard,
 * so a test drops them by hand where the real keyboard would; whether the keyboard really hides is a device check.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class)
class BingeSheetImeBackTest {
    @get:Rule
    val rule = createTakeDownComposeRule()

    private var dismissed = 0
    private var fieldFocused = false
    private var keyboardUp by mutableStateOf(false)
    private var back: OnBackPressedDispatcherOwner? = null

    @Composable
    private fun Draft() {
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

    private fun show(up: Boolean, sheet: @Composable (@Composable () -> Unit) -> Unit) {
        dismissed = 0
        keyboardUp = up
        rule.setContent { TestTheme { sheet { Draft() } } }
        rule.waitForIdle()
        assertTrue("The draft field never took focus, so the test cannot show it keeping it", fieldFocused)
    }

    private fun showBottomSheet(up: Boolean) = show(up) { content -> BingeBottomSheet(onDismissRequest = { dismissed++ }) { content() } }

    private fun showSideSheet(up: Boolean) = show(up) { content -> BingeModalSideSheet(onDismissRequest = { dismissed++ }) { content() } }

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
    fun `Back with the keyboard up on a bottom sheet hides it, keeps the sheet and keeps focus`() {
        showBottomSheet(up = true)
        pressBottomSheetBack()
        assertEquals(0, dismissed)
        assertTrue("Back moved focus off the field", fieldFocused)
    }

    @Test
    fun `a second Back on a bottom sheet, once the keyboard is gone, dismisses it though the field still has focus`() {
        showBottomSheet(up = true)
        pressBottomSheetBack()
        keyboardUp = false
        rule.waitForIdle()
        pressBottomSheetBack()
        assertEquals(1, dismissed)
    }

    @Test
    fun `Back with the keyboard hidden on a bottom sheet dismisses it`() {
        showBottomSheet(up = false)
        pressBottomSheetBack()
        assertEquals(1, dismissed)
    }

    @Test
    fun `Back with the keyboard up on a side sheet hides it, keeps the sheet and keeps focus`() {
        showSideSheet(up = true)
        pressSideSheetBack()
        assertEquals(0, dismissed)
        assertTrue("Back moved focus off the field", fieldFocused)
    }

    @Test
    fun `a second Back on a side sheet, once the keyboard is gone, dismisses it though the field still has focus`() {
        showSideSheet(up = true)
        pressSideSheetBack()
        keyboardUp = false
        rule.waitForIdle()
        pressSideSheetBack()
        assertEquals(1, dismissed)
    }

    @Test
    fun `Back with the keyboard hidden on a side sheet dismisses it`() {
        showSideSheet(up = false)
        pressSideSheetBack()
        assertEquals(1, dismissed)
    }
}
