package com.binge.designsystem.catalog

import androidx.compose.runtime.Composable
import com.android.tools.screenshot.PreviewTest
import com.binge.designsystem.preview.ComponentPreviews

/** Screenshot coverage for the [SettingsGroup] catalog samples. */
class SettingsGroupSamplesScreenshotTest {
    @PreviewTest
    @ComponentPreviews
    @Composable
    fun titled() {
        SettingsGroupTitledSample()
    }

    @PreviewTest
    @ComponentPreviews
    @Composable
    fun tinted() {
        SettingsGroupTintedSample()
    }

    @PreviewTest
    @ComponentPreviews
    @Composable
    fun selected() {
        SettingsGroupSelectedSample()
    }
}
