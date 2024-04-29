// JVM Test Suite is still incubating, but expected to be stable soon, so disabling the warning.
@file:Suppress("UnstableApiUsage")

plugins {
    id("org.orkg.gradle.kotlin-library-with-test-fixtures")
    id("org.orkg.gradle.spring-library")
}

dependencies {
    implementation(project(":content-types:content-types-ports-output"))

    implementation(project(":common"))
    implementation(project(":graph:graph-core-model"))
    implementation(project(":community:community-core-model"))

    implementation("org.springframework.data:spring-data-commons")
    implementation("org.springframework:spring-web")
    implementation(libs.jackson.core)
    implementation(libs.forkhandles.values4k)

    testFixturesImplementation(project(":common"))
    testFixturesImplementation(project(":graph:graph-core-model"))
    testFixturesImplementation(project(":community:community-core-model"))
    testFixturesImplementation(testFixtures(project(":content-types:content-types-core-model")))
}

testing {
    suites {
        val test by getting(JvmTestSuite::class) {
            dependencies {
                implementation(testFixtures(project(":content-types:content-types-ports-output")))
                implementation(project(":common:serialization"))
                implementation(project(":graph:graph-adapter-input-rest-spring-mvc"))
                implementation("org.assertj:assertj-core")
                implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
                implementation("org.springframework:spring-test")
                implementation(libs.spring.mockk)
                implementation(libs.kotest.runner)
            }
        }
    }
}
