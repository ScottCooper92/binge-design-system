package com.binge.designsystem.componentindex

import com.binge.designsystem.catalogregistry.ComponentIndex
import com.binge.designsystem.catalogregistry.IndexConfig
import com.binge.designsystem.catalogregistry.IndexGroup
import com.binge.designsystem.catalogregistry.SampleKind
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import java.io.File

/**
 * What a repository sets to get its component index. Everything but [output], [sources] and [groups] has the design system's own
 * default.
 *
 * ```
 * componentIndex {
 *     output.set(layout.projectDirectory.file("docs/components.md"))
 *     sources.from("app/src/main/kotlin", "feature/src/main/kotlin")
 *     sampleSources.from("catalog/src/test/kotlin")
 *     group("screens", "Screens")
 *     group("feedback", "Feedback and state")
 * }
 * ```
 */
abstract class ComponentIndexExtension {
    /** The file `generateComponentIndex` writes and `checkComponentIndex` compares against. */
    abstract val output: RegularFileProperty

    /** Directories of Kotlin sources scanned for tagged declarations. The first segment of a file's path from the project directory names its module (`app/src/main/kotlin/…` is `app`). */
    abstract val sources: ConfigurableFileCollection

    /** Directories of Kotlin sources that declare the samples a `@sample` tag names. Empty means no sample is checked or listed. */
    abstract val sampleSources: ConfigurableFileCollection

    /** What the sample sources declare: composable functions named `…Sample` (the default), or classes. */
    abstract val sampleKind: Property<SampleKind>

    /** Also list a helper (a class, object, interface, plain function or lowercase composable) whose KDoc carries `@need`. */
    abstract val includeHelpers: Property<Boolean>

    /** Add a column naming each entry's module. */
    abstract val moduleColumn: Property<Boolean>

    abstract val title: Property<String>

    /** The paragraph under the title. */
    abstract val intro: ListProperty<String>

    /** The comment lines at the top of the file. */
    abstract val generatedNote: ListProperty<String>

    val groupList = mutableListOf<IndexGroup>()

    /** Adds a `@group` key and the heading its rows are listed under. Groups are listed in the order they are added. */
    fun group(key: String, heading: String) {
        groupList += IndexGroup(key, heading)
    }
}

/** Reads the tagged KDoc under [sources] and writes, or checks, the index. */
@CacheableTask
abstract class GenerateComponentIndexTask : DefaultTask() {
    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val sources: ConfigurableFileCollection

    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val sampleSources: ConfigurableFileCollection

    @get:Input
    abstract val checkOnly: Property<Boolean>

    @get:Internal
    abstract val projectDir: DirectoryProperty

    /** The roots' paths from the project, which name each entry's module, so a moved root is a different input for the build cache. */
    @get:Input
    val rootPaths: List<String>
        get() = (sources.files + sampleSources.files).map { it.relativeToOrSelf(projectDir.get().asFile).invariantSeparatorsPath }.sorted()

    /** The index file: the generate task's output, and the check task's input. Declared on the task in [ComponentIndexPlugin]. */
    @get:Internal
    abstract val outputFile: RegularFileProperty

    /** The config in pieces Gradle can fingerprint, each `key=heading` for a group. */
    @get:Input
    abstract val groupSpec: ListProperty<String>

    @get:Input
    abstract val sampleKindName: Property<String>

    @get:Input
    abstract val helpers: Property<Boolean>

    @get:Input
    abstract val modules: Property<Boolean>

    @get:Input
    abstract val titleText: Property<String>

    @get:Input
    abstract val introLines: ListProperty<String>

    @get:Input
    abstract val noteLines: ListProperty<String>

    @TaskAction
    fun run() {
        val config = IndexConfig(
            groups = groupSpec.get().map { it.substringBefore('=') to it.substringAfter('=') }.map { IndexGroup(it.first, it.second) },
            sampleKind = SampleKind.valueOf(sampleKindName.get()),
            includeHelpers = helpers.get(),
            moduleColumn = modules.get(),
            title = titleText.get(),
            intro = introLines.get(),
            generatedNote = noteLines.get(),
        )
        val sourceFiles = read(sources)
        val sampleFiles = read(sampleSources)
        val problems = ComponentIndex.problems(sourceFiles, sampleFiles, config)
        if (problems.isNotEmpty()) {
            throw GradleException("The component index cannot be built:\n" + problems.joinToString("\n") { "  $it" })
        }
        val rendered = ComponentIndex.render(sourceFiles, sampleFiles, config)
        val file = outputFile.get().asFile
        if (checkOnly.get()) {
            if (!file.exists() || file.readText() != rendered) {
                throw GradleException("${file.path} is stale. Run generateComponentIndex and commit the result.")
            }
        } else {
            file.parentFile.mkdirs()
            file.writeText(rendered)
        }
    }

    /** Each Kotlin file under the roots, named by its path from the project, so the first segment is the module (`app/src/main/kotlin/…`). */
    private fun read(roots: ConfigurableFileCollection): List<Pair<String, String>> {
        val base = projectDir.get().asFile
        return roots.files.filter { it.isDirectory }.flatMap { root ->
            root
                .walkTopDown()
                .filter { it.isFile && it.extension == "kt" }
                .map { file: File ->
                    file.relativeTo(base).invariantSeparatorsPath to file.readText()
                }.toList()
        }
    }
}

/** `com.binge.designsystem.component-index`: registers `generateComponentIndex` and `checkComponentIndex` (wired into `check` when there is one). */
class ComponentIndexPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        val extension = project.extensions.create("componentIndex", ComponentIndexExtension::class.java)
        val defaults = IndexConfig()
        extension.sampleKind.convention(SampleKind.Functions)
        extension.includeHelpers.convention(false)
        extension.moduleColumn.convention(false)
        extension.title.convention(defaults.title)
        extension.intro.convention(defaults.intro)
        extension.generatedNote.convention(
            listOf(
                "<!-- Generated by the component-index plugin (./gradlew generateComponentIndex) from the @need, @group and @replaces -->",
                "<!-- tags in each public composable's KDoc. Do not edit; change the KDoc and regenerate. -->",
            ),
        )

        fun register(
            name: String,
            checkOnly: Boolean,
            text: String,
        ) = project.tasks.register(name, GenerateComponentIndexTask::class.java) { task ->
            task.group = "documentation"
            task.description = text
            task.sources.from(extension.sources)
            task.sampleSources.from(extension.sampleSources)
            task.checkOnly.set(checkOnly)
            task.projectDir.set(project.layout.projectDirectory)
            task.outputFile.set(extension.output)
            task.groupSpec.set(project.provider { extension.groupList.map { "${it.key}=${it.heading}" } })
            task.sampleKindName.set(extension.sampleKind.map { it.name })
            task.helpers.set(extension.includeHelpers)
            task.modules.set(extension.moduleColumn)
            task.titleText.set(extension.title)
            task.introLines.set(extension.intro)
            task.noteLines.set(extension.generatedNote)
            // A check writes nothing, so it reads the file; the generator owns it.
            if (checkOnly) task.inputs.files(extension.output).optional() else task.outputs.file(extension.output)
        }
        register("generateComponentIndex", false, "Writes the component index from the KDoc tags.")
        val check = register("checkComponentIndex", true, "Fails if the component index is stale or a public composable is untagged.")
        project.plugins.withId("base") { project.tasks.named("check") { it.dependsOn(check) } }
    }
}
