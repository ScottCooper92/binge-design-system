package com.binge.designsystem.catalog

import androidx.compose.runtime.Composable
import com.android.tools.screenshot.PreviewTest
import com.binge.designsystem.preview.ComponentPreviews

/** Screenshot coverage for the [DetailHero][com.binge.designsystem.component.DetailHero] catalog sample. */
class DetailHeroSamplesScreenshotTest {
    @PreviewTest
    @ComponentPreviews
    @Composable
    fun Hero() {
        DetailHeroSample()
    }
}
