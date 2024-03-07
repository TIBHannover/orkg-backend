// JVM Test Suite is still incubating, but expected to be stable soon, so disabling the warning.
@file:Suppress("UnstableApiUsage")

plugins {
    id("org.orkg.gradle.kotlin-library-with-test-fixtures")
}

dependencies {
    api(project(":community:community-core-model"))

    implementation(project(":common"))
    implementation(project(":graph:graph-core-model"))

    implementation("org.springframework.data:spring-data-commons")

    testFixturesImplementation(project(":common"))
    testFixturesImplementation(project(":testing:kotest"))
    testFixturesImplementation(project(":identity-management:idm-core-model"))
    testFixturesImplementation(testFixtures(project(":identity-management:idm-core-model")))
    testFixturesImplementation(project(":identity-management:idm-ports-output"))
    testFixturesImplementation(project(":community:community-core-model"))
    testFixturesImplementation(testFixtures(project(":community:community-core-model")))
    testFixturesImplementation(project(":community:community-ports-output"))
    testFixturesImplementation(project(":community:community-adapter-output-spring-data-jpa"))

    testFixturesImplementation(project(":media-storage:media-storage-core-model"))
    testFixturesImplementation(testFixtures(project(":graph:graph-core-model")))

    testFixturesImplementation(project(":graph:graph-ports-output"))
    testFixturesImplementation("org.springframework.data:spring-data-commons")
    testFixturesImplementation("org.springframework.data:spring-data-jpa")
    testFixturesImplementation(libs.kotest.runner)
    testFixturesImplementation(libs.forkhandles.fabrikate4k)
}
