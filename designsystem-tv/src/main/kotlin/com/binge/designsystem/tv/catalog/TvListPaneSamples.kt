@file:CatalogGroup("Page templates")

package com.binge.designsystem.tv.catalog

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Hd
import androidx.compose.material.icons.filled.HighQuality
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.SentimentSatisfied
import androidx.compose.material.icons.filled.Subtitles
import androidx.compose.material.icons.filled.Theaters
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import com.binge.designsystem.catalog.CatalogGroup
import com.binge.designsystem.theme.BingeShapes
import com.binge.designsystem.tv.template.TvListPaneBoard
import com.binge.designsystem.tv.template.TvListPaneBoardSkeleton
import com.binge.designsystem.tv.template.TvPageHosting
import com.binge.designsystem.tv.template.TvPaneAction
import com.binge.designsystem.tv.template.TvPaneGroup
import com.binge.designsystem.tv.template.TvPaneOption
import com.binge.designsystem.tv.template.TvPaneRow
import com.binge.designsystem.tv.R as TvR

/*
 * The list-and-pane board at the size it ships: a whole panel. Focus is seeded through the board's initial-focus
 * parameters, so the lit states are captured without a focus request.
 */

private const val KEY_QUALITY = "quality"
private const val KEY_SUBTITLES = "subtitles"
private const val KEY_SERVER = "server"
private const val KEY_GENRE = "genre"
private const val KEY_LANGUAGE = "language"
private const val KEY_SERVICE = "service"
private const val SERVICE_STREAMLY = "Streamly"
private const val SERVICE_FLIXORA = "Flixora"
private const val SERVICE_CINEBOX = "Cinebox"
private const val SERVICE_COLOUR_A = 0xFFE50914
private const val SERVICE_COLOUR_B = 0xFF1CE783
private const val SERVICE_COLOUR_C = 0xFF5B8DEF
private const val GENRE_DRAMA = "Drama"
private const val GENRE_COMEDY = "Comedy"
private const val GENRE_SCIENCE_FICTION = "Science fiction"
private const val GENRE_THRILLER = "Thriller"
private const val LANGUAGE_ENGLISH = "English"
private const val LANGUAGE_SPANISH = "Spanish"
private const val LANGUAGE_FRENCH = "French"
private const val QUALITY_HD = "1080p"
private const val QUALITY_UHD = "4K"
private const val STATUS_LOADING_ROW = 1
private const val STATUS_ALERT_ROW = 2
private const val STATUS_COUNT_ROW = 3
private const val STATUS_COUNT = 3

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

/**
 * Rows with their state at the end of the line: one whose value is loading, one that needs attention and one with
 * something waiting. Each leads with its icon, and each label gives way to the mark rather than wrapping.
 */
@Composable
fun TvListPaneBoardRowStatusSample() {
    StatusBoard(
        title = "Settings",
        groupTitle = "Preferences",
        labels = listOf("Sound effects", "Share analytics data", "Send crash reports", "Companion apps"),
    )
}

/**
 * The same rows with the longest labels a settings board ships: two Spanish and two pseudo-accented, at about the
 * length the en-XA pseudolocale gives them. They show where a label ellipsizes against its icon and its mark.
 */
@Composable
fun TvListPaneBoardRowStatusLongLabelsSample() {
    StatusBoard(
        title = "Ajustes",
        groupTitle = "Preferencias",
        labels =
            listOf(
                "Política de privacidad",
                "Compartir datos de análisis",
                "[Šéñð çŕåšĥ ŕéþöŕţš one two]",
                "[Çömþåñîöñ åþþš one two three]",
            ),
    )
}

/** The board while it loads: the title, a column of row placeholders and the pane, where the board puts them. */
@Composable
fun TvListPaneBoardSkeletonSample() {
    TvListPaneBoardSkeleton(title = "Settings", hosting = TvPageHosting.Overlay)
}

/** The skeleton for a board whose rows show a value under their name: each placeholder is two lines, as the rows will be. */
@Composable
fun TvListPaneBoardSkeletonRowDetailSample() {
    TvListPaneBoardSkeleton(title = "Settings", hosting = TvPageHosting.Overlay, rowDetail = true)
}

