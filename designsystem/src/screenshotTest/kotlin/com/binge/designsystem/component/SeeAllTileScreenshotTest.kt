package com.binge.designsystem.component

import android.content.res.Configuration.UI_MODE_NIGHT_NO
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.android.tools.screenshot.PreviewTest
import com.binge.designsystem.preview.ScreenshotTheme

/**
 * Poster-sized trailing tile (card_width-class); give it its own narrow canvas rather than the
 * shared 412dp width so it doesn't stretch across the frame.
 */
private const val SEE_ALL_TILE_CANVAS_DP = 152

/** Dashed-outline "see all" trailing tile sized like a poster cell, in both label forms. */
class SeeAllTileScreenshotTest {
    @PreviewTest
    @Preview(name = "Light", widthDp = SEE_ALL_TILE_CANVAS_DP, uiMode = UI_MODE_NIGHT_NO)
    @Preview(name = "Dark", widthDp = SEE_ALL_TILE_CANVAS_DP, uiMode = UI_MODE_NIGHT_YES)
    @Composable
    fun Tile() {
        ScreenshotTheme {
            SeeAllTile(
                onClick = {},
                modifier = Modifier.aspectRatio(2f / 3f),
            )
        }
    }

    @PreviewTest
    @Preview(name = "discover-more", widthDp = SEE_ALL_TILE_CANVAS_DP, uiMode = UI_MODE_NIGHT_NO)
    @Composable
    fun DiscoverMore() {
        ScreenshotTheme {
            SeeAllTile(
                label = "Discover more",
                onClick = {},
                modifier = Modifier.aspectRatio(2f / 3f),
            )
        }
    }
}
