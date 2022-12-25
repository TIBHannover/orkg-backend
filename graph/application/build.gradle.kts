// JVM Test Suite is still incubating, but expected to be stable soon, so disabling the warning.
@file:Suppress("UnstableApiUsage")

plugins {
    id("org.orkg.kotlin-conventions")
    id("java-test-fixtures")
}

dependencies {
    testFixturesApi(project(":library"))
    testFixturesApi(libs.bundles.kotest)
    testFixturesApi("org.springframework.data:spring-data-commons")
    testFixturesImplementation(libs.forkhandles.fabrikate4k)
}

testing {
    suites {
        val test by getting(JvmTestSuite::class) {
            useJUnitJupiter()
        }
    }
}