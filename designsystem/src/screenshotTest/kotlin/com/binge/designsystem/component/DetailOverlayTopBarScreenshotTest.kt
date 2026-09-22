package com.binge.designsystem.component

import android.content.res.Configuration.UI_MODE_NIGHT_NO
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.tooling.preview.Preview
import com.android.tools.screenshot.PreviewTest
import com.binge.designsystem.R
import com.binge.designsystem.preview.ScreenshotTheme
import com.binge.designsystem.theme.BingeTheme

private const val PAST_HERO_SCROLL_PX = 4000

/** A phone canvas tall enough to show the hero plus the overlay bar. */
private const val OVERLAY_BAR_DEVICE = "spec:width=411dp,height=560dp"

/**
 * The overlay bar at both ends of its scroll fade: transparent glass over the hero at rest, and a
 * solid titled bar once the hero has scrolled up under it. Rendered over a real [DetailHero]
 * (its own chrome suppressed) so the glass state reads faithfully.
 */
class DetailOverlayTopBarScreenshotTest {
    @PreviewTest
    @Preview(name = "resting-dark", device = OVERLAY_BAR_DEVICE, uiMode = UI_MODE_NIGHT_YES)
    @Composable
    fun Resting() {
        ScreenshotTheme {
            Sample(scroll = 0)
        }
    }

    @PreviewTest
    @Preview(name = "scrolled-dark", device = OVERLAY_BAR_DEVICE, uiMode = UI_MODE_NIGHT_YES)
    @Composable
    fun Scrolled() {
        ScreenshotTheme {
            Sample(scroll = PAST_HERO_SCROLL_PX)
        }
    }

    /**
     * Light theme at the bar's fully-scrolled steady state (`progress = 1`): the state where the
     * back button's Glass backing has faded to nothing and only the theme-following [TopBarScrim]
     * is behind it. This is the frame that catches a tint left hardcoded at
     * [BingeTheme.colors.onScrim] — it would render a white icon on the light bar this baseline
     * expects instead.
     */
    @PreviewTest
    @Preview(name = "scrolled-light", device = OVERLAY_BAR_DEVICE, uiMode = UI_MODE_NIGHT_NO)
    @Composable
    fun ScrolledLight() {
        ScreenshotTheme {
            Sample(scroll = PAST_HERO_SCROLL_PX)
        }
    }

    @PreviewTest
    @Preview(name = "midscroll-dark", device = OVERLAY_BAR_DEVICE, uiMode = UI_MODE_NIGHT_YES)
    @Composable
    fun MidScroll() {
        ScreenshotTheme {
            Sample(scroll = 600)
        }
    }

    @Composable
    private fun Sample(scroll: Int) {
        Box(Modifier.fillMaxWidth()) {
            DetailHero(
                title = "The Dark Knight",
                backdropUrl = null,
                tagline = "Why So Serious?",
                metaText = "",
                genres = listOf("Action", "Crime", "Drama"),
                onBack = {},
                showChrome = false,
            )
            DetailOverlayTopBar(
                title = "The Dark Knight",
                scrollState = rememberScrollState(initial = scroll),
                onBack = {},
            ) { glassBackgroundAlpha ->
                ExpressiveIconButton(
                    onClick = {},
                    icon = Icons.Filled.Share,
                    contentDescription = null,
                    tint = lerp(
                        BingeTheme.colors.onScrim,
                        MaterialTheme.colorScheme.onBackground,
                        1f - glassBackgroundAlpha,
                    ),
                    tone = IconButtonTone.Glass,
                    size = dimensionResource(R.dimen.top_bar_icon_size),
                    glassBackgroundAlpha = glassBackgroundAlpha,
                )
            }
        }
    }
}
