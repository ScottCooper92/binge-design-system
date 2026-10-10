package com.binge.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import coil3.compose.SubcomposeAsyncImage
import com.binge.designsystem.CARD_ASPECT_RATIO
import com.binge.designsystem.R
import com.binge.designsystem.component.HeroBackdropMeshWash
import com.binge.designsystem.component.ImagePlaceholder
import com.binge.designsystem.layout.LayoutAnchors
import com.binge.designsystem.layout.layoutAnchor
import com.binge.designsystem.theme.BingeShapes
import com.binge.designsystem.theme.BingeTheme

private const val CINEMATIC_SYNOPSIS_ALPHA = 0.85f

// The poster (196dp wide, 2:3) runs ~294dp tall against a 460dp header. The copy column now fills
// that same height and pins the facts row to its bottom edge, so this clamp is a safety cap for
// when the facts row is present too - not the full poster-height budget synopsis alone would get.
private const val CINEMATIC_SYNOPSIS_MAX_LINES = 4

/**
 * Immersive expanded-width detail header: full-bleed backdrop gradient-blended into the background,
 * and an inline poster beside a copy column (title → genres → [tagline] → [synopsis] → [stats]). The
 * copy sits at the column's foot, just above the facts row, so the fade behind it can stay low. Sizes to its
 * container width so it composes correctly in a side pane; the height is the fixed cinematic header
 * height. The expanded counterpart to [DetailHero]; the single-column detail keeps using
 * [DetailHero].
 *
 * Shows the synopsis rather than a tagline, clamped to [CINEMATIC_SYNOPSIS_MAX_LINES], and the
 * rating/year/runtime/certification facts row beneath it — matching where TV's own hero puts both.
 * The primary actions no longer live here — a caller renders them lower on the page instead, within
 * thumb reach of where the header's fixed height would otherwise have pinned them.
 *
 * Draws no chrome of its own. Back and share come from the pinned [DetailOverlayTopBar] that both
 * detail widths share — a header that carried its own controls scrolled them away with itself and
 * left no way back, and its controls collided with this poster once the status bar inset grew past
 * 46dp (as it does on a large foldable).
 *
 * The backing behind the copy hugs it rather than covering the art. [CopyHuggingScrim] fades the page
 * colour up behind the title, genres, tagline and synopsis and falls away past the widest of them, so a short
 * title leaves the far side of the art raw. A second, stronger band runs the full width behind the
 * facts row, which spans it. Both are placed from the copy's measured bounds, so they follow a title
 * that wraps or a tagline that is missing.
 */
@Composable
fun DetailCinematicHeader(
    title: String,
    genres: List<String>,
    synopsis: String?,
    stats: List<DetailStat>,
    backdropUrl: String?,
    posterUrl: String?,
    modifier: Modifier = Modifier,
    // Seeds CinematicSynopsis's overflow state the same way ExpandableOverview's own
    // initiallyOverflowing does, and for the same reason: onTextLayout fires a frame too late for
    // the preview screenshot lane. Preview and test use only.
    synopsisInitiallyOverflowing: Boolean = false,
    // An italic line under the genres, for a caller that shows the synopsis further down the page.
    tagline: String? = null,
    // Replaces the text title, for a header that shows a title logo. The caller owns loading and tinting,
    // and gives it a fixed height so text and logo swap without moving the layout.
    titleContent: (@Composable () -> Unit)? = null,
) {
    val eyebrow = genres.takeIf { it.isNotEmpty() }?.joinToString(" · ")
    var headerOrigin by remember { mutableStateOf(Offset.Zero) }
    val copyBounds = remember { CinematicCopyBounds() }
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(dimensionResource(R.dimen.detail_cinematic_header_height))
            .onGloballyPositioned { headerOrigin = it.positionInRoot() },
    ) {
        SubcomposeAsyncImage(
            model = backdropUrl,
            contentDescription = title,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
            // The copy column below stretches up to the poster's height, reaching well past the
            // backdrop's vertical centre - ImagePlaceholder's default centred icon would land on
            // top of the synopsis. Top-aligned keeps it in the band above the copy row, which the
            // fixed header height and the row's bottom alignment guarantee stays clear of text.
            loading = { ImagePlaceholder(Modifier.fillMaxSize(), iconAlignment = Alignment.TopCenter) },
            error = { ImagePlaceholder(Modifier.fillMaxSize(), iconAlignment = Alignment.TopCenter) },
        )
        HeroBackdropMeshWash(
            accentStart = MaterialTheme.colorScheme.primaryFixedDim,
            accentEnd = BingeTheme.colors.accentPurple,
        )
        CopyHuggingScrim(copyBounds) { headerOrigin }

        CinematicCopyRow(
            title = title,
            eyebrow = eyebrow,
            tagline = tagline,
            synopsis = synopsis,
            stats = stats,
            posterUrl = posterUrl,
            synopsisInitiallyOverflowing = synopsisInitiallyOverflowing,
            copyBounds = copyBounds,
            titleContent = titleContent,
            modifier = Modifier.align(Alignment.BottomStart),
        )
    }
}

