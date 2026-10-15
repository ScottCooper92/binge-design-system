package com.binge.designsystem.tv.component

import android.app.Application
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Text
import com.binge.designsystem.tv.theme.BingeTvTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private const val SAVING = "saving"
private const val CHOSEN = "chosen"
private const val SAVING_VALUE = "saving-value"
private const val CHOSEN_VALUE = "chosen-value"
private const val TOLERANCE = 0.5f

/** A saving sheet row says it is in progress, and its spinner takes the tick's place without moving what sits before it. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class, qualifiers = "w960dp-h540dp-television-xhdpi")
class TvSideSheetRowSavingTest {
    @get:Rule
    val rule = createComposeRule()

    private fun show() {
        rule.setContent {
            BingeTvTheme {
                Column(Modifier.width(348.dp)) {
                    TvSideSheetRow(label = "Request", onClick = {}, saving = true, opensPage = true, modifier = Modifier.testTag(SAVING)) {
                        Text("12", modifier = Modifier.testTag(SAVING_VALUE))
                    }
                    TvSideSheetRow(
                        label = "Request in 4K",
                        onClick = {},
                        selected = true,
                        opensPage = true,
                        modifier = Modifier.testTag(CHOSEN),
                    ) {
                        Text("12", modifier = Modifier.testTag(CHOSEN_VALUE))
                    }
                }
            }
        }
        rule.waitForIdle()
    }

    @Test
    fun `a saving row announces that it is in progress, and a resting one does not`() {
        show()

        rule.onNodeWithTag(SAVING).assert(SemanticsMatcher.expectValue(SemanticsProperties.StateDescription, "In progress"))
        rule.onNodeWithTag(CHOSEN).assert(SemanticsMatcher.keyNotDefined(SemanticsProperties.StateDescription))
    }

    @Test
    fun `the spinner sits in the tick's place, so the value before it stays where the tick leaves it`() {
        show()

        val saving = rule.onNodeWithTag(SAVING_VALUE, useUnmergedTree = true).getUnclippedBoundsInRoot()
        val chosen = rule.onNodeWithTag(CHOSEN_VALUE, useUnmergedTree = true).getUnclippedBoundsInRoot()
        assertEquals(chosen.right.value, saving.right.value, TOLERANCE)
    }
}
