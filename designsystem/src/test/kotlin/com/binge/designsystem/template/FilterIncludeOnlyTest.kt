package com.binge.designsystem.template

import androidx.compose.foundation.layout.Column
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import com.binge.designsystem.testing.TestTheme
import com.binge.designsystem.testing.createTakeDownComposeRule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** An include-only [FilterControlUi.MultiChoice] has two states and no way to exclude; the include/exclude form is as it was. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class)
class FilterIncludeOnlyTest {
    @get:Rule
    val rule = createTakeDownComposeRule()

    private val options = listOf(FilterOptionUi("action", "Action"), FilterOptionUi("drama", "Drama"), FilterOptionUi("horror", "Horror"))

    private fun show(
        includeOnly: Boolean,
        clicks: MutableList<String> = mutableListOf(),
        longClicks: MutableList<String> = mutableListOf(),
    ) {
        rule.setContent {
            TestTheme {
                Column {
                    FilterSectionContent(
                        section =
                            FilterSectionUi(
                                id = "genre",
                                title = "Genres",
                                control =
                                    FilterControlUi.MultiChoice(
                                        options = options,
                                        includedIds = setOf("action"),
                                        // In the set, but an include-only filter reads it as off.
                                        excludedIds = setOf("horror"),
                                        includeOnly = includeOnly,
                                    ),
                            ),
                        clearLabel = "Clear",
                        clearEnabled = true,
                        doneLabel = "Apply",
                        onClear = {},
                        onDone = {},
                        handlers =
                            FilterControlHandlers(
                                onOptionClick = { _, id -> clicks += id },
                                onOptionLongClick = { _, id -> longClicks += id },
                            ),
                    )
                }
            }
        }
    }

    private fun chip(label: String): SemanticsNodeInteraction = rule.onNodeWithText(label)

    private fun SemanticsNodeInteraction.selected(): Boolean? = fetchSemanticsNode().config.getOrNull(SemanticsProperties.Selected)

    private fun SemanticsNodeInteraction.stateWords(): String? = fetchSemanticsNode().config.getOrNull(SemanticsProperties.StateDescription)

    private fun SemanticsNodeInteraction.hasLongClick(): Boolean = fetchSemanticsNode().config.contains(SemanticsActions.OnLongClick)

    @Test
    fun `an include-only chip reads as selected or not, with no include or exclude words`() {
        show(includeOnly = true)

        assertEquals(true, chip("Action").selected())
        assertEquals(false, chip("Drama").selected())
        assertEquals("an id in excludedIds reads as off", false, chip("Horror").selected())
        listOf("Action", "Drama", "Horror").forEach { assertNull("$it has state words", chip(it).stateWords()) }
    }

    @Test
    fun `an include-only chip takes no long press`() {
        val clicks = mutableListOf<String>()
        val longClicks = mutableListOf<String>()
        show(includeOnly = true, clicks, longClicks)

        listOf("Action", "Drama", "Horror").forEach { assertFalse("$it has a long click", chip(it).hasLongClick()) }
        chip("Drama").performTouchInput { longClick() }

        assertTrue("a long press reached the caller: $longClicks", longClicks.isEmpty())
    }

    @Test
    fun `a tap on an include-only chip names the option`() {
        val clicks = mutableListOf<String>()
        show(includeOnly = true, clicks)

        chip("Drama").performClick()

        assertEquals(listOf("drama"), clicks)
    }

    @Test
    fun `the include and exclude form is unchanged`() {
        val longClicks = mutableListOf<String>()
        show(includeOnly = false, longClicks = longClicks)

        assertEquals("Included", chip("Action").stateWords())
        assertEquals("Excluded", chip("Horror").stateWords())
        assertEquals("Not set", chip("Drama").stateWords())
        assertTrue(chip("Drama").hasLongClick())
        chip("Drama").performTouchInput { longClick() }
        assertEquals(listOf("drama"), longClicks)
    }
}
