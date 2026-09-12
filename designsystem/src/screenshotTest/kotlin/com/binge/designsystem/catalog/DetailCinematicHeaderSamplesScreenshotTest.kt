package com.binge.designsystem.catalog

import android.content.res.Configuration.UI_MODE_NIGHT_NO
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.android.tools.screenshot.PreviewTest

private const val CINEMATIC_PREVIEW_WIDTH_DP = 900

/**
 * Screenshot coverage for the cinematic-header catalog sample — renders the shared
 * [DetailCinematicHeaderSample] at the expanded (~900dp) canvas the component is for; the shared
 * wrap-content multipreviews don't reach that width.
 */
class DetailCinematicHeaderSamplesScreenshotTest {
    @PreviewTest
    @Preview(name = "exp900-light", widthDp = CINEMATIC_PREVIEW_WIDTH_DP, uiMode = UI_MODE_NIGHT_NO)
    @Preview(name = "exp900-dark", widthDp = CINEMATIC_PREVIEW_WIDTH_DP, uiMode = UI_MODE_NIGHT_YES)
    @Composable
    fun Header() {
        DetailCinematicHeaderSample()
    }
}
