package com.binge.designsystem.component

import android.view.Window
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.view.WindowInsetsControllerCompat
import coil3.compose.SubcomposeAsyncImage
import com.binge.designsystem.R
import com.binge.designsystem.resolvedContentPadding
import com.binge.designsystem.theme.BingeExpressiveTheme
import com.binge.designsystem.theme.BingeTheme

private const val HERO_SCRIM_TOP_ALPHA = 0.55f
private const val HERO_SCRIM_CLEAR_STOP = 0.25f
private const val HERO_TAGLINE_ALPHA = 0.85f
private const val HERO_META_ALPHA = 0.80f

/**
 * Sets the status bar to always-light icons for a screen whose content runs under it — [HeroScrim]
 * and the image viewer's own [BingeTheme.colors.scrim] are both always-black regardless of theme, so
 * the icons over them never need to follow the theme either; a theme-following default would go dark-
 * on-dark in light theme against a backdrop that never actually lightens.
 *
 * Restores to the theme-following state on dispose, so the screen navigated back to reads normally
 * rather than inheriting light icons it never asked for. The restore waits for the last effect on
 * the window to leave. A transition between two screens that both call this composes them together,
 * and the outgoing one disposes after the incoming one has set light icons; restoring then would put
 * dark icons over the incoming hero.
 */
@Composable
fun DarkStatusBarEffect() {
    val view = LocalView.current
    val isDark = BingeTheme.isDark
    val window = view.context.findActivity()?.window
    if (!view.isInEditMode && window != null) {
        DisposableEffect(Unit) {
            val controller = WindowInsetsControllerCompat(window, view)
            darkStatusBarHolds.acquire(window)
            controller.isAppearanceLightStatusBars = false
            onDispose {
                if (darkStatusBarHolds.release(window)) {
                    controller.isAppearanceLightStatusBars = !isDark
                }
            }
        }
    }
}

/**
 * The live [DarkStatusBarEffect]s per window. [DetailOverlayTopBar] reads it too: a live hold means a
 * hero sits under the bar, and only then does the bar keep the icons light at rest.
 */
internal val darkStatusBarHolds = StatusBarHolds<Window>()

@Composable
fun DetailHero(
    title: String,
    backdropUrl: String?,
    tagline: String?,
    metaText: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    genres: List<String> = emptyList(),
    eyebrow: String? = null,
    topRightActions: (@Composable RowScope.() -> Unit)? = null,
    // When false the hero draws no back/action chrome — the screen supplies its own overlay top bar.
    showChrome: Boolean = true,
    // Opts into the MeshGradientPainter tonal wash (Compose 1.12) under the scrim. Title heroes (movie/tv)
    // ask for it; the episode hero keeps the plain scrim, so its render is unchanged.
    richBackdrop: Boolean = false,
    // Drawn under the title (and under the tagline and meta line, when set), for a hero whose facts are
    // chips rather than the one line of text [metaText] takes.
    metaContent: (@Composable () -> Unit)? = null,
    // Replaces the text title, for a hero that shows a title logo. The caller owns loading and tinting,
    // and gives it a fixed height so text and logo swap without moving the layout.
    titleContent: (@Composable () -> Unit)? = null,
) {
    val eyebrowText = eyebrow?.takeIf { it.isNotBlank() }
        ?: genres.takeIf { it.isNotEmpty() }?.joinToString(" · ")
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(dimensionResource(R.dimen.detail_hero_height)),
    ) {
        HeroBackdrop(
            backdropUrl = backdropUrl,
            contentDescription = title,
            richBackdrop = richBackdrop,
        )

        if (showChrome) {
            ExpressiveIconButton(
                onClick = onBack,
                icon = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = stringResource(R.string.cd_navigate_back),
                tint = BingeTheme.colors.onScrim,
                tone = IconButtonTone.Glass,
                size = dimensionResource(R.dimen.top_bar_icon_size),
                modifier = Modifier
                    .statusBarsPadding()
                    .padding(dimensionResource(R.dimen.padding_m)),
            )

            if (topRightActions != null) {
                Row(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .statusBarsPadding()
                        .padding(dimensionResource(R.dimen.padding_m)),
                    horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.detail_action_row_spacing)),
                    content = topRightActions,
                )
            }
        }

        HeroTextColumn(
            title = title,
            tagline = tagline,
            metaText = metaText,
            eyebrowText = eyebrowText,
            metaContent = metaContent,
            titleContent = titleContent,
            modifier = Modifier.align(Alignment.BottomStart),
        )
    }
}

