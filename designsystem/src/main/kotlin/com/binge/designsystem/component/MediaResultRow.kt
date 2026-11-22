package com.binge.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.IntrinsicMeasurable
import androidx.compose.ui.layout.IntrinsicMeasureScope
import androidx.compose.ui.layout.LayoutModifier
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.MeasureResult
import androidx.compose.ui.layout.MeasureScope
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import com.binge.designsystem.CARD_ASPECT_RATIO
import com.binge.designsystem.R
import com.binge.designsystem.modifier.describedAs
import com.binge.designsystem.theme.BingeShapes
import com.binge.designsystem.theme.BingeTheme

/** What a [MediaResult] shows beside its text. */
enum class MediaResultKind {
    /** A portrait poster. */
    Poster,

    /** A round portrait of a person. */
    Avatar,

    /** A company or network logo on a tonal tile. */
    Logo,

    /** A 16:9 still, for an episode. */
    Still,
}

/**
 * One result of a search, as plain values. [key] is the app's identity for it as text, so a lazy list can save it.
 * [subtitle] is a line the app has already worded (a kind and a year, a role); [meta] is a second muted line, such as genres. [favourite] and
 * [watched] draw a heart and a tick beside the name, the same marks a poster card draws, so an app words a mark one way across the grid and
 * the search; the `trailing` slot of the row stays for anything else. [blurb] is a short passage (an overview), drawn last in up to three lines
 * and ellipsized; a result with none takes no room for it.
 *
 * [nameSuffix] is drawn after the name in the primary colour (a count, a handle), and a long name ellipsizes before the suffix does, so the suffix
 * stays visible. [statusInline] moves the subtitle off its own line: the row then has no subtitle line, and one wrapping row reads the status first,
 * then the subtitle text, the rating and the [meta]. Both are off by default, so a result that sets neither draws as it always has.
 */
@Immutable
class MediaResult(
    val key: String,
    val kind: MediaResultKind,
    val imageUrl: String?,
    val name: String,
    val subtitle: String? = null,
    val rating: Float? = null,
    val meta: String? = null,
    val favourite: Boolean = false,
    val watched: Boolean = false,
    val blurb: String? = null,
    val nameSuffix: String? = null,
    val statusInline: Boolean = false,
)

/**
 * A search result as a list row: a poster, avatar, logo or episode still, the name, a subtitle, and one wrapping row that holds the
 * [trailing] status first, then the rating and the [MediaResult.meta] text. The row and the gap above it are left out when all three are
 * absent, so a result with only a name stays as short as it is.
 *
 * @param trailing the result's status, drawn first in the wrapping row (an availability chip, a played or favourite mark).
 * @param byline a line drawn above the name, such as a person's avatar, name and time. Null, the default, draws none and takes no room.
 * @param end drawn at the row's end, after the text, in [ListRow]'s trailing slot. Null, the default, draws none.
 * @param footer drawn under the whole row, below the picture and the text, in [ListRow]'s footer slot (a download's progress). Null, the default,
 * draws none and takes no room.
 * @param onPosterClick when set, a poster or still is its own tap target with its own ripple, apart from [onClick], which the rest of the row
 * keeps. The picture is never smaller than the minimum touch target. An avatar or a logo is not tappable by itself, so this does not apply to them.
 * @param posterClickLabel what a screen reader announces for the poster's tap, such as "Open title"; the row's own tap is announced as the row.
 *
 * @need list a search result with its picture, kind and status
 * @group rows
 * @replaces a hand-built ListRow with a poster, a name, a subtitle and chips
 */
