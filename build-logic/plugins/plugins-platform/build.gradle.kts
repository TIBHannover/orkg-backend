plugins {
    id("java-platform")
}

dependencies.constraints {
    // Gradle plugins
    api(libs.gradle.plugin.jar.antora)
    api(libs.gradle.plugin.jar.buildconfig)
    api(libs.gradle.plugin.jar.dependency.analysis)
    api(libs.gradle.plugin.jar.doctor)
    api(libs.gradle.plugin.jar.jib)
    api(libs.gradle.plugin.jar.jvm.dependency.conflict.resolution)
    api(libs.gradle.plugin.jar.kotlin)
    api(libs.gradle.plugin.jar.kotlin.allopen)
    api(libs.gradle.plugin.jar.kotlin.noarg)
    api(libs.gradle.plugin.jar.node)
    api(libs.gradle.plugin.jar.openapi.generator)
    api(libs.gradle.plugin.jar.spotless)
    api(libs.gradle.plugin.jar.spotless.changelog)
    api(libs.gradle.plugin.jar.spring.antora)
    api(libs.gradle.plugin.jar.spring.boot)
    api(libs.gradle.plugin.jar.versions)
    // Regular libraries
    api(libs.diff.utils)
}
