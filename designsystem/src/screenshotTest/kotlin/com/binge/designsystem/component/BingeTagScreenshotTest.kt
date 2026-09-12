package com.binge.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Subtitles
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import com.android.tools.screenshot.PreviewTest
import com.binge.designsystem.R
import com.binge.designsystem.preview.ComponentPreviews
import com.binge.designsystem.preview.ScreenshotTheme
import com.binge.designsystem.theme.BingeTheme

/** The category tag across its neutral media-type, colour-coded issue-type, and cased role forms. */
class BingeTagScreenshotTest {
    @OptIn(ExperimentalLayoutApi::class)
    @PreviewTest
    @ComponentPreviews
    @Composable
    fun Tags() {
        ScreenshotTheme {
            FlowRow(
                modifier = Modifier.padding(dimensionResource(R.dimen.padding_s)),
                horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_s)),
                verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_s)),
            ) {
                // Media type — neutral.
                BingeTag(label = "Movie", icon = Icons.Filled.Movie)
                BingeTag(label = "Series", icon = Icons.Filled.Tv)
                // Issue type — colour-coded.
                BingeTag(label = "Video", icon = Icons.Filled.Videocam, tint = BingeTheme.colors.info)
                BingeTag(label = "Audio", icon = Icons.AutoMirrored.Filled.VolumeUp, tint = BingeTheme.colors.caution)
                BingeTag(label = "Subtitle", icon = Icons.Filled.Subtitles, tint = BingeTheme.colors.accentPurple)
                // Role — cased, with a shield for the privileged roles.
                BingeTag(label = "Owner", icon = Icons.Filled.Shield, tint = BingeTheme.colors.caution, uppercase = false)
                BingeTag(label = "Admin", icon = Icons.Filled.Shield, tint = BingeTheme.colors.info, uppercase = false)
                BingeTag(label = "User", icon = Icons.Filled.Person, uppercase = false)
            }
        }
    }
}
