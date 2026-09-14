package com.binge.designsystem.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.binge.designsystem.R
import com.binge.designsystem.component.DetailSkeletonShape
import com.binge.designsystem.isExpandedLayout
import com.binge.designsystem.layout.LayoutAnchors
import com.binge.designsystem.layout.layoutAnchor
import com.binge.designsystem.theme.BingeExpressiveTheme

/**
 * Loading placeholder for the media-detail screens (movie / TV / episode / person). For the
 * hero-based shapes it mirrors the loaded layout so the skeleton→content swap doesn't reflow: an
 * immersive header with the title copy anchored over the image, a stat row, the primary action row,
 * an overview block, the details list and a cast rail. [DetailSkeletonShape.Person] stands in for a page
 * with none of that structure — see [PersonSkeleton].
 *
 * At [isExpandedLayout] the **media** shape swaps the compact hero for the cinematic
 * backdrop-plus-inline-poster band, matching [DetailCinematicHeader], so the tablet/foldable loading state
 * lands where the content does. Only that shape: the episode page has no cinematic variant —
 * `EpisodeHeaderBand` renders `DetailHero` at every width — so an `expanded`-only branch reserved the 460dp
 * `detail_cinematic_header_height` against the 480dp hero the page draws, and every band anchored below
 * `HERO` inherited the miss.
 *
 * Landing where the content does is a claim about **both** axes. Every resolved detail page caps its
 * reading column at `content_max_width` and centres it from a `BoxWithConstraints` of its own, so this
 * reads the same width and insets its bands from the same measure. Without that the bands sit hard against
 * the start edge on a tablet or unfolded foldable while the content they stand in for lands centred — a
 * cast rail beginning some 180dp left of its own posters. `assertSkeletonReservesGeometry` now compares
 * both edges, but it compared only the top when that shipped, which is how it shipped.
 *
 * [shape] selects which page is being stood in for — see [DetailSkeletonShape] for why the pages cannot all
 * take the default. Every section is tagged with the anchor its resolved counterpart carries, so what this
 * reserves is asserted by `assertSkeletonReservesGeometry` rather than reviewed — see [LayoutAnchors].
 *
 * It scrolls, because every page it stands in for does. Fixed, a short-height window (landscape phone, a
 * height-limited split pane) simply never composited anything below the stat row: the three `phone-land`
 * baselines for Media, Episode and EpisodeSignedOut were byte-identical, despite the shapes differing
 * materially exactly there. That is a real device state, not only a screenshot artefact — the placeholder
 * had no way to reach the rest of itself.
 */
