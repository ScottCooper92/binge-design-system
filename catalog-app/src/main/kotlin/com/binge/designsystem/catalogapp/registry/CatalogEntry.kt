package com.binge.designsystem.catalogapp.registry

import androidx.compose.runtime.Composable

/**
 * One public sample from the design system's `catalog/` package. Entries are written by the
 * registry generator, never by hand: see `:catalog-registry`.
 *
 * @property id the sample function's name, unique within the catalog package.
 * @property group the source file the sample lives in, without its `Samples` suffix.
 * @property description the first sentence of the sample's KDoc, empty when it has none.
 */
class CatalogEntry(
    val id: String,
    val group: String,
    val name: String,
    val description: String,
    val content: @Composable () -> Unit,
)
