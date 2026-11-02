package com.binge.designsystem.component

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.ime
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalSoftwareKeyboardController

/**
 * Back hides the keyboard, and does not go on to dismiss, while the soft keyboard is up. Composed inside a sheet's window,
 * so it sits ahead of the sheet's own Back handling: a draft the user is typing survives the first Back, and the second,
 * with the keyboard gone, dismisses as usual. Focus stays on the field, as the platform leaves it. Clearing it would hand it to
 * the sheet's drag handle, which draws a focus box around itself when Back arrives as a key (#1024).
 * A locked sheet passes [enabled] false: it ignores Back, and stays that way.
 */
@Composable
internal fun HideImeOnBack(enabled: Boolean = true) {
    val keyboardUp = WindowInsets.ime.getBottom(LocalDensity.current) > 0
    val keyboard = LocalSoftwareKeyboardController.current
    BackHandler(enabled = enabled && keyboardUp) { keyboard?.hide() }
}
