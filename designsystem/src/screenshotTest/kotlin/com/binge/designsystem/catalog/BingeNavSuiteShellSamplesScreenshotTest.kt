package com.binge.designsystem.catalog

import android.content.res.Configuration.UI_MODE_NIGHT_NO
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.android.tools.screenshot.PreviewTest

/**
 * Screenshot coverage for the app shell's docked navigation tiers. Each tier is captured at
 * the canvas size its bucket targets, and previews pass the presentation explicitly (not via the
 * window bucket) so the canvas size and rendered nav stay in lockstep.
 */
class BingeNavSuiteShellSamplesScreenshotTest {
    @PreviewTest
    @Preview(name = "bar-light", widthDp = 412, heightDp = 740, uiMode = UI_MODE_NIGHT_NO)
    @Preview(name = "bar-dark", widthDp = 412, heightDp = 740, uiMode = UI_MODE_NIGHT_YES)
    @Composable
    fun BottomBar() {
        BingeNavSuiteShellBottomBarSample()
    }

    @PreviewTest
    @Preview(name = "bar-out-light", widthDp = 412, heightDp = 740, uiMode = UI_MODE_NIGHT_NO)
    @Preview(name = "bar-out-dark", widthDp = 412, heightDp = 740, uiMode = UI_MODE_NIGHT_YES)
    @Composable
    fun BottomBarSignedOut() {
        BingeNavSuiteShellBottomBarSignedOutSample()
    }

    @PreviewTest
    @Preview(name = "bar-tablet-portrait-light", widthDp = 834, heightDp = 1194, uiMode = UI_MODE_NIGHT_NO)
    @Preview(name = "bar-tablet-portrait-dark", widthDp = 834, heightDp = 1194, uiMode = UI_MODE_NIGHT_YES)
    @Composable
    fun TabletPortraitBar() {
        BingeNavSuiteShellTabletPortraitBarSample()
    }

    @PreviewTest
    @Preview(name = "rail-tablet-light", widthDp = 900, heightDp = 600, uiMode = UI_MODE_NIGHT_NO)
    @Preview(name = "rail-tablet-dark", widthDp = 900, heightDp = 600, uiMode = UI_MODE_NIGHT_YES)
    @Composable
    fun TabletRail() {
        BingeNavSuiteShellTabletRailSample()
    }

    @PreviewTest
    @Preview(name = "rail-art-light", widthDp = 900, heightDp = 600, uiMode = UI_MODE_NIGHT_NO)
    @Preview(name = "rail-art-dark", widthDp = 900, heightDp = 600, uiMode = UI_MODE_NIGHT_YES)
    @Composable
    fun TabletRailOverArtwork() {
        BingeNavSuiteShellTabletRailOverArtworkSample()
    }

    @PreviewTest
    @Preview(name = "rail-art-rtl-light", widthDp = 900, heightDp = 600, uiMode = UI_MODE_NIGHT_NO)
    @Preview(name = "rail-art-rtl-dark", widthDp = 900, heightDp = 600, uiMode = UI_MODE_NIGHT_YES)
    @Composable
    fun TabletRailOverArtworkRtl() {
        BingeNavSuiteShellTabletRailOverArtworkRtlSample()
    }

    @PreviewTest
    @Preview(name = "rail-list-light", widthDp = 900, heightDp = 600, uiMode = UI_MODE_NIGHT_NO)
    @Preview(name = "rail-list-dark", widthDp = 900, heightDp = 600, uiMode = UI_MODE_NIGHT_YES)
    @Composable
    fun TabletRailOverList() {
        BingeNavSuiteShellTabletRailOverListSample()
    }
}
