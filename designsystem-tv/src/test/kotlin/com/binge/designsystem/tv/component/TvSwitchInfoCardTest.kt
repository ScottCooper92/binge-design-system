package com.binge.designsystem.tv.component

import android.app.Application
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertHasNoClickAction
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.pressKey
import androidx.compose.ui.test.requestFocus
import com.binge.designsystem.testing.createKeyboardComposeRule
import com.binge.designsystem.tv.theme.BingeTvTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private const val ROW = "row"
private const val CARD = "card"
private const val BELOW = "below"

/**
 * A row ending in a [TvSwitch] announces the switch's state, and OK on the row flips it. A [TvInfoCard] takes focus on the
 * D-pad, has nothing to press, and reads its title and lines as one item.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class, qualifiers = "w960dp-h540dp-television-xhdpi")
class TvSwitchInfoCardTest {
    @get:Rule
    val rule = createKeyboardComposeRule()

    @Test
    fun `a row with a switch says whether it is on, and OK flips it`() {
        var on by mutableStateOf(false)
        rule.setContent {
            BingeTvTheme {
                TvSideSheetRow(label = "Notify me", onClick = { on = !on }, modifier = Modifier.testTag(ROW)) { isFocused ->
                    TvSwitch(checked = on, isFocused = isFocused)
                }
            }
        }
        rule.onNodeWithTag(ROW).assert(toggleable(ToggleableState.Off))

        rule.onNodeWithTag(ROW).requestFocus()
        rule.onRoot().performKeyInput { pressKey(Key.Enter) }
        rule.waitForIdle()

        rule.onNodeWithTag(ROW).assert(toggleable(ToggleableState.On))
    }

    @Test
    fun `an info card takes focus, reads as one item and has nothing to press`() {
        rule.setContent {
            BingeTvTheme {
                Column {
                    TvSideSheetRow(label = "Above", onClick = {}, modifier = Modifier.testTag(ROW))
                    TvInfoCard(title = "Media server", lines = listOf("Connected"), modifier = Modifier.testTag(CARD))
                    TvSideSheetRow(label = "Below", onClick = {}, modifier = Modifier.testTag(BELOW))
                }
            }
        }
        rule.onNodeWithTag(ROW).requestFocus()
        rule.onRoot().performKeyInput { pressKey(Key.DirectionDown) }
        rule.waitForIdle()

        rule
            .onNodeWithTag(CARD)
            .assertIsFocused()
            .assert(hasText("Media server") and hasText("Connected"))
            .assertHasNoClickAction()
    }

    private fun toggleable(state: ToggleableState) = SemanticsMatcher.expectValue(SemanticsProperties.ToggleableState, state)
}
