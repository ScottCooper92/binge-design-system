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
