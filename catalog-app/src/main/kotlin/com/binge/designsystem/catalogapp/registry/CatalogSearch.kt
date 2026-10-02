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

/** Entries in registry order, gathered under their group. */
fun List<CatalogEntry>.byGroup(): List<Pair<String, List<CatalogEntry>>> = groupBy { it.group }.toList()
