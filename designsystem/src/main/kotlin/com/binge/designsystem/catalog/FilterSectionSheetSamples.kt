@file:OnePerScreen

package com.binge.designsystem.catalog

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.binge.designsystem.component.BingeFilterChip
import com.binge.designsystem.preview.ScreenshotTheme
import com.binge.designsystem.template.FilterControlHandlers
import com.binge.designsystem.template.FilterControlUi
import com.binge.designsystem.template.FilterDateBoundsUi
import com.binge.designsystem.template.FilterOptionUi
import com.binge.designsystem.template.FilterSectionContent
import com.binge.designsystem.template.FilterSectionUi
import java.time.LocalDate
import java.util.Locale

private const val RATING_MAX = 10f
private const val RATING_STEPS = 19
private const val GENRES_ID = "genres"
private const val RATING_ID = "rating"
private const val YEAR_ID = "year"

private val SampleToday: LocalDate = LocalDate.of(2026, 6, 15)

/** The date range filter in its own sheet: quick presets, then From and To fields that edit the same range. */
@Composable
fun FilterSectionDatesSample() {
    FilterSectionDatesFrame(spanish = false)
}

/** The same sheet in Spanish, where the labels and the fields' words are longer. */
@Composable
fun FilterSectionDatesSpanishSample() {
    FilterSectionDatesFrame(spanish = true)
}

@Composable
private fun FilterSectionDatesFrame(spanish: Boolean) {
    var from by remember { mutableStateOf<LocalDate?>(LocalDate.of(2020, 1, 1)) }
    var to by remember { mutableStateOf<LocalDate?>(null) }
    val presets = if (spanish) listOf("Cualquier año", "Este año", "2020s", "2010s") else listOf("Any year", "This year", "2020s", "2010s")
    var preset by remember { mutableStateOf<String?>(presets[2]) }
    val bounds =
        FilterDateBoundsUi(
            from = from,
            to = to,
            fromLabel = if (spanish) "Desde" else "From",
            toLabel = if (spanish) "Hasta" else "To",
            emptyLabel = if (spanish) "Cualquiera" else "Any",
            fromClearDescription = if (spanish) "Borrar la fecha inicial" else "Clear the start date",
            toClearDescription = if (spanish) "Borrar la fecha final" else "Clear the end date",
            today = SampleToday,
            valueLabel = { date, _ -> date.year.toString() },
        )
    ScreenshotTheme {
        Column {
            FilterSectionContent(
                section =
                    FilterSectionUi(
                        id = YEAR_ID,
                        title = if (spanish) "Fecha de estreno" else "Release date",
                        control =
                            FilterControlUi.DateRange(
                                presets = presets.map { FilterOptionUi(it, it) },
                                selectedPresetId = preset,
                                customTitle = if (spanish) "O elige un intervalo" else "Or choose a range",
                                bounds = bounds,
                            ),
                    ),
                clearLabel = if (spanish) "Borrar" else "Clear",
                clearEnabled = true,
                doneLabel = if (spanish) "Aplicar" else "Apply",
                onClear = {
                    from = null
                    to = null
                    preset = presets[0]
                },
                onDone = {},
                handlers =
                    FilterControlHandlers(
                        onSingleSelect = { _, id ->
                            preset = id
                            if (id == presets[0]) {
                                from = null
                                to = null
                            }
                        },
                        onDateBoundChange = { _, isEnd, date ->
                            preset = null
                            if (isEnd) to = date else from = date
                        },
                    ),
            )
        }
    }
}

/** A range filter in its own sheet: a hint saying what the top stop means, and a two-thumb slider. */
@Composable
fun FilterSectionRangeSample() {
    var values by remember { mutableStateOf(7f..RATING_MAX) }
    ScreenshotTheme {
        Column {
            FilterSectionContent(
                section =
                    FilterSectionUi(
                        id = RATING_ID,
                        title = "Rating",
                        control =
                            FilterControlUi.Range(
                                values = values,
                                valueRange = 0f..RATING_MAX,
                                steps = RATING_STEPS,
                                valueLabel = { String.format(Locale.ROOT, "%.1f", it) },
                                startThumbDescription = "Minimum rating",
                                endThumbDescription = "Maximum rating",
                            ),
                    ),
                clearLabel = "Clear",
                clearEnabled = values != 0f..RATING_MAX,
                doneLabel = "Apply",
                hint = "Drag either end. Titles with no rating are left out once a minimum is set.",
                onClear = { values = 0f..RATING_MAX },
                onDone = {},
                onBack = {},
                handlers = FilterControlHandlers(onRangeChange = { _, range -> values = range }),
            )
        }
    }
}

/** A multi-choice filter whose sheet has an accessory beside the button, here a match-all switch at the button's height. */
@Composable
fun FilterSectionAccessorySample() {
    var matchAll by remember { mutableStateOf(false) }
    var included by remember { mutableStateOf(setOf("Action", "Drama")) }
    val genres = listOf("Action", "Comedy", "Drama", "Horror", "Romance")
    ScreenshotTheme {
        Column {
            FilterSectionContent(
                section =
                    FilterSectionUi(
                        id = GENRES_ID,
                        title = "Genres",
                        control =
                            FilterControlUi.MultiChoice(
                                options = genres.map { FilterOptionUi(it, it) },
                                includedIds = included,
                                excludedIds = emptySet(),
                            ),
                    ),
                clearLabel = "Clear",
                clearEnabled = included.isNotEmpty(),
                doneLabel = "Apply",
                onClear = { included = emptySet() },
                onDone = {},
                handlers =
                    FilterControlHandlers(
                        onOptionClick = { _, id -> included = if (id in included) included - id else included + id },
                    ),
                footerAccessory = {
                    BingeFilterChip(label = "Match all", selected = matchAll, onClick = { matchAll = !matchAll })
                },
            )
        }
    }
}
