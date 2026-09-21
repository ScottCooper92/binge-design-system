package com.binge.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.isSpecified
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import com.binge.designsystem.R
import com.binge.designsystem.theme.BingeTheme

/**
 * Binge's medium top bar with a collapsing title — the collapsing sibling of [BingeTopBar], for
 * screens whose scrollable body passes a [scrollBehavior].
 *
 * Icon tone tracks the bar's transparency, as it does on [BingeTopBar]: an opaque bar keeps plain
 * [IconButtonTone.Default] glyphs (the M3-idiomatic treatment), while [Color.Transparent] promotes
 * nav + [actions] to [IconButtonTone.Glass], for a bar the content scrolls under. Actions read that
 * tone from [LocalTopBarActionTone], provided here around the [actions] slot, so callers don't
 * hardcode it.
 *
 * A transparent bar draws nothing behind itself, so [scrimFraction] puts a [TopBarScrim] behind it —
 * ramp it on the same [scrollBehavior]'s `collapsedFraction`, or the content passing under runs
 * through the title. A screen whose bar and header need *one* scrim across both (the chip-filtered
 * screens) leaves this at 0 and scrims the header instead.
 *
 * [scrimColor]/[scrimForegroundColor] default to a theme-following pair
 * (`MaterialTheme.colorScheme.background`/`onBackground`), exactly as on [BingeTopBar] — every
 * current caller scrims a plain, predictable surface, where that pair reads as one continuous
 * surface rather than a scrim landing on top of it. Override to [BingeTheme.colors.scrim]/
 * [BingeTheme.colors.onScrim] (always black/white, regardless of theme) only for a bar floating over
 * genuinely unpredictable content — a hero image — same as [DetailOverlayTopBar] does over its own.
 * [foregroundScrimFraction] is the ramp that ties the title's colour to whichever pair is in play,
 * defaulting to the bar's own [scrimFraction].
 *
 * Back and [actions] carry their own [ExpressiveIconButton.glassBackgroundAlpha] wash at rest,
 * fading out over [foregroundScrimFraction] rather than [scrimFraction] — the two default to the
 * same value, but a caller whose own scrim comes from elsewhere (the gallery's chip-filtered header,
 * which sets [foregroundScrimFraction] alone and leaves this bar's own [TopBarScrim] off) needs the
 * icon backing to hand off to *that* scrim, not to a [scrimFraction] that never moves here. [actions]
 * reads the resolved value as the lambda's argument, to pass along to icons of its own, exactly as
 * [DetailOverlayTopBar] does.
 *
 * [edgeInset] gives back and [actions] the same breathing room from both edges that
 * [DetailOverlayTopBar]'s own `horizontalInset` gives its row, rather than M3's tighter built-in
 * edge padding.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BingeMediumTopBar(
    title: String,
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null,
    scrollBehavior: TopAppBarScrollBehavior? = null,
    containerColor: Color = Color.Unspecified,
    scrimFraction: Float = 0f,
    foregroundScrimFraction: Float = scrimFraction,
    scrimColor: Color = MaterialTheme.colorScheme.background,
    scrimForegroundColor: Color = MaterialTheme.colorScheme.onBackground,
    edgeInset: Dp = dimensionResource(R.dimen.medium_top_bar_edge_inset),
    actions: @Composable RowScope.(glassBackgroundAlpha: Float) -> Unit = {},
) {
    val transparent = containerColor == Color.Transparent
    val iconTone = if (transparent) IconButtonTone.Glass else IconButtonTone.Default
    val titleColor = scrimmedTitleColor(transparent, foregroundScrimFraction, scrimForegroundColor)
    // The bar's own scrim takes over legibility as it fades in, so each icon's individual backing
    // hands off to it rather than the two stacking.
    val glassBackgroundAlpha = 1f - foregroundScrimFraction
    // The expanded large title sits on its own (second) row at the 16dp content margin, with no nav
    // circle beside it; only the collapsed title animates up next to the circle. A flat inset would
    // wrongly indent the expanded title, so ramp the start padding 0 -> target as the bar collapses.
    val collapsedFraction = scrollBehavior?.state?.collapsedFraction ?: 0f
    val titleStartInset =
        dimensionResource(R.dimen.medium_top_bar_collapsed_title_inset) * collapsedFraction
    Box {
        TopBarScrim(scrimFraction, scrimColor = scrimColor)
        MediumTopAppBar(
            title = {
                Text(
                    title,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = titleColor,
                    modifier = Modifier.padding(start = titleStartInset),
                )
            },
            modifier = modifier,
            navigationIcon = {
                if (onBack != null) {
                    Box(modifier = Modifier.padding(start = edgeInset)) {
                        ExpressiveIconButton(
                            onClick = onBack,
                            icon = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.cd_navigate_back),
                            tint = if (transparent) scrimForegroundColor else LocalContentColor.current,
                            tone = iconTone,
                            size = dimensionResource(R.dimen.top_bar_icon_size),
                            glassBackgroundAlpha = glassBackgroundAlpha,
                        )
                    }
                }
            },
            actions = {
                CompositionLocalProvider(LocalTopBarActionTone provides iconTone) {
                    Row(
                        modifier = Modifier.padding(end = edgeInset),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        actions(glassBackgroundAlpha)
                    }
                }
            },
            // Only an explicit container overrides M3's medium-bar defaults. Routing an unspecified one
            // through bingeTopBarColors would restyle every existing caller: it resolves to `background`
            // and flattens the scrolled state, which is BingeTopBar's decision to make, not this bar's.
            colors =
                if (containerColor.isSpecified) {
                    TopAppBarDefaults.mediumTopAppBarColors(
                        containerColor = containerColor,
                        scrolledContainerColor = containerColor,
                    )
                } else {
                    TopAppBarDefaults.mediumTopAppBarColors()
                },
            scrollBehavior = scrollBehavior,
        )
    }
}

/**
 * A transparent [BingeMediumTopBar] over a sample coloured background, with [IconButtonTone.Glass]
 * nav/action buttons — mirrors [TransparentBingeTopBarSample]. Lives in main so the screenshot test
 * can render it. [scrimFraction] exercises both the [TopBarScrim] this bar draws behind itself and
 * the title's [scrimmedTitleColor] ramp, since it also drives [BingeMediumTopBar]'s
 * `foregroundScrimFraction` default.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun TransparentBingeMediumTopBarSample(scrimFraction: Float = 0f) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primaryContainer),
    ) {
        BingeMediumTopBar(
            title = "Popular Movies",
            onBack = {},
            containerColor = Color.Transparent,
            scrimFraction = scrimFraction,
            scrimColor = BingeTheme.colors.scrim,
            scrimForegroundColor = BingeTheme.colors.onScrim,
        ) { glassBackgroundAlpha ->
            ExpressiveIconButton(
                onClick = {},
                icon = Icons.Filled.Share,
                contentDescription = null,
                tint = BingeTheme.colors.onScrim,
                tone = LocalTopBarActionTone.current,
                size = dimensionResource(R.dimen.top_bar_icon_size),
                glassBackgroundAlpha = glassBackgroundAlpha,
            )
        }
    }
}
