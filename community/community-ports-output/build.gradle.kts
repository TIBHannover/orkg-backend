// JVM Test Suite is still incubating, but expected to be stable soon, so disabling the warning.
@file:Suppress("UnstableApiUsage")

plugins {
    id("org.orkg.gradle.kotlin-library-with-test-fixtures")
}

dependencies {
    api(project(":community:community-core-model"))

    implementation(project(":common"))

    implementation("org.springframework.data:spring-data-commons")

    testFixturesImplementation(project(":common"))
    testFixturesImplementation(project(":testing:kotest"))
    testFixturesImplementation(project(":media-storage:media-storage-core-model"))
    testFixturesImplementation(project(":identity-management:idm-ports-output"))
    testFixturesImplementation(testFixtures(project(":identity-management:idm-core-model")))
    testFixturesImplementation(testFixtures(project(":community:community-core-model")))

    testFixturesImplementation("org.springframework.data:spring-data-commons")
    testFixturesImplementation(libs.kotest.runner)
}
