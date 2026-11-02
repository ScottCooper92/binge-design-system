package com.binge.designsystem.component

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.ime
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController

/**
 * Back hides the keyboard, and does not go on to dismiss, while the soft keyboard is up. Composed inside a sheet's window,
 * so it sits ahead of the sheet's own Back handling: a draft the user is typing survives the first Back, and the second,
 * with the keyboard gone, dismisses as usual. Focus is cleared with the keyboard, so the field does not take it straight back.
 * A locked sheet passes [enabled] false: it ignores Back, and stays that way.
 */
@Composable
internal fun HideImeOnBack(enabled: Boolean = true) {
    val keyboardUp = WindowInsets.ime.getBottom(LocalDensity.current) > 0
    val focus = LocalFocusManager.current
    val keyboard = LocalSoftwareKeyboardController.current
    BackHandler(enabled = enabled && keyboardUp) {
        focus.clearFocus()
        keyboard?.hide()
    }
}
