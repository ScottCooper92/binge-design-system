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
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import coil3.compose.SubcomposeAsyncImage
import com.binge.designsystem.R
import com.binge.designsystem.theme.BingeShapes
import com.binge.designsystem.theme.BingeTheme
import com.binge.designsystem.theme.LocalReduceMotion

/**
 * A landscape (16:9) tile with a backdrop image behind a text scrim and the category's [title] on it,
 * for a row of genres. It sits beside a [MediaCard] in a [MediaCarousel] and shares its corner shape
 * and press feedback.
 *
 * The image comes from [imageUrl]. While it loads or if it fails, [ImagePlaceholder] stands in; with no
 * URL the tile is a plain surface, still carrying the name. The title wraps to two lines, then
 * ellipsises, and the scrim keeps it legible over any backdrop in both themes. With no image there is no scrim, since the surface already gives the text its contrast.
 *
 * The tile is one click target and one accessibility item announcing [description], or [title] when
 * [description] is null. Pass [description] only when the spoken label should differ from the title.
 */
@Composable
fun BackdropTile(
    title: String,
    imageUrl: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    description: String? = null,
) {
    CategoryTileFrame(label = description ?: title, onClick = onClick, modifier = modifier) {
        if (imageUrl == null) {
            Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surfaceContainerHigh))
        } else {
            SubcomposeAsyncImage(
                model = imageUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
                loading = { ImagePlaceholder(Modifier.fillMaxSize()) },
                error = { ImagePlaceholder(Modifier.fillMaxSize()) },
            )
        }
        if (imageUrl != null) {
            val scrim = BingeTheme.colors.scrim
            Box(
                Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            0f to Color.Transparent,
                            SCRIM_START to scrim.copy(alpha = SCRIM_TOP_ALPHA),
                            1f to scrim.copy(alpha = SCRIM_BOTTOM_ALPHA),
                        ),
                    ),
            )
        }
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
            color = if (imageUrl == null) MaterialTheme.colorScheme.onSurface else BingeTheme.colors.onScrim,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(dimensionResource(R.dimen.padding_sm)),
        )
    }
}

/**
 * A landscape tile with a logo centred on a neutral surface, for a row of studios or networks. It
 * shares [BackdropTile]'s size, corner shape and press feedback, so the two sit alike in a row.
 *
 * The logo is fitted inside the tile with padding and never cropped. It is monochrome artwork, so it
 * reads in both themes from the surface tokens. While it loads, if it fails, or with no [logoUrl], the
 * tile shows [name] as text, so it is never blank.
 *
 * The tile is one click target and one accessibility item announcing [name].
 */
@Composable
fun LogoTile(
    name: String,
    logoUrl: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    CategoryTileFrame(label = name, onClick = onClick, modifier = modifier) {
        Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surfaceContainer))
        if (logoUrl == null) {
            LogoTileName(name)
        } else {
            SubcomposeAsyncImage(
                model = logoUrl,
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(dimensionResource(R.dimen.category_tile_logo_padding)),
                loading = { LogoTileName(name) },
                error = { LogoTileName(name) },
            )
        }
    }
}

@Composable
private fun LogoTileName(name: String) {
    Box(Modifier.fillMaxSize().padding(dimensionResource(R.dimen.padding_sm)), contentAlignment = Alignment.Center) {
        Text(
            text = name,
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

/**
 * The frame both tiles share: the landscape size, the [BingeShapes.MediaCard] corner, the press scale
 * (off under reduced motion, as [MediaCard]'s is) and a single click target that announces [label] as
 * one item. The children's own text and images are merged away so the tile reads once.
 */
@Composable
private fun CategoryTileFrame(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val reduceMotion = LocalReduceMotion.current
    val scale by animateFloatAsState(
        targetValue = if (isPressed && !reduceMotion) PRESSED_SCALE else 1f,
        animationSpec = if (reduceMotion) {
            snap()
        } else {
            spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium)
        },
        label = "category-tile-press-scale",
    )
    Box(
        modifier = modifier
            .width(dimensionResource(R.dimen.category_tile_width))
            .aspectRatio(TILE_ASPECT_RATIO)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }.clip(BingeShapes.MediaCard)
            .clickable(
                interactionSource = interactionSource,
                indication = LocalIndication.current,
                role = Role.Button,
                onClick = onClick,
            ).clearAndSetSemantics {
                contentDescription = label
                role = Role.Button
            },
        content = content,
    )
}

private const val TILE_ASPECT_RATIO = 16f / 9f
private const val PRESSED_SCALE = 0.95f
private const val SCRIM_START = 0.35f
private const val SCRIM_TOP_ALPHA = 0.1f
private const val SCRIM_BOTTOM_ALPHA = 0.75f
