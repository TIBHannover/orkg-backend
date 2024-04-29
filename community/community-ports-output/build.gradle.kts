// JVM Test Suite is still incubating, but expected to be stable soon, so disabling the warning.
@file:Suppress("UnstableApiUsage")

plugins {
    id("org.orkg.gradle.kotlin-library-with-test-fixtures")
}

dependencies {
    api("org.springframework.data:spring-data-commons")
    api(project(":common"))
    api(project(":community:community-core-model"))
    api(project(":graph:graph-core-model"))
    testFixturesApi("io.kotest:kotest-framework-api")
    testFixturesApi("org.junit.jupiter:junit-jupiter-api")
    testFixturesApi(project(":community:community-ports-output"))
    testFixturesApi(project(":graph:graph-ports-output"))
    testFixturesApi(project(":identity-management:idm-ports-output"))
    testFixturesImplementation("io.kotest:kotest-assertions-shared")
    testFixturesImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
    testFixturesImplementation("org.springframework.data:spring-data-commons")
    testFixturesImplementation(libs.forkhandles.fabrikate4k)
    testFixturesImplementation(libs.kotest.assertions.core)
    testFixturesImplementation(libs.kotest.runner)
    testFixturesImplementation(project(":common"))
    testFixturesImplementation(project(":community:community-core-model"))
    testFixturesImplementation(project(":graph:graph-core-model"))
    testFixturesImplementation(project(":identity-management:idm-core-model"))
    testFixturesImplementation(project(":media-storage:media-storage-core-model"))
    testFixturesImplementation(project(":testing:kotest"))
    testFixturesImplementation(testFixtures(project(":community:community-core-model")))
    testFixturesImplementation(testFixtures(project(":graph:graph-core-model")))
    testFixturesImplementation(testFixtures(project(":identity-management:idm-core-model")))
}
