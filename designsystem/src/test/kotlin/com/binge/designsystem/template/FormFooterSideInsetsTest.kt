package com.binge.designsystem.template

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.graphics.Insets
import androidx.core.view.WindowInsetsCompat
import com.binge.designsystem.component.BingeActionFooter
import com.binge.designsystem.testing.TestTheme
import com.binge.designsystem.testing.WithWindowInsets
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private const val FIELD = "field"
private const val CANCEL = "Cancel"
private const val SAVE = "Save"
private const val TOLERANCE = 1f

/** A camera cutout down the start side, as a phone held in landscape has. */
private val Cutout = 40.dp

/** A footer in a `bottomBar` slot clears a side cutout, as the body above it does (#374). */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class, qualifiers = "w800dp-h400dp")
class FormFooterSideInsetsTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `a form's footer starts where its fields start`() {
        rule.setContent {
            TestTheme {
                WithStartCutout(Cutout) {
                    FormScreen(
                        title = "Edit",
                        primaryAction = FormAction(SAVE, onClick = {}),
                        secondaryAction = FormAction(CANCEL, onClick = {}),
                        placement = FormActionPlacement.Footer,
                    ) {
                        Box(Modifier.fillMaxWidth().height(48.dp).testTag(FIELD))
                    }
                }
            }
        }
        rule.waitForIdle()

        assertEquals(
            rule
                .onNodeWithTag(FIELD)
                .getBoundsInRoot()
                .left.value,
            rule
                .onNodeWithText(CANCEL)
                .getBoundsInRoot()
                .left.value,
            TOLERANCE,
        )
    }

    @Test
    fun `an action footer that clears the navigation bar clears a side cutout too`() {
        rule.setContent {
            TestTheme {
                WithStartCutout(Cutout) {
                    BingeActionFooter(label = SAVE, onClick = {}, horizontalPadding = 0.dp, clearsNavigationBar = true)
                }
            }
        }
        rule.waitForIdle()

        assertEquals(
            Cutout.value,
            rule
                .onNodeWithText(SAVE)
                .getBoundsInRoot()
                .left.value,
            TOLERANCE,
        )
    }
}

/** Gives the window a display cutout [width] wide down the start side, answering every insets dispatch at the parent. */
@Composable
private fun WithStartCutout(width: Dp, content: @Composable () -> Unit) =
    WithWindowInsets({
        WindowInsetsCompat.Builder().setInsets(WindowInsetsCompat.Type.displayCutout(), Insets.of(width.roundToPx(), 0, 0, 0)).build()
    }, content)
