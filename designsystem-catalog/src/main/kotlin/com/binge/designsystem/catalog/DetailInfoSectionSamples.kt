@file:CatalogGroup("Detail pages")

package com.binge.designsystem.catalog

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Person
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import com.binge.designsystem.R
import com.binge.designsystem.component.DetailInfoEntry
import com.binge.designsystem.component.DetailInfoSection
import com.binge.designsystem.preview.ScreenshotTheme
import com.binge.designsystem.template.LocalHeroReadingMargin

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

/**
 * [DetailInfoSectionSample] in a page's reading column on a wide window: the heading starts where the facts do, inside the column, not at
 * the window's edge.
 */
@Composable
fun DetailInfoSectionWideSample() {
    ScreenshotTheme {
        val column = dimensionResource(R.dimen.content_max_width)
        BoxWithConstraints(Modifier.fillMaxWidth()) {
            val margin = ((maxWidth - column) / 2).coerceAtLeast(dimensionResource(R.dimen.zero))
            CompositionLocalProvider(LocalHeroReadingMargin provides margin) {
                DetailInfoSectionSample()
            }
        }
    }
}
