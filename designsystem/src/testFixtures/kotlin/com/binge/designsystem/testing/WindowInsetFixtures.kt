package com.binge.designsystem.testing

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import androidx.core.graphics.Insets
import androidx.core.view.WindowInsetsCompat

/** Gives the window a navigation bar [width] wide on the start edge, as a phone in landscape has. */
@Composable
fun WithStartNavigationBar(width: Dp, content: @Composable () -> Unit) =
    WithWindowInsets({
        WindowInsetsCompat.Builder().setInsets(WindowInsetsCompat.Type.navigationBars(), Insets.of(width.roundToPx(), 0, 0, 0)).build()
    }, content)

/** Gives the window a display cutout [width] wide on the start edge. */
@Composable
fun WithStartCutout(width: Dp, content: @Composable () -> Unit) =
    WithWindowInsets({
        WindowInsetsCompat.Builder().setInsets(WindowInsetsCompat.Type.displayCutout(), Insets.of(width.roundToPx(), 0, 0, 0)).build()
    }, content)

/** Opens a keyboard [height] tall over the window, answering every insets dispatch at the parent. */
@Composable
fun WithKeyboard(height: Dp, content: @Composable () -> Unit) =
    WithWindowInsets(
        {
            WindowInsetsCompat
                .Builder()
                .setInsets(WindowInsetsCompat.Type.ime(), Insets.of(0, 0, 0, height.roundToPx()))
                .setVisible(WindowInsetsCompat.Type.ime(), true)
                .build()
        },
        content,
    )

/**
 * A keyboard [height] tall over a bottom navigation bar [navigationBar] tall. The keyboard covers the bar, so it is at least as
 * tall as the bar when it is open, and a [height] of zero is closed.
 */
@Composable
fun WithKeyboard(
    height: Dp,
    navigationBar: Dp,
    content: @Composable () -> Unit,
) = WithWindowInsets(
    {
        val px = height.roundToPx()
        val navPx = navigationBar.roundToPx()
        WindowInsetsCompat
            .Builder()
            .setInsets(WindowInsetsCompat.Type.navigationBars(), Insets.of(0, 0, 0, navPx))
            .setVisible(WindowInsetsCompat.Type.navigationBars(), navPx > 0)
            .setInsets(WindowInsetsCompat.Type.ime(), Insets.of(0, 0, 0, if (px > 0) maxOf(px, navPx) else 0))
            .setVisible(WindowInsetsCompat.Type.ime(), px > 0)
            .build()
    },
    content,
)
