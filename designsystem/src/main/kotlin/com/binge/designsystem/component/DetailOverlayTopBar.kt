package com.binge.designsystem.component

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import com.binge.designsystem.R
import com.binge.designsystem.theme.BingeTheme

/**
 * Transparent overlay top bar for a [DetailHero]-backed screen. The hero draws no chrome
 * (pass `showChrome = false`) and this bar floats over it, keeping back + [actions] reachable.
 *
 * The bar stays clear over most of the hero and brings in a [TopBarScrim] (with the [title] fading
 * in) only near the hero's tail, so content passes under a legible bar rather than stopping at it.
 * The ramp spans a fixed band ([R.dimen.detail_overlay_bar_fade_band], ~one bar height) ending as the
 * hero clears, tracking the hero edge rather than the whole scroll; pinned so back stays reachable at
 * any offset. Both the scrim and the title that rides its fade follow the theme's own background/
 * on-background pair rather than the black-always default, since by the time this ramp engages the
 * hero has already faded to the app's own background underneath it — unlike [HeroScrim] itself,
 * which stays black-always because it sits directly over the raw, unpredictable backdrop with no
 * compensating scrim of its own. Back and [actions] carry their
 * own [ExpressiveIconButton.glassBackgroundAlpha] wash at rest, fading out over the same ramp as the
 * bar's own scrim fades in — [actions] reads that value as its lambda argument, to pass along to icons
 * of its own. The back button's own tint travels the same ramp, from the always-legible
 * [BingeTheme.colors.onScrim] (paired with the Glass wash while it's still opaque) to
 * `MaterialTheme.colorScheme.onBackground` (matching the scrim it hands off to); an icon in [actions]
 * needs the same lerp — `lerp(BingeTheme.colors.onScrim, MaterialTheme.colorScheme.onBackground, 1f -
 * glassBackgroundAlpha)` — rather than a tint fixed at `onScrim`, or it goes illegible in light theme
 * once the bar's own scrim has taken over.
 *
 * Call it inside the [androidx.compose.foundation.layout.Box] / `BoxWithConstraints` that also
 * hosts the scrolling content, so it top-aligns over the same [scrollState].
 *
 * Both headers it serves are full-bleed, so the bar's [horizontalInset] is what lines its controls
 * up with the copy beneath them: the compact [DetailHero] gutters its copy at `padding_m`, while
 * [DetailCinematicHeader] uses the wider `detail_cinematic_header_padding`. Pass the inset the
 * header under it uses, or back and share float outboard of the content they belong to.
 */
@Composable
fun BoxScope.DetailOverlayTopBar(
    title: String,
    scrollState: ScrollState,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    heroHeight: Dp = dimensionResource(R.dimen.detail_hero_height),
    horizontalInset: Dp = dimensionResource(R.dimen.padding_m),
    actions: @Composable RowScope.(glassBackgroundAlpha: Float) -> Unit = {},
) {
    DetailOverlayTopBar(
        title = title,
        scrollOffsetPx = { scrollState.value.toFloat() },
        onBack = onBack,
        modifier = modifier,
        heroHeight = heroHeight,
        horizontalInset = horizontalInset,
        actions = actions,
    )
}

/**
 * The same bar driven by a raw scrolled distance rather than a [ScrollState], for a screen whose
 * header is the first item of a `LazyColumn` — `CollectionScreen`, which keeps its list lazy and so
 * has a `LazyListState` instead. [scrollOffsetPx] is how far the top of the header has travelled
 * above the viewport; read it as a lambda so the fade recomposes on scroll without the caller
 * hoisting a state object this component would only read one number from.
 */
@Composable
fun BoxScope.DetailOverlayTopBar(
    title: String,
    scrollOffsetPx: () -> Float,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    heroHeight: Dp = dimensionResource(R.dimen.detail_hero_height),
    horizontalInset: Dp = dimensionResource(R.dimen.padding_m),
    actions: @Composable RowScope.(glassBackgroundAlpha: Float) -> Unit = {},
) {
    val density = LocalDensity.current
    val heroHeightPx = with(density) { heroHeight.toPx() }
    val bandPx = with(density) { dimensionResource(R.dimen.detail_overlay_bar_fade_band).toPx() }
    val fadeStart = heroHeightPx - bandPx
    val progress = ((scrollOffsetPx() - fadeStart) / bandPx).coerceIn(0f, 1f)
    // The bar's own TopBarScrim takes over legibility as it fades in, so each icon's individual
    // backing hands off to it rather than the two stacking.
    val glassBackgroundAlpha = 1f - progress
    // Travels with the same progress: onScrim (white) is only guaranteed-legible while the Glass
    // backing is still opaque underneath it; by progress 1 that backing is gone and the theme-
    // following TopBarScrim is what's behind the icon instead, so the tint has to land on
    // onBackground to match it, or a light theme leaves a white icon on a light bar.
    val iconTint = lerp(BingeTheme.colors.onScrim, MaterialTheme.colorScheme.onBackground, progress)
    Box(
        modifier = modifier
            .align(Alignment.TopStart)
            .fillMaxWidth(),
    ) {
        TopBarScrim(progress, scrimColor = MaterialTheme.colorScheme.background)
        Row(
            modifier = Modifier
                .statusBarsPadding()
                .padding(
                    horizontal = horizontalInset,
                    vertical = dimensionResource(R.dimen.padding_s),
                ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_s)),
        ) {
            ExpressiveIconButton(
                onClick = onBack,
                icon = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = stringResource(R.string.cd_navigate_back),
                tint = iconTint,
                tone = IconButtonTone.Glass,
                size = dimensionResource(R.dimen.top_bar_icon_size),
                glassBackgroundAlpha = glassBackgroundAlpha,
            )
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = progress),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
            actions(glassBackgroundAlpha)
        }
    }
}
