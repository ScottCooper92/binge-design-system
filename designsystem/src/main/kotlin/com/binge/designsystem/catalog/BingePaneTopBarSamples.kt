@file:ScreenshotOnly

package com.binge.designsystem.catalog

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.res.dimensionResource
import com.binge.designsystem.LocalPaneInnerEdge
import com.binge.designsystem.PaneEdge
import com.binge.designsystem.R
import com.binge.designsystem.component.BingePaneTopBar
import com.binge.designsystem.component.ExpressiveIconButton
import com.binge.designsystem.component.LocalTopBarActionTone
import com.binge.designsystem.preview.ScreenshotTheme

/**
 * Alone in the window — [com.binge.designsystem.component.BingeMediumTopBar]'s shape, but with its
 * `containerColor` pinned the way [com.binge.designsystem.component.BingeTopBar] pins its own, so it
 * is not the same as calling [com.binge.designsystem.component.BingeMediumTopBar] directly with an
 * unspecified colour.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BingePaneTopBarAloneSample() {
    ScreenshotTheme {
        BingePaneTopBar(title = "Settings", onBack = {})
    }
}

/** A list pane with a detail pane beside it: the large, expanded title on the list's content inset. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BingePaneTopBarListPaneSample() {
    ScreenshotTheme {
        CompositionLocalProvider(LocalPaneInnerEdge provides PaneEdge.End) {
            BingePaneTopBar(title = "Settings")
        }
    }
}

/**
 * A detail pane nested below the list's first level, so it keeps its back button: the large title
 * starts on the narrow inner-edge inset, below the row the button sits in. Also the only sample to
 * pass `actions`, so the two-row layout's actions Row renders at least once (#119).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BingePaneTopBarDetailPaneSample() {
    ScreenshotTheme {
        CompositionLocalProvider(LocalPaneInnerEdge provides PaneEdge.Start) {
            BingePaneTopBar(
                title = "Genre order",
                onBack = {},
                actions = {
                    ExpressiveIconButton(
                        onClick = {},
                        icon = Icons.Filled.Search,
                        contentDescription = null,
                        tone = LocalTopBarActionTone.current,
                        size = dimensionResource(R.dimen.top_bar_icon_size),
                    )
                },
            )
        }
    }
}
