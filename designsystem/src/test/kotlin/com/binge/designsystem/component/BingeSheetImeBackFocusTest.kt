package com.binge.designsystem.component

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.isFocused
import androidx.compose.ui.unit.dp
import androidx.core.graphics.Insets
import androidx.core.view.WindowInsetsCompat
import com.binge.designsystem.testing.TestTheme
import com.binge.designsystem.testing.WithWindowInsets
import com.binge.designsystem.testing.createTakeDownKeyboardComposeRule
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowDialog

/**
 * Back with the keyboard up must not hand focus to anything else (#1024). Pressing Back is a key event, so the window is in
 * keyboard input mode, where clearing focus moves it to the first focusable node: the sheet's drag handle, which then drew
 * a focus box that stayed until the sheet closed. Run in keyboard input mode, as that press is.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class)
class BingeSheetImeBackFocusTest {
    @get:Rule
    val rule = createTakeDownKeyboardComposeRule()

    @Composable
    private fun Draft() {
        val requester = remember { FocusRequester() }
        WithWindowInsets({
            WindowInsetsCompat
                .Builder()
                .setInsets(WindowInsetsCompat.Type.ime(), Insets.of(0, 0, 0, 300))
                .setVisible(WindowInsetsCompat.Type.ime(), true)
                .build()
        }) {
            BasicTextField(value = "draft", onValueChange = {}, modifier = Modifier.size(48.dp).focusRequester(requester))
            LaunchedEffect(Unit) { requester.requestFocus() }
        }
    }

    @Test
    fun `Back with the keyboard up leaves nothing but the field focused`() {
        rule.setContent { TestTheme { BingeBottomSheet(onDismissRequest = {}) { Draft() } } }
        rule.waitForIdle()

        val dialog = checkNotNull(ShadowDialog.getLatestDialog()) { "No sheet window was shown" }
        rule.runOnUiThread {
            @Suppress("DEPRECATION")
            dialog.onBackPressed()
        }
        rule.waitForIdle()

        rule.onAllNodes(isFocused().and(!hasSetTextAction())).assertCountEquals(0)
    }
}
