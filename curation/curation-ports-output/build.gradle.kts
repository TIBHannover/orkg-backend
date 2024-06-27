// JVM Test Suite is still incubating, but expected to be stable soon, so disabling the warning.
@file:Suppress("UnstableApiUsage")

plugins {
    id("org.orkg.gradle.kotlin-library-with-test-fixtures")
}

dependencies {
    api("org.springframework.data:spring-data-commons:2.7.16")
    api(project(":graph:graph-core-model"))
    testFixturesApi("io.kotest:kotest-framework-api")
    testFixturesApi(project(":graph:graph-ports-output"))
    testFixturesImplementation("io.kotest:kotest-assertions-shared")
    testFixturesImplementation("org.springframework.data:spring-data-commons:2.7.16")
    testFixturesImplementation(libs.forkhandles.fabrikate4k)
    testFixturesImplementation(libs.kotest.runner)
    testFixturesImplementation(libs.kotest.assertions.core)
    testFixturesImplementation(project(":common"))
    testFixturesImplementation(project(":graph:graph-core-model"))
    testFixturesImplementation(testFixtures(project(":graph:graph-core-model")))
}
