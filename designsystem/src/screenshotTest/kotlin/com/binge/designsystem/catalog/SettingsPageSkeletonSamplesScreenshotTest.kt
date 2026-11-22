package com.binge.designsystem.catalog

import androidx.compose.runtime.Composable
import com.android.tools.screenshot.PreviewTest
import com.binge.designsystem.preview.ScreenStatePreview
import com.binge.designsystem.preview.WorstCaseFontPreviews

/** Screenshot coverage for [com.binge.designsystem.component.SettingsPageSkeleton]: with an intro at the worst-case fonts, and without one. */
class SettingsPageSkeletonSamplesScreenshotTest {
    @PreviewTest
    @WorstCaseFontPreviews
    @Composable
    fun withIntro() = SettingsPageSkeletonSample()

    @PreviewTest
    @ScreenStatePreview
    @Composable
    fun plain() = SettingsPageSkeletonPlainSample()
}
