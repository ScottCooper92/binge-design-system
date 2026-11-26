package com.binge.designsystem.template

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.unit.Dp
import com.binge.designsystem.R
import com.binge.designsystem.component.DetailCinematicHeader
import com.binge.designsystem.component.DetailHero
import com.binge.designsystem.component.DetailStat
import com.binge.designsystem.component.ExternalScore
import com.binge.designsystem.isExpandedPane
import com.binge.designsystem.layout.LayoutAnchors
import com.binge.designsystem.layout.layoutAnchor

/**
 * What the expanded header shows beside the backdrop: the [posterUrl], the [synopsis] and the [stats] the single-column page
 * keeps in its body. A page that gives none of it keeps the single-column hero at every width.
 */
class MediaWideHeader(
    val posterUrl: String?,
    val synopsis: String?,
    val stats: List<DetailStat>,
)

/**
 * A media-hero detail page, a film's, a series' or a person's, on [HeroDetailScreen]: the hero runs full-bleed, the bar fades
 * in over it, and [footer] sits below the scroll. The page owns the hero: [DetailHero] with its chrome off, since the
 * template's bar carries back and [topBarActions] (a share action, say, as a [com.binge.designsystem.component.BingeBarIconAction]),
 * the rich backdrop, and the [LayoutAnchors.Detail.HERO] anchor a page's skeleton is checked against. The body runs full-width,
 * and the status bar icons stay light over the hero until the bar's scrim hands them to the theme.
 *
 * @param wideHeader at the expanded width, a page that passes this swaps in [DetailCinematicHeader], with the poster, synopsis and
 *   stats beside the title; without it, or on a narrower window, the single-column hero stays.
 * @param onBack null where the pane beside this one already offers the way back.
 * @param inFlight shows a change under way.
 * @param genres drawn under the title in the accent colour, with the italic [tagline] under them.
 * @param externalScores review scores from other sources, on a line of their own in either header.
 *
 * @need build a film, series or person page: a full-bleed hero that becomes a cinematic header at the expanded width
 * @group detail
 * @replaces a hand-built HeroDetailScreen with its own hero and its expanded-width header
 */
@Composable
fun MediaHeroDetailPage(
    title: String,
    backdropUrl: String?,
    metaText: String,
    onBack: (() -> Unit)?,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    scrollState: ScrollState = rememberScrollState(),
    topBarActions: @Composable RowScope.(glassBackgroundAlpha: Float) -> Unit = {},
    footer: (@Composable () -> Unit)? = null,
    inFlight: Boolean = false,
    metaContent: (@Composable () -> Unit)? = null,
    genres: List<String> = emptyList(),
    tagline: String? = null,
    wideHeader: MediaWideHeader? = null,
    externalScores: List<ExternalScore> = emptyList(),
    body: @Composable ColumnScope.() -> Unit,
) {
    // At the expanded width the cinematic header takes the place of the hero, as Binge's detail pages do.
    val cinematic = wideHeader != null && isExpandedPane()
    HeroDetailScreen(
        title = title,
        onBack = onBack,
        modifier = modifier,
        snackbarHostState = snackbarHostState,
        scrollState = scrollState,
        heroHeight = dimensionResource(if (cinematic) R.dimen.detail_cinematic_header_height else R.dimen.detail_hero_height),
        horizontalInset = dimensionResource(if (cinematic) R.dimen.detail_cinematic_header_padding else R.dimen.padding_m),
        contentMaxWidth = Dp.Infinity,
        // The hero, or the cinematic header at the expanded width, is under the bar: the icons stay light over it until the bar hands them to the theme.
        darkStatusBar = true,
        inFlight = inFlight,
        actions = topBarActions,
        footer = footer,
        hero = {
            if (cinematic && wideHeader != null) {
                DetailCinematicHeader(
                    title = title,
                    genres = genres,
                    synopsis = wideHeader.synopsis,
                    stats = wideHeader.stats,
                    backdropUrl = backdropUrl,
                    posterUrl = wideHeader.posterUrl,
                    tagline = tagline,
                    externalScores = externalScores,
                    modifier = Modifier.layoutAnchor(LayoutAnchors.section(LayoutAnchors.Detail.HERO)),
                )
            } else {
                DetailHero(
                    title = title,
                    backdropUrl = singleColumnBackdrop(backdropUrl, wideHeader?.posterUrl),
                    tagline = tagline,
                    genres = genres,
                    metaText = metaText,
                    onBack = {},
                    showChrome = false,
                    richBackdrop = true,
                    metaContent = metaContent,
                    externalScores = externalScores,
                    modifier = Modifier.layoutAnchor(LayoutAnchors.section(LayoutAnchors.Detail.HERO)),
                )
            }
        },
        content = body,
    )
}

/**
 * The image behind the single-column hero: the title's own backdrop, or its poster where it has none, so the hero is never
 * bare. The expanded header draws the poster itself beside the title, so it takes the backdrop alone and never shows it twice.
 */
internal fun singleColumnBackdrop(backdropUrl: String?, posterUrl: String?): String? = backdropUrl ?: posterUrl
