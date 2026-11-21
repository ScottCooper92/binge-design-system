package com.binge.designsystem.tv.template

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState

/**
 * The extras loaded so far for the titles the expanding hub has shown, kept while the hub lives so moving back to a title shows its
 * logo at once. [ensure] loads a title once; [get] is state, so a row or the hero reading it redraws when the load lands.
 */
@Stable
internal class TvTitleExtrasCache(
    private val load: suspend (TvHubItem) -> TvTitleExtras?,
) {
    private val loaded = mutableStateMapOf<String, TvTitleExtras>()

    operator fun get(key: String): TvTitleExtras? = loaded[key]

    suspend fun ensure(item: TvHubItem) {
        if (item.key !in loaded) load(item)?.let { loaded[item.key] = it }
    }
}

/**
 * A cache that lives as long as the hub and calls whichever [load] the hub was last given. The hub rebuilds its loader on
 * every recomposition, so a cache that kept the first one would never use a loader that changed. Extras already loaded stay.
 */
@Composable
internal fun rememberTvTitleExtrasCache(load: suspend (TvHubItem) -> TvTitleExtras?): TvTitleExtrasCache {
    val latest by rememberUpdatedState(load)
    return remember { TvTitleExtrasCache { latest(it) } }
}
