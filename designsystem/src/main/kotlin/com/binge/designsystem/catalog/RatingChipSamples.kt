package com.binge.designsystem.catalog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.dimensionResource
import com.binge.designsystem.R
import com.binge.designsystem.component.RatingChip
import com.binge.designsystem.component.RatingChipSize
import com.binge.designsystem.component.RatingChipTone
import com.binge.designsystem.preview.ScreenshotTheme

/**
 * Public samples for [RatingChip] — catalog under `"Chips"` (see [MediaCardRatedSample] for the
 * convention). The two visually distinct axes get a cell each: the three [RatingChipTone]s, and the
 * three [RatingChipSize]s.
 */
@Composable
fun RatingChipToneSample() {
    ScreenshotTheme {
        Row(horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_s))) {
            RatingChip(rating = 8.5f, tone = RatingChipTone.Glass)
            RatingChip(rating = 8.5f, tone = RatingChipTone.Surface)
            RatingChip(rating = 8.5f, tone = RatingChipTone.Accent)
        }
    }
}

/** The three [RatingChipSize]s (Sm / Md / Lg) at the default Glass tone. */
@Composable
fun RatingChipSizeSample() {
    ScreenshotTheme {
        Row(
            horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_s)),
        ) {
            RatingChip(rating = 7.4f, size = RatingChipSize.Sm)
            RatingChip(rating = 8.5f, size = RatingChipSize.Md)
            RatingChip(rating = 9.0f, size = RatingChipSize.Lg)
        }
    }
}
