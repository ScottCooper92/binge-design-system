package com.binge.designsystem.catalog

import androidx.compose.runtime.Composable
import com.binge.designsystem.component.DiscoverEntryRow
import com.binge.designsystem.preview.ScreenshotTheme

/**
 * Public sample for [DiscoverEntryRow] (group `"Rows"`). See the convention KDoc on
 * [MediaCardRatedSample].
 *
 * A full-width entry row (icon + title/subtitle + trailing chevron) — it spans its parent, so the
 * sample lets the canvas bound its width.
 */
@Composable
fun DiscoverEntryRowSample() {
    ScreenshotTheme {
        DiscoverEntryRow(
            title = "Browse all movies",
            subtitle = "Filter by genre, decade, provider and more",
            onClick = {},
        )
    }
}
