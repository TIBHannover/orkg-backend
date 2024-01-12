pluginManagement {
    includeBuild("../meta-plugins")
}
plugins {
    id("org.orkg.gradle.settings")
}

dependencyResolutionManagement {
    repositories.gradlePluginPortal()
    includeBuild("../meta-plugins") // for 'build-parameters'
}
