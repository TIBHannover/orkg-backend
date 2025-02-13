// JVM Test Suite is still incubating, but expected to be stable soon, so disabling the warning.
@file:Suppress("UnstableApiUsage")

plugins {
    id("org.orkg.gradle.kotlin-library-with-test-fixtures")
}

dependencies {
    api("org.springframework.data:spring-data-commons")
    api(project(":graph:graph-core-model"))
    testFixturesApi("io.kotest:kotest-framework-api")
    testFixturesApi(project(":graph:graph-ports-output"))
    testFixturesImplementation("io.kotest:kotest-assertions-shared")
    testFixturesImplementation("org.springframework.data:spring-data-commons")
    testFixturesImplementation("dev.forkhandles:fabrikate4k")
    testFixturesImplementation("io.kotest:kotest-runner-junit5")
    testFixturesImplementation("io.kotest:kotest-assertions-core")
    testFixturesImplementation(project(":common:identifiers"))
    testFixturesImplementation(project(":graph:graph-core-constants"))
    testFixturesImplementation(project(":graph:graph-core-model"))
    testFixturesImplementation(testFixtures(project(":graph:graph-core-model")))
}
