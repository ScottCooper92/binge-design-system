package com.binge.designsystem.template

import androidx.compose.foundation.layout.Column
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.binge.designsystem.testing.TestTheme
import com.binge.designsystem.testing.createTakeDownComposeRule
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private val YEARS = List(80) { (2026 - it).toString() }

private fun yearsSection(selected: Set<String> = emptySet()) =
    FilterSectionUi(
        id = "years",
        title = "Release year",
        control = FilterControlUi.MultiChoice(YEARS.map { FilterOptionUi(it, it) }, includedIds = selected, excludedIds = emptySet()),
    )

/** The search over a long list of options: it keeps the options whose label contains the query, ignoring case, and says so when none do. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class, qualifiers = "w360dp-h2400dp")
class FilterSearchTest {
    @get:Rule
    val rule = createTakeDownComposeRule()

    // The search field shows the query as text of its own, so a chip is found by its role, not by its label alone.
    private fun chip(label: String, substring: Boolean = false) =
        SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Button) and hasText(label, substring = substring)

    private fun show(query: String, clicks: MutableList<String> = mutableListOf()) {
        rule.setContent {
            TestTheme {
                Column {
                    FilterSectionContent(
                        section = yearsSection(),
                        clearLabel = "Clear",
                        clearEnabled = false,
                        doneLabel = "Done",
                        onClear = {},
                        onDone = {},
                        handlers = FilterControlHandlers(onOptionClick = { _, id -> clicks += id }),
                        search = FilterSearchUi(
                            query,
                            onQueryChange = {},
                            placeholder = "Search years",
                            noResultsLabel = "No year matches",
                        ),
                    )
                }
            }
        }
    }

    @Test
    fun `matching keeps the options whose label contains the query, ignoring case`() {
        val section =
            FilterSectionUi(
                "genre",
                "Genre",
                FilterControlUi.SingleChoice(
                    listOf(FilterOptionUi("a", "Action"), FilterOptionUi("s", "Sci-Fi"), FilterOptionUi("c", "Comedy")),
                    null,
                ),
            )

        val kept = (section.matching("ACT").control as FilterControlUi.SingleChoice).options.map { it.label }

        assertEquals(listOf("Action"), kept)
        assertEquals("a blank query keeps them all", section, section.matching("  "))
    }

    @Test
    fun `a range has no options to search, so it is left alone`() {
        val range =
            FilterSectionUi(
                "rating",
                "Rating",
                FilterControlUi.Range(0f..10f, 0f..10f, 9, { it.toString() }, "From", "To"),
            )

        assertEquals(range, range.matching("anything"))
    }

    @Test
    fun `with no query every year is a chip`() {
        show(query = "")

        rule.onAllNodes(chip("20", substring = true)).assertCountEquals(YEARS.count { "20" in it })
        rule.onNode(chip("1947")).assertExists()
    }

    @Test
    fun `a query leaves only the matching years`() {
        show(query = "199")

        rule.onNode(chip("1994")).assertExists()
        rule.onNode(chip("2024")).assertDoesNotExist()
        rule.onAllNodes(chip("199", substring = true)).assertCountEquals(YEARS.count { "199" in it })
    }

    @Test
    fun `a query that matches nothing shows the no-results line and no chips`() {
        show(query = "xyz")

        rule.onNodeWithText("No year matches").assertExists()
        rule.onNode(chip("2024")).assertDoesNotExist()
    }

    @Test
    fun `a chip found by search names its option by the id it was given`() {
        val clicks = mutableListOf<String>()
        show(query = "1994", clicks)

        rule.onNode(chip("1994")).performClick()

        assertEquals(listOf("1994"), clicks)
    }
}
