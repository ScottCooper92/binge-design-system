package com.binge.designsystem.catalog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.dimensionResource
import com.binge.designsystem.R
import com.binge.designsystem.component.MediaTypeTag
import com.binge.designsystem.component.MediaTypeTagType
import com.binge.designsystem.preview.ScreenshotTheme

/**
 * Public sample for [MediaTypeTag] — the neutral [com.binge.designsystem.component.BingeTag]
 * wrapper that labels Movie / TV with the matching glyph. Catalog under `"Tags"`; see
 * [MediaCardRatedSample] for the convention.
 */
@Composable
fun MediaTypeTagSample() {
    ScreenshotTheme {
        Row(horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_s))) {
            MediaTypeTag(type = MediaTypeTagType.Movie)
            MediaTypeTag(type = MediaTypeTagType.Tv)
        }
    }
}
