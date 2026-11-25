package com.binge.designsystem.catalog

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.android.tools.screenshot.PreviewTest

/** A pane's width, as it would be beside a detail pane on a tablet. */
private const val PANE_DEVICE = "spec:width=411dp,height=240dp"

/** A detail pane tall enough for its bar, a chip row and three list rows. */
private const val DETAIL_DEVICE = "spec:width=411dp,height=360dp"

class PaneContentSamplesScreenshotTest {
    @PreviewTest
    @Preview(name = "list-pane", device = PANE_DEVICE, uiMode = UI_MODE_NIGHT_YES)
    @Composable
    fun ListPane() {
        PaneContentListPaneSample()
    }

    @PreviewTest
    @Preview(name = "single-pane", device = PANE_DEVICE, uiMode = UI_MODE_NIGHT_YES)
    @Composable
    fun SinglePane() {
        PaneContentSinglePaneSample()
    }

    @PreviewTest
    @Preview(name = "detail-scaffold", device = DETAIL_DEVICE, uiMode = UI_MODE_NIGHT_YES)
    @Composable
    fun DetailScaffold() {
        PaneContentDetailScaffoldSample()
    }
}
