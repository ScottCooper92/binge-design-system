package com.binge.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import coil3.compose.SubcomposeAsyncImage
import com.binge.designsystem.CARD_ASPECT_RATIO
import com.binge.designsystem.R

/**
 * The shared full-width list **Row** (see the Card / Tile / Row taxonomy in CLAUDE.md): a tonal
 * surface holding an optional [leading] visual, a content column, and an optional [trailing]
 * element, stacked in a `LazyColumn`.
 *
 * `ListRow` owns the row chrome — rounded [containerColor] surface (`surfaceContainer` by default),
 * click, padding and gaps — so callers never re-roll it; pass a [containerColor] to tint a selected
 * row, or a [containerBrush] for a row that paints a gradient instead of a flat tone. The [content] slot receives a `Modifier` carrying `weight(1f)` so the caller builds its own
 * meta column. Supply [leading] via [ListRowPoster] (or an avatar) and [trailing] for an action or
 * status element. An optional [footer] spans the full inner width below the main line (e.g. a
 * progress bar that also runs under the poster).
 */
@Composable
fun ListRow(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    enabled: Boolean = true,
    verticalAlignment: Alignment.Vertical = Alignment.Top,
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainer,
    // Wins over [containerColor] when set. The one thing a flat tone cannot express, and the only
    // reason a gradient call-to-action row (DiscoverEntryRow) would otherwise re-roll this chrome.
    containerBrush: Brush? = null,
    leading: (@Composable () -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
    trailingGap: Dp = dimensionResource(R.dimen.list_row_gap),
    footer: (@Composable () -> Unit)? = null,
    content: @Composable RowScope.(Modifier) -> Unit,
) {
    val clickable = if (onClick != null) {
        Modifier.clickable(enabled = enabled, role = Role.Button, onClick = onClick)
    } else {
        Modifier
    }
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.large)
            .then(
                if (containerBrush != null) {
                    Modifier.background(containerBrush)
                } else {
                    Modifier.background(containerColor)
                },
            ).then(clickable)
            .padding(dimensionResource(R.dimen.list_row_padding)),
    ) {
        Row(verticalAlignment = verticalAlignment) {
            if (leading != null) {
                leading()
                Spacer(Modifier.width(dimensionResource(R.dimen.list_row_gap)))
            }
            content(Modifier.weight(1f))
            if (trailing != null) {
                Spacer(Modifier.width(trailingGap))
                trailing()
            }
        }
        if (footer != null) {
            Spacer(Modifier.height(dimensionResource(R.dimen.padding_s)))
            footer()
        }
    }
}

/**
 * A list row's header line: the [title] (the row's primary text) with an optional [trailing] element
 * beside it — a status badge or action. Top-aligned so the trailing stays put when the title wraps to
 * a second line, and the title takes the remaining width. This is the single home for a row title's
 * style, so every adopting row reads the same; drop it at the top of a [ListRow] content column.
 */
@Composable
fun ListRowHeader(
    title: String,
    modifier: Modifier = Modifier,
    trailing: (@Composable () -> Unit)? = null,
) {
    Row(modifier = modifier, verticalAlignment = Alignment.Top) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
        if (trailing != null) {
            Spacer(Modifier.width(dimensionResource(R.dimen.padding_s)))
            trailing()
        }
    }
}

/**
 * The standard [ListRow] leading poster: a fixed-width image clipped to the row's poster corner,
 * over a tonal placeholder. [aspectRatio] defaults to [CARD_ASPECT_RATIO] (a portrait poster); pass
 * a wider ratio for backdrop-style stills. Pass [onClick] to make the poster tappable on its own
 * (e.g. opening the title while the row body opens an actions sheet) — the ripple stays clipped to
 * the poster's rounded corners.
 */
@Composable
fun ListRowPoster(
    imageUrl: String?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    width: Dp = dimensionResource(R.dimen.list_row_poster_width),
    aspectRatio: Float = CARD_ASPECT_RATIO,
    onClick: (() -> Unit)? = null,
    dimmed: Boolean = false,
) {
    Box(
        modifier = modifier
            .width(width)
            .aspectRatio(aspectRatio)
            .clip(RoundedCornerShape(dimensionResource(R.dimen.list_row_poster_corner)))
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .then(
                if (onClick != null) Modifier.clickable(role = Role.Button, onClick = onClick) else Modifier,
            ),
        contentAlignment = Alignment.Center,
    ) {
        SubcomposeAsyncImage(
            model = imageUrl,
            contentDescription = contentDescription,
            contentScale = ContentScale.Crop,
            colorFilter = if (dimmed) {
                ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(POSTER_DIMMED_SATURATION) })
            } else {
                null
            },
            modifier = Modifier.fillMaxSize(),
            loading = { ImagePlaceholder(Modifier.fillMaxSize()) },
            error = { ImagePlaceholder(Modifier.fillMaxSize()) },
        )
    }
}

/** Desaturation for a dimmed poster — muted but still recognisable (0 = greyscale, 1 = full colour). */
private const val POSTER_DIMMED_SATURATION = 0.4f
