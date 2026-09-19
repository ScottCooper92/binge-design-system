package com.binge.designsystem.component

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.android.tools.screenshot.PreviewTest
import com.binge.designsystem.LocalPaneWidth
import com.binge.designsystem.R
import com.binge.designsystem.preview.ComponentPreviews
import com.binge.designsystem.preview.ScreenshotTheme
import com.binge.designsystem.theme.BingeTheme

/**
 * Detail header — tagline and genre eyebrow wrap differently across widths.
 * [DetailHero] fixes its own height (R.dimen.detail_hero_height), so we pass no
 * height modifier; it renders top-anchored with theme background filling below.
 */
class DetailHeroScreenshotTest {
    @PreviewTest
    @ComponentPreviews
    @Composable
    fun Ready() {
        ScreenshotTheme {
            DetailHero(
                title = "The Dark Knight",
                backdropUrl = null,
                tagline = "Why So Serious?",
                metaText = "9.0 · 2008 · 2h 32m",
                genres = listOf("Action", "Crime", "Drama", "Thriller"),
                onBack = {},
            )
        }
    }

    /**
     * The title-hero variant (movie/TV) with the Compose 1.12 MeshGradientPainter tonal wash enabled —
     * the mesh sits under the scrim, so this frame guards that the enrichment shows while the copy stays
     * legible over it.
     */
    @PreviewTest
    @ComponentPreviews
    @Composable
    fun RichBackdrop() {
        ScreenshotTheme {
            DetailHero(
                title = "The Dark Knight",
                backdropUrl = null,
                tagline = "Why So Serious?",
                metaText = "9.0 · 2008 · 2h 32m",
                genres = listOf("Action", "Crime", "Drama", "Thriller"),
                onBack = {},
                richBackdrop = true,
            )
        }
    }

    /**
     * The immersive-chrome variant shared by movie/TV/person detail: an explicit [DetailHero.eyebrow]
     * with a glass back + [DetailHero.topRightActions] slot (here a lone share, the person-detail shape).
     */
    @PreviewTest
    @ComponentPreviews
    @Composable
    fun EyebrowWithActions() {
        ScreenshotTheme {
            DetailHero(
                title = "Jane Performer",
                backdropUrl = null,
                tagline = null,
                metaText = "",
                eyebrow = "Acting",
                onBack = {},
                topRightActions = {
                    ExpressiveIconButton(
                        onClick = {},
                        icon = Icons.Filled.Share,
                        contentDescription = null,
                        tint = BingeTheme.colors.onScrim,
                        tone = IconButtonTone.Glass,
                        size = dimensionResource(R.dimen.top_bar_icon_size),
                    )
                },
            )
        }
    }

    /**
     * A wide window (840dp, the two-pane breakpoint) with the hero rendered in a 360dp pane rather
     * than alone. [LocalPaneWidth] set to the pane's own width is what keeps its side padding at the
     * compact 16dp band instead of the 840dp window's 32dp — see [resolvedContentInset].
     */
    @PreviewTest
    @Preview(name = "pane360", device = "spec:width=840dp,height=1180dp,orientation=portrait", uiMode = UI_MODE_NIGHT_YES)
    @Composable
    fun AtPaneWidth() {
        ScreenshotTheme {
            CompositionLocalProvider(LocalPaneWidth provides 360.dp) {
                Box(modifier = Modifier.width(360.dp)) {
                    DetailHero(
                        title = "The Dark Knight",
                        backdropUrl = null,
                        tagline = "Why So Serious?",
                        metaText = "9.0 · 2008 · 2h 32m",
                        genres = listOf("Action", "Crime", "Drama", "Thriller"),
                        onBack = {},
                    )
                }
            }
        }
    }
}
