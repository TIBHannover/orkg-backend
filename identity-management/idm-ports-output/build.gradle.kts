// JVM Test Suite is still incubating, but expected to be stable soon, so disabling the warning.
@file:Suppress("UnstableApiUsage")

plugins {
    id("org.orkg.gradle.kotlin-library")
}

dependencies {
    api(project(":identity-management:idm-core-model"))

    implementation("org.springframework.data:spring-data-commons")
}