@Composable
fun MediaResultRow(
    result: MediaResult,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    trailing: (@Composable () -> Unit)? = null,
    byline: (@Composable () -> Unit)? = null,
    end: (@Composable () -> Unit)? = null,
    footer: (@Composable () -> Unit)? = null,
    onPosterClick: (() -> Unit)? = null,
    posterClickLabel: String? = null,
) {
    val topAligned = result.kind == MediaResultKind.Poster || result.kind == MediaResultKind.Still
    ListRow(
        modifier = modifier,
        onClick = onClick,
        verticalAlignment = if (topAligned) Alignment.Top else Alignment.CenterVertically,
        trailingContent = end,
        footer = footer,
        leading = {
            when (result.kind) {
                MediaResultKind.Poster ->
                    ListRowPoster(
                        imageUrl = result.imageUrl,
                        contentDescription = null,
                        onClick = onPosterClick,
                        onClickLabel = posterClickLabel,
                    )
                MediaResultKind.Avatar -> ListRowAvatar(imageUrl = result.imageUrl, name = result.name)
                MediaResultKind.Logo -> ListRowLogo(logoUrl = result.imageUrl)
                MediaResultKind.Still ->
                    ListRowPoster(
                        imageUrl = result.imageUrl,
                        contentDescription = null,
                        width = dimensionResource(R.dimen.media_result_still_width),
                        aspectRatio = STILL_ASPECT_RATIO,
                        onClick = onPosterClick,
                        onClickLabel = posterClickLabel,
                    )
            }
        },
    ) { contentModifier ->
        val gap = dimensionResource(R.dimen.detail_cast_avatar_label_spacing)
        val top = if (topAligned) dimensionResource(R.dimen.detail_meta_spacing) else dimensionResource(R.dimen.zero)
        val inline = result.statusInline
        val hasStatusLine = trailing != null || result.rating != null || result.meta != null || (inline && result.subtitle != null)
        val marks: (@Composable () -> Unit)? = if (result.favourite ||
            result.watched
        ) {
            ({ ResultMarks(result.favourite, result.watched) })
        } else {
            null
        }
        Column(modifier = contentModifier.padding(top = top)) {
            byline?.let {
                it()
                Spacer(Modifier.height(gap))
            }
            if (result.nameSuffix == null) {
                ListRowHeader(title = result.name, trailingContent = marks)
            } else {
                NameWithSuffix(name = result.name, suffix = result.nameSuffix, marks = marks)
            }
            if (!inline) {
                result.subtitle?.let {
                    Spacer(Modifier.height(gap))
                    Text(
                        text = it,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            if (hasStatusLine) {
                Spacer(Modifier.height(gap))
                WrappingRow(horizontalSpacing = gap, verticalSpacing = gap) {
                    trailing?.invoke()
                    if (inline) {
                        result.subtitle?.let {
                            Text(
                                text = it,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                    result.rating?.let { RatingChip(rating = it, tone = RatingChipTone.Surface) }
                    result.meta?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
            result.blurb?.let {
                Spacer(Modifier.height(gap))
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = BLURB_MAX_LINES,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

/**
 * The name with a [suffix] after it in the primary colour. The suffix and the marks take their own width first, and the name, up to two lines, takes what is
 * left and ellipsizes, so a short suffix is never the part that is cut. A long one is bounded: it takes at most half of the width it is offered and wraps to
 * a second line, then ellipsizes, so the name keeps roughly the other half (less the marks beside it), at any font size. Taking its whole width first left the name none at all.
 */
@Composable
private fun NameWithSuffix(
    name: String,
    suffix: String,
    marks: (@Composable () -> Unit)?,
) {
    Row(verticalAlignment = Alignment.Top) {
        Text(
            text = name,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f, fill = false),
        )
        Spacer(Modifier.width(dimensionResource(R.dimen.padding_xs)))
        Text(
            text = suffix,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
            maxLines = SUFFIX_MAX_LINES,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.atMostHalfOfWhatIsLeft(),
        )
        if (marks != null) {
            Spacer(Modifier.width(dimensionResource(R.dimen.padding_s)))
            marks()
        }
    }
}

/** The heart and the tick beside a result's name. Each is announced by its own words, since a row has no merged state description. */
@Composable
private fun ResultMarks(favourite: Boolean, watched: Boolean) {
    val size = dimensionResource(R.dimen.media_result_mark_size)
    Row(horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_xs))) {
        if (watched) {
            Icon(
                imageVector = Icons.Filled.Check,
                contentDescription = stringResource(R.string.media_card_watched),
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(size),
            )
        }
        if (favourite) {
            Icon(
                imageVector = Icons.Filled.Favorite,
                contentDescription = stringResource(R.string.media_card_favourite),
                tint = BingeTheme.colors.favourite,
                modifier = Modifier.size(size),
            )
        }
    }
}

/**
 * What a search draws in a [MediaResultRow]'s place while it loads: the row's own chrome with a plate for the picture and plates for the
 * name and the status line, at the picture's size, so the loading to content swap does not jump. [kind] picks the picture's shape, its box and the text's alignment as the row does.
 * [description] names the loading state for a screen reader; without one it stays silent. The text plates always follow the row's own text (the name and the
 * status line, each at its own font size), so the rows below do not shift down when the list loads. [blurbLines] is for a list whose rows
 * carry a blurb, which runs up to three lines: it adds that many blurb lines.
 *
 * What the name takes depends on its words, which a loading state does not have, so it is one line unless the caller hints. [expectedName] is a string
 * the loaded name is expected to look like (the longest typical title): the plate lays it out invisibly in the row's own style and width, up to the
 * row's two lines, so it takes what the name takes at every font size, wrapping included. The width is the row's whole text column: the favourite
 * and watched marks a loaded row puts beside its name are not reserved, so a title that only just fits on one line here can wrap in a marked row.
 * The hint applies to a [blurbLines] row too, as its name plate; on a row with no blurb it also reserves the subtitle line. The status chips are the app's own, so how many lines they wrap to, and how tall they are, is
 * not something a plate can measure: a row whose status wraps still moves.
 *
 * [byline] adds a line above the name for a row that has a byline, in the label style and the row's gap, and [statusInline] drops the subtitle line a hinted
 * row reserves, since an inline-status row has none. A byline is the app's own slot, so its height is a line of text, which is what the plate holds.
 *
 * @need show a loading placeholder for a search result row
 * @group feedback
 */
@Composable
fun MediaResultRowSkeleton(
    modifier: Modifier = Modifier,
    kind: MediaResultKind = MediaResultKind.Poster,
    description: String? = null,
    blurbLines: Int = 0,
    expectedName: String? = null,
    byline: Boolean = false,
    statusInline: Boolean = false,
) {
    val topAligned = kind == MediaResultKind.Poster || kind == MediaResultKind.Still
    ListRow(
        modifier = modifier.describedAs(description),
        // The same alignment as the row, so the plates sit where the text will.
        verticalAlignment = if (topAligned) Alignment.Top else Alignment.CenterVertically,
        leading = { SkeletonPicture(kind) },
    ) { contentModifier ->
        val top = if (topAligned) dimensionResource(R.dimen.detail_meta_spacing) else dimensionResource(R.dimen.zero)
        // The row's own text at its own font sizes, whether or not it carries a blurb: fixed-height plates stayed as they were at a large font
        // while the loaded row's text grew, and the rows below shifted down when the list loaded.
        BlurbRowPlates(blurbLines, contentModifier.padding(top = top), expectedName, byline, statusInline)
    }
}

/**
 * The text plates of a row, laid out as [MediaResultRow] lays out its text: the name, the status line and [lines] lines
 * of the blurb (none for a row without one), each a line of its own style and the gaps the row has, so the height is the loaded row's at
 * any font size.
 */
@Composable
private fun BlurbRowPlates(
    lines: Int,
    modifier: Modifier,
    expectedName: String? = null,
    byline: Boolean = false,
    statusInline: Boolean = false,
) {
    val gap = dimensionResource(R.dimen.detail_cast_avatar_label_spacing)
    Column(modifier = modifier) {
        if (byline) {
            TextPlate(MaterialTheme.typography.labelMedium, Modifier.fillMaxWidth(SUBTITLE_PLATE_FRACTION))
            Spacer(Modifier.height(gap))
        }
        TextPlate(
            MaterialTheme.typography.titleMedium,
            Modifier.fillMaxWidth(if (expectedName == null) NAME_PLATE_FRACTION else 1f),
            text = expectedName,
            maxLines = 2,
        )
        Spacer(Modifier.height(gap))
        // A hinted row with no blurb is a title that has a subtitle line too, as the hint's own row does, so the plates reserve it.
        if (expectedName != null && lines == 0 && !statusInline) {
            TextPlate(MaterialTheme.typography.labelMedium, Modifier.fillMaxWidth(SUBTITLE_PLATE_FRACTION))
            Spacer(Modifier.height(gap))
        }
        // The status line: a rating chip is a line of its label style with its own vertical padding.
        TextPlate(
            MaterialTheme.typography.labelMedium,
            Modifier.fillMaxWidth(SUBTITLE_PLATE_FRACTION).padding(vertical = dimensionResource(R.dimen.rating_chip_padding_v)),
        )
        // The loaded row draws this gap only before a blurb, so a row without one ends on its status line.
        if (lines > 0) Spacer(Modifier.height(gap))
        repeat(lines) { line ->
            TextPlate(MaterialTheme.typography.bodyMedium, Modifier.fillMaxWidth(if (line == lines - 1) SUBTITLE_PLATE_FRACTION else 1f))
        }
    }
}

/** The plate for a row's picture, in the shape and the box the row draws it in: a poster, a circle in a poster-height box, a tile or a still. */
@Composable
private fun SkeletonPicture(kind: MediaResultKind) {
    val posterWidth = dimensionResource(R.dimen.list_row_poster_width)
    when (kind) {
        MediaResultKind.Poster ->
            SkeletonPlate(Modifier.width(posterWidth).aspectRatio(CARD_ASPECT_RATIO), BingeShapes.ListRowPoster)
        MediaResultKind.Still ->
            SkeletonPlate(
                Modifier.width(dimensionResource(R.dimen.media_result_still_width)).aspectRatio(STILL_ASPECT_RATIO),
                BingeShapes.ListRowPoster,
            )
        MediaResultKind.Avatar ->
            Box(Modifier.width(posterWidth).aspectRatio(CARD_ASPECT_RATIO), contentAlignment = Alignment.Center) {
                SkeletonPlate(Modifier.size(posterWidth), BingeShapes.Pill)
            }
        MediaResultKind.Logo ->
            SkeletonPlate(Modifier.width(posterWidth).aspectRatio(CARD_ASPECT_RATIO), MaterialTheme.shapes.small)
    }
}

private const val BLURB_MAX_LINES = 3

/** How many lines a long name suffix wraps to before it ellipsizes. */
private const val SUFFIX_MAX_LINES = 2

/** The share of the width on offer a name suffix may take, so the name beside it keeps the rest. */
private const val SUFFIX_MAX_SHARE = 0.5f

/**
 * Limits this element to [SUFFIX_MAX_SHARE] of the width it is offered; a narrower one keeps its own width and so its one line. The intrinsic
 * measures are the element's own, uncapped: [ListRow] sizes its main line from them, and a row hands a child the width that child will have, not the
 * width on offer, so halving it there made a short suffix measure as two lines and left empty space under the row.
 */
private fun Modifier.atMostHalfOfWhatIsLeft(): Modifier = this then HalfWidthCap

private object HalfWidthCap : LayoutModifier {
    private fun cap(width: Int): Int = if (width == Constraints.Infinity) width else (width * SUFFIX_MAX_SHARE).toInt()

    override fun MeasureScope.measure(measurable: Measurable, constraints: Constraints): MeasureResult {
        // An unbounded width is an intrinsic measure, which has no width on offer to take a share of.
        val limited =
            if (constraints.hasBoundedWidth) {
                constraints.copy(maxWidth = cap(constraints.maxWidth).coerceAtLeast(constraints.minWidth))
            } else {
                constraints
            }
        val placeable = measurable.measure(limited)
        return layout(placeable.width, placeable.height) { placeable.place(0, 0) }
    }

    override fun IntrinsicMeasureScope.minIntrinsicHeight(measurable: IntrinsicMeasurable, width: Int): Int =
        measurable.minIntrinsicHeight(width)

    override fun IntrinsicMeasureScope.maxIntrinsicHeight(measurable: IntrinsicMeasurable, width: Int): Int =
        measurable.maxIntrinsicHeight(width)

    override fun IntrinsicMeasureScope.minIntrinsicWidth(measurable: IntrinsicMeasurable, height: Int): Int =
        measurable.minIntrinsicWidth(height)

    override fun IntrinsicMeasureScope.maxIntrinsicWidth(measurable: IntrinsicMeasurable, height: Int): Int =
        measurable.maxIntrinsicWidth(height)
}

private const val NAME_PLATE_FRACTION = 0.6f
private const val SUBTITLE_PLATE_FRACTION = 0.4f
private const val STILL_ASPECT_RATIO = 16f / 9f
