package com.binge.designsystem.catalog

import android.content.res.Configuration.UI_MODE_NIGHT_NO
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.android.tools.screenshot.PreviewTest
import com.binge.designsystem.preview.ComponentPreviews

class BingeSnackbarHostSamplesScreenshotTest {
    @PreviewTest
    @ComponentPreviews
    @Composable
    fun WithAction() {
        BingeSnackbarSample()
    }

    @PreviewTest
    @ComponentPreviews
    @Composable
    fun WithDismiss() {
        BingeSnackbarDismissSample()
    }

    /** A full nav shell, not [ComponentPreviews]'s wrap-content canvas — the offset needs the bar beneath it. */
    @PreviewTest
    @Preview(name = "float-phone-light", widthDp = 412, heightDp = 740, uiMode = UI_MODE_NIGHT_NO)
    @Preview(name = "float-phone-dark", widthDp = 412, heightDp = 740, uiMode = UI_MODE_NIGHT_YES)
    @Composable
    fun OverFloatingBar() {
        BingeSnackbarOverFloatingBarSample()
    }
}
