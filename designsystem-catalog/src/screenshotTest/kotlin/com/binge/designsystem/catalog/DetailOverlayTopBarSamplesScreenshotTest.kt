package com.binge.designsystem.catalog

import android.content.res.Configuration.UI_MODE_NIGHT_NO
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.android.tools.screenshot.PreviewTest

/** A phone canvas tall enough to show the hero plus the overlay bar. */
private const val OVERLAY_BAR_DEVICE = "spec:width=411dp,height=560dp"

class DetailOverlayTopBarSamplesScreenshotTest {
    @PreviewTest
    @Preview(name = "resting-dark", device = OVERLAY_BAR_DEVICE, uiMode = UI_MODE_NIGHT_YES)
    @Composable
    fun Resting() {
        DetailOverlayTopBarRestingSample()
    }

    @PreviewTest
    @Preview(name = "midscroll-dark", device = OVERLAY_BAR_DEVICE, uiMode = UI_MODE_NIGHT_YES)
    @Composable
    fun MidScroll() {
        DetailOverlayTopBarMidScrollSample()
    }

    @PreviewTest
    @Preview(name = "scrolled-dark", device = OVERLAY_BAR_DEVICE, uiMode = UI_MODE_NIGHT_YES)
    @Composable
    fun Scrolled() {
        DetailOverlayTopBarScrolledSample()
    }

    /** Fully scrolled in light theme: catches a tint left hardcoded at `onScrim` (white on a light bar). */
    @PreviewTest
    @Preview(name = "scrolled-light", device = OVERLAY_BAR_DEVICE, uiMode = UI_MODE_NIGHT_NO)
    @Composable
    fun ScrolledLight() {
        DetailOverlayTopBarScrolledSample()
    }
}
