package com.binge.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.isSpecified
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
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
 * The scrim tone is black in both themes, so the title has to travel with it or light theme's
 * dark-on-light title goes illegible exactly as the scrim lands — the rule [TopBarScrim]'s own KDoc
 * states. [foregroundScrimFraction] is that ramp, defaulting to the bar's own [scrimFraction],
 * exactly as on [BingeTopBar].
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
    actions: @Composable RowScope.() -> Unit = {},
) {
    val transparent = containerColor == Color.Transparent
    val iconTone = if (transparent) IconButtonTone.Glass else IconButtonTone.Default
    val titleColor = scrimmedTitleColor(transparent, foregroundScrimFraction)
    // The expanded large title sits on its own (second) row at the 16dp content margin, with no nav
    // circle beside it; only the collapsed title animates up next to the circle. A flat inset would
    // wrongly indent the expanded title, so ramp the start padding 0 -> target as the bar collapses.
    val collapsedFraction = scrollBehavior?.state?.collapsedFraction ?: 0f
    val titleStartInset =
        dimensionResource(R.dimen.medium_top_bar_collapsed_title_inset) * collapsedFraction
    Box {
        TopBarScrim(scrimFraction)
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
                    ExpressiveIconButton(
                        onClick = onBack,
                        icon = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.cd_navigate_back),
                        tint = if (transparent) BingeTheme.colors.onScrim else LocalContentColor.current,
                        tone = iconTone,
                        size = dimensionResource(R.dimen.top_bar_icon_size),
                    )
                }
            },
            actions = {
                CompositionLocalProvider(LocalTopBarActionTone provides iconTone) {
                    actions()
                }
            },
            // Only an explicit container overrides M3's medium-bar defaults. Routing an unspecified one
            // through bingeTopBarColors would restyle every existing caller: it resolves to `background`
            // and flattens the scrolled state, which is #2033's decision for BingeTopBar, not this bar's.
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
            actions = {
                ExpressiveIconButton(
                    onClick = {},
                    icon = Icons.Filled.Share,
                    contentDescription = null,
                    tint = BingeTheme.colors.onScrim,
                    tone = LocalTopBarActionTone.current,
                    size = dimensionResource(R.dimen.top_bar_icon_size),
                )
            },
        )
    }
}
