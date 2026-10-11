@file:CatalogGroup("Tags and badges")

package com.binge.designsystem.catalog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Subtitles
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import com.binge.designsystem.R
import com.binge.designsystem.component.BingeTag
import com.binge.designsystem.preview.ScreenshotTheme
import com.binge.designsystem.theme.BingeSentiment
import com.binge.designsystem.theme.BingeTheme
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

/**
 * The tag in each form callers give it: neutral media types, colour-coded issue types, and cased roles, with a shield
 * on the privileged ones. The issue and role tags take a tint alone, with no separate icon fill.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BingeTagKindsSample() {
    ScreenshotTheme {
        FlowRow(
            modifier = Modifier.padding(dimensionResource(R.dimen.padding_s)),
            horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_s)),
            verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_s)),
        ) {
            BingeTag(label = "Movie", icon = Icons.Filled.Movie)
            BingeTag(label = "Series", icon = Icons.Filled.Tv)
            BingeTag(label = "Video", icon = Icons.Filled.Videocam, tint = BingeTheme.colors.info)
            BingeTag(label = "Audio", icon = Icons.AutoMirrored.Filled.VolumeUp, tint = BingeTheme.colors.caution)
            BingeTag(label = "Subtitle", icon = Icons.Filled.Subtitles, tint = BingeTheme.colors.accentPurple)
            BingeTag(label = "Owner", icon = Icons.Filled.Shield, tint = BingeTheme.colors.caution, uppercase = false)
            BingeTag(label = "Admin", icon = Icons.Filled.Shield, tint = BingeTheme.colors.info, uppercase = false)
            BingeTag(label = "User", icon = Icons.Filled.Person, uppercase = false)
        }
    }
}
