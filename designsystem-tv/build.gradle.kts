import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.screenshot)
    alias(libs.plugins.android.junit5)
}

/**
 * The design system's television layer: the tv-material components and the focus units a companion's TV
 * surface is built from — the theme projected from the same tokens as the phone theme, the focus groups and
 * indicator, the navigation rail shell, the button, the card row, the message plate, and the preview matrix.
 *
 * A module of its own rather than more of `:designsystem` because Material 3 and tv-material must not be
 * mixed: they ship separate `MaterialTheme` trees, and the wrong import compiles cleanly and renders subtly
 * wrong. `checkTvMaterialSeparation` below holds the line the same way Binge's build does.
 */
group = "com.binge"

android {
    namespace = "com.binge.designsystem.tv"
    compileSdk = 37

    defaultConfig {
        minSdk = 26
    }

    buildFeatures { compose = true }
    experimentalProperties["android.experimental.enableScreenshotTest"] = true

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlin {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
            freeCompilerArgs.add("-opt-in=kotlin.RequiresOptIn")
        }
    }

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

tasks.withType<Test>().configureEach {
    if (name.contains("ScreenshotTest")) {
        failOnNoDiscoveredTests = false
        doFirst { maxParallelForks = 1 }
    }
    if (name.startsWith("validate") && name.contains("ScreenshotTest")) {
        doNotTrackState("Renders against live sources; a cached render gives a false pass or fail")
    }
}

/**
 * TV source imports only `androidx.tv.material3`; the phone module imports only `androidx.compose.material3`.
 * The one sanctioned crossing is the token adapter, which reads the phone theme's colour scheme to project the
 * TV one slot for slot, and it is named here rather than in a file so the list cannot grow quietly.
 */
val tvMaterialSeparationAllowlist = setOf("designsystem-tv/src/main/kotlin/com/binge/designsystem/tv/theme/BingeTvTokens.kt")

val checkTvMaterialSeparation by tasks.registering {
    group = "verification"
    description = "Checks that Material 3 and tv-material stay on their own sides of the TV seam."
    val root = rootProject.layout.projectDirectory.asFile
    val tvSources = fileTree(layout.projectDirectory.dir("src")) { include("**/*.kt") }
    val phoneSources = fileTree(rootProject.layout.projectDirectory.dir("designsystem/src")) { include("**/*.kt") }
    inputs.files(tvSources, phoneSources)
    val allowlist = tvMaterialSeparationAllowlist
    doLast {
        val violations = mutableListOf<String>()

        fun scan(
            files: Iterable<File>,
            forbidden: String,
            side: String,
        ) {
            files.forEach { file ->
                val relative = file.relativeTo(root).invariantSeparatorsPath
                if (relative in allowlist) return@forEach
                file.readLines().forEachIndexed { index, line ->
                    val trimmed = line.trimStart()
                    if (trimmed.startsWith("import ") && trimmed.removePrefix("import ").trim().startsWith("$forbidden.")) {
                        violations += "$relative:${index + 1}: $side imports ${trimmed.removePrefix("import ").trim()}"
                    }
                }
            }
        }
        scan(tvSources.files, "androidx.compose.material3", "TV source")
        scan(phoneSources.files, "androidx.tv", "phone source")
        if (violations.isNotEmpty()) {
            throw GradleException(
                "Material 3 and tv-material must not be mixed. TV source may only use androidx.tv.material3; " +
                    "phone source may only use androidx.compose.material3.\n\nViolations:\n" +
                    violations.joinToString("\n") { "  $it" },
            )
        }
    }
}

tasks.named("check") { dependsOn(checkTvMaterialSeparation) }

dependencies {
    val composeBom = platform(libs.compose.bom)
    api(composeBom)
    // The tokens — BingeColors, BingeShapes, the typography, the dimens — and the formatters. Its Material 3
    // components are not reusable from here; a TV surface gets its own, projected from the same tokens.
    api(project(":designsystem"))
    // tv-material replaces compose-material3 here rather than sitting alongside it.
    api(libs.tv.material)
    api(libs.compose.ui)
    implementation(libs.compose.material.icons)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    // The initials avatar loads a remote avatar where one is given, through the same Coil stack as the phone's.
    implementation(libs.coil.compose)
    implementation(libs.coil.network.okhttp)
    debugImplementation(libs.compose.ui.tooling)

    screenshotTestImplementation(composeBom)
    screenshotTestImplementation(libs.compose.ui.tooling)
    screenshotTestImplementation(libs.screenshot.validation.api)

    testImplementation(libs.junit.jupiter.api)
    testRuntimeOnly(libs.junit.jupiter.engine)
}
