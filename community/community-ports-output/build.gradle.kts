// JVM Test Suite is still incubating, but expected to be stable soon, so disabling the warning.
@file:Suppress("UnstableApiUsage")

plugins {
    id("org.orkg.kotlin-conventions")
    id("org.orkg.neo4j-conventions")
    id("java-test-fixtures")
    kotlin("plugin.spring")
    alias(libs.plugins.spotless)
}

dependencies {
    api(platform(project(":platform")))

    implementation(project(":common"))
    implementation(project(":identity-management:idm-core-model"))
    implementation(project(":community:community-core-model"))

    implementation("org.springframework.data:spring-data-commons")

    testFixturesImplementation(project(":common"))
    testFixturesImplementation(project(":testing:kotest"))
    testFixturesImplementation(project(":identity-management:idm-core-model"))
    testFixturesImplementation(testFixtures(project(":identity-management:idm-core-model")))
    testFixturesImplementation(project(":identity-management:idm-ports-input"))
    testFixturesImplementation(project(":identity-management:idm-ports-output"))
    testFixturesImplementation(project(":community:community-core-model"))
    testFixturesImplementation(testFixtures(project(":community:community-core-model")))
    testFixturesImplementation(project(":community:community-ports-input"))
    testFixturesImplementation(project(":community:community-ports-output"))
    testFixturesImplementation(project(":community:community-adapter-output-spring-data-jpa"))

    testFixturesImplementation(project(":graph:graph-core-model"))
    testFixturesImplementation(project(":graph:graph-ports-output"))
    testFixturesImplementation(project(":media-storage:media-storage-ports-input"))
    testFixturesImplementation(project(":media-storage:media-storage-core-model"))

    testFixturesImplementation("org.springframework.data:spring-data-commons:2.7.16")
    testFixturesImplementation("org.springframework.data:spring-data-jpa")
    testFixturesImplementation(libs.kotest.runner)
}
