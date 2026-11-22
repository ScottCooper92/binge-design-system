@file:JvmName("ComponentIndexGenerator")

package com.binge.designsystem.catalogregistry

import java.io.File
import kotlin.system.exitProcess

/** A group in the index: the `@group` key a KDoc names, and the heading it appears under. */
enum class IndexGroup(
    val key: String,
    val heading: String,
) {
    Screens("screens", "Screens and scaffolds"),
    Bars("bars", "Bars"),
    Sheets("sheets", "Sheets and pickers"),
    Rows("rows", "Rows and lists"),
    Cards("cards", "Cards and tiles"),
    Media("media", "Media and carousels"),
    Inputs("inputs", "Inputs"),
    Feedback("feedback", "Feedback and state"),
    Detail("detail", "Detail pages"),
    Tv("tv", "TV"),
}

/** One public composable as the index lists it. */
data class IndexEntry(
    val name: String,
    val group: IndexGroup,
    val need: String,
    val replaces: String?,
    val description: String,
    val sample: String?,
)

/** What one file contributed to the index, and what it could not. */
data class IndexScan(
    val entries: List<IndexEntry>,
    val problems: List<String>,
)

/**
 * The need-keyed component index (`docs/components.md`), built from the sources by line scanning, the way
 * the catalog registry is: no Kotlin parser, so the same three tags and one regex port to another repository.
 *
 * A public component is a top-level `fun` with a capitalised name under `@Composable`, not `private` or
 * `internal`, and not named `…Preview`, `…Sample` or `…Demo`. Its KDoc carries, one per line:
 *
 * - `@need` the need in the reader's words ("pick one value from a long list");
 * - `@group` one of [IndexGroup]'s keys;
 * - `@replaces` (optional) what it saves a caller from building;
 * - `@sample` (optional) the catalog sample to copy, when the name does not follow `<Component>Sample`.
 *
 * The first KDoc sentence is the description column. Overloads each carry the tags; the index lists a name once.
 */
object ComponentIndex {
    private val declaration =
        Regex("""^((?:@\w+(?:\([^)]*\))?\s+)*)(public\s+)?(inline\s+)?fun\s+(?:<[^>]*>\s*)?(?:[\w<>?]+\.)?([A-Z]\w*)\s*\(""")
    private val hidden = Regex("""^(?:@\w+(?:\([^)]*\))?\s+)*(internal|private|protected)\s+""")
    private val tag = Regex("""^@(need|group|replaces|sample)\s+(\S.*)$""")
    private val excludedSuffixes = listOf("Preview", "Sample", "Demo")

