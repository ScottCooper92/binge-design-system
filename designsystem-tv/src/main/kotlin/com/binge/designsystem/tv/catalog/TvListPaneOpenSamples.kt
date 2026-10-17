@file:CatalogGroup("Page templates")

package com.binge.designsystem.tv.catalog

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cast
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Subtitles
import androidx.compose.material.icons.filled.ViewDay
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import com.binge.designsystem.catalog.CatalogGroup
import com.binge.designsystem.tv.component.TvMessagePlate
import com.binge.designsystem.tv.focus.tvStepOutFocusGroup
import com.binge.designsystem.tv.template.TvListPaneBoard
import com.binge.designsystem.tv.template.TvListPaneListPreview
import com.binge.designsystem.tv.template.TvPageHosting
import com.binge.designsystem.tv.template.TvPaneGroup
import com.binge.designsystem.tv.template.TvPaneOption
import com.binge.designsystem.tv.template.TvPaneRow

/*
 * Boards whose rows open something of their own: a list of apps, each leading with the glyph the caller resolved
 * for it, whose pane shows where OK leads instead of describing the row.
 */

private const val KEY_STREAMLY = "streamly"
private const val KEY_CINEBOX = "cinebox"
private const val KEY_FLIXORA = "flixora"
private const val KEY_STORE = "store"
private const val APP_STREAMLY = "Streamly"
private const val APP_CINEBOX = "Cinebox"
private const val APP_FLIXORA = "Flixora"

/** Rows leading with a glyph the caller resolved itself, such as an app's own, in the row's content colour. */
@Composable
fun TvListPaneBoardRowPainterSample() {
    var focusedKey by remember { mutableStateOf(KEY_FLIXORA) }
    TvListPaneBoard(
        title = "Apps",
        groups = listOf(TvPaneGroup(title = "Installed", rows = appRows(opens = false))),
        focusedKey = focusedKey,
        onFocusRow = { focusedKey = it },
        hosting = TvPageHosting.Overlay,
        initialListHasFocus = true,
    )
}

/** A row whose pane is drawn by the caller in place of the description: here a message about the store it opens. */
@Composable
fun TvListPaneBoardPaneContentSample() {
    var focusedKey by remember { mutableStateOf(KEY_STORE) }
    val store =
        TvPaneRow(
            key = KEY_STORE,
            label = "Find more apps",
            detail = "Browse the store",
            icon = Icons.Filled.Storefront,
            paneContent = {
                TvMessagePlate(
                    headline = "Find more apps",
                    body = "Apps that work with this one, listed in the store.",
                    icon = Icons.Filled.Storefront,
                )
            },
        )
    TvListPaneBoard(
        title = "Apps",
        groups = listOf(TvPaneGroup(title = "Installed", rows = appRows(opens = false)), TvPaneGroup(title = "More", rows = listOf(store))),
        focusedKey = focusedKey,
        onFocusRow = { focusedKey = it },
        hosting = TvPageHosting.Overlay,
        initialListHasFocus = true,
    )
}

/**
 * Rows that open an app's own settings, each pane previewing that list as it will look. OK or → on a row steps into
 * it, and ← at its start edge steps back out onto the row it came from.
 */
@Composable
fun TvListPaneBoardListPreviewSample() {
    var focusedKey by remember { mutableStateOf(KEY_STREAMLY) }
    var opened by remember { mutableStateOf<String?>(null) }
    val openedApp = opened
    if (openedApp == null) {
        TvListPaneBoard(
            title = "Apps",
            groups = listOf(TvPaneGroup(title = "Installed", rows = appRows(opens = true) { opened = it })),
            focusedKey = focusedKey,
            onFocusRow = { focusedKey = it },
            hosting = TvPageHosting.Overlay,
            initialListHasFocus = true,
        )
    } else {
        var settingKey by remember { mutableStateOf<String?>(null) }
        val groups = appSettings()
        TvListPaneBoard(
            title = appName(openedApp),
            groups = groups,
            focusedKey = settingKey ?: groups
                .first()
                .rows
                .first()
                .key,
            onFocusRow = { settingKey = it },
            hosting = TvPageHosting.Overlay,
            modifier = Modifier.tvStepOutFocusGroup { opened = null },
        )
    }
}

@Composable
private fun appRows(opens: Boolean, onOpen: (String) -> Unit = {}): List<TvPaneRow> =
    listOf(
        appRow(KEY_STREAMLY, "Watch · Requests", Icons.Filled.Cast, opens, onOpen),
        appRow(KEY_CINEBOX, "Watch", Icons.Filled.Movie, opens, onOpen),
        appRow(KEY_FLIXORA, "Not turned on yet", Icons.Filled.CloudQueue, opens = false, onOpen = onOpen),
    )

@Composable
private fun appRow(
    key: String,
    detail: String,
    glyph: ImageVector,
    opens: Boolean,
    onOpen: (String) -> Unit,
): TvPaneRow {
    val painter = rememberVectorPainter(glyph)
    return TvPaneRow(
        key = key,
        label = appName(key),
        detail = detail,
        body = "Turning it on lets it add rows to your hubs.",
        options = if (opens) emptyList() else listOf(TvPaneOption(label = "Turn on", icon = Icons.Filled.PowerSettingsNew, action = true)),
        iconPainter = { painter },
        onClick = if (opens) ({ onOpen(key) }) else null,
        paneContent = if (opens) ({ TvListPaneListPreview(appSettings()) }) else null,
    )
}

private fun appSettings(): List<TvPaneGroup> =
    listOf(
        TvPaneGroup(
            title = "Rows on your hubs",
            rows =
                listOf(
                    TvPaneRow(key = "row_new", label = "New this week", detail = "On", icon = Icons.Filled.ViewDay),
                    TvPaneRow(key = "row_subtitles", label = "With subtitles", detail = "Off", icon = Icons.Filled.Subtitles),
                ),
        ),
    )

private fun appName(key: String): String =
    when (key) {
        KEY_STREAMLY -> APP_STREAMLY
        KEY_CINEBOX -> APP_CINEBOX
        else -> APP_FLIXORA
    }
