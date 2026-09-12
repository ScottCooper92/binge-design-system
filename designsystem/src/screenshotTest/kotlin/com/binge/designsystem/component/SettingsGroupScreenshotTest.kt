package com.binge.designsystem.component

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.runtime.Composable
import com.android.tools.screenshot.PreviewTest
import com.binge.designsystem.preview.ComponentPreviews
import com.binge.designsystem.preview.ScreenshotTheme

/** The variant the catalog sample does not show: an untitled group whose row is not clickable, so no chevron. */
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
}
