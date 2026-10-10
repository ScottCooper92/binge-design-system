package com.binge.designsystem.tv.component

import android.app.Application
import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.onNodeWithTag
import com.binge.designsystem.testing.createKeyboardComposeRule
import com.binge.designsystem.tv.focus.tvClickable
import com.binge.designsystem.tv.theme.BingeTvTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Android TV ships TalkBack. A control that acts like a button has to announce as one, and a chosen row has to
 * announce as chosen, not only draw a tick.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class, qualifiers = "w960dp-h540dp-television-xhdpi")
class TvControlSemanticsTest {
    @get:Rule
    val rule = createKeyboardComposeRule()

    private fun hasRole(role: Role) = SemanticsMatcher.expectValue(SemanticsProperties.Role, role)

    private fun hasSelected(selected: Boolean) = SemanticsMatcher.expectValue(SemanticsProperties.Selected, selected)

    @Test
    fun `a TvButton announces as a button`() {
        rule.setContent { BingeTvTheme { TvButton(label = "Play", onClick = {}, modifier = Modifier.testTag(NODE)) } }

        rule.onNodeWithTag(NODE).assert(hasRole(Role.Button))
    }

    @Test
    fun `a disabled TvButton still announces as a button, an unavailable one`() {
        rule.setContent { BingeTvTheme { TvButton(label = "Play", onClick = {}, enabled = false, modifier = Modifier.testTag(NODE)) } }

        rule.onNodeWithTag(NODE).assert(hasRole(Role.Button))
        rule.onNodeWithTag(NODE).assertIsNotEnabled()
    }

    @Test
    fun `a TvIconButton announces as a button`() {
        rule.setContent {
            BingeTvTheme { TvIconButton(icon = Icons.Filled.Home, label = "Home", onClick = {}, modifier = Modifier.testTag(NODE)) }
        }

        rule.onNodeWithTag(NODE).assert(hasRole(Role.Button))
    }

    @Test
    fun `a TvSeeAllTile announces as a button`() {
        rule.setContent {
            BingeTvTheme {
                TvSeeAllTile(label = "See all", isFocused = false, onFocusChanged = {}, onClick = {}, modifier = Modifier.testTag(NODE))
            }
        }

        rule.onNodeWithTag(NODE).assert(hasRole(Role.Button))
    }

    @Test
    fun `tvClickable announces no role unless it is given one`() {
        rule.setContent {
            BingeTvTheme {
                Box(Modifier.testTag(NODE).tvClickable(onFocusChanged = {}, onClick = {}))
            }
        }

        rule.onNodeWithTag(NODE).assert(SemanticsMatcher.keyNotDefined(SemanticsProperties.Role))
    }

    @Test
    fun `tvSelected reports a row as chosen or not chosen`() {
        rule.setContent {
            BingeTvTheme {
                Box(Modifier.testTag("on").tvSelected(true))
                Box(Modifier.testTag("off").tvSelected(false))
            }
        }

        rule.onNodeWithTag("on").assert(hasSelected(true))
        rule.onNodeWithTag("off").assert(hasSelected(false))
    }

    private companion object {
        const val NODE = "node"
    }
}
