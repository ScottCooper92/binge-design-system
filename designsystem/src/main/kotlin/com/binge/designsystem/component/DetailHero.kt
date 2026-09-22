package com.binge.designsystem.component

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.view.WindowInsetsControllerCompat
import coil3.compose.SubcomposeAsyncImage
import com.binge.designsystem.R
import com.binge.designsystem.resolvedContentInset
import com.binge.designsystem.theme.BingeExpressiveTheme
import com.binge.designsystem.theme.BingeTheme

private const val HERO_SCRIM_TOP_ALPHA = 0.55f
private const val HERO_SCRIM_MID_ALPHA = 0.60f
private const val HERO_SCRIM_BOTTOM_ALPHA = 0.90f
private const val HERO_SCRIM_CLEAR_STOP = 0.25f
private const val HERO_SCRIM_MID_STOP = 0.60f

/**
 * Sets the status bar's icon appearance for a screen whose content runs under it.
 *
 * Defaults to following the theme — dark icons in light theme, light in dark — matching the hero
 * scrim underneath, which now does the same ([HeroScrim]). Pass [alwaysLightIcons] for a screen whose
 * background is black regardless of theme (the image viewer's own [BingeTheme.colors.scrim]) — icons
 * stay light there even in light theme, since the backdrop underneath never gets any lighter.
 *
 * Restores to the theme-following state on dispose either way, so the screen navigated back to reads
 * normally rather than inheriting whichever mode this one asked for.
 */
@Composable
fun DarkStatusBarEffect(alwaysLightIcons: Boolean = false) {
    val view = LocalView.current
    val isDark = isSystemInDarkTheme()
    if (!view.isInEditMode) {
        DisposableEffect(Unit) {
            val controller = WindowInsetsControllerCompat(
                (view.context as Activity).window,
                view,
            )
            controller.isAppearanceLightStatusBars = if (alwaysLightIcons) false else !isDark
            onDispose { controller.isAppearanceLightStatusBars = !isDark }
        }
    }
}

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
            modifier = Modifier.align(Alignment.BottomStart),
        )
    }
}

/**
 * The imagery layer a hero sits on: the backdrop (with its loading/error plate) under the standard
 * scrim, so copy over it stays legible whatever the artwork is. [richBackdrop] adds the
 * MeshGradientPainter tonal wash between the two, which the title heroes opt into.
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
            HeroBackdropMeshWash(
                accentStart = MaterialTheme.colorScheme.primary,
                accentEnd = BingeTheme.colors.accentPurple,
            )
        }
        HeroScrim()
    }
}

/**
 * The theme-following wash [HeroTextColumn] sits on — but not what keeps its text legible: this
 * gradient is fully transparent until [HERO_SCRIM_MID_STOP], and title/tagline/meta can render well
 * above that stop depending on how many of them a given item populates, over whatever the raw
 * backdrop happens to be there. [HeroTextColumn]'s own text stays [BingeTheme.colors.onScrim] for
 * that reason — the same guarantee [IconButtonTone.Glass] carries for the same reason.
 */
@Composable
private fun HeroScrim() {
    // Always-black rather than theme-following: this sits directly over unpredictable backdrop
    // imagery with no compensating scrim of its own (unlike DetailCinematicHeader's CinematicScrim,
    // which has CinematicSideScrim to guarantee coverage where its title lands), so only a
    // guaranteed-dark backing keeps the title/tagline/meta legible regardless of what's underneath.
    val scrim = BingeTheme.colors.scrim
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    0.00f to scrim.copy(alpha = HERO_SCRIM_TOP_ALPHA),
                    HERO_SCRIM_CLEAR_STOP to Color.Transparent,
                    HERO_SCRIM_MID_STOP to scrim.copy(alpha = HERO_SCRIM_MID_ALPHA),
                    1.00f to scrim.copy(alpha = HERO_SCRIM_BOTTOM_ALPHA),
                ),
            ),
    )
}

@Composable
private fun HeroTextColumn(
    title: String,
    tagline: String?,
    metaText: String,
    eyebrowText: String?,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .padding(
                start = resolvedContentInset(),
                end = resolvedContentInset(),
                bottom = dimensionResource(R.dimen.detail_hero_text_bottom_padding),
            ),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.displaySmall,
            color = BingeTheme.colors.onScrim,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        if (!eyebrowText.isNullOrBlank()) {
            Spacer(Modifier.height(dimensionResource(R.dimen.padding_s)))
            Text(
                text = eyebrowText.uppercase(),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
            )
        }
        if (!tagline.isNullOrBlank()) {
            Spacer(Modifier.height(dimensionResource(R.dimen.padding_s)))
            Text(
                text = tagline,
                style = MaterialTheme.typography.bodyMedium,
                color = BingeTheme.colors.onScrim.copy(alpha = 0.85f),
                fontStyle = FontStyle.Italic,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
        if (metaText.isNotBlank()) {
            Spacer(Modifier.height(dimensionResource(R.dimen.padding_s)))
            Text(
                text = metaText,
                style = MaterialTheme.typography.bodySmall,
                color = BingeTheme.colors.onScrim.copy(alpha = 0.80f),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
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
