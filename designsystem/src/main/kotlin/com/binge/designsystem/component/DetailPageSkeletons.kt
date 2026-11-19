package com.binge.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.TextStyle
import com.binge.designsystem.R
import com.binge.designsystem.layout.LayoutAnchors
import com.binge.designsystem.layout.layoutAnchor
import com.binge.designsystem.modifier.skeleton
import com.binge.designsystem.resolvedContentPadding
import com.binge.designsystem.template.readingMargin
import com.binge.designsystem.theme.BingeShapes

private const val PLACEHOLDER_TITLE = "A Long Title On Two Lines"
private const val PLACEHOLDER_TAGLINE = "A tagline that runs one line, no more"
private val PLACEHOLDER_GENRES = listOf("Action", "Adventure", "Science Fiction")
private const val STAT_COUNT = 3
private const val OVERVIEW_LINE_COUNT = 3
private const val OVERVIEW_LAST_LINE_FRACTION = 0.6f

/**
 * The hero of a [MediaHeroDetailPage] while it loads: the page's own [DetailHero] laid out on placeholder text under one plain
 * shimmer, so its height is the loaded hero's, which grows with the title, the genres and the tagline, and no placeholder image is
 * drawn in it. The placeholder is a typical film's: a title over two lines, three genres and a tagline of one. Its semantics are
 * cleared, since the text is never shown or read out. When [cinematic] (the expanded width), the cinematic header has a fixed
 * height, so a plate of that height stands in for it. It carries the [LayoutAnchors.Detail.HERO] anchor.
 *
 * @need hold a media page's hero at its loaded height while the page loads
 * @group detail
 * @replaces a hand-built shimmer plate for a detail hero
 * @sample MediaPageSkeletonSample
 */
@Composable
fun DetailHeroSkeleton(
    cinematic: Boolean,
    modifier: Modifier = Modifier,
    title: String = PLACEHOLDER_TITLE,
    tagline: String? = PLACEHOLDER_TAGLINE,
    genres: List<String> = PLACEHOLDER_GENRES,
    metaText: String = "",
) {
    val anchor = modifier.fillMaxWidth().layoutAnchor(LayoutAnchors.section(LayoutAnchors.Detail.HERO))
    if (cinematic) {
        Box(anchor.height(dimensionResource(R.dimen.detail_cinematic_header_height)).skeleton(visible = true, shape = RectangleShape))
    } else {
        Box(anchor.clearAndSetSemantics {}.skeleton(visible = true, shape = RectangleShape)) {
            DetailHero(
                title = title,
                backdropUrl = null,
                tagline = tagline,
                metaText = metaText,
                onBack = {},
                genres = genres,
                showChrome = false,
                richBackdrop = true,
            )
        }
    }
}

/**
 * The stat rows of a [DetailStatsRows] while the page loads: [count] blank stats in the page's own [DetailStatRow], laid out by
 * [statsPerRow], so a large font breaks them into the rows the page does and each is the height of the page's. The shimmer covers
 * the row. The first row carries the [LayoutAnchors.Detail.STATS] anchor through [modifier].
 *
 * @need hold a media page's stat rows at their loaded height while the page loads
 * @group detail
 * @replaces a hand-built shimmer plate for a row of stats
 */
@Composable
fun DetailStatsRowsSkeleton(modifier: Modifier = Modifier, count: Int = STAT_COUNT) {
    val blank = List(count) { DetailStat(Icons.Filled.Star, " ", " ") }
    Column(Modifier.readingMargin(), verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_m))) {
        blank.chunked(statsPerRow()).forEachIndexed { index, row ->
            Box((if (index == 0) modifier else Modifier).fillMaxWidth().padding(resolvedContentPadding())) {
                DetailStatRow(row, Modifier.skeleton(visible = true, shape = BingeShapes.Large), contentPadding = PaddingValues())
            }
        }
    }
}

/**
 * The heading, the collapsed synopsis and the expand toggle's row of an [DetailOverviewSection] while the page loads. Each line is a blank
 * [Text] in the page's own style under a plate, so its height follows the font as the page's does, and the toggle is the touch
 * target the page's button is.
 *
 * @need hold a media page's overview at its loaded height while the page loads
 * @group detail
 * @replaces a hand-built shimmer plate for a synopsis
 */
@Composable
fun DetailOverviewSkeleton(modifier: Modifier = Modifier) {
    Column(modifier = Modifier.padding(horizontal = dimensionResource(R.dimen.padding_m)).then(modifier)) {
        TextPlate(MaterialTheme.typography.titleMedium, Modifier.width(dimensionResource(R.dimen.skeleton_header_width)))
        Spacer(Modifier.height(dimensionResource(R.dimen.detail_meta_spacing)))
        repeat(OVERVIEW_LINE_COUNT) { index ->
            TextPlate(
                MaterialTheme.typography.bodyMedium,
                Modifier.fillMaxWidth(if (index == OVERVIEW_LINE_COUNT - 1) OVERVIEW_LAST_LINE_FRACTION else 1f),
            )
        }
        Box(Modifier.minimumInteractiveComponentSize(), contentAlignment = Alignment.CenterStart) {
            TextPlate(MaterialTheme.typography.labelLarge, Modifier.width(dimensionResource(R.dimen.skeleton_header_width)))
        }
    }
}

/**
 * A plate one line of [style] tall: blank text sets the height, so it is the line's at any font size, and the shimmer is inset from
 * the top and bottom of it, so lines stacked one on the next read as separate bars.
 *
 * @need hold a line of text's height with a shimmer plate while its content loads
 * @group detail
 * @replaces a hand-built plate whose height is a fixed dp instead of the font's
 */
@Composable
fun TextPlate(style: TextStyle, modifier: Modifier = Modifier) {
    Box(modifier) {
        Text(text = " ", style = style, modifier = Modifier.alpha(0f))
        Box(
            Modifier
                .matchParentSize()
                .padding(vertical = dimensionResource(R.dimen.detail_cast_avatar_label_spacing))
                .skeleton(visible = true, shape = BingeShapes.Tag),
        )
    }
}
