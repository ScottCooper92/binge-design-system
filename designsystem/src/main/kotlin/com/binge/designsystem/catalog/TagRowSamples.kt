@file:CatalogGroup("Tags and badges")
@file:SelfDescribing

package com.binge.designsystem.catalog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import com.binge.designsystem.R
import com.binge.designsystem.component.SectionHeader
import com.binge.designsystem.component.TagRow
import com.binge.designsystem.preview.ScreenshotTheme

private val SampleTags =
    listOf("Heist", "Based on a novel", "Time loop", "Dystopia", "Found family", "Slow burn", "Ensemble cast", "Cult classic")

/**
 * [TagRow] in its two sizes, one above the other: the default tags and the compact ones, both wrapping in a narrow column. Tapping a tag
 * in the sample names it below, so the callback and the index can be seen working.
 */
@Composable
fun TagRowSample() {
    ScreenshotTheme {
        var tapped by remember { mutableStateOf<Int?>(null) }
        // The box loosens the frame's tight constraints so the column can take its own, narrower width.
        Box {
            Column(
                Modifier.width(dimensionResource(R.dimen.wrapping_row_sample_width)).padding(dimensionResource(R.dimen.padding_s)),
                verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_s)),
            ) {
                TagRow(tags = SampleTags, onTagClick = { tapped = it })
                TagRow(tags = SampleTags, compact = true, onTagClick = { tapped = it })
                Text(
                    text = tapped?.let { "Tapped: ${SampleTags[it]}" } ?: "Tap a tag",
                    style = MaterialTheme.typography.labelMedium,
                )
            }
        }
    }
}

private val KeywordTags = listOf(
    "alaska",
    "dog",
    "special forces",
    "military",
    "american wilderness",
    "survival",
    "rescue",
    "snow",
    "friendship",
    "journey",
)

/**
 * A detail page's keywords: the section header over compact, tappable tags of very different lengths. The tags are 8dp apart both ways,
 * short or long, and the heading sits as close to them as it does to any other section's content, because a tag takes only its own size
 * in the layout and its 48dp touch target reaches into the space around it.
 */
@Composable
fun TagRowKeywordsSample() {
    ScreenshotTheme {
        var tapped by remember { mutableStateOf<Int?>(null) }
        Column {
            SectionHeader("Keywords", horizontalPadding = dimensionResource(R.dimen.padding_m))
            TagRow(
                tags = KeywordTags,
                compact = true,
                onTagClick = { tapped = it },
                modifier = Modifier.padding(horizontal = dimensionResource(R.dimen.padding_m)),
            )
            Text(
                text = tapped?.let { "Tapped: ${KeywordTags[it]}" } ?: "Tap a keyword",
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.padding(dimensionResource(R.dimen.padding_m)),
            )
        }
    }
}
