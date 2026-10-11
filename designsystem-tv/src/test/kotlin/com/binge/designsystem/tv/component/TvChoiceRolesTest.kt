package com.binge.designsystem.tv.component

import android.app.Application
import androidx.compose.foundation.layout.Column
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isFocusable
import com.binge.designsystem.testing.createKeyboardComposeRule
import com.binge.designsystem.tv.theme.BingeTvTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private const val CHOSEN = "Chosen"
private const val OTHER = "Other"

/**
 * The single-choice controls say what they are and which one is chosen, as the checkbox row does: an option row and
 * a choice pill announce as radio buttons, a tab as a tab, and the chosen one of each as selected.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class, qualifiers = "w960dp-h540dp-television-xhdpi")
class TvChoiceRolesTest {
    @get:Rule
    val composeTestRule = createKeyboardComposeRule()

    @Test
    fun `an option row announces as a radio button, selected when chosen`() {
        composeTestRule.setContent {
            BingeTvTheme {
                TvOptionGroup(
                    title = "Title",
                    choices = listOf(CHOSEN to CHOSEN, OTHER to OTHER),
                    selected = CHOSEN,
                    onSelect = {},
                )
            }
        }

        control(CHOSEN).assert(hasRole(Role.RadioButton)).assertIsSelected()
        control(OTHER).assert(hasRole(Role.RadioButton)).assertIsNotSelected()
    }

    @Test
    fun `a choice pill announces as a radio button, selected when chosen`() {
        composeTestRule.setContent {
            BingeTvTheme {
                TvChoiceRow(
                    choices = listOf(TvChoiceUi(CHOSEN, CHOSEN), TvChoiceUi(OTHER, OTHER)),
                    selectedKey = CHOSEN,
                    onSelect = {},
                )
            }
        }

        control(CHOSEN).assert(hasRole(Role.RadioButton)).assertIsSelected()
        control(OTHER).assert(hasRole(Role.RadioButton)).assertIsNotSelected()
    }

    @Test
    fun `a tab announces as a tab, selected when chosen`() {
        composeTestRule.setContent {
            BingeTvTheme {
                Column {
                    TvTabRow(
                        tabs = listOf(TvTabUi(CHOSEN, CHOSEN), TvTabUi(OTHER, OTHER)),
                        selectedKey = CHOSEN,
                        onSelect = {},
                    )
                }
            }
        }

        control(CHOSEN).assert(hasRole(Role.Tab)).assertIsSelected()
        control(OTHER).assert(hasRole(Role.Tab)).assertIsNotSelected()
    }

    private fun control(label: String): SemanticsNodeInteraction = composeTestRule.onNode(hasText(label) and isFocusable())

    private fun hasRole(role: Role) = SemanticsMatcher.expectValue(SemanticsProperties.Role, role)
}
