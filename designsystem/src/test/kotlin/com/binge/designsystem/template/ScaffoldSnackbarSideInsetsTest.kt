package com.binge.designsystem.template

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.dp
import com.binge.designsystem.testing.TestTheme
import com.binge.designsystem.testing.WithStartCutout
import com.binge.designsystem.testing.createTakeDownComposeRule
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private const val MESSAGE = "Saved"
private const val TOLERANCE = 1f

/** A camera cutout down the start side, as a phone held in landscape has. */
private val Cutout = 40.dp

/** The scaffold's snackbar clears a side cutout, as the body above it does. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class, qualifiers = "w800dp-h400dp")
class ScaffoldSnackbarSideInsetsTest {
    @get:Rule
    val rule = createTakeDownComposeRule()

    @Test
    fun `a start cutout moves the snackbar in by its width`() {
        var cutout by mutableStateOf(0.dp)
        rule.setContent {
            TestTheme {
                WithStartCutout(cutout) {
                    val snackbar = remember { SnackbarHostState() }
                    LaunchedEffect(Unit) { snackbar.showSnackbar(MESSAGE) }
                    BingeScreenScaffold(title = "Settings", snackbarHostState = snackbar) {}
                }
            }
        }
        rule.waitForIdle()
        val without = messageLeft()

        cutout = Cutout
        rule.waitForIdle()

        assertEquals(without + Cutout.value, messageLeft(), TOLERANCE)
    }

    private fun messageLeft() =
        rule
            .onNodeWithText(MESSAGE)
            .getBoundsInRoot()
            .left.value
}
