// JVM Test Suite is still incubating, but expected to be stable soon, so disabling the warning.
@file:Suppress("UnstableApiUsage")

plugins {
    id("org.orkg.gradle.kotlin-library-with-test-fixtures")
}

dependencies {
    implementation(project(":common")) // for exceptions

    implementation(libs.jackson.databind)

    testFixturesImplementation(testFixtures(project(":testing:spring"))) // for fixedClock
}
