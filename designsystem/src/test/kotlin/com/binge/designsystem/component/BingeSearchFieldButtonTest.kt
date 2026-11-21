package com.binge.designsystem.component

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.Density
import com.binge.designsystem.testing.createTakeDownComposeRule
import com.binge.designsystem.theme.BingeExpressiveTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private const val PLACEHOLDER = "Search films"
private const val FIELD = "field"
private const val BUTTON = "button"

/**
 * [BingeSearchFieldButton] is the search field's read-only form, so it must be the same pill: the same bounds as the live field under the same
 * header, at 1x and at 2x font scale, and a button to accessibility services, labelled with its placeholder, with no text input.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class)
class BingeSearchFieldButtonTest {
    @get:Rule
    val rule = createTakeDownComposeRule()

    private fun showBoth(fontScale: Float, onClick: () -> Unit = {}) {
        rule.setContent {
            BingeExpressiveTheme(dynamicColor = false) {
                val density = LocalDensity.current
                CompositionLocalProvider(LocalDensity provides Density(density.density, fontScale)) {
                    Column {
                        BingeSearchFieldHeader {
                            BingeSearchField(
                                query = "",
                                onQueryChange = {},
                                onClear = {},
                                placeholder = PLACEHOLDER,
                                modifier = Modifier.testTag(FIELD),
                            )
                        }
                        BingeSearchFieldHeader {
                            BingeSearchFieldButton(placeholder = PLACEHOLDER, onClick = onClick, modifier = Modifier.testTag(BUTTON))
                        }
                    }
                }
            }
        }
        rule.waitForIdle()
    }

    private fun assertSameBounds() {
        val field = rule.onNodeWithTag(FIELD).fetchSemanticsNode()
        val button = rule.onNodeWithTag(BUTTON).fetchSemanticsNode()
        assertTrue("the field has a size", field.size.height > 0 && field.size.width > 0)
        assertEquals("width", field.size.width, button.size.width)
        assertEquals("height", field.size.height, button.size.height)
        assertEquals("start edge", field.boundsInRoot.left, button.boundsInRoot.left, 0f)
    }

    @Test
    fun `the button has the live field's bounds at 1x`() {
        showBoth(fontScale = 1f)
        assertSameBounds()
    }

    @Test
    fun `the button has the live field's bounds at 2x font scale`() {
        showBoth(fontScale = 2f)
        assertSameBounds()
    }

    @Test
    fun `it is a button labelled with its placeholder, with no text input, and a tap opens search`() {
        var taps = 0
        showBoth(fontScale = 1f, onClick = { taps++ })
        val config = rule.onNodeWithTag(BUTTON).fetchSemanticsNode().config
        assertEquals(Role.Button, config[SemanticsProperties.Role])
        assertEquals(PLACEHOLDER, config[SemanticsProperties.Text].single().text)
        assertTrue("no text field under the button", !config.contains(androidx.compose.ui.semantics.SemanticsActions.SetText))

        rule.onNodeWithTag(BUTTON).performClick()
        assertEquals(1, taps)
    }
}
