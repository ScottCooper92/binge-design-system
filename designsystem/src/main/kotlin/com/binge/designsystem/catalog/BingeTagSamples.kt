package com.binge.designsystem.catalog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Movie
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.dimensionResource
import com.binge.designsystem.R
import com.binge.designsystem.component.BingeTag
import com.binge.designsystem.preview.ScreenshotTheme
import com.binge.designsystem.theme.BingeSentiment
import com.binge.designsystem.theme.accent
import com.binge.designsystem.theme.fill

/**
 * Public samples for [BingeTag] — catalog under `"Tags"` (see [MediaCardRatedSample] for the
 * convention). Two visually distinct cells: the neutral tag (surface wash) with an optional leading
 * glyph, and a status-tinted tag whose label/wash take a sentiment [accent] and the icon the brighter
 * [fill] — the pattern callers use for type/issue/role tags.
 */
@Composable
fun BingeTagNeutralSample() {
    ScreenshotTheme {
        Row(horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_s))) {
            BingeTag(label = "4K")
            BingeTag(label = "Movie", icon = Icons.Filled.Movie)
        }
    }
}

/** A status-tinted tag — info-blue label + wash, brighter fill on the glyph. */
@Composable
fun BingeTagTintedSample() {
    ScreenshotTheme {
        BingeTag(
            label = "Video",
            icon = Icons.Filled.BugReport,
            tint = BingeSentiment.Info.accent(),
            fill = BingeSentiment.Info.fill(),
        )
    }
}
