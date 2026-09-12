package com.binge.designsystem.component

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import com.android.tools.screenshot.PreviewTest
import com.binge.designsystem.preview.ComponentPreviews
import com.binge.designsystem.preview.ScreenshotTheme
import com.binge.designsystem.theme.BingeTheme

/** Icon button across its three tones plus the disabled state. */
class ExpressiveIconButtonScreenshotTest {
    @PreviewTest
    @ComponentPreviews
    @Composable
    fun Default() {
        ScreenshotTheme {
            ExpressiveIconButton(
                onClick = {},
                icon = Icons.Filled.Favorite,
                contentDescription = "Favourite",
            )
        }
    }

    @PreviewTest
    @ComponentPreviews
    @Composable
    fun Accent() {
        ScreenshotTheme {
            ExpressiveIconButton(
                onClick = {},
                icon = Icons.Filled.Favorite,
                contentDescription = "Favourite",
                tone = IconButtonTone.Accent,
                tint = MaterialTheme.colorScheme.onSecondaryContainer,
            )
        }
    }

    @PreviewTest
    @ComponentPreviews
    @Composable
    fun Glass() {
        ScreenshotTheme {
            ExpressiveIconButton(
                onClick = {},
                icon = Icons.Filled.Favorite,
                contentDescription = "Favourite",
                tone = IconButtonTone.Glass,
                tint = BingeTheme.colors.onScrim,
            )
        }
    }
}
