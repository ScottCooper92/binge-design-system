package com.binge.designsystem.catalog

import android.content.res.Configuration.UI_MODE_NIGHT_NO
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.android.tools.screenshot.PreviewTest

/** The tab shell with its first tab showing, in the bottom bar. */
class TabShellSamplesScreenshotTest {
    @PreviewTest
    @Preview(name = "bar-light", widthDp = 412, heightDp = 740, uiMode = UI_MODE_NIGHT_NO)
    @Preview(name = "bar-dark", widthDp = 412, heightDp = 740, uiMode = UI_MODE_NIGHT_YES)
    @Composable
    fun BottomBar() {
        TabShellSample()
    }
}
