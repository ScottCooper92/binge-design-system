package com.binge.designsystem.componentindex

import org.gradle.testkit.runner.GradleRunner
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

class ComponentIndexPluginTest {
    @TempDir
    lateinit var project: File

    private fun write(path: String, text: String) = File(project, path).apply { parentFile.mkdirs() }.writeText(text.trimIndent())

    private fun consumer(source: String) {
        write("settings.gradle.kts", "rootProject.name = \"consumer\"")
        write(
            "build.gradle.kts",
            """
            plugins {
                base
                id("com.binge.designsystem.component-index")
            }
            componentIndex {
                output.set(layout.projectDirectory.file("docs/components.md"))
                sources.from("app/src/main/kotlin")
                group("screens", "Screens")
                group("glue", "Glue")
                includeHelpers.set(true)
                moduleColumn.set(true)
            }
            """,
        )
        write("app/src/main/kotlin/Page.kt", source)
    }

    private fun run(vararg args: String) =
        GradleRunner
            .create()
            .withProjectDir(project)
            .withPluginClasspath()
            .withArguments(*args, "--stacktrace")

    private val tagged =
        """
        /**
         * A page.
         *
         * @need show a page
         * @group screens
         */
        @Composable
        fun Page() {}
        """

    @Test
    fun `generate writes the index and check passes on it, then fails when it goes stale`() {
        consumer(tagged)
        run("generateComponentIndex").build()
        val index = File(project, "docs/components.md").readText()
        assertTrue("| show a page | `Page` | A page | — | — | app |" in index, index)

        assertEquals(org.gradle.testkit.runner.TaskOutcome.SUCCESS, run("check").build().task(":checkComponentIndex")?.outcome)

        File(project, "docs/components.md").appendText("edited\n")
        assertTrue("is stale" in run("checkComponentIndex").buildAndFail().output)
    }

    @Test
    fun `an untagged public composable fails the build`() {
        consumer("@Composable\nfun Bare() {}")
        assertTrue("public Bare has no @need or @group" in run("generateComponentIndex").buildAndFail().output)
    }
}
