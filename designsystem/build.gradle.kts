plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ktlint)
}

/**
 * The shared design system: the components a companion app needs to look like part of Binge without
 * depending on Binge.
 *
 * A library rather than an application, and a separate Gradle build rather than a module, because
 * the consumers include it with `includeBuild` — source-level sharing, no published artifact, no
 * version skew, and no public-API commitment while the components are still moving.
 */
android {
    namespace = "com.binge.designsystem"
    compileSdk = 37

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
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
        }
    }
}

dependencies {
    val composeBom = platform(libs.compose.bom)
    implementation(composeBom)
    implementation(libs.androidx.core.ktx)
    api(libs.compose.ui)
    api(libs.compose.material3)
    implementation(libs.compose.ui.tooling.preview)

    testImplementation(libs.junit)
}
