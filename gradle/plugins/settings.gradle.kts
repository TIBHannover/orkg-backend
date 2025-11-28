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
}
