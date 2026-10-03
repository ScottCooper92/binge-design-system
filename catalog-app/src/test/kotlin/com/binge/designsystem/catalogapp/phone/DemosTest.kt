package com.binge.designsystem.catalogapp.phone

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeUp
import com.binge.designsystem.catalog.BingeMediumTopBarDemo
import com.binge.designsystem.catalog.BingePaneTopBarDemo
import com.binge.designsystem.catalog.BingeSnackbarHostDemo
import com.binge.designsystem.catalog.BingeTextButtonLoadingDemo
import com.binge.designsystem.catalog.BingeTopBarEnterAlwaysDemo
import com.binge.designsystem.catalog.BingeTopBarExitUntilCollapsedDemo
import com.binge.designsystem.catalog.DialogsDemo
import com.binge.designsystem.catalog.ExpressiveIconButtonLoadingDemo
import com.binge.designsystem.catalog.LocalisedStringsDemo
import com.binge.designsystem.catalog.SheetsDemo
import com.binge.designsystem.catalogapp.overrides.SampleLocale
import com.binge.designsystem.catalogapp.overrides.SampleOverrides
import com.binge.designsystem.catalogapp.overrides.WithOverrides
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import com.binge.designsystem.R as DesR

/** The demos run the real components, so these drive them for real: tap, scroll, dismiss, switch locale. */
@RunWith(RobolectricTestRunner::class)
@Config(qualifiers = "w411dp-h900dp-xxhdpi")
class DemosTest {
    @get:Rule
    val rule = createComposeRule()

    /** Taps the one clickable and steps a frame, since a paused test clock does not recompose on its own. */
    private fun tapOnPausedClock() {
        rule.onNode(hasClickAction()).performClick()
        rule.mainClock.advanceTimeByFrame()
    }

    private fun fraction(): String {
        // Either name: each demo prints the fraction its bar is actually built from.
        val node = rule.onNode(hasText("Fraction = ", substring = true)).fetchSemanticsNode()
        return node.config[SemanticsProperties.Text].joinToString { it.text }.substringAfter("= ")
    }

    @Test
    fun `every scroll demo's scrim fraction rises as the list scrolls`() {
        var which by mutableIntStateOf(0)
        val demos: List<@Composable () -> Unit> =
            listOf(
                { BingeTopBarEnterAlwaysDemo() },
                { BingeTopBarExitUntilCollapsedDemo() },
                { BingeMediumTopBarDemo() },
                { BingePaneTopBarDemo() },
            )
        rule.setContent { demos[which]() }

        demos.indices.forEach { index ->
            which = index
            rule.waitForIdle()
            assertEquals("demo $index at rest", "0.00", fraction())

            repeat(3) {
                rule.onNode(hasScrollAction()).performTouchInput { swipeUp() }
                rule.waitForIdle()
            }

            assertEquals("demo $index after scrolling", "1.00", fraction())
        }
    }

    @Test
    fun `the bottom sheet opens and dismisses for real`() {
        rule.setContent { SheetsDemo() }

        rule.onNodeWithText("Bottom sheet").performClick()
        rule.onNodeWithText("A real modal bottom sheet", substring = true).assertIsDisplayed()
        rule.onNodeWithText("Done").performClick()

        rule.onNodeWithText("Dismissed by Done").assertIsDisplayed()
        rule.onNodeWithText("A real modal bottom sheet", substring = true).assertDoesNotExist()
    }

    @Test
    fun `each dialog opens from its button and reports how it closed`() {
        rule.setContent { DialogsDemo() }

        rule.onNodeWithText("Destructive confirm dialog").performClick()
        rule.onNodeWithText("Delete list?").assertIsDisplayed()
        rule.onNodeWithText("Cancel").performClick()
        rule.onNodeWithText("Cancelled").assertIsDisplayed()
        rule.onNodeWithText("Delete list?").assertDoesNotExist()

        rule.onNodeWithText("Confirm dialog").performClick()
        rule.onNodeWithText("Remove").performClick()
        rule.onNodeWithText("Confirmed").assertIsDisplayed()
        // The create-list dialog is not opened here: its field takes focus, and under Robolectric a
        // focused field's blinking cursor keeps the clock busy until the heap runs out.
        rule.onNodeWithText("Create list dialog").assertIsDisplayed()
    }

