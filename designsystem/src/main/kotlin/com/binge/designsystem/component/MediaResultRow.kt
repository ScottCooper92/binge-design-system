package com.binge.designsystem.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.style.TextOverflow
import com.binge.designsystem.R

/** What a [MediaResult] shows beside its text. */
enum class MediaResultKind {
    /** A portrait poster, the text top-aligned beside it. */
    Poster,

    /** A round portrait of a person, the text centred beside it. */
    Avatar,

    /** A company or network logo on a tonal tile, the text centred beside it. */
    Logo,

    /** A 16:9 still, for an episode; the text is top-aligned beside it. */
    Still,
}

/**
 * One result of a search, as plain values. [key] is the app's identity for it as text, so a lazy list can save it.
 * [subtitle] is a line the app has already worded (a kind and a year, a role); [meta] is a second muted line, such as genres.
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
            ListRowHeader(title = result.name)
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

private const val STILL_ASPECT_RATIO = 16f / 9f
