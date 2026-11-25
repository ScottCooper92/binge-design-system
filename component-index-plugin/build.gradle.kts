import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.ktlint)
    `java-gradle-plugin`
}

/**
 * The Gradle plugin that gives any repository this repository's component index: `com.binge.designsystem.component-index`. It
 * wraps `:catalog-registry`'s generator, so the tag grammar (`@need`, `@group`, `@replaces`, `@sample`) is one implementation. A
 * consumer includes this build for its plugins (see the README) and configures the sources, the groups and the output file.
 */
java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

kotlin {
    compilerOptions { jvmTarget.set(JvmTarget.JVM_17) }
}

gradlePlugin {
    plugins {
        create("componentIndex") {
            id = "com.binge.designsystem.component-index"
            implementationClass = "com.binge.designsystem.componentindex.ComponentIndexPlugin"
        }
    }
}

tasks.test { useJUnitPlatform() }

dependencies {
    api(project(":catalog-registry"))
    testImplementation(gradleTestKit())
    testImplementation(libs.junit.jupiter.api)
    testRuntimeOnly(libs.junit.jupiter.engine)
    testRuntimeOnly(libs.junit.platform.launcher)
}
