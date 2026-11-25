package com.binge.designsystem.catalog

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import com.binge.designsystem.R
import com.binge.designsystem.component.BingeMediumTopBar
import com.binge.designsystem.component.BingeTopBar
import com.binge.designsystem.component.ExpressiveIconButton
import com.binge.designsystem.component.LocalTopBarActionTint
import com.binge.designsystem.component.LocalTopBarActionTone
import com.binge.designsystem.theme.BingeTheme

/**
 * A transparent `BingeTopBar` over a sample coloured background, with `IconButtonTone.Glass`
 * nav/action buttons — the hero-screen treatment. The bar's nav button derives its Glass tone from the transparent container. `scrimFraction`
 * exercises `scrimmedTitleColor`'s ramp on the title this sample renders.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun TransparentBingeTopBarWithScrim(scrimFraction: Float = 0f) {
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

/**
 * A transparent `BingeMediumTopBar` over a sample coloured background, with `IconButtonTone.Glass`
 * nav/action buttons — mirrors `TransparentBingeTopBarWithScrim`. `scrimFraction` exercises both the `TopBarScrim` this bar draws behind itself and
 * the title's `scrimmedTitleColor` ramp, since it also drives `BingeMediumTopBar`'s
 * `foregroundScrimFraction` default.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun TransparentBingeMediumTopBarWithScrim(scrimFraction: Float = 0f) {
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
