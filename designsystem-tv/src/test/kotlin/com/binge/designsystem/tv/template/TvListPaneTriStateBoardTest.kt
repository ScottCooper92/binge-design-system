package com.binge.designsystem.tv.template

import android.app.Application
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isFocusable
import androidx.compose.ui.test.isFocused
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.pressKey
import androidx.compose.ui.test.requestFocus
import com.binge.designsystem.testing.createKeyboardComposeRule
import com.binge.designsystem.tv.theme.BingeTvTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private const val KEY_ROW = "row"
private const val ROW_LABEL = "Genre"
private const val PINNED = "My services"
private const val FIRST = "Drama"
private const val SECOND = "Comedy"
private const val EXCLUDED_DESCRIPTION = "Excluded"
private const val MARK_TAG = "service-mark"

/**
 * A [TvListPaneBoard] row whose options can be ruled out as well as chosen, whose first option is pinned above the
 * rest, and whose options can lead with a mark of their own. Under a real D-pad, as the filter panel is used.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class, qualifiers = "w960dp-h540dp-television-xhdpi")
class TvListPaneTriStateBoardTest {
    @get:Rule
    val rule = createKeyboardComposeRule()

    private var states by mutableStateOf(mapOf<String, String>())
    private var pinnedPressed = 0

    private fun show() {
        rule.setContent {
            var focusedKey by remember { mutableStateOf<String?>(KEY_ROW) }
            BingeTvTheme {
                TvListPaneBoard(
                    title = "Filters",
                    groups =
                        listOf(
                            TvPaneGroup(
                                title = "Refine",
                                rows =
                                    listOf(
                                        TvPaneRow(
                                            key = KEY_ROW,
                                            label = ROW_LABEL,
                                            multiSelect = true,
                                            pinnedOptions = listOf(TvPaneOption(PINNED, onSelect = { pinnedPressed++ })),
                                            options =
                                                listOf(FIRST, SECOND).map { value ->
                                                    TvPaneOption(
                                                        label = value,
                                                        selected = states[value] == INCLUDED,
                                                        excluded = states[value] == EXCLUDED,
                                                        onSelect = { states = states + (value to next(states[value])) },
                                                        leading = { Box(Modifier.testTag("$MARK_TAG-$value")) },
                                                    )
                                                },
                                        ),
                                    ),
                            ),
                        ),
                    focusedKey = focusedKey,
                    onFocusRow = { focusedKey = it },
                )
            }
        }
        rule.waitForIdle()
        node(ROW_LABEL).requestFocus()
        rule.waitForIdle()
        press(Key.DirectionRight)
    }

    private fun next(state: String?) =
        when (state) {
            INCLUDED -> EXCLUDED
            EXCLUDED -> NEUTRAL
            else -> INCLUDED
        }

    private fun node(label: String) = rule.onNode(hasText(label) and isFocusable())

    private fun press(key: Key) {
        rule.onNode(isFocused()).performKeyInput { pressKey(key) }
        rule.waitForIdle()
    }

    private fun toggle(state: ToggleableState) = SemanticsMatcher.expectValue(SemanticsProperties.ToggleableState, state)

    @Test
    fun `an excluded option announces as indeterminate and excluded, not as ticked`() {
        states = mapOf(FIRST to EXCLUDED, SECOND to INCLUDED)
        show()

        node(FIRST).assert(toggle(ToggleableState.Indeterminate))
        node(FIRST).assert(SemanticsMatcher.expectValue(SemanticsProperties.StateDescription, EXCLUDED_DESCRIPTION))
        node(SECOND).assert(toggle(ToggleableState.On))
        node(SECOND).assert(SemanticsMatcher.keyNotDefined(SemanticsProperties.StateDescription))
    }

    @Test
    fun `OK cycles an option through ticked and excluded and back, keeping focus`() {
        show()
        press(Key.DirectionDown)
        node(FIRST).assertIsFocused()

        press(Key.DirectionCenter)
        node(FIRST).assert(toggle(ToggleableState.On))
        press(Key.DirectionCenter)
        node(FIRST).assert(toggle(ToggleableState.Indeterminate))
        press(Key.DirectionCenter)
        node(FIRST).assert(toggle(ToggleableState.Off))

        node(FIRST).assertIsFocused()
    }

    @Test
    fun `the pinned option takes focus first, and down and up move between it and the options`() {
        show()

        node(PINNED).assertIsFocused()
        press(Key.DirectionDown)
        node(FIRST).assertIsFocused()
        press(Key.DirectionUp)
        node(PINNED).assertIsFocused()

        press(Key.DirectionCenter)
        assertEquals(1, pinnedPressed)
    }

    @Test
    fun `an option leads with the mark it was given`() {
        show()

        rule.onNodeWithTagExists("$MARK_TAG-$FIRST")
        rule.onNodeWithTagExists("$MARK_TAG-$SECOND")
    }

    private fun androidx.compose.ui.test.junit4.ComposeContentTestRule.onNodeWithTagExists(tag: String) =
        onNode(SemanticsMatcher.expectValue(SemanticsProperties.TestTag, tag), useUnmergedTree = true).assertExists()
}

private const val INCLUDED = "included"
private const val EXCLUDED = "excluded"
private const val NEUTRAL = "neutral"
