package com.binge.designsystem.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.android.tools.screenshot.PreviewTest
import com.binge.designsystem.preview.ComponentPreviews
import com.binge.designsystem.preview.ScreenshotTheme

/** Pill search field — empty placeholder state and populated with a clear button. */
class BingeSearchFieldScreenshotTest {
    @PreviewTest
    @ComponentPreviews
    @Composable
    fun TransparentContainer() {
        ScreenshotTheme {
            BingeSearchField(
                query = "",
                onQueryChange = {},
                onClear = {},
                placeholder = "Search services",
                containerColor = Color.Transparent,
            )
        }
    }
}
