package com.binge.designsystem.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.union
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.TwoRowsTopAppBar
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
 * The scroll behaviour to pair with [BingePaneTopBar]: exit-until-collapsed everywhere, since both of
 * its forms now carry a collapsing title — the two-row title beside another pane, [BingeMediumTopBar]'s
 * own alone. A screen reads it from here rather than choosing one that only suits half the layouts.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun rememberPaneTopBarScrollBehavior(): TopAppBarScrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

/**
 * A screen's top bar that knows which pane it is in. Alone in the window it is [BingeMediumTopBar].
 * Beside another pane it becomes a two-row bar with a `displaySmall` title on the content's own start
 * inset, so when both panes use it they open on the same heading at the same height, and each
 * collapses to the small title as that pane scrolls. Pair it with [rememberPaneTopBarScrollBehavior].
 *
 * The parameters are [BingeMediumTopBar]'s, and mean the same in both forms, so a screen swaps the call
 * and nothing else.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BingePaneTopBar(
    title: String,
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null,
    scrollBehavior: TopAppBarScrollBehavior? = null,
    containerColor: Color = Color.Unspecified,
    scrimFraction: Float = 0f,
    foregroundScrimFraction: Float = scrimFraction,
    scrimColor: Color = MaterialTheme.colorScheme.background,
    scrimForegroundColor: Color = MaterialTheme.colorScheme.onBackground,
    actions: @Composable RowScope.(glassBackgroundAlpha: Float) -> Unit = {},
) {
    if (!hasPaneBeside()) {
        // BingeMediumTopBar only pins its scrolled colour to an explicit containerColor, leaving
        // Color.Unspecified to M3's own defaults (whose scrolled container tints toward
        // surfaceContainer). Resolving it here first — exactly as bingeTopBarColors does for
        // BingeTopBar — keeps this form's seam-continuity guarantee the same as the two-row one.
        BingeMediumTopBar(
            title = title,
            modifier = modifier,
            onBack = onBack,
            scrollBehavior = scrollBehavior,
            containerColor = containerColor.takeOrElse { MaterialTheme.colorScheme.background },
            scrimFraction = scrimFraction,
            foregroundScrimFraction = foregroundScrimFraction,
            scrimColor = scrimColor,
            scrimForegroundColor = scrimForegroundColor,
            actions = actions,
        )
        return
    }
    TwoRowTopBar(
        title = title,
        modifier = modifier,
        onBack = onBack,
        scrollBehavior = scrollBehavior,
        containerColor = containerColor,
        scrimFraction = scrimFraction,
        foregroundScrimFraction = foregroundScrimFraction,
        scrimColor = scrimColor,
        scrimForegroundColor = scrimForegroundColor,
        actions = actions,
    )
}

/**
 * [BingePaneTopBar]'s two-row form, and [BingeLargeTopBar]'s. Icon tone, tint and scrim hand-off are
 * [BingeTopBar]'s. A [subtitle] and a [titleTrailing] show on the expanded row only.
 *
 * M3 starts both titles [R.dimen.pane_top_bar_title_inset] in from the bar's edge; they are shifted
 * onto the content's own start — [resolvedContentPadding] plus any nav rail overlaying the pane — so a
 * title lines up with the rows under it. With a back button the collapsed title sits past it instead.
 *
 * The bar keeps M3's top inset but takes its side insets from [paneSideInsets], not M3's default.
 * The default reserves a side cutout on both edges, so a cutout beside the other pane pushed the
 * bar in from an edge that is nowhere near it.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun TwoRowTopBar(
    title: String,
    modifier: Modifier,
    onBack: (() -> Unit)?,
    scrollBehavior: TopAppBarScrollBehavior?,
    containerColor: Color,
    scrimFraction: Float,
    foregroundScrimFraction: Float,
    scrimColor: Color,
    scrimForegroundColor: Color,
    actions: @Composable RowScope.(glassBackgroundAlpha: Float) -> Unit,
    expandedHeight: Dp = dimensionResource(R.dimen.pane_top_bar_expanded_height),
    subtitle: String? = null,
    titleTrailing: (@Composable () -> Unit)? = null,
) {
    val transparent = containerColor == Color.Transparent
    val iconTone = if (transparent) IconButtonTone.Glass else IconButtonTone.Default
    val titleColor = scrimmedTitleColor(transparent, foregroundScrimFraction, scrimForegroundColor)
    val glassBackgroundAlpha = 1f - foregroundScrimFraction
    val iconTint = lerp(BingeTheme.colors.onScrim, scrimForegroundColor, foregroundScrimFraction)
    val edgeInset = dimensionResource(R.dimen.medium_top_bar_edge_inset)
    // Beside a pane the title offsets itself past the rail; alone, the bar's own insets do (topBarSideInsets).
    val contentStart =
        resolvedContentPadding().calculateStartPadding(LocalLayoutDirection.current) +
            if (hasPaneBeside()) navOverlayStart() else dimensionResource(R.dimen.zero)
    val onContentInset = contentStart - dimensionResource(R.dimen.pane_top_bar_title_inset)
    val contentEnd = resolvedContentPadding().calculateEndPadding(LocalLayoutDirection.current)
    val besideBackButton = dimensionResource(R.dimen.padding_s)
    Box {
        TopBarScrim(
            scrimFraction,
            scrimColor = scrimColor,
            tailHeight = dimensionResource(R.dimen.top_bar_scrim_tail_height),
        )
        TwoRowsTopAppBar(
            title = { expanded ->
                val titleModifier =
                    if (expanded || onBack == null) {
                        Modifier.offset(x = onContentInset)
                    } else {
                        Modifier.padding(start = besideBackButton)
                    }
                val trailing = titleTrailing.takeIf { expanded }
                if (trailing == null) {
                    Text(
                        title,
                        style = if (expanded) MaterialTheme.typography.displaySmall else LocalTextStyle.current,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = titleColor,
                        modifier = titleModifier,
                    )
                } else {
                    // Padded rather than offset, so the control ends on the content's edge too: M3 already
                    // keeps the title row its own small end inset, which the end padding takes off.
                    val zero = dimensionResource(R.dimen.zero)
                    val endInset = contentEnd - dimensionResource(R.dimen.top_bar_title_row_end_inset)
                    Row(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(start = onContentInset.coerceAtLeast(zero), end = endInset.coerceAtLeast(zero)),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            title,
                            style = MaterialTheme.typography.displaySmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            color = titleColor,
                            modifier = Modifier.weight(1f),
                        )
                        trailing()
                    }
                }
            },
            subtitle = { expanded ->
                if (expanded && subtitle != null) {
                    Text(
                        subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier =
                            Modifier
                                .offset(x = onContentInset)
                                .padding(bottom = dimensionResource(R.dimen.padding_s)),
                    )
                }
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
            expandedHeight = expandedHeight,
            windowInsets = TopAppBarDefaults.windowInsets.only(WindowInsetsSides.Top).union(paneSideInsets()),
            colors = bingeTopBarColors(containerColor),
            scrollBehavior = scrollBehavior,
        )
    }
}
