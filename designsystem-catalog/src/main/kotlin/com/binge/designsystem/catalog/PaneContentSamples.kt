@file:OnePerScreen
@file:CatalogGroup("Layout")

package com.binge.designsystem.catalog

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import com.binge.designsystem.LocalIsSinglePaneNav
import com.binge.designsystem.LocalPaneWidth
import com.binge.designsystem.PaneContent
import com.binge.designsystem.PaneEdge
import com.binge.designsystem.R
import com.binge.designsystem.component.BingeFilterChip
import com.binge.designsystem.preview.ScreenshotTheme
import com.binge.designsystem.resolvedContentPadding
import com.binge.designsystem.template.BingeScreenScaffold
import com.binge.designsystem.template.ScreenBar
import com.binge.designsystem.template.screenInnerPadding
import com.binge.designsystem.template.screenListPadding
import kotlin.math.roundToInt

/**
 * A list pane beside its detail: the content's own side padding is [resolvedContentPadding], so its
 * end edge (the one shared with the other pane) takes the narrow inner inset and its start edge the
 * window-edge ramp. The block's background shows the padding box, and the text reports the width
 * [PaneContent] measured.
 */
@Composable
fun PaneContentListPaneSample() {
    ScreenshotTheme {
        CompositionLocalProvider(LocalIsSinglePaneNav provides false) {
            PaneContent(innerEdge = PaneEdge.End, modifier = Modifier.fillMaxSize()) {
                PaneContentBody()
            }
        }
    }
}

/** The same content as the only pane showing: no inner edge, so both sides take the same inset. */
@Composable
fun PaneContentSinglePaneSample() {
    ScreenshotTheme {
        PaneContent(innerEdge = PaneEdge.End, modifier = Modifier.fillMaxSize()) {
            PaneContentBody()
        }
    }
}

/**
 * A detail pane beside its list, through the scaffold: a small bar, a chip row and list rows. All three start on one
 * edge, the pane's narrow inner inset from the edge it shares with the list.
 */
@Composable
fun PaneContentDetailScaffoldSample() {
    ScreenshotTheme {
        var pending by rememberSaveable { mutableStateOf(true) }
        var approved by rememberSaveable { mutableStateOf(false) }
        CompositionLocalProvider(LocalIsSinglePaneNav provides false) {
            PaneContent(innerEdge = PaneEdge.Start, modifier = Modifier.fillMaxSize()) {
                BingeScreenScaffold(title = "Requests", bar = ScreenBar.Small) { padding ->
                    Column(Modifier.padding(padding.screenInnerPadding())) {
                        Row(
                            modifier = Modifier.padding(resolvedContentPadding()),
                            horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_s)),
                        ) {
                            BingeFilterChip(label = "Pending", selected = pending, onClick = { pending = !pending })
                            BingeFilterChip(label = "Approved", selected = approved, onClick = { approved = !approved })
                        }
                        LazyColumn(
                            contentPadding = PaddingValues().screenListPadding(),
                            verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_s)),
                        ) {
                            items(listOf("The Bear", "Severance", "Shōgun")) { title ->
                                Box(Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.primaryContainer)) {
                                    Text(
                                        text = title,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.padding(vertical = dimensionResource(R.dimen.padding_m)),
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PaneContentBody() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(resolvedContentPadding(top = dimensionResource(R.dimen.padding_m))),
    ) {
        Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.primaryContainer)) {
            Text(
                text = "Pane width: ${LocalPaneWidth.current?.value?.roundToInt()}dp",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.padding(dimensionResource(R.dimen.padding_s)),
            )
        }
    }
}
