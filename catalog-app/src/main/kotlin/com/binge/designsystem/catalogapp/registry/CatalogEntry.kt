package com.binge.designsystem.catalogapp.registry

import androidx.compose.runtime.Composable

/** A fixed fixture, or a stateful demo of behaviour a screenshot cannot show. */
enum class CatalogKind { Sample, Demo }

/**
 * One public sample or demo from the design system's `catalog/` package. Entries are written by the
 * registry generator, never by hand: see `:catalog-registry`.
 *
 * @property id the sample function's name, unique within the catalog package.
 * @property group the source file the sample lives in, without its `Samples` suffix.
 * @property description the first sentence of the sample's KDoc, empty when it has none.
 * @property kind [CatalogKind.Demo] for a `…Demo()`, listed in its own section; otherwise a sample.
 */
class CatalogEntry(
    val id: String,
    val group: String,
    val name: String,
    val description: String,
    val kind: CatalogKind = CatalogKind.Sample,
    val content: @Composable () -> Unit,
)
