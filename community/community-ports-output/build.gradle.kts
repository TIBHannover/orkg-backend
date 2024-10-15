// JVM Test Suite is still incubating, but expected to be stable soon, so disabling the warning.
@file:Suppress("UnstableApiUsage")

plugins {
    id("org.orkg.gradle.kotlin-library-with-test-fixtures")
}

dependencies {
    api("org.springframework.data:spring-data-commons")
    api(project(":common"))
    api(project(":community:community-core-model"))
    testFixturesApi("org.junit.jupiter:junit-jupiter-api")
    testFixturesApi(project(":community:community-ports-output"))
    testFixturesApi(project(":eventbus"))
    testFixturesImplementation("io.kotest:kotest-assertions-shared")
    testFixturesImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
    testFixturesImplementation("org.springframework.data:spring-data-commons")
    testFixturesImplementation(libs.kotest.assertions.core)
    testFixturesImplementation(libs.kotest.runner)
    testFixturesImplementation(project(":common"))
    testFixturesImplementation(project(":community:community-core-model"))
    testFixturesImplementation(project(":media-storage:media-storage-core-model"))
    testFixturesImplementation(project(":testing:kotest"))
    testFixturesImplementation(testFixtures(project(":community:community-core-model")))
    testFixturesImplementation(testFixtures(project(":testing:spring")))
}
