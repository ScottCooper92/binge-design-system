package com.binge.designsystem.component

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import com.android.tools.screenshot.PreviewTest
import com.binge.designsystem.R
import com.binge.designsystem.preview.ScreenPreviews
import com.binge.designsystem.preview.ScreenshotTheme

/**
 * Shared list-row loading skeleton — a column of full-width row placeholders. Rendered across the
 * device-class matrix so the centred reading-column width caps are exercised alongside the shimmer
 * (frozen under [ScreenshotTheme]'s reduce-motion for a deterministic frame).
 */
class ListRowSkeletonScreenshotTest {
    @PreviewTest
    @ScreenPreviews
    @Composable
    fun Loading() {
        ScreenshotTheme(modifier = Modifier.fillMaxSize()) {
            ListRowSkeletonColumn(
                contentPadding = PaddingValues(dimensionResource(R.dimen.padding_m)),
            )
        }
    }
}
