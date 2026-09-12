package com.binge.designsystem.catalog

import androidx.compose.runtime.Composable
import com.binge.designsystem.component.SectionHeader
import com.binge.designsystem.preview.ScreenshotTheme

/** Section header with a trailing "More" pill — the carousel-heading variant. */
@Composable
fun SectionHeaderWithMoreSample() {
    ScreenshotTheme {
        SectionHeader(title = "Trending Movies", onMoreClick = {})
    }
}

/** Title-only header — no trailing action, used where the section has nothing to expand into. */
@Composable
fun SectionHeaderTitleOnlySample() {
    ScreenshotTheme {
        SectionHeader(title = "Cast")
    }
}
