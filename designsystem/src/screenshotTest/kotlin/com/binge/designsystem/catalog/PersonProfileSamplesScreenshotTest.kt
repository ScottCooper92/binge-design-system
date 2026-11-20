package com.binge.designsystem.catalog

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.android.tools.screenshot.PreviewTest
import com.binge.designsystem.preview.ComponentPreviews
import com.binge.designsystem.preview.SOURCE_LOCALE
import com.binge.designsystem.preview.STANDARD_PHONE_WIDTH_DP
import com.binge.designsystem.preview.WorstCaseFontPreviews

/** Screenshot coverage for [com.binge.designsystem.component.PersonProfileHeader] and its skeleton. */
class PersonProfileSamplesScreenshotTest {
    @PreviewTest
    @ComponentPreviews
    @WorstCaseFontPreviews
    @Composable
    fun header() = PersonProfileHeaderSample()

    @PreviewTest
    @ComponentPreviews
    @WorstCaseFontPreviews
    @Composable
    fun headerLong() = PersonProfileHeaderLongSample()

    @PreviewTest
    @ComponentPreviews
    @Composable
    fun skeleton() = PersonProfileSkeletonSample()

    @PreviewTest
    @ComponentPreviews
    @Preview(name = "font13-dark", widthDp = STANDARD_PHONE_WIDTH_DP, fontScale = 1.3f, uiMode = UI_MODE_NIGHT_YES, locale = SOURCE_LOCALE)
    // Tall enough for the skeleton and the loaded header under it at 2x.
    @Preview(
        name = "font20-dark",
        widthDp = STANDARD_PHONE_WIDTH_DP,
        heightDp = 1100,
        fontScale = 2.0f,
        uiMode = UI_MODE_NIGHT_YES,
        locale = SOURCE_LOCALE,
    )
    @Composable
    fun skeletonHinted() = PersonProfileSkeletonHintedSample()
}
