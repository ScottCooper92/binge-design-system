package com.binge.designsystem.template

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.onNodeWithContentDescription
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
import java.time.LocalDate

/** One filter's own sheet: its Clear and Apply, its hint and accessory, a preset and a date bound named by id, and no clipping at 2x. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class, qualifiers = "w360dp-h2400dp")
class FilterSectionContentTest {
    @get:Rule
    val rule = createTakeDownComposeRule()

    private val picks = mutableListOf<Pair<String, String>>()
    private val bounds = mutableListOf<Triple<String, Boolean, LocalDate?>>()
    private var cleared = 0
    private var done = 0

    private val section =
        FilterSectionUi(
            id = "year",
            title = "Release date",
            control =
                FilterControlUi.DateRange(
                    presets = listOf(FilterOptionUi("any", "Any year"), FilterOptionUi("2020s", "2020s")),
                    selectedPresetId = "any",
                    customTitle = "Or choose a range",
                    bounds =
                        FilterDateBoundsUi(
                            from = LocalDate.of(2020, 1, 1),
                            to = null,
                            fromLabel = "From",
                            toLabel = "To",
                            emptyLabel = "Any",
                            fromClearDescription = "Clear the start",
                            toClearDescription = "Clear the end",
                            today = LocalDate.of(2026, 6, 15),
                            valueLabel = { date, _ -> date.year.toString() },
                        ),
                ),
        )

    private fun show(clearEnabled: Boolean = true, fontScale: Float = 1f) {
        rule.setContent {
            TestTheme {
                val density = LocalDensity.current
                CompositionLocalProvider(LocalDensity provides Density(density.density, fontScale)) {
                    Column(Modifier.height(2000.dp)) {
                        FilterSectionContent(
                            section = section,
                            clearLabel = "Clear",
                            clearEnabled = clearEnabled,
                            doneLabel = "Apply",
                            hint = "Pick a preset or set the bounds.",
                            onClear = { cleared++ },
                            onDone = { done++ },
                            handlers =
                                FilterControlHandlers(
                                    onSingleSelect = { s, o -> picks += s to o },
                                    onDateBoundChange = { s, end, d -> bounds += Triple(s, end, d) },
                                ),
                            footerAccessory = { Text("Accessory") },
                        )
                    }
                }
            }
        }
    }

    @Test
    fun `a preset and a cleared bound name their section by id`() {
        show()
        rule.onNodeWithText("2020s").performClick()
        rule.onNodeWithContentDescription("Clear the start").performClick()
        assertEquals(listOf("year" to "2020s"), picks)
        assertEquals(listOf(Triple("year", false, null)), bounds)
    }

    @Test
    fun `the hint and the accessory are shown, and apply closes`() {
        show()
        rule.onNodeWithText("Pick a preset or set the bounds.").assertIsDisplayed()
        rule.onNodeWithText("Accessory").assertIsDisplayed()
        rule.onNodeWithText("Apply").performClick()
        assertEquals(1, done)
    }

    @Test
    fun `clear is inert when there is nothing to clear`() {
        show(clearEnabled = false)
        rule.onNodeWithText("Clear").assertIsNotEnabled().performClick()
        assertEquals(0, cleared)
    }

    @Test
    fun `no text is clipped at twice the font size`() {
        show(fontScale = 2f)
        rule.waitForIdle()
        rule.assertNoTextClipped()
    }
}
