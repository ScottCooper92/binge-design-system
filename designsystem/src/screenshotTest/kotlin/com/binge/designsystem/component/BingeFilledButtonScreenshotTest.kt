package com.binge.designsystem.component

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.runtime.Composable
import com.android.tools.screenshot.PreviewTest
import com.binge.designsystem.catalog.FilledButtonLoadingSample
import com.binge.designsystem.preview.ComponentPreviews
import com.binge.designsystem.preview.ScreenshotTheme

/**
 * Primary filled button — enabled and disabled across the colour axis. The `Loading` state renders
 * the shared public [FilledButtonLoadingSample] that the catalog's `CatalogButtonLoading` preview
 * also calls, so the two share one source; the enabled/disabled states stay inline as they
 * carry no catalog entry.
 */
class BingeFilledButtonScreenshotTest {
    @PreviewTest
    @ComponentPreviews
    @Composable
    fun Enabled() {
        ScreenshotTheme {
            BingeFilledButton(
                label = "Watch trailer",
                leadingIcon = Icons.Filled.PlayArrow,
                onClick = {},
            )
        }
    }

    @PreviewTest
    @ComponentPreviews
    @Composable
    fun Disabled() {
        ScreenshotTheme {
            BingeFilledButton(
                label = "Watch trailer",
                leadingIcon = Icons.Filled.PlayArrow,
                onClick = {},
                enabled = false,
            )
        }
    }

    @PreviewTest
    @ComponentPreviews
    @Composable
    fun Loading() {
        FilledButtonLoadingSample()
    }
}