/**
 * The imagery layer a hero sits on: the backdrop (with its loading/error plate) under the black
 * status-bar band. [richBackdrop] adds the MeshGradientPainter tonal wash between the two, which the
 * title heroes opt into. The copy brings its own backing: put [heroCopyFade] on the column that
 * holds it.
 *
 * Public so a hero speaking a different vocabulary — `CollectionHero`, which has no tagline, genres
 * or single-title meta line — can reuse the imagery without inheriting [DetailHero]'s slots.
 */
@Composable
fun HeroBackdrop(
    backdropUrl: String?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    richBackdrop: Boolean = false,
) {
    Box(modifier = modifier.fillMaxSize()) {
        SubcomposeAsyncImage(
            model = backdropUrl,
            contentDescription = contentDescription,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
            loading = { ImagePlaceholder(Modifier.fillMaxSize()) },
            error = { ImagePlaceholder(Modifier.fillMaxSize()) },
        )
        if (richBackdrop) {
            // The fixed amber, not primary: the wash sits on raw art in both themes, and light primary
            // is a burnt amber tuned for text on a white page, which turns muddy there.
            HeroBackdropMeshWash(
                accentStart = MaterialTheme.colorScheme.primaryFixedDim,
                accentEnd = BingeTheme.colors.accentPurple,
            )
        }
        HeroScrim()
    }
}

/**
 * The black band under the status bar: strongest at the top edge and clear by a quarter of the way
 * down. It keeps the light status bar icons [DarkStatusBarEffect] sets legible over any art. Black in
 * both themes, because those icons are light in both themes.
 *
 * It no longer backs the copy. [HeroTextColumn] carries its own [heroCopyFade] in the page colour, so
 * the copy reads in `onBackground` and a light theme ends the hero on its own white page.
 */
@Composable
private fun HeroScrim() {
    val scrim = BingeTheme.colors.scrim
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    0.00f to scrim.copy(alpha = HERO_SCRIM_TOP_ALPHA),
                    HERO_SCRIM_CLEAR_STOP to Color.Transparent,
                ),
            ),
    )
}

/**
 * The hero's copy: title, then genres, then tagline, so the small text sits nearest the seam where
 * [heroCopyFade] is strongest. The column runs the hero's full width, so the fade behind it does too.
 * The title shrinks a step or two to stay on one line before it wraps; the genres take the accent.
 */
@Composable
private fun HeroTextColumn(
    title: String,
    tagline: String?,
    metaText: String,
    eyebrowText: String?,
    metaContent: (@Composable () -> Unit)?,
    titleContent: (@Composable () -> Unit)?,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .heroCopyFade()
            .padding(resolvedContentPadding(bottom = dimensionResource(R.dimen.detail_hero_text_bottom_padding))),
    ) {
        val titleStyle = DetailHeroDefaults.titleStyle()
        val lineGap = dimensionResource(R.dimen.padding_xs)
        if (titleContent != null) {
            CompositionLocalProvider(
                LocalTextStyle provides titleStyle,
                LocalContentColor provides MaterialTheme.colorScheme.onBackground,
            ) {
                titleContent()
            }
        } else {
            Text(
                text = title,
                style = titleStyle,
                color = MaterialTheme.colorScheme.onBackground,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                autoSize = DetailHeroDefaults.titleAutoSize(),
                modifier = Modifier.semantics { heading() },
            )
        }
        if (!eyebrowText.isNullOrBlank()) {
            Spacer(Modifier.height(lineGap))
            Text(
                text = eyebrowText,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
            )
        }
        if (!tagline.isNullOrBlank()) {
            Spacer(Modifier.height(lineGap))
            Text(
                text = tagline,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = HERO_TAGLINE_ALPHA),
                fontStyle = FontStyle.Italic,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
        if (metaText.isNotBlank()) {
            Spacer(Modifier.height(lineGap))
            Text(
                text = metaText,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = HERO_META_ALPHA),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
        if (metaContent != null) {
            Spacer(Modifier.height(lineGap))
            metaContent()
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewDetailHero() {
    BingeExpressiveTheme {
        DetailHero(
            title = "The Dark Knight",
            backdropUrl = null,
            tagline = "Why So Serious?",
            metaText = "9.0 · 2008 · 2h 32m",
            genres = listOf("Action", "Crime", "Drama"),
            onBack = {},
        )
    }
}
