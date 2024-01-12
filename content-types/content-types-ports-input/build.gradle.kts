// JVM Test Suite is still incubating, but expected to be stable soon, so disabling the warning.
@file:Suppress("UnstableApiUsage")

plugins {
    id("org.orkg.gradle.kotlin-library-with-test-fixtures")
}

dependencies {
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
