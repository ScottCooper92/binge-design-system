package com.binge.designsystem.catalogregistry

/** One catalog entry placed in its component, with the name it is listed by there. */
data class ComponentEntry(
    val declaration: SampleDeclaration,
    val component: String,
    val componentName: String,
    val variantName: String,
)

/**
 * Gathers declarations into components, the unit the catalog app lists: one card per component, with
 * its samples and demos as the variants on that card's page.
 *
 * A file marked `@file:CatalogGroup("…")` puts every entry in it under that name, which is how
 * components a screen chooses between share a page. Otherwise a component is a `…Samples.kt` file's
 * group, with two merges so a component's variants are not split
 * across cards by how its files happen to be named:
 * - a demo joins the longest sample group its function name starts with (`BingeBottomSheetDemo` in
 *   `ModalDemos.kt` joins `BingeBottomSheet`), and keeps its file's group when none does;
 * - a samples file none of whose functions start with its own group, and all of which start with a
 *   shorter group, joins that group (`BingeNavFloatingBarToneSamples.kt` joins `BingeNavFloatingBar`).
 *   One function that does start with its own group keeps the file separate, which is what keeps
 *   `BingeFilterChipRow` from folding into `BingeFilterChip`.
 *
 * [declarations] are given in source order, files in name order; the result is sorted by component
 * name, then the component's default variant (the function named after it) first, then skeletons
 * after the real thing they stand in for, then samples before demos, each in source order.
 */
fun toComponents(declarations: List<SampleDeclaration>): List<ComponentEntry> {
    val sampleGroups = declarations.filter { it.kind == EntryKind.Sample }.map { it.group }.toSet()
    val mergedFiles = declarations
        .filter { it.kind == EntryKind.Sample }
        .groupBy { it.group }
        .mapNotNull { (group, members) ->
            if (members.any { it.function.startsWithWord(group) }) return@mapNotNull null
            val parent = sampleGroups
                .filter { it != group && members.all { member -> member.function.startsWithWord(it) } }
                .maxByOrNull { it.length }
            parent?.let { group to it }
        }.toMap()

    fun componentOf(declaration: SampleDeclaration): String =
        declaration.catalogGroup ?: when (declaration.kind) {
            EntryKind.Sample -> mergedFiles[declaration.group] ?: declaration.group
            EntryKind.Demo ->
                sampleGroups.filter { declaration.function.startsWithWord(it) }.maxByOrNull { it.length } ?: declaration.group
        }

    return declarations
        .withIndex()
        .map { (index, declaration) ->
            val component = componentOf(declaration)
            index to ComponentEntry(
                declaration = declaration,
                component = component,
                componentName = declaration.catalogGroup ?: componentName(component),
                variantName = variantName(declaration.function, component),
            )
        }.sortedWith(
            compareBy<Pair<Int, ComponentEntry>>(
                { it.second.componentName },
                { it.second.component },
                { it.second.variantName != DEFAULT_VARIANT },
                { it.second.variantName.isSkeleton() },
                { it.second.declaration.kind },
                { it.first },
            ),
        ).map { it.second }
}

/** What a variant is called on its component's page: `BingeTopBarTransparentSample` → `Transparent`. */
fun variantName(function: String, component: String): String {
    val stem = EntryKind.entries.fold(function) { name, kind -> name.removeSuffix(kind.name) }
    return when {
        stem == component -> DEFAULT_VARIANT
        stem.startsWithWord(component) -> SampleScanner.displayName(stem.removePrefix(component))
        else -> componentName(stem)
    }
}

/** What a component's card is called: `BingeNavFloatingBar` → `Nav floating bar`, `MediaCard` → `Media card`. */
fun componentName(component: String): String {
    val unprefixed = component.removePrefix(DESIGN_SYSTEM_PREFIX)
    return SampleScanner.displayName(if (unprefixed.firstOrNull()?.isUpperCase() == true) unprefixed else component)
}

/** A loading placeholder, which reads after the component it stands in for, whatever its file is called. */
private fun String.isSkeleton(): Boolean = contains("skeleton", ignoreCase = true)

/** The variant named after its component, which the component's card shows. */
const val DEFAULT_VARIANT = "Default"

private const val DESIGN_SYSTEM_PREFIX = "Binge"

/** `TextButtonIcon` starts with the word `TextButton`; `TagsRow` does not start with `Tag`. */
private fun String.startsWithWord(prefix: String): Boolean = this == prefix || startsWith(prefix) && !this[prefix.length].isLowerCase()
