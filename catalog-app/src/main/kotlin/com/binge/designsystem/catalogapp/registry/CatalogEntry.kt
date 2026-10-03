package com.binge.designsystem.catalogapp.registry

import androidx.compose.runtime.Composable

/** A fixed fixture, or a stateful demo of behaviour a screenshot cannot show. */
enum class CatalogKind { Sample, Demo }

/**
 * One public sample or demo from the design system's `catalog/` package. Entries are written by the
 * registry generator, never by hand: see `:catalog-registry`.
 *
 * @property id the sample function's name, unique within the catalog package.
 * @property group the component the entry is a variant of: its samples file without the `Samples`
 *   suffix, after the generator's merges (see `toComponents` in `:catalog-registry`).
 * @property groupName the component as it is shown: `BingeNavFloatingBar` is `Nav floating bar`.
 * @property name the variant, as it is shown on its component's page: `Transparent`, or `Default`.
 * @property description the first sentence of the sample's KDoc, empty when it has none.
 * @property kind [CatalogKind.Demo] for a `…Demo()`; otherwise a sample.
 * @property onePerScreen the sample's file carries `@file:OnePerScreen`: a screen holds one of it.
 * @property fullScreen its file says `OnePerScreen(fullScreen = true)`: it replaces the app's chrome.
 * @property selfDescribing its file carries `@file:SelfDescribing`: its own copy says what it shows,
 *   so [description] is for search only and no card shows it.
 */
class CatalogEntry(
    val id: String,
    val group: String,
    val groupName: String = group,
    val name: String,
    val description: String,
    val kind: CatalogKind = CatalogKind.Sample,
    val onePerScreen: Boolean = false,
    val fullScreen: Boolean = false,
    val selfDescribing: Boolean = false,
    val content: @Composable () -> Unit,
)
