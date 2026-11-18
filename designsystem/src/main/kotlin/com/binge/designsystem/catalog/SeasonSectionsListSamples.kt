@file:OnePerScreen

package com.binge.designsystem.catalog

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.binge.designsystem.component.SeasonSectionState
import com.binge.designsystem.component.SeasonSectionUi
import com.binge.designsystem.component.SeasonSectionsList

/**
 * Public samples for [SeasonSectionsList], which scrolls, so the catalog shows one variant at a time. This one is season 1 loaded and
 * season 2 loading its place with skeleton rows.
 */
@Composable
fun SeasonSectionsListSample() {
    SeasonList(
        listOf(
            loadedSeason(1, "Season 1", "3 episodes", airDate = "5 Mar 2023"),
            SeasonSectionUi("2", "Season 2", "10 episodes", SeasonSectionState.Loading(rowCount = 4)),
        ),
    )
}

/** Every season still loading: skeleton rows under each header. */
@Composable
fun SeasonSectionsListLoadingSample() {
    SeasonList(
        listOf(
            SeasonSectionUi("1", "Season 1", "10 episodes", SeasonSectionState.Loading(rowCount = 4)),
            SeasonSectionUi("2", "Season 2", "10 episodes", SeasonSectionState.Loading(rowCount = 4)),
        ),
    )
}

/** Season 1 failed: its message and a Retry button under its header, season 2 untouched. Retry sends it back to loading. */
@Composable
fun SeasonSectionsListFailedSample() {
    var failed by remember { mutableStateOf(true) }
    SeasonList(
        listOf(
            SeasonSectionUi(
                key = "1",
                title = "Season 1",
                subtitle = "10 episodes",
                state = if (failed) {
                    SeasonSectionState.Failed(
                        message = "Couldn't load this season.",
                        retryLabel = "Retry",
                        onRetry = { failed = false },
                    )
                } else {
                    SeasonSectionState.Loading(rowCount = 4)
                },
            ),
            loadedSeason(2, "Season 2", "3 episodes", airDate = "5 Mar 2023"),
        ),
    )
}

/** The loaded list in Spanish: the copy and the formatted date are the consumer's, the "E1" and "min" come from the design system. */
@Composable
fun SeasonSectionsListSpanishSample() {
    SeasonList(
        listOf(
            loadedSeason(1, "Temporada 1", "3 episodios", airDate = "5 mar 2023"),
            SeasonSectionUi(
                key = "2",
                title = "Temporada 2",
                subtitle = "10 episodios",
                state = SeasonSectionState.Failed(
                    message = "No se ha podido cargar esta temporada.",
                    retryLabel = "Reintentar",
                    onRetry = {},
                ),
            ),
        ),
    )
}
