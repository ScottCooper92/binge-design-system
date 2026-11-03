@file:CatalogGroup("Page templates")

package com.binge.designsystem.tv.catalog

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.binge.designsystem.SaveStatus
import com.binge.designsystem.catalog.CatalogGroup
import com.binge.designsystem.tv.template.TvListPaneBoard
import com.binge.designsystem.tv.template.TvPageHosting
import com.binge.designsystem.tv.template.TvPaneGroup
import com.binge.designsystem.tv.template.TvPaneOption
import com.binge.designsystem.tv.template.TvPaneRow
import kotlinx.coroutines.delay

private const val KEY_AUDIO = "audio"
private const val OPTION_ENGLISH = "English"
private const val OPTION_SPANISH = "Spanish"
private const val REFUSAL_REASON = "Only an administrator can change this."
private const val SAVE_MILLIS = 900L

/** A row whose last change is being saved: "Saving…" after the pane's options, where nothing above it moves. */
@Composable
fun TvListPaneBoardSavingSample() {
    SeededSaveStatusBoard(SaveStatus.Saving)
}

/** A change that did not save: the failure with its Retry in the pane, and the chosen option still on screen. */
@Composable
fun TvListPaneBoardSaveFailedSample() {
    SeededSaveStatusBoard(SaveStatus.Failed)
}

/** A change the page put back, and asking again would be refused again: the reason, with no Retry. */
@Composable
fun TvListPaneBoardSaveRefusedSample() {
    SeededSaveStatusBoard(SaveStatus.Refused(REFUSAL_REASON))
}

/**
 * Live: choosing a language saves for a moment, and the first save fails. Retry sits below the options and takes no
 * focus when the failure arrives, so the option stays where it is; pressing it asks again and hands focus back up to the
 * option, and the second save lands as "Saved".
 */
@Composable
fun TvListPaneBoardSaveStatusDemo() {
    var language by remember { mutableStateOf(OPTION_ENGLISH) }
    var status by remember { mutableStateOf<SaveStatus>(SaveStatus.Idle) }
    var saves by remember { mutableIntStateOf(0) }
    LaunchedEffect(saves) {
        if (saves == 0) return@LaunchedEffect
        status = SaveStatus.Saving
        delay(SAVE_MILLIS)
        status = if (saves == 1) SaveStatus.Failed else SaveStatus.Saved
    }
    SaveStatusBoard(
        status = status,
        selected = language,
        onSelect = {
            language = it
            saves++
        },
        onRetry = { saves++ },
    )
}

/** Seeded with the status its frame shows; choosing an option ticks it, and Retry moves a failure on to Saved. */
@Composable
private fun SeededSaveStatusBoard(seed: SaveStatus) {
    var language by remember { mutableStateOf(OPTION_SPANISH) }
    var status by remember { mutableStateOf(seed) }
    SaveStatusBoard(
        status = status,
        selected = language,
        onSelect = { language = it },
        onRetry = { status = SaveStatus.Saved },
    )
}

@Composable
private fun SaveStatusBoard(
    status: SaveStatus,
    selected: String,
    onSelect: (String) -> Unit,
    onRetry: () -> Unit,
) {
    TvListPaneBoard(
        title = "Settings",
        groups =
            listOf(
                TvPaneGroup(
                    title = "Playback",
                    rows =
                        listOf(
                            TvPaneRow(
                                key = KEY_AUDIO,
                                label = "Audio language",
                                body = "The language a title starts in.",
                                detail = selected,
                                options =
                                    listOf(OPTION_ENGLISH, OPTION_SPANISH).map { option ->
                                        TvPaneOption(label = option, selected = selected == option, onSelect = { onSelect(option) })
                                    },
                                saveStatus = status,
                                onRetry = onRetry,
                            ),
                        ),
                ),
            ),
        focusedKey = KEY_AUDIO,
        onFocusRow = {},
        hosting = TvPageHosting.Overlay,
    )
}
