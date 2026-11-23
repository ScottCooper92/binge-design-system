import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.android.junit5)
}

/**
 * The shared design system: the components a companion app needs to look like part of Binge without
 * depending on Binge.
 *
 * A library rather than an application, and a separate Gradle build rather than a module, because
 * the consumers include it with `includeBuild` — source-level sharing, no published artifact, no
 * version skew, and no public-API commitment while the components are still moving.
 *
 * The group is what a consumer's `includeBuild` substitutes on: `com.binge:designsystem` resolves to
 * this project without the consumer naming a path.
 */
group = "com.binge"

android {
    namespace = "com.binge.designsystem"
    compileSdk = 37

    testFixtures {
        enable = true
    }

    defaultConfig {
        // 26 matches Binge's floor. A component that cannot run on a consumer's oldest device is
        // not shareable, whatever this build says.
        minSdk = 26
    }

    buildFeatures { compose = true }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlin {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
            freeCompilerArgs.add("-opt-in=kotlin.RequiresOptIn")
            freeCompilerArgs.add("-opt-in=com.binge.designsystem.component.DesignSystemInternalApi")
        }
    }

    testOptions {
        // The fold and window tests drive Robolectric on the JVM against components built out of
        // dimensionResource, so they need the merged Android resources on the unit-test classpath.
        unitTests.isIncludeAndroidResources = true
    }

    // A locale is complete or it does not exist — the same policy as Binge, stated here rather than
    // inherited, so a consumer's lint sees the translations it expects from this module.
    lint {
        error +=
            setOf(
                "MissingTranslation",
                "ExtraTranslation",
                "MissingQuantity",
                "UnusedQuantity",
                "StringFormatMatches",
                "StringFormatCount",
            )
    }
}

ktlint {
    filter {
        exclude { it.file.path.contains("generated/") }
    }
}

/**
 * The library holds what draws or lays out and can be described without a server, an account or a store (see CLAUDE.md).
 * Main source of this module and of `designsystem-tv` may not import a dispatcher, a data store, a network client, a view
 * model base class or a work scheduler.
 * The two files below break that today and are named here, not skipped in a baseline, so the list cannot grow quietly:
 * the save-as-made engine (its future is #1171) and the keyed view-model stores Binge's navigation uses.
 */
val libraryScopeExceptions =
    setOf(
        "designsystem/src/main/kotlin/com/binge/designsystem/save/SaveAsMade.kt",
        "designsystem/src/main/kotlin/com/binge/designsystem/viewmodel/KeyedViewModelStores.kt",
    )

val checkLibraryScope = tasks.register("checkLibraryScope") {
    group = "verification"
    description = "Checks that main source takes no dispatcher, data store, network client, view model or work scheduler."
    val root = rootProject.layout.projectDirectory.asFile
    val sources = fileTree(layout.projectDirectory.dir("src/main")) { include("**/*.kt") }
    val tvSources = fileTree(rootProject.layout.projectDirectory.dir("designsystem-tv/src/main")) { include("**/*.kt") }
    inputs.files(sources, tvSources)
    val exceptions = libraryScopeExceptions
    doLast {
        val forbidden =
            listOf(
                "kotlinx.coroutines.CoroutineDispatcher",
                "kotlinx.coroutines.Dispatchers",
                "androidx.datastore.",
                "okhttp3.",
                "retrofit2.",
                "io.ktor.",
                "androidx.lifecycle.ViewModel",
                "androidx.lifecycle.AndroidViewModel",
                "androidx.work.",
            )
        val violations = mutableListOf<String>()
        (sources.files + tvSources.files).forEach { file ->
            val relative = file.relativeTo(root).invariantSeparatorsPath
            if (relative in exceptions) return@forEach
            file.readLines().forEachIndexed { index, line ->
                val trimmed = line.trim()
                if (!trimmed.startsWith("import ")) return@forEachIndexed
                val imported = trimmed.removePrefix("import ").substringBefore(" as ").trim()
                // `androidx.lifecycle.ViewModel` is the base class; `ViewModelStore` and the rest are not.
                if (forbidden.any { if (it.endsWith(".")) imported.startsWith(it) else imported == it }) {
                    violations += "$relative:${index + 1}: imports $imported"
                }
            }
        }
        if (violations.isNotEmpty()) {
            throw GradleException(
                "This library holds no class that takes a dispatcher, a data store, a network client, a view model or a " +
                    "scheduler. Move the code to the app or an app-side shared library, or name it in libraryScopeExceptions " +
                    "with the reason.\n\nViolations:\n" + violations.joinToString("\n") { "  $it" },
            )
        }
    }
}

