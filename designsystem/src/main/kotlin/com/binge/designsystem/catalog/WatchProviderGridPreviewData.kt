package com.binge.designsystem.catalog

import com.binge.designsystem.component.WatchProviderUi

/** Static seed data for [WatchProviderGridSample]; the ids here and below are arbitrary sample values. */
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

/** Two providers, too few to fill a row, for [WatchProviderGridPartialRowSample]. */
internal fun catalogPartialRowWatchProviders(): List<WatchProviderUi> =
    listOf(
        WatchProviderUi(id = 1, name = "Netflix", logoUrl = ""),
        WatchProviderUi(id = 2, name = "Disney+", logoUrl = ""),
    )

internal fun catalogPartialRowSelectedIds(): Set<Int> = setOf(1)
