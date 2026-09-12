package com.binge.designsystem.catalog

import com.binge.designsystem.component.WatchProviderUi

/** Static seed data for [WatchProviderGridSample]; the provider ids are TMDB provider ids. */
internal fun catalogSampleWatchProviders(): List<WatchProviderUi> =
    listOf(
        WatchProviderUi(id = 8, name = "Netflix", logoUrl = ""),
        WatchProviderUi(id = 337, name = "Disney+", logoUrl = ""),
        WatchProviderUi(id = 9, name = "Prime Video", logoUrl = ""),
        WatchProviderUi(id = 2, name = "Apple TV+", logoUrl = ""),
        WatchProviderUi(id = 384, name = "Max", logoUrl = ""),
        WatchProviderUi(id = 15, name = "Hulu", logoUrl = ""),
    )

internal fun catalogSelectedWatchProviderIds(): Set<Int> = setOf(8, 337)
