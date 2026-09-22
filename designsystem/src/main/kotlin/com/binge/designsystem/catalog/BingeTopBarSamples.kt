package com.binge.designsystem.catalog

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import com.binge.designsystem.R
import com.binge.designsystem.component.BingeTopBar
import com.binge.designsystem.component.ExpressiveIconButton
import com.binge.designsystem.component.LocalTopBarActionTint
import com.binge.designsystem.component.LocalTopBarActionTone
import com.binge.designsystem.component.TransparentBingeTopBarSample
import com.binge.designsystem.preview.ScreenshotTheme

/** Standard top bar — a back button, title, and a trailing plain search action. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BingeTopBarSample() {
    ScreenshotTheme {
        BingeTopBar(
            title = "Popular Movies",
            onBack = {},
            actions = {
                ExpressiveIconButton(
                    onClick = {},
                    icon = Icons.Filled.Search,
                    contentDescription = null,
                    tone = LocalTopBarActionTone.current,
                    size = dimensionResource(R.dimen.top_bar_icon_size),
                )
            },
        )
    }
}

/** Hero treatment — a transparent bar with Glass nav/action buttons over imagery. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BingeTopBarTransparentSample() {
    ScreenshotTheme {
        TransparentBingeTopBarSample()
    }
}

/**
 * The default scrim, fully in — a transparent bar over a plain surface (a grid or list on the app's
 * own background), left at its default [BingeTopBar.scrimColor]/`scrimForegroundColor` rather than
 * overridden to the always-black pair the hero sample above uses. Proves the pair this PR's whole
 * default-flip is about actually renders legibly in both themes, since every other transparent
 * fixture in this catalog explicitly opts back into black-always.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BingeTopBarThemeFollowingScrimSample() {
    ScreenshotTheme {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.background),
        ) {
            BingeTopBar(
                title = "Popular Movies",
                onBack = {},
                containerColor = Color.Transparent,
                scrimFraction = 1f,
                actions = { glassBackgroundAlpha ->
                    ExpressiveIconButton(
                        onClick = {},
                        icon = Icons.Filled.Search,
                        contentDescription = null,
                        tint = LocalTopBarActionTint.current,
                        tone = LocalTopBarActionTone.current,
                        size = dimensionResource(R.dimen.top_bar_icon_size),
                        glassBackgroundAlpha = glassBackgroundAlpha,
                    )
                },
            )
        }
    }
}