    @Test
    fun `the snackbar host shows a message and reports its action`() {
        rule.setContent { BingeSnackbarHostDemo() }

        rule.onNodeWithText("Error with Retry").performClick()
        rule.onNodeWithText("Couldn't save your rating").assertIsDisplayed()
        rule.onNodeWithText("Retry").performClick()

        rule.onNodeWithText("Action tapped").assertIsDisplayed()
    }

    @Test
    fun `the side sheet opens and closes for real`() {
        rule.setContent { SheetsDemo() }

        rule.onNodeWithText("Side sheet").performClick()
        rule.onNodeWithText("A real modal side sheet", substring = true).assertIsDisplayed()
        rule.onNodeWithText("Close").performClick()

        rule.onNodeWithText("Closed by button").assertIsDisplayed()
    }

    @Test
    fun `a busy text button swallows taps until it settles`() {
        rule.mainClock.autoAdvance = false
        rule.setContent { BingeTextButtonLoadingDemo() }

        tapOnPausedClock()
        rule.onNodeWithText("Taps that reached the button: 1").assertIsDisplayed()
        tapOnPausedClock()
        rule.onNodeWithText("Taps that reached the button: 1").assertIsDisplayed()

        rule.mainClock.advanceTimeBy(BUSY_SETTLE_MILLIS)
        tapOnPausedClock()
        rule.onNodeWithText("Taps that reached the button: 2").assertIsDisplayed()
    }

    @Test
    fun `a busy icon button swallows taps until it settles`() {
        rule.mainClock.autoAdvance = false
        rule.setContent { ExpressiveIconButtonLoadingDemo() }

        rule.onNode(hasClickAction()).performClick()
        tapOnPausedClock()
        rule.onNodeWithText("Taps that reached the button: 1").assertIsDisplayed()

        rule.mainClock.advanceTimeBy(BUSY_SETTLE_MILLIS)
        tapOnPausedClock()
        rule.onNodeWithText("Taps that reached the button: 2").assertIsDisplayed()
    }

    @Test
    fun `switching locale re-renders a labelled component`() {
        var locale by mutableStateOf(SampleLocale.English)
        rule.setContent {
            WithOverrides(SampleOverrides(dark = false, fontScale = 1f, rtl = false, locale = locale)) {
                Text(stringResource(DesR.string.action_cancel))
            }
        }

        rule.onNodeWithText("Cancel").assertIsDisplayed()
        locale = SampleLocale.Spanish
        rule.waitForIdle()
        rule.onNodeWithText("Cancelar").assertIsDisplayed()
        rule.onNodeWithText("Cancel").assertDoesNotExist()
    }

    @Test
    fun `the locale demo shows Spanish through the real components`() {
        rule.setContent {
            WithOverrides(SampleOverrides(dark = false, fontScale = 1f, rtl = false, locale = SampleLocale.Spanish)) {
                LocalisedStringsDemo()
            }
        }

        rule.onNodeWithText("Cancelar").assertIsDisplayed()
    }

    @Test
    fun `a mirrored pseudolocale mirrors layout without the RTL chip`() {
        var seen = ""
        rule.setContent {
            WithOverrides(SampleOverrides(dark = false, fontScale = 1f, rtl = false, locale = SampleLocale.PseudoMirrored)) {
                seen = androidx.compose.ui.platform.LocalLayoutDirection.current.name
            }
        }
        rule.waitForIdle()

        assertTrue("layout direction was $seen", seen == "Rtl")
    }

    private companion object {
        const val BUSY_SETTLE_MILLIS = 2_100L
    }
}
