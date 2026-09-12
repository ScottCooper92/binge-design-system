package com.binge.designsystem.component

import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.android.tools.screenshot.PreviewTest
import com.binge.designsystem.preview.ComponentPreviews
import com.binge.designsystem.preview.ScreenshotTheme

/**
 * Atomic image fallback — a tinted surface with a centred image glyph. Only the colour
 * axis matters, so it runs at the standard width in light + dark with a fixed size.
 */
class ImagePlaceholderScreenshotTest {
    @PreviewTest
    @ComponentPreviews
    @Composable
    fun Default() {
        ScreenshotTheme {
            ImagePlaceholder(Modifier.size(120.dp))
        }
    }
}
