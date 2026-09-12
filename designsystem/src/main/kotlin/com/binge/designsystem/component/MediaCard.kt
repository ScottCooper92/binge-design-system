package com.binge.designsystem.component

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import coil3.compose.SubcomposeAsyncImage
import com.binge.designsystem.CARD_ASPECT_RATIO
import com.binge.designsystem.R
import com.binge.designsystem.component.ImagePlaceholder
import com.binge.designsystem.component.MediaCardAction
import com.binge.designsystem.component.MediaTypeTag
import com.binge.designsystem.component.MediaTypeTagType
import com.binge.designsystem.modifier.skeleton
import com.binge.designsystem.theme.BingeShapes
import com.binge.designsystem.theme.BingeTheme
import com.binge.designsystem.theme.LocalReduceMotion

@Composable
fun MediaCard(
    posterUrl: String?,
    title: String,
    rating: Float?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    topEndAction: MediaCardAction? = null,
    typeBadge: MediaTypeTagType? = null,
    userRating: Float? = null,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val reduceMotion = LocalReduceMotion.current

    val scale by animateFloatAsState(
        targetValue = if (isPressed && !reduceMotion) 0.95f else 1f,
        animationSpec = if (reduceMotion) {
            snap()
        } else {
            spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMedium,
            )
        },
        label = "card-press-scale",
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }.clickable(
                // `detectTapGestures` is a gesture detector and `semantics { onClick }` an accessibility action;
                // neither makes a focus target, so nothing navigating by focus could reach a poster (#2199). `clickable`
                // supplies focusability, and the interaction source hands the scale above the press signal it once read from the detector.
                interactionSource = interactionSource,
                indication = LocalIndication.current,
                role = Role.Button,
                onClick = onClick,
            ),
    ) {
        Card(
            shape = BingeShapes.MediaCard,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
            elevation = CardDefaults.cardElevation(defaultElevation = dimensionResource(R.dimen.card_elevation)),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(CARD_ASPECT_RATIO),
            ) {
                SubcomposeAsyncImage(
                    model = posterUrl,
                    contentDescription = title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                    loading = { ImagePlaceholder(Modifier.fillMaxSize()) },
                    error = { ImagePlaceholder(Modifier.fillMaxSize()) },
                )
                MediaCardOverlays(
                    rating = rating,
                    userRating = userRating,
                    typeBadge = typeBadge,
                    topEndAction = topEndAction,
                )
            }
        }
        MediaCardTitle(title)
    }
}

/**
 * The card's caption, always two lines so a one-line title and a two-line one leave the row the same
 * height. Shared with [MediaCardSkeleton] rather than repeated there: the placeholder's whole job is to
 * reserve this, and a style or padding change that moved one and not the other is the drift it exists to
 * prevent.
 */
@Composable
private fun MediaCardTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
        color = MaterialTheme.colorScheme.onSurface,
        minLines = 2,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis,
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = dimensionResource(R.dimen.card_title_padding_h),
                vertical = dimensionResource(R.dimen.card_title_padding_v),
            ),
    )
}

/**
 * What a poster grid draws in a [MediaCard]'s place while it loads: the poster plate, and the blank title
 * band the card fills beneath it.
 *
 * The band is the point. A cell that reserved the poster alone measured 46dp short of the card replacing
 * it, and a grid's first cell keeps its corner whatever its height — so the check that compares positions
 * passed while every row below the first dropped on resolve (#2096, the shape #2079 found next door).
 */
@Composable
fun MediaCardSkeleton(modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(CARD_ASPECT_RATIO)
                .skeleton(visible = true),
        )
        MediaCardTitle(title = " ")
    }
}

/**
 * The stacked poster overlays: the TMDB [rating] bottom-start, the user's own [userRating] as an
 * accent chip bottom-end, the [typeBadge] top-start (for mixed movie/TV grids), and the contextual
 * [topEndAction] top-end. Each is optional so a plain [MediaCard] renders an image alone.
 */
@Composable
private fun MediaCardOverlays(
    rating: Float?,
    userRating: Float?,
    typeBadge: MediaTypeTagType?,
    topEndAction: MediaCardAction?,
) {
    val badgeInset = dimensionResource(R.dimen.card_badge_inset)
    Box(modifier = Modifier.fillMaxSize()) {
        if (rating != null) {
            RatingChip(
                rating = rating,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(dimensionResource(R.dimen.card_rating_padding)),
            )
        }
        if (userRating != null) {
            RatingChip(
                rating = userRating,
                tone = RatingChipTone.Accent,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(dimensionResource(R.dimen.card_rating_padding)),
            )
        }
        if (typeBadge != null) {
            MediaTypeTag(
                type = typeBadge,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(badgeInset),
            )
        }
        if (topEndAction != null) {
            MediaCardActionButton(
                action = topEndAction,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(dimensionResource(R.dimen.card_action_inset)),
            )
        }
    }
}

@Composable
private fun MediaCardActionButton(action: MediaCardAction, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(dimensionResource(R.dimen.card_action_button_size))
            .clip(CircleShape)
            .background(BingeTheme.colors.scrim.copy(alpha = ACTION_SCRIM_ALPHA)),
        contentAlignment = Alignment.Center,
    ) {
        IconButton(onClick = action.onClick, modifier = Modifier.fillMaxSize()) {
            Icon(
                imageVector = action.icon,
                contentDescription = action.contentDescription,
                tint = BingeTheme.colors.onScrim,
                modifier = Modifier.size(dimensionResource(R.dimen.card_action_icon_size)),
            )
        }
    }
}

private const val ACTION_SCRIM_ALPHA = 0.65f
