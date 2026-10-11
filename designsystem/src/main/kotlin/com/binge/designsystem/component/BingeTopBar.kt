package com.binge.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.union
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import com.binge.designsystem.R
import com.binge.designsystem.clearNavRail
import com.binge.designsystem.hasPaneBeside
import com.binge.designsystem.navOverlayStart
import com.binge.designsystem.paneSideInsets
import com.binge.designsystem.resolvedContentPadding
import com.binge.designsystem.theme.BingeTheme

/**
 * Binge's standard top bar: the single-row, non-collapsing title variant. For screens whose chrome
 * stays a fixed standard height; pass a [scrollBehavior] to tie the bar's surface to the body's
 * scroll.
 *
 * Icon tone tracks the bar's transparency. An opaque bar uses plain [IconButtonTone.Default] glyphs
 * (the bar surface already separates them from content); a transparent bar promotes nav + [actions]
 * to [IconButtonTone.Glass] (a scrim-backed circle) where the container is load-bearing over imagery.
 * Actions read that tone from [LocalTopBarActionTone], provided around the [actions] slot.
 *
 * [containerColor] defaults to [Color.Unspecified], which resolves to the app background rather than
 * M3's `surface` — the bar, the screen body (a Scaffold is `background` too) and the expanded nav
 * rail beside it then read as one continuous surface instead of stepping at each seam. Pass
 * [Color.Transparent] to let a hero screen's imagery show through: the override drives both resting
 * and scrolled container, so the bar stays transparent on scroll and nav + actions go Glass.
 *
 * A transparent bar draws nothing behind itself, so [scrimFraction] puts a [TopBarScrim] behind it.
 * Drive it with how much of the bar the scrolled content overlaps: the same [scrollBehavior]'s
 * `collapsedFraction` for a collapsing behaviour (`exitUntilCollapsed`), and its `overlappedFraction`
 * for one that hides the bar (`enterAlwaysScrollBehavior`) or pins it. On an enter-always bar
 * `collapsedFraction` measures how far the bar has slid off screen, the inverse of what the scrim
 * needs, so the scrim would vanish exactly when the bar is showing over content. A screen whose
 * bar and header want *one* scrim across both (Gallery) leaves this at 0 and scrims the header
 * instead. That scrim tapers past this bar's own bottom edge into the content scrolling under it,
 * rather than cutting off at the bar's boundary (#94).
 *
 * [scrimColor]/[scrimForegroundColor] default to a theme-following pair
 * (`MaterialTheme.colorScheme.background`/`onBackground`) — every current caller scrims a plain,
 * predictable surface (a grid or list on the app's own background), where that pair reads as one
 * continuous surface rather than a scrim landing on top of it. Override to [BingeTheme.colors.scrim]/
 * [BingeTheme.colors.onScrim] (always black/white, regardless of theme) only for a bar floating over
 * genuinely unpredictable content — a hero image that could be any colour — where a caller needs a
 * legibility guarantee the theme's own colours can't make. [foregroundScrimFraction] is the ramp
 * that ties the title's colour to whichever pair is in play, defaulting to the bar's own
 * [scrimFraction]; override it only where something *behind* the bar supplies the scrim instead, so
 * the bar draws none itself but its title still sits on one (the gallery's chip overlay).
 *
 * Back and [actions] carry their own [ExpressiveIconButton.glassBackgroundAlpha] wash at rest,
 * fading out over [foregroundScrimFraction] rather than [scrimFraction] — the two default to the
 * same value, exactly as on [BingeMediumTopBar]. [actions] reads the resolved value as the lambda's
 * argument, to pass along to icons of its own. Back's own tint travels the same ramp, from
 * [BingeTheme.colors.onScrim] to [scrimForegroundColor] as [foregroundScrimFraction] goes to 1 — a
 * tint fixed at `onScrim` has no guaranteed contrast left once the backing (its other half of the
 * guarantee) has faded away against a theme-following [scrimForegroundColor]; an [actions] icon
 * supplying its own `tint` reads [LocalTopBarActionTint] rather than re-deriving that lerp, exactly
 * as it reads [LocalTopBarActionTone] for tone.
 *
 * [edgeInset] is the same symmetric leading/trailing inset [BingeMediumTopBar] carries, matching
 * [DetailOverlayTopBar]'s own `horizontalInset` so a transparent bar's controls float the same
 * distance from the edge everywhere.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BingeTopBar(
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
    // hands off to it rather than the two stacking. The icon's own tint has to travel with that
    // handoff too, or a hardcoded onScrim goes illegible once the backing (its other half of the
    // contrast guarantee) has faded away against a theme-following scrimForegroundColor.
    val glassBackgroundAlpha = 1f - foregroundScrimFraction
    val iconTint = lerp(BingeTheme.colors.onScrim, scrimForegroundColor, foregroundScrimFraction)
    val titleModifier =
        if (onBack == null && hasPaneBeside()) {
            // Beside another pane, the title starts where the pane's content does, as the two-row bar's does: M3 puts it
            // pane_top_bar_title_inset in, and the content may start nearer the shared edge than that.
            val contentStart = resolvedContentPadding().calculateStartPadding(LocalLayoutDirection.current) + navOverlayStart()
            Modifier.offset(x = contentStart - dimensionResource(R.dimen.pane_top_bar_title_inset))
        } else {
            // Clears the filled circular nav container, whose visible edge sits ~12dp closer to
            // the title than a bare glyph would; restores a standard-spacing gap.
            Modifier.padding(start = dimensionResource(R.dimen.padding_s))
        }
    Box {
        TopBarScrim(
            scrimFraction,
            scrimColor = scrimColor,
            tailHeight = dimensionResource(R.dimen.top_bar_scrim_tail_height),
        )
        TopAppBar(
            title = {
                Text(
                    title,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = titleColor,
                    modifier = titleModifier,
                )
            },
            modifier = modifier.clearNavRail(),
            navigationIcon = {
                if (onBack != null) {
                    Box(modifier = Modifier.padding(start = edgeInset)) {
                        ExpressiveIconButton(
                            onClick = onBack,
                            icon = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.cd_navigate_back),
                            tint = if (transparent) iconTint else LocalContentColor.current,
                            tone = iconTone,
                            size = dimensionResource(R.dimen.top_bar_icon_size),
                            glassBackgroundAlpha = glassBackgroundAlpha,
                        )
                    }
                }
            },
            actions = {
                CompositionLocalProvider(
                    LocalTopBarActionTone provides iconTone,
                    LocalTopBarActionTint provides if (transparent) iconTint else LocalContentColor.current,
                ) {
                    Row(
                        modifier = Modifier.padding(end = edgeInset),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        actions(glassBackgroundAlpha)
                    }
                }
            },
            // The sides the scaffold body clears, cutout included, so the title and actions line up with it.
            windowInsets = TopAppBarDefaults.windowInsets.only(WindowInsetsSides.Top).union(paneSideInsets()),
            colors = bingeTopBarColors(containerColor),
            scrollBehavior = scrollBehavior,
        )
    }
}

/**
 * M3 [TopAppBarColors] for a Binge top bar. [containerColor] drives both the resting and the scrolled
 * container, so a bar holds its colour as content scrolls under it; [Color.Unspecified] resolves to
 * the app background. Pinning the scrolled container matters as much as the resting one here — M3's
 * default tints to `surfaceContainer` on scroll, which would re-open the seam against the nav rail
 * the moment the user scrolls.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun bingeTopBarColors(containerColor: Color): TopAppBarColors {
    val resolved = containerColor.takeOrElse { MaterialTheme.colorScheme.background }
    return TopAppBarDefaults.topAppBarColors(
        containerColor = resolved,
        scrolledContainerColor = resolved,
    )
}

/**
 * A transparent [BingeTopBar] over a sample coloured background, with [IconButtonTone.Glass]
 * nav/action buttons — the hero-screen treatment. Lives in main so the screenshot test can render
 * it; the bar's nav button derives its Glass tone from the transparent container. [scrimFraction]
 * exercises [scrimmedTitleColor]'s ramp on the title this sample renders.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun TransparentBingeTopBarSample(scrimFraction: Float = 0f) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primaryContainer),
    ) {
        BingeTopBar(
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
                tint = LocalTopBarActionTint.current,
                tone = LocalTopBarActionTone.current,
                size = dimensionResource(R.dimen.top_bar_icon_size),
                glassBackgroundAlpha = glassBackgroundAlpha,
            )
        }
    }
}
