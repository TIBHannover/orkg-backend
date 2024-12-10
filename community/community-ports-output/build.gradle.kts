// JVM Test Suite is still incubating, but expected to be stable soon, so disabling the warning.
@file:Suppress("UnstableApiUsage")

plugins {
    id("org.orkg.gradle.kotlin-library-with-test-fixtures")
}

dependencies {
    api("org.springframework.data:spring-data-commons")
    api(project(":common:identifiers"))
    api(project(":community:community-core-model"))
    testFixturesApi("org.junit.jupiter:junit-jupiter-api")
    testFixturesApi(project(":community:community-ports-output"))
    testFixturesApi(project(":eventbus"))
    testFixturesImplementation("io.kotest:kotest-assertions-shared")
    testFixturesImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
    testFixturesImplementation("org.springframework.data:spring-data-commons")
    testFixturesImplementation("io.kotest:kotest-assertions-core")
    testFixturesImplementation("io.kotest:kotest-runner-junit5")
    testFixturesImplementation(project(":common:identifiers"))
    testFixturesImplementation(project(":community:community-core-model"))
    testFixturesImplementation(project(":media-storage:media-storage-core-model"))
    testFixturesImplementation(testFixtures(project(":community:community-core-model")))
    testFixturesImplementation(testFixtures(project(":testing:spring")))
}
