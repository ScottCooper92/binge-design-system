package com.binge.designsystem.template

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.dp
import com.binge.designsystem.component.BingeFilledButton
import com.binge.designsystem.testing.TestTheme
import com.binge.designsystem.testing.WithStartCutout
import com.binge.designsystem.testing.createTakeDownComposeRule
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

/** The side inset a page keeps for its content, on top of the cutout the bar clears. */
private const val CONTENT_INSET = 24f

/** A camera cutout down the start side, as a phone held in landscape has. */
private val Cutout = 40.dp

/** A footer in a `bottomBar` slot clears a side cutout, as the body above it does. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class, qualifiers = "w800dp-h400dp")
class FormFooterSideInsetsTest {
    @get:Rule
    val rule = createTakeDownComposeRule()

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
    fun `a screen footer clears a side cutout too`() {
        rule.setContent {
            TestTheme {
                WithStartCutout(Cutout) {
                    ScreenFooter { BingeFilledButton(label = SAVE, onClick = {}) }
                }
            }
        }
        rule.waitForIdle()

        assertEquals(
            Cutout.value + CONTENT_INSET,
            rule
                .onNodeWithText(SAVE)
                .getBoundsInRoot()
                .left.value,
            TOLERANCE,
        )
    }
}
