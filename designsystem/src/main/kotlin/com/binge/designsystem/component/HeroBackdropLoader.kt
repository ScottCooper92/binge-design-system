package com.binge.designsystem.component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.unit.IntSize
import coil3.SingletonImageLoader
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import coil3.size.Size

/**
 * Loads a hero backdrop ahead of its slide, so the carousel can keep the previous backdrop on screen until the next one is
 * there. [load] returns once the image is in the cache, when the load fails, or at once when there is nothing to load: a
 * missing image must not hold a slide back, and a failed one shows its own placeholder as it always did.
 */
internal fun interface HeroBackdropLoader {
    suspend fun load(url: String?, size: IntSize)
}

/** Overridden by a test that needs to hold a load open; production loads through Coil. */
internal val LocalHeroBackdropLoader = staticCompositionLocalOf<HeroBackdropLoader?> { null }

@Composable
internal fun rememberHeroBackdropLoader(): HeroBackdropLoader {
    LocalHeroBackdropLoader.current?.let { return it }
    val context = LocalPlatformContext.current
    // A preview has no network to wait on, and a frame must not hold its slide back.
    if (LocalInspectionMode.current) return remember { HeroBackdropLoader { _, _ -> } }
    return remember(context) {
        HeroBackdropLoader { url, size ->
            if (url.isNullOrBlank()) return@HeroBackdropLoader
            val request = ImageRequest
                .Builder(context)
                .data(url)
                // The size the backdrop draws at, so the decoded image lands in the memory cache under the key the slide reads.
                .size(Size(size.width, size.height))
                .build()
            SingletonImageLoader.get(context).execute(request)
        }
    }
}

/** The images the hero carousel has already waited for, so it knows which slides arrive loaded and which still need a plate. */
internal class HeroBackdropReadiness {
    private val loaded = mutableSetOf<String>()

    fun markLoaded(url: String?) {
        if (!url.isNullOrBlank()) loaded += url
    }

    /** A slide draws the plate while loading unless its image was waited for: it is held behind the previous backdrop instead. */
    fun needsPlaceholder(url: String?): Boolean = url.isNullOrBlank() || url !in loaded
}

/** Loads [url] and records that it was waited for, whether the load succeeded or failed: a failure shows its own plate. */
internal suspend fun HeroBackdropLoader.loadAndMark(
    readiness: HeroBackdropReadiness,
    url: String?,
    size: IntSize,
) {
    load(url, size)
    readiness.markLoaded(url)
}
