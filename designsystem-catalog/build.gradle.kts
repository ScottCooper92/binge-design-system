import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.screenshot)
}

/**
 * The phone design system's catalog: every `…Sample()` and `…Demo()` that shows a component in the debug-only catalog app, and the
 * `@PreviewTest` frames that render the samples against the committed baselines.
 *
 * A module of its own so that none of it ships in `:designsystem`. A consumer includes that module with `includeBuild` and compiles
 * only the components, not about 18,000 lines of samples no app imports. Only `catalog-app` and `catalog-registry` depend on this one.
 */
group = "com.binge"

android {
    namespace = "com.binge.designsystem.catalog"
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

    testOptions {
        // The tests drive Robolectric on the JVM against samples built out of dimensionResource, so they need the merged
        // Android resources on the unit-test classpath.
        unitTests.isIncludeAndroidResources = true
    }

    kotlin {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
            freeCompilerArgs.add("-opt-in=kotlin.RequiresOptIn")
            freeCompilerArgs.add("-opt-in=com.binge.designsystem.component.DesignSystemInternalApi")
        }
    }
}

ktlint {
    filter {
        exclude { it.file.path.contains("generated/") }
    }
}

tasks.withType<Test>().configureEach {
    // The screenshot tasks are Test tasks too. An empty screenshotTest source set would otherwise fail on discovering no tests,
    // and the plugin warns unless maxParallelForks is 1.
    if (name.contains("ScreenshotTest")) {
        failOnNoDiscoveredTests = false
        doFirst { maxParallelForks = 1 }
    }
    // validate*ScreenshotTest's cache key does not track the rendered output it compares against, so a cached run can validate
    // a stale render. Always re-render live, as :designsystem does.
    if (name.startsWith("validate") && name.contains("ScreenshotTest")) {
        doNotTrackState("Renders against live sources; a cached render gives a false pass or fail")
    }
}

dependencies {
    val composeBom = platform(libs.compose.bom)
    api(project(":designsystem"))
    api(composeBom)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.window)
    implementation(libs.coil.compose)
    debugImplementation(libs.compose.ui.tooling)

    // The samples' own tests: the geometry, clipping and wiring checks that render a sample. The rule, themes and shadows they
    // share are :designsystem's test fixtures.
    testImplementation(testFixtures(project(":designsystem")))
    testImplementation(libs.junit4)
    testImplementation(libs.robolectric)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(composeBom)
    testImplementation(libs.compose.ui.test.junit4)
    testImplementation(libs.androidx.window.testing)
    debugImplementation(libs.compose.ui.test.manifest)

    screenshotTestImplementation(composeBom)
    screenshotTestImplementation(libs.compose.ui.tooling)
    screenshotTestImplementation(libs.screenshot.validation.api)
}
