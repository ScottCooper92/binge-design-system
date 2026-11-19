package com.binge.designsystem.template

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import com.binge.designsystem.testing.TestTheme
import com.binge.designsystem.testing.assertNoTextClipped
import com.binge.designsystem.testing.createTakeDownComposeRule
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** The filter sheet's body: it names what was tapped by id, enables Clear all only with something applied, and clips no text at 2x. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class, qualifiers = "w360dp-h2400dp")
class FilterSheetContentTest {
    @get:Rule
    val rule = createTakeDownComposeRule()

    private val picks = mutableListOf<Pair<String, String>>()
    private val rowsOpened = mutableListOf<String>()
    private var cleared = 0
    private var applied = 0

    private val sections =
        listOf(
            FilterSectionUi(
                id = "kind",
                title = "Content",
                control =
                    FilterControlUi.SingleChoice(
                        options = listOf(FilterOptionUi("movies", "Movies"), FilterOptionUi("shows", "TV shows")),
                        selectedId = "movies",
                    ),
            ),
            FilterSectionUi(
                id = "genres",
                title = "Genres",
                control =
                    FilterControlUi.MultiChoice(
                        options = listOf(FilterOptionUi("action", "Action"), FilterOptionUi("horror", "Horror")),
                        includedIds = setOf("action"),
                        excludedIds = emptySet(),
                    ),
            ),
        )

    private fun show(appliedLabel: String?, fontScale: Float = 1f) {
        rule.setContent {
            TestTheme {
                val density = LocalDensity.current
                CompositionLocalProvider(LocalDensity provides Density(density.density, fontScale)) {
                    Column(Modifier.height(2000.dp)) {
                        FilterSheetContent(
                            title = "Filters",
                            clearAllLabel = "Clear all",
                            showResultsLabel = "Show results",
                            appliedLabel = appliedLabel,
                            sections = sections,
                            rowsTitle = "Filter by",
                            rows = listOf(FilterRowUi("year", "Release date", "Any", Icons.Filled.Category)),
                            onSingleSelect = { section, option -> picks += section to option },
                            onOptionClick = { section, option -> picks += section to option },
                            onRowClick = { rowsOpened += it },
                            onClearAll = { cleared++ },
                            onApply = { applied++ },
                        )
                    }
                }
            }
        }
    }

    @Test
    fun `a single choice names its section and option and reads as selected`() {
        show(appliedLabel = null)
        rule.onNodeWithText("Movies").assertIsSelected()
        rule.onNodeWithText("TV shows").assertIsNotSelected().performClick()
        assertEquals(listOf("kind" to "shows"), picks)
    }

    @Test
    fun `a multi choice and a row name themselves by id`() {
        show(appliedLabel = null)
        rule.onNodeWithText("Horror").performClick()
        rule.onNodeWithText("Release date").performClick()
        assertEquals(listOf("genres" to "horror"), picks)
        assertEquals(listOf("year"), rowsOpened)
    }

    @Test
    fun `clear all is inert with nothing applied and fires with something applied`() {
        show(appliedLabel = null)
        rule.onNodeWithText("Clear all").assertIsNotEnabled().performClick()
        assertEquals(0, cleared)
    }

    @Test
    fun `clear all fires once something is applied, and show results applies`() {
        show(appliedLabel = "2 applied")
        rule.onNodeWithText("Clear all").assertIsEnabled().performClick()
        rule.onNodeWithText("Show results").performClick()
        assertEquals(1, cleared)
        assertEquals(1, applied)
    }

    @Test
    fun `no text is clipped at twice the font size`() {
        show(appliedLabel = "2 applied", fontScale = 2f)
        rule.waitForIdle()
        rule.assertNoTextClipped()
    }
}
