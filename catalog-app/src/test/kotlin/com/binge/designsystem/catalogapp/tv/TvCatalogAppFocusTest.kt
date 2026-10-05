package com.binge.designsystem.catalogapp.tv

import android.app.Application
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.pressKey
import androidx.compose.ui.unit.dp
import com.binge.designsystem.catalogapp.registry.CatalogEntry
import com.binge.designsystem.testing.createKeyboardComposeRule
import com.binge.designsystem.tv.theme.BingeTvTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * The TV list is operable from a remote alone: first focus, directional moves, OK to open a sample and
 * the on-screen Back returning to the row that opened it. Rows are fixed fakes, so a new design-system
 * sample cannot change what these assert. `television` is load-bearing: a 2D focus search on the
 * default handset screen behaves nothing like a TV panel.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class, qualifiers = "w960dp-h540dp-television-xhdpi")
class TvCatalogAppFocusTest {
    @get:Rule
    val rule = createKeyboardComposeRule()

    private val entries =
        listOf("Alpha", "Beta", "Gamma").map { name ->
            CatalogEntry(id = name, group = "Group", name = name, description = "") { Box(Modifier.size(10.dp)) }
        }

    private fun launch() = rule.setContent { BingeTvTheme { TvCatalogApp(entries) } }

    private fun press(text: String, key: Key) {
        rule.onNodeWithText(text).performKeyInput { pressKey(key) }
        rule.waitForIdle()
    }

    @Test
    fun `the first row takes focus on launch`() {
        launch()
        rule.waitForIdle()

        rule.onNodeWithText("Alpha").assertIsFocused()
    }

    @Test
    fun `the D-pad moves down through the rows and back up`() {
        launch()
        rule.waitForIdle()

        press("Alpha", Key.DirectionDown)
        rule.onNodeWithText("Beta").assertIsFocused()
        press("Beta", Key.DirectionDown)
        rule.onNodeWithText("Gamma").assertIsFocused()
        press("Gamma", Key.DirectionUp)
        rule.onNodeWithText("Beta").assertIsFocused()
    }

    @Test
    fun `OK opens a sample with focus on Back, and Back returns to the same row`() {
        launch()
        rule.waitForIdle()
        press("Alpha", Key.DirectionDown)

        press("Beta", Key.DirectionCenter)
        rule.onNodeWithText("Back").assertIsFocused()

        press("Back", Key.DirectionCenter)
        rule.onNodeWithText("Back").assertDoesNotExist()
        rule.onNodeWithText("Beta").assertIsFocused()
    }

    @Test
    fun `focus cannot leave an open sample into the list beneath`() {
        launch()
        rule.waitForIdle()
        press("Alpha", Key.DirectionCenter)

        press("Back", Key.DirectionUp)
        press("Back", Key.DirectionLeft)

        rule.onNodeWithText("Back").assertIsFocused()
    }

    /**
     * Activity recreation with a sample open on a row below the fold: the selection and the list's scroll
     * position are both saved, so the row is composed again and focus can return to it.
     */
    @Test
    fun `Back returns to a row below the fold after the activity is recreated`() {
        val tall = (0 until TALL_LIST_SIZE).map { index ->
            val name = "Row %02d".format(index)
            CatalogEntry(id = name, group = "Group", name = name, description = "") { Box(Modifier.size(10.dp)) }
        }
        val tester = StateRestorationTester(rule)
        tester.setContent { BingeTvTheme { TvCatalogApp(tall) } }
        rule.waitForIdle()
        rule.onNodeWithText("Row %02d".format(DEEP_ROW)).assertDoesNotExist()
        (0 until DEEP_ROW).forEach { press("Row %02d".format(it), Key.DirectionDown) }
        press("Row %02d".format(DEEP_ROW), Key.DirectionCenter)

        tester.emulateSavedInstanceStateRestore()
        rule.waitForIdle()
        press("Back", Key.DirectionCenter)

        rule.onNodeWithText("Back").assertDoesNotExist()
        rule.onNodeWithText("Row %02d".format(DEEP_ROW)).assertIsFocused()
    }

    private companion object {
        const val TALL_LIST_SIZE = 40
        const val DEEP_ROW = 25
    }
}
