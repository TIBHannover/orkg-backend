// JVM Test Suite is still incubating, but expected to be stable soon, so disabling the warning.
@file:Suppress("UnstableApiUsage")

plugins {
    id("org.orkg.gradle.input-adapter-spring-web")
    id("org.orkg.gradle.kotlin-library")
}

dependencies {
    api("com.fasterxml.jackson.core:jackson-annotations")
    // api("com.fasterxml.jackson.core:jackson-core")
    api("org.springframework:spring-web")
    // api(libs.jackson.databind)
    api("jakarta.validation:jakarta.validation-api")
    api(project(":common"))
    api(project(":community:community-core-model"))
    api(project(":community:community-ports-input-legacy"))
}
