package com.binge.designsystem.component

import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import coil3.compose.SubcomposeAsyncImage
import com.binge.designsystem.R
import com.binge.designsystem.modifier.describedAs
import com.binge.designsystem.modifier.skeleton
import com.binge.designsystem.theme.BingeShapes
import com.binge.designsystem.toInitials

/**
 * A carousel tile for a person, such as a cast or crew member: a round photo, the [name], and an optional one-line [subtitle] (a character,
 * a job). It sits beside a [MediaCard] in a [MediaCarousel] and gives the same press feedback.
 *
 * The photo comes from [photoUrl]. With no URL, or while it loads or if it fails, the person's initials stand in on a neutral surface.
 * The tile is a fixed width, and its caption always takes the same room, three lines: the name wraps to two lines and then ellipsizes, and
 * the subtitle under it is one line that ellipsizes. A row of people is the same height whatever each one's text, and [PersonCardSkeleton] holds
 * exactly that space.
 *
 * The tile is one click target and one accessibility item announcing [description], or the [name] and [subtitle] when [description] is
 * null. The caller owns all the words.
 *
 * @need show a person with a round photo in a carousel
 * @group cards
 * @replaces a hand-built Column of a round image and text
 */
@Composable
fun PersonCard(
    name: String,
    subtitle: String?,
    photoUrl: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    description: String? = null,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val label = description ?: listOf(name, subtitle).filterNot { it.isNullOrBlank() }.joinToString(", ")
    Column(
        modifier = modifier
            .width(dimensionResource(R.dimen.person_card_width))
            .pressScale(interactionSource)
            .clip(BingeShapes.Medium)
            .clickable(
                interactionSource = interactionSource,
                indication = LocalIndication.current,
                role = Role.Button,
                onClick = onClick,
            ).clearAndSetSemantics {
                contentDescription = label
                role = Role.Button
            },
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        PersonPhoto(name = name, photoUrl = photoUrl)
        PersonCardCaption(name = name, subtitle = subtitle)
    }
}

/**
 * The loading stand-in for a [PersonCard]: a round plate where the photo goes and a caption that takes the card's own room, so a row does not
 * move when its people arrive. [description] names the loading state for a screen reader; without one it stays silent.
 *
 * @need show a loading placeholder for a person card row
 * @group feedback
 */
@Composable
fun PersonCardSkeleton(modifier: Modifier = Modifier, description: String? = null) {
    Column(
        modifier = modifier.width(dimensionResource(R.dimen.person_card_width)).describedAs(description),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(dimensionResource(R.dimen.person_card_photo_size))
                .skeleton(visible = true, shape = BingeShapes.Pill),
        )
        PersonCardCaption(name = null, subtitle = null)
    }
}

@Composable
private fun PersonPhoto(name: String, photoUrl: String?) {
    val size = dimensionResource(R.dimen.person_card_photo_size)
    Box(
        modifier = Modifier
            .size(size)
            .clip(BingeShapes.Pill),
    ) {
        if (photoUrl.isNullOrBlank()) {
            PersonInitials(name)
        } else {
            SubcomposeAsyncImage(
                model = photoUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
                loading = { PersonInitials(name) },
                error = { PersonInitials(name) },
            )
        }
    }
}

/** The person's initials on a neutral surface, sized to the circle and not to the user's font size. */
@Composable
private fun PersonInitials(name: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surfaceContainerHigh),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = name.toInitials(locale = LocalConfiguration.current.locales[0]),
            // The line height is dropped because a fixed one scales with the system font and outgrows the circle.
            style = MaterialTheme.typography.titleLarge.copy(lineHeight = TextUnit.Unspecified),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            maxLines = 1,
            // The bounds are in dp, not sp: the circle is a fixed size, so its initials must not grow with the system font.
            autoSize =
                with(LocalDensity.current) {
                    TextAutoSize.StepBased(
                        minFontSize = dimensionResource(R.dimen.avatar_initials_min_size).toSp(),
                        maxFontSize = dimensionResource(R.dimen.avatar_initials_max_size).toSp(),
                    )
                },
        )
    }
}

/**
 * The caption under the photo: the [name] over up to two lines and the [subtitle] under it on one, in a space three lines tall whether or
 * not the text fills it, so a row of people is the same height whatever each one's text and a short name keeps its subtitle close under it.
 * A null [name] and [subtitle] leave the space empty, which is [PersonCardSkeleton]'s caption. Shared with the skeleton rather than repeated
 * there, so the placeholder cannot drift from the room the card takes.
 */
@Composable
private fun PersonCardCaption(name: String?, subtitle: String?) {
    val style = MaterialTheme.typography.bodySmall
    val nameStyle = style.copy(fontWeight = FontWeight.Medium)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = dimensionResource(R.dimen.padding_s))
            .padding(horizontal = dimensionResource(R.dimen.padding_xs)),
    ) {
        // Three lines of the caption's own style, drawn in nothing: the height comes from the same text layout the real lines use, so it
        // cannot come out shorter than they do at any font scale, as a height worked out from the line height can.
        Text(
            text = " ",
            style = nameStyle,
            color = Color.Transparent,
            minLines = CAPTION_LINES,
            maxLines = CAPTION_LINES,
            modifier = Modifier.fillMaxWidth(),
        )
        Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            if (name != null) {
                Text(
                    text = name,
                    style = nameStyle,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                    maxLines = NAME_LINES,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            if (!subtitle.isNullOrBlank()) {
                Text(
                    text = subtitle,
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

private const val NAME_LINES = 2
private const val CAPTION_LINES = NAME_LINES + 1
