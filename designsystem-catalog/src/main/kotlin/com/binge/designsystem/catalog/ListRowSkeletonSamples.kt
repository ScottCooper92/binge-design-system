@file:OnePerScreen

package com.binge.designsystem.catalog

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import com.binge.designsystem.R
import com.binge.designsystem.component.FontScaleHeight
import com.binge.designsystem.component.ListRowSkeletonColumn
import com.binge.designsystem.preview.ScreenshotTheme

/**
 * Public samples for [ListRowSkeletonColumn] — the shared list-row loading skeleton, a column of
 * full-width row placeholders. See the convention KDoc on [MediaCardRatedSample]. The column fills
 * its container, so the samples pass an explicit [Modifier.fillMaxSize] to [ScreenshotTheme].
 */
@Composable
fun ListRowSkeletonSample() {
    ScreenshotTheme(modifier = Modifier.fillMaxSize()) {
        ListRowSkeletonColumn(contentPadding = PaddingValues(dimensionResource(R.dimen.padding_m)))
    }
}

/** The [height][ListRowSkeletonColumn] override — an avatar-leading row shorter than the poster-leading default. */
@Composable
fun ListRowSkeletonCompactSample() {
    ScreenshotTheme(modifier = Modifier.fillMaxSize()) {
        ListRowSkeletonColumn(
            contentPadding = PaddingValues(dimensionResource(R.dimen.padding_m)),
            height = dimensionResource(R.dimen.avatar_size_lg),
        )
    }
}

private const val SAMPLE_SCALE_MID = 1.3f
private const val SAMPLE_SCALE_LARGE = 2f
private const val SAMPLE_GROWTH_MID = 1.25f
private const val SAMPLE_GROWTH_LARGE = 1.75f

/**
 * The [fontScaleHeights][ListRowSkeletonColumn] hint — a row measured at three font scales, whose skeleton follows them, so it is
 * taller at 1.3 and again at 2 and the rows below it do not move when the list loads.
 */
@Composable
fun ListRowSkeletonFontScaleSample() {
    val base = dimensionResource(R.dimen.avatar_size_lg)
    ScreenshotTheme(modifier = Modifier.fillMaxSize()) {
        ListRowSkeletonColumn(
            contentPadding = PaddingValues(dimensionResource(R.dimen.padding_m)),
            fontScaleHeights =
                listOf(
                    FontScaleHeight(1f, base),
                    FontScaleHeight(SAMPLE_SCALE_MID, base * SAMPLE_GROWTH_MID),
                    FontScaleHeight(SAMPLE_SCALE_LARGE, base * SAMPLE_GROWTH_LARGE),
                ),
        )
    }
}
