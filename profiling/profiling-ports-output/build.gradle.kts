// JVM Test Suite is still incubating, but expected to be stable soon, so disabling the warning.
@file:Suppress("UnstableApiUsage")

plugins {
    id("org.orkg.kotlin-conventions")
    alias(libs.plugins.spotless)
    kotlin("plugin.spring")
}

dependencies {
    api(platform(project(":platform")))
    implementation(project(":profiling:profiling-core-model"))
}
