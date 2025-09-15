// JVM Test Suite is still incubating, but expected to be stable soon, so disabling the warning.
@file:Suppress("UnstableApiUsage")

plugins {
    id("org.orkg.gradle.kotlin-library-with-test-fixtures")
}

dependencies {
    testFixturesApi("io.kotest:kotest-framework-engine")
    testFixturesApi(project(":graph:graph-ports-output"))
    testFixturesImplementation("io.kotest:kotest-assertions-core")
    testFixturesImplementation("dev.forkhandles:fabrikate4k")
    testFixturesImplementation("io.kotest:kotest-runner-junit5")
    testFixturesImplementation(project(":common:core-identifiers"))
    testFixturesImplementation(project(":graph:graph-core-constants"))
    testFixturesImplementation(project(":graph:graph-core-model"))
    testFixturesImplementation(testFixtures(project(":graph:graph-core-model")))
}
