@file:CatalogGroup("Detail pages")

package com.binge.designsystem.catalog

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Person
import androidx.compose.runtime.Composable
import com.binge.designsystem.component.DetailInfoEntry
import com.binge.designsystem.component.DetailInfoSection
import com.binge.designsystem.preview.ScreenshotTheme

/** A page's facts under their heading: two columns on a phone, falling back to one at a large font. */
@Composable
fun DetailInfoSectionSample() {
    ScreenshotTheme {
        DetailInfoSection(
            heading = "Details",
            entries = listOf(
                DetailInfoEntry(Icons.Filled.Person, "Director", "Christopher Nolan"),
                DetailInfoEntry(Icons.Filled.CalendarMonth, "Released", "18 July 2008"),
                DetailInfoEntry(Icons.Filled.Language, "Original language", "English"),
                DetailInfoEntry(Icons.Filled.Business, "Studio", "Warner Bros. Pictures"),
            ),
        )
    }
}
