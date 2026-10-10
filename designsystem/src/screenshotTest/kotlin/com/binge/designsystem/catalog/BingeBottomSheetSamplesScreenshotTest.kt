package com.binge.designsystem.catalog

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.android.tools.screenshot.PreviewTest
import com.binge.designsystem.preview.ComponentPreviews
import com.binge.designsystem.preview.PreviewCutout
import com.binge.designsystem.preview.PreviewEdge
import com.binge.designsystem.preview.PreviewSystemBarInsets

private const val PHONE_LANDSCAPE = "spec:width=411dp,height=600dp,orientation=landscape"

class BingeBottomSheetSamplesScreenshotTest {
    @PreviewTest
    @ComponentPreviews
    @Composable
    fun TextField() {
        BingeBottomSheetSample()
    }

    /** A landscape phone with the camera cutout on the left: the sheet, narrower than its cap here, clears it. */
    @PreviewTest
    @Preview(name = "phone-land", device = PHONE_LANDSCAPE, uiMode = UI_MODE_NIGHT_YES)
    @Composable
    fun UnderSideCutout() {
        PreviewSystemBarInsets(cutout = PreviewCutout(PreviewEdge.Left)) { BingeBottomSheetSideInsetsSample() }
    }
}
