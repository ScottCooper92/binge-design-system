package com.binge.designsystem.tv.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.em
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.binge.designsystem.CARD_ASPECT_RATIO
import com.binge.designsystem.theme.BingeShapes
import com.binge.designsystem.tv.focus.tvClickable
import com.binge.designsystem.tv.focus.tvFocusIndicator
import com.binge.designsystem.tv.nav.tvContentGutterStart
import com.binge.designsystem.R as DesR
import com.binge.designsystem.tv.R as TvR

/** The separator between facts on one line. */
private const val TV_DETAIL_META_SEPARATOR = "  ·  "

/** Wide tracking makes the all-caps overline read as a label rather than shouting. */
private val OVERLINE_TRACKING = 0.14.em

private const val TITLE_MAX_LINES = 2

/**
 * Fixed (min == max), not just capped: a short overview must not collapse the block and shift the metadata
 * line — the band's geometry has to hold still whatever the data returns.
 */
private const val OVERVIEW_LINES = 3

/**
 * The content of a TV detail hero, already formatted. [overline] is the small caps line over the title (a genre
 * line, a kind of thing), [facts] one line of meta under the synopsis, [certification] an age rating shown boxed.
 */
data class TvDetailHeroItem(
    val title: String,
    val overline: String? = null,
    val overview: String? = null,
    val facts: List<String> = emptyList(),
    val certification: String? = null,
)

/**
 * Makes the hero synopsis a focusable block whose OK runs [onClick]. Focus is a parameter ([isFocused] in,
 * [onFocusChanged] out) so the state is screenshot-testable. [focusRequester] is for a caller aiming page entry at
 * the synopsis when the band has no action row — the one focus stop such a band has.
 */
data class TvHeroOverview(
    val isFocused: Boolean,
    val onFocusChanged: (Boolean) -> Unit,
    val onClick: () -> Unit,
    val focusRequester: FocusRequester? = null,
)

/**
 * A detail page's opening band: an end-aligned backdrop fading into the background on its left, the poster at
 * the gutter, and a copy column of overline, title, clamped synopsis and one line of facts, with the action row
 * pinned at the foot. It scrolls with the page, unlike an immersive hub's pinned, per-card-repainting backdrop.
 *
 * [artwork] and [poster] are slots so the design system needs no image loader and a screenshot with no network
 * is not stuck baking a fallback plate.
 */
@Composable
fun TvDetailHero(
    item: TvDetailHeroItem,
    artwork: @Composable () -> Unit,
    poster: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    overview: TvHeroOverview? = null,
    overviewLines: Int = OVERVIEW_LINES,
    actions: @Composable () -> Unit,
) {
    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .height(dimensionResource(TvR.dimen.tv_detail_page_hero_height)),
    ) {
        TvHeroArtworkPanel(
            veilColor = MaterialTheme.colorScheme.background,
            bedFoot = true,
            richBackdrop = true,
            artwork = artwork,
        )
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(
                        start = tvContentGutterStart(),
                        end = dimensionResource(TvR.dimen.tv_overscan_horizontal),
                        top = dimensionResource(TvR.dimen.tv_detail_page_hero_content_top),
                    ),
        ) {
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(dimensionResource(TvR.dimen.tv_detail_page_hero_gap)),
            ) {
                Box(
                    modifier =
                        Modifier
                            .width(dimensionResource(TvR.dimen.tv_detail_page_poster_width))
                            .aspectRatio(CARD_ASPECT_RATIO)
                            .clip(BingeShapes.MediaCard)
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                ) {
                    poster()
                }
                TvDetailHeroCopy(
                    item = item,
                    overview = overview,
                    overviewLines = overviewLines,
                    modifier = Modifier.width(dimensionResource(TvR.dimen.tv_detail_page_copy_width)),
                )
            }
            actions()
        }
    }
}

@Composable
private fun TvDetailHeroCopy(
    item: TvDetailHeroItem,
    overview: TvHeroOverview?,
    overviewLines: Int,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(dimensionResource(TvR.dimen.tv_detail_page_copy_gap)),
    ) {
        item.overline?.takeIf { it.isNotBlank() }?.let {
            Text(
                // Upper-cased here, not by the caller: the case is typography, not data.
                text = it.uppercase(),
                style = MaterialTheme.typography.labelLarge.copy(letterSpacing = OVERLINE_TRACKING),
                color = MaterialTheme.colorScheme.primary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Text(
            text = item.title,
            style = MaterialTheme.typography.displayMedium,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = TITLE_MAX_LINES,
            overflow = TextOverflow.Ellipsis,
        )
        item.overview?.takeIf { it.isNotBlank() }?.let { text -> TvHeroSynopsis(text, overview, overviewLines) }
        TvDetailHeroMeta(item)
    }
}

/** The synopsis, clamped to a fixed line count so a short one cannot drag the metadata line up under the title. */
@Composable
private fun TvHeroSynopsis(
    text: String,
    overview: TvHeroOverview?,
    overviewLines: Int,
) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        minLines = overviewLines,
        maxLines = overviewLines,
        overflow = TextOverflow.Ellipsis,
        modifier =
            if (overview == null) {
                Modifier
            } else {
                Modifier
                    .then(overview.focusRequester?.let { Modifier.focusRequester(it) } ?: Modifier)
                    .tvFocusIndicator(isFocused = overview.isFocused, shape = BingeShapes.Medium)
                    .tvClickable(onFocusChanged = overview.onFocusChanged, onClick = overview.onClick)
                    // Inside the ring, so the outline floats clear of the text rather than crowding it.
                    .padding(dimensionResource(TvR.dimen.tv_detail_page_synopsis_padding))
            },
    )
}

/** `2026 · Pending · Claude · 3 weeks ago`, with the age rating boxed beside it. */
@Composable
private fun TvDetailHeroMeta(item: TvDetailHeroItem) {
    val facts = item.facts.filter { it.isNotBlank() }.joinToString(TV_DETAIL_META_SEPARATOR)
    val certification = item.certification?.takeIf { it.isNotBlank() }
    if (facts.isEmpty() && certification == null) return
    Row(
        horizontalArrangement = Arrangement.spacedBy(dimensionResource(TvR.dimen.tv_detail_page_meta_gap)),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (facts.isNotEmpty()) {
            Text(
                text = facts,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        certification?.let { TvDetailHeroCertification(it) }
    }
}

/** The age rating, boxed. Bordered rather than filled: it is a classification, not a state of anything. */
@Composable
private fun TvDetailHeroCertification(certification: String) {
    Text(
        text = certification,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        maxLines = 1,
        modifier =
            Modifier
                .border(
                    width = dimensionResource(DesR.dimen.hairline_thickness),
                    color = MaterialTheme.colorScheme.border,
                    shape = BingeShapes.Tag,
                ).padding(
                    horizontal = dimensionResource(TvR.dimen.tv_detail_page_certification_padding_horizontal),
                    vertical = dimensionResource(TvR.dimen.tv_detail_page_certification_padding_vertical),
                ),
    )
}
