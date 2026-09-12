package com.binge.designsystem.catalog

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import com.android.tools.screenshot.PreviewTest
import com.binge.designsystem.preview.ComponentPreviews

private const val HERO_RTL_PREVIEW_WIDTH_DP = 412

class HeroCarouselSamplesScreenshotTest {
    @PreviewTest
    @ComponentPreviews
    @Composable
    fun HeroCarousel() {
        HeroCarouselSample()
    }

    /**
     * The regression guard for `HeroScrims`' direction-aware side ramp (#2022). The copy it darkens is
     * `Alignment.BottomStart` and mirrors, so an absolute `Brush.horizontalGradient` would leave the
     * dense end of the vignette on the side the copy just left. One cell rather than the full
     * `@ComponentPreviews` matrix: the fault is a mirrored ramp, which every cell would show identically
     * — and it shows over the flat placeholder, since `validateDebugScreenshotTest` diffs pixels
     * exactly. #2065.
     */
    @PreviewTest
    @Preview(name = "rtl", widthDp = HERO_RTL_PREVIEW_WIDTH_DP, uiMode = UI_MODE_NIGHT_YES)
    @Composable
    fun HeroCarouselRtl() {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            HeroCarouselSample()
        }
    }
}
