pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
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
