package com.binge.designsystem.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.dimensionResource
import com.binge.designsystem.R
import com.binge.designsystem.theme.BingeSentiment
import com.binge.designsystem.theme.accent
import com.binge.designsystem.theme.fill

/** How a job's last run ended, shown for a moment in place of its run and stop buttons. */
enum class JobOutcome { Succeeded, Failed }

/**
 * The words a job row uses, one set for every job so they read alike: [run] and [stop] name the buttons for a screen reader, [running]
 * is the detail while a job runs with no progress to report, and [succeeded] and [failed] say how the last run ended, in the detail and
 * on the outcome glyph.
 */
@Immutable
class JobCopy(
    val run: String,
    val stop: String,
    val running: String,
    val succeeded: String,
    val failed: String,
)

/**
 * A job as a list item for an [ItemGroup]: run it, or stop it while it runs, in place at the row's end. The detail says what the job is
 * doing: [idleDetail] at rest (when it next runs, say), [runningDetail] while it runs where the job reports progress ("Running 42%"),
 * else [JobCopy.running], and for a moment after a run ends its [outcome]: a check, or an error glyph in the error colour, with the
 * row's icon tinted to match. The glyph sits in the same box as the buttons, so it stays put from play to stop to check.
 *
 * A job that cannot be stopped ([onStop] null) spins in its Run button while it runs. [busy] shows the spinner while a tap is in
 * flight. [onOpen] makes the row tappable, for a job with a page or a schedule of its own, and [selected] marks the job open beside
 * the list. Which icon a job gets is the caller's: it is a domain choice.
 */
@Composable
fun bingeJobItem(
    icon: ImageVector,
    label: String,
    idleDetail: String,
    copy: JobCopy,
    running: Boolean,
    onRun: () -> Unit,
    onStop: (() -> Unit)? = null,
    runningDetail: String? = null,
    busy: Boolean = false,
    outcome: JobOutcome? = null,
    onOpen: (() -> Unit)? = null,
    selected: Boolean = false,
): ListItem =
    ListItem(
        icon = icon,
        iconTint =
            when (outcome) {
                JobOutcome.Succeeded -> BingeSentiment.Positive.fill()
                JobOutcome.Failed -> BingeSentiment.Negative.fill()
                null -> null
            },
        label = label,
        detail =
            when {
                outcome == JobOutcome.Succeeded -> copy.succeeded
                outcome == JobOutcome.Failed -> copy.failed
                running -> runningDetail ?: copy.running
                else -> idleDetail
            },
        detailColor = if (outcome == JobOutcome.Failed) BingeSentiment.Negative.accent() else null,
        selected = selected,
        clickable = onOpen != null,
        onClick = onOpen ?: {},
        trailingContent = { JobAction(copy, running, onRun, onStop, busy, outcome) },
    )

/** Run, Stop, or the outcome, in one box at the row's end. */
@Composable
private fun JobAction(
    copy: JobCopy,
    running: Boolean,
    onRun: () -> Unit,
    onStop: (() -> Unit)?,
    busy: Boolean,
    outcome: JobOutcome?,
) {
    Box(modifier = Modifier.size(dimensionResource(R.dimen.button_tonal_size)), contentAlignment = Alignment.Center) {
        when {
            outcome != null -> {
                val ok = outcome == JobOutcome.Succeeded
                Icon(
                    imageVector = if (ok) Icons.Filled.Check else Icons.Filled.Error,
                    contentDescription = if (ok) copy.succeeded else copy.failed,
                    tint = if (ok) LocalContentColor.current else BingeSentiment.Negative.fill(),
                )
            }
            running && onStop != null ->
                ExpressiveIconButton(onClick = onStop, icon = Icons.Filled.Stop, contentDescription = copy.stop, loading = busy)
            else ->
                ExpressiveIconButton(
                    onClick = onRun,
                    icon = Icons.Filled.PlayArrow,
                    contentDescription = copy.run,
                    loading = busy || running,
                )
        }
    }
}
