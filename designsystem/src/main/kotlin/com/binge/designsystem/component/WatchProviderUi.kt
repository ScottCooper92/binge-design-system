package com.binge.designsystem.component

/**
 * Compose-friendly projection of a watch provider for provider-grid tiles. The caller maps its own
 * provider model to this; it is kept lean so the tiles know nothing of where providers come from.
 */
data class WatchProviderUi(
    val id: Int,
    val name: String,
    val logoUrl: String,
)
