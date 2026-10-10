package com.binge.designsystem.catalog

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.android.tools.screenshot.PreviewTest
import com.binge.designsystem.preview.ComponentPreviews
import com.binge.designsystem.preview.PreviewCutout
import com.binge.designsystem.preview.PreviewEdge
import com.binge.designsystem.preview.PreviewSystemBarInsets

private const val NARROW_LANDSCAPE = "spec:width=360dp,height=660dp,orientation=landscape"

class BingeBottomSheetSamplesScreenshotTest {
    @PreviewTest
    @ComponentPreviews
    @Composable
    fun TextField() {
        BingeBottomSheetSample()
    }

    /** A narrow landscape window with a camera cutout on the left. The 640dp sheet reaches into the cutout, so its content clears it. */
    @PreviewTest
    @Preview(name = "narrow-land", device = NARROW_LANDSCAPE, uiMode = UI_MODE_NIGHT_YES)
    @Composable
    fun UnderSideCutout() {
        PreviewSystemBarInsets(cutout = PreviewCutout(PreviewEdge.Left)) { BingeBottomSheetSideInsetsSample() }
    }
}
