package com.binge.designsystem.component

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Star
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import com.android.tools.screenshot.PreviewTest
import com.binge.designsystem.preview.ComponentPreviews
import com.binge.designsystem.preview.ScreenshotTheme

/** The variants the catalog sample does not show: a non-clickable row (no chevron), and a row drawn from its painter slot. */
class SettingsGroupScreenshotTest {
    @PreviewTest
    @ComponentPreviews
    @Composable
    fun UntitledNonClickable() {
        ScreenshotTheme {
            SettingsGroup(
                title = null,
                rows = listOf(
                    SettingsRow(
                        icon = Icons.Filled.Notifications,
                        label = "Notifications",
                        detail = "Push alerts enabled",
                        clickable = false,
                    ),
                ),
            )
        }
    }

    /** The painter slot wins over [SettingsRow.icon]: the row draws the star, never the bell it names as fallback. */
    @PreviewTest
    @ComponentPreviews
    @Composable
    fun PainterIcon() {
        ScreenshotTheme {
            SettingsGroup(
                title = null,
                rows = listOf(
                    SettingsRow(
                        icon = Icons.Filled.Notifications,
                        iconPainter = { rememberVectorPainter(Icons.Filled.Star) },
                        label = "Seerr",
                        detail = "A glyph resolved by the caller",
                    ),
                ),
            )
        }
    }
}
