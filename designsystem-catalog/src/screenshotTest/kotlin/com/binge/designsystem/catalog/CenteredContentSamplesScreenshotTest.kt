package com.binge.designsystem.catalog

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.android.tools.screenshot.PreviewTest

/** Wider than the reading-width cap, so the centring shows. */
private const val WIDE_DEVICE = "spec:width=900dp,height=240dp"

class CenteredContentSamplesScreenshotTest {
    @PreviewTest
    @Preview(name = "wide-dark", device = WIDE_DEVICE, uiMode = UI_MODE_NIGHT_YES)
    @Composable
    fun Wide() {
        CenteredContentSample()
    }
}
