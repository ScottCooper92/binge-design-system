package com.binge.designsystem.catalog

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.binge.designsystem.component.BingeDateBoundFields
import com.binge.designsystem.preview.ScreenshotTheme
import java.time.LocalDate

private val SAMPLE_TODAY: LocalDate = LocalDate.of(2025, 6, 15)
private val SAMPLE_FROM: LocalDate = LocalDate.of(2018, 3, 1)
private val SAMPLE_TO: LocalDate = LocalDate.of(2024, 9, 30)

private const val LONG_FROM_LABEL = "Released on or after this month"
private const val LONG_TO_LABEL = "Released on or before this month"

/**
 * Public samples for [BingeDateBoundFields]: the From and To pair, seeded with the dates each frame shows. The
 * fields keep their own state, so in the catalog app a field opens its picker, a pick sets it and the clear button
 * empties it. The fields bound each other, so From cannot be picked past To nor To before From.
 */
@Composable
fun DateBoundFieldsEmptySample() {
    DateBoundFieldsSample(from = null, to = null)
}

/** One bound set: the From field shows its month and year with a clear button, To is still empty. */
@Composable
fun DateBoundFieldsOneSetSample() {
    DateBoundFieldsSample(from = SAMPLE_FROM, to = null)
}

/** Both bounds set, each with its clear button. */
@Composable
fun DateBoundFieldsBothSetSample() {
    DateBoundFieldsSample(from = SAMPLE_FROM, to = SAMPLE_TO)
}

/** Long labels with both bounds set: the text wraps and the two fields grow together rather than clip. */
@Composable
fun DateBoundFieldsLongLabelsSample() {
    DateBoundFieldsSample(from = SAMPLE_FROM, to = SAMPLE_TO, fromLabel = LONG_FROM_LABEL, toLabel = LONG_TO_LABEL)
}

/** Long labels with nothing set, the other state where a label is the whole field. */
@Composable
fun DateBoundFieldsLongLabelsEmptySample() {
    DateBoundFieldsSample(from = null, to = null, fromLabel = LONG_FROM_LABEL, toLabel = LONG_TO_LABEL)
}

@Composable
private fun DateBoundFieldsSample(
    from: LocalDate?,
    to: LocalDate?,
    fromLabel: String = "From",
    toLabel: String = "To",
) {
    var currentFrom by remember { mutableStateOf(from) }
    var currentTo by remember { mutableStateOf(to) }
    ScreenshotTheme {
        // A Column so the surface's tight constraints do not stretch the pair to the frame's height.
        Column {
            BingeDateBoundFields(
                from = currentFrom,
                to = currentTo,
                fromLabel = fromLabel,
                toLabel = toLabel,
                fromClearDescription = "Clear $fromLabel",
                toClearDescription = "Clear $toLabel",
                onFromChange = { currentFrom = it },
                onToChange = { currentTo = it },
                fromMaximum = currentTo,
                toMinimum = currentFrom,
                today = SAMPLE_TODAY,
            )
        }
    }
}
