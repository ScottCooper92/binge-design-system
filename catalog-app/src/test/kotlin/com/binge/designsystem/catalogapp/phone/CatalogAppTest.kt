package com.binge.designsystem.catalogapp.phone

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasScrollToNodeAction
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextInput
import com.binge.designsystem.theme.BingeExpressiveTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** The list, search, detail and back flow, driven through semantics rather than pixels. */
@RunWith(RobolectricTestRunner::class)
@Config(qualifiers = "w411dp-h900dp-xxhdpi")
class CatalogAppTest {
    @get:Rule
    val rule = createComposeRule()

    private fun launch() = rule.setContent { BingeExpressiveTheme { CatalogApp() } }

    private fun openTweaks() {
        rule.onNodeWithContentDescription("Tweaks").performClick()
        rule.waitForIdle()
    }

    private fun search(text: String) {
        rule.onNode(hasSetTextAction()).performTextInput(text)
        rule.waitForIdle()
    }

    @Test
    fun `opens on the list with a search field`() {
        launch()

        rule.onNode(hasSetTextAction()).assertIsDisplayed()
    }

    @Test
    fun `searching narrows the grid and a component opens on its variants, back returns`() {
        launch()
        search("filled button")

        rule.onNodeWithText("Buttons").performClick()
        // The filled button's list leads the page: its heading, and its buttons named by their state.
        rule.onNodeWithText("Filled button").assertIsDisplayed()
        rule.onAllNodesWithText("Leading icon").onFirst().assertIsDisplayed()

        rule.onNodeWithContentDescription(NAVIGATE_BACK).performClick()
        rule.onNode(hasSetTextAction()).assertIsDisplayed()
    }

    @Test
    fun `the grid keeps its scroll position across opening a component`() {
        launch()
        rule.onNode(hasScrollToNodeAction()).performScrollToNode(hasText("Watch provider grid"))
        rule.onNodeWithText("Watch provider grid").performClick()

        rule.onNodeWithContentDescription(NAVIGATE_BACK).performClick()

        rule.onNodeWithText("Watch provider grid").assertIsDisplayed()
    }

    @Test
    fun `a query that matches nothing says so`() {
        launch()
        search("zzz-no-such-sample")

        rule.onNodeWithText("No samples match “zzz-no-such-sample”").assertIsDisplayed()
    }

    @Test
    fun `the controls toggle in the tweaks sheet`() {
        launch()
        search("filled button")
        rule.onNodeWithText("Buttons").performClick()
        openTweaks()

        rule.onNodeWithText("RTL").assertIsNotSelected().performClick()
        rule.onNodeWithText("RTL").assertIsSelected()

        rule.onNodeWithText("Dark").performClick()
        rule.onNodeWithText("2.0×").performClick()
        rule.onNodeWithText("2.0×").assertIsSelected()
        rule.onNodeWithText("1.0×").assertIsNotSelected()
    }

    @Test
    fun `a top bar demo replaces the page's chrome and its own back button leaves`() {
        launch()
        rule.onNodeWithText("Demos").assertIsDisplayed()

        search("enter always")
        rule.onNodeWithText("Top app bars").performClick()

        rule.onNodeWithText("collapsedFraction = 0.00").assertIsDisplayed()
        // No catalog bar above the demo: its title would name the page.
        rule.onNodeWithText("Top app bars").assertDoesNotExist()

        rule.onNodeWithContentDescription(NAVIGATE_BACK).performClick()
        rule.onNode(hasSetTextAction()).assertIsDisplayed()
    }

    @Test
    fun `the top app bars share a page whose sheet switches the bar`() {
        launch()
        search("enter always")
        rule.onNodeWithText("Top app bars").performClick()
        openTweaks()

        rule.onNode(hasText("Detail overlay top bar") and hasClickAction()).performScrollTo().performClick()

        rule.onNode(hasText("Detail overlay top bar") and hasClickAction()).assertIsSelected()
        rule.onNodeWithText("collapsedFraction = 0.00").assertDoesNotExist()
        rule.onNodeWithText("scrolled = 0.00").assertExists()
    }

    @Test
    fun `the navigation bars share a page, and their fixtures are not listed`() {
        launch()
        search("floating")

        rule.onNodeWithText("Navigation bars").assertIsDisplayed()
        rule.onNodeWithText("Nav floating bar").assertDoesNotExist()
    }

    @Test
    fun `a listed component offers no variant choice in the sheet`() {
        launch()
        search("filled button")
        rule.onNodeWithText("Buttons").performClick()
        openTweaks()

        rule.onNodeWithText("Variant").assertDoesNotExist()
    }

    @Test
    fun `the language control is offered in the tweaks sheet`() {
        launch()
        search("filled button")
        rule.onNodeWithText("Buttons").performClick()
        openTweaks()

        rule
            .onNodeWithText("Español")
            .performScrollTo()
            .assertIsNotSelected()
            .performClick()
        rule.onNodeWithText("Español").assertIsSelected()
    }

    private companion object {
        /** The design system's own back button, on the demo's bar. */
        const val NAVIGATE_BACK = "Back"
    }
}
