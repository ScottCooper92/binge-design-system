package com.binge.designsystem.catalogregistry

/**
 * What a catalog entry is: a fixed fixture the screenshot suite also renders, or a stateful demo of
 * behaviour a screenshot cannot show. The suffix of the function name decides.
 */
enum class EntryKind { Sample, Demo }

/** A public no-argument `@Composable fun …Sample()` or `…Demo()` found in a `catalog/` source file. */
data class SampleDeclaration(
    val function: String,
    val group: String,
    val name: String,
    val description: String,
    val kind: EntryKind = EntryKind.Sample,
    val onePerScreen: Boolean = false,
    val catalogGroup: String? = null,
    val fullScreen: Boolean = false,
    val selfDescribing: Boolean = false,
)

/** What one file contributed: the samples it registers and the shapes it could not register. */
data class ScanResult(
    val samples: List<SampleDeclaration>,
    val problems: List<String>,
)

/**
 * Finds the catalog's public samples and demos in Kotlin source text.
 *
 * Source scanning rather than reflection or KSP, by decision on the catalog epic: the samples are
 * top-level, annotated one per line and carry a KDoc, which a line scan reads exactly, and a
 * reflection pass over Compose-rewritten signatures would not. A public function ending in `Sample` or
 * `Demo` that is not a no-argument composable is reported with its file and line, not skipped.
 *
 * Three file annotations from the catalog package are read the same way, as lines: `@file:OnePerScreen`
 * (and its `fullScreen = true`), `@file:CatalogGroup("…")`, `@file:SelfDescribing` and `@file:ScreenshotOnly`, whose file lists nothing.
 */
object SampleScanner {
    private val declaration = Regex("""^(public\s+)?fun\s+(\w+)\s*\(""")
    private val hiddenDeclaration = Regex("""^(internal|private|protected)\s+""")
    private val sentenceEnd = Regex("""(?<=[.!?])\s+(?=[A-Z])""")
    private val kdocLink = Regex("""\[([^\]]+)]""")
    private val onePerScreenMarker = Regex("""^@file:\s*([\w.]+\.)?OnePerScreen\b""")
    private val fullScreenMarker = Regex("""^@file:\s*([\w.]+\.)?OnePerScreen\(\s*fullScreen\s*=\s*true\s*\)""")
    private val selfDescribingMarker = Regex("""^@file:\s*([\w.]+\.)?SelfDescribing\b""")
    private val screenshotOnlyMarker = Regex("""^@file:\s*([\w.]+\.)?ScreenshotOnly\b""")
    private val catalogGroupMarker = Regex("""^@file:\s*([\w.]+\.)?CatalogGroup\(\s*(name\s*=\s*)?"([^"]+)"\s*\)""")

    fun scan(fileName: String, source: String): ScanResult {
        val lines = source.lines()
        val group = groupOf(fileName)
        val onePerScreen = lines.any { onePerScreenMarker.containsMatchIn(it.trim()) }
        val fullScreen = lines.any { fullScreenMarker.containsMatchIn(it.trim()) }
        val selfDescribing = lines.any { selfDescribingMarker.containsMatchIn(it.trim()) }
        val screenshotOnly = lines.any { screenshotOnlyMarker.containsMatchIn(it.trim()) }
        val catalogGroup = lines.firstNotNullOfOrNull { catalogGroupMarker.find(it.trim())?.groupValues?.get(3) }
        val samples = mutableListOf<SampleDeclaration>()
        val problems = mutableListOf<String>()
        for ((index, line) in lines.withIndex()) {
            if (hiddenDeclaration.containsMatchIn(line)) continue
            val function = declaration.find(line)?.groupValues?.get(2) ?: continue
            val kind = EntryKind.entries.firstOrNull { function.endsWith(it.name) } ?: continue
            val where = "$fileName:${index + 1}"
            val annotations = annotationsAbove(lines, index)
            val parameters = parametersFrom(lines, index)
            when {
                annotations.none { it.startsWith(COMPOSABLE) } ->
                    problems += "$where: public $function is not @Composable; a catalog ${kind.noun} must be"
                parameters.isNotBlank() ->
                    problems += "$where: $function takes parameters ($parameters); a ${kind.noun} takes none"
                else -> samples += SampleDeclaration(
                    function = function,
                    group = group,
                    name = displayName(function),
                    description = firstSentence(kdocAbove(lines, index - annotations.size)),
                    kind = kind,
                    onePerScreen = onePerScreen,
                    catalogGroup = catalogGroup,
                    fullScreen = fullScreen,
                    selfDescribing = selfDescribing,
                )
            }
        }
        // A screenshot-only file is still checked, so a bad sample fails the build, but lists nothing.
        return ScanResult(if (screenshotOnly) emptyList() else samples, problems)
    }

    /** `BingeConfirmDialogSamples.kt` → `BingeConfirmDialog`; a `…Demos.kt` file groups the same way. */
    fun groupOf(fileName: String): String {
        val stem = fileName.substringAfterLast('/').removeSuffix(".kt")
        return EntryKind.entries.fold(stem) { name, kind -> name.removeSuffix(kind.name + "s").removeSuffix(kind.name) }
    }

    /** `FilledButtonLoadingSample` → `Filled button loading`. */
    fun displayName(function: String): String {
        val stem = EntryKind.entries.fold(function) { name, kind -> name.removeSuffix(kind.name) }
        val words = stem.split(wordBoundary).filter { it.isNotEmpty() }
        return words
            .mapIndexed { i, word -> if (i == 0 || word.all { it.isUpperCase() }) word else word.lowercase() }
            .joinToString(" ")
    }

    private val wordBoundary = Regex("""(?<=[a-z0-9])(?=[A-Z])|(?<=[A-Z])(?=[A-Z][a-z])""")

    private fun annotationsAbove(lines: List<String>, declarationIndex: Int): List<String> {
        val found = ArrayDeque<String>()
        var i = declarationIndex - 1
        while (i >= 0 && lines[i].trimStart().startsWith("@")) {
            found.addFirst(lines[i].trim())
            i--
        }
        return found.toList()
    }

    private fun parametersFrom(lines: List<String>, declarationIndex: Int): String {
        val text = lines.drop(declarationIndex).joinToString("\n")
        val open = text.indexOf('(')
        var depth = 0
        for (i in open until text.length) {
            when (text[i]) {
                '(' -> depth++
                ')' -> if (--depth == 0) return text.substring(open + 1, i).trim()
            }
        }
        return text.substring(open + 1).trim()
    }

    private fun kdocAbove(lines: List<String>, firstAnnotationIndex: Int): List<String> {
        val end = firstAnnotationIndex - 1
        if (end < 0 || !lines[end].trim().endsWith("*/")) return emptyList()
        val start = (end downTo 0).firstOrNull { lines[it].trim().startsWith("/**") } ?: return emptyList()
        return (start..end).map { row ->
            lines[row]
                .trim()
                .removePrefix("/**")
                .removeSuffix("*/")
                .trim()
                .removePrefix("*")
                .trim()
        }
    }

    private fun firstSentence(kdoc: List<String>): String {
        val prose = kdoc.dropWhile { it.isEmpty() }.takeWhile { it.isNotEmpty() && !it.startsWith("@") }.joinToString(" ")
        val cleaned = kdocLink
            .replace(prose, "$1")
            .replace("`", "")
            .replace(Regex("""\s+"""), " ")
            .trim()
        return cleaned
            .split(sentenceEnd)
            .first()
            .trimEnd('.', '!', '?')
            .trim()
    }

    private val EntryKind.noun get() = name.lowercase()
    private const val COMPOSABLE = "@Composable"
}
