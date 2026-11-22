package com.binge.designsystem.testing

import android.app.Dialog
import androidx.activity.OnBackPressedDispatcherOwner
import androidx.compose.ui.test.junit4.ComposeTestRule
import org.robolectric.shadows.ShadowDialog

/** Moves the test clock on [count] frames, one at a time, so a test can stop partway through an animation. */
fun ComposeTestRule.advanceFrames(count: Int) = repeat(count) { mainClock.advanceTimeByFrame() }

/** Presses Back on [owner], an activity or the dispatcher owner a composable reads, as the system would, and waits for the result to settle. */
fun ComposeTestRule.pressBack(owner: OnBackPressedDispatcherOwner) {
    runOnUiThread { owner.onBackPressedDispatcher.onBackPressed() }
    waitForIdle()
}

/** Presses Back on the window of the last dialog shown, such as a sheet or a message dialog, and waits for the result to settle. */
fun ComposeTestRule.pressBackInDialog() {
    val dialog: Dialog = checkNotNull(ShadowDialog.getLatestDialog()) { "No dialog window was shown" }
    runOnUiThread {
        @Suppress("DEPRECATION")
        dialog.onBackPressed()
    }
    waitForIdle()
}
