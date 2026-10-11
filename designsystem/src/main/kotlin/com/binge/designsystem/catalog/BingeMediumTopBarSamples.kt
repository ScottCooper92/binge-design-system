@file:ScreenshotOnly

package com.binge.designsystem.catalog

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
import com.binge.designsystem.R
import com.binge.designsystem.component.BingeMediumTopBar
import com.binge.designsystem.component.ExpressiveIconButton
import com.binge.designsystem.component.LocalTopBarActionTint
import com.binge.designsystem.component.LocalTopBarActionTone
import com.binge.designsystem.component.TransparentBingeMediumTopBarSample
import com.binge.designsystem.preview.ScreenshotTheme
import com.binge.designsystem.theme.BingeTheme

/** Medium collapsing top bar — the expanded (uncollapsed) title with a trailing plain action. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BingeMediumTopBarSample() {
    ScreenshotTheme {
        BingeMediumTopBar(
            title = "Popular Movies",
            onBack = {},
            actions = { SearchAction() },
        )
    }
}

/** Hero treatment — a transparent medium bar with Glass nav/action buttons over imagery. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BingeMediumTopBarTransparentSample() {
    ScreenshotTheme {
        TransparentBingeMediumTopBarSample()
    }
}

/**
 * Scrolled past the title, so the bar renders its collapsed row: the title clears the nav circle via
 * the fraction-driven start inset. The collapsed look is scroll-driven, not a param, so the sample
 * forces it through [collapsedTopBarScrollBehavior].
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BingeMediumTopBarCollapsedSample() {
    ScreenshotTheme {
        BingeMediumTopBar(
            title = "Popular Movies",
            onBack = {},
            scrollBehavior = collapsedTopBarScrollBehavior(),
            actions = { SearchAction() },
        )
    }
}

/**
 * Title and back button only, expanded. Its frame renders it at the ≥840dp expanded breakpoint, where
 * `resolvedContentInset()` (32dp) runs ahead of M3's fixed [R.dimen.pane_top_bar_title_inset] (16dp)
 * — the gap `contentInsetGap` closes.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BingeMediumTopBarTitleOnlySample() {
    ScreenshotTheme {
        BingeMediumTopBar(title = "Popular Movies", onBack = {})
    }
}

/** The hero treatment with its scrim fully in: the always-black scrim behind the bar and the title stepped to its scrimmed colour. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BingeMediumTopBarScrimmedSample() {
    ScreenshotTheme {
        TransparentBingeMediumTopBarSample(scrimFraction = 1f)
    }
}

/**
 * Just below `scrimmedTitleColor`'s 0.75 switch fraction: the title stays `onSurface`. Paired with
 * [BingeMediumTopBarTitleAfterScrimSwitchSample] to straddle the step (#93) — 0 and 1 alone give the
 * same title colour under the old blend and the new step, by construction, so neither proves the
 * switch actually lands at 0.75.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BingeMediumTopBarTitleBeforeScrimSwitchSample() {
    ScreenshotTheme {
        TransparentBingeMediumTopBarSample(scrimFraction = BEFORE_SCRIM_SWITCH_FRACTION)
    }
}

/** Just past `scrimmedTitleColor`'s 0.75 switch fraction: the title has stepped to the scrimmed colour. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BingeMediumTopBarTitleAfterScrimSwitchSample() {
    ScreenshotTheme {
        TransparentBingeMediumTopBarSample(scrimFraction = AFTER_SCRIM_SWITCH_FRACTION)
    }
}

/**
 * A fully scrimmed bar over a fixed-height, distinctly coloured frame with room below its own bottom
 * edge — proving `TopBarScrim`'s tail fades gradually into that room instead of cutting off at the
 * bar's boundary (#94).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BingeMediumTopBarScrimTailFadeSample() {
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

/**
 * The default scrim, fully in — a transparent bar over a plain surface (a grid or list on the app's
 * own background), left at its default [BingeMediumTopBar.scrimColor]/`scrimForegroundColor` rather
 * than overridden to the always-black pair the hero sample above uses. Proves the pair this PR's
 * whole default-flip is about actually renders legibly in both themes, since every other transparent
 * fixture in this catalog explicitly opts back into black-always.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BingeMediumTopBarThemeFollowingScrimSample() {
    ScreenshotTheme {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.background),
        ) {
            BingeMediumTopBar(
                title = "Popular Movies",
                onBack = {},
                containerColor = Color.Transparent,
                scrimFraction = 1f,
                actions = { glassBackgroundAlpha ->
                    ExpressiveIconButton(
                        onClick = {},
                        icon = Icons.Filled.Search,
                        contentDescription = null,
                        tint = LocalTopBarActionTint.current,
                        tone = LocalTopBarActionTone.current,
                        size = dimensionResource(R.dimen.top_bar_icon_size),
                        glassBackgroundAlpha = glassBackgroundAlpha,
                    )
                },
            )
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

private const val BEFORE_SCRIM_SWITCH_FRACTION = 0.5f
private const val AFTER_SCRIM_SWITCH_FRACTION = 0.9f
private const val COLLAPSED_HEIGHT_OFFSET_LIMIT = -200f

/**
 * A scroll behavior fully scrolled past the title, so a collapsing bar renders collapsed: with
 * `heightOffset == heightOffsetLimit`, `collapsedFraction == 1f`. Also used by
 * [BingePaneTopBarAloneScrolledSample].
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun collapsedTopBarScrollBehavior(): TopAppBarScrollBehavior =
    TopAppBarDefaults.exitUntilCollapsedScrollBehavior(
        TopAppBarState(
            initialHeightOffsetLimit = COLLAPSED_HEIGHT_OFFSET_LIMIT,
            initialHeightOffset = COLLAPSED_HEIGHT_OFFSET_LIMIT,
            initialContentOffset = 0f,
        ),
    )
