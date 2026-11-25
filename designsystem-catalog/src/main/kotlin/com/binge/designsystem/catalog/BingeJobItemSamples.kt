@file:CatalogGroup("Settings")
@file:SelfDescribing

package com.binge.designsystem.catalog

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.binge.designsystem.component.ItemGroup
import com.binge.designsystem.component.JobCopy
import com.binge.designsystem.component.JobOutcome
import com.binge.designsystem.component.bingeJobItem
import com.binge.designsystem.preview.ScreenshotTheme

private val SampleCopy = JobCopy(run = "Run now", stop = "Stop", running = "Running", succeeded = "Finished", failed = "Failed")
private const val PERCENT_DONE = 42

/**
 * [bingeJobItem] in a group, one row for each thing a job can be doing while it has not just finished: waiting for its next run, running
 * with nothing to report, running with a percentage, busy with a tap in flight, and a job that cannot be stopped, which spins in its Run
 * button while it runs. Tap Run and it runs; tap Stop and it stops. The words are the sample's own.
 */
@Composable
fun BingeJobItemSample() {
    var scanRunning by remember { mutableStateOf(false) }
    ScreenshotTheme {
        ItemGroup(
            title = "Jobs",
            rows =
                listOf(
                    bingeJobItem(
                        icon = Icons.Filled.VideoLibrary,
                        label = "Library scan",
                        idleDetail = "Next run in 2 hours",
                        copy = SampleCopy,
                        running = scanRunning,
                        onRun = { scanRunning = true },
                        onStop = { scanRunning = false },
                    ),
                    bingeJobItem(
                        icon = Icons.Filled.CloudDownload,
                        label = "Download metadata",
                        idleDetail = "Next run in 6 hours",
                        copy = SampleCopy,
                        running = true,
                        onRun = {},
                        onStop = {},
                        runningDetail = "Running $PERCENT_DONE%",
                    ),
                    bingeJobItem(
                        Icons.Filled.Image,
                        "Cache images",
                        "Next run in a week",
                        SampleCopy,
                        running = false,
                        onRun = {},
                        busy = true,
                    ),
                    bingeJobItem(
                        Icons.Filled.Sync,
                        "Rebuild index",
                        "Next run never",
                        SampleCopy,
                        running = true,
                        onRun = {},
                        onStop = null,
                    ),
                ),
        )
    }
}

/**
 * [bingeJobItem] for how a run ended, and for the job open beside the list: finished, with its check; failed, with its error glyph and
 * the error colour; and a selected row. The words are the sample's own.
 */
@Composable
fun BingeJobItemOutcomesSample() {
    ScreenshotTheme {
        ItemGroup(
            title = "Jobs",
            rows =
                listOf(
                    bingeJobItem(
                        Icons.Filled.Movie,
                        "Scan movies",
                        "Next run in 3 hours",
                        SampleCopy,
                        running = false,
                        onRun = {},
                        outcome = JobOutcome.Succeeded,
                    ),
                    bingeJobItem(
                        Icons.Filled.Schedule,
                        "Clean up",
                        "Next run in a day",
                        SampleCopy,
                        running = false,
                        onRun = {},
                        outcome = JobOutcome.Failed,
                    ),
                    bingeJobItem(
                        icon = Icons.Filled.VideoLibrary,
                        label = "Open job",
                        idleDetail = "Next run in 2 hours",
                        copy = SampleCopy,
                        running = false,
                        onRun = {},
                        onOpen = {},
                        selected = true,
                    ),
                ),
        )
    }
}
