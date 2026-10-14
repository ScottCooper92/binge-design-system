package com.binge.designsystem.tv.template

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import coil3.compose.SubcomposeAsyncImage
import com.binge.designsystem.BACKDROP_ASPECT_RATIO
import com.binge.designsystem.CARD_ASPECT_RATIO
import com.binge.designsystem.theme.BingeShapes
import com.binge.designsystem.tv.focus.TvOverlayArrivalFocusEffect
import com.binge.designsystem.tv.focus.tvEndDirectionKey
import com.binge.designsystem.tv.focus.tvFocusTarget
import com.binge.designsystem.tv.focus.tvStartDirectionKey
import kotlinx.coroutines.launch
import com.binge.designsystem.R as DesR
import com.binge.designsystem.tv.R as TvR

/**
 * The backdrop is kept as a dim wash behind the copy rather than a legible image — this is a reading
 * surface, not a hero — so the scrim sits high. Short of opaque so the artwork still tints the screen.
 */
private const val OVERVIEW_SCRIM_ALPHA = 0.86f

/**
 * One ↓ press's worth of overview text, in px at this density — a paragraph-ish step that keeps reading
 * position, not a page jump. Matches the old overview sheet's step.
 */
private const val OVERVIEW_SCROLL_STEP = 240f

/**
 * A full-screen reader for long text: dimmed artwork, a still on the left, and the title with the **full**,
 * D-pad-scrollable text beside it. An overlay, not a route: unlike `TvSideSheet`, which registers its own
 * Back, the caller's `BackHandler` dismisses it.
 *
 * [artwork] is the full-bleed layer behind the scrim and defaults to a flat plate, since this module knows
 * no image source. [posterUrl] is the left still when the caller has one, [backdropUrl] otherwise.
 * Supplying [onPrevious] or [onNext] turns it into a reader over a list and claims both horizontal keys, so
 * only BACK dismisses; [footer] is where the key hint goes.
 */
@Composable
fun TvOverviewScreen(
    title: String,
    overview: String,
    backdropUrl: String?,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    posterUrl: String? = null,
    subtitle: String? = null,
    scrollState: ScrollState = rememberScrollState(),
    onPrevious: (() -> Unit)? = null,
    onNext: (() -> Unit)? = null,
    footer: (@Composable () -> Unit)? = null,
    artwork: @Composable () -> Unit = { TvOverviewPlate() },
) {
    val scope = rememberCoroutineScope()
    val focus = remember { FocusRequester() }
    val startKey = tvStartDirectionKey()
    val endKey = tvEndDirectionKey()
    val crossing = onPrevious != null || onNext != null
    TvOverlayArrivalFocusEffect(focus)
    Box(modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        artwork()
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background.copy(alpha = OVERVIEW_SCRIM_ALPHA)),
        )
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    start = dimensionResource(TvR.dimen.tv_overview_content_inset),
                    end = dimensionResource(TvR.dimen.tv_overscan_horizontal),
                )
                // The start-edge key closes, ↓/↑ scroll the text — the surface is a single focus stop, so a
                // directional press has no other target and would otherwise fall through to the shell. That
                // key mirrors with the panel under RTL (#2108).
                .onPreviewKeyEvent { event ->
                    if (event.type != KeyEventType.KeyDown) return@onPreviewKeyEvent false
                    when {
                        // Crossing claims both horizontal keys, so the start edge stops closing and BACK
                        // becomes the only way out. Deliberate: a reader that steps between items cannot
                        // also treat one of those steps as "leave", and the footer hint says which is which.
                        crossing && event.key == startKey -> {
                            onPrevious?.invoke()
                            true
                        }
                        crossing && event.key == endKey -> {
                            onNext?.invoke()
                            true
                        }
                        event.key == startKey -> {
                            onDismiss()
                            true
                        }
                        event.key == Key.DirectionDown -> {
                            scope.launch { scrollState.animateScrollBy(OVERVIEW_SCROLL_STEP) }
                            true
                        }
                        event.key == Key.DirectionUp -> {
                            scope.launch { scrollState.animateScrollBy(-OVERVIEW_SCROLL_STEP) }
                            true
                        }
                        else -> false
                    }
                }.focusRequester(focus)
                .tvFocusTarget { }
                .verticalScroll(scrollState)
                // Vertical overscan inset as content padding (inside the scroll), so the title clears the
                // panel's top edge at rest yet a long synopsis still scrolls the full height to the edge.
                .padding(
                    top = dimensionResource(TvR.dimen.tv_overscan_vertical) + com.binge.designsystem.tv.nav.LocalTvTopInset.current,
                    bottom = dimensionResource(TvR.dimen.tv_overscan_vertical),
                ),
            horizontalArrangement = Arrangement.spacedBy(dimensionResource(DesR.dimen.padding_l)),
        ) {
            TvOverviewArtwork(posterUrl = posterUrl, backdropUrl = backdropUrl)
            TvOverviewCopy(title = title, subtitle = subtitle, overview = overview)
        }
        // Outside the scroll and bottom-aligned, so a key hint or a position read-out stays put while the
        // body moves under it — the one thing on a pointerless surface that must not scroll away.
        if (footer != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(
                        start = dimensionResource(TvR.dimen.tv_overview_content_inset),
                        end = dimensionResource(TvR.dimen.tv_overscan_horizontal),
                        bottom = dimensionResource(TvR.dimen.tv_overscan_vertical),
                    ),
            ) {
                footer()
            }
        }
    }
}

/** A flat wash, so the copy still has a bed when the caller supplies no artwork. */
@Composable
private fun TvOverviewPlate() {
    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background))
}

/** The leading still: the caller's portrait when it has one, the 16:9 backdrop otherwise. */
@Composable
private fun TvOverviewArtwork(posterUrl: String?, backdropUrl: String?) {
    val portrait = posterUrl != null
    Box(
        modifier = Modifier
            .width(
                dimensionResource(
                    if (portrait) TvR.dimen.tv_overview_poster_width else TvR.dimen.tv_overview_backdrop_width,
                ),
            ).aspectRatio(if (portrait) CARD_ASPECT_RATIO else BACKDROP_ASPECT_RATIO)
            .clip(BingeShapes.MediaCard)
            .background(MaterialTheme.colorScheme.surfaceVariant),
    ) {
        SubcomposeAsyncImage(
            model = posterUrl ?: backdropUrl,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
            loading = {},
            error = {},
        )
    }
}

/** Title, the optional quieter line under it, and the body — capped at a readable measure. */
@Composable
private fun TvOverviewCopy(
    title: String,
    subtitle: String?,
    overview: String,
) {
    Column(verticalArrangement = Arrangement.spacedBy(dimensionResource(DesR.dimen.padding_l))) {
        Text(
            text = title,
            style = MaterialTheme.typography.displaySmall,
            color = MaterialTheme.colorScheme.onSurface,
        )
        if (subtitle != null) {
            Text(
                text = subtitle,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Text(
            text = overview,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            overflow = TextOverflow.Clip,
            modifier = Modifier.widthIn(max = dimensionResource(TvR.dimen.tv_overview_text_max_width)),
        )
    }
}
