pluginManagement {
    includeBuild("../meta-plugins")
    includeBuild("../../include/restdocs-api-spec")
}
plugins {
    id("org.orkg.gradle.settings")
}

dependencyResolutionManagement {
    repositories.gradlePluginPortal()
    includeBuild("../meta-plugins") // for 'build-parameters'
    includeBuild("../../include/restdocs-api-spec")
    versionCatalogs {
        create("libs") {
            from(files("../../gradle/libs.versions.toml"))
        }
    }
}

include(
    "base-plugins",
    "dependency-analysis-plugins",
    "dependency-rules-plugins",
    "kotlin-application-plugins",
    "kotlin-base-plugins",
    "kotlin-library-plugins",
    "plugins-platform",
)
