package com.binge.designsystem.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.click
import androidx.compose.ui.test.isRoot
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onLast
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.unit.dp
import com.binge.designsystem.theme.BingeExpressiveTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowDialog

/**
 * A locked sheet holds a draft the user is mid-way through. What dismisses it, and what does not, is a
 * promise the caller builds on: no frame can show it, and a modifier reorder or a Material upgrade that
 * changed what the window properties mean would drop the draft with nothing failing.
 *
 * The baseline cases first, so a case that expects no dismissal cannot pass only because the harness
 * never managed to dismiss anything.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class)
class BingeBottomSheetDismissTest {
    @get:Rule
    val rule = createComposeRule()

    private var dismissed = 0

    private fun showSheet(gesturesEnabled: Boolean, dismissOnClickOutside: Boolean = gesturesEnabled) {
        dismissed = 0
        rule.setContent {
            BingeExpressiveTheme(dynamicColor = false) {
                BingeBottomSheet(
                    onDismissRequest = { dismissed++ },
                    gesturesEnabled = gesturesEnabled,
                    dismissOnClickOutside = dismissOnClickOutside,
                ) {
                    Box(Modifier.size(SHEET_CONTENT_SIZE))
                }
            }
        }
        rule.waitForIdle()
    }

    private fun pressBack() {
        val dialog = checkNotNull(ShadowDialog.getLatestDialog()) { "No sheet window was shown" }
        rule.runOnUiThread {
            @Suppress("DEPRECATION")
            dialog.onBackPressed()
        }
        rule.waitForIdle()
    }

    /**
     * A tap near the top of the sheet's own window, well clear of the sheet. By position rather than by the
     * scrim's "Close sheet" description, because Material drops that node when the outside tap is blocked, and
     * a test that cannot find the scrim would pass for the wrong reason.
     */
    private fun tapScrim() {
        rule.onAllNodes(isRoot()).onLast().performTouchInput { click(Offset(width / 2f, SCRIM_TAP_Y)) }
        rule.waitForIdle()
    }

    @Test
    fun `a free sheet is dismissed by Back`() {
        showSheet(gesturesEnabled = true)

        pressBack()

        assertEquals(1, dismissed)
    }

    @Test
    fun `a free sheet is dismissed by a tap on the scrim`() {
        showSheet(gesturesEnabled = true)

        tapScrim()

        assertEquals(1, dismissed)
    }

    @Test
    fun `a locked sheet ignores Back`() {
        showSheet(gesturesEnabled = false)

        pressBack()

        assertEquals(0, dismissed)
    }

    @Test
    fun `a locked sheet ignores a tap on the scrim`() {
        showSheet(gesturesEnabled = false)

        tapScrim()

        assertEquals(0, dismissed)
    }

    @Test
    fun `a sheet that blocks only the outside tap ignores the scrim and still answers Back`() {
        showSheet(gesturesEnabled = true, dismissOnClickOutside = false)

        tapScrim()
        assertEquals(0, dismissed)

        pressBack()
        assertEquals(1, dismissed)
    }

    private companion object {
        val SHEET_CONTENT_SIZE = 120.dp
        const val SCRIM_TAP_Y = 20f
    }
}
