package com.binge.designsystem.catalogapp.registry

/** Entries whose name, group or description contain every word of [query], ignoring case. */
fun List<CatalogEntry>.matching(query: String): List<CatalogEntry> {
    val words = query
        .trim()
        .lowercase()
        .split(Regex("""\s+"""))
        .filter { it.isNotEmpty() }
    if (words.isEmpty()) return this
    return filter { entry ->
        val haystack = "${entry.name} ${entry.group} ${entry.description}".lowercase()
        words.all { it in haystack }
    }
}

/** The heading every demo is listed under, ahead of the sample groups. */
const val DEMOS_SECTION = "Demos"

/** Entries in registry order, gathered under their group, with every demo under [DEMOS_SECTION] first. */
fun List<CatalogEntry>.byGroup(): List<Pair<String, List<CatalogEntry>>> {
    val (demos, samples) = partition { it.kind == CatalogKind.Demo }
    return listOfNotNull(demos.takeIf { it.isNotEmpty() }?.let { DEMOS_SECTION to it }) + samples.groupBy { it.group }.toList()
}
