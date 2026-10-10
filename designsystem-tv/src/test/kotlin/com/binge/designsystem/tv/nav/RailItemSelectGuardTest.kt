package com.binge.designsystem.tv.nav

import android.app.Application
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertIsFocused
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

/**
 * Focus on a rail item commits a *change* of selection and never a restatement (`RailItem`). The guard is what
 * keeps a drill-down alive: a forward navigation disposes the control that caused it, Compose parks focus on the
 * rail, and committing that would pick a destination and drop the route the user asked for.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class, qualifiers = "w960dp-h540dp-television-xhdpi")
class RailItemSelectGuardTest {
    @get:Rule
    val rule = createKeyboardComposeRule()

    private val selected = mutableListOf<Any>()

    private val tab = SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Tab)

    private fun show(selectedKey: Any?) =
        rule.setContent {
            BingeTvTheme {
                RailItem(
                    item = TvNavRailItem(key = ITEM_KEY, label = "Home", icon = Icons.Filled.Home),
                    selectedKey = selectedKey,
                    expanded = true,
                    onSelect = { selected += it },
                )
            }
        }

    @Test
    fun `focus on an item that is not selected selects it`() {
        show(selectedKey = OTHER_KEY)

        rule.onNode(tab).requestFocus()
        rule.waitForIdle()

        rule.onNode(tab).assertIsFocused()
        assertEquals(listOf<Any>(ITEM_KEY), selected)
    }

    @Test
    fun `focus on the item that is already selected selects nothing`() {
        show(selectedKey = ITEM_KEY)

        rule.onNode(tab).requestFocus()
        rule.waitForIdle()

        rule.onNode(tab).assertIsFocused()
        assertEquals(emptyList<Any>(), selected)
    }

    @Test
    fun `OK on the item that is already selected still selects it`() {
        show(selectedKey = ITEM_KEY)
        rule.onNode(tab).requestFocus()
        rule.waitForIdle()

        rule.onNode(tab).performKeyInput { pressKey(Key.DirectionCenter) }
        rule.waitForIdle()

        assertEquals(listOf<Any>(ITEM_KEY), selected)
    }

    private companion object {
        const val ITEM_KEY = "home"
        const val OTHER_KEY = "other"
    }
}