@Composable
private fun CinematicCopyRow(
    title: String,
    eyebrow: String?,
    tagline: String?,
    synopsis: String?,
    stats: List<DetailStat>,
    posterUrl: String?,
    synopsisInitiallyOverflowing: Boolean,
    copyBounds: CinematicCopyBounds,
    titleContent: (@Composable () -> Unit)?,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                start = dimensionResource(R.dimen.detail_cinematic_header_padding),
                end = dimensionResource(R.dimen.detail_cinematic_header_padding),
            )
            // No bottom padding here — every caller already follows this header with its own
            // spacer, and stacking one here doubled that gap. IntrinsicSize.Max below lets the
            // copy column's fillMaxHeight() latch onto the poster's own (aspectRatio-computed) height.
            .height(IntrinsicSize.Max),
        horizontalArrangement =
            Arrangement.spacedBy(dimensionResource(R.dimen.detail_cinematic_poster_copy_spacing)),
        verticalAlignment = Alignment.Top,
    ) {
        SubcomposeAsyncImage(
            model = posterUrl,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .width(dimensionResource(R.dimen.detail_cinematic_poster_width))
                .aspectRatio(CARD_ASPECT_RATIO)
                .clip(BingeShapes.MediaCard),
            loading = { ImagePlaceholder(Modifier.fillMaxSize()) },
            error = { ImagePlaceholder(Modifier.fillMaxSize()) },
        )
        Column(modifier = Modifier.weight(1f).fillMaxHeight()) {
            // Pushes the copy down to sit just above the facts row, so the fade behind it stays low
            // and the art above it stays raw.
            Spacer(Modifier.weight(1f))
            Spacer(Modifier.onGloballyPositioned { copyBounds.copyTop = it.positionInRoot().y })
            val titleStyle = DetailHeroDefaults.cinematicTitleStyle()
            val lineGap = dimensionResource(R.dimen.padding_xs)
            if (titleContent != null) {
                Box(reportsLineEnd(copyBounds, CopyLine.Title)) {
                    CompositionLocalProvider(
                        LocalTextStyle provides titleStyle,
                        LocalContentColor provides MaterialTheme.colorScheme.onBackground,
                    ) { titleContent() }
                }
            } else {
                Text(
                    text = title,
                    modifier = reportsLineEnd(copyBounds, CopyLine.Title),
                    style = titleStyle,
                    color = MaterialTheme.colorScheme.onBackground,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    autoSize = DetailHeroDefaults.cinematicTitleAutoSize(),
                )
            }
            if (!eyebrow.isNullOrBlank()) {
                Spacer(Modifier.height(lineGap))
                Text(
                    text = eyebrow,
                    modifier = reportsLineEnd(copyBounds, CopyLine.Genres),
                    style = MaterialTheme.typography.labelLarge,
                    // Matches DetailHero and the TV hero, both of which key genres off the theme's
                    // accent.
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            if (!tagline.isNullOrBlank()) {
                Spacer(Modifier.height(lineGap))
                Text(
                    text = tagline,
                    modifier = reportsLineEnd(copyBounds, CopyLine.Tagline),
                    style = MaterialTheme.typography.bodyMedium,
                    fontStyle = FontStyle.Italic,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = CINEMATIC_SYNOPSIS_ALPHA),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            if (!synopsis.isNullOrBlank()) {
                Spacer(Modifier.height(dimensionResource(R.dimen.detail_cinematic_copy_spacing)))
                CinematicSynopsis(
                    synopsis,
                    initiallyOverflowing = synopsisInitiallyOverflowing,
                    modifier = reportsLineEnd(copyBounds, CopyLine.Synopsis),
                )
            }
            Spacer(Modifier.height(dimensionResource(R.dimen.detail_cinematic_copy_spacing)))
            if (stats.isNotEmpty()) {
                DisposableEffect(copyBounds) { onDispose { copyBounds.statsTop = null } }
                DetailStatRow(
                    stats = stats,
                    valueColor = MaterialTheme.colorScheme.onBackground,
                    labelColor = MaterialTheme.colorScheme.onBackground.copy(alpha = CINEMATIC_SYNOPSIS_ALPHA),
                    modifier = Modifier
                        .layoutAnchor(LayoutAnchors.section(LayoutAnchors.Detail.STATS))
                        .onGloballyPositioned { copyBounds.statsTop = it.positionInRoot().y },
                )
            }
        }
    }
}

/** The copy lines whose end edges [CopyHuggingScrim] falls away from. */
private enum class CopyLine { Title, Genres, Tagline, Synopsis }

/**
 * Where the cinematic copy sits, in root coordinates, as the layout reports it: the top of the copy,
 * the end edge of each copy line, and the top of the facts row.
 */
@Stable
private class CinematicCopyBounds {
    var copyTop by mutableStateOf<Float?>(null)
    var statsTop by mutableStateOf<Float?>(null)

    /** Each line's horizontal extent, as (left, right). */
    val lines = mutableStateMapOf<CopyLine, Pair<Float, Float>>()

    /**
     * The copy's end edge, in root coordinates: the furthest right of any line in a left-to-right
     * layout, the furthest left in a right-to-left one.
     */
    fun copyEnd(layoutDirection: LayoutDirection): Float? =
        if (layoutDirection == LayoutDirection.Rtl) {
            lines.values.minOfOrNull { it.first }
        } else {
            lines.values.maxOfOrNull { it.second }
        }
}

/**
 * Reports [line]'s extent into [bounds] and forgets it when the line leaves composition, so a header
 * reused for another title does not fade around a line that is no longer there.
 */
@Composable
private fun reportsLineEnd(bounds: CinematicCopyBounds, line: CopyLine): Modifier {
    DisposableEffect(bounds, line) { onDispose { bounds.lines.remove(line) } }
    return Modifier.onGloballyPositioned {
        val left = it.positionInRoot().x
        bounds.lines[line] = left to left + it.size.width
    }
}

/**
 * The page-colour backing behind the cinematic copy, in two layers.
 *
 * The copy fade eases in over [R.dimen.hero_copy_fade_band] above the title, is 45% at the title and
 * solid at the header's foot. It is masked horizontally: full behind the copy, falling away over
 * [R.dimen.hero_copy_fade_falloff] past the widest copy line, so the art beyond a short title stays
 * raw.
 *
 * The stats band runs the full width, because the facts row does. It eases in over the same band
 * above the row, is 62% at the row's top and 85% halfway down it, which keeps the row's small labels
 * legible over any art, and solid at the foot.
 *
 * Draws nothing until the copy has been measured. [headerOrigin] is a lambda so the scroll-driven
 * position is read in the draw phase and does not recompose the header.
 */
@Composable
private fun CopyHuggingScrim(bounds: CinematicCopyBounds, headerOrigin: () -> Offset) {
    val page = MaterialTheme.colorScheme.background
    val density = LocalDensity.current
    val bandPx = with(density) { dimensionResource(R.dimen.hero_copy_fade_band).toPx() }
    val falloffPx = with(density) { dimensionResource(R.dimen.hero_copy_fade_falloff).toPx() }
    Box(
        modifier = Modifier
            .fillMaxSize()
            // The horizontal mask applies to the copy fade only, so it needs its own layer to mask into.
            .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
            .drawWithContent {
                val origin = headerOrigin()
                val copyTop = bounds.copyTop ?: return@drawWithContent
                val copyEnd = bounds.copyEnd(layoutDirection) ?: return@drawWithContent
                drawPageFade(page, copyTop = copyTop - origin.y, bandPx = bandPx, ramp = CopyFadeRamp)
                maskPastCopyEnd(copyEnd = copyEnd - origin.x, falloffPx = falloffPx)
                bounds.statsTop?.let { statsTop ->
                    drawPageFade(page, copyTop = statsTop - origin.y, bandPx = bandPx, ramp = StatsFadeRamp)
                }
            },
    )
}

/**
 * The synopsis line inside the cinematic header - clamped to [CINEMATIC_SYNOPSIS_MAX_LINES], with
 * a "Show more" that opens the untruncated text in a sheet. Shown only once the clamp actually
 * cuts something, via the same `onTextLayout`/`hasVisualOverflow` check [ExpandableOverview] uses -
 * the header's fixed height rules out growing the text in place the way that component does.
 *
 * [initiallyOverflowing] seeds that state for the same reason [ExpandableOverview]'s own parameter
 * of that name does: `onTextLayout` fires a frame after the one the preview screenshot lane
 * captures, so without seeding it the "Show more" state is unrenderable there. Preview and test use
 * only - at runtime the layout pass reports the truth.
 */
@Composable
internal fun CinematicSynopsis(
    text: String,
    modifier: Modifier = Modifier,
    initiallyOverflowing: Boolean = false,
) {
    var overflows by remember(text, initiallyOverflowing) { mutableStateOf(initiallyOverflowing) }
    var showSheet by rememberSaveable(text) { mutableStateOf(false) }

    Text(
        text = text,
        modifier = modifier,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onBackground.copy(alpha = CINEMATIC_SYNOPSIS_ALPHA),
        maxLines = CINEMATIC_SYNOPSIS_MAX_LINES,
        overflow = TextOverflow.Ellipsis,
        onTextLayout = { if (!overflows) overflows = it.hasVisualOverflow },
    )
    if (overflows) {
        TextButton(
            onClick = { showSheet = true },
            contentPadding = PaddingValues(dimensionResource(R.dimen.zero)),
        ) {
            Text(
                text = stringResource(R.string.detail_show_more),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
    if (showSheet) {
        BingeBottomSheet(onDismissRequest = { showSheet = false }) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(dimensionResource(R.dimen.padding_l)),
            )
        }
    }
}