/** The skeleton for a board whose first group carries a note: a line is reserved under the title, so the rows do not move. */
@Composable
fun TvListPaneBoardSkeletonGroupNoteSample() {
    TvListPaneBoardSkeleton(title = "Settings", hosting = TvPageHosting.Overlay, groupNote = true)
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
private fun StatusBoard(
    title: String,
    groupTitle: String,
    labels: List<String>,
) {
    var focusedKey by remember { mutableStateOf(labels.first()) }
    val icons = listOf(Icons.Filled.VolumeUp, Icons.Filled.Analytics, Icons.Filled.BugReport, Icons.Filled.Apps)
    val rows =
        labels.zip(icons).mapIndexed { index, (label, rowIcon) ->
            TvPaneRow(
                key = label,
                label = label,
                body = "What this setting changes.",
                icon = rowIcon,
                loading = index == STATUS_LOADING_ROW,
                badgeAlert = index == STATUS_ALERT_ROW,
                badgeCount = if (index == STATUS_COUNT_ROW) STATUS_COUNT else null,
            )
        }
    TvListPaneBoard(
        title = title,
        groups = listOf(TvPaneGroup(title = groupTitle, rows = rows)),
        focusedKey = focusedKey,
        onFocusRow = { focusedKey = it },
        hosting = TvPageHosting.Overlay,
        initialListHasFocus = true,
    )
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

/**
 * A filter whose values can be ruled out as well as chosen: OK cycles a genre from off to ticked to excluded and back,
 * and the box carries the excluded mark where the tick would be. Focus rests on a value, so the fill and the three
 * states sit side by side.
 */
@Composable
fun TvListPaneFilterBoardTriStateSample() {
    SampleTriStateBoard(describedKey = KEY_GENRE, initialFocusedOptionLabel = GENRE_COMEDY)
}

/**
 * A filter led by a pinned option, a shortcut that sits above the values behind a divider, over values that lead
 * with a mark of their own, such as a service's logo.
 */
@Composable
fun TvListPaneFilterBoardPinnedSample() {
    SampleTriStateBoard(describedKey = KEY_SERVICE, initialFocusedOptionLabel = null)
}

@Composable
private fun SampleTriStateBoard(describedKey: String, initialFocusedOptionLabel: String?) {
    var focusedKey by remember { mutableStateOf(describedKey) }
    var genreStates by remember {
        mutableStateOf(mapOf(GENRE_DRAMA to FilterState.Included, GENRE_THRILLER to FilterState.Excluded))
    }
    var services by remember { mutableStateOf(setOf(SERVICE_STREAMLY)) }

    fun cycle(state: FilterState?) =
        when (state) {
            null -> FilterState.Included
            FilterState.Included -> FilterState.Excluded
            FilterState.Excluded -> null
        }
    val serviceMarks =
        listOf(
            SERVICE_STREAMLY to Color(SERVICE_COLOUR_A),
            SERVICE_FLIXORA to Color(SERVICE_COLOUR_B),
            SERVICE_CINEBOX to Color(SERVICE_COLOUR_C),
        )
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
                                detail = if (genreStates.isEmpty()) "Any" else "${genreStates.size} set",
                                multiSelect = true,
                                options =
                                    listOf(GENRE_DRAMA, GENRE_COMEDY, GENRE_SCIENCE_FICTION, GENRE_THRILLER).map { genre ->
                                        TvPaneOption(
                                            label = genre,
                                            selected = genreStates[genre] == FilterState.Included,
                                            excluded = genreStates[genre] == FilterState.Excluded,
                                            onSelect = {
                                                val next = cycle(genreStates[genre])
                                                genreStates = if (next == null) genreStates - genre else genreStates + (genre to next)
                                            },
                                        )
                                    },
                                clear = TvPaneAction("Clear genre", { genreStates = emptyMap() }, enabled = genreStates.isNotEmpty()),
                            ),
                            TvPaneRow(
                                key = KEY_SERVICE,
                                label = "Streaming service",
                                detail = if (services.isEmpty()) "Any" else "${services.size} selected",
                                multiSelect = true,
                                pinnedOptions =
                                    listOf(
                                        TvPaneOption(
                                            label = "My services",
                                            selected = services == setOf(SERVICE_STREAMLY),
                                            onSelect = { services = setOf(SERVICE_STREAMLY) },
                                            icon = Icons.Filled.Apps,
                                        ),
                                    ),
                                options =
                                    serviceMarks.map { (service, colour) ->
                                        TvPaneOption(
                                            label = service,
                                            selected = service in services,
                                            onSelect = { services = if (service in services) services - service else services + service },
                                            leading = { SampleServiceMark(colour) },
                                        )
                                    },
                            ),
                        ),
                ),
            ),
        focusedKey = focusedKey,
        onFocusRow = { focusedKey = it },
        hosting = TvPageHosting.Overlay,
        initialListHasFocus = false,
        initialFocusedOptionLabel = initialFocusedOptionLabel,
    )
}

private enum class FilterState { Included, Excluded }

/** A stand-in for a service's logo: a rounded square in the service's colour, at the row icon's size. */
@Composable
private fun SampleServiceMark(colour: Color) {
    Box(
        modifier =
            Modifier
                .size(dimensionResource(TvR.dimen.tv_list_pane_row_icon))
                .background(colour, BingeShapes.ElementExtraSmall),
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
                note = "Changed from your phone.",
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
