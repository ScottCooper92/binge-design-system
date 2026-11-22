package com.binge.designsystem.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import com.binge.designsystem.R
import com.binge.designsystem.theme.BingeTheme

/**
 * What every top bar shares: how its navigation icon, its actions and its title are toned over what is behind them, and the scrim that backs
 * them. The three Material hosts ([BingeTopBar], [BingeMediumTopBar] and the two-row bar behind [BingePaneTopBar] and [BingeLargeTopBar]) draw it
 * through [BarScrimHost], [BarNavigationIcon] and [BarActions], so the four cannot drift apart.
 *
 * A bar on a transparent container is the hero treatment: its icons sit on a glass backing and a light tint. The bar's own scrim takes over
 * legibility as it fades in, so each icon's backing hands off to it rather than the two stacking ([glassBackgroundAlpha]). The icon's own tint travels with
 * that handoff too ([iconTint]), or a hardcoded onScrim goes illegible once the backing, its other half of the contrast guarantee, has faded away
 * against a theme-following [scrimForegroundColor].
 */
@Immutable
internal class BarChrome(
    val transparent: Boolean,
    val iconTone: IconButtonTone,
    val iconTint: Color,
    val glassBackgroundAlpha: Float,
    val titleColor: Color,
    val edgeInset: Dp,
)

/** The [BarChrome] of a bar on [containerColor], whose foreground has handed [foregroundScrimFraction] of the way to [scrimForegroundColor]. */
@Composable
internal fun barChrome(
    containerColor: Color,
    foregroundScrimFraction: Float,
    scrimForegroundColor: Color,
    edgeInset: Dp = dimensionResource(R.dimen.medium_top_bar_edge_inset),
): BarChrome {
    val transparent = containerColor == Color.Transparent
    return BarChrome(
        transparent = transparent,
        iconTone = if (transparent) IconButtonTone.Glass else IconButtonTone.Default,
        iconTint = lerp(BingeTheme.colors.onScrim, scrimForegroundColor, foregroundScrimFraction),
        glassBackgroundAlpha = 1f - foregroundScrimFraction,
        titleColor = scrimmedTitleColor(transparent, foregroundScrimFraction, scrimForegroundColor),
        edgeInset = edgeInset,
    )
}

/** The bar's [content] over its own [TopBarScrim], which fades in at [scrimFraction]. */
@Composable
internal fun BarScrimHost(
    scrimFraction: Float,
    scrimColor: Color,
    content: @Composable () -> Unit,
) {
    Box {
        TopBarScrim(
            scrimFraction,
            scrimColor = scrimColor,
            tailHeight = dimensionResource(R.dimen.top_bar_scrim_tail_height),
        )
        content()
    }
}

/** The back button, when there is one to go back to, [BarChrome.edgeInset] in from the bar's start. Read inside the bar's own slot, for its content colour. */
@Composable
internal fun BarNavigationIcon(chrome: BarChrome, onBack: (() -> Unit)?) {
    if (onBack == null) return
    Box(modifier = Modifier.padding(start = chrome.edgeInset)) {
        ExpressiveIconButton(
            onClick = onBack,
            icon = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = stringResource(R.string.cd_navigate_back),
            tint = if (chrome.transparent) chrome.iconTint else LocalContentColor.current,
            tone = chrome.iconTone,
            size = dimensionResource(R.dimen.top_bar_icon_size),
            glassBackgroundAlpha = chrome.glassBackgroundAlpha,
        )
    }
}

/** The bar's [actions], toned as its back button is and [BarChrome.edgeInset] in from its end. Read inside the bar's own slot, for its content colour. */
@Composable
internal fun BarActions(chrome: BarChrome, actions: @Composable RowScope.(glassBackgroundAlpha: Float) -> Unit) {
    CompositionLocalProvider(
        LocalTopBarActionTone provides chrome.iconTone,
        LocalTopBarActionTint provides if (chrome.transparent) chrome.iconTint else LocalContentColor.current,
    ) {
        Row(
            modifier = Modifier.padding(end = chrome.edgeInset),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            actions(chrome.glassBackgroundAlpha)
        }
    }
}
