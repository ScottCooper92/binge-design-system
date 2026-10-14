package com.binge.designsystem.catalog

import android.content.res.Configuration.UI_MODE_NIGHT_NO
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.android.tools.screenshot.PreviewTest

/** A phone, below the expanded width, and a tablet in landscape, above it. */
private const val COMPACT_DEVICE = "spec:width=412dp,height=800dp"
private const val EXPANDED_DEVICE = "spec:width=900dp,height=600dp"

class BingeAdaptiveSheetSamplesScreenshotTest {
    @PreviewTest
    @Preview(name = "compact-dark", device = COMPACT_DEVICE, uiMode = UI_MODE_NIGHT_YES)
    @Preview(name = "compact-light", device = COMPACT_DEVICE, uiMode = UI_MODE_NIGHT_NO)
    @Preview(name = "expanded-dark", device = EXPANDED_DEVICE, uiMode = UI_MODE_NIGHT_YES)
    @Preview(name = "expanded-light", device = EXPANDED_DEVICE, uiMode = UI_MODE_NIGHT_NO)
    @Composable
    fun Task() {
        BingeAdaptiveTaskSheetSample()
    }

    @PreviewTest
    @Preview(name = "compact-dark", device = COMPACT_DEVICE, uiMode = UI_MODE_NIGHT_YES)
    @Preview(name = "expanded-dark", device = EXPANDED_DEVICE, uiMode = UI_MODE_NIGHT_YES)
    @Composable
    fun TaskStacked() {
        BingeAdaptiveTaskSheetStackedSample()
    }

    @PreviewTest
    @Preview(name = "compact-dark", device = COMPACT_DEVICE, uiMode = UI_MODE_NIGHT_YES)
    @Preview(name = "expanded-dark", device = EXPANDED_DEVICE, uiMode = UI_MODE_NIGHT_YES)
    @Composable
    fun Menu() {
        BingeAdaptiveMenuSheetSample()
    }
}
