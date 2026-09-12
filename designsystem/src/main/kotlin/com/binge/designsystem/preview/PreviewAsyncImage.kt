package com.binge.designsystem.preview

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import coil3.ColorImage
import coil3.annotation.ExperimentalCoilApi
import coil3.compose.AsyncImagePreviewHandler
import coil3.compose.LocalAsyncImagePreviewHandler

/**
 * Wraps [content] so every Coil `AsyncImage` inside it resolves to a deterministic solid image of
 * [widthPx] × [heightPx], instead of hitting a network a screenshot does not have.
 *
 * The size is the point, not the colour. `ContentScale` is computed from the painter's *intrinsic* size, so
 * with nothing loaded a `Fit` and a `Crop` render identically — both nothing — and a frame that stands its
 * own shape in for the image asserts the stand-in rather than the production scaling. Give the size the
 * aspect ratio the real artwork has and the composable letterboxes, crops or fills for real.
 *
 * A null or blank model still takes the placeholder branch, as in production.
 *
 * The preview-handler API is `@ExperimentalCoilApi` (Coil 3.5.0) with no stable equivalent; the opt-in is
 * accepted for preview-only tooling.
 */
@OptIn(ExperimentalCoilApi::class)
@Composable
fun WithPreviewImage(
    widthPx: Int,
    heightPx: Int,
    color: Int = PREVIEW_IMAGE_COLOR,
    content: @Composable () -> Unit,
) {
    val handler = AsyncImagePreviewHandler { ColorImage(color = color, width = widthPx, height = heightPx) }
    CompositionLocalProvider(LocalAsyncImagePreviewHandler provides handler) {
        content()
    }
}

/** Opaque slate — a fixed stand-in for artwork, distinct from both scrim and the surfaceVariant plates. */
const val PREVIEW_IMAGE_COLOR: Int = 0xFF6E7A8A.toInt()
