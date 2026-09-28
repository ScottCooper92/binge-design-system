package com.binge.designsystem.catalog

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.TopAppBarState
import androidx.compose.runtime.Composable
import com.android.tools.screenshot.PreviewTest
import com.binge.designsystem.component.BingePaneTopBar
import com.binge.designsystem.preview.ComponentPreviews
import com.binge.designsystem.preview.ScreenshotTheme

class BingePaneTopBarSamplesScreenshotTest {
    @PreviewTest
    @ComponentPreviews
    @Composable
    fun Alone() {
        BingePaneTopBarAloneSample()
    }

    /**
     * [BingePaneTopBar]'s alone form, scrolled, at its default (unspecified) `containerColor` —
     * the exact case #121 regressed: the alone form delegates to `BingeMediumTopBar`, which only
     * pins its scrolled colour when given an explicit `containerColor`. No sample or frame drove
     * `collapsedFraction` above 0 here, so the regression shipped with CI green throughout (#122).
     */
    @OptIn(ExperimentalMaterial3Api::class)
    @PreviewTest
    @ComponentPreviews
    @Composable
    fun AloneScrolled() {
        ScreenshotTheme {
            BingePaneTopBar(title = "Settings", onBack = {}, scrollBehavior = collapsedScrollBehavior())
        }
    }

    @PreviewTest
    @ComponentPreviews
    @Composable
    fun ListPane() {
        BingePaneTopBarListPaneSample()
    }

    @PreviewTest
    @ComponentPreviews
    @Composable
    fun DetailPane() {
        BingePaneTopBarDetailPaneSample()
    }
}

private const val COLLAPSED_HEIGHT_OFFSET_LIMIT = -200f

/**
 * A scroll behavior fully scrolled past the title, so the bar renders collapsed
 * (`collapsedFraction == 1f`). Mirrors `BingeMediumTopBarScreenshotTest`'s own helper of the same
 * shape — the collapsed look is scroll-driven, not a param, so a frame has to force it.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun collapsedScrollBehavior(): TopAppBarScrollBehavior =
    TopAppBarDefaults.exitUntilCollapsedScrollBehavior(
        TopAppBarState(
            initialHeightOffsetLimit = COLLAPSED_HEIGHT_OFFSET_LIMIT,
            initialHeightOffset = COLLAPSED_HEIGHT_OFFSET_LIMIT,
            initialContentOffset = 0f,
        ),
    )
