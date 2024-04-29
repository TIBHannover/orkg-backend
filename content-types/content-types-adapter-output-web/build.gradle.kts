// JVM Test Suite is still incubating, but expected to be stable soon, so disabling the warning.
@file:Suppress("UnstableApiUsage")

plugins {
    id("org.orkg.gradle.kotlin-library-with-test-fixtures")
    id("org.orkg.gradle.spring-library")
}

dependencies {
    implementation(project(":content-types:content-types-ports-output"))

    implementation(project(":graph:graph-core-model"))

    implementation("org.springframework.data:spring-data-commons")
    implementation("org.springframework:spring-web")
    implementation(libs.jackson.core)
    implementation(libs.forkhandles.values4k)
}

testing {
    suites {
        val test by getting(JvmTestSuite::class) {
            dependencies {
                implementation(testFixtures(project(":content-types:content-types-ports-output")))
                implementation("org.assertj:assertj-core")
                implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
                implementation(libs.spring.mockk)
                implementation(libs.kotest.runner)
            }
        }
    }
}