tasks.named("check") { dependsOn(checkLibraryScope) }

tasks.withType<Test>().configureEach {
    // ComposeRuleGuardTest reads the other modules' test sources, which Gradle would not otherwise see as inputs.
    if (name == "testDebugUnitTest") {
        inputs
            .files(
                fileTree("../designsystem-tv/src/test"),
                fileTree("../catalog-app/src/test"),
                fileTree("../designsystem-catalog/src/test"),
            ).withPropertyName("guardedTestSources")
    }
}

dependencies {
    val composeBom = platform(libs.compose.bom)
    api(composeBom)
    api(libs.compose.ui)
    api(libs.compose.material3)
    api(libs.compose.material.icons)
    // The nav shell is a NavigationSuiteScaffold; consumers compose its items, so the types are api.
    api(libs.compose.material3.adaptive.navigation.suite)
    // ListDetailSceneStrategy/BackNavigationBehavior/PaneScaffoldDirective all flow into a consumer's
    // own scene-strategy call, so the types are api too.
    api(libs.compose.material3.adaptive.navigation3)
    // The paged-list phase takes load states, so the paging types are part of its signature.
    api(libs.paging.common)
    // The save-as-made engine takes the scopes and dispatcher it writes on, so the types are part of its signature.
    api(libs.kotlinx.coroutines.core)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    // The keyed view-model stores hand a consumer a ViewModelStoreOwner and a ViewModel, so the types are part of its signature.
    api(libs.androidx.lifecycle.viewmodel.compose)
    // The step-flow template installs the system Back handler for the step it shows.
    implementation(libs.androidx.activity.compose)
    implementation(libs.material.color.utilities)
    // Foldable posture: WindowInfoTracker/FoldingFeature behind rememberHorizontalHinge.
    implementation(libs.androidx.window)
    implementation(libs.coil.compose)
    implementation(libs.coil.network.okhttp)
    debugImplementation(libs.compose.ui.tooling)

    // JUnit 5 for the plain tests; the vintage engine runs the JUnit4-style Robolectric tests on the
    // same platform, exactly as Binge's core:designsystem arranges it.
    testImplementation(libs.junit.jupiter.api)
    testRuntimeOnly(libs.junit.jupiter.engine)
    testImplementation(libs.junit.jupiter.params)
    testImplementation(libs.junit4)
    testRuntimeOnly(libs.junit.vintage.engine)
    testImplementation(libs.robolectric)
    testImplementation(libs.androidx.window.testing)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(composeBom)
    testImplementation(libs.compose.ui.test.junit4)
    debugImplementation(libs.compose.ui.test.manifest)
    testFixturesImplementation(composeBom)
    testFixturesImplementation(libs.compose.ui)
    testFixturesImplementation(libs.compose.ui.test.junit4)
    // Not for its API: material3 is pinned ahead of the BOM and pulls the Compose UI line forward, so
    // this compiles the keyboard rule against the ui-test every test runs, which has ComposeUiTestConfig.
    testFixturesImplementation(libs.compose.material3)
    testFixturesImplementation(libs.robolectric)
    testFixturesImplementation(libs.androidx.core.ktx)
    testFixturesImplementation(libs.androidx.window)
    testFixturesImplementation(libs.androidx.window.testing)
    testFixturesImplementation(libs.androidx.activity.compose)
}
