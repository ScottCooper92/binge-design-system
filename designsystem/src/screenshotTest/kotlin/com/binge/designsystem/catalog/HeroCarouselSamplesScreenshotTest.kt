package com.binge.designsystem.catalog

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import com.android.tools.screenshot.PreviewTest
import com.binge.designsystem.preview.ComponentPreviews

private const val HERO_ONE_CELL_WIDTH_DP = 412
private const val HERO_WIDE_WIDTH_DP = 840

class HeroCarouselSamplesScreenshotTest {
    @PreviewTest
    @ComponentPreviews
    @Composable
    fun HeroCarousel() {
        HeroCarouselSample()
    }

    /** A list that is not a ranking draws no trending pill. One cell: the pill's absence is the whole frame. */
    @PreviewTest
    @Preview(name = "unranked", widthDp = HERO_ONE_CELL_WIDTH_DP, uiMode = UI_MODE_NIGHT_YES)
    @Composable
    fun HeroCarouselUnranked() {
        HeroCarouselUnrankedSample()
    }

    /**
     * The regression guard for the direction-aware fade behind the copy. The copy is
     * `Alignment.BottomStart` and mirrors, so the fade has to fall away from the copy's end edge, its
     * left in RTL. Read the wrong edge and the fade sits on the side the copy just left. One cell rather than the full
     * `@ComponentPreviews` matrix: the fault is a mirrored ramp, which every cell would show identically
     * — and it shows over the flat placeholder, since `validateDebugScreenshotTest` diffs pixels
     * exactly.
     */
    @PreviewTest
    @Preview(name = "rtl", widthDp = HERO_ONE_CELL_WIDTH_DP, uiMode = UI_MODE_NIGHT_YES)
    @Composable
    fun HeroCarouselRtl() {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            HeroCarouselSample()
        }
    }

    /** The full copy column: trending pill, title, tagline and meta row over the scrim, on the first slide. */
    @PreviewTest
    @ComponentPreviews
    @Composable
    fun Ready() {
        HeroCarouselTaglineSample()
    }

    /** Expanded width: across a wide window the copy and dot rail hold their layout instead of stretching. */
    @PreviewTest
    @Preview(name = "wide-light", widthDp = HERO_WIDE_WIDTH_DP, showBackground = true)
    @Composable
    fun Wide() {
        HeroCarouselTaglineSample()
    }
}
