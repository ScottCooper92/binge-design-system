import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.tasks.Classpath
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.process.ExecOperations
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import javax.inject.Inject

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.android.junit5)
}

/**
 * Debug-only catalog app: lists every public `…Sample()` in the design system's `catalog/` package
 * and shows it full screen on a device, with live controls for dark mode, font scale and layout
 * direction. Run it with `./gradlew :catalog-app:installDebug`; it is never published or signed.
 *
 * The package differs from the application id on purpose. `com.binge.designsystem.catalog` already
 * holds the samples, so the app's namespace keeps its generated `R` class out of that package, and
 * the id differs from Binge's and binge-seerr's so it installs beside them.
 */
android {
    namespace = "com.binge.designsystem.catalogapp"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.binge.designsystem.catalog"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "0.1"
    }

    buildFeatures { compose = true }

    testOptions {
        // The UI tests read the app's own resources (strings, dimens) under Robolectric.
        unitTests.isIncludeAndroidResources = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlin {
        compilerOptions { jvmTarget.set(JvmTarget.JVM_17) }
    }
}

androidComponents {
    // Debug only: no release variant exists to ship or sign.
    beforeVariants { variant -> variant.enable = variant.buildType == "debug" }
}

/**
 * Writes `CatalogRegistry.kt` and `TvCatalogRegistry.kt` from the two design-system modules' catalog
 * sources by running `:catalog-registry`. The samples are read by relative path, not through the
 * `:designsystem` and `:designsystem-tv` projects, so this build stays free of cross-project access.
 */
abstract class GenerateCatalogRegistry
    @Inject
    constructor(
        private val exec: ExecOperations,
    ) : DefaultTask() {
        @get:InputFiles
        @get:PathSensitive(PathSensitivity.RELATIVE)
        abstract val samples: ConfigurableFileCollection

        @get:Classpath
        abstract val generator: ConfigurableFileCollection

        @get:OutputDirectory
        abstract val outputDir: DirectoryProperty

        @get:Internal
        abstract val phoneSamplesDir: DirectoryProperty

        @get:Internal
        abstract val tvSamplesDir: DirectoryProperty

        @TaskAction
        fun generate() {
            val out = outputDir.get().asFile.also { it.deleteRecursively() }
            exec.javaexec {
                classpath = generator
                mainClass.set("com.binge.designsystem.catalogregistry.RegistryGenerator")
                args(
                    out.absolutePath,
                    "CatalogRegistry=com.binge.designsystem.catalog=${phoneSamplesDir.get().asFile.absolutePath}",
                    "TvCatalogRegistry=com.binge.designsystem.tv.catalog=${tvSamplesDir.get().asFile.absolutePath}",
                )
            }
        }
    }

val generator by configurations.creating {
    isCanBeConsumed = false
    isCanBeResolved = true
}

dependencies {
    generator(project(":catalog-registry"))
    implementation(project(":designsystem"))
    implementation(project(":designsystem-tv"))
    implementation(libs.androidx.activity.compose)
    debugImplementation(libs.compose.ui.tooling)

    // JUnit 5 for the plain tests; the vintage engine runs the JUnit4-style Robolectric UI tests on
    // the same platform, as :designsystem does.
    testImplementation(libs.junit.jupiter.api)
    testRuntimeOnly(libs.junit.jupiter.engine)
    testImplementation(libs.junit4)
    testRuntimeOnly(libs.junit.vintage.engine)
    testImplementation(libs.robolectric)
    testImplementation(platform(libs.compose.bom))
    testImplementation(libs.compose.ui.test.junit4)
    debugImplementation(libs.compose.ui.test.manifest)
}

val phoneCatalogSources = layout.projectDirectory.dir("../designsystem/src/main/kotlin/com/binge/designsystem/catalog")
val tvCatalogSources = layout.projectDirectory.dir("../designsystem-tv/src/main/kotlin/com/binge/designsystem/tv/catalog")

androidComponents {
    onVariants { variant ->
        val generate =
            tasks.register<GenerateCatalogRegistry>("generate${variant.name.replaceFirstChar { it.uppercase() }}CatalogRegistry") {
                samples.from(fileTree(phoneCatalogSources) { include("*.kt") }, fileTree(tvCatalogSources) { include("*.kt") })
                phoneSamplesDir.set(phoneCatalogSources)
                tvSamplesDir.set(tvCatalogSources)
                generator.from(configurations.named("generator"))
                outputDir.set(layout.buildDirectory.dir("generated/catalogRegistry/${variant.name}"))
            }
        variant.sources.kotlin?.addGeneratedSourceDirectory(generate, GenerateCatalogRegistry::outputDir)
    }
}

ktlint {
    filter {
        exclude { it.file.path.contains("generated/") }
    }
}
