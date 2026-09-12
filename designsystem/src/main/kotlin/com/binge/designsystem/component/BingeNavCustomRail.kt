package com.binge.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.unit.Dp
import com.binge.designsystem.LocalNavOverlayInsets
import com.binge.designsystem.R
import com.binge.designsystem.startHorizontalGradient

/** The glass ramp's densest stop, at the panel edge — near-solid under the glyphs, whatever is behind them. */
private const val RAIL_SCRIM_ALPHA = 0.94f

/**
 * Held through [RAIL_SCRIM_HOLD_FRACTION] of the rail's width, then falling to clear at its inner edge.
 * The hold has to outrun the labels: the widest ends ~76dp out, and a fraction that fell short of that
 * dropped a label's inner edge onto bare artwork while its outer edge stayed on near-solid glass —
 * which reads as the rail being thinner on one side. The alternative was a wider rail, giving the fall-off
 * more room; the width is also the inset content clears by, so the runway is squeezed instead.
 */
private const val RAIL_SCRIM_HOLD_ALPHA = 0.86f
private const val RAIL_SCRIM_HOLD_FRACTION = 0.85f

/**
 * The bespoke expanded rail (tablet landscape / unfolded foldable). Every destination — Account
 * included — sits in one centred group; item chrome (icon/avatar + label + selected pill) matches
 * the floating bar via [NavSuiteItemIcon].
 *
 * Material allows the rail's destinations to be aligned as a group to the top, centre, or bottom.
 * Centred, with nothing pinned: the group stays near the vertical middle of a tall tablet window
 * instead of being stranded against the top edge, and Account keeps its list position rather than
 * being separated from the destinations it sits among. It keeps the larger avatar, though — at this
 * width the bar's 26dp reads undersized.
 *
 * The column scrolls, so a short expanded window (small unfolded foldable, or a height-limited
 * >=840dp split pane) pushes nothing off-screen.
 *
 * The rail **overlays** [content] rather than reserving width beside it, so a hub's artwork reaches
 * the panel edge and passes under the glass. What must stay clear opts out through the start inset
 * published here — see [com.binge.designsystem.navOverlayStart]. The scrim is drawn in the
 * *background* colour rather than a neutral black, which is what lets it be unconditional: over a
 * plain surface it is indistinguishable from the background, and only over artwork does it read as
 * glass. The TV rail, whose scrim is neutral, needs a solid/glass state machine so a backdrop
 * crossfade doesn't play out inside the rail; this one does not.
 *
 * The published inset also carries the window's bottom safe area, which the rail itself doesn't
 * occupy. At expanded width this rail *is* the shell, and the screens inside it run at zero window
 * insets so their content can bleed — so without this nothing holds a scrollable's last row off the
 * gesture bar. The floating bar folds the same safe area into its own strip.
 *
 * The shell decides when to use this (the `binge_nav_rail_expanded` bool); the rail itself is
 * presentation-only and takes the same stateless [items]/[selectedKey]/[onSelect] contract.
 */
@Composable
internal fun BingeNavCustomRail(
    items: List<BingeNavSuiteItem>,
    selectedKey: Any?,
    onSelect: (Any) -> Unit,
    content: @Composable () -> Unit,
) {
    val railWidth = dimensionResource(R.dimen.nav_custom_rail_width)
    val safeInsets = WindowInsets.safeDrawing.asPaddingValues()
    val safeStart = safeInsets.calculateStartPadding(LocalLayoutDirection.current)
    Box(Modifier.fillMaxSize()) {
        CompositionLocalProvider(
            LocalNavOverlayInsets provides
                PaddingValues(start = railWidth + safeStart, bottom = safeInsets.calculateBottomPadding()),
        ) {
            content()
        }
        val background = MaterialTheme.colorScheme.background
        NavigationRail(
            containerColor = Color.Transparent,
            // Vertical only. Left to its default the rail would also inset its content by the start safe area,
            // taking that space out of the item column while the scrim still spanned it — the column would sit
            // off-centre by the cutout's width. The start inset goes on the whole rail below, so the column stays [railWidth] and centred.
            windowInsets = WindowInsets.safeDrawing.only(WindowInsetsSides.Vertical),
            modifier = Modifier
                .width(railWidth + safeStart)
                .background(
                    startHorizontalGradient(
                        0f to background.copy(alpha = RAIL_SCRIM_ALPHA),
                        RAIL_SCRIM_HOLD_FRACTION to background.copy(alpha = RAIL_SCRIM_HOLD_ALPHA),
                        1f to Color.Transparent,
                    ),
                ).windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Start)),
        ) {
            val accountAvatarSize = dimensionResource(R.dimen.nav_rail_account_avatar_size)
            val itemAvatarSize = dimensionResource(R.dimen.nav_item_avatar_size)
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(
                    dimensionResource(R.dimen.padding_sm),
                    Alignment.CenterVertically,
                ),
            ) {
                items.forEach { tab ->
                    NavRailItem(
                        tab = tab,
                        selected = selectedKey == tab.key,
                        onSelect = onSelect,
                        avatarSize = if (tab.isAccount) accountAvatarSize else itemAvatarSize,
                        // NavigationRailItem sizes itself from its widest content (80dp is a *minimum*),
                        // so "TV shows" and "Search" would claim different widths and centre on different
                        // gutters. Filling the column pins every row to the rail's width instead.
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
    }
}

@Composable
private fun NavRailItem(
    tab: BingeNavSuiteItem,
    selected: Boolean,
    onSelect: (Any) -> Unit,
    modifier: Modifier = Modifier,
    avatarSize: Dp = dimensionResource(R.dimen.nav_item_avatar_size),
) {
    NavigationRailItem(
        selected = selected,
        onClick = { onSelect(tab.key) },
        icon = { NavSuiteItemIcon(tab, avatarSize = avatarSize) },
        label = { Text(tab.label) },
        modifier = modifier,
    )
}
