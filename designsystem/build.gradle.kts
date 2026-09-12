import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.screenshot)
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

    defaultConfig {
        // 26 matches Binge's floor. A component that cannot run on a consumer's oldest device is
        // not shareable, whatever this build says.
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

tasks.withType<Test>().configureEach {
    // The screenshot tasks are Test tasks too. An empty screenshotTest source set would otherwise
    // fail on discovering no tests, and the plugin warns unless maxParallelForks is 1.
    if (name.contains("ScreenshotTest")) {
        failOnNoDiscoveredTests = false
        doFirst { maxParallelForks = 1 }
    }
    // validate*ScreenshotTest's cache key does not track the rendered output it compares against,
    // so a cached run can validate a stale render. Always re-render live, as Binge does.
    if (name.startsWith("validate") && name.contains("ScreenshotTest")) {
        doNotTrackState("Renders against live sources; a cached render gives a false pass or fail")
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
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.compose.ui.text.google.fonts)
    implementation(libs.androidx.core.ktx)
    implementation(libs.material.color.utilities)
    // Foldable posture: WindowInfoTracker/FoldingFeature behind rememberVerticalHinge.
    implementation(libs.androidx.window)
    implementation(libs.coil.compose)
    implementation(libs.coil.network.okhttp)
    debugImplementation(libs.compose.ui.tooling)

    screenshotTestImplementation(composeBom)
    screenshotTestImplementation(libs.compose.ui.tooling)
    screenshotTestImplementation(libs.screenshot.validation.api)

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
}
