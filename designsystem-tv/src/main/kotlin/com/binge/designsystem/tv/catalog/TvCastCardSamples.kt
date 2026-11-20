@file:SelfDescribing

package com.binge.designsystem.tv.catalog

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.dimensionResource
import androidx.tv.material3.MaterialTheme
import com.binge.designsystem.catalog.SelfDescribing
import com.binge.designsystem.preview.WithPreviewImage
import com.binge.designsystem.tv.R
import com.binge.designsystem.tv.component.TvCastCard
import com.binge.designsystem.tv.component.TvCastCardPlate
import com.binge.designsystem.tv.component.TvImageKind
import com.binge.designsystem.tv.component.TvImagePlaceholder

/** A non-blank model, so the preview handler resolves it; a null one would take the flat-plate branch. */
private const val PREVIEW_PROFILE_URL = "https://preview.invalid/profile.jpg"
private const val PROFILE_SIZE_PX = 400
private const val POSTER_RATIO = 2f / 3f
private const val BACKDROP_RATIO = 16f / 9f

@Composable
private fun SampleRow(content: @Composable () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(dimensionResource(R.dimen.tv_overscan_vertical)),
        horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.tv_media_row_card_gap)),
    ) {
        content()
    }
}

@Composable
private fun CastCell(
    name: String,
    role: String?,
    url: String?,
    isFocused: Boolean,
) {
    TvCastCard(
        name = name,
        role = role,
        profileUrl = url,
        isFocused = isFocused,
        onFocusChanged = {},
        onClick = {},
        modifier = Modifier.width(dimensionResource(R.dimen.tv_cast_card_width)),
    )
}

/** Three cast cells at the row's own width: focused (ring on the circle), resting, and the no-headshot plate with the person glyph. */
@Composable
fun TvCastCardSample() {
    SampleRow {
        WithPreviewImage(widthPx = PROFILE_SIZE_PX, heightPx = PROFILE_SIZE_PX) {
            CastCell("Wagner Moura", "Marcus", PREVIEW_PROFILE_URL, isFocused = true)
        }
        WithPreviewImage(widthPx = PROFILE_SIZE_PX, heightPx = PROFILE_SIZE_PX) {
            CastCell("Florence Pugh", "Yelena", PREVIEW_PROFILE_URL, isFocused = false)
        }
        CastCell("Denis Villeneuve", "Director", null, isFocused = false)
    }
}

/** The loading plate beside the error plate, the same circle: the two "no headshot yet" states stay sized and tinted alike. */
@Composable
fun TvCastCardPlatesSample() {
    SampleRow {
        TvCastCard(
            name = "Loading",
            role = null,
            profileUrl = null,
            isFocused = false,
            onFocusChanged = {},
            onClick = {},
            avatar = { TvCastCardPlate() },
            modifier = Modifier.width(dimensionResource(R.dimen.tv_cast_card_width)),
        )
        CastCell("Error", null, null, isFocused = false)
    }
}

@Composable
private fun PosterSlot(content: @Composable () -> Unit) {
    Box(Modifier.width(dimensionResource(R.dimen.tv_immersive_card_width)).aspectRatio(POSTER_RATIO)) { content() }
}

@Composable
private fun ThumbnailSlot(content: @Composable () -> Unit) {
    Box(Modifier.width(dimensionResource(R.dimen.tv_thumbnail_card_width)).aspectRatio(BACKDROP_RATIO)) { content() }
}

/** Every kind that carries a title, at poster geometry: a poster-only card with no artwork says what it stands in for. */
@Composable
fun TvImagePlaceholderPostersSample() {
    SampleRow {
        PosterSlot { TvImagePlaceholder(kind = TvImageKind.Movie, title = "Dune: Part Two") }
        PosterSlot { TvImagePlaceholder(kind = TvImageKind.TvShow, title = "Severance") }
        PosterSlot { TvImagePlaceholder(kind = TvImageKind.Person, title = "Florence Pugh") }
    }
}

/** A title long enough to need both lines beside one short enough not to. */
@Composable
fun TvImagePlaceholderTitleLengthsSample() {
    SampleRow {
        PosterSlot { TvImagePlaceholder(kind = TvImageKind.Movie, title = "The Lord of the Rings: The Fellowship of the Ring") }
        PosterSlot { TvImagePlaceholder(kind = TvImageKind.Movie, title = "Heat") }
    }
}

/** The landscape slots, where the glyph is sized off the slot's smaller side so the title still fits. */
@Composable
fun TvImagePlaceholderLandscapeSample() {
    SampleRow {
        ThumbnailSlot { TvImagePlaceholder(kind = TvImageKind.Episode, title = "Chapter One: Cold Open") }
        ThumbnailSlot { TvImagePlaceholder(kind = TvImageKind.Photo, title = null) }
    }
}

/** The cast circle: no title, because the name already sits under the card. */
@Composable
fun TvImagePlaceholderPersonCircleSample() {
    SampleRow {
        Box(Modifier.width(dimensionResource(R.dimen.tv_cast_card_width)).aspectRatio(1f).clip(CircleShape)) {
            TvImagePlaceholder(kind = TvImageKind.Person, title = null)
        }
    }
}
