package com.binge.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.binge.designsystem.theme.BingeTheme

private const val STATUS_BAR_SCRIM_TOP_ALPHA = 0.40f
private const val STATUS_BAR_SCRIM_MID_ALPHA = 0.25f
private const val STATUS_BAR_SCRIM_MID_STOP = 0.60f

/**
 * A light, always-dark wash exactly as tall as the status bar, for a screen whose content scrolls
 * under the status bar while [DarkStatusBarEffect] holds its icons light.
 *
 * Light icons are legible over a hero's black scrim, but the content that scrolls up after it can be
 * a bright backdrop or the light theme's page. The wash keeps a dark floor under the icons whatever
 * passes beneath. It uses [BingeTheme.colors.scrim], which is black in both themes, because the icons
 * over it are light in both themes. It stops at the status bar's own edge, so the content below it
 * reads at full strength.
 *
 * Call it as the last child of the [Box] that hosts the scrolling content, so it draws on top.
 */
@Composable
fun BoxScope.StatusBarScrim(modifier: Modifier = Modifier) {
    val scrim = BingeTheme.colors.scrim
    Box(
        modifier = modifier
            .align(Alignment.TopStart)
            .fillMaxWidth()
            .windowInsetsTopHeight(WindowInsets.statusBars)
            .background(
                Brush.verticalGradient(
                    0f to scrim.copy(alpha = STATUS_BAR_SCRIM_TOP_ALPHA),
                    STATUS_BAR_SCRIM_MID_STOP to scrim.copy(alpha = STATUS_BAR_SCRIM_MID_ALPHA),
                    1f to Color.Transparent,
                ),
            ),
    )
}
