package com.binge.designsystem.component

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import com.binge.designsystem.R
import com.binge.designsystem.formatRelativeOrAbsolute
import com.binge.designsystem.theme.BingeSentiment
import com.binge.designsystem.theme.BingeShapes
import com.binge.designsystem.theme.accent
import com.binge.designsystem.uppercaseLocalised

private const val COLLAPSED_MESSAGE_LINES = 3

/** A log line's severity, lowest first. The row draws each with its own icon, name and tone, so it never rests on colour alone. */
enum class LogRowLevel { Debug, Info, Warn, Error }

/**
 * One log line for [LogRow]. [data] is an attached object already formatted as text, shown when the row is opened. The
 * time is [timestampMillis] when the source could parse it and [timestampRaw] otherwise.
 */
data class LogRowEntry(
    val id: String,
    val timestampMillis: Long?,
    val timestampRaw: String,
    val level: LogRowLevel,
    val label: String?,
    val message: String,
    val data: String?,
)

/**
 * One log line as a card: a stripe, an icon and the level's name all carry the severity. The message is held to a few
 * lines until the row is opened; a row with attached data or a clipped message shows a chevron, and a long press
 * copies the line either way ([onCopy] decides what goes on the clipboard).
 *
 * @param initiallyExpanded starts the row open; its open state is kept per [LogRowEntry.id] across recomposition and rotation.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LogRow(
    entry: LogRowEntry,
    onCopy: () -> Unit,
    modifier: Modifier = Modifier,
    initiallyExpanded: Boolean = false,
) {
    var expanded by rememberSaveable(entry.id) { mutableStateOf(initiallyExpanded) }
    var clipped by remember { mutableStateOf(false) }
    val expandable = entry.data != null || clipped || expanded
    val tone = entry.level.sentiment().accent()
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(BingeShapes.Medium)
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .height(IntrinsicSize.Min)
            .combinedClickable(
                onClickLabel = if (expandable) {
                    stringResource(if (expanded) R.string.log_row_collapse else R.string.log_row_expand)
                } else {
                    null
                },
                onClick = { if (expandable) expanded = !expanded },
                onLongClick = onCopy,
                onLongClickLabel = stringResource(R.string.log_row_copy),
            ),
    ) {
        Box(Modifier.width(dimensionResource(R.dimen.log_row_severity_stripe_width)).fillMaxHeight().background(tone))
        Column(
            modifier = Modifier.weight(1f).padding(dimensionResource(R.dimen.padding_m)),
            verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_s)),
        ) {
            LogRowHeader(entry, tone, expandable, expanded)
            Text(
                entry.message,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = if (expanded) Int.MAX_VALUE else COLLAPSED_MESSAGE_LINES,
                overflow = TextOverflow.Ellipsis,
                onTextLayout = { if (!expanded) clipped = it.hasVisualOverflow },
            )
            if (expanded) LogRowDetails(entry, onCopy)
        }
    }
}

@Composable
private fun LogRowHeader(
    entry: LogRowEntry,
    tone: Color,
    expandable: Boolean,
    expanded: Boolean,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_xs)),
    ) {
        Icon(
            entry.level.icon(),
            contentDescription = null,
            tint = tone,
            modifier = Modifier.size(dimensionResource(R.dimen.log_row_level_icon_size)),
        )
        Text(stringResource(entry.level.labelRes()).uppercaseLocalised(), style = MaterialTheme.typography.labelMedium, color = tone)
        // The time does not tick: an old line's age is not what a reader scans for, and a refresh re-reads the log.
        val meta = listOfNotNull(
            entry.label,
            formatRelativeOrAbsolute(entry.timestampMillis) ?: entry.timestampRaw.takeIf { it.isNotEmpty() },
        )
        if (meta.isNotEmpty()) {
            val separator = stringResource(R.string.log_row_meta_separator)
            Text(
                meta.joinToString(separator, prefix = separator),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
        } else {
            Spacer(Modifier.weight(1f))
        }
        if (expandable) {
            Icon(
                if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                contentDescription = stringResource(if (expanded) R.string.log_row_collapse else R.string.log_row_expand),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun ColumnScope.LogRowDetails(entry: LogRowEntry, onCopy: () -> Unit) {
    entry.data?.let { data ->
        Text(
            data,
            style = MaterialTheme.typography.bodySmall,
            fontFamily = FontFamily.Monospace,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .fillMaxWidth()
                .clip(BingeShapes.ElementSmall)
                .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                .padding(dimensionResource(R.dimen.padding_s)),
        )
    }
    TextButton(onClick = onCopy, modifier = Modifier.align(Alignment.End)) {
        Icon(
            Icons.Filled.ContentCopy,
            contentDescription = null,
            modifier = Modifier.size(dimensionResource(R.dimen.log_row_level_icon_size)),
        )
        Spacer(Modifier.width(dimensionResource(R.dimen.padding_xs)))
        Text(stringResource(R.string.log_row_copy))
    }
}

private fun LogRowLevel.icon(): ImageVector =
    when (this) {
        LogRowLevel.Debug -> Icons.Filled.BugReport
        LogRowLevel.Info -> Icons.Filled.Info
        LogRowLevel.Warn -> Icons.Filled.Warning
        LogRowLevel.Error -> Icons.Filled.Error
    }

private fun LogRowLevel.labelRes(): Int =
    when (this) {
        LogRowLevel.Debug -> R.string.log_level_debug
        LogRowLevel.Info -> R.string.log_level_info
        LogRowLevel.Warn -> R.string.log_level_warn
        LogRowLevel.Error -> R.string.log_level_error
    }

private fun LogRowLevel.sentiment(): BingeSentiment =
    when (this) {
        LogRowLevel.Debug -> BingeSentiment.Neutral
        LogRowLevel.Info -> BingeSentiment.Info
        LogRowLevel.Warn -> BingeSentiment.Caution
        LogRowLevel.Error -> BingeSentiment.Negative
    }
