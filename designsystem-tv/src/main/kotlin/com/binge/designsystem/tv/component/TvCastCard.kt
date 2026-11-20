package com.binge.designsystem.tv.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.tv.material3.Icon
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import coil3.compose.SubcomposeAsyncImage
import com.binge.designsystem.tv.focus.tvClickable
import com.binge.designsystem.tv.focus.tvFocusIndicator
import com.binge.designsystem.tv.R as TvR

/**
 * One person in a TV cast row: a circular profile image with the name and role beneath.
 *
 * The circle alone is the focus target: the ring is a border treatment ([tvFocusIndicator], `CircleShape`)
 * because a fill would bury a headshot. [name] is the focusable's content description, so the
 * announcement never depends on the image arriving.
 *
 * Focus is a parameter. [avatar] is a composition slot.
 * Aspect is fixed (1:1) but width is not — the row decides that, so a grid cell can size it differently.
 *
 * @need show a person as a circular picture with a name and role on a tv screen
 * @group tv
 * @replaces a hand-built tv cast cell
 */
@Composable
fun TvCastCard(
    name: String,
    role: String?,
    profileUrl: String?,
    isFocused: Boolean,
    onFocusChanged: (Boolean) -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    avatar: @Composable () -> Unit = { TvCastCardAvatar(profileUrl) },
) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                // Ring before clip — it draws outside the circle's bounds, so a clip earlier in the
                // chain would cut it. Same order as the poster card.
                .tvFocusIndicator(isFocused = isFocused, shape = CircleShape)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .tvClickable(onFocusChanged = onFocusChanged, onClick = onClick)
                .semantics { contentDescription = name },
        ) {
            avatar()
        }
        Text(
            text = name,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurface,
            minLines = NAME_LINES,
            maxLines = NAME_LINES,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = dimensionResource(TvR.dimen.tv_card_caption_gap)),
        )
        // The role slot is always reserved, even when empty: a `role?.let` left a card without one
        // a line shorter than its neighbours, leaving a ragged gap in the row.
        Text(
            text = role.orEmpty(),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            minLines = ROLE_LINES,
            maxLines = ROLE_LINES,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

/**
 * The headshot, or a person glyph where there is none.
 *
 * Loading keeps a bare plate; error routes to the shared [TvImagePlaceholder] glyph — a blank circle beside
 * filled ones reads as broken, where a blank plate reads as loading.
 */
@Composable
internal fun TvCastCardAvatar(profileUrl: String?) {
    SubcomposeAsyncImage(
        model = profileUrl,
        // Null: the card owns the description, so labelling the image too would announce twice.
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = Modifier.fillMaxSize(),
        loading = { TvCastCardPlate() },
        error = { TvImagePlaceholder(kind = TvImageKind.Person, title = null) },
    )
}

/**
 * The loading plate for [TvCastCardAvatar]. Sized by [tvPlaceholderGlyphSize] and tinted off [GLYPH_ALPHA]
 * rather than its own copies of either, so the glyph a card shows while loading matches the one it shows on
 * error — the parity [TvCastCardAvatar]'s doc calls for.
 *
 * Internal rather than private so the screenshot suite can render it directly, beside the error plate —
 * `SubcomposeAsyncImage`'s `loading` slot has no preview-reachable trigger of its own.
 */
@Composable
internal fun TvCastCardPlate() {
    BoxWithConstraints(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Icon(
            imageVector = Icons.Filled.Person,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = GLYPH_ALPHA),
            modifier = Modifier.size(tvPlaceholderGlyphSize(minOf(maxWidth, maxHeight))),
        )
    }
}

/** Fixed rather than capped: a cap lets a card's height depend on its text, which makes a row ragged. */
private const val NAME_LINES = 1
private const val ROLE_LINES = 1
