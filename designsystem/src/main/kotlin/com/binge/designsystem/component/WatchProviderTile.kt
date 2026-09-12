package com.binge.designsystem.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import coil3.compose.SubcomposeAsyncImage
import com.binge.designsystem.R
import com.binge.designsystem.modifier.selectionLift
import com.binge.designsystem.theme.BingeShapes
import com.binge.designsystem.theme.LocalReduceMotion

private const val CHECK_POP_INITIAL_SCALE = 0.7f

/**
 * Square watch-provider tile with logo, selection border, and a pop-in check badge.
 * Shared between the onboarding services step and the account settings provider grid.
 *
 * [provider] supplies id/name/logo metadata. Toggle semantics use [Role.Checkbox].
 * Reduce-motion safe: the check badge fades rather than scales and the selection
 * lift snaps instead of springing under [LocalReduceMotion].
 */
@Composable
fun WatchProviderTile(
    provider: WatchProviderUi,
    selected: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val tileShape = BingeShapes.Large
    val reduceMotion = LocalReduceMotion.current
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .selectionLift(
                selected = selected,
                shape = tileShape,
                restingElevation = dimensionResource(R.dimen.provider_tile_elevation),
                liftedElevation = dimensionResource(R.dimen.provider_tile_elevation_selected),
            ).clip(tileShape)
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .border(
                width = dimensionResource(R.dimen.provider_tile_border),
                color = if (selected) MaterialTheme.colorScheme.primary else Color.Transparent,
                shape = tileShape,
            ).toggleable(
                value = selected,
                role = Role.Checkbox,
                onValueChange = { onToggle() },
            ),
    ) {
        SubcomposeAsyncImage(
            model = provider.logoUrl,
            contentDescription = provider.name,
            contentScale = ContentScale.Crop,
            loading = { WatchProviderNameFallback(provider.name) },
            error = { WatchProviderNameFallback(provider.name) },
            modifier = Modifier.fillMaxSize(),
        )
        AnimatedVisibility(
            visible = selected,
            enter = if (reduceMotion) {
                fadeIn()
            } else {
                scaleIn(spring(stiffness = Spring.StiffnessMedium), initialScale = CHECK_POP_INITIAL_SCALE) + fadeIn()
            },
            exit = if (reduceMotion) ExitTransition.None else fadeOut(),
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(dimensionResource(R.dimen.provider_tile_check_inset)),
        ) {
            Box(
                modifier = Modifier
                    .size(dimensionResource(R.dimen.provider_tile_check_badge_size))
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(dimensionResource(R.dimen.provider_tile_check_icon)),
                )
            }
        }
    }
}

/** Brand identity shown when the logo image is loading or fails to render. */
@Composable
private fun WatchProviderNameFallback(name: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(dimensionResource(R.dimen.padding_s)),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = name,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
