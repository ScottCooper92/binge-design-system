package com.binge.designsystem.component

import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import coil3.compose.SubcomposeAsyncImage
import com.binge.designsystem.CARD_ASPECT_RATIO
import com.binge.designsystem.R
import com.binge.designsystem.modifier.describedAs
import com.binge.designsystem.modifier.skeleton
import com.binge.designsystem.theme.BingeShapes
import androidx.compose.ui.semantics.role as semanticsRole

/**
 * A person in a grid: a portrait photo on a card, with the [name] under it on one line and the [role] under that on one line, both
 * centred and ellipsized. It fills the width of its cell, so the column count decides its size, and its photo keeps the poster's 2:3.
 *
 * The photo comes from [photoUrl]. With no URL, while it loads, or if it fails, a neutral plate with an image glyph stands in. The
 * caption always takes the room of two lines, whether or not there is a [role], so a row of people is the same height whatever each
 * one's text and [PersonGridCardSkeleton] holds exactly that space.
 *
 * The whole card is one click target and one accessibility item, announcing [description], or the [name] and [role] when [description]
 * is null. The photo carries no description of its own, so the name is not read twice.
 *
 * @need show a person as a portrait photo with a name and role in a grid
 * @group cards
 * @replaces a hand-built grid cell with a portrait image and two lines of text
 */
@Composable
fun PersonGridCard(
    name: String,
    role: String?,
    photoUrl: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    description: String? = null,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val label = description ?: listOf(name, role).filterNot { it.isNullOrBlank() }.joinToString(", ")
    Column(
        modifier = modifier
            .pressScale(interactionSource)
            .clip(BingeShapes.Medium)
            .clickable(
                interactionSource = interactionSource,
                indication = LocalIndication.current,
                role = Role.Button,
                onClick = onClick,
            ).clearAndSetSemantics {
                contentDescription = label
                semanticsRole = Role.Button
            },
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        PersonGridPhoto(photoUrl)
        PersonGridCaption(name = name, role = role)
    }
}

/**
 * The loading stand-in for a [PersonGridCard]: a portrait plate where the photo goes and a caption that takes the card's own room, so a
 * grid does not move when its people arrive. [description] names the loading state for a screen reader; without one it stays silent.
 *
 * @need show a loading placeholder for a person grid cell
 * @group feedback
 */
@Composable
fun PersonGridCardSkeleton(modifier: Modifier = Modifier, description: String? = null) {
    Column(modifier = modifier.describedAs(description), horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(CARD_ASPECT_RATIO)
                .skeleton(visible = true, shape = BingeShapes.Medium),
        )
        PersonGridCaption(name = null, role = null)
    }
}

@Composable
private fun PersonGridPhoto(photoUrl: String?) {
    Card(
        shape = BingeShapes.Medium,
        elevation = CardDefaults.cardElevation(defaultElevation = dimensionResource(R.dimen.card_elevation)),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Box(modifier = Modifier.fillMaxWidth().aspectRatio(CARD_ASPECT_RATIO)) {
            if (photoUrl.isNullOrBlank()) {
                ImagePlaceholder(Modifier.fillMaxSize())
            } else {
                SubcomposeAsyncImage(
                    model = photoUrl,
                    // The card names the person, so describing the photo too would announce the name twice.
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                    loading = { ImagePlaceholder(Modifier.fillMaxSize()) },
                    error = { ImagePlaceholder(Modifier.fillMaxSize()) },
                )
            }
        }
    }
}

/**
 * The caption under the photo: the [name] over the [role], each on one line, in a space two lines tall whether or not the role is there. A null
 * [name] and [role] leave the space empty, which is [PersonGridCardSkeleton]'s caption, so the placeholder cannot drift from the card.
 */
@Composable
private fun PersonGridCaption(name: String?, role: String?) {
    val style = MaterialTheme.typography.bodySmall
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = dimensionResource(R.dimen.detail_cast_avatar_label_spacing)),
    ) {
        // Two lines of the caption's own style, drawn in nothing: the height comes from the same text layout the real lines use, so it
        // cannot come out shorter than they do at any font scale.
        Text(
            text = " ",
            style = style,
            color = Color.Transparent,
            minLines = CAPTION_LINES,
            maxLines = CAPTION_LINES,
            modifier = Modifier.fillMaxWidth(),
        )
        Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            if (name != null) {
                Text(
                    text = name,
                    style = style,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            if (!role.isNullOrBlank()) {
                Text(
                    text = role,
                    style = style,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

private const val CAPTION_LINES = 2
