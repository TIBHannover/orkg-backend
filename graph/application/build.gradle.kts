// JVM Test Suite is still incubating, but expected to be stable soon, so disabling the warning.
@file:Suppress("UnstableApiUsage")

plugins {
    id("org.orkg.kotlin-conventions")
    id("java-test-fixtures")
    alias(libs.plugins.spotless)
}

dependencies {
    api(platform(project(":platform")))

    testFixturesApi(project(":library"))
    testFixturesApi(libs.bundles.kotest)
    testFixturesApi("org.springframework.data:spring-data-commons")
    testFixturesImplementation("org.springframework.boot:spring-boot-starter-data-jpa")
    testFixturesImplementation(libs.forkhandles.fabrikate4k)
    testFixturesImplementation(libs.forkhandles.values4k)
    testFixturesImplementation(libs.bundles.jaxb)
}

testing {
    suites {
        val test by getting(JvmTestSuite::class) {
            useJUnitJupiter()
        }
    }
}
