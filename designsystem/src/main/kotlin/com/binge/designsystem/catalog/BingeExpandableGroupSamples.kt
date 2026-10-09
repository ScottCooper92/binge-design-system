package com.binge.designsystem.catalog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.HighQuality
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Tune
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.dimensionResource
import com.binge.designsystem.R
import com.binge.designsystem.component.BingeExpandableGroup
import com.binge.designsystem.component.BingeGroupContent
import com.binge.designsystem.component.ListItem
import com.binge.designsystem.component.bingeExpandableItem
import com.binge.designsystem.preview.ScreenshotTheme
import kotlinx.coroutines.delay

private const val ADVANCED = "Advanced options"
private const val ADVANCED_HINT = "Server, quality and folder"
private const val LOAD_MILLIS = 1_500L

private val DestinationRows =
    listOf(
        ListItem(icon = Icons.Filled.Dns, label = "Server", detail = "Home"),
        ListItem(icon = Icons.Filled.HighQuality, label = "Quality profile", detail = "HD-1080p"),
        ListItem(icon = Icons.Filled.Folder, label = "Root folder", detail = "/media/movies"),
    )

private fun qualityRow(toggled: Boolean, onToggle: () -> Unit) =
    ListItem(icon = Icons.Filled.Movie, label = "Request in 4K", toggled = toggled, onClick = onToggle)

/** Shut: the header's chevron points down and its detail hints at what opening it offers. */
@Composable
fun BingeExpandableGroupClosedSample() {
    ScreenshotTheme {
        var expanded by remember { mutableStateOf(false) }
        var fourK by remember { mutableStateOf(false) }
        BingeExpandableGroup(
            title = "Request",
            header = bingeExpandableItem(
                Icons.Filled.Tune,
                ADVANCED,
                expanded = expanded,
                onExpandedChange = { expanded = it },
                collapsedDetail = ADVANCED_HINT,
            ),
            content = BingeGroupContent.Ready(DestinationRows),
            rowsAbove = listOf(qualityRow(fourK) { fourK = !fourK }),
        )
    }
}

/** Open with its rows read: they hang from the header as its children, each a row that opens its own picker. */
@Composable
fun BingeExpandableGroupOpenSample() {
    ScreenshotTheme {
        var expanded by remember { mutableStateOf(true) }
        var fourK by remember { mutableStateOf(false) }
        BingeExpandableGroup(
            title = "Request",
            header = bingeExpandableItem(
                Icons.Filled.Tune,
                ADVANCED,
                expanded = expanded,
                onExpandedChange = { expanded = it },
                collapsedDetail = ADVANCED_HINT,
            ),
            content = BingeGroupContent.Ready(DestinationRows),
            rowsAbove = listOf(qualityRow(fourK) { fourK = !fourK }),
        )
    }
}

/** Open while its rows are read, then with the read failed and its retry. */
@Composable
fun BingeExpandableGroupStatesSample() {
    ScreenshotTheme {
        var loadingOpen by rememberSaveable { mutableStateOf(true) }
        var failedOpen by rememberSaveable { mutableStateOf(true) }
        Column(verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_m))) {
            BingeExpandableGroup(
                title = null,
                header = bingeExpandableItem(Icons.Filled.Tune, ADVANCED, expanded = loadingOpen, onExpandedChange = { loadingOpen = it }),
                content = BingeGroupContent.Loading,
            )
            BingeExpandableGroup(
                title = null,
                header = bingeExpandableItem(Icons.Filled.Tune, ADVANCED, expanded = failedOpen, onExpandedChange = { failedOpen = it }),
                content = BingeGroupContent.Failed("Couldn't load the server's options.", "Try again") {},
            )
        }
    }
}

/**
 * The group reading its rows when it opens. The first read fails, to show the retry; the second arrives. Shutting the
 * group and opening it again reads afresh.
 */
@Composable
fun BingeExpandableGroupDemo() {
    ScreenshotTheme {
        var expanded by remember { mutableStateOf(false) }
        var fourK by remember { mutableStateOf(false) }
        var reads by remember { mutableIntStateOf(0) }
        var content by remember { mutableStateOf<BingeGroupContent>(BingeGroupContent.Loading) }
        LaunchedEffect(expanded, reads) {
            if (!expanded) return@LaunchedEffect
            content = BingeGroupContent.Loading
            delay(LOAD_MILLIS)
            content =
                if (reads == 0) {
                    BingeGroupContent.Failed("Couldn't load the server's options.", "Try again") { reads++ }
                } else {
                    BingeGroupContent.Ready(DestinationRows)
                }
        }
        BingeExpandableGroup(
            title = "Request",
            header =
                bingeExpandableItem(
                    icon = Icons.Filled.Tune,
                    title = ADVANCED,
                    expanded = expanded,
                    onExpandedChange = { expanded = it },
                    collapsedDetail = ADVANCED_HINT,
                ),
            content = content,
            rowsAbove = listOf(qualityRow(fourK) { fourK = !fourK }),
        )
    }
}
