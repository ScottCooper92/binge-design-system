package com.binge.designsystem.catalog

import android.content.res.Configuration.UI_MODE_NIGHT_NO
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
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

    @PreviewTest
    @Preview(name = "tag-light", widthDp = CINEMATIC_PREVIEW_WIDTH_DP, uiMode = UI_MODE_NIGHT_NO)
    @Preview(name = "tag-dark", widthDp = CINEMATIC_PREVIEW_WIDTH_DP, uiMode = UI_MODE_NIGHT_YES)
    @Composable
    fun Tagline() {
        DetailCinematicHeaderTaglineSample()
    }

    @PreviewTest
    @Preview(name = "logo-light", widthDp = CINEMATIC_PREVIEW_WIDTH_DP, uiMode = UI_MODE_NIGHT_NO)
    @Preview(name = "logo-dark", widthDp = CINEMATIC_PREVIEW_WIDTH_DP, uiMode = UI_MODE_NIGHT_YES)
    @Composable
    fun TitleContent() {
        DetailCinematicHeaderTitleContentSample()
    }

    /**
     * The regression guard for `CinematicSideScrim`'s direction-aware ramp. The scrim is start-anchored, so an absolute
     * gradient would put its dense end under the end of the backdrop rather than behind the poster and copy. It shows
     * over the flat placeholder too, since `validateDebugScreenshotTest` diffs pixels exactly.
     */
    @PreviewTest
    @Preview(name = "exp900-rtl", widthDp = CINEMATIC_PREVIEW_WIDTH_DP, uiMode = UI_MODE_NIGHT_YES)
    @Composable
    fun HeaderRtl() {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            DetailCinematicHeaderSample()
        }
    }

    @PreviewTest
    @Preview(name = "exp900-overflow", widthDp = CINEMATIC_PREVIEW_WIDTH_DP, uiMode = UI_MODE_NIGHT_YES)
    @Composable
    fun HeaderSynopsisOverflow() {
        DetailCinematicHeaderSynopsisOverflowSample()
    }
}
