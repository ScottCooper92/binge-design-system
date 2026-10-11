@file:CatalogGroup("Page templates")

package com.binge.designsystem.tv.catalog

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.HighQuality
import androidx.compose.material.icons.filled.Subtitles
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.binge.designsystem.catalog.CatalogGroup
import com.binge.designsystem.tv.template.TvListPaneBoard
import com.binge.designsystem.tv.template.TvListPaneBoardSkeleton
import com.binge.designsystem.tv.template.TvPageHosting
import com.binge.designsystem.tv.template.TvPaneGroup
import com.binge.designsystem.tv.template.TvPaneOption
import com.binge.designsystem.tv.template.TvPaneRow

/*
 * The list-and-pane board at the size it ships: a whole panel. Focus is seeded through the board's initial-focus
 * parameters, so the lit states are captured without a focus request.
 */

private const val KEY_QUALITY = "quality"
private const val KEY_SUBTITLES = "subtitles"
private const val KEY_SERVER = "server"
private const val QUALITY_HD = "1080p"
private const val QUALITY_UHD = "4K"

/** Settings as a list-and-pane board, focus on the described row: the full fill in the list, its options beside it. */
@Composable
fun TvListPaneBoardSample() {
    SampleBoard(initialListHasFocus = true, initialFocusedOptionLabel = null)
}

/**
 * The same board with focus moved into the pane, on the option not chosen: the described row drops to the dim
 * accent, the focused option takes the fill, and the chosen one keeps its tick.
 */
@Composable
fun TvListPaneBoardPaneFocusedSample() {
    SampleBoard(initialListHasFocus = false, initialFocusedOptionLabel = QUALITY_UHD)
}

/** The board while it loads: the title, a column of row placeholders and the pane, where the board puts them. */
@Composable
fun TvListPaneBoardSkeletonSample() {
    TvListPaneBoardSkeleton(title = "Settings", hosting = TvPageHosting.Overlay)
}

@Composable
private fun SampleBoard(initialListHasFocus: Boolean, initialFocusedOptionLabel: String?) {
    var focusedKey by remember { mutableStateOf(KEY_QUALITY) }
    var quality by remember { mutableStateOf(QUALITY_HD) }
    var subtitles by remember { mutableStateOf(true) }
    val groups =
        listOf(
            TvPaneGroup(
                title = "Playback",
                rows =
                    listOf(
                        TvPaneRow(
                            key = KEY_QUALITY,
                            label = "Default quality",
                            body = "The quality a new request asks for.",
                            note = "You can still pick another for each request.",
                            icon = Icons.Filled.HighQuality,
                            options =
                                listOf(QUALITY_HD, QUALITY_UHD).map { option ->
                                    TvPaneOption(label = option, selected = quality == option, onSelect = { quality = option })
                                },
                        ),
                        TvPaneRow(
                            key = KEY_SUBTITLES,
                            label = "Subtitles",
                            body = "Whether new titles start with subtitles on.",
                            icon = Icons.Filled.Subtitles,
                            options =
                                listOf(
                                    TvPaneOption(label = "On", selected = subtitles, onSelect = { subtitles = true }),
                                    TvPaneOption(label = "Off", selected = !subtitles, onSelect = { subtitles = false }),
                                ),
                        ),
                    ),
            ),
            TvPaneGroup(
                title = "Connection",
                rows =
                    listOf(
                        TvPaneRow(
                            key = KEY_SERVER,
                            label = "Server",
                            body = "media.example.com",
                            note = "Change the server from your phone.",
                        ),
                    ),
            ),
        )
    TvListPaneBoard(
        title = "Settings",
        groups = groups,
        focusedKey = focusedKey,
        onFocusRow = { focusedKey = it },
        hosting = TvPageHosting.Overlay,
        initialListHasFocus = initialListHasFocus,
        initialFocusedOptionLabel = initialFocusedOptionLabel,
    )
}
