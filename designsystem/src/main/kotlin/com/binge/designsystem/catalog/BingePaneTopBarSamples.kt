package com.binge.designsystem.catalog

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import com.binge.designsystem.LocalPaneInnerEdge
import com.binge.designsystem.PaneEdge
import com.binge.designsystem.component.BingePaneTopBar
import com.binge.designsystem.preview.ScreenshotTheme

/** Alone in the window — renders exactly as [com.binge.designsystem.component.BingeTopBar]. */
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
 * starts on the narrow inner-edge inset, below the row the button sits in.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BingePaneTopBarDetailPaneSample() {
    ScreenshotTheme {
        CompositionLocalProvider(LocalPaneInnerEdge provides PaneEdge.Start) {
            BingePaneTopBar(title = "Genre order", onBack = {})
        }
    }
}
