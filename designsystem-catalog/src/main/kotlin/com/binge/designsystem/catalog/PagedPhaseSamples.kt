@file:OnePerScreen

package com.binge.designsystem.catalog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import com.binge.designsystem.R
import com.binge.designsystem.component.ListRow
import com.binge.designsystem.component.ListRowSkeletonColumn
import com.binge.designsystem.preview.ScreenshotTheme
import com.binge.designsystem.template.MessageScreen
import com.binge.designsystem.template.PagedPhase
import com.binge.designsystem.template.PagedPhaseContent
import com.binge.designsystem.template.ScreenAction

private val SampleTitles = listOf("The Bear", "Severance", "Shōgun", "Andor", "Fallout")

/** A paged list before anything has arrived and something is on its way: the row skeleton fills the slot. */
@Composable
fun PagedPhaseSkeletonSample() = PagedPhaseSampleFrame(PagedPhase.Skeleton)

/** A paged list with no rows and nothing loading: the empty message fills the slot. */
@Composable
fun PagedPhaseEmptySample() = PagedPhaseSampleFrame(PagedPhase.Empty)

/** The first load failed with nothing cached to show instead: the failure message, with its retry. */
@Composable
fun PagedPhaseFailedSample() = PagedPhaseSampleFrame(PagedPhase.Failed(IllegalStateException("Couldn't reach the server.")))

/** Rows on screen, the refresh behind them finished: the rows slot gets `refreshing = false` and no error. */
@Composable
fun PagedPhaseRowsSample() = PagedPhaseSampleFrame(PagedPhase.Rows(refreshing = false, refreshError = null))

@Composable
private fun PagedPhaseSampleFrame(phase: PagedPhase) {
    ScreenshotTheme(modifier = Modifier.fillMaxSize()) {
        PagedPhaseContent(
            phase = phase,
            skeleton = { ListRowSkeletonColumn(contentPadding = PaddingValues(dimensionResource(R.dimen.padding_m))) },
            empty = { MessageScreen(headline = "Nothing here", body = "Requests you make show up here.", icon = Icons.Filled.Inbox) },
            failed = { error ->
                MessageScreen(
                    headline = "Can't load requests",
                    body = error.message.orEmpty(),
                    icon = Icons.Filled.CloudOff,
                    primary = ScreenAction(label = "Try again", onClick = {}),
                )
            },
            rows = { _, _ ->
                LazyColumn(
                    contentPadding = PaddingValues(dimensionResource(R.dimen.padding_m)),
                    verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_s)),
                ) {
                    items(SampleTitles) { title ->
                        ListRow { textModifier ->
                            Text(text = title, style = MaterialTheme.typography.titleMedium, modifier = textModifier)
                        }
                    }
                }
            },
        )
    }
}
