// JVM Test Suite is still incubating, but expected to be stable soon, so disabling the warning.
@file:Suppress("UnstableApiUsage")

plugins {
    id("org.orkg.gradle.kotlin-library-with-test-fixtures")
}

dependencies {
    api(project(":common")) // for exceptions

    testFixturesImplementation(testFixtures(project(":testing:spring"))) // for fixedClock
}
