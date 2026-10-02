package com.binge.designsystem.catalogapp.registry

/** Entries whose variant name, component, function or description contain every word of [query], ignoring case. */
fun List<CatalogEntry>.matching(query: String): List<CatalogEntry> {
    val words = query
        .trim()
        .lowercase()
        .split(Regex("""\s+"""))
        .filter { it.isNotEmpty() }
    if (words.isEmpty()) return this
    return filter { entry ->
        val haystack = "${entry.name} ${entry.groupName} ${entry.group} ${entry.id} ${entry.description}".lowercase()
        words.all { it in haystack }
    }
}

/**
 * One component and its variants, in registry order, which puts the default variant first. The card
 * shows [preview]; the component's page offers every variant.
 */
class CatalogComponent(
    val group: String,
    val name: String,
    val variants: List<CatalogEntry>,
) {
    /** The first sample, since a still render of a demo says nothing; a demo only when there is no sample. */
    val preview: CatalogEntry get() = variants.firstOrNull { it.kind == CatalogKind.Sample } ?: variants.first()

    val hasDemo: Boolean get() = variants.any { it.kind == CatalogKind.Demo }

    /**
     * Shown one variant at a time rather than as a list, because its file says a screen holds one. A
     * demo is listed like any other variant unless its component is marked.
     */
    val onePerScreen: Boolean get() = variants.any { it.onePerScreen }

    /** Shown edge to edge in place of the app's chrome, because it is screen chrome itself. */
    val fullScreen: Boolean get() = variants.any { it.fullScreen }
}

/** Entries gathered into their components, in registry order. */
fun List<CatalogEntry>.components(): List<CatalogComponent> =
    groupBy { it.group }.map { (group, members) -> CatalogComponent(group, members.first().groupName, members) }

/** The entries in [group], in registry order: a component's variants. */
fun List<CatalogEntry>.variantsOf(group: String): List<CatalogEntry> = filter { it.group == group }

/** Entries in registry order, gathered under the name of their component. */
fun List<CatalogEntry>.byGroup(): List<Pair<String, List<CatalogEntry>>> = groupBy { it.groupName }.toList()
