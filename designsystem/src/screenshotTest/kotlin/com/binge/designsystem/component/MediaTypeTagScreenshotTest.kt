package com.binge.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import com.android.tools.screenshot.PreviewTest
import com.binge.designsystem.R
import com.binge.designsystem.preview.ComponentPreviews
import com.binge.designsystem.preview.ScreenshotTheme

/** The shared media-type tag in its Movie and TV variants. */
class MediaTypeTagScreenshotTest {
    @PreviewTest
    @ComponentPreviews
    @Composable
    fun Variants() {
        ScreenshotTheme {
            Row(
                modifier = Modifier.padding(dimensionResource(R.dimen.padding_s)),
                horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_s)),
            ) {
                MediaTypeTag(type = MediaTypeTagType.Movie)
                MediaTypeTag(type = MediaTypeTagType.Tv)
            }
        }
    }
}
