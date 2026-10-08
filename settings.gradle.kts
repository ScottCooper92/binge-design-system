pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        // Google's mirror of Maven Central, asked before Central. Shared build hosts (CI runners, cloud agent sessions)
        // are rate-limited by Central and get 429s mid-build, and Gradle treats a 429 as a failure rather than a miss,
        // so the build stops even when a later repository has the artifact. Anything the mirror lacks falls through.
        maven("https://maven-central.storage-download.googleapis.com/maven2/") { name = "MavenCentralMirror" }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        // The same mirror, ahead of Central, for the same reason as above.
        maven("https://maven-central.storage-download.googleapis.com/maven2/") { name = "MavenCentralMirror" }
        mavenCentral()
    }

    // Catalog at the root rather than gradle/, matching Binge and binge-seerr, so a version bump
    // can be applied to every repository the same way. This one matters more than most: a consumer
    // includes this build, so a version disagreement here is a version disagreement in their build.
    versionCatalogs {
        create("libs") { from(files("libs.versions.toml")) }
    }
}

rootProject.name = "binge-design-system"

include(":designsystem")
include(":designsystem-tv")

// The debug-only catalog app and the generator behind its registry (see the epic, #200). Neither
// is published, and neither is substituted by a consumer, so a consumer never configures them.
include(":catalog-registry")
include(":catalog-app")
