package com.binge.designsystem.component

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * The [heading] over a [DetailInfoGrid] of a page's facts (director, dates, languages, studios), for the end of a film, series or
 * person page. Draws nothing where every entry is blank. The heading is the caller's own string.
 *
 * @need show a page's facts in columns under a heading at the end of a detail page
 * @group detail
 * @replaces a hand-built SectionHeader over a DetailInfoGrid
 * @sample DetailInfoSectionSample
 */
@Composable
fun DetailInfoSection(
    heading: String,
    entries: List<DetailInfoEntry>,
    modifier: Modifier = Modifier,
) {
    if (entries.all { it.value.isBlank() }) return
    Column(modifier) {
        SectionHeader(title = heading)
        DetailInfoGrid(entries = entries)
    }
}
