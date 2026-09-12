package com.binge.designsystem.component

/**
 * Compose-friendly projection of a watch provider for provider-grid tiles. Mapped from
 * `core.domain.model.WatchProvider` at the feature/data boundary; kept lean to keep UI
 * decoupled from domain.
 */
data class WatchProviderUi(
    val id: Int,
    val name: String,
    val logoUrl: String,
)
