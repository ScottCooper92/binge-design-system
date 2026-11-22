package com.binge.designsystem.template

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.onAllNodesWithText
import com.binge.designsystem.testing.TestTheme
import com.binge.designsystem.testing.createTakeDownComposeRule
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private const val TITLE = "Settings"

/**
 * [LocalScreenBarDefault] sets the bar a template carries when its caller does not name one, and a named bar still wins. [ScreenBar.None]
 * is the probe: it draws no bar, so the title is on screen exactly when a bar is.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class, qualifiers = "w411dp-h891dp")
class LocalScreenBarDefaultTest {
    @get:Rule
    val rule = createTakeDownComposeRule()

    @Test
    fun `without a provider a scaffold carries the collapsing bar, so its title is drawn`() {
        rule.setContent { TestTheme { BingeScreenScaffold(title = TITLE) {} } }
        rule.waitForIdle()

        rule.onAllNodesWithText(TITLE).assertCountEquals(1)
    }

    @Test
    fun `a provided default replaces the bar of a scaffold that names none`() {
        rule.setContent {
            TestTheme { CompositionLocalProvider(LocalScreenBarDefault provides ScreenBar.None) { BingeScreenScaffold(title = TITLE) {} } }
        }
        rule.waitForIdle()

        rule.onAllNodesWithText(TITLE).assertCountEquals(0)
    }

    @Test
    fun `a bar the caller names wins over the provided default`() {
        rule.setContent {
            TestTheme {
                CompositionLocalProvider(LocalScreenBarDefault provides ScreenBar.None) {
                    BingeScreenScaffold(title = TITLE, bar = ScreenBar.Small) {}
                }
            }
        }
        rule.waitForIdle()

        rule.onAllNodesWithText(TITLE).assertCountEquals(1)
    }

    @Test
    fun `a provided default reaches a form screen too`() {
        rule.setContent {
            TestTheme {
                CompositionLocalProvider(LocalScreenBarDefault provides ScreenBar.None) {
                    FormScreen(title = TITLE, primaryAction = FormAction("Save", onClick = {})) {}
                }
            }
        }
        rule.waitForIdle()

        rule.onAllNodesWithText(TITLE).assertCountEquals(0)
    }
}
