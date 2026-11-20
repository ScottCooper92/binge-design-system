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
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
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
 * the search; the `trailing` slot of the row stays for anything else.
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
)

/**
 * A search result as a list row: a poster, avatar, logo or episode still, the name, a subtitle, and one wrapping row that holds the
 * [trailing] status first, then the rating and the [MediaResult.meta] text. The row and the gap above it are left out when all three are
 * absent, so a result with only a name stays as short as it is.
 *
 * @param trailing the result's status, drawn first in the wrapping row (an availability chip, a played or favourite mark).
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
) {
    val topAligned = result.kind == MediaResultKind.Poster || result.kind == MediaResultKind.Still
    ListRow(
        modifier = modifier,
        onClick = onClick,
        verticalAlignment = if (topAligned) Alignment.Top else Alignment.CenterVertically,
        leading = {
            when (result.kind) {
                MediaResultKind.Poster -> ListRowPoster(imageUrl = result.imageUrl, contentDescription = null)
                MediaResultKind.Avatar -> ListRowAvatar(imageUrl = result.imageUrl)
                MediaResultKind.Logo -> ListRowLogo(logoUrl = result.imageUrl)
                MediaResultKind.Still ->
                    ListRowPoster(
                        imageUrl = result.imageUrl,
                        contentDescription = null,
                        width = dimensionResource(R.dimen.media_result_still_width),
                        aspectRatio = STILL_ASPECT_RATIO,
                    )
            }
        },
    ) { contentModifier ->
        val gap = dimensionResource(R.dimen.detail_cast_avatar_label_spacing)
        val top = if (topAligned) dimensionResource(R.dimen.detail_meta_spacing) else dimensionResource(R.dimen.zero)
        val hasStatusLine = trailing != null || result.rating != null || result.meta != null
        Column(modifier = contentModifier.padding(top = top)) {
            ListRowHeader(
                title = result.name,
                trailingContent = if (result.favourite || result.watched) ({ ResultMarks(result.favourite, result.watched) }) else null,
            )
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
            if (hasStatusLine) {
                Spacer(Modifier.height(gap))
                WrappingRow(horizontalSpacing = gap, verticalSpacing = gap) {
                    trailing?.invoke()
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
 * What a search draws in a [MediaResultRow]'s place while it loads: the row's own chrome with a plate for the picture and two for the
 * name and the subtitle, at the picture's size, so the loading to content swap does not jump. [kind] picks the picture's shape, its box and the text's alignment as the row does.
 * [description] names the loading state for a screen reader; without one it stays silent.
 *
 * @need show a loading placeholder for a search result row
 * @group feedback
 */
@Composable
fun MediaResultRowSkeleton(
    modifier: Modifier = Modifier,
    kind: MediaResultKind = MediaResultKind.Poster,
    description: String? = null,
) {
    val topAligned = kind == MediaResultKind.Poster || kind == MediaResultKind.Still
    ListRow(
        modifier = modifier.describedAs(description),
        // The same alignment as the row, so the plates sit where the text will.
        verticalAlignment = if (topAligned) Alignment.Top else Alignment.CenterVertically,
        leading = { SkeletonPicture(kind) },
    ) { contentModifier ->
        Column(
            modifier = contentModifier.padding(
                top = if (topAligned) dimensionResource(R.dimen.detail_meta_spacing) else dimensionResource(R.dimen.zero),
            ),
            verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_s)),
        ) {
            SkeletonPlate(Modifier.fillMaxWidth(NAME_PLATE_FRACTION).height(dimensionResource(R.dimen.media_result_skeleton_name_height)))
            SkeletonPlate(
                Modifier.fillMaxWidth(SUBTITLE_PLATE_FRACTION).height(dimensionResource(R.dimen.media_result_skeleton_subtitle_height)),
            )
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

private const val NAME_PLATE_FRACTION = 0.6f
private const val SUBTITLE_PLATE_FRACTION = 0.4f
private const val STILL_ASPECT_RATIO = 16f / 9f
