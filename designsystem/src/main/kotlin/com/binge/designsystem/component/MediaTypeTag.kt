package com.binge.designsystem.component

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Tv
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.binge.designsystem.R

/**
 * Media type as a neutral [BingeTag] — Movie / TV with the matching glyph, for surfaces that mix
 * movie and TV rows.
 */
@Composable
fun MediaTypeTag(type: MediaTypeTagType, modifier: Modifier = Modifier) {
    BingeTag(
        label = stringResource(type.labelRes()),
        modifier = modifier,
        icon = when (type) {
            MediaTypeTagType.Movie -> Icons.Filled.Movie
            MediaTypeTagType.Tv -> Icons.Filled.Tv
        },
    )
}

/** The two media types that carry a type tag — episodes never need one (no type ambiguity). */
enum class MediaTypeTagType { Movie, Tv }

@StringRes
private fun MediaTypeTagType.labelRes(): Int =
    when (this) {
        MediaTypeTagType.Movie -> R.string.media_type_movie
        MediaTypeTagType.Tv -> R.string.media_type_tv
    }
