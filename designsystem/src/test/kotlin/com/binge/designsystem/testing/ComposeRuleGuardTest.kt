package com.binge.designsystem.testing

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * A test that leaves work pending on Compose's shared UI dispatcher when its rule disposes breaks later tests in the same
 * JVM, depending on order (#1067). The take-down rules drain it, so a plain `createComposeRule()`,
 * `createKeyboardComposeRule()` or `createAndroidComposeRule()` in these modules' tests is a hazard that CI cannot see until
 * some other test falls over.
 */
class ComposeRuleGuardTest {
    private val sourceRoots = listOf("designsystem/src/test", "designsystem-tv/src/test", "catalog-app/src/test")

    /**
     * Tests that assert a throw from composition itself, which leave nothing pending and where the wrapper moves the throw,
     * and this file, which names the rules it forbids.
     */
    private val allowed = setOf("BingeStopSliderTest.kt", "ComposeRuleGuardTest.kt")

    private val plainRule = Regex("""\b(createComposeRule|createKeyboardComposeRule)\(|\bcreateAndroidComposeRule[<(]""")

    @Test
    fun `no test builds a plain compose rule`() {
        val repoRoot = generateSequence(File("").absoluteFile) { it.parentFile }.first { File(it, "settings.gradle.kts").exists() }
        val offenders =
            sourceRoots
                .map { File(repoRoot, it) }
                .filter { it.exists() }
                .flatMap { root -> root.walkTopDown().filter { it.isFile && it.extension == "kt" }.toList() }
                .filter { it.name !in allowed }
                .filter { plainRule.containsMatchIn(it.readText()) }
                .map { it.relativeTo(repoRoot).path }

        assertTrue(
            "Use createTakeDownComposeRule(), createTakeDownKeyboardComposeRule() or createTakeDownAndroidComposeRule() (#1067), not a plain rule, in: $offenders",
            offenders.isEmpty(),
        )
    }
}
