package com.binge.designsystem.tv.nav

import android.app.Application
import androidx.activity.OnBackPressedDispatcher
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.pressKey
import androidx.compose.ui.test.requestFocus
import androidx.compose.ui.unit.dp
import com.binge.designsystem.testing.createKeyboardComposeRule
import com.binge.designsystem.tv.theme.BingeTvTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private const val HOME = "home"
private const val SETTINGS = "settings"
private const val CONTENT = "content"
private const val OVERLAY = "overlay"

/** [TvShellScaffold]'s Back rule: content to the rail, the rail to home, home to the caller. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class, qualifiers = "w960dp-h540dp-television-xhdpi")
class TvShellScaffoldTest {
    @get:Rule
    val rule = createKeyboardComposeRule()

    private val tab = SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Tab)

    /** A rail item names itself by description while collapsed and by its label once the rail expands. */
    private fun item(label: String) = tab and (hasContentDescription(label) or hasText(label))

    private val selected = mutableListOf<Any>()
    private var rootBacks = 0
    private lateinit var back: OnBackPressedDispatcher

    private fun show(selectedKey: Any, overlay: (@Composable BoxScope.() -> Unit)? = null) =
        rule.setContent {
            back = requireNotNull(LocalOnBackPressedDispatcherOwner.current).onBackPressedDispatcher
            BingeTvTheme {
                TvShellScaffold(
                    header = null,
                    items = listOf(TvNavRailItem(key = HOME, label = "Home", icon = Icons.Filled.Home)),
                    footer = TvNavRailItem(key = SETTINGS, label = "Settings", icon = Icons.Filled.Settings),
                    selectedKey = selectedKey,
                    homeKey = HOME,
                    onSelect = { selected += it },
                    overlay = overlay,
                    onBackAtRoot = { rootBacks++ },
                ) {
                    Box(Modifier.size(40.dp).testTag(CONTENT).focusable())
                }
            }
        }

    private fun pressBack() {
        rule.runOnUiThread { back.onBackPressed() }
        rule.waitForIdle()
    }

    @Test
    fun `Back in the content moves focus onto the rail's selected item`() {
        show(selectedKey = SETTINGS)
        rule.onNodeWithTag(CONTENT).requestFocus()
        rule.waitForIdle()

        pressBack()

        rule.onNode(item("Settings")).assertIsFocused()
        assertEquals(emptyList<Any>(), selected)
    }

    @Test
    fun `Back on the rail off home selects home`() {
        show(selectedKey = SETTINGS)
        rule.onNode(item("Settings")).requestFocus()
        rule.waitForIdle()

        pressBack()

        assertEquals(listOf<Any>(HOME), selected)
    }

    @Test
    fun `Back on the rail at home goes to the caller`() {
        show(selectedKey = HOME)
        rule.onNode(item("Home")).requestFocus()
        rule.waitForIdle()

        pressBack()

        assertEquals(1, rootBacks)
        assertEquals(emptyList<Any>(), selected)
    }

    @Test
    fun `an overlay is hosted as one`() {
        var hostedAsOverlay = false
        show(selectedKey = HOME, overlay = { hostedAsOverlay = LocalTvHostedAsOverlay.current })

        assertTrue(hostedAsOverlay)
    }

    @Test
    fun `an overlay holds directional focus`() {
        show(selectedKey = HOME, overlay = {
            Box(Modifier.size(40.dp).testTag(OVERLAY).focusable())
        })
        rule.onNodeWithTag(OVERLAY).requestFocus()
        rule.waitForIdle()

        rule.onNodeWithTag(OVERLAY).performKeyInput { pressKey(Key.DirectionLeft) }
        rule.waitForIdle()

        rule.onNodeWithTag(OVERLAY).assertIsFocused()
    }
}