    fun scan(fileName: String, source: String): IndexScan {
        val lines = source.lines()
        val entries = mutableListOf<IndexEntry>()
        val problems = mutableListOf<String>()
        for ((index, line) in lines.withIndex()) {
            if (hidden.containsMatchIn(line)) continue
            val match = declaration.find(line) ?: continue
            val name = match.groupValues[4]
            if (excludedSuffixes.any { name.endsWith(it) }) continue
            val above = SampleScanner.annotationsAbove(lines, index)
            if (above.none { it.startsWith("@Composable") } && "@Composable" !in match.groupValues[1]) continue
            val where = "$fileName:${index + 1}"
            val kdoc = SampleScanner.kdocAbove(lines, index - above.size)
            val tags = kdoc.mapNotNull { tag.find(it) }.associate { it.groupValues[1] to it.groupValues[2].trim() }
            val need = tags["need"]
            val groupKey = tags["group"]
            val group = IndexGroup.entries.firstOrNull { it.key == groupKey }
            when {
                need == null || groupKey == null ->
                    problems +=
                        "$where: public $name has no ${listOfNotNull(
                            "@need".takeIf { need == null },
                            "@group".takeIf { groupKey == null },
                        ).joinToString(" or ")} in its KDoc"
                group == null ->
                    problems += "$where: $name has unknown @group '$groupKey'; use one of ${IndexGroup.entries.joinToString { it.key }}"
                else -> entries += IndexEntry(
                    name = name,
                    group = group,
                    need = need,
                    replaces = tags["replaces"],
                    description = SampleScanner.firstSentence(kdoc),
                    sample = tags["sample"],
                )
            }
        }
        return IndexScan(entries, problems)
    }

    /** The sample to copy for [name]: its `@sample` tag, else `<name>Sample`, else the first `<name>…Sample`. */
    fun sampleFor(entry: IndexEntry, samples: Set<String>): String? =
        entry.sample
            ?: "${entry.name}Sample".takeIf { it in samples }
            ?: samples.filter { it.startsWith(entry.name) }.minOrNull()

    /** Every problem the sources have, as lines; empty when the index can be written. */
    fun problems(sources: List<Pair<String, String>>, catalogSources: List<Pair<String, String>>): List<String> {
        val samples = sampleNames(catalogSources)
        val scans = sources.sortedBy { it.first }.map { (path, text) -> scan(path, text) }
        val unknownSamples = scans
            .flatMap { it.entries }
            .filter { it.sample != null && it.sample !in samples }
            .map { "${it.name}: @sample ${it.sample} is not a catalog sample" }
        return scans.flatMap { it.problems } + unknownSamples
    }

    fun render(sources: List<Pair<String, String>>, catalogSources: List<Pair<String, String>>): String {
        val samples = sampleNames(catalogSources)
        val entries = sources
            .sortedBy { it.first }
            .flatMap { (path, text) -> scan(path, text).entries }
            .distinctBy { it.name to it.need }
        return buildString {
            appendLine("<!-- Generated by :catalog-registry (./gradlew generateComponentIndex) from the @need, @group and @replaces -->")
            appendLine("<!-- tags in each public composable's KDoc. Do not edit; change the KDoc and regenerate. -->")
            appendLine()
            appendLine("# Component index")
            appendLine()
            appendLine("Find a component by what you need, not by its name. Each row gives the component, what it is, the catalog")
            appendLine("sample to copy and what it replaces. If a need is not here, check `catalog/` before building one in an app.")
            for (group in IndexGroup.entries) {
                val rows = entries.filter { it.group == group }.sortedWith(compareBy({ it.need.lowercase() }, { it.name }))
                if (rows.isEmpty()) continue
                appendLine()
                appendLine("## ${group.heading}")
                appendLine()
                appendLine("| Need | Component | What it is | Sample | Replaces |")
                appendLine("| --- | --- | --- | --- | --- |")
                for (row in rows) {
                    val sample = sampleFor(row, samples)?.let { "`$it`" } ?: "—"
                    appendLine("| ${cell(row.need)} | `${row.name}` | ${cell(row.description)} | $sample | ${cell(row.replaces ?: "—")} |")
                }
            }
        }
    }

    private fun sampleNames(catalogSources: List<Pair<String, String>>): Set<String> =
        catalogSources
            .flatMap { (path, text) -> SampleScanner.scan(path, text).declared }
            .filter { it.kind == EntryKind.Sample }
            .map { it.function }
            .toSet()

    private fun cell(text: String): String = text.replace("|", "\\|")
}

/**
 * `write <output> <root>…` writes the index; `check <output> <root>…` fails if the file differs or a public
 * composable is untagged. A root is `src=<dir>` (composable sources) or `catalog=<dir>` (samples).
 */
fun main(args: Array<String>) {
    val mode = args.getOrNull(0)
    val output = File(args.getOrNull(1) ?: usage())

    fun kotlinFiles(prefix: String) =
        args
            .drop(2)
            .filter { it.startsWith("$prefix=") }
            .map { File(it.substringAfter('=')) }
            .flatMap { root ->
                root
                    .walkTopDown()
                    .filter { it.isFile && it.extension == "kt" }
                    .map { it.relativeTo(root.parentFile).invariantSeparatorsPath to it.readText() }
                    .toList()
            }
    val sources = kotlinFiles("src")
    val catalog = kotlinFiles("catalog")
    val problems = ComponentIndex.problems(sources, catalog)
    if (problems.isNotEmpty()) {
        System.err.println("The component index cannot be built:\n" + problems.joinToString("\n") { "  $it" })
        exitProcess(1)
    }
    val rendered = ComponentIndex.render(sources, catalog)
    when (mode) {
        "write" -> output.apply { parentFile.mkdirs() }.writeText(rendered)
        "check" -> if (!output.exists() || output.readText() != rendered) {
            System.err.println("${output.path} is stale. Run ./gradlew generateComponentIndex and commit the result.")
            exitProcess(1)
        }
        else -> usage()
    }
}

private fun usage(): Nothing {
    System.err.println("usage: write|check <output> src=<dir>… catalog=<dir>…")
    exitProcess(2)
}
