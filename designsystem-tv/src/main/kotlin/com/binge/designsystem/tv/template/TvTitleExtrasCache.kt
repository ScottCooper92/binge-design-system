package com.binge.designsystem.tv.template

import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateMapOf

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
