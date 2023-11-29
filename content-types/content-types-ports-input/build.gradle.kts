// JVM Test Suite is still incubating, but expected to be stable soon, so disabling the warning.
@file:Suppress("UnstableApiUsage")

plugins {
    id("org.orkg.kotlin-conventions")
    id("org.orkg.neo4j-conventions")
    id("org.orkg.jackson-conventions")
    kotlin("plugin.spring")
    alias(libs.plugins.spotless)
}

dependencies {
    api(platform(project(":platform")))

    implementation(project(":common"))
    implementation(project(":graph:graph-core-model"))
    implementation(project(":graph:graph-ports-input"))
    implementation(project(":community:community-core-model"))
    implementation(project(":content-types:content-types-core-model"))

    implementation("org.springframework.data:spring-data-commons")
    implementation(libs.jackson.core)
    implementation(libs.forkhandles.values4k)
}
