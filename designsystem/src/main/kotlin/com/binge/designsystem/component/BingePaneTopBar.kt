package com.binge.designsystem.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import com.binge.designsystem.R
import com.binge.designsystem.hasPaneBeside
import com.binge.designsystem.navOverlayStart
import com.binge.designsystem.resolvedContentPadding
import com.binge.designsystem.theme.BingeTheme

/**
 * The scroll behaviour to pair with [BingePaneTopBar]: collapse-and-stay beside another pane, where the
 * bar carries a large title that should settle into the small one, and M3's enter-always everywhere
 * else, matching what a [BingeTopBar] screen already uses. The two change together with the bar, so a
 * screen reads both from here rather than choosing one that only suits half the layouts.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun rememberPaneTopBarScrollBehavior(): TopAppBarScrollBehavior =
    if (hasPaneBeside()) {
        TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    } else {
        TopAppBarDefaults.enterAlwaysScrollBehavior()
    }

/**
 * A screen's top bar that knows which pane it is in. Alone in the window it is exactly [BingeTopBar].
 * Beside another pane it becomes a two-row bar with a `displaySmall` title on the content's own start
 * inset, so when both panes use it they open on the same heading at the same height, and each
 * collapses to the small title as that pane scrolls. Pair it with [rememberPaneTopBarScrollBehavior].
 *
 * The parameters are [BingeTopBar]'s, and mean the same in both forms, so a screen swaps the call and
 * nothing else.
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
        BingeTopBar(
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
        return
    }
    TwoRowPaneTopBar(
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
 * [BingePaneTopBar]'s two-row form. Icon tone, tint and scrim hand-off are [BingeTopBar]'s.
 *
 * M3 starts both titles [R.dimen.pane_top_bar_title_inset] in from the bar's edge; they are shifted
 * onto the content's own start — [resolvedContentPadding] plus any nav rail overlaying the pane — so a
 * title lines up with the rows under it. With a back button the collapsed title sits past it instead.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun TwoRowPaneTopBar(
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
) {
    val transparent = containerColor == Color.Transparent
    val iconTone = if (transparent) IconButtonTone.Glass else IconButtonTone.Default
    val titleColor = scrimmedTitleColor(transparent, foregroundScrimFraction, scrimForegroundColor)
    val glassBackgroundAlpha = 1f - foregroundScrimFraction
    val iconTint = lerp(BingeTheme.colors.onScrim, scrimForegroundColor, foregroundScrimFraction)
    val edgeInset = dimensionResource(R.dimen.medium_top_bar_edge_inset)
    val contentStart =
        resolvedContentPadding().calculateStartPadding(LocalLayoutDirection.current) + navOverlayStart()
    val onContentInset = contentStart - dimensionResource(R.dimen.pane_top_bar_title_inset)
    val besideBackButton = dimensionResource(R.dimen.padding_s)
    Box {
        TopBarScrim(
            scrimFraction,
            scrimColor = scrimColor,
            tailHeight = dimensionResource(R.dimen.top_bar_scrim_tail_height),
        )
        TwoRowsTopAppBar(
            title = { expanded ->
                Text(
                    title,
                    style = if (expanded) MaterialTheme.typography.displaySmall else LocalTextStyle.current,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = titleColor,
                    modifier =
                        if (expanded || onBack == null) {
                            Modifier.offset(x = onContentInset)
                        } else {
                            Modifier.padding(start = besideBackButton)
                        },
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
            expandedHeight = dimensionResource(R.dimen.pane_top_bar_expanded_height),
            colors = bingeTopBarColors(containerColor),
            scrollBehavior = scrollBehavior,
        )
    }
}
