@file:CatalogGroup("Detail pages")

package com.binge.designsystem.catalog

import androidx.compose.runtime.Composable
import com.binge.designsystem.component.OverviewSection
import com.binge.designsystem.preview.ScreenshotTheme

/** A title's synopsis under its heading, collapsed with a toggle where it runs past three lines. */
@Composable
fun OverviewSectionSample() {
    ScreenshotTheme { OverviewSection(heading = "Overview", overview = MEDIA_PAGE_OVERVIEW) }
}
