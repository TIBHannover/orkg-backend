// JVM Test Suite is still incubating, but expected to be stable soon, so disabling the warning.
@file:Suppress("UnstableApiUsage")

plugins {
    id("org.orkg.gradle.kotlin-library-with-test-fixtures")
    id("org.orkg.gradle.spring-library")
}

dependencies {
    implementation(project(":common"))
    implementation(project(":graph:graph-core-model"))

    implementation("org.springframework.data:spring-data-commons")
    implementation("org.springframework.boot:spring-boot")
    implementation("org.springframework:spring-web")
    implementation(libs.jackson.core)
    implementation(libs.forkhandles.values4k)

    testFixturesImplementation(project(":common"))
    testFixturesImplementation(project(":graph:graph-core-model"))
    testFixturesImplementation(testFixtures(project(":graph:graph-core-model")))
    testFixturesImplementation(libs.forkhandles.values4k)
}

testing {
    suites {
        val test by getting(JvmTestSuite::class) {
            dependencies {
                implementation("org.assertj:assertj-core")
                implementation(libs.kotest.runner)
            }
        }
    }
}
