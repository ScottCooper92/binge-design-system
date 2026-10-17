package com.binge.designsystem.catalog

import androidx.compose.runtime.Composable
import com.android.tools.screenshot.PreviewTest
import com.binge.designsystem.preview.ComponentPreviews
import com.binge.designsystem.preview.PreviewSystemBarInsets
import com.binge.designsystem.preview.ScreenStatePreview

class BingeSheetDockingSamplesScreenshotTest {
    @PreviewTest
    @ComponentPreviews
    @Composable
    fun TopBar() {
        BingeSheetTopBarSample()
    }

    @PreviewTest
    @ComponentPreviews
    @Composable
    fun DockingHeader() {
        BingeSheetDockingHeaderSample()
    }

    /** The pinned footer paints its backing down over the navigation bar, which a default frame renders as zero. */
    @PreviewTest
    @ScreenStatePreview
    @Composable
    fun PinnedFooterUnderSystemBars() {
        PreviewSystemBarInsets { BingeSheetPinnedFooterSample() }
    }
}
