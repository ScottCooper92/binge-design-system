package com.binge.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.binge.designsystem.CARD_ASPECT_RATIO
import com.binge.designsystem.R
import com.binge.designsystem.theme.BingeExpressiveTheme

/**
 * The trailing **Tile** that ends a carousel, grid, or image strip and links to a "see all" /
 * "Discover more" screen: a chevron in a circle inside a dashed outline, with [label] underneath.
 * The caller sizes it via [modifier] to match the surrounding cells.
 */
@Composable
fun SeeAllTile(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    label: String = stringResource(R.string.gallery_see_all),
) {
    val corner = dimensionResource(R.dimen.see_all_tile_corner)
    val density = LocalDensity.current
    val dashOnPx = with(density) { dimensionResource(R.dimen.see_all_tile_dash_on).toPx() }
    val dashOffPx = with(density) { dimensionResource(R.dimen.see_all_tile_dash_off).toPx() }
    val strokePx = with(density) { dimensionResource(R.dimen.hairline_thickness).toPx() }
    val cornerPx = with(density) { corner.toPx() }
    val outlineColor = MaterialTheme.colorScheme.outline
    // Single source for both the clip and the dashed outline below, so they can't drift apart.
    val shape = RoundedCornerShape(corner)

    Box(
        modifier = modifier
            .clip(shape)
            .drawBehind {
                drawRoundRect(
                    color = outlineColor,
                    size = Size(size.width, size.height),
                    cornerRadius = CornerRadius(cornerPx, cornerPx),
                    style = Stroke(
                        width = strokePx,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(dashOnPx, dashOffPx)),
                    ),
                )
            }.clickable(onClick = onClick, role = Role.Button)
            .padding(
                horizontal = dimensionResource(R.dimen.padding_s),
                vertical = dimensionResource(R.dimen.padding_sm),
            ),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_s)),
        ) {
            Box(
                modifier = Modifier
                    .size(dimensionResource(R.dimen.see_all_tile_icon_circle))
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceContainerHigh),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(dimensionResource(R.dimen.see_all_tile_icon)),
                )
            }
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewSeeAllTile() {
    BingeExpressiveTheme {
        SeeAllTile(
            onClick = {},
            modifier = Modifier.width(120.dp).aspectRatio(CARD_ASPECT_RATIO),
        )
    }
}
