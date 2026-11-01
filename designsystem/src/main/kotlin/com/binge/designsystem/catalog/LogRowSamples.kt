package com.binge.designsystem.catalog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import com.binge.designsystem.R
import com.binge.designsystem.component.LogRow
import com.binge.designsystem.component.LogRowEntry
import com.binge.designsystem.component.LogRowLevel
import com.binge.designsystem.preview.ScreenshotTheme

private const val SAMPLE_DATA = "{\n  \"errorMessage\": \"connect ECONNREFUSED 10.0.0.12:8989\",\n  \"status\": 503\n}"
private val SAMPLE_LONG_MESSAGE = "Request failed after retrying: " + "the instance did not answer within the timeout. ".repeat(6)

/** The time is left unparsed so a frame shows the source's own timestamp rather than a date that moves with the clock. */
private fun sampleEntry(
    level: LogRowLevel,
    label: String? = "Sonarr",
    message: String = "Unable to reach the instance",
    data: String? = null,
) = LogRowEntry(
    id = "${level.name}-$message",
    timestampMillis = null,
    timestampRaw = "2026-09-29T21:14:07.000Z",
    level = level,
    label = label,
    message = message,
    data = data,
)

@Composable
private fun LogRowColumn(content: @Composable () -> Unit) {
    ScreenshotTheme {
        Column(
            modifier = Modifier.padding(dimensionResource(R.dimen.padding_m)),
            verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.list_row_spacing)),
        ) { content() }
    }
}

/** The four levels in their tones. A line with no attached data and a short message has nothing to open. */
@Composable
fun LogRowLevelsSample() {
    LogRowColumn {
        LogRowLevel.entries.forEach { level ->
            LogRow(sampleEntry(level, message = "Sample ${level.name.lowercase()} line from the server"), onCopy = {})
        }
    }
}

/** A line carrying data, closed: it shows the chevron that opens it. */
@Composable
fun LogRowWithDataSample() {
    LogRowColumn { LogRow(sampleEntry(LogRowLevel.Warn, data = SAMPLE_DATA), onCopy = {}) }
}

/** The same line opened to what it carried, with its copy button. */
@Composable
fun LogRowOpenedSample() {
    LogRowColumn { LogRow(sampleEntry(LogRowLevel.Error, data = SAMPLE_DATA), onCopy = {}, initiallyExpanded = true) }
}

/** A long message is held to three lines. The chevron that opens it appears after the first layout, which a static frame never reaches. */
@Composable
fun LogRowLongMessageSample() {
    LogRowColumn { LogRow(sampleEntry(LogRowLevel.Error, message = SAMPLE_LONG_MESSAGE), onCopy = {}) }
}

/** A line with no label and a time that did not parse shows the source's own timestamp. */
@Composable
fun LogRowUnlabelledSample() {
    LogRowColumn { LogRow(sampleEntry(LogRowLevel.Info, label = null, message = "Server started"), onCopy = {}) }
}