@Composable
fun DetailScreenSkeleton(modifier: Modifier = Modifier, shape: DetailSkeletonShape = DetailSkeletonShape.Media) {
    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        // The same measure MovieDetailContent, TvDetailContent, EpisodeDetailScreen and PersonDetailScreen
        // each compute for themselves — see this function's KDoc for why the skeleton has to as well.
        val centredInset = (maxWidth - dimensionResource(R.dimen.content_max_width)) / 2

        if (shape == DetailSkeletonShape.Person) {
            // The person page floors its reading inset at the profile's own padding rather than at zero, so
            // below the cap it sits 20dp in where a media page sits 16dp — see `PersonDetailScreen`.
            PersonSkeleton(
                readingInset = centredInset.coerceAtLeast(dimensionResource(R.dimen.person_profile_padding_h)),
            )
            return@BoxWithConstraints
        }

        // The media pages add their margin to `screen_content_inset` instead of flooring at it, so the
        // two reduce to the same phone inset by different arithmetic. Mirrored rather than unified: the
        // skeleton's job is to match what each page does, not to decide it.
        val sideMargin = centredInset.coerceAtLeast(0.dp)
        val contentInset = dimensionResource(R.dimen.screen_content_inset) + sideMargin

        val expanded = isExpandedLayout()

        Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
            // The header bleeds to both edges on every width, exactly as the resolved header band does.
            Box(modifier = Modifier.layoutAnchor(LayoutAnchors.section(LayoutAnchors.Detail.HERO))) {
                if (expanded && shape == DetailSkeletonShape.Media) {
                    SkeletonCinematicHeader()
                } else {
                    SkeletonHero()
                }
            }

            Spacer(Modifier.height(dimensionResource(R.dimen.padding_m)))

            SkeletonStatRow(
                modifier = Modifier
                    .layoutAnchor(LayoutAnchors.section(LayoutAnchors.Detail.STATS))
                    .padding(horizontal = sideMargin),
            )

            Spacer(Modifier.height(dimensionResource(R.dimen.hub_content_spacing)))

            when {
                shape == DetailSkeletonShape.Episode -> {
                    SkeletonRatingCard(
                        modifier = Modifier
                            .layoutAnchor(LayoutAnchors.section(LayoutAnchors.Detail.RATING))
                            .padding(horizontal = sideMargin),
                    )
                    Spacer(Modifier.height(dimensionResource(R.dimen.hub_content_spacing)))
                }
                // Signed out, the episode page runs stat row straight into overview, so the skeleton does
                // too. Ordered ahead of the `!expanded` arm deliberately: it is a phone shape, and falling
                // through would draw the action row this page has never had.
                shape == DetailSkeletonShape.EpisodeSignedOut -> Unit
                // The cinematic header carries its own play/add chrome, so an expanded media page renders
                // no action row — promising one reserved a band the page never fills.
                !expanded -> {
                    SkeletonActionRow(
                        modifier = Modifier
                            .layoutAnchor(LayoutAnchors.section(LayoutAnchors.Detail.ACTIONS))
                            .padding(horizontal = sideMargin),
                    )
                    Spacer(Modifier.height(dimensionResource(R.dimen.hub_content_spacing)))
                }
            }

            SkeletonOverview(
                horizontalPadding = contentInset,
                modifier = Modifier.layoutAnchor(LayoutAnchors.section(LayoutAnchors.Detail.OVERVIEW)),
            )

            Spacer(Modifier.height(dimensionResource(R.dimen.hub_content_spacing)))

            SkeletonDetailsSection(horizontalPadding = contentInset)

            Spacer(Modifier.height(dimensionResource(R.dimen.hub_content_spacing)))

            SkeletonCastRow(
                horizontalPadding = contentInset,
                modifier = Modifier.layoutAnchor(LayoutAnchors.section(LayoutAnchors.Detail.CAST)),
            )
        }
    }
}

/**
 * The person page's loading shape, on [readingInset] — the centred reading measure its resolved counterpart
 * uses for its profile block.
 *
 * **The profile block is all of it.** The biography below it is conditional on data —
 * `PersonDetailContent` renders `BiographySection` only `if (state.person.biography.isNotBlank())` — and a
 * blank biography is not the exception: sampling TMDB, 27% of a popular film's top-billed cast have none,
 * 56% of its full cast, and 77% of its crew. Reserving 78dp of overview plates for it collapsed the page on
 * resolve for most of the people a user can actually tap through to.
 *
 * That leaves this shape reserving only what every person page has, which is the rule already followed for
 * the gallery strip, the known-for rail and the external links — biography just was not held to it. The
 * cost is that a person who *does* have one gets no placeholder below the profile block; that grows the
 * page downward under a header that stays put, rather than yanking it up.
 *
 * The offsets ride the column, not the anchored block, because that is where the resolved page puts them:
 * its scroll column carries the status-bar inset and the top-bar offset, and the profile block starts below
 * both. Anchoring inside them would compare two different edges.
 */
@Composable
private fun PersonSkeleton(readingInset: Dp) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .statusBarsPadding()
            .padding(top = dimensionResource(R.dimen.person_profile_top_bar_offset)),
    ) {
        SkeletonProfileHeader(
            horizontalPadding = readingInset,
            modifier = Modifier.layoutAnchor(LayoutAnchors.section(LayoutAnchors.Detail.PROFILE)),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewDetailScreenSkeleton() {
    BingeExpressiveTheme {
        DetailScreenSkeleton()
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewEpisodeDetailScreenSkeleton() {
    BingeExpressiveTheme {
        DetailScreenSkeleton(shape = DetailSkeletonShape.Episode)
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewEpisodeSignedOutDetailScreenSkeleton() {
    BingeExpressiveTheme {
        DetailScreenSkeleton(shape = DetailSkeletonShape.EpisodeSignedOut)
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewPersonDetailScreenSkeleton() {
    BingeExpressiveTheme {
        DetailScreenSkeleton(shape = DetailSkeletonShape.Person)
    }
}
