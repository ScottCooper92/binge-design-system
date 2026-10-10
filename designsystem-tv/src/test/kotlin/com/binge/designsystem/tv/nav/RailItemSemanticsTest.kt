package com.binge.designsystem.tv.nav

import android.app.Application
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.SemanticsMatcher
import com.binge.designsystem.testing.createKeyboardComposeRule
import com.binge.designsystem.tv.theme.BingeTvTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private const val HOME = "Home"
private const val ACCOUNT = "Account"

/**
 * A screen reader names a rail item by its label, once, collapsed or expanded (#370). Collapsed, the rail draws
 * only the glyph, which used to leave each destination an unnamed tab, and the header's avatar read its initials.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class, qualifiers = "w960dp-h540dp-television-xhdpi")
class RailItemSemanticsTest {
    @get:Rule
    val rule = createKeyboardComposeRule()

    private val tab = SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Tab)

    private fun show(item: TvNavRailItem, expanded: Boolean) =
        rule.setContent {
            BingeTvTheme {
                RailItem(item = item, selectedKey = item.key, expanded = expanded, onSelect = {})
            }
        }

    /** Everything the item's merged node reads: its descriptions, then its text. */
    private fun spoken(): List<String> {
        val config = rule.onNode(tab).fetchSemanticsNode().config
        return config.getOrNull(SemanticsProperties.ContentDescription).orEmpty() +
            config.getOrNull(SemanticsProperties.Text).orEmpty().map { it.text }
    }

    @Test
    fun `a collapsed item is named by its label`() {
        show(TvNavRailItem(key = "home", label = HOME, icon = Icons.Filled.Home), expanded = false)

        assertEquals(listOf(HOME), spoken())
    }

    @Test
    fun `an expanded item reads its label once`() {
        show(TvNavRailItem(key = "home", label = HOME, icon = Icons.Filled.Home), expanded = true)

        assertEquals(listOf(HOME), spoken())
    }

    @Test
    fun `a collapsed avatar item reads its label, not its initials`() {
        show(
            TvNavRailItem(key = "account", label = ACCOUNT, icon = Icons.Filled.AccountCircle, displayName = "Ana Lima"),
            expanded = false,
        )

        assertEquals(listOf(ACCOUNT), spoken())
    }
}
