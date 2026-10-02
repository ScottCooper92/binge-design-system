@file:ScreenshotOnly

package com.binge.designsystem.catalog

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
import com.binge.designsystem.R
import com.binge.designsystem.component.DetailHero
import com.binge.designsystem.component.DetailOverlayTopBar
import com.binge.designsystem.component.ExpressiveIconButton
import com.binge.designsystem.component.IconButtonTone
import com.binge.designsystem.preview.ScreenshotTheme
import com.binge.designsystem.theme.BingeTheme

private const val SAMPLE_TITLE = "The Dark Knight"
private const val MID_SCROLL_PX = 600
private const val PAST_HERO_SCROLL_PX = 4000

/** The overlay bar at rest: transparent glass over the hero, with only back and share showing. */
@Composable
fun DetailOverlayTopBarRestingSample() {
    DetailOverlayTopBarSample(scrollPx = 0)
}

/** Part-way through the fade: the glass wash thinning while the bar's own scrim comes in. */
@Composable
fun DetailOverlayTopBarMidScrollSample() {
    DetailOverlayTopBarSample(scrollPx = MID_SCROLL_PX)
}

/** The hero scrolled up under the bar: a solid titled bar whose tints follow the theme. */
@Composable
fun DetailOverlayTopBarScrolledSample() {
    DetailOverlayTopBarSample(scrollPx = PAST_HERO_SCROLL_PX)
}

/** Over a real [DetailHero] with its own chrome suppressed, so the glass state reads faithfully. */
@Composable
private fun DetailOverlayTopBarSample(scrollPx: Int) {
    ScreenshotTheme {
        Box(Modifier.fillMaxWidth()) {
            DetailHero(
                title = SAMPLE_TITLE,
                backdropUrl = null,
                tagline = "Why So Serious?",
                metaText = "",
                genres = listOf("Action", "Crime", "Drama"),
                onBack = {},
                showChrome = false,
            )
            DetailOverlayTopBar(
                title = SAMPLE_TITLE,
                scrollState = rememberScrollState(initial = scrollPx),
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
