@file:OnePerScreen

package com.binge.designsystem.catalog

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import com.binge.designsystem.LocalIsSinglePaneNav
import com.binge.designsystem.LocalPaneWidth
import com.binge.designsystem.PaneContent
import com.binge.designsystem.PaneEdge
import com.binge.designsystem.R
import com.binge.designsystem.preview.ScreenshotTheme
import com.binge.designsystem.resolvedContentPadding
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
