package com.binge.designsystem.catalogregistry

/** A public no-argument `@Composable fun …Sample()` found in a `catalog/` source file. */
data class SampleDeclaration(
    val function: String,
    val group: String,
    val name: String,
    val description: String,
)

/** What one file contributed: the samples it registers and the shapes it could not register. */
data class ScanResult(
    val samples: List<SampleDeclaration>,
    val problems: List<String>,
)

/**
 * Finds the catalog's public samples in Kotlin source text.
 *
 * Source scanning rather than reflection or KSP, by decision on the catalog epic: the samples are
 * top-level, annotated one per line and carry a KDoc, which a line scan reads exactly, and a
 * reflection pass over Compose-rewritten signatures would not. A public function ending in `Sample`
 * that is not a no-argument composable is reported with its file and line instead of being skipped.
 */
object SampleScanner {
    private val declaration = Regex("""^(public\s+)?fun\s+(\w+)\s*\(""")
    private val hiddenDeclaration = Regex("""^(internal|private|protected)\s+""")
    private val sentenceEnd = Regex("""(?<=[.!?])\s+(?=[A-Z])""")
    private val kdocLink = Regex("""\[([^\]]+)]""")

    fun scan(fileName: String, source: String): ScanResult {
        val lines = source.lines()
        val group = groupOf(fileName)
        val samples = mutableListOf<SampleDeclaration>()
        val problems = mutableListOf<String>()
        for ((index, line) in lines.withIndex()) {
            if (hiddenDeclaration.containsMatchIn(line)) continue
            val function = declaration.find(line)?.groupValues?.get(2) ?: continue
            if (!function.endsWith(SUFFIX)) continue
            val where = "$fileName:${index + 1}"
            val annotations = annotationsAbove(lines, index)
            val parameters = parametersFrom(lines, index)
            when {
                annotations.none { it.startsWith(COMPOSABLE) } ->
                    problems += "$where: public $function is not @Composable; a catalog sample must be"
                parameters.isNotBlank() ->
                    problems += "$where: $function takes parameters ($parameters); a sample takes none"
                else -> samples += SampleDeclaration(
                    function = function,
                    group = group,
                    name = displayName(function),
                    description = firstSentence(kdocAbove(lines, index - annotations.size)),
                )
            }
        }
        return ScanResult(samples, problems)
    }

    /** `BingeConfirmDialogSamples.kt` → `BingeConfirmDialog`. */
    fun groupOf(fileName: String): String =
        fileName
            .substringAfterLast('/')
            .removeSuffix(".kt")
            .removeSuffix("Samples")
            .removeSuffix(SUFFIX)

    /** `FilledButtonLoadingSample` → `Filled button loading`. */
    fun displayName(function: String): String {
        val words = function.removeSuffix(SUFFIX).split(wordBoundary).filter { it.isNotEmpty() }
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

    private const val SUFFIX = "Sample"
    private const val COMPOSABLE = "@Composable"
}
