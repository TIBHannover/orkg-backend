// JVM Test Suite is still incubating, but expected to be stable soon, so disabling the warning.
@file:Suppress("UnstableApiUsage")

plugins {
    id("org.orkg.kotlin-conventions")
    id("org.orkg.neo4j-conventions")
    id("org.orkg.jackson-conventions")
    kotlin("plugin.spring")
    id("java-test-fixtures")
    alias(libs.plugins.spotless)
}

dependencies {
    api(platform(project(":platform")))

    implementation(project(":common")) // for exceptions

    implementation(libs.jackson.databind)
}
