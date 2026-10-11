package com.binge.designsystem.catalog

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.android.tools.screenshot.PreviewTest
import com.binge.designsystem.preview.ComponentPreviews

class BingeMediumTopBarSamplesScreenshotTest {
    @PreviewTest
    @ComponentPreviews
    @Composable
    fun Standard() {
        BingeMediumTopBarSample()
    }

    @PreviewTest
    @ComponentPreviews
    @Composable
    fun Transparent() {
        BingeMediumTopBarTransparentSample()
    }

    @PreviewTest
    @ComponentPreviews
    @Composable
    fun ThemeFollowingScrim() {
        BingeMediumTopBarThemeFollowingScrimSample()
    }

    @PreviewTest
    @ComponentPreviews
    @Composable
    fun Collapsed() {
        BingeMediumTopBarCollapsedSample()
    }

    @PreviewTest
    @ComponentPreviews
    @Composable
    fun Scrimmed() {
        BingeMediumTopBarScrimmedSample()
    }

    @PreviewTest
    @ComponentPreviews
    @Composable
    fun TitleBeforeScrimSwitch() {
        BingeMediumTopBarTitleBeforeScrimSwitchSample()
    }

    @PreviewTest
    @ComponentPreviews
    @Composable
    fun TitleAfterScrimSwitch() {
        BingeMediumTopBarTitleAfterScrimSwitchSample()
    }

    /**
     * The ≥840dp expanded breakpoint, where `resolvedContentInset()` runs ahead of M3's fixed title
     * inset. Same device spec as `DetailHeroSamplesScreenshotTest.AtPaneWidth`. Paired with
     * [AtExpandedWidthCollapsed] to cover both rows the gap applies to.
     */
    @PreviewTest
    @Preview(name = "expanded", device = "spec:width=840dp,height=1180dp,orientation=portrait", uiMode = UI_MODE_NIGHT_YES)
    @Composable
    fun AtExpandedWidth() {
        BingeMediumTopBarTitleOnlySample()
    }

    /** Same expanded window, scrolled past the title, so the collapsed row's own extra nudge stacks on top of the same gap. */
    @PreviewTest
    @Preview(name = "expanded-collapsed", device = "spec:width=840dp,height=1180dp,orientation=portrait", uiMode = UI_MODE_NIGHT_YES)
    @Composable
    fun AtExpandedWidthCollapsed() {
        BingeMediumTopBarCollapsedSample()
    }

    @PreviewTest
    @ComponentPreviews
    @Composable
    fun ScrimTailFade() {
        BingeMediumTopBarScrimTailFadeSample()
    }
}
