package com.binge.designsystem.component

import android.content.res.Configuration.UI_MODE_NIGHT_NO
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.android.tools.screenshot.PreviewTest
import com.binge.designsystem.preview.ScreenshotTheme

// A provider tile is a small square grid cell (~quarter of a row in WatchProviderGrid),
// so it gets its own narrow canvas rather than the shared 412dp ThemePreviews width —
// otherwise it fills the screen. fillMaxWidth + aspectRatio(1f) make it square.
private const val PROVIDER_TILE_CANVAS_DP = 120

/** Single provider tile — selected vs unselected, colour axis only. */
class WatchProviderTileScreenshotTest {
    @PreviewTest
    @Preview(name = "Light", widthDp = PROVIDER_TILE_CANVAS_DP, uiMode = UI_MODE_NIGHT_NO)
    @Preview(name = "Dark", widthDp = PROVIDER_TILE_CANVAS_DP, uiMode = UI_MODE_NIGHT_YES)
    @Composable
    fun Selected() {
        ScreenshotTheme {
            WatchProviderTile(
                provider = WatchProviderUi(id = 1, name = "Netflix", logoUrl = ""),
                selected = true,
                onToggle = {},
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }

    @PreviewTest
    @Preview(name = "Light", widthDp = PROVIDER_TILE_CANVAS_DP, uiMode = UI_MODE_NIGHT_NO)
    @Preview(name = "Dark", widthDp = PROVIDER_TILE_CANVAS_DP, uiMode = UI_MODE_NIGHT_YES)
    @Composable
    fun Unselected() {
        ScreenshotTheme {
            WatchProviderTile(
                provider = WatchProviderUi(id = 2, name = "Disney+", logoUrl = ""),
                selected = false,
                onToggle = {},
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
