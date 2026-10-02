package com.binge.designsystem.catalogapp.phone

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
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

    private fun launch() = rule.setContent { MaterialTheme { CatalogApp() } }

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
    fun `searching narrows the list and selecting opens the sample, back returns`() {
        launch()
        search("button family")

        rule.onNodeWithText("Button family").performClick()
        rule.onNodeWithText("Filled").assertIsDisplayed()

        rule.onNodeWithContentDescription("Back to the list").performClick()
        rule.onNode(hasSetTextAction()).assertIsDisplayed()
    }

    @Test
    fun `a query that matches nothing says so`() {
        launch()
        search("zzz-no-such-sample")

        rule.onNodeWithText("No samples match “zzz-no-such-sample”").assertIsDisplayed()
    }

    @Test
    fun `the three controls toggle in the detail view`() {
        launch()
        search("button family")
        rule.onNodeWithText("Button family").performClick()

        rule.onNodeWithText("RTL").assertIsNotSelected().performClick()
        rule.onNodeWithText("RTL").assertIsSelected()

        rule.onNodeWithText("Dark").performClick()
        rule.onNodeWithText("2.0×").performClick()
        rule.onNodeWithText("2.0×").assertIsSelected()
        rule.onNodeWithText("1.0×").assertIsNotSelected()
    }
}
