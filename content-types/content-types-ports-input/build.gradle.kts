// JVM Test Suite is still incubating, but expected to be stable soon, so disabling the warning.
@file:Suppress("UnstableApiUsage")

plugins {
    id("org.orkg.kotlin-conventions")
    id("org.orkg.neo4j-conventions")
    id("org.orkg.jackson-conventions")
    id("java-test-fixtures")
    kotlin("plugin.spring")
    alias(libs.plugins.spotless)
}

dependencies {
    api(platform(project(":platform")))

    api(project(":content-types:content-types-core-model"))

    implementation(project(":common"))
    implementation(project(":graph:graph-core-model"))
    implementation(project(":graph:graph-ports-input"))
    implementation(project(":community:community-core-model"))

    implementation("org.springframework.data:spring-data-commons")
    implementation(libs.jackson.core)
    implementation(libs.forkhandles.values4k)

    testFixturesImplementation(project(":common"))
    testFixturesImplementation(project(":graph:graph-core-model"))
    testFixturesImplementation(project(":community:community-core-model"))
    testFixturesImplementation(project(":content-types:content-types-core-model"))
    testFixturesImplementation(project(":content-types:content-types-core-services"))
    testFixturesImplementation(project(":content-types:content-types-ports-input"))
    testFixturesImplementation(libs.forkhandles.values4k)
}
