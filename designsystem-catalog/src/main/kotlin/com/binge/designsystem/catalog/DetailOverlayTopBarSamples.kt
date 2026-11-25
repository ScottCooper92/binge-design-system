@file:ScreenshotOnly

package com.binge.designsystem.catalog

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.dimensionResource
import com.binge.designsystem.R
import com.binge.designsystem.component.DetailHero
import com.binge.designsystem.component.DetailOverlayTopBar
import com.binge.designsystem.component.ExpressiveIconButton
import com.binge.designsystem.component.IconButtonTone
import com.binge.designsystem.component.measuredHeroHeight
import com.binge.designsystem.component.rememberMeasuredHeroHeight
import com.binge.designsystem.preview.ScreenshotTheme
import com.binge.designsystem.theme.BingeTheme

private const val SAMPLE_TITLE = "The Dark Knight"
private const val MID_SCROLL_PX = 600
private const val PAST_HERO_SCROLL_PX = 4000
private const val CONTENT_LINES = 12

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

/**
 * The bar once the hero has gone, over page text that runs up under it: the solid surface the bar sits on at full
 * progress, so none of the text shows through behind the back button and title.
 */
@Composable
fun DetailOverlayTopBarOverContentSample() {
    DetailOverlayTopBarOverContent(progress = 1f)
}

/** The same bar halfway through its fade, the surface at half strength over the same text. */
@Composable
fun DetailOverlayTopBarOverContentHalfSample() {
    DetailOverlayTopBarOverContent(progress = 0.5f)
}

/** Text from the top edge of the page, so it sits under the bar; the bar's scroll is set to the given progress. */
@Composable
private fun DetailOverlayTopBarOverContent(progress: Float) {
    ScreenshotTheme {
        val density = LocalDensity.current
        val heroPx = with(density) { dimensionResource(R.dimen.detail_hero_height).toPx() }
        val bandPx = with(density) { dimensionResource(R.dimen.detail_overlay_bar_fade_band).toPx() }
        Box(Modifier.fillMaxWidth()) {
            Column {
                repeat(CONTENT_LINES) { index ->
                    Text(
                        text = "Cast member $index · Director of photography",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                }
            }
            DetailOverlayTopBar(
                title = SAMPLE_TITLE,
                scrollOffsetPx = { heroPx - bandPx * (1f - progress) },
                onBack = {},
            )
        }
    }
}

/** Over a real [DetailHero] with its own chrome suppressed, so the glass state reads faithfully. */
@Composable
private fun DetailOverlayTopBarSample(scrollPx: Int) {
    ScreenshotTheme {
        val measured = rememberMeasuredHeroHeight()
        Box(Modifier.fillMaxWidth()) {
            DetailHero(
                modifier = Modifier.measuredHeroHeight(measured),
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
                heroHeight = measured.height(dimensionResource(R.dimen.detail_hero_height)),
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
