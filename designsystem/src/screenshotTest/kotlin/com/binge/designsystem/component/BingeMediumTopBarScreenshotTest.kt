package com.binge.designsystem.component

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.TopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.dimensionResource
import com.android.tools.screenshot.PreviewTest
import com.binge.designsystem.R
import com.binge.designsystem.preview.ComponentPreviews
import com.binge.designsystem.preview.ScreenshotTheme

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
