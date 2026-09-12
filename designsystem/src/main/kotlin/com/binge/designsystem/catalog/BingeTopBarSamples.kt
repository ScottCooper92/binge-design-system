package com.binge.designsystem.catalog

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.dimensionResource
import com.binge.designsystem.R
import com.binge.designsystem.component.BingeTopBar
import com.binge.designsystem.component.ExpressiveIconButton
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
