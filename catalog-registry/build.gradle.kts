import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.ktlint)
}

/**
 * The generator behind the catalog app's registry: it reads the design system's `catalog/` sources
 * and writes the Kotlin file that lists every public sample. A module of its own, rather than code
 * in `:catalog-app`'s build script, so the parser has unit tests. Plain JVM: it never touches
 * Android, which is also what keeps it inert for the consumers that include this build.
 */
java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

kotlin {
    compilerOptions { jvmTarget.set(JvmTarget.JVM_17) }
}

tasks.test { useJUnitPlatform() }

dependencies {
    testImplementation(libs.junit.jupiter.api)
    testRuntimeOnly(libs.junit.jupiter.engine)
    testRuntimeOnly(libs.junit.platform.launcher)
}

/**
 * The need-keyed component index, `docs/components.md`, built from the `@need`, `@group` and `@replaces` tags
 * in the public composables' KDoc. `generateComponentIndex` writes it; `checkComponentIndex` (part of `check`)
 * fails when the file is stale or a public composable is untagged. Read by relative path, as the catalog
 * registry is, so this module needs no dependency on the Android modules.
 */
val indexSources = listOf(
    "src=${rootProject.file("designsystem/src/main/kotlin")}",
    "src=${rootProject.file("designsystem-tv/src/main/kotlin")}",
    "catalog=${rootProject.file("designsystem-catalog/src/main/kotlin/com/binge/designsystem/catalog")}",
    "catalog=${rootProject.file("designsystem-tv/src/main/kotlin/com/binge/designsystem/tv/catalog")}",
)
val indexFile = rootProject.file("docs/components.md")

fun indexTask(
    name: String,
    mode: String,
    text: String,
) = tasks.register<JavaExec>(name) {
    group = "documentation"
    description = text
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("com.binge.designsystem.catalogregistry.ComponentIndexGenerator")
    args(listOf(mode, indexFile.absolutePath) + indexSources)
    inputs.files(
        fileTree(rootProject.file("designsystem/src/main/kotlin")),
        fileTree(rootProject.file("designsystem-catalog/src/main/kotlin")),
        fileTree(rootProject.file("designsystem-tv/src/main/kotlin")),
    )
}

indexTask("generateComponentIndex", "write", "Writes docs/components.md from the KDoc tags.")
val checkComponentIndex =
    indexTask("checkComponentIndex", "check", "Fails if docs/components.md is stale or a public composable is untagged.")
tasks.named("check") { dependsOn(checkComponentIndex) }
