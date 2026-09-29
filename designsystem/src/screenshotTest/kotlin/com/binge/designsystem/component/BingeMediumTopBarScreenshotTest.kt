package com.binge.designsystem.component

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.TopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.tooling.preview.Preview
import com.android.tools.screenshot.PreviewTest
import com.binge.designsystem.R
import com.binge.designsystem.preview.ComponentPreviews
import com.binge.designsystem.preview.ScreenshotTheme
import com.binge.designsystem.theme.BingeTheme

/** Medium top bar — medium collapsing title with plain nav/action glyphs. */
@OptIn(ExperimentalMaterial3Api::class)
class BingeMediumTopBarScreenshotTest {
    @PreviewTest
    @ComponentPreviews
    @Composable
    fun TitleAndBack() {
        ScreenshotTheme {
            BingeMediumTopBar(title = "Popular Movies", onBack = {})
        }
    }

    @PreviewTest
    @ComponentPreviews
    @Composable
    fun Collapsed() {
        ScreenshotTheme {
            BingeMediumTopBar(
                title = "Popular Movies",
                onBack = {},
                scrollBehavior = collapsedScrollBehavior(),
                actions = { SearchAction() },
            )
        }
    }

    /**
     * A wide (840dp) single-pane window, no [com.binge.designsystem.LocalPaneWidth] override — the
     * title's start inset should widen to [com.binge.designsystem.resolvedContentInset]'s own
     * expanded step (32dp) rather than staying at the fixed 16dp M3 default (#2953).
     */
    @PreviewTest
    @Preview(name = "wide840", device = "spec:width=840dp,height=200dp,orientation=landscape", uiMode = UI_MODE_NIGHT_YES)
    @Composable
    fun TitleAndBackAtWideWindow() {
        ScreenshotTheme {
            BingeMediumTopBar(title = "Popular Movies", onBack = {})
        }
    }

    /** [TitleAndBackAtWideWindow]'s collapsed counterpart — the same wide-window inset, plus the collapsed-only nav clearance. */
    @PreviewTest
    @Preview(name = "wide840", device = "spec:width=840dp,height=200dp,orientation=landscape", uiMode = UI_MODE_NIGHT_YES)
    @Composable
    fun CollapsedAtWideWindow() {
        ScreenshotTheme {
            BingeMediumTopBar(
                title = "Popular Movies",
                onBack = {},
                scrollBehavior = collapsedScrollBehavior(),
                actions = { SearchAction() },
            )
        }
    }

    @PreviewTest
    @ComponentPreviews
    @Composable
    fun Transparent() {
        ScreenshotTheme {
            TransparentBingeMediumTopBarSample()
        }
    }

    @PreviewTest
    @ComponentPreviews
    @Composable
    fun Scrimmed() {
        ScreenshotTheme {
            TransparentBingeMediumTopBarSample(scrimFraction = 1f)
        }
    }

    /**
     * Just below [scrimmedTitleColor]'s 0.75 switch fraction: the title stays `onSurface`. Paired
     * with [TitleAfterScrimSwitch] to straddle the step (#93) — 0 and 1 alone give the same title
     * colour under the old blend and the new step, by construction, so neither proves the switch
     * actually lands at 0.75.
     */
    @PreviewTest
    @ComponentPreviews
    @Composable
    fun TitleBeforeScrimSwitch() {
        ScreenshotTheme {
            TransparentBingeMediumTopBarSample(scrimFraction = 0.5f)
        }
    }

    /** Just past [scrimmedTitleColor]'s 0.75 switch fraction: the title has stepped to the scrimmed colour. */
    @PreviewTest
    @ComponentPreviews
    @Composable
    fun TitleAfterScrimSwitch() {
        ScreenshotTheme {
            TransparentBingeMediumTopBarSample(scrimFraction = 0.9f)
        }
    }

    /**
     * A window at the ≥840dp expanded breakpoint, where `resolvedContentInset()` (32dp) runs ahead of
     * M3's fixed [R.dimen.pane_top_bar_title_inset] (16dp) — the gap `contentInsetGap` closes. Same
     * device spec as `DetailHeroScreenshotTest.AtPaneWidth`. Paired with [AtExpandedWidthCollapsed] to
     * cover both rows the gap applies to.
     */
    @PreviewTest
    @Preview(name = "expanded", device = "spec:width=840dp,height=1180dp,orientation=portrait", uiMode = UI_MODE_NIGHT_YES)
    @Composable
    fun AtExpandedWidth() {
        ScreenshotTheme {
            BingeMediumTopBar(title = "Popular Movies", onBack = {})
        }
    }

    /** Same expanded window, scrolled past the title, so the collapsed row's own extra nudge stacks on top of the same gap. */
    @PreviewTest
    @Preview(name = "expanded-collapsed", device = "spec:width=840dp,height=1180dp,orientation=portrait", uiMode = UI_MODE_NIGHT_YES)
    @Composable
    fun AtExpandedWidthCollapsed() {
        ScreenshotTheme {
            BingeMediumTopBar(
                title = "Popular Movies",
                onBack = {},
                scrollBehavior = collapsedScrollBehavior(),
                actions = { SearchAction() },
            )
        }
    }

    /**
     * A fully scrimmed bar over a fixed-height, distinctly coloured frame with room below its own
     * bottom edge — proving [TopBarScrim]'s tail fades gradually into that room instead of cutting
     * off at the bar's boundary (#94).
     */
    @PreviewTest
    @ComponentPreviews
    @Composable
    fun ScrimTailFade() {
        ScreenshotTheme {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(dimensionResource(R.dimen.top_bar_scrim_tail_preview_height))
                    .background(MaterialTheme.colorScheme.tertiaryContainer),
            ) {
                BingeMediumTopBar(
                    title = "Popular Movies",
                    onBack = {},
                    containerColor = Color.Transparent,
                    scrimFraction = 1f,
                    scrimColor = BingeTheme.colors.scrim,
                    scrimForegroundColor = BingeTheme.colors.onScrim,
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchAction() {
    ExpressiveIconButton(
        onClick = {},
        icon = Icons.Filled.Search,
        contentDescription = null,
        tone = LocalTopBarActionTone.current,
        size = dimensionResource(R.dimen.top_bar_icon_size),
    )
}

private const val COLLAPSED_HEIGHT_OFFSET_LIMIT = -200f

/** A scroll behavior at rest, so the bar renders expanded (`collapsedFraction == 0f`). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun restingScrollBehavior(): TopAppBarScrollBehavior =
    TopAppBarDefaults.exitUntilCollapsedScrollBehavior(
        TopAppBarState(
            initialHeightOffsetLimit = COLLAPSED_HEIGHT_OFFSET_LIMIT,
            initialHeightOffset = 0f,
            initialContentOffset = 0f,
        ),
    )

/**
 * A scroll behavior fully scrolled past the title, so the bar renders collapsed. The collapsed look
 * is scroll-driven, not a param, so the preview forces it: with `heightOffset == heightOffsetLimit`,
 * `collapsedFraction == 1f` and the title clears the nav circle via the fraction-driven start inset.
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
