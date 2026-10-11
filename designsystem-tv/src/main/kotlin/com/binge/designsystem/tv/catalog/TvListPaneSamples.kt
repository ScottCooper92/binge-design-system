@file:CatalogGroup("Page templates")

package com.binge.designsystem.tv.catalog

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Hd
import androidx.compose.material.icons.filled.HighQuality
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.SentimentSatisfied
import androidx.compose.material.icons.filled.Subtitles
import androidx.compose.material.icons.filled.Theaters
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.binge.designsystem.catalog.CatalogGroup
import com.binge.designsystem.tv.template.TvListPaneBoard
import com.binge.designsystem.tv.template.TvListPaneBoardSkeleton
import com.binge.designsystem.tv.template.TvPageHosting
import com.binge.designsystem.tv.template.TvPaneAction
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
private const val KEY_GENRE = "genre"
private const val KEY_LANGUAGE = "language"
private const val GENRE_DRAMA = "Drama"
private const val GENRE_COMEDY = "Comedy"
private const val GENRE_SCIENCE_FICTION = "Science fiction"
private const val GENRE_THRILLER = "Thriller"
private const val LANGUAGE_ENGLISH = "English"
private const val LANGUAGE_SPANISH = "Spanish"
private const val LANGUAGE_FRENCH = "French"
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

/**
 * A filter panel on the same board: filters in the list, each showing what it has applied under its name, and the values of
 * the described filter beside it as checkboxes with no description above them. A clear button sits under the
 * values and another under the list.
 */
@Composable
fun TvListPaneFilterBoardSample() {
    SampleFilterBoard(initialListHasFocus = true, initialFocusedOptionLabel = null)
}

/** The filter panel with focus on a value that is off: the focused checkbox takes the fill, the ticked ones keep their tick. */
@Composable
fun TvListPaneFilterBoardPaneFocusedSample() {
    SampleFilterBoard(initialListHasFocus = false, initialFocusedOptionLabel = GENRE_COMEDY)
}

@Composable
private fun SampleFilterBoard(initialListHasFocus: Boolean, initialFocusedOptionLabel: String?) {
    var focusedKey by remember { mutableStateOf(KEY_GENRE) }
    var genres by remember { mutableStateOf(setOf(GENRE_DRAMA, GENRE_SCIENCE_FICTION)) }
    var languages by remember { mutableStateOf(emptySet<String>()) }

    fun toggle(current: Set<String>, value: String) = if (value in current) current - value else current + value

    fun detail(selected: Set<String>, any: String) = if (selected.isEmpty()) any else "${selected.size} selected"
    val genreOptions =
        listOf(
            GENRE_DRAMA to Icons.Filled.Theaters,
            GENRE_COMEDY to Icons.Filled.SentimentSatisfied,
            GENRE_SCIENCE_FICTION to Icons.Filled.RocketLaunch,
            GENRE_THRILLER to Icons.Filled.Bolt,
        )
    val languageOptions = listOf(LANGUAGE_ENGLISH, LANGUAGE_SPANISH, LANGUAGE_FRENCH)
    TvListPaneBoard(
        title = "Filters",
        groups =
            listOf(
                TvPaneGroup(
                    title = "Refine",
                    rows =
                        listOf(
                            TvPaneRow(
                                key = KEY_GENRE,
                                label = "Genre",
                                detail = detail(genres, "Any"),
                                multiSelect = true,
                                options =
                                    genreOptions.map { (genre, genreIcon) ->
                                        TvPaneOption(
                                            label = genre,
                                            selected = genre in genres,
                                            onSelect = { genres = toggle(genres, genre) },
                                            icon = genreIcon,
                                        )
                                    },
                                clear = TvPaneAction(
                                    label = "Clear genre",
                                    onClick = { genres = emptySet() },
                                    enabled = genres.isNotEmpty(),
                                ),
                            ),
                            TvPaneRow(
                                key = KEY_LANGUAGE,
                                label = "Language",
                                detail = detail(languages, "Any"),
                                multiSelect = true,
                                options = languageOptions.map {
                                    TvPaneOption(it, selected = it in languages, onSelect = {
                                        languages =
                                            toggle(languages, it)
                                    })
                                },
                                clear = TvPaneAction(
                                    label = "Clear language",
                                    onClick = { languages = emptySet() },
                                    enabled = languages.isNotEmpty(),
                                ),
                            ),
                        ),
                ),
            ),
        focusedKey = focusedKey,
        onFocusRow = { focusedKey = it },
        hosting = TvPageHosting.Overlay,
        initialListHasFocus = initialListHasFocus,
        initialFocusedOptionLabel = initialFocusedOptionLabel,
        clearAll = TvPaneAction(label = "Clear all", onClick = {
            genres = emptySet()
            languages = emptySet()
        }),
    )
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
                            detail = quality,
                            options =
                                listOf(QUALITY_HD to Icons.Filled.Hd, QUALITY_UHD to Icons.Filled.HighQuality).map { (option, glyph) ->
                                    TvPaneOption(
                                        label = option,
                                        selected = quality == option,
                                        onSelect = { quality = option },
                                        icon = glyph,
                                    )
                                },
                        ),
                        TvPaneRow(
                            key = KEY_SUBTITLES,
                            label = "Subtitles",
                            body = "Whether new titles start with subtitles on.",
                            icon = Icons.Filled.Subtitles,
                            detail = if (subtitles) "On" else "Off",
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
